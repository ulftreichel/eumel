package com.eumel.ui;

import com.eumel.JumpAndRun;
import com.eumel.GameEngine;
import com.eumel.MainMenuAnimation;
import com.eumel.SettingsScreen;
import com.eumel.data.DBDAO;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PauseDialog {
    private final JumpAndRun jumpAndRun;
    private final GameEngine gameEngine;
    private final Stage primaryStage;
    private Scene previousScene;
    private StackPane pauseOverlay;
    private MainMenuAnimation pauseAnimation;
    private String buttonStyle;

    public PauseDialog(JumpAndRun jumpAndRun, GameEngine gameEngine, Stage primaryStage) {
        this.jumpAndRun = jumpAndRun;
        this.gameEngine = gameEngine;
        this.primaryStage = primaryStage;
        this.buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: 'Riffic Free Bold';";
    }

    public void show() {
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().stop();
        }

        // Speichere die aktuelle Szene
        previousScene = primaryStage.getScene();
        StackPane rootPane = (StackPane) previousScene.getRoot();

        // Erstelle das Overlay
        pauseOverlay = new StackPane();
        pauseOverlay.setStyle("-fx-background-color: rgba(51, 51, 51, 0.8);");
        pauseOverlay.setPrefSize(800, 600);

        // Animation hinzufügen
        pauseAnimation = new MainMenuAnimation(pauseOverlay);
        pauseAnimation.play();

        // Innerer Container
        StackPane pausePane = new StackPane();
        pausePane.setStyle("-fx-alignment: center; -fx-background-color: rgba(51, 51, 51, 0.8);");
        pausePane.setPrefWidth(300);
        pausePane.setPrefHeight(200);

        // Menu-Box
        VBox menuBox = new VBox(20);
        menuBox.setStyle("-fx-alignment: center;");

        // Label und Buttons
        Label pauseLabel = new Label("Pause");
        pauseLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        Button resumeButton = new Button("Weiter");
        Button mainMenuButton = new Button("Zum Hauptmenü");
        Button settingsButton = new Button("Einstellungen");
        Button exitButton = new Button("Verlassen");

        resumeButton.setStyle(buttonStyle);
        mainMenuButton.setStyle(buttonStyle);
        settingsButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        menuBox.getChildren().addAll(pauseLabel, resumeButton, mainMenuButton, settingsButton, exitButton);
        pausePane.getChildren().add(menuBox);
        pauseOverlay.getChildren().add(pausePane);

        // Füge das Overlay zur rootPane hinzu
        rootPane.getChildren().add(pauseOverlay);

        // Event-Handler
        resumeButton.setOnAction(e -> {
            pauseAnimation.stop();
            hide();
        });
        mainMenuButton.setOnAction(e -> {
            pauseAnimation.stop();
            rootPane.getChildren().remove(pauseOverlay);
            new MainMenu(jumpAndRun, jumpAndRun.getDbDAO(), primaryStage).show();
        });
        settingsButton.setOnAction(e -> new SettingsScreen(jumpAndRun, jumpAndRun.getAudioManager(), jumpAndRun.getDbDAO()).show());
        exitButton.setOnAction(e -> {
            pauseAnimation.stop();
            jumpAndRun.getDbDAO().close();
            primaryStage.close();
        });

        if (JumpAndRun.debugMode) {
            System.out.println("Pausemenü als Overlay zur rootPane hinzugefügt. Größe von rootPane: " + rootPane.getChildren().size());
        }
    }

    public void hide() {
        if (previousScene != null) {
            StackPane rootPane = (StackPane) previousScene.getRoot();
            rootPane.getChildren().remove(pauseOverlay);
            if (jumpAndRun.getFallingLeaves() != null) {
                jumpAndRun.getFallingLeaves().start();
            }
            if (gameEngine.getGameLoop() != null) {
                gameEngine.getGameLoop().start();
            }
        }
    }

    public AnimationTimer getGameLoop() {
        return gameEngine.getGameLoop();
    }

    public DBDAO getDbDAO() {
        return jumpAndRun.getDbDAO();
    }
}