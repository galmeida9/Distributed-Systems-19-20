package pt.tecnico.sauron.silo.domain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CameraRepository {
    private Map<String, Camera> cameras = new ConcurrentHashMap<>();

    public Camera getCamera(String id) throws CameraNotFoundException {
        if (id.isBlank() || id.isEmpty() || !cameras.containsKey(id))
            throw new CameraNotFoundException("Camera '" + id + "' does not exist.");
        return cameras.get(id);
    }

    public void addCamera(String id, double lat,  double lon) throws InvalidCameraArguments {
        if (cameras.containsKey(id) &&
                (cameras.get(id).getLatitude() != lat || cameras.get(id).getLongitude() != lon)) {
            throw new InvalidCameraArguments("Camera already exists.");
        }
        cameras.put(id, new Camera(id, lat, lon));
    }

    public List<Double> getCameraInfo(String id) throws CameraNotFoundException {
        if (id == null || id.isEmpty() || id.isBlank() || !cameras.containsKey(id))
            throw new CameraNotFoundException("Camera '" + id + "' does not exist.");
        return cameras.get(id).getCoordinates();
    }

    public int getRepoSize() {
        return cameras.size();
    }

    public void clear() {
        cameras.clear();
    }
}