package de.doubleslash.hide;

import de.doubleslash.hideAPI.EncodingException;
//import de.doubleslash.hide.API.ImageHandler;
import de.doubleslash.hideAPI.ImageHandler;
import de.doubleslash.hide.model.EncodingProperties;
import de.doubleslash.hide.model.TableData;
import de.doubleslash.hide.view.EncodingPropertiesController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class UserLogic extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;

	private File inputFile;
	private File outputFile;

	private ImageHandler imageHandler;

	private EncodingProperties properties;

	// private ObservableList<EncodingProperties> propertiesList =
	// FXCollections.observableArrayList();

	public UserLogic() {
		imageHandler = new ImageHandler();
		// propertiesList.add(new EncodingProperties());
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("HIDE Proto v 0.01");

		initRootLayout();

		showUserInterface();
	}

	/**
	 * Initializes the root layout.
	 */
	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(UserLogic.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows the user interface inside the root layout.
	 */
	public void showUserInterface() {
		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(UserLogic.class.getResource("view/UserInterface.fxml"));
			AnchorPane userInterface = (AnchorPane) loader.load();

			// Set person overview into the center of root layout.
			rootLayout.setCenter(userInterface);

			// Give Controller Reference of this class
			// Note: This is creating a circular *until this function
			// is done* - there is no way to call controller functions without
			// referencing it at least once
			EncodingPropertiesController controller = loader.getController();
			controller.setUserLogic(this);
			properties = controller.getEncodingProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startEncoding() {
		try {
			if (!imageHandler.loadImageFile(inputFile)) {
				showError("inputFile not found", "Could not find the original image file / could not read - try again");
				return;
			}

			// System.out.println("sign:" + properties.getGpgSign() + " custom:"
			// + properties.getTakeCustomMetadata()
			// + " tag:" + properties.getImageTagging());
			ArrayList<String> customMetadata = null;
			if (properties.getTakeCustomMetadata())
				customMetadata = properties.getCustomMetadata();

			if (properties.getTimestamp())
				imageHandler.addTimestamp();

			if (properties.getImageTagging()) {
				String tags = imageHandler.getTags();
				showTextInputDialog("Image Tags", "Please Modify the automated tags as you wish", tags,
						s -> imageHandler.setCustomTags(s));
			}

			imageHandler.startEncoding(customMetadata, properties.getGpgSign(), properties.getGpgPassword(),
					properties.getImageTagging());

		} catch (EncodingException | IOException e) {
			showError("Encoding Failure", e.getMessage());
			e.printStackTrace();
			return;
		}

		try {
			if (imageHandler.saveImageFile(outputFile)) {
				showMessage("Success!", "Encoding successful", "Image " + outputFile.getName() + " has been saved.");
			} else {
				showError("outputFile could not be written",
						"Failed to write image into output File. Make sure to select one.");
			}
		} catch (IOException e) {
			showError("Image corrupt", "Could not save image File.");
			e.printStackTrace();
		}

	}

	public TableData startDecoding() {

		try {
			if (!imageHandler.loadImageFile(inputFile)) {
				System.err.println("inputFile not found");
				showError("inputFile not found", "Could not load image File. Make sure to choose the correct file");
				return null;
			}

			if (!imageHandler.startDecoding(properties.getVerifyGpgSign())) {
				System.err.println("signature Error!");
				showError("Signature Verification Failure", "Signature could not be verified!");
				return null;
			} else {
				if (properties.getVerifyGpgSign())
					showMessage("Success!", "Signature Verified", "The Signature inside the picture is intact.");
				else
					showMessage("Success!", "Decoding successful", "");
			}
		} catch (EncodingException e) {
			showError("Signature Verification Failure", "Signature could not be verified!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new TableData(imageHandler.getMessageJSON());
	}

	public void showError(String header, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Something went wrong.");
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	public void showMessage(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setResizable(true);
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}

	public void showTextInputDialog(String header, String content, String defaultValue,
			Consumer<String> resultConsumer) {
		TextInputDialog dialog = new TextInputDialog(defaultValue);
		dialog.setTitle(header);
		dialog.setHeaderText(content);
		dialog.setContentText("");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(s -> resultConsumer.accept(s));
		// hacky bonanza: result.ifPresent(s -> resultText.replaceAll("*",s));

		return;
	}

	/**
	 * Returns the main stage.
	 * 
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

}