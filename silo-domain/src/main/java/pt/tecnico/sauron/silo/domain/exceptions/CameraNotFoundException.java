package pt.tecnico.sauron.silo.domain.exceptions;

/**
 * Exception for when a camera is not found with given identifier
 */
public class CameraNotFoundException extends Exception{
    private static final long serialVersionUID = 1L;

    public CameraNotFoundException(String message){
        super(message);

    }
}