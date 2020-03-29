package pt.tecnico.sauron.silo;

// Exception for no observation found
public class NoObservationsException extends Exception {
    private static final long serialVersionUID = 1L;

    public NoObservationsException(String message){
        super(message);
    }
}