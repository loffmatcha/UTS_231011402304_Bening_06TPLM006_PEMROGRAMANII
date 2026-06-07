package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.MainController;

import java.io.File;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController();
        Scene scene = new Scene(mainController.getRoot(), 1200, 720);
        
        try {
            File cssFile = new File("resources/theisbe.css");
            scene.getStylesheets().add(cssFile.toURI().toString());
        } catch (Exception e) {
            System.err.println("Warning: Could not load theisbe.css");
        }
        
        primaryStage.setTitle("TheIsbe POS");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
