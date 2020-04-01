package pt.tecnico.sauron.spotter;


import pt.tecnico.sauron.silo.client.InvalidTypeException;
import pt.tecnico.sauron.silo.client.ObservationObject;
import pt.tecnico.sauron.silo.client.SiloFrontend;

import java.util.*;


public class SpotterApp {

    static boolean DEBUG = true;
    private static SiloFrontend silo;
	
	public static void main(String[] args) {
		System.out.println(SpotterApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

        if (args.length != 2) {
            System.out.println("Wrong number of arguments");
            System.out.printf("Usage: java %s host port%n", SpotterApp.class.getName());
            return;
        }
        silo = new SiloFrontend(args[0], args[1]);

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().strip().split(" ", 2);
            String[] commandArgs;

            switch (line[0]) {
                case "spot":
                    commandArgs = line[1].split(" ");
                    if (commandArgs.length != 2){
                        System.out.println("Wrong number of arguments");
                        continue;
                    }
                    spot(commandArgs[0], commandArgs[1]);
                    break;

                case "trail":
                    String[] arguments = line[1].split(" ");
                    if (arguments.length != 2){
                        System.out.println("Wrong number of arguments");
                        continue;
                    }
                    trail(arguments[0], arguments[1]);
                    break;

                case "help":
                    if (line.length > 1){
                        //TODO
                    }
                    help();
                    break;

                case "ctrl_ping":
                    if (line.length < 1){
                        //TODO
                    }
                    System.out.printf(silo.ctrlPing(line[1]));
                    break;

                /*case "crtl_clear":
                    //TODO
                    SiloFrontend.ResponseStatus res = silo.ctrlClear();

                case "ctrl_init":
                    //TODO
                    SiloFrontend.ResponseStatus res = silo.ctrlInit();*/


                case "exit":
                    scanner.close();
                default:

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
	        coordinates = silo.camInfo(camName);
	         cams.put(camName, coordinates);
        }

	    //Convert observation info into string
	    return String.valueOf(obs.getType()) + ','
                + obs.getId() + ','
                + obs.getDatetime() + ','
                + camName + ','
                + coordinates;
    }


    public static void processObservations(List<ObservationObject> obs){
	    Map<String, String> cams = new HashMap<>();
	    List<String> res = new ArrayList<>();

	    for (ObservationObject observation : obs)
	        res.add(convertToString(observation, cams));

	    for (String observationStr : res)
	        System.out.printf(observationStr);
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

	    } catch (InvalidTypeException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void trail(String type, String id){
        try{
            List<ObservationObject> obs= silo.trace(type, id);
            obs.sort(Comparator.comparing(ObservationObject::getDatetime).reversed());
            processObservations(obs);

        } catch (InvalidTypeException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void help(){
	    System.out.printf("Spot command - shows latest observation of a person or object-> spot <type> <id>  Comando spot\n" +
                "Trail command - shows trail of a person or object -> trail <type> <id>\n" +
                "Ping command - indicates state of server -> ctrl_ping <message>\n" +
                "Clear command - cleans state of server -> ctrl_clear\n" +
                "Init command - initializes parameters -> ctrl_init\n" +
                "Exit command - exits Spotter -> exit"
        );
    }
}
