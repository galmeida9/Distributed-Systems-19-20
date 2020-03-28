package pt.tecnico.sauron.silo;

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
		// TODO:
		TrackResponse response = TrackResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
		// TODO:
		TrackMatchResponse response = TrackMatchResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {
		// TODO:
		TraceResponse response = TraceResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
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
	
	
}
