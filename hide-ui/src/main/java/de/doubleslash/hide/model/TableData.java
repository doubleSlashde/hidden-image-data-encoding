package de.doubleslash.hide.model;

import java.util.ArrayList;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class TableData {

	private ArrayList<String> keyList = new ArrayList<String>();
	private ArrayList<String> payloadList = new ArrayList<String>();

	/**
	 * Default constructor
	 */
	public TableData() {
	}

	public TableData(ArrayList<String> keyList, ArrayList<String> payloadList) {
		this.keyList = keyList;
		this.payloadList = payloadList;
	}

	public TableData(JSONObject messageJSON) {
		keyList.clear();
		payloadList.clear();

		if (messageJSON == null)
			return;

		String element1 = "CustomMetadata";
		String element2 = "AutomatedInformation";
		ArrayList<String> jsonKeys = new ArrayList<String>();
		jsonKeys.add(element1);
		jsonKeys.add(element2);
		
		jsonKeys.forEach(element -> {
			if (messageJSON.has(element)) {
				JSONArray jsonArray = (JSONArray) messageJSON.get(element);
				IntStream.range(0, jsonArray.length()).forEach(i -> {
					String key = jsonArray.getJSONObject(i).keys().next();
					keyList.add(key);
					payloadList.add(jsonArray.getJSONObject(i).getString(key));
				});
			}
		});
	}

	public ArrayList<String> getKeyList() {
		return keyList;
	}

	public void setKeyList(ArrayList<String> keyList) {
		this.keyList = keyList;
	}

	public ArrayList<String> getPayloadList() {
		return payloadList;
	}

	public void setPayloadList(ArrayList<String> payloadList) {
		this.payloadList = payloadList;
	}

}
