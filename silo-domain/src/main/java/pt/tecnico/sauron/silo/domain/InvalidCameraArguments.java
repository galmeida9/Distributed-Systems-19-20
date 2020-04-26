package pt.tecnico.sauron.silo.domain;

//Exception for when camera is not found
public class InvalidCameraArguments extends Exception{
    private static final long serialVersionUID = 1L;

    public InvalidCameraArguments(String message){
        super(message);

    }
}