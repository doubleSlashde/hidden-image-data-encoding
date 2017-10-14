package de.doubleslash.hideAPI;

import static org.junit.Assert.assertTrue;


import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageHandlerUnitTests {

	private static MessageHandler messageHandler;

	private static String key = "123456789";
	private static String value = "987654321";


	@BeforeClass
	public static void setUpClass() throws IOException {
		messageHandler = new MessageHandler();
	}

	@Test
	public void testMessageConstruction() {
		ArrayList<String> message = new ArrayList<String>();
		message.add(key);
		message.add(value);

		messageHandler = new MessageHandler(message);

		assertTrue(((JSONArray) messageHandler.getMessageObject().get("CustomMetadata")).getJSONObject(0).keys()
				.next() instanceof String);
	}

	@Test
	public void testAddEntry() {
		messageHandler = new MessageHandler();
		assertTrue(messageHandler.addEntryToMessageObject(key, value));
	}
	
	@Test
	public void testTimestamp() {
		messageHandler = new MessageHandler();
		String timestamp = messageHandler.addTimestamp();
		assertTrue(messageHandler.getMessageObject().toString().indexOf(timestamp) > 0);
	}

	@Test
	public void testConstructAndReconstructMessage() {
		ArrayList<String> message = new ArrayList<String>();
		message.add(key);
		message.add(value);

		messageHandler = new MessageHandler(message);
		assertTrue(((JSONArray) messageHandler.reconstructMessageObject(messageHandler.getMessageBytestream())
				.get("CustomMetadata")).getJSONObject(0).keys().next() instanceof String);
	}

}