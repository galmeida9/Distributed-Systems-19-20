package pt.tecnico.sauron.silo.client.exceptions;

//Exception for when camera is not found
public class NoObservationsFoundException extends Exception{
    private static final long serialVersionUID = 1L;

    public NoObservationsFoundException(String message){
        super(message);

    }
}