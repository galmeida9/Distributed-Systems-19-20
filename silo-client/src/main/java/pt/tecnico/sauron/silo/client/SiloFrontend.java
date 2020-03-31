package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import com.google.protobuf.Timestamp;

public class SiloFrontend {

    private ManagedChannel channel;
    private SiloGrpc.SiloBlockingStub stub;

    public SiloFrontend(String host, String port) {
        String target = host + ":" + Integer.parseInt(port);
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
    *   Contract related classes and methods
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

    public String camInfo(String camName){
        CamInfoResponse response = stub.camInfo(CamInfoRequest.newBuilder().setCamName(camName).build());
        return String.valueOf(response.getCoordinates().getLat()) + String.valueOf(response.getCoordinates().getLong());
    }

    public ResponseStatus report(String camName, List<ObservationObject> observations) throws InvalidTypeException {
        ReportRequest.Builder request = ReportRequest.newBuilder().setCamName(camName);  

        for (ObservationObject observation : observations){
            request.addObservation(Observation.newBuilder()
                    .setType(getTypeFromStr(observation.getType()))
                    .setId(observation.getId()));
        }
        
        ReportResponse response = stub.report(request.build());
        return getStatus(response.getStatus());
    }

    public ObservationInfoObject track(String type, String id) throws InvalidTypeException {
        TypeObject enumType = getTypeFromStr(type);
        TrackResponse response = stub.track(TrackRequest.newBuilder().setType(enumType).setId(id).build());
        ObservationInfo observation = response.getObservation();
        ObservationObject obs = convertToObservation(observation, observation.getObs().getCamName());
        return new ObservationInfoObject(
                obs, 
                observation.getCoords().getLat(), 
                observation.getCoords().getLong()
            );
    }
    
    public List<ObservationInfoObject> trackMatch(String type, String partId) throws InvalidTypeException {
        TrackMatchRequest.Builder request = TrackMatchRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setPartialId(partId);        
        TrackMatchResponse response = stub.trackMatch(request.build());
        
        return convertObservationInfoList(response.getObservationList());
    }

    public List<ObservationInfoObject> trace(String type, String id) throws InvalidTypeException {
        TraceRequest.Builder request = TraceRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setId(id);
        
        TraceResponse response = stub.trace(request.build());

        return convertObservationInfoList(response.getObservationList());
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

    // Converts LocalDateTime in com.google.protobuf.TimeStamp
    private Timestamp convertToTimeStamp(LocalDateTime dt) {
        return Timestamp.newBuilder().setSeconds(dt.toEpochSecond(ZoneOffset.UTC))
                        .setNanos(dt.getNano())
                        .build();
    }

    // Converts lists of grpc Observation in list of ObservationObject
    private List<ObservationObject> convertObservations(List<Observation> oldObs){
        return oldObs.stream()
                .map(x -> new ObservationObject( 
                    getStrFromType(x.getType()), 
                    x.getId(), 
                    convertToLocalDateTime(x.getDateTime()),
                    x.getCamName()))
                .collect(Collectors.toList());
    }

    // Converts lists of grpc ObservationInfo in list of ObservationInfoObject
    private List<ObservationInfoObject> convertObservationInfoList(List<ObservationInfo> oldObs){
        return oldObs.stream()
                .map(x -> new ObservationInfoObject( 
                    convertToObservation(x, x.getObs().getCamName()),
                    x.getCoords().getLat(),
                    x.getCoords().getLong()))
                .collect(Collectors.toList());
    }

    // Converts grpc ObservationInfo in ObservationObject
    private ObservationObject convertToObservation(ObservationInfo observation, String camName) {
        return new ObservationObject(
            getStrFromType(observation.getObs().getType()),
            observation.getObs().getId(),
            convertToLocalDateTime(observation.getObs().getDateTime()),
            camName
            );
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

