package pt.tecnico.sauron.silo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.grpc.*;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

	private SiloBackend backend = new SiloBackend();

	/* Functionality operations */

	@Override
	public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver){

		boolean res = backend.camJoin(request.getCamName(), request.getCoordinates().getLat(), request.getCoordinates().getLong());

		CamJoinResponse.Builder response = CamJoinResponse.newBuilder();
		if (res) response.setStatus(Status.OK);
		else response.setStatus(Status.NOK);

		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
		List<Double> listCoords;
		try {
			listCoords = backend.camInfo(request.getCamName());
		}
		catch (CameraNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
		Coordinates coords = Coordinates.newBuilder()
				.setLat(listCoords.get(0))
				.setLong(listCoords.get(1))
				.build();
		CamInfoResponse response = CamInfoResponse.newBuilder().setCoordinates(coords).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
		try{
			List<ObservationEntity> obsEntity = new ArrayList<>();
			List<Observation> obs = request.getObservationList();
			for (Observation observation : obs){
				obsEntity.add(convertToObsEntity(observation));
			}

			boolean res = backend.report(request.getObservation(0).getCamName(), obsEntity);

			ReportResponse.Builder response = ReportResponse.newBuilder();
			if (res) response.setStatus(Status.OK);
		    else response.setStatus(Status.NOK);
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		} catch (CameraNotFoundException | InvalidIdException e){
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	@Override
	public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {
		try {
			ObservationEntity obs = backend.track(convertToObsEntityType(request.getType()), request.getId());
			Observation obsResponse = convertToObservation(obs);
			TrackResponse response = TrackResponse.newBuilder().setObservation(obsResponse).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidIdException | NoObservationsException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
		try {
			List<ObservationEntity> obs = backend.trackMatch(convertToObsEntityType(request.getType()), request.getPartialId());
			TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();

			for (ObservationEntity observation: obs) {
				response.addObservation(convertToObservation(observation));
			}

			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		} catch (InvalidIdException | NoObservationsException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {
		try{
			List<ObservationEntity> obs = backend.trace(convertToObsEntityType(request.getType()), request.getId());
			TraceResponse.Builder response = TraceResponse.newBuilder();

			for (ObservationEntity observation : obs) {
				response.addObservation(convertToObservation(observation));
			}
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		} catch (InvalidIdException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/* Control operations */

	@Override
	public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {
		// FIXME: Check if this is right

		String input = request.getInput();
		String output = "Hello " + input + "!";
		CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearResponse> responseObserver) {
		boolean res = backend.ctrlClear();

		CtrlClearResponse.Builder response = CtrlClearResponse.newBuilder();
		if (res) response.setStatus(Status.OK);
		else response.setStatus(Status.NOK);

		responseObserver.onNext(response.build());
		responseObserver.onCompleted();
	}

	@Override
	public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitResponse> responseObserver) {
		// TODO:
		CtrlInitResponse response = CtrlInitResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	private Observation convertToObservation(ObservationEntity observation) {
		return Observation.newBuilder()
						.setType(convertToType(observation.getType()))
						.setId(observation.getId())
						.setDateTime(convertToTimeStamp(observation.getDateTime()))
						.setCamName(observation.getCamName())
						.build();
	}


	private TypeObject convertToType(ObservationEntity.ObservationEntityType type) throws RuntimeException {
		switch (type) {
			case PERSON:
				return TypeObject.PERSON;
			case CAR:
				return TypeObject.CAR;
			default:
				throw new RuntimeException("Unknown type.");
		}
	}

	private ObservationEntity.ObservationEntityType convertToObsEntityType(TypeObject type) throws RuntimeException {
		switch (type) {
			case PERSON:
				return ObservationEntity.ObservationEntityType.PERSON;
			case CAR:
				return ObservationEntity.ObservationEntityType.CAR;
			default:
				throw new RuntimeException("Unknown type.");
		}
	}

		private ObservationEntity convertToObsEntity(Observation obs){
		return new ObservationEntity(convertToObsEntityType(obs.getType()),
				obs.getId(),
				obs.getCamName());
	}

	private Timestamp convertToTimeStamp(LocalDateTime date) {
		return Timestamp.newBuilder().setSeconds(date.toEpochSecond(ZoneOffset.UTC))
									.setNanos(date.getNano())
									.build();
	}
}