package pt.tecnico.sauron.silo.client;

import static org.junit.Assert.*;

import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exceptions.CannotClearServerException;
import pt.tecnico.sauron.silo.client.exceptions.FailedConnectionException;
import pt.tecnico.sauron.silo.client.exceptions.InvalidCameraArgumentsException;

public class SiloCamJoinIT extends BaseIT {
	
	// static members
    private static String HOST = testProps.getProperty("zoo.host");
    private static String PORT = testProps.getProperty("zoo.port");
    private static int INSTANCE = Integer.parseInt(testProps.getProperty("instance"));
    private static SiloFrontend frontEnd;
    
    private static String DEFAULT_CAMERA = "camName";
    private static double DEFAULT_LAT = 1.232;
    private static double DEFAULT_LONG = -5.343;
	
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
        try {
            // start frontend
            frontEnd = new SiloFrontend(HOST, PORT, INSTANCE);
        } catch (FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@AfterAll
	public static void oneTimeTearDown() {
		frontEnd.exit();
	}
	
	// initialization and clean-up for each test
	
	@BeforeEach
	public void setUp() {
		
	}
	
	@AfterEach
	public void tearDown() {
		try{
            frontEnd.ctrlClear();
        } catch (CannotClearServerException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
	}
		
	// tests 
	
	@Test
	public void correctArgumentsTest() {
        // Should not throw exception
        try {
            frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
        } catch (InvalidCameraArgumentsException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void duplicateCameraTest() {
        // Should not throw exception
        try {
            // Given a camera
            frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
        
            frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
        } catch (InvalidCameraArgumentsException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }
    
    @Test
    public void duplicateCameraWrongCoordinatesTest() {
        // Should not throw exception
        try {
            // Given a camera
            frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
        
            frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
        } catch (InvalidCameraArgumentsException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }

        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin(DEFAULT_CAMERA, 2.2222, -4.4343));
    }

    @Test
    public void nonValidLatitudeTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", -91, DEFAULT_LONG));
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", 91, DEFAULT_LONG));
    }

    @Test
    public void nonValidLongitudeTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", DEFAULT_LAT, -91));
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", DEFAULT_LAT, 91));
    }

    @Test
    public void nonValidCoordinatesNanTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", Double.NaN, Double.NaN));
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", Double.NaN, Double.NaN));
    }

    @Test
    public void nonValidCoordinatesInfiniteTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("test camera", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void nullCameraNameTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin(null, DEFAULT_LAT, DEFAULT_LONG));
    }

    @Test
    public void emptyCameraNameTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("", DEFAULT_LAT, DEFAULT_LONG));
    }

    @Test
    public void blankCameraNameTest() {
        // Should throw exception
        Assertions.assertThrows(InvalidCameraArgumentsException.class, () -> frontEnd.camJoin("   ", DEFAULT_LAT, DEFAULT_LONG));
    }

}
