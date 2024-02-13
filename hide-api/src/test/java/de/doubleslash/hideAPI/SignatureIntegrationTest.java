package de.doubleslash.hideAPI;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SignatureIntegrationTest {

	private static ImageHandler imageHandler;

	private static File inputFile;
	private static File outputFile;

	private static String fileLocation = "src/test/resources/";

	/**
	 * Ohne GPG key wird dieser Test durchfallen! Entweder hier das passwort des
	 * gpg keys angeben, oder wenn nicht vorhanden, einen neuen gpg rsa2048 key
	 * anlegen, am besten ohne passwort
	 * 
	 * Um die Signatur - Tests zu überbrücken, einfach die Boolean auf false
	 * setzen
	 */
	private static String gpgPassword = "";
	private static Boolean doTests = true;

	@BeforeClass
	public static void setUpClass() {
		imageHandler = new ImageHandler();

		inputFile = new File(fileLocation + "exampleInput.png");
		outputFile = new File(fileLocation + "exampleOutput.png");
	}

	@Test
	public void testEncoding() throws FileNotFoundException, Exception {
		if (doTests) {
			imageHandler.loadImageFile(inputFile);

			imageHandler.startEncoding(null, true, gpgPassword, false);

			assertTrue(imageHandler.saveImageFile(outputFile));

		} else
			assertTrue(true);
	}

	@Test
	public void testDecodingAndSignature() throws FileNotFoundException, Exception {
		if (doTests) {
			imageHandler.loadImageFile(outputFile);

			assertTrue(imageHandler.startDecoding(true));

		} else
			assertTrue(true);
	}

}