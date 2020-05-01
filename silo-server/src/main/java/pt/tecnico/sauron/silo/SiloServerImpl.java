package pt.tecnico.sauron.silo;

import com.google.protobuf.Timestamp;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.ObservationEntity;
import pt.tecnico.sauron.silo.domain.Operation;
import pt.tecnico.sauron.silo.domain.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidIdException;
import pt.tecnico.sauron.silo.grpc.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class SiloServerImpl extends SiloGrpc.SiloImplBase {

	private SiloGossipManager manager;

	public SiloServerImpl(int instance, String path, String zooHost, String zooPort, int retry) {
		manager = new SiloGossipManager(instance, path, zooHost, zooPort, retry);
	}

	/* Functionality operations */
	
	/** 
	 * Execute camJoin request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver){
		try {
			Operation o = manager.getSiloBackend()
					.addCamera(request.getCamName(), request.getCoordinates().getLat(), request.getCoordinates().getLong());
			manager.addOperation(o, manager.getInstance());

			CamJoinResponse response = CamJoinResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidCameraArguments e) {
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute camInfo request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {
		try {
			List<Double> listCoords = manager.getSiloBackend().camInfo(request.getCamName());
			Coordinates coords = Coordinates.newBuilder()
				.setLat(listCoords.get(0))
				.setLong(listCoords.get(1))
				.build();
			CamInfoResponse response = CamInfoResponse.newBuilder().setCoordinates(coords).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (CameraNotFoundException e) {
			responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute report request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {
		try{
			List<Observation> obs = request.getObservationList();
			for (Observation observation : obs){
				ObservationEntity observationEntity = convertToObsEntity(observation);
				manager.addOperation( observationEntity.addToStore(manager.getSiloBackend()) , manager.getInstance());
			}

			ReportResponse response = ReportResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidIdException | InvalidTypeException e){
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		} catch (CameraNotFoundException e) {
			responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute track request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {
		try {
			ObservationEntity obs = manager.getSiloBackend().track(convertToObsEntityType(request.getType()), request.getId());
			Observation obsResponse = convertToObservation(obs);
			TrackResponse response = TrackResponse.newBuilder().setObservation(obsResponse).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (InvalidIdException | InvalidTypeException e) {
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		} catch (NoObservationsException e) {
			responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute trackMatch request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
		try {
			List<ObservationEntity> obs = manager.getSiloBackend().trackMatch(convertToObsEntityType(request.getType()), request.getPartialId());
			TrackMatchResponse.Builder response = TrackMatchResponse.newBuilder();

			for (ObservationEntity observation: obs) {
				response.addObservation(convertToObservation(observation));
			}

			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		} catch (InvalidIdException | InvalidTypeException e) {
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		} catch (NoObservationsException e) {
			responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute trace request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {
		try{
			List<ObservationEntity> obs = manager.getSiloBackend().trace(convertToObsEntityType(request.getType()), request.getId());
			TraceResponse.Builder response = TraceResponse.newBuilder();

			for (ObservationEntity observation : obs) {
				response.addObservation(convertToObservation(observation));
			}
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		} catch (InvalidIdException | InvalidTypeException e) {
			responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute ctrPing request
	 * @param request
	 * @param responseObserver
	 */
	/* Control operations */

	@Override
	public void ctrlPing(CtrlPingRequest request, StreamObserver<CtrlPingResponse> responseObserver) {
		String input = request.getInput();
		String output = "Hello " + input + "!";
		CtrlPingResponse response = CtrlPingResponse.newBuilder().setOutput(output).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	
	/** 
	 * Execute ctrlClear request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void ctrlClear(CtrlClearRequest request, StreamObserver<CtrlClearResponse> responseObserver) {
		try {
			manager.getSiloBackend().ctrlClear();

			CtrlClearResponse response = CtrlClearResponse.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (CannotClearServerException e) {
			responseObserver.onError(io.grpc.Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
		}
	}

	
	/** 
	 * Execute ctrlInit request
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void ctrlInit(CtrlInitRequest request, StreamObserver<CtrlInitResponse> responseObserver) {
		CtrlInitResponse response = CtrlInitResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	
	/** 
	 * Receive request to start exchanging gossip messages
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void gossipTS(GossipTSRequest request, StreamObserver<GossipTSResponse> responseObserver) {
		new Thread(() -> {
			try {
				GossipTSResponse response = GossipTSResponse.newBuilder()
						.setInstance(manager.getInstance()).putAllTimestamp(manager.getTimestamp())
						.build();
				responseObserver.onNext(response);
				responseObserver.onCompleted();
			} catch (StatusRuntimeException e) {
				manager.checkGossipException(e.getStatus(), Integer.toString(request.getInstance()));
			}

        }).start();
	}

	
	/**
	 * Receive gossip messages to update data in the server 
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void gossipUpdate(GossipUpdateRequest request, StreamObserver<GossipUpdateResponse> responseObserver) {
		new Thread(() -> {
			try {
				for (OperationMessage opMessage : request.getOperationList()) {
					if (opMessage.hasCamera()) {
						Operation o = manager.getSiloBackend().addCamera(
								opMessage.getCamera().getCamName(), opMessage.getCamera().getCoordinates().getLat(),
								opMessage.getCamera().getCoordinates().getLong());

						manager.addOperation(o, opMessage.getInstance());
					} else if (opMessage.hasObservation()) {
						ObservationEntity entity = convertToObsEntity(opMessage.getObservation());
						manager.addOperation(entity.addToStore(manager.getSiloBackend()), opMessage.getInstance());
					}
				}
				manager.updateTimestamp(request.getInstance(), request.getTimestampMap().get(request.getInstance()));

				GossipUpdateResponse response = GossipUpdateResponse.newBuilder().build();

				responseObserver.onNext(response);
				responseObserver.onCompleted();
			} catch (StatusRuntimeException e) {
				manager.checkGossipException(e.getStatus(), Integer.toString(request.getInstance()));
			} catch (InvalidCameraArguments | CameraNotFoundException | InvalidIdException | InvalidTypeException e) {
				responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).asRuntimeException());
			}
		}).start();
	}

	/** 
	 * Converts ObservationEntity into its grpc equivalent class
	 * @param observation
	 * @return Observation
	 * @throws InvalidTypeException
	 */
	private Observation convertToObservation(ObservationEntity observation) throws InvalidTypeException {
		return Observation.newBuilder()
			.setType(convertToType(observation.getType()))
			.setId(observation.getId())
			.setDateTime(convertToTimeStamp(observation.getDateTime()))
			.setCamName(observation.getCamName())
			.build();
	}


	
	/** 
	 * Convert ObservationEntityType into its equivalent grpc class
	 * @param type
	 * @return TypeObject
	 * @throws InvalidTypeException
	 */
	private TypeObject convertToType(ObservationEntity.ObservationEntityType type) throws InvalidTypeException {
		switch (type) {
			case PERSON:
				return TypeObject.PERSON;
			case CAR:
				return TypeObject.CAR;
			default:
				throw new InvalidTypeException("Unknown type: " + type.toString());
		}
	}

	
	/** 
	 * Convert TypeObject from grpc into domain specific class, ObservationEntityType
	 * @param type
	 * @return ObservationEntityType
	 * @throws InvalidTypeException
	 */
	private ObservationEntity.ObservationEntityType convertToObsEntityType(TypeObject type) throws InvalidTypeException {
		switch (type) {
			case PERSON:
				return ObservationEntity.ObservationEntityType.PERSON;
			case CAR:
				return ObservationEntity.ObservationEntityType.CAR;
			default:
				throw new InvalidTypeException("Unknown type: " + type.toString());
		}
	}

	
	/** 
	 * Convert Observation from grpc into domain specific class, ObservationEntity
	 * @param obs
	 * @return ObservationEntity
	 * @throws InvalidTypeException
	 */
	private ObservationEntity convertToObsEntity(Observation obs) throws InvalidTypeException {
		return new ObservationEntity(convertToObsEntityType(obs.getType()),
				obs.getId(),
				obs.getCamName());
	}

	
	/** 
	 * Convert LocalDateTime into google's Timestamp
	 * @param date
	 * @return Timestamp
	 */
	private Timestamp convertToTimeStamp(LocalDateTime date) {
		return Timestamp.newBuilder().setSeconds(date.toEpochSecond(ZoneOffset.UTC))
									.setNanos(date.getNano())
									.build();
	}
}