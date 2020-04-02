package pt.tecnico.sauron.silo.client;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SiloReportIT extends BaseIT {

	// static members
	private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1";
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
		observations.clear();
	}

	// tests

	@Test
	public void successTest() {
		// Given a valid observation
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, CAM_NAME_EXISTENT));

		try {
			SiloFrontend.ResponseStatus res;
			res = frontend.report( observations);
			assertEquals(SiloFrontend.ResponseStatus.OK, res);
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

		try {
			SiloFrontend.ResponseStatus res;
			res = frontend.report(observations);
			assertEquals(SiloFrontend.ResponseStatus.OK, res);
		}
		catch (InvalidTypeException | ReportException e){
			fail("Should not have thrown any exception");
		}

	}

	@Test
	public void emptyCamNameTest(){
		// Given a observation with a empty cam name
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, ""));

		// Should throw camera not found exception
		Assertions.assertThrows(CameraNotFoundException.class, () -> {
				frontend.report(observations);
		});
	}

	@Test
	public void nullCamNameTest(){
		// Given a observation with a null cam name
		observations.add(new ObservationObject(PERSON, PERSON_ID_VALID, null));

		//FIXME: Wrong exception
		// Should throw camera not found exception
		Assertions.assertThrows(CameraNotFoundException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidTypeTest(){
		// Given a observation with an invalid type
		observations.add(new ObservationObject("river", PERSON_ID_VALID, CAM_NAME_EXISTENT));

		// Should throw invalid type exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void emptyTypeTest(){
		// Given a observation with an empty type
		observations.add(new ObservationObject("", PERSON_ID_VALID, CAM_NAME_EXISTENT));

		// Should throw invalid type exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void emptyIdTest(){
		// Given a observation with an empty id
		observations.add(new ObservationObject(PERSON, "", CAM_NAME_EXISTENT));

		//FIXME: Wrong exception
		// Should throw invalid id exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidCombinationTypeIdPersonTest(){
		// Given a observation with an invalid combination of type (person) and id
		observations.add(new ObservationObject(PERSON, CAR_ID_VALID, CAM_NAME_EXISTENT));

		//FIXME: Wrong exception
		// Should throw invalid id exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidCombinationTypeIdCarTest(){
		// Given a observation with an invalid combination of type (car) and id
		observations.add(new ObservationObject(CAR, PERSON_ID_VALID, CAM_NAME_EXISTENT));

		//FIXME: Wrong exception
		// Should throw invalid id exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidIdCarTest(){
		// Given a observation with an invalid id for the car type
		observations.add(new ObservationObject(CAR, CAR_ID_INVALID, CAM_NAME_EXISTENT));

		//FIXME: Wrong exception
		// Should throw invalid id exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

	@Test
	public void invalidIdPersonTest(){
		// Given a observation with an invalid id for the person type
		observations.add(new ObservationObject(PERSON, PERSON_ID_INVALID, CAM_NAME_EXISTENT));

		//FIXME: Wrong exception
		// Should throw invalid id exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.report(observations);
		});
	}

}
