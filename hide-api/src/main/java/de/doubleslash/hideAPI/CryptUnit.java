package de.doubleslash.hideAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Iterator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;

/**
 * 
 * HIDE - Hidden Image Data Encoding - is a tool for steganography - encoding
 * and signing information into a PNG picture with cryptographic security.
 * Disclaimer: This is a proof-of-concept or prototype and may include
 * unfinished functions or inconsistencies.
 * 
 * Copyright (C) 2017 Wolfgang Michael Hermann
 * 
 * This file is part of HIDE.
 * 
 * HIDE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * HIDE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * HIDE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * This File is published toghether with the thesis "Hidden Image Data Encoding"
 * by Wolfgang M. Hermann and is a resource for said thesis.
 * 
 * @author whermann
 *
 */

class CryptUnit {

	private PGPSecretKey secretKey;
	private PGPPublicKey publicKey;

	private File publicKeyFile;
	private File secretKeyFile;

	private Boolean createNewKey = false;
	private String passphrase = "";

	/**
	 * 
	 * 
	 * @param publicKeyFile
	 * @param createNewKey
	 */
	protected CryptUnit(File publicKeyFile, File secretKeyFile) {
		this.publicKeyFile = publicKeyFile;
		this.secretKeyFile = secretKeyFile;
		this.createNewKey = false;

		Security.addProvider(new BouncyCastleProvider());
	}

	protected CryptUnit() {
		this.createNewKey = true;

		Security.addProvider(new BouncyCastleProvider());
	}

	// PUBLIC FUNCTIONS

	public byte[] sign(byte[] verificatorHash)
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException {
		if (!createNewKey) {
			publicKey = this.readPublicKeyFromRing(new FileInputStream(publicKeyFile));
			secretKey = this.readSecretKeyFromRing(new FileInputStream(secretKeyFile));
		} else {
			this.generateNewKeys();
		}

		PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(secretKey.getPublicKey().getAlgorithm(),
				PGPUtil.SHA256, "BC");
		try {
			signatureGenerator.initSign(PGPSignature.CASUAL_CERTIFICATION,
					secretKey.extractPrivateKey(passphrase.toCharArray(), "BC"));
		} catch (PGPException e) {
			System.err.println("Could not extract private key from " + secretKeyFile.getAbsolutePath()
					+ " using passphrase " + passphrase);
			throw e;
		}

		File tempFile = File.createTempFile("pgp_", ".tmp");
		OutputStream outStream = new FileOutputStream(tempFile);
		signatureGenerator.update(verificatorHash);
		signatureGenerator.generate().encode(outStream);

		byte[] standaloneSignature = new byte[ImageHandler.SIGNATURE_SIZE];
		InputStream in = new FileInputStream(tempFile);
		in.read(standaloneSignature);
		in.close();
		tempFile.delete();

		return standaloneSignature;
	}

	public byte[] getSignatureWithPassphrase(byte[] verificatorHash, String passphrase)
			throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, IOException, PGPException {
		this.passphrase = passphrase;
		return this.sign(verificatorHash);
	}

	public Boolean verify(byte[] verificatorHash, byte[] signatureBytes, byte[] signatureKeyBytes)
			throws IOException, NoSuchProviderException, PGPException, SignatureException {

		PGPObjectFactory sigFact = new PGPObjectFactory(signatureKeyBytes);
		Object sigObj = sigFact.nextObject();
		PGPPublicKey signatureKey = null;
		if (sigObj instanceof PGPPublicKeyRing) {
			signatureKey = ((PGPPublicKeyRing) sigObj).getPublicKey();
		} else {
			System.err.println("Expected org.bouncycastle.openpgp.PGPPublicKey but got " + sigObj.getClass().getName());
			return null;
		}

		PGPObjectFactory pgpFact = new PGPObjectFactory(signatureBytes);
		Object pgpObj = pgpFact.nextObject();
		PGPSignature signature = null;
		if (pgpObj instanceof PGPSignatureList) {
			signature = ((PGPSignatureList) pgpObj).get(0);
		} else {
			System.err.println(
					"Expected org.bouncycastle.openpgp.PGPSignatureList but got " + pgpObj.getClass().getName());
			return null;
		}

		signature.initVerify(signatureKey, "BC");
		signature.update(verificatorHash);

		return signature.verify();
	}

	public byte[] getVerificationKey() {
		byte[] keyBytes = null;
		try {
			keyBytes = publicKey.getEncoded();
		} catch (IOException e) {
			// Is never thrown.
			e.printStackTrace();
		}
		return keyBytes;
	}

	/**
	 * This function will SHA-256 hash any String put in, and will return the
	 * digest.
	 * 
	 * @param verificator
	 *            - where you should put the colorVerificationString
	 * @return the digest byte array
	 */
	public byte[] getColorVerificationHash(String verificator) {
		MessageDigest hasher = null;
		try {
			hasher = MessageDigest.getInstance("SHA-256");
			hasher.update(verificator.getBytes(StandardCharsets.ISO_8859_1));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return hasher.digest();
	}

	// PRIVATE FUNCTIONS

	@SuppressWarnings("rawtypes")
	private PGPPublicKey readPublicKeyFromRing(InputStream in) throws IOException, PGPException {

		in = PGPUtil.getDecoderStream(in);
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
		PGPPublicKey key = null;
		in.close();

		Iterator ringIt = pgpPub.getKeyRings();
		while (key == null && ringIt.hasNext()) {
			PGPPublicKeyRing keyRing = (PGPPublicKeyRing) ringIt.next();
			Iterator keyIt = keyRing.getPublicKeys();

			while (key == null && keyIt.hasNext()) {
				PGPPublicKey k = (PGPPublicKey) keyIt.next();
				if (k.isEncryptionKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			throw new IllegalArgumentException("Can't find encryption key in key ring.");
		}

		return key;
	}

	private PGPSecretKey readSecretKeyFromRing(InputStream in) throws IOException, PGPException {
		in = PGPUtil.getDecoderStream(in);
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in);

		PGPSecretKey key = null;

		Iterator<?> ringIt = pgpSec.getKeyRings();

		while (key == null && ringIt.hasNext()) {
			PGPSecretKeyRing keyRing = (PGPSecretKeyRing) ringIt.next();
			Iterator<?> keyIt = keyRing.getSecretKeys();

			while (key == null && keyIt.hasNext()) {
				PGPSecretKey k = (PGPSecretKey) keyIt.next();

				if (k.isSigningKey()) {
					key = k;
				}
			}
		}

		if (key == null) {
			throw new IllegalArgumentException("Can't find signing key in key ring.");
		}
		return key;
	}

	private PGPPublicKey generateNewKeys() {
		return publicKey;
	}

}
