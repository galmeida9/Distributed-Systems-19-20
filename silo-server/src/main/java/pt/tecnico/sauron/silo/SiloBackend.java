package pt.tecnico.sauron.silo;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.tecnico.sauron.silo.ObservationEntity.ObservationEntityType;

class SiloBackend {
    private final Map<ObservationEntityType, Map<String, List<ObservationEntity>>> observations = new HashMap<>();

    private Map<String, List<ObservationEntity>> getTypeObservations(ObservationEntityType type) {
        return observations.get(type);
    }

    private List<ObservationEntity> getObservations(ObservationEntityType type, String id) {
        return getTypeObservations(type).get(id);
    }

    private void addIds(final ObservationEntityType type, String id) {
        getTypeObservations(type).put(id, new ArrayList<>());
    }

    private void addObservation(ObservationEntityType type, String id, ObservationEntity observation) {
        observation.setDateTime(LocalDateTime.now());
        getObservations(type, id).add(observation);
    }

    // tamanho do ID pelo menos 63bits
    private void checkId(ObservationEntityType type, String id) throws InvalidIdException {
        switch(type) {
            case PERSON:
                int size = id.getBytes(StandardCharsets.UTF_16BE).length * 8;
                if (size < 63) throw new InvalidIdException(id + " for type " + type.toString() + " id too small.");
                return;
            case CAR:
                String licensePlatePattern = "([A-Z][A-Z]|[0-9][0-9])([A-Z][A-Z]|[0-9][0-9])([A-Z][A-Z]|[0-9][0-9])";
                if (!id.matches(licensePlatePattern) 
                    || id.chars().filter(Character::isDigit).count() > 4 
                    || id.chars().filter(Character::isLetter).count() > 4) 
                    throw new InvalidIdException(id + " for type " + type.toString() + " is not a license plate.");
                return;
            default:
                return;
        }
    }

    public ObservationEntity track(ObservationEntityType type, String id) throws InvalidIdException, NoObservationsException {
        checkId(type, id);
        List<ObservationEntity> obs = getObservations(type, id);
        if (!obs.isEmpty()) {
            int size = obs.size() - 1;
            return obs.get(size);
        }

        throw new NoObservationsException("No observations found for " + id);
    }

    //verificacao diferente
    public List<ObservationEntity> trackMatch(ObservationEntityType type, String partId) throws InvalidIdException, NoObservationsException {
        List<ObservationEntity> matches = new ArrayList<>();
        String pattern = partId.replace("*", ".*");
        pattern += "$";

        for (String id: observations.get(type).keySet()) {
            if (id.matches(pattern)) matches.add(track(type, id)); 
        }

        return matches;
    }

    public List<ObservationEntity> trace(ObservationEntityType type, String id) throws InvalidIdException, NoObservationsException {
        checkId(type, id);
        List<ObservationEntity> obs = new ArrayList<>(getObservations(type, id));
        if (obs.isEmpty()) throw new NoObservationsException("No observations found for " + id);
        Collections.reverse(obs);
        return obs;
    }
    
}