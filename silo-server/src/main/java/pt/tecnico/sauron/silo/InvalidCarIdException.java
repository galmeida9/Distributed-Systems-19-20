package pt.tecnico.sauron.silo;

// Exception for Invalid car id
public class InvalidCarIdException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidCarIdException(String message){
        super(message);
    }
}