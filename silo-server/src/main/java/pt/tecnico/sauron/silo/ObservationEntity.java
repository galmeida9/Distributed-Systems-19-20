package pt.tecnico.sauron.silo;

import java.time.LocalDateTime;

class ObservationEntity {
    private ObservationEntityType type;
    private String id;
    private LocalDateTime dateTime;

    enum ObservationEntityType {
        PERSON,
        CAR
    }

    public ObservationEntity(ObservationEntityType type, String id, String date) {
        this.type = type;
        this.id = id;
        this.dateTime = LocalDateTime.parse(date);
    }

    public ObservationEntityType getType() {
        return this.type;
    }

    public void setType(ObservationEntityType type) {
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime timeStamp) {
        this.dateTime = timeStamp;
    }
}