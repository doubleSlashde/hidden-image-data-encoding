package de.doubleslash.hide.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.doubleslash.hide.*;
import de.doubleslash.hide.model.EncodingProperties;
import de.doubleslash.hide.model.TableData;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;

public class EncodingPropertiesController {

	private UserLogic userLogic;
	private EncodingProperties properties;
	private FileChooser fileChooser = new FileChooser();

	private ArrayList<String> keyList = new ArrayList<String>();
	private ArrayList<String> payloadList = new ArrayList<String>();

	private ArrayList<String> keyListEditable = new ArrayList<String>();
	private ArrayList<String> payloadListEditable = new ArrayList<String>();

	private static final int MAX_LABELLENGTH = 22;

	@FXML
	private CheckBox takeCustomMetadataCheckbox;
	@FXML
	private CheckBox signatureCheckbox;
	@FXML
	private CheckBox verfifySignatureCheckbox;
	@FXML
	private CheckBox imageTaggingCheckbox;
	@FXML
	private CheckBox timestampCheckbox;

	@FXML
	private Button configureSignatureButton;

	@FXML
	private Label inputLabel;
	@FXML
	private Label inputLabel2;
	@FXML
	private Label outputLabel;

	@FXML
	private TableView<Integer> metadataTableView;

	@FXML
	private TableColumn<Integer, String> metadataTableViewCol1;
	@FXML
	private TableColumn<Integer, String> metadataTableViewCol2;

	@FXML
	private TableView<Integer> metadataListingView;
	@FXML
	private TableColumn<Integer, String> metadataListingViewCol1;
	@FXML
	private TableColumn<Integer, String> metadataListingViewCol2;

	/**
	 * Standard Constructor.
	 */
	public EncodingPropertiesController() {
		properties = new EncodingProperties();
	}

	/**
	 * Initializes the controller class, automatically called after initializing
	 * all elements is done
	 */
	@FXML
	private void initialize() {

		inputLabel.setText("");
		outputLabel.setText("");
		inputLabel2.setText("");

		metadataTableView.getItems().add(0);

		metadataTableView.setEditable(true);
		metadataTableView.refresh();

		metadataTableViewCol1.setCellFactory(TextFieldTableCell.forTableColumn());
		metadataTableViewCol1.setEditable(true);

		metadataTableViewCol2.setCellFactory(TextFieldTableCell.forTableColumn());
		metadataTableViewCol2.setEditable(true);

		keyListEditable.add("");
		payloadListEditable.add("");

		/**
		 * I know this is NOT how you do it. I ran out of time, so i did this.
		 */
		metadataTableViewCol1.setOnEditCommit(event -> {
			if (event.getNewValue().equals("")) {
				userLogic.showError("No.", "You cant add an empty key as string");
				return;
			}

			int pos = event.getTablePosition().getRow();
			if (keyListEditable.size() <= pos + 1) {
				keyListEditable.add("");
				payloadListEditable.add("");
			}
			keyListEditable.set(pos, event.getNewValue());

			refreshEditTable();

			if (metadataTableView.getItems().size() <= Math.min(keyListEditable.size(), payloadListEditable.size()))
				metadataTableView.getItems().add(metadataTableView.getItems().size());
		});

		metadataTableViewCol2.setOnEditCommit(event -> {
			int pos = event.getTablePosition().getRow();
			if (payloadListEditable.size() <= pos + 1) {
				payloadListEditable.add("");
				keyListEditable.add("");
			}
			payloadListEditable.set(pos, event.getNewValue());

			refreshEditTable();
		});
	}

	@FXML
	private void customMetadataCheckboxAction(ActionEvent event) {
		properties.setTakeCustomMetadata(takeCustomMetadataCheckbox.selectedProperty().getValue());
	}

	@FXML
	private void imageTaggingCheckboxAction(ActionEvent event) {
		properties.setImageTagging(imageTaggingCheckbox.selectedProperty().getValue());
	}

	@FXML
	private void signatureCheckboxAction(ActionEvent event) {
		properties.setGpgSign(signatureCheckbox.selectedProperty().getValue());
	}

	@FXML
	private void timestampCheckboxAction(ActionEvent event) {
		properties.setTimestamp(timestampCheckbox.selectedProperty().getValue());
	}

	@FXML
	private void verifySignatureCheckboxAction(ActionEvent event) {
		properties.setVerifyGpgSign(verfifySignatureCheckbox.selectedProperty().getValue());
	}

	@FXML
	private void inputFileChooser() {
		fileChooser.setTitle("Öffnen:");
		File input = fileChooser.showOpenDialog(userLogic.getPrimaryStage());
		if (input == null)
			userLogic.showError("File error", "You must choose a file!");
		else {
			try {
				inputLabel.setText(this.getLabelText(input.getCanonicalPath().toString()));
				inputLabel2.setText(inputLabel.getText());

				userLogic.setInputFile(input);
			} catch (IOException e) {
				userLogic.showError("File error", e.getMessage().toString());
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void outputFileChooser() {
		fileChooser.setTitle("Speicherort wählen:");
		File output = fileChooser.showOpenDialog(userLogic.getPrimaryStage());
		if (output == null) {
			userLogic.showError("File error", "You must choose a file!");
		} else {
			try {
				outputLabel.setText(this.getLabelText(output.getCanonicalPath().toString()));
			} catch (IOException e) {
				userLogic.showError("File error", e.getMessage().toString());
				e.printStackTrace();
			}
			userLogic.setOutputFile(output);
		}
	}

	@FXML
	private void startEncoding() {
		properties.setCustomMetadata(keyListEditable, payloadListEditable);
		userLogic.startEncoding();
	}

	@FXML
	private void startDecoding() {
		TableData table = userLogic.startDecoding();
		if (table == null)
			return;

		this.keyList = table.getKeyList();
		this.payloadList = table.getPayloadList();
		refreshListingTable();
	}

	@FXML
	private void updateTable() {
	}

	@FXML
	private void configureSignatureButtonPressed() {
		userLogic.showTextInputDialog("Password", "Please enter the password for your GPG Keys:", "password",
				s -> properties.setGpgPassword(s));
	}

	// PRIVATE NON FXML FUNCTIONS

	private void refreshEditTable() {

		metadataTableViewCol1.setCellValueFactory(cell -> {
			int row = cell.getValue();
			return new ReadOnlyStringWrapper(keyListEditable.get(row));
		});

		metadataTableViewCol2.setCellValueFactory(cell -> {
			int row = cell.getValue();
			return new ReadOnlyStringWrapper(payloadListEditable.get(row));
		});

		metadataTableView.refresh();
	}

	private void refreshListingTable() {

		if (metadataListingView.getItems().size() < keyList.size()) {
			for (int i = 0; i < keyList.size() && i < payloadList.size(); i++) {
				metadataListingView.getItems().add(i);
			}
		}

		metadataListingViewCol1.setCellValueFactory(cell -> {
			int row = cell.getValue();
			return new ReadOnlyStringWrapper(keyList.get(row));

		});

		metadataListingViewCol2.setCellValueFactory(cell -> {
			int row = cell.getValue();
			return new ReadOnlyStringWrapper(payloadList.get(row));

		});

		metadataListingView.refresh();
	}

	private String getLabelText(String string) {
		if (string.length() > MAX_LABELLENGTH)
			return "…" + (String) string.substring(Math.max(string.length() - MAX_LABELLENGTH, 0), string.length());
		else
			return string;
	}

	// PUBLIC FUNCTIONS

	public void setUserLogic(UserLogic userLogic) {
		this.userLogic = userLogic;
	}

	public EncodingProperties getEncodingProperties() {
		return properties;
	}

}
