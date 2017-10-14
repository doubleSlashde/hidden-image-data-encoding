package de.doubleslash.hideAPI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomMetadataIntegrationTest {

	private static ImageHandler imageHandler;
	
	private static File inputFile;
	private static File outputFile;
	
	private JSONObject message;
	
	private static String fileLocation = "src/test/resources/";
	
	private static String key = "123456789";
	private static String value = "987654321";
	
    @BeforeClass 
    public static void setUpClass() {      
        imageHandler = new ImageHandler();
        
        inputFile = new File(fileLocation + "exampleInput.png");
        outputFile = new File(fileLocation + "exampleOutput.png");
    }

    @Test
    public void testEncoding() throws FileNotFoundException, Exception {
    	imageHandler.loadImageFile(inputFile);
    	
    	ArrayList<String> customMetadata = new ArrayList<String>();
    	customMetadata.add(key);
    	customMetadata.add(value);
    	
    	imageHandler.startEncoding(customMetadata);
    	
    	assertTrue(imageHandler.saveImageFile(outputFile));
    }
    
    @Test
    public void testDecodingAndPayload() throws IOException, EncodingException {
    	imageHandler.loadImageFile(outputFile);
        imageHandler.startDecoding();
        
        message = imageHandler.getMessageJSON();
        
        String key = ((JSONArray)message.get("CustomMetadata")).getJSONObject(0).keys().next();
        String value = ((JSONArray)message.get("CustomMetadata")).getJSONObject(0).getString(key);
		assertEquals(CustomMetadataIntegrationTest.key, key);
		assertEquals(CustomMetadataIntegrationTest.value, value);
    }

    
}    