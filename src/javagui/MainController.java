package javagui;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import creation.CreationModel;
import creation.MathsAidCreationModel;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

// TODO check creation doesnt already exist
// TODO check directory doesnt already exist before making it
// TODO replace popup with recording icon, dialog perhaps?

public class MainController implements Initializable{
	/* MACROS */
	private final String CREATION_NAME_DIALOG_HEADER = "Name your creation";
	private final String CREATION_NAME_DIALOG_DEFAULT_TEXT = "Creation Name";
	private final String CREATION_AUDIO_DIALOG_HEADER = "Recording";
	
	private CreationModel _creationModel = new MathsAidCreationModel();

	@FXML
	private Button play_button;

	@FXML
	private Button quit_button;

	@FXML
	private Button create_button;

	@FXML
	private Button delete_button;
	
	@FXML
	private ListView<String> creation_list;

	@FXML
	private ImageView creation_thumbnail;
	
	@FXML 
	private MediaView media_view;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO BACKGROUND THREAD????????s
		ObservableList<String> creationNames = FXCollections.observableArrayList(_creationModel.listCreations());
		
		creation_list.setItems(creationNames);
		
		creation_list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
	}
	
	///// List handling methods \\\\\

	///// Quit button methods \\\\\
	
	public void quitProgram(ActionEvent event) {
		System.exit(0);
	}

	///// Delete button methods \\\\\
	
	public void deleteCreation(ActionEvent event) {
		
		String creationName = creation_list.getSelectionModel().getSelectedItem();
		
		boolean confirmation = false;
		if (creationName != null && !creationName.equals("")) {
			confirmation = confirmDeletion(creationName);
		}
		
		if (confirmation) {
			Task<Void> deletionTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					_creationModel.deleteCreation(creationName);
					return null;
				}
			};
			Thread th = new Thread(deletionTask);
			th.setDaemon(true);
			th.start();

			creation_list.getItems().remove(creationName);
		} 

	}
	
	public boolean confirmDeletion(String creationName) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete");
		alert.setContentText("Are you sure you want to delete " + creationName);
		
		ButtonType yesButton = new ButtonType("Yes", ButtonData.YES);
		ButtonType noButton = new ButtonType("no", ButtonData.NO);
		
		
		alert.getButtonTypes().setAll(yesButton, noButton);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == yesButton) {
			return true;
		} else {
			return false;
		}

	}
	
	///// Play button methods \\\\\ 
	
	public void playCreation(ActionEvent event) {
		String creationName = creation_list.getSelectionModel().getSelectedItem();
	
		Task<Void> playTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				_creationModel.playCreation(creationName);
				
				return null;
			}
		};
		
		Thread th = new Thread(playTask);
		th.setDaemon(true);
		th.start();
	}
	
	///// Creation Button Methods \\\\\
	
	public void createCreation(ActionEvent event) {
		String creationName = creationNamePrompt(CREATION_NAME_DIALOG_HEADER, CREATION_NAME_DIALOG_DEFAULT_TEXT);
		
		if (creationName == null) {
			return;
		}
		if (!_creationModel.containsCreation(creationName) && !creationName.equals("")) {
			finishCreationPrompt(CREATION_AUDIO_DIALOG_HEADER, creationName);
		} 
		else {
			incorrectNameDialog();
			createCreation(event);
		}
	}	
	
	public void incorrectNameDialog() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("INVALID CREATION NAME!");
		alert.setHeaderText("You choose an incorrect creation name!");
		alert.setContentText("Please check you have done the following"
				+ "\n - Name is not empty"
				+ "\n - Creation does not already exist");

		alert.showAndWait();
	}
	
	/**
	 * Displays a dialog box which requests a text input from the user.
	 * 
	 * @param title
	 * @param prompt
	 * @return
	 */
	public String creationNamePrompt(String title, String prompt) {
		TextInputDialog userPrompt = new TextInputDialog(prompt);
		userPrompt.setHeaderText(title);
		
		Optional<String> result = userPrompt.showAndWait();
		
		String userInput = null;
		if (result.isPresent()) {
			TextField textField = userPrompt.getEditor();
			userInput = textField.getText();
		} else {
			userPrompt.close();
		}
		
		return userInput;
	}

	public void finishCreationPrompt(String title, String creationName) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Recording Dialog");
		alert.setHeaderText("Record Audio");
		alert.setContentText("Press okay to record audio");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			
			// Create the creation
			Task<Void> creatingTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					_creationModel.addCreation(creationName);
					return null;
				}
			};
			creation_list.getItems().add(creationName);
			
			Thread th = new Thread(creatingTask);
			th.setDaemon(true);
			th.start();
			
			// can use an Alert, Dialog, or PopupWindow as needed...
			Alert popup = new Alert(AlertType.INFORMATION);
			popup.setHeaderText("Recording audio...");
			popup.setTitle("Recording");
						
			// hide popup after 3 seconds:
			PauseTransition delay = new PauseTransition(Duration.seconds(3));
			delay.setOnFinished(e -> popup.close());

			popup.show();
			delay.play();

		} else {
			alert.close();
		}
	}
}
