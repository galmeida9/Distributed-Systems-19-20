package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.sauron.silo.domain.ObservationEntity.ObservationEntityType;

public class ObservationRepository {
    private Map<ObservationEntityType, Map<String, List<ObservationEntity>>> observations = new ConcurrentHashMap<>();

    /**
     * Creates a repository for observations, starting it with the existent types
     */
    public ObservationRepository() {
        for (ObservationEntityType type: ObservationEntityType.values()) {
            observations.put(type, new ConcurrentHashMap<String, List<ObservationEntity>>());
        }
    }

    /**
     * Returns all observations of a given type
     * @param type
     * @return
     */
    public Map<String, List<ObservationEntity>> getTypeObservations(ObservationEntityType type) {
        return observations.get(type);
    }

    /**
     * Returns all observations of a given type and a given identifier
     * @param type
     * @param id
     * @return
     */
    public List<ObservationEntity> getObservations(ObservationEntityType type, String id) {
        Map<String, List<ObservationEntity>> typeMap = getTypeObservations(type);
        if (!typeMap.containsKey(id)) typeMap.put(id, new ArrayList<ObservationEntity>());
        return typeMap.get(id);
    }

    /**
     * Adds a given observation to the repository, given itself, its type and the
     * identifier of whom it observes
     * @param type
     * @param id
     * @param obs
     * @return
     */
    public Operation addObservation(ObservationEntityType type, String id, ObservationEntity obs) {
        List<ObservationEntity> oldObs = getObservations(type, id);
        if (oldObs == null){
            oldObs = new ArrayList<>();
            getTypeObservations(type).put(id, oldObs);
        }
        oldObs.add(obs);
        return obs;
    }

    /**
     * Returns the size of the repository
     * @return
     */
    public int getRepoSize() {
        int size = 0;
        for (ObservationEntityType type: observations.keySet()) {
            size += getTypeObservations(type).size();
        }

        return size;
    }

    /**
     * Deletes all observations in the repository
     */
    public void clear() {
        for (ObservationEntityType type: observations.keySet()) {
            getTypeObservations(type).clear();
        }
    }
}