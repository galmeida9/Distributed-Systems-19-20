package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.*;

import org.junit.jupiter.api.*;
import pt.tecnico.sauron.silo.client.exceptions.CameraNotFoundException;
import pt.tecnico.sauron.silo.client.exceptions.CannotClearServerException;
import pt.tecnico.sauron.silo.client.exceptions.FailedConnectionException;
import pt.tecnico.sauron.silo.client.exceptions.InvalidCameraArgumentsException;

public class SiloCamInfoIT extends BaseIT {
	
	// static members
    private static String DEFAULT_CAMERA = "camName";
    private static String NON_EXISTING_NAME = "fdsfsdfd";
    private static double DEFAULT_LAT = 1.232;
    private static double DEFAULT_LONG = -5.343;
    private static String COORDINATES = "1.232,-5.343";
	
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
        try {
            // given a Camera for all the tests
            frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
        } catch (InvalidCameraArgumentsException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

	@AfterAll
	public static void oneTimeTearDown() {
        try {
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
		
	// tests 
	
	@Test
	public void correctArgumentstest() {
        try{
            // Should return the coordinates for the camera
            assertEquals(COORDINATES, frontEnd.camInfo(DEFAULT_CAMERA), 
                    "Should return the coordinates of the camera.");
        } catch (CameraNotFoundException | FailedConnectionException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void nullCameraNameTest() {
        // Should throw an exception
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo(null));
    }

    @Test
    public void emptyCameraNameTest() {
        // Should throw an exception
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo(""));
    }

    @Test
    public void blankCameraNameTest() {
        // Should throw an exception
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo("   "));
    }

    @Test
    public void nonExistingCameraNameTest() {
        // Should throw an exception
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo(NON_EXISTING_NAME));
    }

}
