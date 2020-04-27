package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exceptions.CannotClearServerException;
import pt.tecnico.sauron.silo.client.exceptions.InvalidCameraArgumentsException;
import pt.tecnico.sauron.silo.client.exceptions.InvalidTypeException;
import pt.tecnico.sauron.silo.client.exceptions.ReportException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SiloReportIT extends BaseIT {

	// static members
	private static String HOST = testProps.getProperty("zoo.host");
	private static String PORT = testProps.getProperty("zoo.port");
	private static int INSTANCE = Integer.parseInt(testProps.getProperty("instance"));
	private static SiloFrontend frontend = new SiloFrontend(HOST, PORT, INSTANCE);

	private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1";
	private static String PERSON_ID_INVALID = "1111a";
	private static String CAR = "car";
	private static String CAR_ID_VALID = "20SD21";
	private static String CAR_ID_INVALID = "202122";

	private static String CAM_NAME_EXISTENT = "Tagus";
	private static String CAM_NAME_NON_EXISTENT = "Alameda";

	private static int NUM_OF_TESTS = 20;

	private static List<ObservationObject> observations;

	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
		observations = new ArrayList<>();
	}

	@AfterAll
	public static void oneTimeTearDown() {
		frontend.exit();
	}

	// initialization and clean-up for each test
	@BeforeEach
	public void setUp() {
		try {
			frontend.camJoin(CAM_NAME_EXISTENT, 1, 1);
		} catch (InvalidCameraArgumentsException e) {
            fail("Should not have thrown any exception.");
        }
	}

	@AfterEach
	public void tearDown() {
		try {
            frontend.ctrlClear();
        } catch (CannotClearServerException e) {
            fail("Should not have thrown any exception.");
        }
		observations.clear();
	}

	// tests

	@Test
	public void successPersonTest() {
		// Given a valid observation
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, CAM_NAME_EXISTENT));

		try {
			frontend.report( observations);
		}
		catch (InvalidTypeException | ReportException e){
			System.out.println(e.getMessage());
			fail("Should not have thrown any exception");
		}
	}

	@Test
	public void successCarTest() {
		// Given a valid observation
		observations.add(new ObservationObject(CAR, CAR_ID_VALID, CAM_NAME_EXISTENT));

		try {
			frontend.report( observations);
		}
		catch (InvalidTypeException | ReportException e){
			System.out.println(e.getMessage());
			fail("Should not have thrown any exception");
		}
	}

	@Test
	public void successMultiplePeopleTest() {
		// Given a valid observation
		for (int i = 0; i < NUM_OF_TESTS; i ++)
			observations.add(new ObservationObject(PERSON, PERSON_ID_VALID + Integer.toString(i), CAM_NAME_EXISTENT));

		try {
			frontend.report( observations);
		}
		catch (InvalidTypeException | ReportException e){
			System.out.println(e.getMessage());
			fail("Should not have thrown any exception");
		}
	}

	@Test
	public void successMultipleCarTest() {
		// Given a valid observation
		for (int i = 0; i < NUM_OF_TESTS; i ++)
			observations.add(new ObservationObject(CAR, CAR_ID_VALID, CAM_NAME_EXISTENT));

		try {
			frontend.report( observations);
		}
		catch (InvalidTypeException | ReportException e){
			System.out.println(e.getMessage());
			fail("Should not have thrown any exception");
		}
	}

	@Test
	public void successMultipleTest() {
		// Given a valid observation
		for (int i = 0; i < NUM_OF_TESTS; i ++) {
			observations.add(new ObservationObject(PERSON, PERSON_ID_VALID + Integer.toString(i), CAM_NAME_EXISTENT));
			observations.add(new ObservationObject(CAR, CAR_ID_VALID, CAM_NAME_EXISTENT));
		}

		try {
			frontend.report( observations);
		}
		catch (InvalidTypeException | ReportException e){
			System.out.println(e.getMessage());
			fail("Should not have thrown any exception");
		}
	}

	@Test
	public void invalidCamNameTest(){
		// Given a observation with a invalid cam
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, CAM_NAME_NON_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
			frontend.report(observations);
		});

	}

	@Test
	public void emptyCamNameTest(){
		// Given a observation with a empty cam name
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, ""));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
				frontend.report(observations);
		});
	}

	@Test
	public void invalidTypeTest(){
		// Given a observation with an invalid type
		observations.add(new ObservationObject("river", PERSON_ID_VALID, CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void emptyTypeTest(){
		// Given a observation with an empty type
		observations.add(new ObservationObject("", PERSON_ID_VALID, CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void emptyIdTest(){
		// Given a observation with an empty id
		observations.add(new ObservationObject(PERSON, "", CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidCombinationTypeIdPersonTest(){
		// Given a observation with an invalid combination of type (person) and id
		observations.add(new ObservationObject(PERSON, CAR_ID_VALID, CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidCombinationTypeIdCarTest(){
		// Given a observation with an invalid combination of type (car) and id
		observations.add(new ObservationObject(CAR, PERSON_ID_VALID, CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidIdCarTest(){
		// Given a observation with an invalid id for the car type
		observations.add(new ObservationObject(CAR, CAR_ID_INVALID, CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidIdPersonTest(){
		// Given a observation with an invalid id for the person type
		observations.add(new ObservationObject(PERSON, PERSON_ID_INVALID, CAM_NAME_EXISTENT));

		// Should throw exception
		Assertions.assertThrows(ReportException.class, () -> {
			frontend.report(observations);
		});
	}

}
