package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.sauron.silo.domain.ObservationEntity.ObservationEntityType;

public class ObservationRepository {
    private Map<ObservationEntityType, Map<String, List<ObservationEntity>>> observations = new HashMap<>();

    public ObservationRepository() {
        for (ObservationEntityType type: ObservationEntityType.values()) {
            observations.put(type, new ConcurrentHashMap<String, List<ObservationEntity>>());
        }
    }

    public Map<String, List<ObservationEntity>> getTypeObservations(ObservationEntityType type) {
        return observations.get(type);
    }

    public List<ObservationEntity> getObservations(ObservationEntityType type, String id) {
        return getTypeObservations(type).get(id);
    }

    public void addObservation(ObservationEntityType type, String id, ObservationEntity obs) {
        List<ObservationEntity> oldObs = getObservations(type, id);
        if (oldObs == null){
            oldObs = new ArrayList<>();
            getTypeObservations(type).put(id, oldObs);
        }
        oldObs.add(obs); 
    }

    public int getRepoSize() {
        int size = 0;
        for (ObservationEntityType type: observations.keySet()) {
            size += getTypeObservations(type).size();
        }

        return size;
    }

    public void clear() {
        for (ObservationEntityType type: observations.keySet()) {
            getTypeObservations(type).clear();
        }
    }
}