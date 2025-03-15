package com.eumel.ui;

import com.eumel.JumpAndRun;
import com.eumel.MainMenuAnimation;
import com.eumel.data.DBDAO;
import com.eumel.data.Highscore;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

public class HighscoreView {
    private final JumpAndRun jumpAndRun;
    private final DBDAO dbDAO;
    private final Stage primaryStage;

    public HighscoreView(JumpAndRun jumpAndRun, DBDAO dbDAO, Stage primaryStage) {
        this.jumpAndRun = jumpAndRun;
        this.dbDAO = dbDAO;
        this.primaryStage = primaryStage;
    }

    public void show() {
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().stop();
        }

        StackPane root = new StackPane();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        root.getStyleClass().add("highscore-root");

        MainMenuAnimation animation = new MainMenuAnimation(root);
        animation.play();

        VBox highscorePane = new VBox(20);
        highscorePane.getStyleClass().add("highscore-pane");

        Label highscoreLabel = new Label("Highscores");
        highscoreLabel.getStyleClass().add("dialog-label-large");

        StackPane listPane = new StackPane();
        listPane.getStyleClass().add("highscore-list-pane");

        ListView<String> highscoreList = new ListView<>();
        highscoreList.getStyleClass().add("highscore-list");

        List<Highscore> highscores = dbDAO.getHighscores(10);
        if (JumpAndRun.debugMode) {
            System.out.println("Geladene Highscores: " + highscores.size());
            for (Highscore highscore : highscores) {
                System.out.println("Highscore: " + highscore.getName() + " - " + highscore.getScore());
            }
        }

        if (highscores.isEmpty()) {
            highscoreList.getItems().add("Kein Eintrag vorhanden");
        } else {
            for (int i = 0; i < highscores.size(); i++) {
                Highscore highscore = highscores.get(i);
                highscoreList.getItems().add(String.format("%d. %s - %d", i + 1, highscore.getName(), highscore.getScore()));
            }
        }

        listPane.getChildren().add(highscoreList);

        Button backButton = new Button("Zurück");
        backButton.getStyleClass().add("dialog-button");
        backButton.setOnAction(e -> new MainMenu(jumpAndRun, dbDAO, primaryStage).show());

        highscorePane.getChildren().addAll(highscoreLabel, listPane, backButton);
        root.getChildren().add(highscorePane);

        Scene highscoreScene = new Scene(root, 800, 600);
        primaryStage.setScene(highscoreScene);
        UIUtils.setStageIcon(primaryStage);
    }
}