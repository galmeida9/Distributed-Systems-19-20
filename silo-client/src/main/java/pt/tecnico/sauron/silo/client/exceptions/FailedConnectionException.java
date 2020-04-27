package pt.tecnico.sauron.silo.client.exceptions;

//Exception for when the connection is failed
public class FailedConnectionException extends Exception{
    private static final long serialVersionUID = 1L;

    public FailedConnectionException(String message){
        super(message);

    }
}