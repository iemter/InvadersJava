package de.javagf;

import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFX App
 */
public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("Space Invaders");
		
		ViewerEngine viewerEngine = new ViewerEngine(primaryStage);
		
		viewerEngine.LoadMenu();
		
		primaryStage.show(); 
	}

	public static void main(String[] args) {
		launch();
	}

}