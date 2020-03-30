package pt.tecnico.sauron.silo;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.grpc.*;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

	private SiloBackend backend = new SiloBackend();

	/* Functionality operations */
	
	@Override
	public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver){
		// TODO: 
		CamJoinResponse response = CamJoinResponse.newBuilder().build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
		// TODO:
		CamInfoResponse response = CamInfoResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
		// TODO:
		ReportResponse response = ReportResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
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
			//perguntar sobre excecao generica
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
			//perguntar sobre excecao generica
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
		} catch (InvalidIdException | NoObservationsException e) {
			//perguntar sobre excecao generica
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
		// TODO:
		CtrlClearResponse response = CtrlClearResponse.newBuilder().build();
		responseObserver.onNext(response);
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
							.build();
	}

	private TypeObject convertToType(ObservationEntity.ObservationEntityType type) throws RuntimeException {
		switch (type) {
			case PERSON:
				return TypeObject.PERSON;
			case CAR:
				return TypeObject.CAR;
			default:
				throw new RuntimeException("Unkown type.");
		}
	}

	private ObservationEntity.ObservationEntityType convertToObsEntityType(TypeObject type) throws RuntimeException {
		switch (type) {
			case PERSON:
				return ObservationEntity.ObservationEntityType.PERSON;
			case CAR:
				return ObservationEntity.ObservationEntityType.CAR;
			default:
				throw new RuntimeException("Unkown type.");
		}
	}

	private Timestamp convertToTimeStamp(LocalDateTime date) {
		return Timestamp.newBuilder().setSeconds(date.toEpochSecond(ZoneOffset.UTC))
									.setNanos(date.getNano())
									.build();
	}
}
