package pt.tecnico.sauron.silo.client;

import java.time.LocalDateTime;

public class ObservationObject {
    private String type;
    private String id;
    private LocalDateTime datetime;
    private String camName;
    
    public ObservationObject(String type, String id, String camName){
        this.type = type;
        this.id = id;
        this.camName = camName;
    }
    
    ObservationObject(String type, String id, LocalDateTime datetime, String camName){
        this(type, id, camName);
        this.datetime = datetime;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDatetime() {
        return this.datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getCamName() {
        return this.camName;
    }

    public void setCamName(String camName) {
        this.camName = camName;
    }
}