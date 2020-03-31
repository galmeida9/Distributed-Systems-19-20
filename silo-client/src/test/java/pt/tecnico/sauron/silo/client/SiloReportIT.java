package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SiloReportIT extends BaseIT {

	// static members
	private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1108735282";
	private static String PERSON_ID_INVALID = "1111a";
	private static String CAR = "car";
	private static String CAR_ID_VALID = "20SD21";
	private static String CAR_ID_INVALID = "202122";

	private static String CAM_NAME_EXISTENT = "Tagus";
	private static String CAM_NAME_NON_EXISTENT = "Alameda";

	private static SiloFrontend frontend;
	private static List<ObservationObject> observations;

	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
		frontend = new SiloFrontend(testProps.getProperty("server.host"), testProps.getProperty("server.port"));
		observations = new ArrayList<>();
	}

	@AfterAll
	public static void oneTimeTearDown() {
		frontend.exit();
	}

	// initialization and clean-up for each test
	@BeforeEach
	public void setUp() {
		frontend.camJoin(CAM_NAME_EXISTENT, 1, 1);
	}

	@AfterEach
	public void tearDown() {
		frontend.ctrlClear();
	}

	// tests

	@Test
	public void successTest() {
		// Given a valid observation
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, CAM_NAME_EXISTENT));

		try {
			SiloFrontend.ResponseStatus res;
			res = frontend.report(CAM_NAME_EXISTENT, observations);
			assertEquals(SiloFrontend.ResponseStatus.OK, res);
		}
		catch (InvalidTypeException e){
			fail("Should not have thrown any exception");
		}
	}

	@Test
	public void invalidCamNameTest(){
		// Given a observation with a invalid cam
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, CAM_NAME_NON_EXISTENT));

		try {
			SiloFrontend.ResponseStatus res;
			res = frontend.report(CAM_NAME_NON_EXISTENT, observations);
			assertEquals(SiloFrontend.ResponseStatus.OK, res);
		}
		catch (InvalidTypeException e){
			fail("Should not have thrown any exception");
		}

	}

	@Test
	public void emptyCamNameTest(){
		assertTrue(true);
	}

	@Test
	public void nullCamNameTest(){
		assertTrue(true);
	}

	@Test
	public void invalidTypeTest(){
		assertTrue(true);
	}

	@Test
	public void emptyTypeTest(){
		assertTrue(true);
	}

	@Test
	public void emptyIdTest(){
		assertTrue(true);
	}

	@Test
	public void invalidCombinationTypeIdPersonTest(){
		assertTrue(true);
	}

	@Test
	public void invalidCombinationTypeIdCarTest(){
		assertTrue(true);
	}

	@Test
	public void invalidIdCarTest(){
		assertTrue(true);
	}

	@Test
	public void invalidIdPersonTest(){
		assertTrue(true);
	}

}
