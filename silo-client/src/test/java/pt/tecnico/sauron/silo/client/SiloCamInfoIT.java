package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.*;

import org.junit.jupiter.api.*;

public class SiloCamInfoIT extends BaseIT {
	
	// static members
    private static String HOST = "localhost";
    private static String PORT = "8080";
    private static SiloFrontend frontEnd = new SiloFrontend(HOST, PORT);
    
    private static String DEFAULT_CAMERA = "camName";
    private static double DEFAULT_LAT = 1.232;
    private static double DEFAULT_LONG = -5.343;
    private static String COORDENATES = "1.232,-5.343";
	
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
        frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG);
    }

	@AfterAll
	public static void oneTimeTearDown() {
        frontEnd.ctrlClear();
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
            assertEquals(COORDENATES, frontEnd.camInfo(DEFAULT_CAMERA), 
                    "Should return the coordenates of the camera.");
        } catch (CameraNotFoundException e) {
            fail("Should not have thrown any exception.");
        }
    }

    @Test
    public void nullCameraNameTest() {
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo(null));
    }

    @Test
    public void emptyCameraNameTest() {
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo(""));
    }

    @Test
    public void blankCameraNameTest() {
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo("   "));
    }

    @Test
    public void nonExistingCameraNameTest() {
        Assertions.assertThrows(CameraNotFoundException.class, () -> frontEnd.camInfo("fkdjsfljs"));
    }

}