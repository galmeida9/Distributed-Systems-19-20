package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidIdException;

public class Operation {
    private int opId = -1;
    private int instance = -1;
    private String className = "UNKNOWN";

    /**
     * Creates an operation given its specific name
     * @param className
     */
    public Operation(String className) {
        this.className = className;
    }

    /**
     * Returns the operation id
     * @return int
     */
    public int getOpId() {
        return opId;
    }

    /**
     * Sets the operation id
     * @param opId
     */
    public void setOpId(int opId) {
        this.opId = opId;
    }

    /**
     * Returns the instance number of the replica that created the operation
     * @return int
     */
    public int getInstance() {
        return instance;
    }

    /**
     * Sets the instance number of the replica that created the operation
     * @param instance
     */
    public void setInstance(int instance) {
        this.instance = instance;
    }

    /**
     * Returns the name of the specific operation
     * @return
     */
    public String getClassName() {
        return className;
    }

    /**
     * Method created to specific operations store themselves in a operationStore
     * Does not apply to the abstract Operation
     * @param operationStore where operations are stored
     * @return Operation
     * @throws CameraNotFoundException
     * @throws InvalidIdException
     */
    public Operation addToStore(OperationStore operationStore) throws InvalidCameraArguments, CameraNotFoundException, InvalidIdException {
        return this;
    }

    /**
     * Return string to display this operation
     * @return String
     */
    @Override
    public String toString() {
        return className;
    }
}
