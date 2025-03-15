package com.eumel.ui;

import com.eumel.JumpAndRun;
import com.eumel.GameEngine; // Neue Import-Anweisung
import com.eumel.data.DBDAO;
import com.eumel.ui.UIUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class HighscoreDialog {
    private final JumpAndRun jumpAndRun;
    private final GameEngine gameEngine; // Neue Instanzvariable
    private final DBDAO dbDAO;
    private final Stage primaryStage;

    public HighscoreDialog(JumpAndRun jumpAndRun, GameEngine gameEngine, DBDAO dbDAO, Stage primaryStage) {
        this.jumpAndRun = jumpAndRun;
        this.gameEngine = gameEngine; // Initialisierung
        this.dbDAO = dbDAO;
        this.primaryStage = primaryStage;
    }

    public void show() {
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().stop();
        }

        VBox highscoreInputPane = new VBox(20);
        highscoreInputPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        highscoreInputPane.getStyleClass().add("highscore-input-root");

        Label inputLabel = new Label("Bitte gib deinen Namen ein:");
        inputLabel.getStyleClass().add("dialog-label");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("dialog-text-field");
        nameField.setPrefWidth(200);

        Button submitButton = new Button("Einreichen");
        submitButton.getStyleClass().add("dialog-button");

        highscoreInputPane.getChildren().addAll(inputLabel, nameField, submitButton);
        highscoreInputPane.setPadding(new Insets(10));
        highscoreInputPane.setStyle("-fx-alignment: center;");

        Scene highscoreInputScene = new Scene(highscoreInputPane, 400, 200);
        primaryStage.setScene(highscoreInputScene);
        UIUtils.setStageIcon(primaryStage);

        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                dbDAO.saveHighscore(name, gameEngine.getScore()); // Zugriff auf den Score über gameEngine
                new HighscoreView(jumpAndRun, dbDAO, primaryStage).show();
            } else {
                new MessageDialog().show("Name darf nicht leer sein!", primaryStage);
            }
        });
    }

    // Getter für interne Verwendung
    public int getScore() {
        return gameEngine.getScore(); // Zugriff über gameEngine
    }
}