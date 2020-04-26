package pt.tecnico.sauron.silo.client;

//Exception for when camera is not found
public class InvalidCameraArgumentsException extends Exception{
    private static final long serialVersionUID = 1L;

    public InvalidCameraArgumentsException(String message){
        super(message);

    }
}