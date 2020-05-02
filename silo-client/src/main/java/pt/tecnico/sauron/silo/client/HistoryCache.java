package pt.tecnico.sauron.silo.client;

import java.util.*;

class HistoryCache {
    /**
     * Class to represent an observation or camera command
     */
    private class Command {
        public String command;
        public List<ObservationObject> observationList = Collections.emptyList();
        public String camCoordinates = "";
        Map<Integer, Integer> timestamp;

        public Command(String command, List<ObservationObject> observationList, Map<Integer, Integer> timestamp) {
            this.command = command;
            this.observationList = observationList;
            this.timestamp = timestamp;
        }
        public Command(String command, String coordinates, Map<Integer, Integer> timestamp) {
            this.command = command;
            this.camCoordinates = coordinates;
            this.timestamp = timestamp;
        }
    }

    List<Command> cache = new LinkedList<>();
    int maxSize = 10;
    int size = 0;
    int currIndex = 0;

    /**
     * Adds a observation command to the cache
     * @param fullCommand
     * @param objectList
     * @param currTimestamp
     */
    private void addCommand(String fullCommand, List<ObservationObject> objectList, Map<Integer, Integer> currTimestamp) {
        Command cachedCommand = getCommand(fullCommand);
        if (cachedCommand != null) {
            cachedCommand.observationList = objectList;
            return;
        }
        if (size == maxSize) cache.set(currIndex, new Command(fullCommand, objectList, currTimestamp));
        else {
            cache.add(currIndex, new Command(fullCommand, objectList, currTimestamp));
            size++;
        }
        currIndex = (currIndex +1) % maxSize;
    }

    /**
     * Adds a camera command to the cache
     * @param camName
     * @param coordinates
     * @param currTimestamp
     */
    private void addCommand(String camName, String coordinates, Map<Integer, Integer> currTimestamp) {
        Command cachedCommand = getCommand(camName);
        if (cachedCommand != null) {
            cachedCommand.camCoordinates = coordinates;
            return;
        }
        if (size == maxSize) cache.set(currIndex, new Command(camName, coordinates, currTimestamp));
        else {
            cache.add(currIndex, new Command(camName, coordinates, currTimestamp));
            size++;
        }
        currIndex = (currIndex +1) % maxSize;
    }

    /**
     * Returns a Command if it exists in the cache
     * @param fullCommand
     * @return
     */
    Command getCommand(String fullCommand) {
        for (Command command : cache) {
            if (command.command.equals(fullCommand)) return command;
        }
        return null;
    }

    /**
     * Returns if the new command is updated with the timestamp stored (using its timestamp)
     *
     * @param cachedCommand
     * @param otherTimestamp
     * @return
     */
    private boolean isNewCommandUpdated(Command cachedCommand, Map<Integer, Integer> otherTimestamp) {
        for (int inst : cachedCommand.timestamp.keySet()) {
            if (!otherTimestamp.containsKey(inst)) return false;
            if (cachedCommand.timestamp.get(inst) > otherTimestamp.get(inst)) return false;
        }
        return true;
    }

    /**
     * Compare the command given with the one we have on cache if we have it here
     * This method is used for the observation commands
     * @param fullCommand
     * @param receivedObs
     * @param otherTimestamp
     * @return
     */
    public List<ObservationObject> compareCommands(
            String fullCommand, List<ObservationObject> receivedObs, Map<Integer, Integer> otherTimestamp) {
        Command cachedCommand = getCommand(fullCommand);
        if (cachedCommand == null) {
            addCommand(fullCommand, receivedObs, otherTimestamp);
            return receivedObs;
        }
        else if (!isNewCommandUpdated(cachedCommand, otherTimestamp)) {
            return cachedCommand.observationList;
        }
        else if (cachedCommand != null) {
            cachedCommand.observationList = receivedObs;
            return receivedObs;
        }
        return receivedObs;
    }

    /**
     * Compare the command given with the one we have on cache if we have it here
     * This method is used for the camera commands
     * @param camName
     * @param coordinates
     * @param otherTimestamp
     * @return
     */
    public String compareCommands(
            String camName, String coordinates, Map<Integer, Integer> otherTimestamp) {
        Command cachedCommand = getCommand(camName);
        if (cachedCommand == null) {
            addCommand(camName, coordinates, otherTimestamp);
            return coordinates;
        }
        else if (!isNewCommandUpdated(cachedCommand, otherTimestamp)) {
            return cachedCommand.camCoordinates;
        }
        else if (cachedCommand != null) {
            cachedCommand.camCoordinates = coordinates;
            return coordinates;
        }
        return coordinates;
    }

    /**
     * Return camera coordinates if it exists in cache, only used as last result
     * @param camName
     * @return
     */
    public String getCamCoords(String camName) {
        Command cachedCommand = getCommand(camName);
        if (cachedCommand == null) return "";
        else return cachedCommand.camCoordinates;
    }
}
