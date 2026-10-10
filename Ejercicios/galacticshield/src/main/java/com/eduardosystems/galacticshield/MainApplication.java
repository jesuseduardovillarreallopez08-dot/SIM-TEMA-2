package com.eduardosystems.galacticshield;

import com.eduardosystems.galacticshield.controller.GameController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);

        // Inicializar el controlador que gestionará la UI y el render
        GameController controller = new GameController(canvas);

        HBox root = new HBox(controller.getControlPanel(), canvas);
        HBox.setHgrow(canvas, Priority.ALWAYS);

        Scene scene = new Scene(root, 1150, 600);

        stage.setTitle("Galactic Shield: Monte Carlo Force");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        controller.startGameLoop();
    }

    public static void main(String[] args) {
        launch();
    }
}