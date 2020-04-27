package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exceptions.*;

import java.util.ArrayList;
import java.util.List;

public class SiloTraceIT extends BaseIT {

    	// static members
	private static String HOST = testProps.getProperty("zoo.host");
	private static String PORT = testProps.getProperty("zoo.port");
	private static int INSTANCE = Integer.parseInt(testProps.getProperty("instance"));
	private static SiloFrontend frontend;

    private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1";
	private static String PERSON_ID_INVALID = "1111a";
	private static String CAR = "car";
	private static String CAR_ID_VALID = "20SD21";
	private static String CAR_ID_INVALID = "202122";
	private static String CAM_1 = "cam1";
    private static double CAM_1_LAT_ = 1.232;
    private static double CAM_1_LONG = -5.343;
    private static String CAM_2 = "cam2";
    private static double CAM_2_LAT_ = 2.952;
    private static double CAM_2_LONG = -1.343;


		// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp() throws ReportException, InvalidTypeException {
		List<ObservationObject> obsList = new ArrayList<>();
		obsList.add(new ObservationObject(PERSON, PERSON_ID_VALID,CAM_1));
		obsList.add(new ObservationObject(PERSON, PERSON_ID_VALID,CAM_2));
		obsList.add(new ObservationObject(CAR, CAR_ID_VALID,CAM_1));

		try {
			frontend = new SiloFrontend(HOST, PORT, INSTANCE);
            frontend.camJoin(CAM_1 ,CAM_1_LAT_, CAM_1_LONG);
			frontend.camJoin(CAM_2, CAM_2_LAT_, CAM_2_LONG);
			frontend.report(obsList);
        } catch (InvalidCameraArgumentsException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@AfterAll
	public static void oneTimeTearDown() {
        try{
            frontend.ctrlClear();
        } catch (CannotClearServerException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
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
	        List<ObservationObject> obs = frontend.trace(PERSON, PERSON_ID_VALID);
	        Assert.assertEquals(PERSON, obs.get(0).getType());
	        Assert.assertEquals(PERSON_ID_VALID, obs.get(0).getId());
	        Assert.assertEquals(CAM_2, obs.get(0).getCamName());

	        Assert.assertEquals(PERSON, obs.get(1).getType());
	        Assert.assertEquals(PERSON_ID_VALID, obs.get(1).getId());
	        Assert.assertEquals(CAM_1, obs.get(1).getCamName());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }


    @Test
    public void successNoObservationTest() {
	    try {
	        List<ObservationObject> obs = frontend.trace(CAR, "40SA21");
	        Assertions.assertTrue(obs.isEmpty());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }


    @Test
	public void invalidTypeTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontend.trace("object", PERSON_ID_VALID);
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
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.trace(PERSON, "");
		});
	}

	@Test
	public void invalidCombinationTypeIdPersonTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.trace(PERSON, CAR_ID_VALID);
		});
	}

	@Test
	public void invalidCombinationTypeIdCarTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.trace(CAR, PERSON_ID_VALID);
		});
	}

	@Test
	public void invalidIdCarTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.trace(CAR, CAR_ID_INVALID);
		});
	}

	@Test
	public void invalidIdPersonTest(){
		Assertions.assertThrows(NoObservationsFoundException.class, () -> {
			frontend.trace(PERSON, PERSON_ID_INVALID);
		});
	}

}
