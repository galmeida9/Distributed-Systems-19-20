package pt.tecnico.sauron.silo.domain;

public class Operation {
    private int opId = -1;
    private int instance = -1;
    private String className = "UNKNOWN";

    public Operation(String className) {
        this.className = className;
    }

    public int getOpId() {
        return opId;
    }

    public void setOpId(int opId) {
        this.opId = opId;
    }

    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return className;
    }
}
