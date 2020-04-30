package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidIdException;

import java.time.LocalDateTime;
import java.util.List;

public class ObservationEntity extends Operation {
    private ObservationEntityType type;
    private String id;
    private LocalDateTime dateTime;
    private String camName;

    public enum ObservationEntityType {
        PERSON,
        CAR
    }

    /**
     * Creates an ObservationEntity, that extends an Operation
     * @param type
     * @param id
     * @param camName
     */
    public ObservationEntity(ObservationEntityType type, String id, String camName) {
        super(ObservationEntity.class.getSimpleName());
        this.type = type;
        this.id = id;
        this.camName = camName;
    }

    /**
     * Returns type of the observation
     * @return ObservationEntityType
     */
    public ObservationEntityType getType() {
        return this.type;
    }

    /**
     * Returns the id of whom is observed
     * @return String
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets id of whom is observed
     * @param id String
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the time when this observation happened
     * @return LocalDateTime
     */
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    /**
     * Sets the date when this observation happened
     * @param timeStamp LocalDateTime
     */
    public void setDateTime(LocalDateTime timeStamp) {
        this.dateTime = timeStamp;
    }

    /**
     * Returns the name of the camera that observed
     * @return String
     */
    public String getCamName() {
        return this.camName;
    }

    /**
     * Adds itself to an object that stores operations and returns itself
     * @param operationStore
     * @return
     * @throws CameraNotFoundException
     * @throws InvalidIdException
     */
    @Override
    public Operation addToStore(OperationStore operationStore) throws CameraNotFoundException, InvalidIdException {
        operationStore.addObservation(getCamName(), List.of(this));
        return this;
    }
}