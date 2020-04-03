package pt.tecnico.sauron.silo.client;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class SiloTrackIT extends BaseIT{

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
	private static String CAM_1 = "camName";
    private static double CAM_1_LAT_ = 1.232;
    private static double CAM_1_LONG = -5.343;
    private static String CAM_2 = "cam";
    private static double CAM_2_LAT_ = 2.952;
    private static double CAM_2_LONG = -1.343;



		// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp() throws ReportException, InvalidTypeException {
		List<ObservationObject> obsList = new ArrayList<>();
		obsList.add(new ObservationObject(PERSON, PERSON_ID_VALID,CAM_1));
		obsList.add(new ObservationObject(PERSON, "12", CAM_1));
		obsList.add(new ObservationObject(CAR, CAR_ID_VALID,CAM_1));
		obsList.add(new ObservationObject(PERSON, PERSON_ID_VALID,CAM_2));

		frontend.camJoin(CAM_1 ,CAM_1_LAT_, CAM_1_LONG);
		frontend.camJoin(CAM_2, CAM_2_LAT_, CAM_2_LONG);
		frontend.report(obsList);
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
    public void successTest() {
	    try {
	        ObservationObject obs = frontend.track(PERSON, PERSON_ID_VALID);
	        Assert.assertEquals(PERSON, obs.getType());
	        Assert.assertEquals(PERSON_ID_VALID, obs.getId());
	        Assert.assertEquals(CAM_2, obs.getCamName());

        } catch (InvalidTypeException | NoObservationsFoundException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@Test
	public void tooManyOfSameGroupCarIdTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(CAR, "AAAAAA");
		});
	}

	@Test
	public void invalidGroupCarIdTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(CAR, "A1AA1B");
		});
	}

	@Test
	public void noObservationForIdTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(PERSON, "137");
		});
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
			frontend.track("", PERSON_ID_VALID);
		});
	}

	@Test
	public void emptyIdTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(PERSON, "");
		});
	}

	@Test
	public void invalidCombinationTypeIdPersonTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(PERSON, CAR_ID_VALID);
		});
	}

	@Test
	public void invalidCombinationTypeIdCarTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(CAR, PERSON_ID_VALID);
		});
	}

	@Test
	public void invalidIdCarTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(CAR, CAR_ID_INVALID);
		});
	}

	@Test
	public void invalidIdPersonTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.track(PERSON, PERSON_ID_INVALID);
		});
	}

}
