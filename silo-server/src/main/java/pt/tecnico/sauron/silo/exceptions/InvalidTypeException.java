package pt.tecnico.sauron.silo.exceptions;

//Exception for when camera is not found
public class InvalidTypeException extends Exception{
    private static final long serialVersionUID = 1L;

    public InvalidTypeException(String message){
        super(message);

    }
}