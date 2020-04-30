package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera extends Operation {
    private String name;
    private double latitude;
    private double longitude;

	/**
	 * Creates a Camera, given its name, latitude, longitude
	 * Might throw an invalidCameraArguments if there are problems with its arguments
	 * @param name
	 * @param lat
	 * @param lon
	 * @throws InvalidCameraArguments
	 */
    public Camera(String name, double lat, double lon) throws InvalidCameraArguments {
		super(Camera.class.getSimpleName());
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

	/**
	 * Returns the name of the camera
	 * @return Name of camera
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets name of the Camera
	 * @param name Name of camera
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the latitude of this camera
	 * @return double
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * Returns the longitude of this camera
	 * @return double
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * Returns the coordinates associated with this camera
	 * @return List with Latitude and Longitude
	 */
	public List<Double> getCoordinates() {
		return new ArrayList<>(Arrays.asList(latitude, longitude));
	}

	/**
	 * Adds itself to an object that stores operations and returns itself
	 * @param operationStore where operations are stored
	 * @return
	 * @throws InvalidCameraArguments
	 */
	@Override
	public Operation addToStore(OperationStore operationStore) throws InvalidCameraArguments {
		operationStore.addCamera(getName(), getLatitude(), getLongitude());
		return this;
	}

	/**
	 * Return string to display this operation
	 * @return String
	 */
	@Override
	public String toString() {
		return "Camera " + getName();
	}
}
