package pt.tecnico.sauron.silo.domain.exceptions;

/**
 * Exception for when a camera is being created with wrong arguments
 */
public class InvalidCameraArguments extends Exception{
    private static final long serialVersionUID = 1L;

    public InvalidCameraArguments(String message){
        super(message);

    }
}