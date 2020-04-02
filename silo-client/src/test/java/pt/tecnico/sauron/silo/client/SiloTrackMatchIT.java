package pt.tecnico.sauron.silo.client;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.*;

import org.junit.jupiter.api.*;

public class SiloTrackMatchIT extends BaseIT{

    // static members
    private static String HOST = "localhost";
    private static String PORT = "8080";
    private static SiloFrontend frontend = new SiloFrontend(HOST, PORT);

    private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1";
	private static String PERSON_ID_INVALID = "1111a";
	private static String PERSON_ID_PART = "1*";
	private static String CAR = "car";
	private static String CAR_ID_VALID = "20SD21";
	private static String CAR_ID_INVALID = "202122";
	private static String CAR_ID_PART = "20*";


    	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
    }

	@AfterAll
	public static void oneTimeTearDown() {
        frontend.ctrlClear();
		frontend.exit();
	}

	// initialization and clean-up for each test

	@BeforeEach
	public void setUp() {

	}

	@AfterEach
	public void tearDown() {

	}





    @Test
    public void sucessTest() {
    }


}
