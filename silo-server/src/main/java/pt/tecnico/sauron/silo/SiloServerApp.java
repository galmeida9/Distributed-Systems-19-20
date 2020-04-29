package pt.tecnico.sauron.silo;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;

import java.io.IOException;
import java.util.Scanner;

public class SiloServerApp {
	
	public static void main(String[] args) throws IOException, InterruptedException{
		System.out.println(SiloServerApp.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", SiloServerApp.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String host = args[2];
		final String port = args[3];
		final int instance = Integer.parseInt(args[4]);
		final String root = "/grpc/sauron/silo";
		final String path = root + '/' + Integer.toString(instance);
		final BindableService impl = new SiloServerImpl(instance, root, zooHost, zooPort);
		ZKNaming zkNaming = null;

		try {
			//TODO: Check if zooKeeper is not on
			zkNaming = new ZKNaming(zooHost, zooPort);
			//TODO: Check if is not replacing another server
			zkNaming.rebind(path, host, port);

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Replica " + instance + " has started");

			// Create new thread where we wait for user to end the server
			new Thread(() -> {
				System.out.println("<Press enter to shutdown>");
				new Scanner(System.in).nextLine();

				server.shutdown();
			}).start();

			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (zkNaming != null) {
				// remove
				try {
					zkNaming.unbind(path, host, port);
				} catch (Exception e) {
					//FIXME: Bad catching
					System.out.println(e.getMessage());
				}
			}
			System.exit(0);
		}
	}
	
}
