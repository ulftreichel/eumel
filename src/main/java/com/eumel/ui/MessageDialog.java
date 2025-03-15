package com.eumel.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.Objects;

public class MessageDialog {
    public void show(String message, Stage primaryStage) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Hinweis");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styling auf das DialogPane anwenden
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");

        // Icon setzen
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        UIUtils.setStageIcon(dialogStage);

        alert.showAndWait();
    }
}