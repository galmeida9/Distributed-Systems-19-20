package pt.tecnico.sauron.silo;

// Exception for Invalid person id size
public class InvalidPersonIdException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidPersonIdException(String message){
        super(message);
    }
}