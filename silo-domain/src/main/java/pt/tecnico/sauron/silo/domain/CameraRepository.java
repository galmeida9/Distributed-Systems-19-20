package pt.tecnico.sauron.silo.domain;

import pt.tecnico.sauron.silo.domain.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.domain.exceptions.InvalidCameraArguments;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CameraRepository {
    private Map<String, Camera> cameras = new ConcurrentHashMap<>();

    /**
     * Returns the camera with the id given
     * Throws cameraNotFound if no camera with the id was found
     * @param id Identifier of the camera
     * @return Camera
     * @throws CameraNotFoundException
     */
    public Camera getCamera(String id) throws CameraNotFoundException {
        if (id.isBlank() || id.isEmpty() || !cameras.containsKey(id))
            throw new CameraNotFoundException("Camera '" + id + "' does not exist.");
        return cameras.get(id);
    }

    /**
     * Adds a camera to the repository given the id, latitude and longitude
     * Throws InvalidCameraArguments if there are problems with the arguments
     * @param id
     * @param lat
     * @param lon
     * @return Camera as an Operation
     * @throws InvalidCameraArguments
     */
    public Operation addCamera(String id, double lat,  double lon) throws InvalidCameraArguments {
        if (cameras.containsKey(id) &&
                (cameras.get(id).getLatitude() != lat || cameras.get(id).getLongitude() != lon)) {
            throw new InvalidCameraArguments("Camera already exists.");
        }
        Camera camera = new Camera(id, lat, lon);
        cameras.put(id, camera);
        return camera;
    }

    /**
     * Returns the coordinates of the camera with the given identifier
     * @param id
     * @return List with Latitude and Longitude
     * @throws CameraNotFoundException
     */
    public List<Double> getCameraInfo(String id) throws CameraNotFoundException {
        if (id == null || id.isEmpty() || id.isBlank() || !cameras.containsKey(id))
            throw new CameraNotFoundException("Camera '" + id + "' does not exist.");
        return cameras.get(id).getCoordinates();
    }

    /**
     * Returns the size of this repository
     * @return
     */
    public int getRepoSize() {
        return cameras.size();
    }

    /**
     * Deletes all cameras in this repository
     */
    public void clear() {
        cameras.clear();
    }
}