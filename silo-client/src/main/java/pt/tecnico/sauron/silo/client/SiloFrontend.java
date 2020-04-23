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
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class SiloFrontend {

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;
    private ZKNaming zkNaming;
    private ZKRecord record;

    public SiloFrontend(String zooHost, String zooPort, int instance) {

        String path = "/grpc/sauron/silo/";

        if (instance >= 0) path = path + Integer.toString(instance);
        if (instance < 0) {
            //TODO: If instance not given, choose randomly
        }
        zkNaming = new ZKNaming(zooHost, zooPort);

        //TODO: Check for server not connected
        //lookup
        try {
            record = zkNaming.lookup(path);
        } catch (Exception e) {
            //FIXME: Bad catching
            System.out.println(e.getMessage());
        }

        String target = record.getURI();
        debug("Target: " + target);
        
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = SiloGrpc.newBlockingStub(channel);
    }

    /*
    * Method to shutdown the channel when exiting.
    */
    public void exit(){
        channel.shutdown();
    }

    /* 
    *   Contract related classes
    */

    public enum ResponseStatus {
        OK,
        ID_DUPLICATED,
        NOK
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
    public ResponseStatus camJoin(String camName, double lat, double lon){
        Coordinates coords = Coordinates.newBuilder().setLat(lat).setLong(lon).build();
        CamJoinResponse response = stub.camJoin(CamJoinRequest.newBuilder().setCamName(camName).setCoordinates(coords).build());
        return getStatus(response.getStatus());
    }

    public String camInfo(String camName) throws CameraNotFoundException {
        try {
            CamInfoResponse response = stub.camInfo(CamInfoRequest.newBuilder().setCamName(camName).build());
            return String.valueOf(response.getCoordinates().getLat()) + "," + String.valueOf(response.getCoordinates().getLong());
        }
        catch (RuntimeException e) {
            throw new CameraNotFoundException(e.getMessage());
        }

    }

    public ResponseStatus report(List<ObservationObject> observations) throws InvalidTypeException, ReportException {
        try{
            ReportRequest.Builder request = ReportRequest.newBuilder();

            for (ObservationObject observation : observations){
                request.addObservation(Observation.newBuilder()
                        .setType(getTypeFromStr(observation.getType()))
                        .setId(observation.getId())
                        .setCamName(observation.getCamName()));
            }

            ReportResponse response = stub.report(request.build());
            return getStatus(response.getStatus());
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

    public ResponseStatus ctrlClear(){
        CtrlClearResponse response = CtrlClearResponse.newBuilder().build();
        return getStatus(response.getStatus());
    }

    public ResponseStatus ctrlInit(){
        CtrlClearResponse response = CtrlClearResponse.newBuilder().build();
        return getStatus(response.getStatus());
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

    private ResponseStatus getStatus(Status status){
        switch (status){
            case OK:
                return ResponseStatus.OK;
            case ID_DUPLICATED:
                return ResponseStatus.ID_DUPLICATED;
            case NOK:
            default:
                return ResponseStatus.NOK;
        }
    }
}

