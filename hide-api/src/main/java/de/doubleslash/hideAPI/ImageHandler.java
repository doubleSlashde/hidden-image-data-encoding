package de.doubleslash.hideAPI;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.bouncycastle.openpgp.PGPException;
import org.json.JSONObject;

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

public class ImageHandler {

	private BufferedImage image;
	private BufferedImage canvas;

	private File inputFile;

	private MessageHandler messageHandler;
	private TBSEncoder encoder;
	private CryptUnit cryptunit;

	private String pubringPath = "";
	private String secringPath = "";

	private Boolean gpgSign;
	private String gpgPassword;
	private byte[] verificatorHash;
	private byte[] signatureBytes;

	private Boolean addTimestamp;

	private Boolean imageTagging;
	private String customTags;

	private JSONObject messageJson;
	private ArrayList<String> message = new ArrayList<String>();
	private byte[] messageBytes;

	static final int BYTELENGTH = 8;
	static final byte ENDBYTE = (byte) 0x90; // old: 0xaa;
	static final byte SEPERATOR = (byte) 0x9f; // old: 0xa5;
	static final int SEPERATOR_SIZE = 4;

	static final int VERIFICATOR_SIZE = 32;
	static final int SIGNATURE_SIZE = 287;
	static final int KEY_SIZE = 677;

	/**
	 * Default Constructor
	 */
	public ImageHandler() {
		encoder = new TBSEncoder();
		messageHandler = new MessageHandler();

		if ("Linux".equals(System.getProperty("os.name"))) {
			pubringPath = System.getProperty("user.home") + "/.gnupg/pubring.gpg";
			secringPath = System.getProperty("user.home") + "/.gnupg/secring.gpg";
		}
		
		/**
		 * You might add support for Windows GPG Key Management yourself;
		 * Official GPG Implementation: https://www.gpg4win.org/
		 */

		cryptunit = new CryptUnit(new File(pubringPath), new File(secringPath));
		gpgSign = false;
		gpgPassword = "";

		addTimestamp = false;

		imageTagging = false;
	}

	/**
	 * This function loads the File into a BufferedImage. Call this function
	 * first, if you want to encode
	 * 
	 * @param inputFile
	 *            File that will be read into BufferedImage
	 * @return Returns true if successful. False indicates errors while loading
	 *         the file (double check if the inputName is correct)
	 * @throws IOException
	 */
	public Boolean loadImageFile(File inputFile) throws IOException {
		// if a new image file is loaded, create a fresh encoder instance
		encoder = new TBSEncoder();

		if (inputFile == null)
			return false;

		this.inputFile = inputFile;

		image = ImageIO.read(inputFile);

		if (image == null) {
			return false;
		}

		// Draw image onto ARGB canvas (which is very detail precise) to have a
		// consistent image type to handle with.
		// All images saved by this API v0.1 are INT_ARGB color type.
		canvas = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		canvas.getGraphics().drawImage(image, 0, 0, null);

		return true;
	}

	/**
	 * Saves modified Image to output File specified in parameters. Returns true
	 * if the image file is written. False indicates an error concerning the
	 * input parameter. If the file itself could not be handled, this function
	 * throws an error.
	 * 
	 * @param outputFile
	 *            The file in which the Graphics should be written
	 * @return true if successful
	 * @throws IOException
	 */
	public Boolean saveImageFile(File outputFile) throws IOException {
		if (outputFile == null) {
			throw new IllegalArgumentException("Function parameter is null");
		}

		ImageIO.write(canvas, "PNG", outputFile);
		return true;
	}

	/**
	 * Call this function to add a timestamp to the Message.
	 */
	public void addTimestamp() {
		addTimestamp = true;
	}

	/**
	 * Call this function to get automated tags for the input picture. These can
	 * be modified and returned with the "setCustomTags" - Method.
	 * 
	 * @return a String containing all Tags
	 */
	public String getTags() {
		messageHandler = new MessageHandler();
		messageHandler.setImageFileForTagging(inputFile);
		return messageHandler.getImageTagString();
	}

	/**
	 * Set the taglist, which will be encoded "as is". If you want to have
	 * automated tags to customize, use the "getTags" - Method
	 * 
	 * @param customTags
	 *            - will be encoded under the key "Image Tags:"
	 */
	public void setCustomTags(String customTags) {
		this.customTags = customTags;
	}

	/**
	 * Overload function for "startEncoding()" that takes an Arraylist of String
	 * containing the custom message, a boolean if GPG signature should be
	 * created and a string that is the password of the GPG signature keys on
	 * this machine.
	 * 
	 * @param message
	 *            - ArrayList which contains the message. The message list ist
	 *            mapped as: String[0]: key1, String[1]: payload1, String[2]:
	 *            key2, String[3]: payload2 etc. as alternating key/value pairs.
	 *            If the message ArrayList has an uneven amount of entries, the
	 *            last dangling element will be cut.
	 * 
	 * @param gpgSign
	 *            - true when GPG Signature should be encoded
	 * @param gpgPassword
	 *            - the password of the GPG keys
	 * @return - code 1 for success, negative for failure: -1: no message to
	 *         encode | -2: fatal internal encoding failure
	 * 
	 * @throws EncodingException
	 *             Contains underlying exceptions flown in the encoding process.
	 */
	public void startEncoding(ArrayList<String> message, Boolean gpgSign, String gpgPassword, Boolean imageTagging)
			throws EncodingException {
		this.message = message;
		this.gpgPassword = gpgPassword;
		if (gpgSign != null)
			this.gpgSign = gpgSign;

		if (imageTagging != null)
			this.imageTagging = imageTagging;

		startEncoding();

		return;
	}

	/**
	 * Overload function for "startEncoding()" that takes an Arraylist of String
	 * beforehand, instead of beforehand message construction. Check private
	 * File publicKeyFile; private File secretKeyFile; "startEncoding()" Javadoc
	 * for Details.
	 * 
	 * @param message
	 *            - ArrayList which contains the message. The message list ist
	 *            mapped as: String[0]: key1, String[1]: payload1, String[2]:
	 *            key2, String[3]: payload2 etc. as alternating key/value pairs.
	 *            If the message ArrayList has an uneven amount of entries, the
	 *            last dangling element will be cut.
	 * 
	 * @throws EncodingException
	 *             Contains underlying exceptions flown in the encoding process.
	 */
	public void startEncoding(ArrayList<String> message) throws EncodingException {
		this.message = message;

		startEncoding();

		return;
	}

	/**
	 * Start the actual encoding. The Message constructed with the parameters
	 * handed over, call this function to let the image held by this class hold
	 * all specified Data in an steganographic encoded manner. After this
	 * function has been called, save the image with "saveImageFile(File file)"
	 * which will save the encoded image at "file"
	 * 
	 * 
	 * @throws EncodingException
	 *             Contains underlying exceptions flown in the encoding process.
	 */
	public void startEncoding() throws EncodingException {
		if (message != null) {
			if (message.size() % 2 != 0)
				throw new IllegalArgumentException("Illegal key/value message List!");

			messageHandler = new MessageHandler(message);
		} else {
			messageHandler = new MessageHandler();
		}

		if (addTimestamp) {
			messageHandler.addTimestamp();
		}

		if (imageTagging) {
			if (customTags == null) {
				messageHandler.setImageFileForTagging(inputFile);
			} else {
				messageHandler.setCustomImageTags(customTags);
			}
		}

		messageBytes = messageHandler.getMessageBytestream();

		if (messageBytes == null)
			throw new IllegalArgumentException("No message to encode!");

		if (canvas == null)
			throw new IllegalArgumentException("No image to encode into!");

		canvas = encoder.encode(messageBytes, canvas);

		if (gpgSign) {
			verificatorHash = cryptunit.getColorVerificationHash(encoder.getColorVerificationString());

			try {
				signatureBytes = cryptunit.getSignatureWithPassphrase(verificatorHash, gpgPassword);
			} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException
					| PGPException e) {
				throw new EncodingException(e);
			}

			encoder.encodeVerificator(verificatorHash, signatureBytes, cryptunit.getVerificationKey());

		}

		return;
	}

	/**
	 * Start the decoding algorithm. You can call this as soon as an image has
	 * been specified. This will fill the message JSON file held here, you may
	 * retrieve it as org.json.jsonobject with "getMessageJSON()" or as an JSON
	 * String "getMessageJSONString()". Throws an Exception when no message is
	 * found.
	 * 
	 * @return
	 * @throws EncodingException
	 */
	public Boolean startDecoding() throws EncodingException {
		messageBytes = encoder.decode(image);

		messageJson = messageHandler.reconstructMessageObject(messageBytes);

		if (messageJson.length() == 0) {
			throw new EncodingException("Decoded Image contains no Message!");
		}

		return true;
	}

	/**
	 * Start the decoding algorithm. You can call this as soon as an image has
	 * been specified. This will fill the message JSON file held here, you may
	 * retrieve it as org.json.jsonobject with "getMessageJSON()" or as an JSON
	 * String "getMessageJSONString()" Throws NO Exception when no message is
	 * found.
	 * 
	 * @param verifysignature
	 *            - true if signature should be verified. If set true, this
	 *            function returns whether the signature was valid (returns
	 *            true) or not.
	 * @return always true, on signature proven wrong -> false
	 * @throws EncodingException
	 *             Contains underlying exceptions flown in the encoding process.
	 */
	public Boolean startDecoding(Boolean verifySignature) throws EncodingException {

		messageBytes = encoder.decode(image);

		messageHandler = new MessageHandler(message);
		messageJson = messageHandler.reconstructMessageObject(messageBytes);

		if (verifySignature) {
			ArrayList<byte[]> verification = messageHandler.getVerificationFromBytestream(messageBytes);

			verificatorHash = cryptunit.getColorVerificationHash(encoder.getColorVerificationStringForComparison());
			if (!Arrays.equals(verification.get(0), verificatorHash)) {
				System.err.println("Verification Hashes broken! Expected: "
						+ String.format("%064x", new BigInteger(1, verification.get(0))));
				System.err.println("But got:                              "
						+ String.format("%064x", new BigInteger(1, verificatorHash)));

				return false;
			}

			try {
				return cryptunit.verify(verification.get(0), verification.get(1), verification.get(2));
			} catch (NoSuchProviderException | SignatureException | IOException | PGPException e) {
				throw new EncodingException(e);
			}

		} else {
			return true;
		}
	}

	/**
	 * Getter function for the message JSON read from the image, if there is
	 * none, no decoding happened, or it failed, it will be null.
	 * 
	 * @return org.json.jsonobject
	 */
	public JSONObject getMessageJSON() {
		return messageJson;
	}

	/**
	 * Getter function for the message JSON read from the image, converted to a
	 * common JSON String notation. If there is none, no decoding happened, or
	 * it failed, it will be null.
	 * 
	 * @return org.json.jsonobject
	 */
	public String getMessageJSONString() {
		return messageJson.toString();
	}

}
