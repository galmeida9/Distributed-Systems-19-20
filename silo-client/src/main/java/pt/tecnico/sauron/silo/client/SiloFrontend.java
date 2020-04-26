package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import com.google.protobuf.Timestamp;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloFrontend {

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;
    private ZKNaming zkNaming;
    private ZKRecord record;

    public SiloFrontend(String zooHost, String zooPort, int instance) {
        String path = "/grpc/sauron/silo";
        zkNaming = new ZKNaming(zooHost, zooPort);

        if (instance >= 0) {
            path = path + '/' + Integer.toString(instance);
            try {
                record = zkNaming.lookup(path);
            } catch (ZKNamingException e) {
                //FIXME:
                e.printStackTrace();
            }
        }
        else if (instance < 0) {
            try {
                Collection servers = zkNaming.listRecords(path);
                if (servers.isEmpty()) {
                    //FIXME:
                    System.out.println("No server is on");
                    return;
                }
                int num = (int) (Math.random()*servers.size());
                record = (ZKRecord) servers.toArray()[num];
            }
            catch (ZKNamingException e) {
                //FIXME:
                e.printStackTrace();
            }
        }

        if (record == null) return;

        //lookup
        String target = record.getURI();
        debug("Target: " + target);
        
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SiloGrpc.newBlockingStub(channel);
    }

    /*
    * Method to shutdown the channel when exiting.
    */
    public void exit(){
        if (channel != null) channel.shutdown();
    }

    // Get a list of the valid types for the observations
    public static List<String> getValidTypes(){
        List<String> res = new ArrayList<>();
        for (TypeObject t : TypeObject.values()) res.add(t.toString().toLowerCase());
        return res;
    }

    /*
    *   Debug
    */

    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    private static void debug(String debugMessage){
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    /*
    *   Public methods - server related
    */
    public void camJoin(String camName, double lat, double lon) throws InvalidCameraArgumentsException {
        try {
            Coordinates coords = Coordinates.newBuilder().setLat(lat).setLong(lon).build();
            stub.camJoin(CamJoinRequest.newBuilder().setCamName(camName).setCoordinates(coords).build());
        } catch (RuntimeException e) {
            throw new InvalidCameraArgumentsException(e.getMessage());
        }
    }

    public String camInfo(String camName) throws CameraNotFoundException {
        try {
            CamInfoResponse response = stub.camInfo(CamInfoRequest.newBuilder().setCamName(camName).build());
            double lat = response.getCoordinates().getLat();
            double lon = response.getCoordinates().getLong();
            return String.valueOf(lat + "," + lon);
        }
        catch (RuntimeException e) {
            throw new CameraNotFoundException(e.getMessage());
        }

    }

    public void report(List<ObservationObject> observations) throws InvalidTypeException, ReportException {
        try{
            ReportRequest.Builder request = ReportRequest.newBuilder();

            for (ObservationObject observation : observations){
                request.addObservation(Observation.newBuilder()
                        .setType(getTypeFromStr(observation.getType()))
                        .setId(observation.getId())
                        .setCamName(observation.getCamName()));
            }

            stub.report(request.build());
        } catch (RuntimeException e){
            throw new ReportException(e.getMessage());
        }
    }

    public ObservationObject track(String type, String id) throws InvalidTypeException, NoObservationsFoundException {
        try{
            TypeObject enumType = getTypeFromStr(type);
            TrackResponse response = stub.track(TrackRequest.newBuilder().setType(enumType).setId(id).build());
            return convertObservation(response.getObservation());
        } catch (RuntimeException e) {
            throw new NoObservationsFoundException(e.getMessage());
        }
    }
    
    public List<ObservationObject> trackMatch(String type, String partId) throws InvalidTypeException, NoObservationsFoundException {
        try {
            TrackMatchRequest.Builder request = TrackMatchRequest.newBuilder();

            request.setType(getTypeFromStr(type));
            request.setPartialId(partId);        
            TrackMatchResponse response = stub.trackMatch(request.build());
        
            return convertObservationList(response.getObservationList());
        } catch (RuntimeException e) {
            throw new NoObservationsFoundException(e.getMessage());
        }
    }

    public List<ObservationObject> trace(String type, String id) throws InvalidTypeException, NoObservationsFoundException {
        try {
            TraceRequest.Builder request = TraceRequest.newBuilder();

            request.setType(getTypeFromStr(type));
            request.setId(id);
        
            TraceResponse response = stub.trace(request.build());

            return convertObservationList(response.getObservationList());
        } catch (RuntimeException e) {
            throw new NoObservationsFoundException(e.getMessage());
        }
    }

    /* 
    *   Control operations
    */

    public String ctrlPing(String input){
        CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput(input).build();
        
        CtrlPingResponse response = stub.ctrlPing(request);
        
        return response.getOutput();
    }

    public void ctrlClear() throws CannotClearServerException {
        try {
            CtrlClearResponse.newBuilder().build();
        } catch (RuntimeException e) {
            throw new CannotClearServerException("Could clear the server.");
        }
    }

    public void ctrlInit(){
        CtrlClearResponse.newBuilder().build();
    }

    /*
    *   Auxiliary functions
    */

    // Converts com.google.protobuf.TimeStamp in LocalDateTime
    private LocalDateTime convertToLocalDateTime(Timestamp ts) {
        return LocalDateTime.ofEpochSecond(ts.getSeconds(), ts.getNanos(), ZoneOffset.UTC);
    }

    // Converts lists of grpc Observation in list of ObservationObject
    private List<ObservationObject> convertObservationList(List<Observation> oldObs){
        return oldObs.stream()
                .map(x -> convertObservation(x))
                .collect(Collectors.toList());
    }

    private ObservationObject convertObservation(Observation obs) {
        return new ObservationObject(getStrFromType(obs.getType()), obs.getId(), 
                                    convertToLocalDateTime(obs.getDateTime()), obs.getCamName());
    }

    private TypeObject getTypeFromStr(String type) throws InvalidTypeException {
        switch (type){
            case "person":
                return TypeObject.PERSON;
            case "car":
                return TypeObject.CAR;
            default:
                throw new InvalidTypeException("Type \'" + type + "\' is incorrect.'");
        }
    }

    private String getStrFromType(TypeObject type) {
        if (type == TypeObject.PERSON) return "person";
        return "car";
    }
}

