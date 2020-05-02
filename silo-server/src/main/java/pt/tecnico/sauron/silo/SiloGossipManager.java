package pt.tecnico.sauron.silo;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationEntity;
import pt.tecnico.sauron.silo.domain.Operation;
import pt.tecnico.sauron.silo.exceptions.InvalidTypeException;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SiloGossipManager {
    private SiloBackend siloBackend = new SiloBackend();
    private Map<Integer, Map<Integer, Operation>> executedOperations = new HashMap<>();
    private Map<Integer, Integer> timestamp = new HashMap<>();
    private int instance;
    private String root;
    private String currentPath;
    private ZKNaming zkNaming;
    private int numbOfRetries = 0;
    private int gossipInterval;

    public SiloGossipManager(int instance, String root, String zooHost, String zooPort, int gossipInterval) {
        this.instance = instance;
        this.root = root;
        this.currentPath = root + '/' + Integer.toString(instance);
        timestamp.put(instance, 0);
        this.zkNaming = new ZKNaming(zooHost, zooPort);
        this.gossipInterval = gossipInterval;

        // Create new thread where we propagate our gossips
        new Thread(() -> {
            Timer timer = new Timer();
            TimerTask gossip = new TimerTask() {
                @Override
                public void run() {
                    propagateGossip();
                }
            };
            timer.schedule(gossip, gossipInterval, gossipInterval);
        }).start();
    }

    /**
     * Returns the timestamp of this replica
     * @return Map representing the timestamp
     */
    public Map<Integer, Integer> getTimestamp() {
        return timestamp;
    }

    /**
     * Update timestamp given an instance and the new timestamp value
     * @param instance
     * @param value
     */
    public void updateTimestamp(int instance, int value) {
        if (!timestamp.containsKey(instance)) timestamp.put(instance, value);
        else timestamp.replace(instance, value);
    }

    /**
     * Get this replica instance number
     * @return instance
     */
    public int getInstance() {
        return instance;
    }

    /**
     * Adds an operation to the register
     * @param operation
     */
    public void addOperation(Operation operation, int instance) {
        if (!timestamp.containsKey(instance)) timestamp.put(instance, 0);
        int opId = timestamp.get(instance)+1;
        if (!executedOperations.containsKey(instance))
            executedOperations.put(instance, new ConcurrentHashMap<Integer, Operation>());

        operation.setOpId(opId);
        operation.setInstance(instance);
        executedOperations.get(instance).put(opId, operation);
        timestamp.replace(instance, opId);
        System.out.println("Received a " + operation + " from " +
                ( (instance != this.instance) ? "replica " + instance : "client") );
    }

    /**
     * Gets operations given the small operationId the other replica has
     * @param minOpId
     * @return Operations
     */
    public List<Operation> getOperations(int minOpId, int instance) {
        List<Operation> result = new ArrayList<>();
        if (!executedOperations.containsKey(instance)) return result;
        Map<Integer, Operation> instanceOperations = executedOperations.get(instance);
        for (Integer i: instanceOperations.keySet()) {
            if (i > minOpId) result.add(instanceOperations.get(i));
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
        if (numbOfRetries == 3) {
            System.out.println("Could not establish a connection to the other server, try again later.");
            System.exit(1);
        }

        System.out.println("\nReplica " + instance + " initiating gossip...");
        try {
            Collection<ZKRecord> servers = zkNaming.listRecords(root);
            if (servers.isEmpty()) {
                System.out.println("Could not connect other replicas, retrying in 30 seconds.");
                numbOfRetries++;
            }

            if (servers.size() == 1) {
                System.out.println("No replicas to contact.");
            }

            for (ZKRecord zkRecord: servers) {
                if (zkRecord.getPath().equals(this.currentPath) ) continue;

                //Get stub for communicating
                String target = zkRecord.getURI();
                ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                SiloGrpc.SiloBlockingStub stub = SiloGrpc.newBlockingStub(channel);

                //Prepare request
                GossipTSRequest.Builder request = GossipTSRequest.newBuilder()
                        .setInstance(instance).putAllTimestamp(timestamp);

                int splitPathSize = zkRecord.getPath().split("/").length;
                String receivingReplicaInstance = zkRecord.getPath().split("/")[splitPathSize - 1];
                //Send request
                new Thread(() -> {
                    try {
                        System.out.println("\nContacting replica " + receivingReplicaInstance + " at " + target + "...");
                        System.out.println("Sending timestamp " + timestamp.toString() + "...");
                        GossipTSResponse response = stub
                                .withDeadlineAfter(gossipInterval/2, TimeUnit.MILLISECONDS)
                                .gossipTS(request.build());
                        System.out.println("Received timestamp " + response.getTimestampMap() + " from replica " + response.getInstance());
                        receiveGossip(response.getTimestampMap(), response.getInstance());
                        numbOfRetries = 0;
                    } catch (InvalidTypeException e) {
                        System.out.println("Caught exception '" + e.getMessage() +
                                "' when trying to contact " + zkRecord.toString() + " at " + target);
                    } catch (StatusRuntimeException e) {
                        checkGossipException(e.getStatus(), receivingReplicaInstance);
                    } catch (RuntimeException e) {
                        System.out.println("Caught exception!");
                    } finally {
                        channel.shutdown();
                    }
                }).start();

            }
        }
        catch (ZKNamingException e) {
            System.out.println("Caught an exception while trying to communicate with the other replicas, trying again in 30 seconds: " + e.getMessage());
            numbOfRetries++;
        }
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
        List<OperationMessage> opsToSend = new ArrayList<>();
        for (Operation operation : getUpdatesForInstance(otherTimestamp, otherInstance)) {
            opsToSend.add(convertOperationToMessage(operation));
            System.out.println("Sending " + operation);
        }

        // If there aren't updates, just return
        if (opsToSend.isEmpty()) return;

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
            stub.withDeadlineAfter(gossipInterval/2, TimeUnit.MILLISECONDS).gossipUpdate(request);
            channel.shutdown();
        } catch (StatusRuntimeException e) {
            checkGossipException(e.getStatus(), Integer.toString(otherInstance));
        } catch (ZKNamingException e) {
            System.out.println("Caught exception '" + e.getMessage() +
                    "' when trying to contact " + otherInstance);
        }
    }

    /**
     * Returns a list with the operations that the other replica doesn't have
     * @param otherTimestamp
     * @param otherInstance
     * @return
     */
    private List<Operation> getUpdatesForInstance(Map<Integer, Integer> otherTimestamp, int otherInstance) {
        List<Operation> result = new ArrayList<>();
        for (int i: this.timestamp.keySet()) {
            if (i == otherInstance) continue;
            if (!otherTimestamp.containsKey(i))
                result.addAll(getOperations(-1, i));
            else if (otherTimestamp.get(i) < this.timestamp.get(i))
                result.addAll(getOperations(otherTimestamp.get(i), i));
        }
        return result;
    }

    /**
     * Prints a message according to the exception status
     * @param status
     */
    void checkGossipException(Status status, String instance) {
        String error;
        if (status.getCode() == Status.CANCELLED.getCode())
            error = "Timed out";
        else if (status.getCode() == Status.DEADLINE_EXCEEDED.getCode())
            error = "Response took too long";
        else if (status.getCode() == Status.UNAVAILABLE.getCode())
            error = "Replica not available";
        else error = status.getCode().toString();
        System.out.println(error + " when trying to send request at replica " + instance);
    }

    /**
     * Receives an operations and returns an OperationMessage from protobuf
     * @param operation
     * @return
     * @throws InvalidTypeException
     */
    private OperationMessage convertOperationToMessage(Operation operation) throws InvalidTypeException {
        if (operation.getClassName().equals(Camera.class.getSimpleName())) {
            Camera camera = (Camera) operation;
            CamJoinRequest cameraRequest = CamJoinRequest.newBuilder().setCamName(camera.getName())
                    .setCoordinates(Coordinates.newBuilder()
                            .setLat(camera.getLatitude())
                            .setLong(camera.getLongitude()).build())
                    .build();
            return OperationMessage.newBuilder()
                    .setCamera(cameraRequest)
                    .setOperationId(operation.getOpId())
                    .setInstance(operation.getInstance())
                    .build();
        }
        else {
            ObservationEntity obs = (ObservationEntity) operation;
            Observation obsRequest = Observation.newBuilder()
                    .setType(convertToType(obs.getType()))
                    .setId(obs.getId())
                    .setDateTime(convertToTimeStamp(obs.getDateTime()))
                    .setCamName(obs.getCamName())
                    .build();
            return OperationMessage.newBuilder()
                    .setObservation(obsRequest)
                    .setOperationId(operation.getOpId())
                    .setInstance(operation.getInstance())
                    .build();
        }
    }

    /**
     * Returns the protobuf TypeObject corresponding to the observation type
     * @param type
     * @return TypeObject
     * @throws InvalidTypeException
     */
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

    /**
     * Converts a localdatetime date to a Timestamp from protobuf
     * @param date
     * @return
     */
    public Timestamp convertToTimeStamp(LocalDateTime date) {
		return Timestamp.newBuilder().setSeconds(date.toEpochSecond(ZoneOffset.UTC))
									.setNanos(date.getNano()).build();
    }

}
