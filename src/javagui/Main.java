package javagui;

import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A simple GUI application which allows a user to author creations
 * for a maths tutoring program.
 * 
 * @author Nathan Cairns
 *
 */
public class Main extends Application{
	
	/* Macros */
	public static final String TITLE = "Maths Aid";
	
	/**
	 * Main entry point 
	 * 
	 * @param args
	 */
	public static void main(String[] args ) {
		launch(args); // launch the application
	}
	
	/**
	 * The start method of the main stage of the application.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			// Set the fxml file to use for layouts
			VBox root = (VBox) FXMLLoader.load(getClass().getResource("Main.fxml"));
			
			// Get the scene ready and show it
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle(TITLE);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
