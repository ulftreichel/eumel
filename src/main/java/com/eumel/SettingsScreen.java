package com.eumel;

import com.eumel.data.DBDAO;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingsScreen {
    private final JumpAndRun game;
    private final AudioManager audioManager;
    private final DBDAO dbDao;
    private MainMenuAnimation settingsAnimation;
    private String style;
    private String buttonStyle;
    private final Map<String, Button> primaryButtons = new HashMap<>();
    private final Map<String, Button> secondaryButtons = new HashMap<>();
    private String waitingForKey = null;
    private boolean isPrimary = true;

    public SettingsScreen(JumpAndRun game, AudioManager audioManager, DBDAO dbDao) {
        this.game = game;
        this.audioManager = audioManager;
        this.dbDao = dbDao;
    }

    public void show() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/player/bonbon.png"))));
        settingsStage.setTitle("Einstellungen");

        StackPane settingsRoot = new StackPane();
        settingsRoot.setStyle("-fx-background-color: #333333;");

        settingsAnimation = new MainMenuAnimation(settingsRoot);
        settingsAnimation.play();

        GridPane settingsPane = new GridPane();
        settingsPane.setHgap(10);
        settingsPane.setVgap(20);
        settingsPane.setPadding(new Insets(20));
        settingsPane.setAlignment(javafx.geometry.Pos.CENTER);
        settingsPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(150);
        labelColumn.setHalignment(HPos.RIGHT);
        ColumnConstraints primaryColumn = new ColumnConstraints();
        primaryColumn.setPrefWidth(150);
        primaryColumn.setHalignment(HPos.LEFT);
        ColumnConstraints secondaryColumn = new ColumnConstraints();
        secondaryColumn.setPrefWidth(150);
        secondaryColumn.setHalignment(HPos.LEFT);
        settingsPane.getColumnConstraints().addAll(labelColumn, primaryColumn, secondaryColumn);

        style = "-fx-font-size: 20px; -fx-text-fill: white; -fx-font-family: '" + game.customFont.getName() + "';";
        buttonStyle = "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + game.customFont.getName() + "';";

        Label volumeLabel = new Label("Lautstärke:");
        volumeLabel.setStyle(style);

        Slider volumeSlider = new Slider(0, 1, dbDao.getVolume());
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setPrefWidth(300);

        volumeSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.valueOf((int) (value * 100));
            }
            @Override
            public Double fromString(String string) {
                return Double.valueOf(string) / 100;
            }
        });
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audioManager.setVolume(newVal.doubleValue());
            dbDao.saveSetting("volume", String.valueOf(newVal.doubleValue()));
        });

        Label primaryHeader = new Label("Primär");
        primaryHeader.setStyle(style);
        Label secondaryHeader = new Label("Sekundär");
        secondaryHeader.setStyle(style);

        String[] actions = {"move_left", "move_right", "jump", "shoot"};
        int row = 2;
        for (String action : actions) {
            Label actionLabel = new Label(getActionName(action) + ":");
            actionLabel.setStyle(style);

            Button primaryButton = new Button(dbDao.getKeyBinding(action, true));
            primaryButton.setStyle(buttonStyle);
            primaryButton.setPrefWidth(140);
            primaryButton.setOnAction(e -> startKeyBinding(action, true, primaryButton));
            primaryButtons.put(action, primaryButton);

            Button secondaryButton = new Button(dbDao.getKeyBinding(action, false));
            secondaryButton.setStyle(buttonStyle);
            secondaryButton.setPrefWidth(140);
            secondaryButton.setOnAction(e -> startKeyBinding(action, false, secondaryButton));
            secondaryButtons.put(action, secondaryButton);

            settingsPane.add(actionLabel, 0, row);
            settingsPane.add(primaryButton, 1, row);
            settingsPane.add(secondaryButton, 2, row);
            row++;
        }

        Button replayIntroButton = new Button("Intro nochmal anzeigen");
        replayIntroButton.setStyle(buttonStyle);
        replayIntroButton.setPrefWidth(300);
        replayIntroButton.setOnAction(e -> {
            IntroScene intro = new IntroScene(settingsStage, () -> settingsAnimation.play(), dbDao, true);
            intro.show();
        });

        Button closeButton = new Button("Schließen");
        closeButton.setStyle(buttonStyle);
        closeButton.setPrefWidth(300);
        closeButton.setOnAction(e -> {
            settingsAnimation.stop();
            settingsStage.close();
        });

        settingsPane.add(volumeLabel, 0, 0);
        settingsPane.add(volumeSlider, 1, 0, 2, 1); // Spanne über 2 Spalten
        settingsPane.add(primaryHeader, 1, 1); // Kopfzeile
        settingsPane.add(secondaryHeader, 2, 1); // Kopfzeile
        settingsPane.add(replayIntroButton, 1, row, 2, 1);
        settingsPane.add(closeButton, 1, row + 1, 2, 1);

        settingsRoot.getChildren().add(settingsPane);
        Scene settingsScene = new Scene(settingsRoot, 800, 600);
        settingsScene.setOnKeyPressed(e -> {
            if (waitingForKey != null) {
                String newKey = e.getCode().toString();
                if (isKeyValid(newKey)) {
                    String keyType = waitingForKey + (isPrimary ? "_primary" : "_secondary");
                    dbDao.saveSetting(keyType, newKey);
                    updateButtonText(waitingForKey, isPrimary);
                    waitingForKey = null;
                } else {
                    if (JumpAndRun.debugMode) {
                        System.out.println("Taste " + newKey + " ist bereits belegt!");
                    }
                }
            }
        });

        settingsStage.setScene(settingsScene);
        settingsStage.show();

        if (JumpAndRun.debugMode) {
            System.out.println("SettingsScreen gezeigt mit Animation.");
        }
    }

    private void startKeyBinding(String action, boolean primary, Button button) {
        waitingForKey = action;
        isPrimary = primary;
        button.setText("Drücke eine Taste...");
    }

    private void updateButtonText(String action, boolean primary) {
        Button button = primary ? primaryButtons.get(action) : secondaryButtons.get(action);
        button.setText(dbDao.getKeyBinding(action, primary));
    }

    private boolean isKeyValid(String newKey) {
        List<String> allBindings = dbDao.getAllKeyBindings();
        return !allBindings.contains(newKey);
    }

    private String getActionName(String action) {
        return switch (action) {
            case "move_left" -> "Bewegung Links";
            case "move_right" -> "Bewegung Rechts";
            case "jump" -> "Springen";
            case "shoot" -> "Schießen";
            default -> action;
        };
    }
}