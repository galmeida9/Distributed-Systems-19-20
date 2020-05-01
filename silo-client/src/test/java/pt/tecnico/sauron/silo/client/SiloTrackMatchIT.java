package pt.tecnico.sauron.silo.client;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exceptions.*;

import java.util.ArrayList;
import java.util.List;

public class SiloTrackMatchIT extends BaseIT{
    // static members
    private static String PERSON = "person";
	private static String PERSON_ID_VALID = "1";
	private static String PERSON_ID_INVALID = "1111a";
	private static String PERSON_PART_ID = "1*";
	private static String CAR = "car";
	private static String CAR_ID_VALID = "20SD21";
	private static String CAR_PART_BEGINNING = "20*";
	private static String CAR_PART_MIDDLE = "2*1";
	private static String CAR_PART_END = "*21";
	private static String CAR_ID_INVALID = "202122";
	private static String CAM = "camName";
    private static double CAM_LAT = 1.232;
    private static double CAM_LONG = -5.343;

    	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp() throws ReportException, InvalidTypeException {
		List<ObservationObject> obsList = new ArrayList<>();
		obsList.add(new ObservationObject(CAR, CAR_ID_VALID,CAM));
		obsList.add(new ObservationObject(CAR, "20SZ21",CAM));

		try {
			frontEnd.ctrlClear();
			frontEnd.camJoin(CAM , CAM_LAT, CAM_LONG);
			frontEnd.report(obsList);
        } catch (InvalidCameraArgumentsException | FailedConnectionException | CannotClearServerException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@AfterAll
	public static void oneTimeTearDown() {
        try{
            frontEnd.ctrlClear();
        } catch (CannotClearServerException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
		frontEnd.exit();
	}

	// initialization and clean-up for each test

	@BeforeEach
	public void setUp() {

	}

	@AfterEach
	public void tearDown() {

	}

    @Test
    public void successCompleteIdTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, CAR_ID_VALID);
	        Assert.assertEquals(CAR, obs.get(0).getType());
	        Assert.assertEquals(CAR_ID_VALID, obs.get(0).getId());
	        Assert.assertEquals(CAM, obs.get(0).getCamName());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void successBeginningPartIdTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, CAR_PART_BEGINNING);
	        Assert.assertEquals(CAR, obs.get(0).getType());
	        Assert.assertEquals(CAR_ID_VALID, obs.get(0).getId());
	        Assert.assertEquals(CAM, obs.get(0).getCamName());

	        Assert.assertEquals(CAR, obs.get(1).getType());
	        Assert.assertEquals("20SZ21", obs.get(1).getId());
	        Assert.assertEquals(CAM, obs.get(1).getCamName());


        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void successMiddlePartIdTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, CAR_PART_MIDDLE);
	        Assert.assertEquals(CAR, obs.get(0).getType());
	        Assert.assertEquals(CAR_ID_VALID, obs.get(0).getId());
	        Assert.assertEquals(CAM, obs.get(0).getCamName());

	        Assert.assertEquals(CAR, obs.get(1).getType());
	        Assert.assertEquals("20SZ21", obs.get(1).getId());
	        Assert.assertEquals(CAM, obs.get(1).getCamName());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void successEndPartIdTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, CAR_PART_END);
	        Assert.assertEquals(CAR, obs.get(0).getType());
	        Assert.assertEquals(CAR_ID_VALID, obs.get(0).getId());
	        Assert.assertEquals(CAM, obs.get(0).getCamName());

	        Assert.assertEquals(CAR, obs.get(1).getType());
	        Assert.assertEquals("20SZ21", obs.get(1).getId());
	        Assert.assertEquals(CAM, obs.get(1).getCamName());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void successMultipleAsteriskIdTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, "2*S*1");
	        Assert.assertEquals(CAR, obs.get(0).getType());
	        Assert.assertEquals(CAR_ID_VALID, obs.get(0).getId());
	        Assert.assertEquals(CAM, obs.get(0).getCamName());

	        Assert.assertEquals(CAR, obs.get(1).getType());
	        Assert.assertEquals("20SZ21", obs.get(1).getId());
	        Assert.assertEquals(CAM, obs.get(1).getCamName());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@Test
    public void partIdWithoutAsteriskTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, "20SD");
	        Assertions.assertTrue(obs.isEmpty());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void successNoObservationTest() {
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, "40SA21");
	        Assertions.assertTrue(obs.isEmpty());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@Test
	public void emptyIdTest(){
	    try {
	        List<ObservationObject> obs = frontEnd.trackMatch(CAR, "");
	        Assertions.assertTrue(obs.isEmpty());

        } catch (InvalidTypeException | NoObservationsFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
	}

    @Test
	public void invalidTypeTest(){
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontEnd.trackMatch("object", PERSON_ID_VALID);
		});
	}

    @Test
	public void emptyTypeTest(){
		// Should throw invalid type exception
		Assertions.assertThrows(InvalidTypeException.class, () -> {
			frontEnd.trackMatch("", PERSON_ID_VALID);
		});
	}

}
