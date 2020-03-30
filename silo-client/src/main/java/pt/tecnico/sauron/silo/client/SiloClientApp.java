package pt.tecnico.sauron.silo.client;


public class SiloClientApp {
	
	public static void main(String[] args) {
		System.out.println(SiloClientApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		SiloFrontend silo = new SiloFrontend("localhost", "8080");

		// ctrl_ping test
		System.out.println(silo.ctrlPing("friend"));
	}


	
}
