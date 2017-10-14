package de.doubleslash.hide.model;

import java.util.ArrayList;
import java.util.stream.IntStream;

//import java.time.LocalDate;

public class EncodingProperties {

	private String inputFilePath;
	private String outputFilePath;
	private ArrayList<String> customMetadata = new ArrayList<String>();

	private String gpgPassword;

	private Boolean timestamp;
	private Boolean takeExif;
	private Boolean showMetadata;
	private Boolean saveMetadata;
	private Boolean imageTagging;
	private Boolean gpgSign;
	private Boolean verifyGpgSign;
	private Boolean takeCustomMetadata;

	private int degreeOfRedundancy;

	/**
	 * Default Constructor.
	 */
	public EncodingProperties() {
		inputFilePath = "";
		outputFilePath = "";

		gpgPassword = "";

		timestamp = false;
		takeExif = false;
		showMetadata = false;
		saveMetadata = false;
		imageTagging = false;
		gpgSign = false;
		verifyGpgSign = false;
		takeCustomMetadata = false;

		degreeOfRedundancy = 0;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public ArrayList<String> getCustomMetadata() {
		return customMetadata;
	}

	public void setCustomMetadata(ArrayList<String> customMetadataKeys, ArrayList<String> customMetadataPayload) {
		customMetadata.clear();
		
		if (customMetadataKeys.size() != customMetadataPayload.size())
			return;

		IntStream.range(0, customMetadataKeys.size()).forEach(i -> {
			if (customMetadataKeys.get(i) == "")
				return;

			customMetadata.add(customMetadataKeys.get(i));
			customMetadata.add(customMetadataPayload.get(i));
		});
	}

	public void addToCustomMetadata() {

	}

	public Boolean getTakeExif() {
		return takeExif;
	}

	public void setTakeExif(Boolean takeExif) {
		this.takeExif = takeExif;
	}

	public Boolean getShowMetadata() {
		return showMetadata;
	}

	public void setShowMetadata(Boolean showMetadata) {
		this.showMetadata = showMetadata;
	}

	public Boolean getSaveMetadata() {
		return saveMetadata;
	}

	public void setSaveMetadata(Boolean saveMetadata) {
		this.saveMetadata = saveMetadata;
	}
	
	public void setTimestamp(Boolean timestamp) {
		this.timestamp = timestamp;
	}
	
	public Boolean getTimestamp() {
		return timestamp;
	}

	public Boolean getImageTagging() {
		return imageTagging;
	}

	public void setImageTagging(Boolean imageTagging) {
		this.imageTagging = imageTagging;
	}
	
	public Boolean getGpgSign() {
		return gpgSign;
	}

	public void setGpgSign(Boolean gpgSign) {
		this.gpgSign = gpgSign;
	}
	
	public Boolean getVerifyGpgSign() {
		return verifyGpgSign;
	}

	public void setVerifyGpgSign(Boolean verifyGpgSign) {
		this.verifyGpgSign = verifyGpgSign;
	}

	public Boolean getTakeCustomMetadata() {
		return takeCustomMetadata;
	}

	public void setTakeCustomMetadata(Boolean takeCustomJson) {
		this.takeCustomMetadata = takeCustomJson;
	}

	public int getDegreeOfRedundancy() {
		return degreeOfRedundancy;
	}

	public void setDegreeOfRedundancy(int degreeOfRedundancy) {
		this.degreeOfRedundancy = degreeOfRedundancy;
	}

	public void setGpgPassword(String gpgPassword) {
		this.gpgPassword = gpgPassword;
	}

	public String getGpgPassword() {
		return gpgPassword;
	}

	

}
