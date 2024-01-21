package application;

/**
 * Assignment Final
 * Submitted by: 
 * Student 1. Itamar Abir 	ID# 
 * Student 2. Rami Abo Rabia	ID# 
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;



public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			//BorderPane root = new BorderPane();
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			
			Scene scene = new Scene(root,500,400);
			
			primaryStage.setTitle("LZZ Encoder Decoder");
			
			Image icon = new Image("icon.jpg");
			primaryStage.getIcons().add(icon);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
