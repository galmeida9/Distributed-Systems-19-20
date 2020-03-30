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

    public static class ObservationObject {
        private String type;
        private String id;
        private LocalDateTime datetime;
        private String camName;
        
        
        public ObservationObject(String type, String id, LocalDateTime datetime, String camName){
            this.type = type;
            this.id = id;
            this.datetime = datetime;
            this.camName = camName;
        }
    }

    public static class ObservationInfoObject {
        private ObservationObject obs;
        private double lat;
        private double lon;

        public ObservationInfoObject(ObservationObject obs, double lat, double lon) {
            this.obs = obs;
            this.lat = lat;
            this.lon = lon;
        }
    }

    enum ResponseStatus {
        OK,
        ID_DUPLICATED,
        NOK
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

    private List<ObservationObject> convertObservations(List<Observation> oldObs){
        return oldObs.stream()
                .map(x -> new ObservationObject( 
                    getStrFromType(x.getType()), 
                    x.getId(), 
                    convertToLocalDateTime(x.getDateTime()),
                    x.getCamName()))
                .collect(Collectors.toList());
    }

    private List<ObservationInfoObject> convertObservationInfoList(List<ObservationInfo> oldObs){
        return oldObs.stream()
                .map(x -> new ObservationInfoObject( 
                    convertToObservation(x, x.getObs().getCamName()),
                    x.getCoords().getLat(),
                    x.getCoords().getLong()))
                .collect(Collectors.toList());
    }

    private ObservationObject convertToObservation(ObservationInfo observation, String camName) {
        return new ObservationObject(
            getStrFromType(observation.getObs().getType()),
            observation.getObs().getId(),
            convertToLocalDateTime(observation.getObs().getDateTime()),
            camName
            );
    }

    // Exception for Invalid object type
    public class InvalidTypeException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidTypeException(String message){
            super(message);
        }
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
        switch (type){
            case PERSON:
                return "person";
            case CAR:
                return "car";
            // Only happens if this is not updated with proto
            default:
                return null;
                //throw new InvalidTypeException("Type \'" + type.toString() + "\' is incorrect.'");
        }
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
                    .setType(getTypeFromStr(observation.type))
                    .setId(observation.id)
                    .setDateTime(convertToTimeStamp(observation.datetime)));
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
    
    public List<ObservationInfoObject> trackMatch(String type, String id) throws InvalidTypeException {
        TrackMatchRequest.Builder request = TrackMatchRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setPartialId(id);        
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

    private LocalDateTime convertToLocalDateTime(Timestamp ts) {
        return LocalDateTime.ofEpochSecond(ts.getSeconds(), ts.getNanos(), ZoneOffset.UTC);
    }

    private Timestamp convertToTimeStamp(LocalDateTime dt) {
        return Timestamp.newBuilder().setSeconds(dt.toEpochSecond(ZoneOffset.UTC))
                        .setNanos(dt.getNano())
                        .build();
    }
}

