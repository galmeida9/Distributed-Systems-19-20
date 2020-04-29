package pt.tecnico.sauron.silo.domain;

public class Operation {
    private int opId;
    private String className = "UNKNOWN";

    public Operation(int opId, String className) {
        this.opId = opId;
        this.className = className;
    }

    public int getOpId() {
        return opId;
    }

    public void setOpId(int opId) {
        this.opId = opId;
    }

    public String getClassName() {
        return className;
    }
}
