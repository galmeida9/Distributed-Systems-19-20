package pt.tecnico.sauron.silo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.tecnico.sauron.silo.ObservationEntity.ObservationEntityType;

class SiloBackend {
    private final Map<ObservationEntityType, Map<String, List<ObservationEntity>>> observations = new HashMap<>();
    private static final String EMPTY_STRING = "";

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
        getObservations(type, id).add(observation);
    }

    // tamanho do ID pelo menos 63bits
    private void checkId(ObservationEntityType type, String id) throws InvalidPersonIdException, InvalidCarIdException {
        switch(type) {
            case PERSON:
                int size = id.getBytes(StandardCharsets.UTF_16BE).length * 8;
                if (size < 63) throw new InvalidPersonIdException(id + " id too small");
                return;
            case CAR:
                String licensePlatePattern = "([A-Z][A-Z]|[0-9][0-9])([A-Z][A-Z]|[0-9][0-9])([A-Z][A-Z]|[0-9][0-9])";
                if (!id.matches(licensePlatePattern) 
                    || id.chars().filter(Character::isDigit).count() > 4 
                    || id.chars().filter(Character::isLetter).count() > 4) 
                    throw new InvalidCarIdException("id is not a license plate.");
                return;
            default:
                return;
        }
    }

    public ObservationEntity track(ObservationEntityType type, String id) throws InvalidPersonIdException, InvalidCarIdException, NoObservationsException {
        checkId(type, id);
        List<ObservationEntity> obs = getObservations(type, id);
        if (!obs.isEmpty()) {
            int size = obs.size() - 1;
            return obs.get(size);
        }

        throw new NoObservationsException("No observations found for " + id);
    }

    //verificacao diferente
    public List<ObservationEntity> trackMatch(ObservationEntityType type, String partId) throws InvalidPersonIdException, InvalidCarIdException, NoObservationsException {
        List<ObservationEntity> matches = new ArrayList<>();
        String start = EMPTY_STRING;
        String end = EMPTY_STRING;
        if (partId.startsWith("*")) {
            end = partId.split("*")[1];
        }
        else if (partId.endsWith("*")) {
            start = partId.split("*")[0];
        }
        else if (partId.contains("*")) {
            start = partId.split("*")[0];
            end = partId.split("*")[1];
        }
        
        for (String id: observations.get(type).keySet()) {
            if (
                start.isEmpty() && end.isEmpty() && id.contains(partId) ||
                start.isEmpty() && id.endsWith(end) ||
                end.isEmpty() && id.startsWith(start) ||
                id.startsWith(start) && id.endsWith(end)
                ) matches.add(track(type, id)); 
        }

        return matches;
    }

    public List<ObservationEntity> trace(ObservationEntityType type, String id) throws InvalidPersonIdException, InvalidCarIdException, NoObservationsException {
        checkId(type, id);
        List<ObservationEntity> obs = new ArrayList<>(getObservations(type, id));
        if (obs.isEmpty()) throw new NoObservationsException("No observations found for " + id);
        Collections.reverse(obs);
        return obs;
    }
    
}