package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.sauron.silo.client.exceptions.FailedConnectionException;
import pt.tecnico.sauron.silo.client.exceptions.InvalidCameraArgumentsException;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.fail;


public class BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	static SiloFrontend frontEnd;
	
	@BeforeAll
	public static void oneTimeSetup () throws IOException {
		testProps = new Properties();
		
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		String HOST = testProps.getProperty("zoo.host");
		String PORT = testProps.getProperty("zoo.port");
		try {
			frontEnd = new SiloFrontend(HOST, PORT, -1);
		} catch (FailedConnectionException e) {
			fail("Should not have thrown any exception.");
		}

	}
	
	@AfterAll
	public static void cleanup() {
		
	}

}
