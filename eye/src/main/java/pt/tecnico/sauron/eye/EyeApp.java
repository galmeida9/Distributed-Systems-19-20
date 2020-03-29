package pt.tecnico.sauron.eye;


import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.client.SiloFrontend;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class EyeApp {

	static boolean DEBUG = true;
	static List<SiloFrontend.ObservationObject> observations = new ArrayList<>();
	static SiloFrontend frontend;
	static String camName;

	public static void main(String[] args) {
		System.out.println(EyeApp.class.getSimpleName());

		// Add hook to shutdown
		addShutdownHook();

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		//FIXME: Add arguments
		camName = "Tagus";
		double lat = 38.737613, lon = -9.303164;

		// Join server
		frontend = new SiloFrontend("localhost", "8080");
		try {
			String res = frontend.camJoin(camName, lat, lon);
			log(true, res);
			// TODO: fix verification
			if (res.contains("error")) {
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
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()){
				String[] line = scanner.nextLine().strip().split(",");

				// Check if it is a comment
				if (line.length == 0 || line[0].startsWith("#")) {
					log("COMMENT"); continue;
				}

				switch (line[0]) {
					case "zzz": // Pause on processing
						if (line.length != 2 || !isNumeric(line[1])) break;
						log("PAUSE: " + line[1] + " ms.");
						//TODO: Sleep for x milliseconds DOUBT, in send or in processing?

						pause += Integer.parseInt(line[1]);
						break;
					case "car":
					case "person": // Add observation
						if (line.length != 2) break;
						String id = line[1].strip(), type = line[0].strip();
						log("OBS: TYPE: " + type + ", ID: " + id);

						//TODO: Check if we can use timestamp of google
						Instant time = Instant.now();
						observations.add(
								new SiloFrontend.ObservationObject(type, id,
										Timestamp.newBuilder().setSeconds(time.getEpochSecond()).setNanos(time.getNano()).build()
								));
						break;
					case "": // Empty line
						if (line.length != 1) break;
						log("EMPTY LINE");

						sleep(pause);
						pause = 0;
						log(true, report(camName));
						break;
					default: // Unknown commands
						log("UNKNOWN");
						break;
				}
			}
			// On close send observations
			if (observations.size() > 0) report(camName);
		}
		// FIXME: Fix exceptions
		catch (Exception e) {
			log(e.getMessage());
		}
		finally {
			frontend.exit();
		}
	}


	/*Helpers */

	private static String report(String camName) throws SiloFrontend.InvalidTypeException {
		String res = frontend.report(camName, observations);
		observations.clear();
		return res;
	}

	private static boolean isNumeric(String input){
		return input.matches("^[1-9][0-9]*$");
	}

	private static void addShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				try { if (observations.size()>0) log(true, report(camName)); }
				catch (SiloFrontend.InvalidTypeException e) { log(true, e.getMessage()); }
			}
		});
	}

	// Log functions
	private static void log(String input, boolean force){ log(force, "%s", input);}
	private static void log(String input){ log(false, "%s", input);}
	private static void log(String format, Object... args){ log(false, format, args);	}
	private static void log(boolean force, String format, Object... args){
		if (DEBUG || force)
			System.out.printf(format+"\n", args);
	}

}
