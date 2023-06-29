package com.puzzle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PuzzleApplication extends Application {
    public static Stage indexWindow;

    @Override
    public void start(Stage stage) throws IOException {
        indexWindow = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(PuzzleApplication.class.getResource("index.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Puzzle");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}