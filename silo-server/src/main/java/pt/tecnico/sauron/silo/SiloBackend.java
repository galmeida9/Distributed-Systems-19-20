package pt.tecnico.sauron.silo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.tecnico.sauron.silo.grpc.*;

public class SiloBackend {
    private final Map<TypeObject, Map<String, List<Observation>>> observations = new HashMap<>();
    private static final String EMPTY_STRING = "";

    private Map<TypeObject, Map<String, List<Observation>>> getObservations() {
        return observations;
    }

    private void addIds(final TypeObject type, String id) {
        observations.get(type).put(id, new ArrayList<>());
    }

    private void addObservation(TypeObject type, String id, Observation observation) {
        observations.get(type).get(id).add(observation);
    }

    // Exception for Invalid person id size
    public class InvalidPersonIdException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidPersonIdException(String message){
            super(message);
        }
    }

    // Exception for Invalid car id
    public class InvalidCarIdException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidCarIdException(String message){
            super(message);
        }
    }

    // tamanho do ID pelo menos 63bits
    private void checkId(TypeObject type, String id) throws InvalidPersonIdException, InvalidCarIdException {
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

    // Exception for no observation found
    public class NoObservationsException extends Exception {
        private static final long serialVersionUID = 1L;

        public NoObservationsException(String message){
            super(message);
        }
    }

    public Observation track(TypeObject type, String id) throws InvalidPersonIdException, InvalidCarIdException, NoObservationsException {
        checkId(type, id);
        List<Observation> obs = observations.get(type).get(id);
        if (!obs.isEmpty()) {
            int size = obs.size() - 1;
            return obs.get(size);
        }

        throw new NoObservationsException("No observations found for " + id);
    }

    //verificacao diferente
    public List<Observation> trackMatch(TypeObject type, String partId) throws InvalidPersonIdException, InvalidCarIdException {
        List<Observation> matches = new ArrayList<>();
        String start = EMPTY_STRING, end = EMPTY_STRING;
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

    public List<Observation> trace(TypeObject type, String id) throws InvalidPersonIdException, InvalidCarIdException {
        checkId(type, id);
        List<Observation> obs = new ArrayList<>(observations.get(type).get(id));
        Collections.reverse(obs);
        return obs;
    }
    
}