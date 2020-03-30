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
        
        
        public ObservationObject(String type, String id, LocalDateTime datetime ){
            this.type = type;
            this.id = id;
            this.datetime = datetime;
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
                    convertToLocalDateTime(x.getDateTime())))
                .collect(Collectors.toList());
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

    public ObservationObject track(String type, String id) throws InvalidTypeException {
        TypeObject enumType = getTypeFromStr(type);
        TrackResponse response = stub.track(TrackRequest.newBuilder().setType(enumType).setId(id).build());
        return new ObservationObject(
            getStrFromType(response.getObservation().getType()),
            response.getObservation().getId(),
            convertToLocalDateTime(response.getObservation().getDateTime())
        );
    }
    
    public List<ObservationObject> trackMatch(String type, String id) throws InvalidTypeException {
        TrackMatchRequest.Builder request = TrackMatchRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setPartialId(id);        
        TrackMatchResponse response = stub.trackMatch(request.build());
        
        return convertObservations(response.getObservationList());
    }

    public List<ObservationObject> trace(String type, String id) throws InvalidTypeException {
        TraceRequest.Builder request = TraceRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setId(id);
        
        TraceResponse response = stub.trace(request.build());

        return convertObservations(response.getObservationList());
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

