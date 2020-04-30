package pt.tecnico.sauron.silo;

import java.time.LocalDateTime;
import java.util.*;

import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.domain.ObservationEntity.ObservationEntityType;
import pt.tecnico.sauron.silo.domain.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidIdException;

class SiloBackend implements OperationStore {
    private ObservationRepository obsRepo;
    private CameraRepository camRepo;

    SiloBackend() {
        obsRepo = new ObservationRepository();
        camRepo = new CameraRepository();
    }

    public List<Double> camInfo(String id) throws CameraNotFoundException {
        return camRepo.getCameraInfo(id);
    }

    public Operation addCamera(String id, double lat, double lon) throws InvalidCameraArguments {
        return camRepo.addCamera(id, lat, lon);
    }

    public List<Operation> addObservation(String camName, List<ObservationEntity> obs) throws CameraNotFoundException, InvalidIdException {
        camRepo.getCamera(camName);
        List<Operation> operations = new ArrayList<>();
        for (ObservationEntity observation : obs){
            checkId(observation.getType(), observation.getId());
            observation.setDateTime(LocalDateTime.now());
            operations.add(obsRepo.addObservation(observation.getType(), observation.getId(), observation));
        }
        return operations;
    }

    private void checkId(ObservationEntityType type, String id) throws InvalidIdException {
        if (id == null || id.isEmpty() || id.isBlank()) {
            throw new InvalidIdException("Id cannot be null, empty or blank.");
        }

        switch(type) {
            case PERSON:
                try {
                    Long.parseLong(id);
                } catch (NumberFormatException e) {
                    throw new InvalidIdException(id + " for type " + type.toString() + " id too small.");
                }
                break;
            case CAR:
                String licensePlatePattern = "([A-Z][A-Z]|[0-9][0-9])([A-Z][A-Z]|[0-9][0-9])([A-Z][A-Z]|[0-9][0-9])";
                if (!id.matches(licensePlatePattern) 
                    || id.chars().filter(Character::isDigit).count() > 4 
                    || id.chars().filter(Character::isLetter).count() > 4) 
                    throw new InvalidIdException(id + " for type " + type.toString() + " is not a license plate.");
                break;
        }
    }


    public ObservationEntity track(ObservationEntityType type, String id) throws InvalidIdException, NoObservationsException {
        checkId(type, id);
        List<ObservationEntity> obs = obsRepo.getObservations(type, id);
        if (!obs.isEmpty()) {
            int size = obs.size() - 1;
            return obs.get(size);
        }

        throw new NoObservationsException("No observations found for " + id);
    }

    public List<ObservationEntity> trackMatch(ObservationEntityType type, String partId)
            throws InvalidIdException, NoObservationsException {
        List<ObservationEntity> matches = new ArrayList<>();
        String pattern = partId.replace("*", ".*");
        if (pattern == null) pattern = ".*(" + partId + ").*";
        pattern = "^" + pattern + "$";

        for (String id: obsRepo.getTypeObservations(type).keySet()) {
            if (id.matches(pattern)) matches.add(track(type, id)); 
        }

        return matches;
    }

    public List<ObservationEntity> trace(ObservationEntityType type, String id) throws InvalidIdException {
        checkId(type, id);
        List<ObservationEntity> obs = obsRepo.getObservations(type, id);
        if (obs == null) return new ArrayList<>();
        Collections.reverse(obs);
        return obs;
    }

    public void ctrlClear() throws CannotClearServerException {
        obsRepo.clear();
        camRepo.clear();
        if (obsRepo.getRepoSize() != 0 || camRepo.getRepoSize() != 0) {
            throw new CannotClearServerException("Could not clear the server.");
        }
    }
}