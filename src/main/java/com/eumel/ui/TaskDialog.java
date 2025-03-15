package com.eumel.ui;

import com.eumel.GameEngine;
import com.eumel.JumpAndRun;
import com.eumel.data.DBDAO;
import com.eumel.data.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

import static com.eumel.JumpAndRun.debugTaskAnswers;

public class TaskDialog {
    private final JumpAndRun jumpAndRun;
    private final GameEngine gameEngine;
    private final DBDAO dbDAO;
    private final Stage primaryStage;

    public TaskDialog(JumpAndRun jumpAndRun, GameEngine gameEngine, DBDAO dbDAO, Stage primaryStage) {
        this.jumpAndRun = jumpAndRun;
        this.gameEngine = gameEngine;
        this.dbDAO = dbDAO;
        this.primaryStage = primaryStage;
    }

    public void show() {
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().stop();
        }

        Task task = dbDAO.getRandomTask();
        if (task == null) {
            new GameOverDialog(jumpAndRun, gameEngine, primaryStage).show();
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Wiederbelebung");
        dialog.setHeaderText(null);

        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        UIUtils.setStageIcon(dialogStage);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setPrefSize(600, 400);

        Node titleBar = dialogPane.lookup(".header-panel");
        if (titleBar != null) {
            titleBar.setVisible(false);
            titleBar.setManaged(false);
        }

        Label questionLabel = new Label(task.getQuestion());
        questionLabel.getStyleClass().add("dialog-label");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(550);

        TextField answerField = new TextField();
        answerField.getStyleClass().add("dialog-text-field");
        answerField.setPrefWidth(200);

        VBox content = new VBox(15, questionLabel, answerField);
        content.setStyle("-fx-alignment: center;");
        dialogPane.setContent(content);

        ButtonType submitButton = new ButtonType("Einreichen", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(submitButton, cancelButton);

        dialogPane.lookupButton(submitButton).getStyleClass().add("dialog-button");
        dialogPane.lookupButton(cancelButton).getStyleClass().add("dialog-button");

        dialog.setResultConverter(button -> {
            if (button == submitButton) return answerField.getText();
            else if (button == cancelButton) return "CANCEL";
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        String answer = result.orElse(null);
        if (debugTaskAnswers) {
            System.out.println("Answer String: " + answer);
        }
        if (result.isPresent()) {
            String resultValue = result.get().trim().replace(",", ".");
            if ("CANCEL".equals(answer)) {
                if (JumpAndRun.debugMode) System.out.println("Abbrechen geklickt, kehre zum Hauptmenü zurück...");
                jumpAndRun.getAudioManager().stopAllAudio();
                new MainMenu(jumpAndRun, dbDAO, primaryStage).show();
            } else {
                try {
                    // erwartete Antwort
                    String normalizedCorrectAnswer = task.getAnswer().trim().replace(",", ".");
                    double playerAnswer = Double.parseDouble(resultValue);
                    double correctAnswer = Double.parseDouble(normalizedCorrectAnswer);

                    // Numerischer Vergleich mit kleiner Toleranz
                    double tolerance = 0.0001; // Toleranz für Rundungsfehler
                    if (Math.abs(playerAnswer - correctAnswer) < tolerance) {
                        gameEngine.revivePlayer(primaryStage);
                        if (debugTaskAnswers) {
                            System.out.println("Richtige Antwort: " + correctAnswer + " - Erwartete Antwort: " + playerAnswer);
                        }
                    } else {
                        if (debugTaskAnswers) {
                            System.out.println("Falsche Antwort: " + correctAnswer + " - Erwartete Antwort: " + playerAnswer);
                        }
                        showErrorDialog(resultValue, task.getAnswer());
                    }
                } catch (NumberFormatException e) {
                    // Falls eine der Antworten keine Zahl ist, String-Vergleich als Fallback
                    String normalizedCorrectAnswer = task.getAnswer().trim();
                    if (normalizedCorrectAnswer.equals(resultValue)) {
                        gameEngine.revivePlayer(primaryStage);
                        if (debugTaskAnswers) {
                            System.out.println("Richtige Antwort (Text): " + task.getAnswer() + " - Erwartete Antwort: " + resultValue);
                        }
                    } else {
                        if (debugTaskAnswers) {
                            System.out.println("Falsche Antwort (Text): " + task.getAnswer() + " - Erwartete Antwort: " + resultValue);
                        }
                        showErrorDialog(resultValue, task.getAnswer());
                    }
                }
            }
        } else {
            jumpAndRun.getAudioManager().stopAllAudio();
            new MainMenu(jumpAndRun, dbDAO, primaryStage).show();
        }
    }

    private void showErrorDialog(String playerAnswer, String correctAnswer) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Falsche Antwort");
        alert.setHeaderText("Leider falsch!");
        alert.setContentText("Deine Antwort war '" + playerAnswer + "', aber es sollte '" + correctAnswer + "' sein. Spiel vorbei!");
        DialogPane alertPane = alert.getDialogPane();
        alertPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        alertPane.getStyleClass().add("custom-alert");
        alertPane.setPrefSize(600, 200);
        UIUtils.setStageIcon((Stage) alertPane.getScene().getWindow());
        alert.showAndWait();
        if (JumpAndRun.debugMode) System.out.println("Alert geschlossen, kehre zum Hauptmenü zurück...");
        jumpAndRun.getAudioManager().stopAllAudio();
        new MainMenu(jumpAndRun, dbDAO, primaryStage).show();
    }

}