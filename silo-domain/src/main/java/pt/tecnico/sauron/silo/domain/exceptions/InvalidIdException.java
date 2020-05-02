package pt.tecnico.sauron.silo.domain.exceptions;

/**
 * Exception for when a operation is created with a identifier that doesn't match type
 */
public class InvalidIdException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidIdException(String message){
        super(message);
    }
}