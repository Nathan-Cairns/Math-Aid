package javagui;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import creation.CreationModel;
import creation.MathsAidCreationModel;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * 
 * The controller for the main stage of the GUI application.
 * This class contains the controls for all the button, list view
 * and media view components in the main GUI.
 * 
 * @author Nathan Cairns
 *
 */

public class MainController implements Initializable {
	/* MACROS */
	private final String CREATION_NAME_DIALOG_HEADER = "Name your creation";
	private final String CREATION_NAME_DIALOG_DEFAULT_TEXT = "Creation Name";
	private final String CREATION_AUDIO_DIALOG_HEADER = "Recording";
	
	/* fields */
	private CreationModel _creationModel;

	/* FXML attributes */
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
	private MediaView creation_thumbnail;

	@FXML
	private MediaView media_view;
	
	///// Start up \\\\\
	
	/**
	 * Constructor. Instantiates a MathsAidCreationModel.
	 * Other CreationModels can also be used.
	 */
	public MainController() {
		_creationModel = new MathsAidCreationModel();
	}
	
	/**
	 * Initialize the listView to be populated with mp4s found
	 * in creation directory.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> creationNames = FXCollections.observableArrayList(_creationModel.listCreations());

		creation_list.setItems(creationNames);

		creation_list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

	}

	///// List handling methods \\\\\
	
	/**
	 * When an item is selected in the list view, display a preview
	 * in the thumbnail area of the GUI.
	 */
	public void handleListItemSelected() {
		// Get the string stored in the currently selected item
		String creationName = creation_list.getSelectionModel().getSelectedItem();
		
		// Create a media player using that creation
		Task<MediaPlayer> thumbnailTask = new Task<MediaPlayer>() {
			@Override
			protected MediaPlayer call() throws Exception {
				MediaPlayer mp = new MediaPlayer(createMedia(creationName));
				mp.setAutoPlay(false);
				mp.setMute(true);

				return mp;	
			}
		}; 
		
		// On completion set the thumbnail media view to use that media player
		thumbnailTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
				e -> creation_thumbnail.setMediaPlayer(thumbnailTask.getValue()));
		
		Thread th = new Thread(thumbnailTask);
		th.setDaemon(true);
		th.start();
		
	}
	
	///// Quit button methods \\\\\
	
	/**
	 * Exit the program
	 * 
	 * @param event : quit button was pressed
	 */
	public void quitProgram(ActionEvent event) {
		System.exit(0);
	}

	///// Delete button methods \\\\\
	
	/**
	 * Handles what to do when the delete button is pressed.
	 * Displays a dialog for confirmation and then either
	 * deletes the creation currently selected or does nothing.
	 * 
	 * @param event
	 */
	public void deleteCreation(ActionEvent event) {
		// Get the currently selected creation from the list view
		String creationName = creation_list.getSelectionModel().getSelectedItem();
		
		// Confirm that the user wants to delete the creation
		boolean confirmation = false;
		if (creationName != null && !creationName.equals("")) {
			confirmation = confirmDeletion(creationName);
		}

		// If the user wanted to delete it.
		if (confirmation) {
			Task<Void> deletionTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// Remove from model and delete file
					_creationModel.deleteCreation(creationName); 
					return null;
				}
			};
			Thread th = new Thread(deletionTask);
			th.setDaemon(true);
			th.start();
			
			// Remove from the list_view
			creation_list.getItems().remove(creationName);
		}
	}
	
	/**
	 * Displays a dialog box to get confirmation of deletetion from
	 * the user.
	 * 
	 * @param creationName : the name of the creation the user wants to delete.
	 * @return boolean : whether the user wanted to delete creation or not.
	 */
	public boolean confirmDeletion(String creationName) {
		// Create alert
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete");
		alert.setContentText("Are you sure you want to delete " + creationName);
		
		// Create yes + no buttons
		ButtonType yesButton = new ButtonType("Yes", ButtonData.YES);
		ButtonType noButton = new ButtonType("no", ButtonData.NO);
		alert.getButtonTypes().setAll(yesButton, noButton);
		
		// Handle the response
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == yesButton) {
			return true;
		} else {
			return false;
		}

	}

	///// Play button methods \\\\\
	
	/**
	 * Executed when a user presses the play button.
	 * Plays the creation currently selected in the list view
	 * on the main media view.
	 * 
	 * @param event: play button was pressed
	 */
	public void playCreation(ActionEvent event) {
		// Get the currently selected creation from the list view
		String creationName = creation_list.getSelectionModel().getSelectedItem();
		
		// Create the media player from the currently selected creation
		Task<MediaPlayer> playTask = new Task<MediaPlayer>() {
			@Override
			protected MediaPlayer call() throws Exception {
				MediaPlayer mp = new MediaPlayer(createMedia(creationName));
				mp.setAutoPlay(true);
				return mp;
			}
		};
		
		// Upon completion set the media view's media player to that returned from the task
		playTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, 
				e -> media_view.setMediaPlayer(playTask.getValue()));
		
		Thread th = new Thread(playTask);
		th.setDaemon(true);
		th.start();
	}
	
	/**
	 * This method get the file name from the creation model and generates a
	 * Media object from that.
	 * 
	 * @param creationName the name of the creation we want to create media from.
	 * @return Media : the media created from the creationName.
	 */
	public Media createMedia(String creationName) {
		// Get the file name from the CreationModel
		File f = _creationModel.getCreationFile(creationName);
		String mediaUrl = f.toURI().toString();
		
		// Create the media
		Media media = new Media(mediaUrl);
		
		return media;
	}

	///// Creation Button Methods \\\\\
	
	/**
	 * Displays the appropriate dialogs when the creation button is pressed.
	 * Adds a creation to the CreationModel and generates the file.
	 * 
	 * @param event : Creation button was pressed
	 */
	public void createCreation(ActionEvent event) {
		// prompt the user for a creation name
		String creationName = creationNamePrompt(CREATION_NAME_DIALOG_HEADER, CREATION_NAME_DIALOG_DEFAULT_TEXT);
		
		// Check the validity of the name and proceed with recording
		if (creationName == null) {
			return;
		}
		if (!_creationModel.containsCreation(creationName) && !creationName.equals("")) {
			// Valid name proceed with recording
			finishCreationPrompt(CREATION_AUDIO_DIALOG_HEADER, creationName);
		} else {
			// Invalid name display error dialog
			incorrectNameDialog();
			createCreation(event);
		}
	}

	/**
	 * Warning dialog which is displayed when user enters an incorrect creationName
	 */
	public void incorrectNameDialog() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("INVALID CREATION NAME!");
		alert.setHeaderText("You choose an incorrect creation name!");
		alert.setContentText("Please check you have done the following" + "\n - Name is not empty"
				+ "\n - Creation does not already exist");

		alert.showAndWait();
	}

	/**
	 * Displays a dialog box which requests a text input from the user.
	 * 
	 * @param title : title of the prompt
	 * @param prompt : the message of the prompt
	 * @return String : the creation name the user entered
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
	
	/**
	 * Asks the user to record audio.
	 * Records audio.
	 * Adds creation to model and generates the finalised creation.
	 * 
	 * @param title
	 * @param creationName
	 */
	public void finishCreationPrompt(String title, String creationName) {
		// Create the alert
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Recording Dialog");
		alert.setHeaderText("Record Audio");
		alert.setContentText("Press okay to record audio");
		
		// Handle button clicks
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {

			// Create the creation
			Task<Void> creatingTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// add to model and generate file (records)
					_creationModel.addCreation(creationName);
					return null;
				}
			};

			Thread th = new Thread(creatingTask);
			th.setDaemon(true);
			th.start();
			
			// add creation to the list view
			creation_list.getItems().add(creationName);
			
			// display recording dialog
			recordingDialog();

		} else {
			alert.close();
		}
	}
	
	/**
	 * Shows a dialong which notifies the user the program is recording
	 */
	public void recordingDialog() {
		// show recording dialog
		Alert popup = new Alert(AlertType.INFORMATION);
		popup.setHeaderText("Recording audio...");
		popup.setTitle("Recording");
		ButtonType dismissButton = new ButtonType("Dismiss", ButtonData.CANCEL_CLOSE);
		popup.getButtonTypes().setAll(dismissButton);
		
		// hide popup after 3 seconds:
		PauseTransition delay = new PauseTransition(Duration.seconds(3));
		delay.setOnFinished(e -> popup.close());

		popup.show();
		delay.play();
	}
}
