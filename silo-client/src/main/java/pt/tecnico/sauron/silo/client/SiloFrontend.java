package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.client.exceptions.*;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SiloFrontend {

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;
    private ZKNaming zkNaming;
    private ZKRecord record;
    private int retries = 0;
    private int retryTime = 5000;

    public SiloFrontend(String zooHost, String zooPort, int instance) throws FailedConnectionException {
        zkNaming = new ZKNaming(zooHost, zooPort);
        connectToServer(instance);
    }

    private SiloFrontend connectToServer(int instance) throws FailedConnectionException {
        String path = "/grpc/sauron/silo";
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
                Collection<ZKRecord> servers = zkNaming.listRecords(path);
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
        return this;
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
            stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS)
                .camJoin(CamJoinRequest.newBuilder().setCamName(camName).setCoordinates(coords).build());
            retries = 0;
        }
        catch (NullPointerException e) {
            throw new InvalidCameraArgumentsException(e.getMessage());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("camJoin", String.class, Double.class, Double.class), this, camName, lat, lon);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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
            CamInfoResponse response = stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS)
                                            .camInfo(CamInfoRequest.newBuilder().setCamName(camName).build());
            retries = 0;
            double lat = response.getCoordinates().getLat();
            double lon = response.getCoordinates().getLong();
            return lat + "," + lon;
        }
        catch (NullPointerException e) {
            throw new CameraNotFoundException(e.getMessage());
        }
        catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("camInfo", String.class), this, camName);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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

            stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS).report(request.build());
            retries = 0;
        } catch (StatusRuntimeException e){
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("report", List.class), this, observations);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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
            TrackResponse response = stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS)
                                            .track(TrackRequest.newBuilder().setType(enumType).setId(id).build());
            retries = 0;                                            
            return convertObservation(response.getObservation());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("track", String.class, String.class), this, type, id);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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
            TrackMatchResponse response = stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS).trackMatch(request.build());
            retries = 0;
        
            return convertObservationList(response.getObservationList());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("trackMatch", String.class, String.class), this, type, partId);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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
        
            TraceResponse response = stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS).trace(request.build());
            retries = 0;

            return convertObservationList(response.getObservationList());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("trace", String.class, String.class), this, type, id);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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
            CtrlPingResponse response = stub.withDeadlineAfter(retryTime, TimeUnit.MILLISECONDS).ctrlPing(request);
            retries = 0;
            return response.getOutput();
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
                try {
                    increaseRetries(SiloFrontend.class.getMethod("ctrlPing", String.class), this, input);
                } catch (NoSuchMethodException ex) {
                    // impossible
                }
            }
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

    private void increaseRetries(Method func, SiloFrontend frontend , Object... args) throws FailedConnectionException {
        retries++;
        System.out.println("Retrying request.");
        try {
            if (retries > 2) {
                System.out.println("Failed to retry request, changing server and trying again.");
                retries = 0;
                channel.shutdown();
                func.invoke(connectToServer(-1), args);
            }
            else 
                func.invoke(frontend, args);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            System.out.println("djkfhsdkjfhs");
            throw new FailedConnectionException("Failed to retry request1111.");
        }
    }
}

