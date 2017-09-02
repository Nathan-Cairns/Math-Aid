package javagui;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import creation.CreationModel;
import creation.MathsAidCreationModel;
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
import javafx.scene.control.Label;
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
	private static final String CREATION_NAME_DIALOG_HEADER = "Name your creation";
	private static final String CREATION_NAME_DIALOG_DEFAULT_TEXT = "Creation Name";
	
	private static final String CREATION_AUDIO_DIALOG_TITLE = "Recording Dialog";
	private static final String CREATION_AUDIO_DIALOG_HEADER = "Record Audio";
	private static final String CREATION_AUDIO_DIALOG_MESSAGE = "Press okay to record audio, you will have 3 seconds.";
	
	private static final String RECORDING_TITLE = "Recording";
	private static final String RECORDING_HEADER = "Recording audio...";
	
	private static final String CREATION_SUCCESS_DIALOG_TITLE = "Success!";
	private static final String CREATION_SUCCESS_DIALOG_HEADER = "Creation Successfully created";
	
	private static final String CREATION_DELETE_CONFIRM_TITLE = "Deletion";
	private static final String CREATION_DELETE_CONFIRM_MESSAGE = "Are you sure you wish to delete: ";
	
	private static final String OVERWRITE_CONFIRMATION_TITLE = "Existing Creation";
	private static final String OVERWRITE_CONFIRMATION_MESSAGE = "Creation already exists do you wish to overwrite: ";
	
	private static final String CREATION_DELETEION_SUCCESS_DIALOG_TITLE = "Deletion";
	private static final String CREATION_DELETEION_SUCCESS_DIALOG_HEADER = "Creation deleted";
	
	private static final String INVALID_NAME_TITLE = "Empty Craetion Name";
	private static final String INVALID_NAME_HEADER = "Invalid Creation Name";
	private static final String INVALID_NAME_MESSAGE = "Make sure you have entered a valid creation name\n"
			+ "-The creation name is not empty\n"
			+ "-A creation with that name does not already exist\n"
			+ "-Only contains the valid characters (See Readme)";
	
	private static final String LISTENING_DIALOG_TITLE = "Review";
	private static final String LISTENING_DIALOG_HEADER = "Audio successfully recorded!";
	private static final String LISTENING_DIALOG_MESSAGE = "Would you like to review your creation or re-record your \n "
			+ "audio before finishing?";
	
	private static final char[] INVALID_CHARS = {'$', '"', '\\', '.'};
	
	/* fields */
	private CreationModel _creationModel;
	private String _nowPlaying;

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
		
		startBackgroundThread(thumbnailTask);
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
			confirmation = showWarningDialog(CREATION_DELETE_CONFIRM_TITLE, CREATION_DELETE_CONFIRM_MESSAGE, creationName);
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
			deletionTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				showSuccessAlert(CREATION_DELETEION_SUCCESS_DIALOG_TITLE, CREATION_DELETEION_SUCCESS_DIALOG_HEADER, 
						"The creation " + creationName + " was successfully deleted.");
				// Remove from the list_view
				if (creation_thumbnail.getMediaPlayer() != null) {
				creation_thumbnail.getMediaPlayer().dispose(); 
				}
				creation_list.getItems().remove(creationName);
			});
			
			startBackgroundThread(deletionTask);
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
		
		if (creationName == null) {
			return;
		}
		
		if (_nowPlaying != null && _nowPlaying.equals(creationName)) {
			media_view.getMediaPlayer().seek(new Duration(0));
		} 
		
		// Create the media player from the currently selected creation
		Task<MediaPlayer> playTask = new Task<MediaPlayer>() {
			@Override
			protected MediaPlayer call() throws Exception {
				try {
					MediaPlayer mp = new MediaPlayer(createMedia(creationName));
					mp.setAutoPlay(true);
					return mp;
				} catch (Exception e) {
					e.printStackTrace();
				} //TODO
				return null;
			}
		};
		
		// Upon completion set the media view's media player to that returned from the task
		playTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
					media_view.setMediaPlayer(playTask.getValue());
				});
		
		startBackgroundThread(playTask);
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
		String creationName = creationNamePrompt();
		
		// Check the validity of the name and proceed with recording
		if (creationName == null) {
			return;
		}
		
		// Trim leading and following spaces
		creationName = creationName.trim();
		
		boolean record = false;
		if (!_creationModel.containsCreation(creationName) && !creationName.equals("") && containsValidChars(creationName)) {
			// Valid name proceed with recording
			record = true;
		} else if (_creationModel.containsCreation(creationName)) {
			boolean overwrite = showWarningDialog(OVERWRITE_CONFIRMATION_TITLE, OVERWRITE_CONFIRMATION_MESSAGE, creationName);
				if (overwrite) {
					refreshCreation(creationName);
					record = true;
				}
		} else {
			// Invalid name display error dialog
			record = false;
		}
		
		if (record) {
			finishCreationPrompt(creationName);
		} else {
			incorrectNameDialog();
			createCreation(event);
		}
	}
	
	/**
	 * Makes sure string does not contain any invalid characters
	 * 
	 * @param creationName: The string to check
	 * @return: True if contained all valid chars, false otherwise.
	 */
	public boolean containsValidChars(String creationName) {
		for (char invalidCharacter: INVALID_CHARS) {
			if (creationName.contains("" + invalidCharacter)) {
				return false;
			}
		}
		return true;
	}
		
	/**
	 * Refresh the creation in preparation to be overwritten
	 */
	public void refreshCreation(String creationName) {
		Task<Void> refreshTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				_creationModel.deleteCreation(creationName);
				return null;
			}
		};
		
		creation_list.getItems().remove(creationName);
		
		startBackgroundThread(refreshTask);
	}
	
	/**
	 * Shows a dialog which allows the user to review their creation before completetion.
	 * 
	 * @param creationName: The name of the creation being made.
	 */
	public void showListeningDialog(String creationName) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(LISTENING_DIALOG_TITLE);
		alert.setHeaderText(LISTENING_DIALOG_HEADER);
		alert.setContentText(LISTENING_DIALOG_MESSAGE);
		
		// Create yes + no buttons
		ButtonType previewButton = new ButtonType("Preview", ButtonData.OTHER);
		ButtonType finishButton = new ButtonType("Finish", ButtonData.FINISH);
		ButtonType reRecordButton = new ButtonType("Rerecord", ButtonData.OTHER);
		ButtonType canelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(previewButton, finishButton, reRecordButton, canelButton);
		
		// Handle the response
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == previewButton) {
			previewCreation(creationName);
			alert.close();
		} else if (result.get() == finishButton){
			alert.close();
			showSuccessAlert(CREATION_SUCCESS_DIALOG_TITLE , CREATION_SUCCESS_DIALOG_HEADER, 
					"The creation " + creationName + " was successfully created.");
			// add creation to the list view
			creation_list.getItems().add(creationName);
		} else if (result.get() == reRecordButton) {
			alert.close();
			refreshCreation(creationName);
			finishCreationPrompt(creationName);
		} else {
			alert.close();
			refreshCreation(creationName);
		}
	}
	
	public void previewCreation(String creationName) {
		Task<Void> previewTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				MediaPlayer mp = new MediaPlayer(createMedia(creationName));
				mp.play();
				
				//_creationModel.playCreation(creationName);
				return null;
			}
		};
		previewTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> showListeningDialog(creationName));
		
		startBackgroundThread(previewTask);
	}
	
 
	/**
	 * Warning dialog which is displayed when user enters an incorrect creationName
	 */
	public void incorrectNameDialog() {
		Label l = new Label(INVALID_NAME_MESSAGE);
		l.setWrapText(true);
		
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(INVALID_NAME_TITLE);
		alert.setHeaderText(INVALID_NAME_HEADER);
		alert.getDialogPane().setContent(l);

		alert.showAndWait();
	}

	/**
	 * Displays a dialog box which requests a text input from the user.
	 * 
	 * @param title : title of the prompt
	 * @param prompt : the message of the prompt
	 * @return String : the creation name the user entered
	 */
	public String creationNamePrompt() {
		TextInputDialog userPrompt = new TextInputDialog(CREATION_NAME_DIALOG_DEFAULT_TEXT);
		userPrompt.setHeaderText(CREATION_NAME_DIALOG_HEADER);

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
	public void finishCreationPrompt(String creationName) {
		// Create the alert
		Label l = new Label(CREATION_AUDIO_DIALOG_MESSAGE);
		l.setWrapText(true);
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(CREATION_AUDIO_DIALOG_TITLE);
		alert.setHeaderText(CREATION_AUDIO_DIALOG_HEADER);
		alert.getDialogPane().setContent(l);
		
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
			
			Alert rec = recordingDialog(creationName);
			
			creatingTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
				rec.hide();
				showListeningDialog(creationName);
			});

			startBackgroundThread(creatingTask);
			
			rec.show();
			
		} else {
			alert.close();
		}
	}
	
	/**
	 * Shows a dialog which notifies the user the program is recording
	 */
	public Alert recordingDialog(String creationName) {
		// show recording dialog
		Alert popup = new Alert(AlertType.INFORMATION);
		popup.setTitle(RECORDING_TITLE);
		popup.setHeaderText(RECORDING_HEADER);
		ButtonType dismissButton = new ButtonType("Dimiss", ButtonData.CANCEL_CLOSE);
		popup.getButtonTypes().setAll(dismissButton);
		refreshCreation(creationName);
		
		return popup;
	}
	
	///// Misc \\\\\
	
	/**
	 * Starts a background thread
	 * 
	 * @param task: the task to be performed in the background thread
	 */
	private void startBackgroundThread(@SuppressWarnings("rawtypes") Task task) {
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}
	
	/**
	 * Shows an alert which shows an event has successfully executed
	 * 
	 * @param title: The title of the alert
	 * @param Message: The message of the alert
	 */
	public void showSuccessAlert(String title,String headerText, String Message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(Message);
		alert.show();
	}
	
	/**
	 * Displays a dialog box to get confirmation of deletetion from
	 * the user.
	 * 
	 * @param creationName : the name of the creation the user wants to delete.
	 * @return boolean : whether the user wanted to delete creation or not.
	 */
	public boolean showWarningDialog(String title, String message, String creationName) {
		// Create alert
		Label l = new Label(message + creationName);
		l.setWrapText(true);
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);		
		alert.getDialogPane().setContent(l);
		
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
}
