package org.tang.py.laucher;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FaceRecognitionLauncher extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException{
		
		Parent root = null;
		try {
			root = FXMLLoader.load(	getClass().getResource("/fxml/gui/Menu.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Pane myPane = FXMLLoader.load(getClass().getResource("/fxml/gui/Menu.fxml"));
		Scene scene = new Scene(myPane);

        primaryStage.setTitle("Face Recognition");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
