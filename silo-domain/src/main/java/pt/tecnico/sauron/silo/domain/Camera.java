package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera {
    private String name;
    private double latitude;
    private double longitude;

    Camera(String name, double lat, double lon) throws InvalidCameraArguments {
		if ( name == null || name.isEmpty() || name.isBlank()
                || lat > 90 || lat < -90
                || lon > 90 || lon < -90
                || Double.isNaN(lat) || Double.isNaN(lon)
                || Double.isInfinite(lat) || Double.isInfinite(lon))
            throw new InvalidCameraArguments("Invalid coordinates.");
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
    }

    public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public List<Double> getCoordinates() {
		return new ArrayList<>(Arrays.asList(latitude, longitude));
	}
}
