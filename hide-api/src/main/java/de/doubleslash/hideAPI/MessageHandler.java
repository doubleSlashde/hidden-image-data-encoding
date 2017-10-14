package de.doubleslash.hideAPI;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiFileImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.exception.ClarifaiException;

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

class MessageHandler {

	private ArrayList<String> customMetadata;
	private List<String> tagList = new ArrayList<String>();

	private File imageFile = null;

	private String timestamp;
	private String customTaglist;

	private JSONObject messageObject;
	private byte[] messageBytestream = null;

	static final int SEPERATOR_MINPOS = 12;
	static final String TAG_SEPERATOR = ", ";

	/**
	 * Protected Default constructor
	 */
	protected MessageHandler() {
	}

	/**
	 * Constructor takes String ArrayList, which is to filled with: String[0]:
	 * key1, String[1]: payload1, String[2]: key2, String[3]: payload2 etc ...
	 * this notation is better fit for the desired JSONObject structure.
	 * 
	 * The Array HAS TO HAVE an even amount of entries, otherwise the Message
	 * Constructor does not know where to put the dangling element and will exit
	 * with an error.
	 * 
	 * @param customMetadata
	 *            Metadata arraylist.
	 */
	protected MessageHandler(ArrayList<String> customMetadata) {
		this.customMetadata = customMetadata;
	}

	/**
	 * Add a free entry to the message object, this function is designed to fill
	 * in the custom "user specified" metadata
	 * 
	 * @param key
	 *            - Key for the message Field
	 * @param text
	 *            - Payload of the message Field
	 * @return true if successful, false if message could not be processed
	 */
	public Boolean addEntryToMessageObject(String key, String text) {
		if (customMetadata == null) {
			customMetadata = new ArrayList<String>();
		}
		if (key == null || text == null) {
			return false;
		}
		customMetadata.add(key);
		customMetadata.add(text);
		return true;
	}

	/**
	 * Returns the current state of the JSON MessageObject.
	 * 
	 * @return MessageObject
	 */
	public JSONObject getMessageObject() {
		constructMessageObject();
		return messageObject;
	}

	/**
	 * Reconstruct the MessageObject from the bytestream read from a picture: If
	 * JSON reconstruction fails, i.e. there is no message at all, it returns an
	 * empty JSONObject. (aside from return value MessageObject can be gripped
	 * with the getter Method)
	 * 
	 * @param bytestream
	 * @return the reconstructed MessageObject, or an empty Object upon
	 *         reconstruction error.
	 */
	public JSONObject reconstructMessageObject(byte[] bytestream) {
		if (bytestream == null) {
			throw new IllegalArgumentException("Function parameter byte array is null.");
		}
		byte[] endbytes = new byte[ImageHandler.SEPERATOR_SIZE - 1];
		Arrays.fill(endbytes, ImageHandler.ENDBYTE);

		String bytes = new String(bytestream);
		String end = new String(endbytes);
		int messagelength = bytes.indexOf(end) + 1;

		messageBytestream = new byte[messagelength];
		System.arraycopy(bytestream, 0, messageBytestream, 0, messagelength);
		try {
			messageObject = new JSONObject(new String(messageBytestream, StandardCharsets.ISO_8859_1));
		} catch (JSONException e) {
			e.getMessage();
			return new JSONObject();
		}
		return messageObject;
	}

	/**
	 * Set image File. If this function has been called and an image given,
	 * automatic image tags will be generated and added to the metadata. This is
	 * completely optional.
	 * 
	 * @param imageFile
	 */
	public void setImageFileForTagging(File imageFile) {
		this.imageFile = imageFile;
	}

	public String addTimestamp() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		return timestamp = dateFormat.format(date);
	}

	/**
	 * Constructor for the actual Bytestream Object fit for encoding. Returns
	 * null if no Message to convert to Bytestream is present.
	 * 
	 * Encoding Charset is ISO 8859/1 to ensure 1 on 1 mapping.
	 * 
	 * @return
	 */
	public byte[] getMessageBytestream() throws IndexOutOfBoundsException {
		this.constructMessageObject();

		byte[] messagebytes = messageObject.toString().getBytes(StandardCharsets.ISO_8859_1);
		byte[] endbytes = new byte[ImageHandler.SEPERATOR_SIZE];
		Arrays.fill(endbytes, ImageHandler.ENDBYTE);

		messageBytestream = new byte[messagebytes.length + endbytes.length];
		System.arraycopy(messagebytes, 0, messageBytestream, 0, messagebytes.length);
		System.arraycopy(endbytes, 0, messageBytestream, messagebytes.length, endbytes.length);

		return messageBytestream;
	}

	public ArrayList<byte[]> getVerificationFromBytestream(byte[] bytestream) {
		ArrayList<byte[]> hashSigKey = new ArrayList<byte[]>();

		byte[] seperator = new byte[ImageHandler.SEPERATOR_SIZE - 1];
		Arrays.fill(seperator, ImageHandler.SEPERATOR);

		String bytes = new String(bytestream);
		String sep = new String(seperator);
		System.out.println(sep);
		int verificationStart = bytes.indexOf(sep) + ImageHandler.SEPERATOR_SIZE * 2 + 1;

		// return null if no seperator is found
		if (verificationStart < 0) {
			return null;
		}

		verificationStart = (verificationStart < SEPERATOR_MINPOS) ? SEPERATOR_MINPOS : verificationStart;

		byte[] hash = new byte[ImageHandler.VERIFICATOR_SIZE];
		byte[] sig = new byte[ImageHandler.SIGNATURE_SIZE];
		byte[] key = new byte[ImageHandler.KEY_SIZE];

		System.arraycopy(bytestream, verificationStart, hash, 0, ImageHandler.VERIFICATOR_SIZE);
		System.arraycopy(bytestream, verificationStart + ImageHandler.VERIFICATOR_SIZE, sig, 0,
				ImageHandler.SIGNATURE_SIZE);
		System.arraycopy(bytestream, verificationStart + ImageHandler.VERIFICATOR_SIZE + ImageHandler.SIGNATURE_SIZE,
				key, 0, ImageHandler.KEY_SIZE);

		hashSigKey.add(hash);
		hashSigKey.add(sig);
		hashSigKey.add(key);

		return hashSigKey;
	}

	public String getImageTagString() {
		if (customTaglist != null)
			return customTaglist;

		if (tagList.size() <= 0)
			this.fillTagList();

		String tags = "";
		int i = 0;
		while (i < tagList.size()) {
			tags += tagList.get(i);
			i++;
			if (i < tagList.size())
				tags += TAG_SEPERATOR;
		}
		return tags;
	}

	public void setCustomImageTags(String customTaglist) {
		this.customTaglist = customTaglist;
	}

	// PRIVATE FUNCTIONS

	/**
	 * Call this function to construct the MessageObject. Is implicitly called
	 * by "getMessageBytestream".
	 * 
	 * @return the constructed MessageObject
	 * @param imageFile
	 * @throws IndexOutOfBoundsException
	 */
	private JSONObject constructMessageObject() throws IndexOutOfBoundsException {
		// initialize / reset MessageObject
		messageObject = new JSONObject();

		// TODO add existing EXIF metadata

		// call AI for image tagging, unless the taglist is filled with custom
		if (imageFile != null && tagList.size() <= 0) {
			this.fillTagList();
		}

		JSONArray array;
		JSONObject item;

		// add custom Metadata
		if (customMetadata != null) {
			int i = 0;
			array = new JSONArray();
			try {
				while (i < customMetadata.size()) {
					item = new JSONObject();
					item.put(customMetadata.get(i), customMetadata.get(i + 1));
					i += 2;
					array.put(item);
				}
			} catch (IndexOutOfBoundsException t) {
				System.err.println("Custom metadata has an uneven amount of entries!!!");
				throw t;
			}
			messageObject.put("CustomMetadata", array);
		}

		// now for all kinds of automated info
		array = new JSONArray();

		// add timestamp
		if (timestamp != null) {
			item = new JSONObject();
			item.put("Timestamp", timestamp);
			array.put(item);
		}

		// add taglist
		if (tagList.size() > 0 || customTaglist != null) {
			item = new JSONObject();
			item.put("Image Tags", this.getImageTagString());
			array.put(item);
		}

		if (array.length() > 0) {
			messageObject.put("AutomatedInformation", array);
		}

		return messageObject;
	}

	private void fillTagList() {

		// Removed ClarifAI API key
		
		String clarifaiID = "";
		String clarifaiSecret = "";

		final ClarifaiClient client = new ClarifaiBuilder(clarifaiID, clarifaiSecret).buildSync();

		client.getDefaultModels().generalModel().predict()
				.withInputs(ClarifaiInput.forImage(ClarifaiFileImage.of(imageFile))).executeAsync(output -> {
					for (ClarifaiOutput<Concept> cl : output) {
						for (Concept c : cl.data()) {
							tagList.add(c.name());
						}
					}
				}, code -> System.err.println("Error code: " + code + ". Look up on clarifai homepage"), e -> {
					throw new ClarifaiException(e);
				});
	}

}
