package pt.tecnico.sauron.silo.client;

//Exception for when camera is not found
public class CameraNotFoundException extends Exception{
    private static final long serialVersionUID = 1L;

    public CameraNotFoundException(String message){
        super(message);

    }
}