package pt.tecnico.sauron.silo.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import pt.tecnico.sauron.silo.client.SiloFrontend.ResponseStatus;

public class SiloCamJoinIT extends BaseIT {
	
	// static members
    private static String HOST = "localhost";
    private static String PORT = "8080";
    private static SiloFrontend frontEnd = new SiloFrontend(HOST, PORT);
    
    private static String DEFAULT_CAMERA = "camName";
    private static double DEFAULT_LAT = 1.232;
    private static double DEFAULT_LONG = -5.343;
	
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){

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
		frontEnd.ctrlClear();
	}
		
	// tests 
	
	@Test
	public void correctArgumentstest() {
        assertEquals(ResponseStatus.OK, frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG), 
                    "adding new camera should return OK");
    }

    @Test
    public void duplicateCameraTest() {
        assertEquals(ResponseStatus.OK, frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG), 
                    "adding new camera should return OK");
        assertEquals(ResponseStatus.OK, frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG), 
                    "adding duplicate camera should return OK");
    }
    
    @Test
    public void duplicateCameraWrongCoordenatesTest() {
        assertEquals(ResponseStatus.OK, frontEnd.camJoin(DEFAULT_CAMERA, DEFAULT_LAT, DEFAULT_LONG), 
                    "adding new camera should return OK");
        assertEquals(ResponseStatus.NOK, frontEnd.camJoin(DEFAULT_CAMERA, 2.2222, -4.4343), 
                    "Adding duplicate camera with different location should return NOK");
    }

    @Test
    public void nonValidCoordenatesTest() {
        assertEquals(ResponseStatus.NOK, frontEnd.camJoin("test camera", Double.NaN, Double.NaN), 
                    "Creating camera with a non existing latitude or longitude should return NOK");
    }

    @Test
    public void nullCameraNameTest() {
        assertEquals(ResponseStatus.NOK, frontEnd.camJoin(null, DEFAULT_LAT, DEFAULT_LONG), 
                    "Creating camera with a null name should return NOK");
    }

    @Test
    public void emptyCameraNameTest() {
        assertEquals(ResponseStatus.NOK, frontEnd.camJoin("", DEFAULT_LAT, DEFAULT_LONG), 
                    "Creating camera with an empty name should return NOK");
    }

    @Test
    public void blankCameraNameTest() {
        assertEquals(ResponseStatus.NOK, frontEnd.camJoin("   ", DEFAULT_LAT, DEFAULT_LONG), 
                    "Creating camera with a blank name should return NOK");
    }

}
