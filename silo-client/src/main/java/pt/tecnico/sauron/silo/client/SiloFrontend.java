package pt.tecnico.sauron.silo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sauron.silo.grpc.*;

import java.util.*;
import java.util.stream.Collectors;

import com.google.protobuf.Timestamp;

public class SiloFrontend {

    private ManagedChannel _channel;
    private SiloGrpc.SiloBlockingStub _stub;

    public SiloFrontend(String host, String port) {
		String target = host + ":" + Integer.parseInt(port);
        debug("Target: " + target);
        
		_channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
	    _stub = SiloGrpc.newBlockingStub(_channel);
    }

    /* 
    *   Contract related classes and methods
    */

    public class ObservationObject {
        private String _type;
        private String _id;
        private Timestamp _timestamp;
        
        
        ObservationObject(String type, String id, Timestamp timestamp ){
            _type = type;
            _id = id;
            _timestamp = timestamp;
        }
    }

    private String getStatus(Status status){
        switch (status){
            case OK:
                return "Operation Succeeded.";
            case ID_DUPLICATED:
                return "Duplicated ID, could not resolve operation.";
            case NOK:
            default:
                return "An error occurred during the operation.";
        }
    }

    private List<ObservationObject> convertObservations(List<Observation> oldObs){
        return oldObs.stream()
                .map(x -> new ObservationObject( 
                    getStrFromType(x.getType()), x.getId(), x.getDateTime()))
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
    public String camJoin(String camName, double lat, double lon){
        Coordinates coords = Coordinates.newBuilder().setLat(lat).setLong(lon).build();
        CamJoinResponse response = _stub.camJoin(CamJoinRequest.newBuilder().setCamName(camName).setCoordinates(coords).build());
        return getStatus(response.getStatus());
    }

    public String camInfo(String camName){
        CamInfoResponse response = _stub.camInfo(CamInfoRequest.newBuilder().setCamName(camName).build());
        return String.valueOf(response.getCoordinates().getLat()) + String.valueOf(response.getCoordinates().getLong());
    }

    public String report(String camName, List<ObservationObject> observations) throws InvalidTypeException {
        ReportRequest.Builder request = ReportRequest.newBuilder().setCamName(camName);  

        for (ObservationObject observation : observations){
            request.addObservation(Observation.newBuilder().setType(getTypeFromStr(observation._type)).setId(observation._id).setDateTime(observation._timestamp));
        }
        
        ReportResponse response = _stub.report(request.build());
        return getStatus(response.getStatus());
    }

    public ObservationObject track(String type, String id) throws InvalidTypeException {
        TypeObject enumType = getTypeFromStr(type);
        TrackResponse response = _stub.track(TrackRequest.newBuilder().setType(enumType).setId(id).build());
        return new ObservationObject(
            getStrFromType(response.getObservation().getType()),
            response.getObservation().getId(),
            response.getObservation().getDateTime()
        );
    }
    
    public List<ObservationObject> trackMatch(String type, String id) throws InvalidTypeException {
        TrackMatchRequest.Builder request = TrackMatchRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setPartialId(id);        
        TrackMatchResponse response = _stub.trackMatch(request.build());
        
        return convertObservations(response.getObservationList());
    }

    public List<ObservationObject> trace(String type, String id) throws InvalidTypeException {
        TraceRequest.Builder request = TraceRequest.newBuilder();

        request.setType(getTypeFromStr(type));
        request.setId(id);
        
        TraceResponse response = _stub.trace(request.build());

        return convertObservations(response.getObservationList());
    }

    /* 
    *   Control operations
    */

    public String ctrl_ping(String input){
        CtrlPingRequest request = CtrlPingRequest.newBuilder().setInput(input).build();
        
        CtrlPingResponse response = _stub.ctrlPing(request);
        
        return response.getOutput();
    }

    public String ctrl_clear(){
        CtrlClearResponse response = CtrlClearResponse.newBuilder().build();
        return getStatus(response.getStatus());
    }

    public String ctrl_init(){
        CtrlClearResponse response = CtrlClearResponse.newBuilder().build();
        return getStatus(response.getStatus());
    }

}

