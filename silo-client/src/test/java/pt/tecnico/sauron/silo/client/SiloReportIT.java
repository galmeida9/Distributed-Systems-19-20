package pt.tecnico.sauron.silo.client;

import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.ExpectedException;

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
	private static String CAM_NAME_INEXISTENT = "Alameda";

	private static SiloFrontend frontend = new SiloFrontend("localhost", "8080");
	private static List<ObservationObject> observations = new ArrayList<>();
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
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
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, CAM_NAME_INEXISTENT));

		try {
			SiloFrontend.ResponseStatus res;
			res = frontend.report(CAM_NAME_INEXISTENT, observations);
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
