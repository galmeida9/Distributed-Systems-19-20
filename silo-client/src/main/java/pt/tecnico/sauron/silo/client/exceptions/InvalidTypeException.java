package pt.tecnico.sauron.silo.client.exceptions;

// Exception for Invalid object type
public class InvalidTypeException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidTypeException(String message){
        super(message);
    }
}