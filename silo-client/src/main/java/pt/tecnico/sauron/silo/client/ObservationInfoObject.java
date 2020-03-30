package pt.tecnico.sauron.silo.client;

public class ObservationInfoObject {
    private ObservationObject obs;
    private double lat;
    private double lon;

    public ObservationInfoObject(ObservationObject obs, double lat, double lon) {
        this.obs = obs;
        this.lat = lat;
        this.lon = lon;
    }

    public ObservationObject getObs() {
        return this.obs;
    }

    public void setObs(ObservationObject obs) {
        this.obs = obs;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}