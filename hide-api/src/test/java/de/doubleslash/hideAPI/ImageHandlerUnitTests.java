package de.doubleslash.hideAPI;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImageHandlerUnitTests {

	private static ImageHandler imageHandler;

	private static File inputFile;
	private static File outputFile;

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
	public void testForTimestamp() throws FileNotFoundException, Exception {
		imageHandler = new ImageHandler();
		imageHandler.loadImageFile(inputFile);

		imageHandler.addTimestamp();
		imageHandler.startEncoding();

		imageHandler.saveImageFile(outputFile);

		imageHandler.loadImageFile(outputFile);

		assertTrue((imageHandler.startDecoding(false)) != null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForNoImagefile() throws FileNotFoundException, Exception {
		imageHandler = new ImageHandler();
		imageHandler.addTimestamp();

		imageHandler.startEncoding();
	}

	@Test(expected = EncodingException.class)
	public void testDecodeNoMessage() throws FileNotFoundException, Exception {
		imageHandler = new ImageHandler();
		imageHandler.loadImageFile(inputFile);
		imageHandler.startDecoding();
	}

	@Test
	public void testJSONGetterFunctions() throws FileNotFoundException, Exception {
		imageHandler = new ImageHandler();
		imageHandler.loadImageFile(inputFile);
		ArrayList<String> customMetadata = new ArrayList<String>();
		customMetadata.add(key);
		customMetadata.add(value);
		imageHandler.startEncoding(customMetadata);

		imageHandler.saveImageFile(outputFile);
		imageHandler.loadImageFile(outputFile);
		imageHandler.startDecoding();

		assertTrue(imageHandler.getMessageJSONString().equals(imageHandler.getMessageJSON().toString()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalCustomMetadata() throws FileNotFoundException, Exception {
		imageHandler = new ImageHandler();
		imageHandler.loadImageFile(inputFile);
		
		ArrayList<String> customMetadata = new ArrayList<String>();
		customMetadata.add(key);
		customMetadata.add(value);
		customMetadata.add(key);
		imageHandler.startEncoding(customMetadata);
	}

	@Test
	public void testForNoSignature() throws FileNotFoundException, Exception {
		imageHandler = new ImageHandler();
		imageHandler.loadImageFile(inputFile);

		ArrayList<String> customMetadata = new ArrayList<String>();
		customMetadata.add(key);
		customMetadata.add(value);
		imageHandler.startEncoding(customMetadata, false, "WRONG_FREAKING_PASSW0RD", false);

		imageHandler.saveImageFile(outputFile);

		imageHandler.loadImageFile(outputFile);

		assertTrue(!(imageHandler.startDecoding(true)));
	}

}