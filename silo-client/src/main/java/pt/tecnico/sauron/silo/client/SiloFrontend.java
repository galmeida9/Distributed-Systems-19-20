package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.exceptions.*;
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

    public SiloFrontend(String zooHost, String zooPort, int instance) throws FailedConnectionException {
        String path = "/grpc/sauron/silo";
        zkNaming = new ZKNaming(zooHost, zooPort);

        if (instance >= 0) {
            path = path + '/' + Integer.toString(instance);
            try {
                record = zkNaming.lookup(path);
            } catch (ZKNamingException e) {
                throw new FailedConnectionException("Server with instance number " + instance + " not found.");
            }
        }
        else {
            try {
                Collection servers = zkNaming.listRecords(path);
                if (servers.isEmpty()) {
                    throw new FailedConnectionException("No server is on");
                }
                int num = (int) (Math.random()*servers.size());
                record = (ZKRecord) servers.toArray()[num];
            }
            catch (ZKNamingException e) {
                throw new FailedConnectionException(e.getMessage());
            }
        }

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

    
    /** 
     * Get a list of the valid types for the observations
     * @return List<String>
     */
    public static List<String> getValidTypes(){
        List<String> res = new ArrayList<>();
        for (TypeObject t : TypeObject.values()) res.add(t.toString().toLowerCase());
        return res;
    }

    /*
    *   Debug
    */
    private static final boolean DEBUG_FLAG = "true".equals(System.getenv("debug"));

    
    /** 
     * @param debugMessage
     */
    private static void debug(String debugMessage){
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    /*
    *   Public methods - server related
    */

    /** 
     * Sends camJoin request to server
     * @param camName
     * @param lat
     * @param lon
     * @throws InvalidCameraArgumentsException
     * @throws FailedConnectionException
     */
    public void camJoin(String camName, double lat, double lon) throws InvalidCameraArgumentsException, FailedConnectionException {
        try {
            Coordinates coords = Coordinates.newBuilder().setLat(lat).setLong(lon).build();
            stub.camJoin(CamJoinRequest.newBuilder().setCamName(camName).setCoordinates(coords).build());
        }
        catch (NullPointerException e) {
            throw new InvalidCameraArgumentsException(e.getMessage());
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new InvalidCameraArgumentsException(e.getMessage());
        }
    }

    
    /** 
     * Sends camInfo request to server
     * @param camName
     * @return String
     * @throws CameraNotFoundException
     * @throws FailedConnectionException
     */
    public String camInfo(String camName) throws CameraNotFoundException, FailedConnectionException {
        try {
            CamInfoResponse response = stub.camInfo(CamInfoRequest.newBuilder().setCamName(camName).build());
            double lat = response.getCoordinates().getLat();
            double lon = response.getCoordinates().getLong();
            return lat + "," + lon;
        }
        catch (NullPointerException e) {
            throw new CameraNotFoundException(e.getMessage());
        }
        catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new CameraNotFoundException(e.getMessage());
        }

    }

    
    /** 
     * Send report request to server
     * @param observations
     * @throws InvalidTypeException
     * @throws ReportException
     * @throws FailedConnectionException
     */
    public void report(List<ObservationObject> observations) throws InvalidTypeException, ReportException, FailedConnectionException {
        try{
            ReportRequest.Builder request = ReportRequest.newBuilder();

            for (ObservationObject observation : observations){
                request.addObservation(Observation.newBuilder()
                        .setType(getTypeFromStr(observation.getType()))
                        .setId(observation.getId())
                        .setCamName(observation.getCamName()));
            }

            stub.report(request.build());
        } catch (StatusRuntimeException e){
            checkConnection(e.getStatus());
            throw new ReportException(e.getMessage());
        }
    }

    
    /** 
     * Send track request to server
     * @param type
     * @param id
     * @return ObservationObject
     * @throws InvalidTypeException
     * @throws NoObservationsFoundException
     * @throws FailedConnectionException
     */
    public ObservationObject track(String type, String id) throws InvalidTypeException, NoObservationsFoundException, FailedConnectionException {
        try{
            TypeObject enumType = getTypeFromStr(type);
            TrackResponse response = stub.track(TrackRequest.newBuilder().setType(enumType).setId(id).build());
            return convertObservation(response.getObservation());
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new NoObservationsFoundException(e.getMessage());
        }
    }
    
    
    /** 
     * Send trackMatch request to server
     * @param type
     * @param partId
     * @return List<ObservationObject>
     * @throws InvalidTypeException
     * @throws NoObservationsFoundException
     * @throws FailedConnectionException
     */
    public List<ObservationObject> trackMatch(String type, String partId)
            throws InvalidTypeException, NoObservationsFoundException, FailedConnectionException {
        try {
            TrackMatchRequest.Builder request = TrackMatchRequest.newBuilder();

            request.setType(getTypeFromStr(type));
            request.setPartialId(partId);        
            TrackMatchResponse response = stub.trackMatch(request.build());
        
            return convertObservationList(response.getObservationList());
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new NoObservationsFoundException(e.getMessage());
        }
    }

    
    /** 
     * Send trace request to server
     * @param type
     * @param id
     * @return List<ObservationObject>
     * @throws InvalidTypeException
     * @throws NoObservationsFoundException
     * @throws FailedConnectionException
     */
    public List<ObservationObject> trace(String type, String id)
            throws InvalidTypeException, NoObservationsFoundException, FailedConnectionException {
        try {
            TraceRequest.Builder request = TraceRequest.newBuilder();

            request.setType(getTypeFromStr(type));
            request.setId(id);
        
            TraceResponse response = stub.trace(request.build());

            return convertObservationList(response.getObservationList());
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new NoObservationsFoundException(e.getMessage());
        }
    }

    /* 
    *   Control operations
    */

    /** 
     * Send ctrlPing request to server
     * @param input
     * @return String
     * @throws FailedConnectionException
     */
    public String ctrlPing(String input) throws FailedConnectionException {
        try {
            CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput(input).build();
            CtrlPingResponse response = stub.ctrlPing(request);
            return response.getOutput();
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new FailedConnectionException("Failed to connect to server.");
        }
    }

    
    /** 
     * Send ctrlClear request to server
     * @throws CannotClearServerException
     * @throws FailedConnectionException
     */
    public void ctrlClear() throws CannotClearServerException, FailedConnectionException {
        try {
            CtrlClearResponse.newBuilder().build();
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
            throw new CannotClearServerException("Could clear the server.");
        }
    }

    
    /** 
     * Send ctrlInit request to server
     * @throws FailedConnectionException
     */
    public void ctrlInit() throws FailedConnectionException {
        try {
            CtrlClearResponse.newBuilder().build();
        } catch (StatusRuntimeException e) {
            checkConnection(e.getStatus());
        }
    }

    
    /*
    *   Auxiliary functions
    */

    /** 
     * Converts com.google.protobuf.TimeStamp in LocalDateTime
     * @param ts
     * @return LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Timestamp ts) {
        return LocalDateTime.ofEpochSecond(ts.getSeconds(), ts.getNanos(), ZoneOffset.UTC);
    }

    
    /** 
     * Converts lists of grpc Observation in list of ObservationObject
     * @param oldObs
     * @return List<ObservationObject>
     */
    private List<ObservationObject> convertObservationList(List<Observation> oldObs){
        return oldObs.stream()
                .map(x -> convertObservation(x))
                .collect(Collectors.toList());
    }

    
    /** 
     * Converts grpc Observation to ObservationObject, client's equivalent class
     * @param obs
     * @return ObservationObject
     */
    private ObservationObject convertObservation(Observation obs) {
        return new ObservationObject(getStrFromType(obs.getType()), obs.getId(), 
                                    convertToLocalDateTime(obs.getDateTime()), obs.getCamName());
    }

    
    /** 
     * Converts a type from a String into a TypeObject
     * @param type
     * @return TypeObject
     * @throws InvalidTypeException
     */
    private TypeObject getTypeFromStr(String type) throws InvalidTypeException {
        switch (type){
            case "person":
                return TypeObject.PERSON;
            case "car":
                return TypeObject.CAR;
            default:
                throw new InvalidTypeException("Type '" + type + "' is incorrect.'");
        }
    }

    
    /** 
     * Converts TypeObject class into string
     * @param type
     * @return String
     */
    private String getStrFromType(TypeObject type) {
        if (type == TypeObject.PERSON) return "person";
        return "car";
    }

    
    /** 
     * Checks connection with the server
     * @param s
     * @throws FailedConnectionException
     */
    private void checkConnection(Status s) throws FailedConnectionException {
        if (s.getCode().equals(Status.UNAVAILABLE.getCode())) {
            throw new FailedConnectionException("Server is unavailable.");
        }
    }
}

