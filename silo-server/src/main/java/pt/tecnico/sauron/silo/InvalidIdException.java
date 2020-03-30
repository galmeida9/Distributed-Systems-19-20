package pt.tecnico.sauron.silo;

// Exception for Invalid person id size
public class InvalidIdException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidIdException(String message){
        super(message);
    }
}