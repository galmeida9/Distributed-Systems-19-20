package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.silo.client.exceptions.FailedConnectionException;
import pt.tecnico.sauron.silo.client.exceptions.InvalidTypeException;
import pt.tecnico.sauron.silo.client.ObservationObject;
import pt.tecnico.sauron.silo.client.exceptions.ReportException;
import pt.tecnico.sauron.silo.client.SiloFrontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class EyeApp {

	static List<ObservationObject> observations = new ArrayList<>();
	static SiloFrontend frontend;
	static String camName;

	static List<String> validTypes = SiloFrontend.getValidTypes();

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());

		// Add hook to shutdown by CTRL-C
		addShutdownHook();

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check if there are enough arguments
		if (args.length > 6 || args.length < 5) {
			System.out.println("Wrong number of arguments.\nUsage example: $ eye localhost 2181 Tagus 38.737613 -9.303164.\n");
			return;
		}
		// Check if 4º and 5º arguments are doubles
		else if (!isDouble(args[3]) || !isDouble(args[4])) {
			System.out.println("Wrong argument types.\nUsage example: $ eye localhost 2181 /grpc/sauron/silo/1 Tagus 38.737613 -9.303164.\n");
			return;
		}
		// Check if last argument is an number
		if (args.length == 6) {
			try {
				Integer.parseInt(args[5]);
			} catch (NumberFormatException e) {
				System.out.println("Wrong argument types.\nUsage example: $ eye localhost 2181 /grpc/sauron/silo/1 Tagus 38.737613 -9.303164 1.\n");
				return;
			}
		}


		camName = args[2];
		double lat = Double.parseDouble(args[3]);
		double lon = Double.parseDouble(args[4]);
		String zooHost = args[0];
		String zooPort = args[1];
		int instance = -1;
		if (args.length == 6) instance = Integer.parseInt(args[5]);

		// Join server
		frontend = new SiloFrontend(zooHost, zooPort, instance);
		//TODO: Check for server not connected

		try {
			frontend.camJoin(camName, lat, lon);
			System.out.println("Login success.");
		}
		catch (Exception e) {
			System.out.println("Login failure.");
			frontend.exit();
			return;
		}

		int pause = 0;

		//TODO: make these next lines one function
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().strip().split(",");

			// Check if it is a comment
			if (line.length == 0 || line[0].startsWith("#")) {
				debug("COMMENT");
			}
			// Check if it is a pause command
			else if (line[0].equals("zzz") && line.length == 2 && isNumeric(line[1])) {
				debug("PAUSE: " + line[1] + " ms.");
				pause += Integer.parseInt(line[1]);

				try {
					sleep(pause);
					pause = 0;
				}
				catch (InterruptedException e) {
					debug(e.getMessage());
				}
			}
			// Check if an observation is sent
			else if (validTypes.contains(line[0]) && line.length == 2) {
				String id = line[1].strip();
				String type = line[0].strip();
				System.out.println("Added a " + type + " with id " + id + ".");

				observations.add(new ObservationObject(type, id, camName));
			}
			// Check if it is an empty line and report
			else if (line[0].equals("") && line.length == 1) {
				debug("EMPTY LINE");
				report();
			}
			else if (line[0].equals("exit") && line.length == 1) {
				break;
			}
			else debug("UNKNOWN");
		}

		// On close send observations
		if (!observations.isEmpty()) report();
		frontend.exit();
	}


	/*Helpers */
	private static void report() {
		if (observations.isEmpty()) return;
		try {
			frontend.report(observations);
			observations.clear();
			System.out.println("Report was OK");
		}
		catch (InvalidTypeException | ReportException e){
			debug(e.getMessage());
			System.out.println("Report was NOK");
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

	/*
	 *   Debug
	 */
	private static final boolean DEBUG_FLAG = (System.getenv("debug").equals("true"));

	private static void debug(String debugMessage){
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

}
