package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.sauron.silo.domain.ObservationEntity.ObservationEntityType;

public class ObservationRepository {
    private Map<ObservationEntityType, Map<String, List<ObservationEntity>>> observations = new ConcurrentHashMap<>();

    public ObservationRepository() {
        for (ObservationEntityType type: ObservationEntityType.values()) {
            observations.put(type, new ConcurrentHashMap<String, List<ObservationEntity>>());
        }
    }

    public Map<String, List<ObservationEntity>> getTypeObservations(ObservationEntityType type) {
        return observations.get(type);
    }

    public List<ObservationEntity> getObservations(ObservationEntityType type, String id) {
        Map<String, List<ObservationEntity>> typeMap = getTypeObservations(type);
        if (!typeMap.containsKey(id)) typeMap.put(id, new ArrayList<ObservationEntity>());
        return typeMap.get(id);
    }

    public Operation addObservation(ObservationEntityType type, String id, ObservationEntity obs) {
        List<ObservationEntity> oldObs = getObservations(type, id);
        if (oldObs == null){
            oldObs = new ArrayList<>();
            getTypeObservations(type).put(id, oldObs);
        }
        oldObs.add(obs);
        return obs;
    }

    public void deleteObservation(ObservationEntityType type, String id, int opId) {
        List<ObservationEntity> obs = getObservations(type, id);
        for (int i = 0; i < obs.size(); i++) {
            if (obs.get(i).getOpId() == opId) {
                obs.remove(i);
                break;
            }
        }
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