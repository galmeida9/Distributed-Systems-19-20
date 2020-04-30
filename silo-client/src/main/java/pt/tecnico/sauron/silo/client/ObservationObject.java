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

    
    /**
     * Gets the type of an ObservationObject object 
     * @return String
     */
    public String getType() {
        return this.type;
    }

    
    /** 
     * Sets the type for an ObservationObject object
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    
    /** 
     * Gets the id of an ObservationObject object
     * @return String
     */
    public String getId() {
        return this.id;
    }

    
    /** 
     * Sets the id for an ObservationObject object
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    
    /** 
     * Gets da date time from an ObservationObject object
     * @return LocalDateTime
     */
    public LocalDateTime getDatetime() {
        return this.datetime;
    }

    
    /** 
     * Sets the date time for an ObservationObject object
     * @param datetime
     */
    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    
    /** 
     * Gets the camera name of an ObservationObject object
     * @return String
     */
    public String getCamName() {
        return this.camName;
    }

    
    /** 
     * Sets the camera name for an ObservationObject object
     * @param camName
     */
    public void setCamName(String camName) {
        this.camName = camName;
    }
}