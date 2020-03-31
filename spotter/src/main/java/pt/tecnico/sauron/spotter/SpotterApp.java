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

        final String host = args[0];
        final String port = args[1];

        silo = new SiloFrontend(host, port);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String[] line = scanner.nextLine().strip().split(" ");

            switch (line[0]) {
                case "spot":
                    if (line.length != 3)
                        System.out.println("Wrong number of arguments");

                    spot(line[1], line[2]);

                case "trail":
                    if (line.length != 3)
                        System.out.println("Wrong number of arguments");

                    trail(line[1], line[2]);

                case "help":
                    help();
                case "ctrl_ping":
                    //TODO
                case "crtl_clear":
                    //TODO
                case "ctrl_init":
                    //TODO
                default:

            }

        }
    }


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

    //TODO: refactor to convert Observations to list in same function

    public static void spot(String type, String id){
        Map<String, String> cams = new HashMap<>();
        List<String> res = new ArrayList<>();

        try{
            if (id.contains("*")){
                List<ObservationObject> obs= silo.trackMatch(type, id);
                obs.sort(Comparator.comparing(ObservationObject::getId).reversed());

                for (ObservationObject observation : obs)
                    res.add(convertToString(observation, cams));
            }
            else {
                ObservationObject observation = silo.track(type, id);
                res.add(convertToString(observation, cams));
            }

            for (String observationStr : res)
                System.out.printf(observationStr);

        } catch (InvalidTypeException e) {
            //TODO
            e.getMessage();
        }
    }


    public static void trail(String type, String id){
	    Map<String, String> cams = new HashMap<>();
	    List<String> res = new ArrayList<>();

        try{
            List<ObservationObject> obs= silo.trace(type, id);
            obs.sort(Comparator.comparing(ObservationObject::getDatetime));

            for (ObservationObject observation : obs)
                res.add(convertToString(observation, cams));

            for (String observationStr : res)
                System.out.printf(observationStr);


        } catch (InvalidTypeException e) {
            //TODO
            e.getMessage();
        }
    }


    public static void help(){
	    System.out.printf("spot <type> <id> : Comando spot\n" +
                "trail <type> <id> : Comando trail");
    }

}
