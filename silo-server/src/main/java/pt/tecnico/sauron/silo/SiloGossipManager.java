package pt.tecnico.sauron.silo;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationEntity;
import pt.tecnico.sauron.silo.domain.Operation;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SiloGossipManager {
    private SiloBackend siloBackend = new SiloBackend();
    private Map<Integer, Operation> executedOperations = new HashMap<>();
    private Map<Integer, Integer> timestamp = new HashMap<>();
    private int instance;
    private String root;
    private String currentPath;
    private String zooHost;
    private String zooPort;
    private ZKNaming zkNaming;

    public SiloGossipManager(int instance, String root, String zooHost, String zooPort) {
        this.instance = instance;
        this.root = root;
        this.currentPath = root + '/' + Integer.toString(instance);
        this.zooHost = zooHost;
        this.zooPort = zooPort;
        timestamp.put(instance, 0);
        this.zkNaming = new ZKNaming(zooHost, zooPort);

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                propagateGossip();
            }
        }, 30, TimeUnit.SECONDS);
    }

    /**
     * Returns the timestamp of this replica
     * @return Map representing the timestamp
     */
    public Map<Integer, Integer> getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp(int inst, int value) {
        if (!timestamp.containsKey(inst)) timestamp.put(inst, value);
        else timestamp.replace(inst, value);
    }

    public int getInstance() {
        return instance;
    }

    /**
     * Adds an operation to the register
     * @param operation
     */
    public void addOperation(Operation operation) {
        int opId = timestamp.get(instance)+1;
        executedOperations.put(opId, operation);
        operation.setOpId(opId);
        timestamp.replace(instance, opId);
    }

    /**
     * Deletes all operations after the given smallest operation id
     * @param minOpId
     */
    public void deleteOperations(int minOpId) {
        for (Integer i: executedOperations.keySet()) {
            if (i > minOpId) {
                deleteOperation(executedOperations.get(i));
                executedOperations.remove(i);
            }
        }
    }

    /**
     * Deletes an operation given itself
     * @param operation
     */
    public void deleteOperation(Operation operation) {
        if (operation.getClassName().equals(ObservationEntity.class.getSimpleName())) {
            siloBackend.deleteObservation( (ObservationEntity) operation );
        }
        else if (operation.getClassName().equals(Camera.class.getSimpleName())) {
            siloBackend.camDelete( (Camera) operation);
        }
    }

    /**
     * Gets operations given the small operationId the other replica has
     * @param minOpId
     * @return Operations
     */
    public Map<Integer, Operation> getOperations(int minOpId) {
        Map<Integer, Operation> result = new HashMap<>();
        for (Integer i: executedOperations.keySet()) {
            if (i > minOpId) result.put(i, executedOperations.get(i));
        }
        return result;
    }

    /**
     * Returns SiloBackend
     * @return SiloBackend
     */
    public SiloBackend getSiloBackend() {
        return siloBackend;
    }

    /**
     * Propagates updates from this replica to all the other replicas
     */
    public void propagateGossip() {
        System.out.println("Replica " + instance + " starting gossip");
        try {
            Collection<ZKRecord> servers = zkNaming.listRecords(root);
            if (servers.isEmpty()) {
                System.out.println("Could not find other replicas, retrying in 30 seconds.");
                new Timer().schedule(new TimerTask(){
                
                    @Override
                    public void run() {
                        propagateGossip();
                        
                    }
                }, 30000);
            }

            System.out.println("Replica " + instance + " initiating gossip...");
            for (ZKRecord zkRecord: servers) {
                if (zkRecord.getPath().equals(this.currentPath) ) continue;

                //Get stub for communicating
                String target = zkRecord.getURI();
                ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                SiloGrpc.SiloBlockingStub stub = SiloGrpc.newBlockingStub(channel);

                //Prepare request
                GossipTSRequest.Builder request = GossipTSRequest.newBuilder()
                        .setInstance(instance).putAllTimestamp(timestamp);

                //Send request
                try {
                    int splitPathSize = zkRecord.getPath().split("/").length;
                    String receivingReplicaInstance = zkRecord.getPath().split("/")[splitPathSize - 1];
                    System.out.println("Contacting replica " + receivingReplicaInstance + " at " + target + "...");
                    System.out.println("Sending " + timestamp.toString() + "...");
                    GossipTSResponse response = stub.gossipTS(request.build());
                    System.out.println("Received answer " + response.getTimestampMap());
                    channel.shutdown();
                    receiveGossip(response.getTimestampMap(), response.getInstance());
                } catch (StatusRuntimeException | InvalidTypeException e) {
                    System.out.println("Caught exception '" + e.getMessage() +
                            "' when trying to contact " + zkRecord.toString() + " at " + target);
                }

            }
        }
        catch (ZKNamingException e) {
            //FIXME: Again, should not happen, but if is does...
            e.printStackTrace();
        }

        final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                propagateGossip();
            }
        }, 30, TimeUnit.SECONDS);
    }

    /**
     * Receives a gossip containing the other replica's timestamp and instance number and processes it,
     * by sending the operations it misses, receiving the operations this replica misses or, if updated,
     * stands by
     * @param otherTimestamp
     * @param otherInstance
     */
    public void receiveGossip(Map<Integer, Integer> otherTimestamp, int otherInstance) throws InvalidTypeException {
        // Check if other replica needs update from this replica
        if ( !(otherTimestamp.containsKey(instance)) || ( otherTimestamp.get(instance) < timestamp.get(instance))) {
            
            int startPoint;
            if (!(otherTimestamp.containsKey(instance)))
                startPoint = 1;
            else
                startPoint = otherTimestamp.get(instance) + 1;
            List<OperationMessage> opsToSend = new ArrayList<>();
            for (int i = startPoint; i <= timestamp.get(instance); i++) {
                if (executedOperations.get(i).getClassName().equals(Camera.class.getSimpleName())) {
                    Camera camera = (Camera) executedOperations.get(i);
                    CamJoinRequest cameraRequest = CamJoinRequest.newBuilder().setCamName(camera.getName())
                                                    .setCoordinates(Coordinates.newBuilder()
                                                        .setLat(camera.getLatitude())
                                                        .setLong(camera.getLongitude()).build())
                                                    .build();
                    opsToSend.add(OperationMessage.newBuilder().setCamera(cameraRequest).setOperationId(i).build());
                    System.out.println("Sending Camera " + camera.getName());
                }
                else {
                    ObservationEntity obs = (ObservationEntity) executedOperations.get(i);
                    Observation obsRequest = Observation.newBuilder()
                                                .setType(convertToType(obs.getType()))
                                                .setId(obs.getId())
                                                .setDateTime(convertToTimeStamp(obs.getDateTime()))
                                                .setCamName(obs.getCamName())
                                                .build();
                    opsToSend.add(OperationMessage.newBuilder().setObservation(obsRequest).setOperationId(i).build());
                    System.out.println("Sending observation " + obs.getId());
                }
            }

            try{
                String path = root + '/' + Integer.toString(otherInstance);
                ZKRecord zkRecord = zkNaming.lookup(path);
                ManagedChannel channel = ManagedChannelBuilder.forTarget(zkRecord.getURI()).usePlaintext().build();
                SiloGrpc.SiloBlockingStub stub = SiloGrpc.newBlockingStub(channel);
                GossipUpdateRequest request = GossipUpdateRequest.newBuilder()
                                                                    .setInstance(instance)
                                                                    .putAllTimestamp(timestamp)
                                                                    .addAllOperation(opsToSend)
                                                                    .build();
                stub.gossipUpdate(request);
                channel.shutdown();
            } catch (StatusRuntimeException | ZKNamingException e) {
                System.out.println("Caught exception " + e.getMessage() + "when trying to contact replica " + otherInstance);
            }
        }
    }

    public TypeObject convertToType(ObservationEntity.ObservationEntityType type) throws InvalidTypeException {
		switch (type) {
			case PERSON:
				return TypeObject.PERSON;
			case CAR:
				return TypeObject.CAR;
			default:
				throw new InvalidTypeException("Unknown type: " + type.toString());
		}
    }
    
    public Timestamp convertToTimeStamp(LocalDateTime date) {
		return Timestamp.newBuilder().setSeconds(date.toEpochSecond(ZoneOffset.UTC))
									.setNanos(date.getNano())
									.build();
	}

    /*
     * Debug functions
     */
    private static final boolean DEBUG_FLAG = "true".equals(System.getenv("debug"));

    private static void debug(String debugMessage){
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }
}
