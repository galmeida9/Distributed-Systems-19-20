package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.*;

import org.junit.jupiter.api.*;

public class SiloTraceIT extends BaseIT {

    	// static members
    private static String HOST = "localhost";
    private static String PORT = "8080";
    private static SiloFrontend frontend = new SiloFrontend(HOST, PORT);

    private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1";
	private static String PERSON_ID_INVALID = "1111a";
	private static String CAR = "car";
	private static String CAR_ID_VALID = "20SD21";
	private static String CAR_ID_INVALID = "202122";


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

    @Test
	public void invalidTypeTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.track("object", PERSON_ID_VALID);
		});
	}

    @Test
	public void emptyTypeTest(){
		// Should throw invalid type exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace("", PERSON_ID_VALID);
		});
	}

	@Test
	public void emptyIdTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace(PERSON, "");
		});
	}

	@Test
	public void invalidCombinationTypeIdPersonTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace(PERSON, CAR_ID_VALID);
		});
	}

	@Test
	public void invalidCombinationTypeIdCarTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace(CAR, PERSON_ID_VALID);
		});
	}

	@Test
	public void invalidIdCarTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace(CAR, CAR_ID_INVALID);
		});
	}

	@Test
	public void invalidIdPersonTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace(PERSON, PERSON_ID_VALID);
		});
	}

}
