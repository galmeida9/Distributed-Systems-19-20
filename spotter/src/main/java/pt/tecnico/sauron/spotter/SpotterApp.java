package pt.tecnico.sauron.spotter;

import pt.tecnico.sauron.silo.client.CameraNotFoundException;
import pt.tecnico.sauron.silo.client.InvalidTypeException;
import pt.tecnico.sauron.silo.client.NoObservationsFoundException;
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

        if (args.length < 2 || args.length > 3) {
            System.out.println("Wrong number of arguments");
            System.out.printf("Usage: java %s host port%n", SpotterApp.class.getName());
            return;
        }

        //TODO: Add from args
        int instance = -1;
        if (args.length == 3) instance = Integer.parseInt(args[2]);
        silo = new SiloFrontend(args[0], args[1], instance);
        //TODO: Check for server not connected

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
                    System.out.println(silo.ctrlPing(line[1]));
                    break;

                case "ctrl_clear":
                    SiloFrontend.ResponseStatus res = silo.ctrlClear();
                    System.out.println("CtrlClear was " + res.toString());
                    break;

                case "ctrl_init":
                    SiloFrontend.ResponseStatus status = silo.ctrlInit();
                    System.out.println("CtrlInit was " + status.toString());
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


    /*
    *   Auxiliary functions
    */

    public static String convertToString(ObservationObject obs, Map<String, String> cams){
	    String camName = obs.getCamName();
	    String coordinates;

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
            }
        }

	    //Convert observation info into string
	    return String.valueOf(obs.getType()) + ','
                + obs.getId() + ','
                + obs.getDatetime() + ','
                + camName + ','
                + coordinates +'\n';
    }


    public static void processObservations(List<ObservationObject> obs){
	    Map<String, String> cams = new HashMap<>();
	    List<String> res = new ArrayList<>();

	    for (ObservationObject observation : obs)
	        res.add(convertToString(observation, cams));

	    for (String observationStr : res)
	        System.out.print(observationStr);
    }


    /*
    *  Command functions
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
        }
    }


    public static void trail(String type, String id){
        try{
            List<ObservationObject> obs= silo.trace(type, id);
            obs.sort(Comparator.comparing(ObservationObject::getDatetime).reversed());
            processObservations(obs);

        } catch (InvalidTypeException | NoObservationsFoundException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void help(){
	    System.out.print("Spot command - shows latest observation of a person or object-> spot <type> <id/partId>  Comando spot\n" +
                "Trail command - shows trail of a person or object -> trail <type> <id>\n" +
                "Ping command - indicates state of server -> ctrl_ping <message>\n" +
                "Clear command - cleans state of server -> ctrl_clear\n" +
                "Init command - initializes parameters -> ctrl_init\n" +
                "Exit command - exits Spotter -> exit\n"
        );
    }
}
