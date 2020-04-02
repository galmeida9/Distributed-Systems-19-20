package pt.tecnico.sauron.silo.client;

//Exception for when camera is not found
public class ReportException extends Exception{
    private static final long serialVersionUID = 1L;

    public ReportException(String message){
        super(message);

    }
}