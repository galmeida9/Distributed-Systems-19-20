package pt.tecnico.sauron.silo.client.exceptions;

//Exception for when report operation failed
public class ReportException extends Exception{
    private static final long serialVersionUID = 1L;

    public ReportException(String message){
        super(message);

    }
}