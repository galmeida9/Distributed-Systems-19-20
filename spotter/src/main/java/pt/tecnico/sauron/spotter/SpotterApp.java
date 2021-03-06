package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.exceptions.*;
import pt.tecnico.sauron.silo.client.ObservationObject;
import pt.tecnico.sauron.silo.client.SiloFrontend;

import java.util.*;


public class SpotterApp {

    private static SiloFrontend silo;
	
	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check if there is enough arguments
        if (args.length < 2 || args.length > 3) {
            System.out.printf("Wrong number of arguments%nUsage: spotter host port%n");
            return;
        }
        // Check if last argument is an integer between 0 and 9
        int inst = -1;
        if (args.length == 3) {
            try {
                inst = Integer.parseInt(args[2]);
                if (inst < 0 || inst > 9) {
                    System.out.printf("Wrong type of arguments%nExample: spotter localhost 2181 1%n");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.printf("Instance number is bigger than 9 or smaller than 0" +
                        "%nExample: spotter localhost 2181 1%n");
                return;
            }
        }

        int instance = inst;
        try {
            silo = new SiloFrontend(args[0], args[1], instance);
        } catch (FailedConnectionException e) {
            System.out.println(e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().strip().split(" ", 2);

            switch (line[0]) {
                case "spot":
                    String[] commandArgs = line[1].split(" ");
                    if (commandArgs.length != 2){
                        System.out.println("Wrong number of arguments");
                        System.out.println("Usage: spot <type> <id/partId>");
                        break;
                    }
                    spot(commandArgs[0], commandArgs[1]);
                    break;

                case "trail":
                    String[] arguments = line[1].split(" ");
                    if (arguments.length != 2){
                        System.out.println("Wrong number of arguments");
                        System.out.println("Usage: trail <type> <id>");
                        break;
                    }
                    trail(arguments[0], arguments[1]);
                    break;

                case "help":
                    help();
                    break;

                case "ctrl_ping":
                    if (line.length < 2){
                        System.out.println("Wrong number of arguments");
                        System.out.println("Usage: ctrl_ping <message>");
                        break;
                    }
                    try {
                        System.out.println(silo.ctrlPing(line[1]));
                    } catch (FailedConnectionException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                    break;

                case "ctrl_clear":
                    try {
                        silo.ctrlClear();
                        System.out.println("CtrlClear was OK");
                    } catch (CannotClearServerException e) {
                        System.out.println("CtrlClear was NOK: " + e.getMessage());
                    } catch (FailedConnectionException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                    break;
                case "ctrl_init":
                    try {
                        silo.ctrlInit();
                    } catch (FailedConnectionException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                    System.out.println("CtrlInit was OK");
                    break;

                case "exit":
                    scanner.close();
                    silo.exit();
                    return ;

                default:
                    System.out.println("Command does not exist");
                    help();
            }
        }
        silo.exit();
    }

    /**
     * Converts observation and camera to a formatted line in a string
     * @param obs
     * @param cams
     * @return
     */
    public static String convertToString(ObservationObject obs, Map<String, String> cams){
	    String camName = obs.getCamName();
	    String coordinates = "";

	    //Obtain coordinates of camera
	    if (cams.containsKey(camName)){
	        coordinates = cams.get(camName);
        }
	    else{
            try {
                coordinates = silo.camInfo(camName);
                cams.put(camName, coordinates);
            } catch (CameraNotFoundException e) {
                System.out.println(e.getMessage());

                return "";
            } catch (FailedConnectionException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        }

	    //Convert observation info into string
	    return String.valueOf(obs.getType()) + ','
                + obs.getId() + ','
                + obs.getDatetime() + ','
                + camName + ','
                + coordinates +'\n';
    }

    /**
     * Process observations to string and prints on terminal
     * @param obs
     */
    public static void processObservations(List<ObservationObject> obs){
	    Map<String, String> cams = new HashMap<>();
	    List<String> res = new ArrayList<>();

	    for (ObservationObject observation : obs)
	        res.add(convertToString(observation, cams));

	    for (String observationStr : res)
	        System.out.print(observationStr);
    }

    /**
     * Process command spot
     * @param type
     * @param id
     */
    public static void spot(String type, String id){
	    try{
	        if (id.contains("*")){
	            List<ObservationObject> obs = silo.trackMatch(type, id);
	            obs.sort(Comparator.comparing(ObservationObject::getId));
	            processObservations(obs);
            }
	        else {
	            List<ObservationObject> obs = new ArrayList<>();
	            obs.add(silo.track(type, id));
	            processObservations(obs);
            }
	    } catch (InvalidTypeException | NoObservationsFoundException e) {
            System.out.println(e.getMessage());
        } catch (FailedConnectionException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Processes command trail, by receiving from silo and ordering them
     * @param type
     * @param id
     */
    public static void trail(String type, String id){
        try{
            List<ObservationObject> obs = silo.trace(type, id);
            obs.sort(Comparator.comparing(ObservationObject::getDatetime).reversed());
            processObservations(obs);
        } catch (InvalidTypeException | NoObservationsFoundException e) {
            System.out.println(e.getMessage());
        } catch (FailedConnectionException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Prints to terminal the commands possible
     */
    public static void help(){
	    System.out.print(
                "Spot command - shows latest observation of a person or object-> spot <type> <id/partId>  Command spot\n" +
                "Trail command - shows trail of a person or object -> trail <type> <id>\n" +
                "Ping command - indicates state of server -> ctrl_ping <message>\n" +
                "Clear command - cleans state of server -> ctrl_clear\n" +
                "Init command - initializes parameters -> ctrl_init\n" +
                "Exit command - exits Spotter -> exit\n"
        );
    }
}
