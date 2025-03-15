package com.eumel.ui;

import com.eumel.JumpAndRun;
import com.eumel.GameEngine;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class GameOverDialog {
    private final JumpAndRun jumpAndRun;
    private final GameEngine gameEngine;
    private final Stage primaryStage;

    public GameOverDialog(JumpAndRun jumpAndRun, GameEngine gameEngine, Stage primaryStage) {
        this.jumpAndRun = jumpAndRun;
        this.gameEngine = gameEngine;
        this.primaryStage = primaryStage;
    }

    public void show() {
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().stop();
        }

        VBox gameOverPane = new VBox(20);
        gameOverPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        gameOverPane.getStyleClass().add("game-over-root");

        Label gameOverLabel = new Label("Game Over!\nDein eumel ist leider gestorben\nPunkte: " + gameEngine.getScore());
        gameOverLabel.getStyleClass().add("dialog-label-large");

        Button startButton = new Button("Starten");
        Button highscoreButton = new Button("Highscores");
        Button exitButton = new Button("Verlassen");

        startButton.getStyleClass().add("dialog-button");
        highscoreButton.getStyleClass().add("dialog-button");
        exitButton.getStyleClass().add("dialog-button");

        gameOverPane.getChildren().addAll(gameOverLabel, startButton, highscoreButton, exitButton);

        Scene gameOverScene = new Scene(gameOverPane, 800, 600);
        primaryStage.setScene(gameOverScene);

        startButton.setOnAction(e -> {
            gameEngine.getGameLoop().stop();
            JumpAndRun.debugMode = false;
            jumpAndRun.startGame(primaryStage, jumpAndRun.getStartLevel(), jumpAndRun.getStartDesign());
        });
        highscoreButton.setOnAction(e -> new HighscoreView(jumpAndRun, jumpAndRun.getDbDAO(), primaryStage).show());
        exitButton.setOnAction(e -> {
            jumpAndRun.getDbDAO().close();
            primaryStage.close();
        });

        UIUtils.setStageIcon(primaryStage);
    }
}