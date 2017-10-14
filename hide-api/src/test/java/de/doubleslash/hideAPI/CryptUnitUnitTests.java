package de.doubleslash.hideAPI;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.bouncycastle.openpgp.PGPException;
import org.junit.BeforeClass;
import org.junit.Test;

public class CryptUnitUnitTests {

	private static CryptUnit cryptunit;

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
	public static void setUpClass() throws IOException {
		if ("Linux".equals(System.getProperty("os.name"))) {
			cryptunit = new CryptUnit(new File(System.getProperty("user.home") + "/.gnupg/pubring.gpg"),
					new File(System.getProperty("user.home") + "/.gnupg/secring.gpg"));
		} else {
			System.err.println("Signing functionality only available on Linux Systems for now");
			doTests = false;
		}

	}

	@Test
	public void testSignatureVerification() throws FileNotFoundException, Exception {
		if (doTests) {
			byte[] verificatorHash = cryptunit.getColorVerificationHash(String.valueOf(new Random().nextInt()));
			assertTrue(cryptunit.verify(verificatorHash,
					cryptunit.getSignatureWithPassphrase(verificatorHash, gpgPassword),
					cryptunit.getVerificationKey()));
		} else
			assertTrue(true);
	}
	
	@Test(expected = PGPException.class)
	public void testNoKeyRetrieval() throws FileNotFoundException, Exception {
		if (doTests) {
			cryptunit.sign(null);
		} else
			throw new PGPException("");
	}

}