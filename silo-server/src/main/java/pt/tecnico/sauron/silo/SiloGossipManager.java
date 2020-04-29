package pt.tecnico.sauron.silo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sauron.silo.domain.Camera;
import pt.tecnico.sauron.silo.domain.ObservationEntity;
import pt.tecnico.sauron.silo.domain.Operation;
import pt.tecnico.sauron.silo.grpc.CamJoinRequest;
import pt.tecnico.sauron.silo.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SiloGossipManager {
    private SiloBackend siloBackend = new SiloBackend();
    private Map<Integer, Operation> executedOperations = new HashMap<>();
    private Map<Integer, Integer> timestamp = new HashMap<>();
    private int instance;
    private String root;
    private String currentPath;
    private String zooHost;
    private String zooPort;

    public SiloGossipManager(int instance, String root, String zooHost, String zooPort) {
        this.instance = instance;
        this.root = root;
        this.currentPath = root + '/' + Integer.toString(instance);
        this.zooHost = zooHost;
        this.zooPort = zooPort;
        timestamp.put(instance, 0);
    }

    /**
     * Returns the timestamp of this replica
     * @return Map representing the timestamp
     */
    public Map<Integer, Integer> getTimestamp() {
        return timestamp;
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
     * Propages updates from this replica to all the other replicas
     */
    public void propagateGossip() {
        System.out.println("Replica " + instance + " starting gossip");
        ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
        try {
            Collection servers = zkNaming.listRecords(root);
            if (servers.isEmpty()) {
                //FIXME: Should not happen, but if it happens, that should be terrible
            }
            for (Object object: servers) {
                ZKRecord record = (ZKRecord) object;
                if (record.getPath().equals(this.currentPath) ) continue;

                //Get stub for communicating
                String target = record.getURI();
                ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                SiloGrpc.SiloBlockingStub stub = SiloGrpc.newBlockingStub(channel);

                //FIXME: Send all timestamps or send only from this instance?
                //Prepare request
                GossipTSRequest.Builder request = GossipTSRequest.newBuilder()
                        .setInstance(instance).putAllTimestamp(timestamp);

                //Send request
                try {
                    //TODO: Improve message
                    System.out.println("Sending timestamp to replica " + record.toString() + " at " + target);
                    stub.gossipTS(request.build());
                } catch (StatusRuntimeException e) {
                    System.out.println("Caught exception '" + e.getMessage() +
                            "' when trying to contact " + record.toString() + " at " + target);
                }

            }
        }
        catch (ZKNamingException e) {
            //FIXME: Again, should not happen, but if is does...
            e.printStackTrace();
        }
    }

    /**
     * Receives a gossip containing the other replica's timestamp and instance number and processes it,
     * by sending the operations it misses, receiving the operations this replica misses or, if updated,
     * stands by
     * @param otherTimestamp
     * @param otherInstance
     */
    public void receiveGossip(Map<Integer, Integer> otherTimestamp, int otherInstance) {
        //FIXME: Check if a replica only sends updates from itself or from other replicas
        System.out.println("Received timestamp '" + otherTimestamp.toString() + "' from replica " + otherInstance);

        //FIXME: maybe join asking for update and sending update

        // Check if this replica needs update from the other replica
        if ( !(timestamp.containsKey(otherInstance))
                || ( timestamp.get(otherInstance) < otherTimestamp.get(otherInstance))) {
            //TODO: ask for update
        }

        // Check if other replica needs update from this replica
        if ( !(otherTimestamp.containsKey(instance)) || ( otherTimestamp.get(instance) < timestamp.get(instance))) {
            //TODO: send update
        }
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
