package de.doubleslash.hideAPI;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;

public class TBSEncoderUnitTests {

	private static TBSEncoder encoder;

	private static BufferedImage testImage;
	private static BufferedImage canvas;

	private static String fileLocation = "src/test/resources/";

	@BeforeClass
	public static void setUpClass() throws IOException {
		encoder = new TBSEncoder();

		File inputFile = new File(fileLocation + "exampleInput.png");

		testImage = ImageIO.read(inputFile);
		rebootCanvas();
	}

	// --NOTEST--
	public static void rebootCanvas() {
		canvas = new BufferedImage(testImage.getWidth(), testImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		canvas.getGraphics().drawImage(testImage, 0, 0, null);
	}

	@Test
	public void testEncode() {
		rebootCanvas();

		byte[] randombytes = new byte[new Random().nextInt(500) + 10];
		new Random().nextBytes(randombytes);
		assertTrue(encoder.encode(randombytes, canvas) instanceof BufferedImage);
	}

	@Test
	public void testSignatureEncoding() {
		rebootCanvas();

		byte[] hash = new byte[ImageHandler.VERIFICATOR_SIZE];
		byte[] sig = new byte[ImageHandler.SIGNATURE_SIZE];
		byte[] key = new byte[ImageHandler.KEY_SIZE];
		byte[] randombytes = new byte[new Random().nextInt(500)];

		new Random().nextBytes(hash);
		new Random().nextBytes(sig);
		new Random().nextBytes(key);
		new Random().nextBytes(randombytes);

		encoder.setImage(canvas);
		assertTrue(encoder.encodeVerificator(hash, sig, key));
	}

	@Test
	public void testDecode() {
		rebootCanvas();

		assertTrue(encoder.decode(canvas) instanceof byte[]);
	}

	@Test
	public void testVerificator() {
		rebootCanvas();
		encoder = new TBSEncoder();
		
		int minimumVerificationPixels = (canvas.getHeight() * canvas.getWidth() * 3) - ImageHandler.VERIFICATOR_SIZE
				- ImageHandler.SIGNATURE_SIZE - ImageHandler.KEY_SIZE;
		
		byte[] randombytes = new byte[new Random().nextInt(500) + 10];
		new Random().nextBytes(randombytes);
		encoder.setImage(canvas);
		encoder.setMessage(randombytes);
		encoder.encode();

		assertTrue(encoder.getColorVerificationString().length() > minimumVerificationPixels);
	}

}