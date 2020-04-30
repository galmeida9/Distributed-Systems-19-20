package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidIdException;

import java.util.List;

public interface OperationStore {
    /**
     * Adds a camera to the store, with the following arguments
     * @param id
     * @param lat
     * @param lon
     * @return Operation added
     * @throws InvalidCameraArguments
     */
    Operation addCamera(String id, double lat, double lon) throws InvalidCameraArguments;

    /**
     * Adds observations to the store, with the following arguments
     * @param camName
     * @param obs
     * @return List with operations added
     * @throws CameraNotFoundException
     * @throws InvalidIdException
     */
    List<Operation> addObservation(String camName, List<ObservationEntity> obs) throws CameraNotFoundException, InvalidIdException;
}
