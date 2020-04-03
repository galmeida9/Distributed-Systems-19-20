package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.silo.client.InvalidTypeException;
import pt.tecnico.sauron.silo.client.ObservationObject;
import pt.tecnico.sauron.silo.client.SiloFrontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class EyeApp {

	static boolean DEBUG = false;
	static List<ObservationObject> observations = new ArrayList<>();
	static SiloFrontend frontend;
	static String camName;

	static List<String> validTypes = SiloFrontend.getValidTypes();

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());

		// Add hook to shutdown
		addShutdownHook();

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check if there are enough arguments
		if (args.length != 5) {
			System.out.println("Too few arguments.\nUsage example: $ eye localhost 8080 Tagus 38.737613 -9.303164.\n");
			return;
		}
		else if (!isNumeric(args[1]) || !isDouble(args[3]) || !isDouble(args[4])) {
			System.out.println("Wrong argument types.\nUsage example: $ eye localhost 8080 Tagus 38.737613 -9.303164.\n");
			return;
		}

		camName = args[2];
		double lat = Double.parseDouble(args[3]);
		double lon = Double.parseDouble(args[4]);

		// Join server
		frontend = new SiloFrontend(args[0], args[1]);
		try {
			SiloFrontend.ResponseStatus res = frontend.camJoin(camName, lat, lon);
			if (res.equals(SiloFrontend.ResponseStatus.OK)) {
				log("Login success.");
			}
			else {
				log("Login failure.");
				frontend.exit();
				return;
			}
		}
		catch (Exception e) {
			log(true, e.getMessage());
			frontend.exit();
			return;
		}

		int pause = 0;
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().strip().split(",");

			// Check if it is a comment
			if (line.length == 0 || line[0].startsWith("#")) {
				log("COMMENT");
			}
			// Check if it is a pause command
			else if (line[0].equals("zzz") && line.length == 2 && isNumeric(line[1])) {
				log("PAUSE: " + line[1] + " ms.");
				pause += Integer.parseInt(line[1]);

				try {
					sleep(pause);
					pause = 0;
				}
				catch (InterruptedException e) {
						log("%s", e.getMessage(), true);
				}
			}
			// Check if an observation is sent
			else if (validTypes.contains(line[0]) && line.length == 2) {
				String id = line[1].strip();
				String type = line[0].strip();
				log("OBS - TYPE: " + type + ", ID: " + id);
				System.out.println("Added a " + type + " and id " + id);

				observations.add(new ObservationObject(type, id, camName));
			}
			// Check if it is an empty line and report
			else if (line[0].equals("") && line.length == 1) {
				log("EMPTY LINE");

				report();
			}
			else log("UNKNOWN");
		}

		// On close send observations
		if (!observations.isEmpty()) report();
		frontend.exit();
	}


	/*Helpers */
	private static void report() {
		if (observations.isEmpty()) return;
		try {
			SiloFrontend.ResponseStatus res = frontend.report(observations);
			observations.clear();
			log("REPORT: "+res.toString());
			System.out.println("Report was " + res.toString());
		}
		catch (InvalidTypeException e){
			log(e.getMessage(), true);
		}
	}

	private static boolean isNumeric(String input){
		return input.matches("^[1-9][0-9]*$");
	}

	private static boolean isDouble(String input){
		return input.matches("^(-?)(0|([1-9][0-9]*))(\\.[0-9]+)?$");
	}

	private static void addShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				if (!observations.isEmpty()) report();
			}
		});
	}

	// Log functions
	private static void log(String input){ log(false, "%s", input);}
	private static void log(String format, Object... args){ log(false, format, args);	}
	private static void log(boolean force, String format, Object... args){
		if (DEBUG || force)
			System.out.printf(format+"\n", args);
	}

}
