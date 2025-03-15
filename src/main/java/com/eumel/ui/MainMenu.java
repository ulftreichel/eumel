package com.eumel.ui;

import com.eumel.IntroScene;
import com.eumel.JumpAndRun;
import com.eumel.MainMenuAnimation;
import com.eumel.SettingsScreen;
import com.eumel.data.DBDAO;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

import static com.eumel.JumpAndRun.audioManager;

public class MainMenu {
    private final JumpAndRun jumpAndRun;
    private final DBDAO dbDAO;
    private final Stage primaryStage;
    private MainMenuAnimation menuAnimation;

    public MainMenu(JumpAndRun jumpAndRun, DBDAO dbDAO, Stage primaryStage) {
        this.jumpAndRun = jumpAndRun;
        this.dbDAO = dbDAO;
        this.primaryStage = primaryStage;
    }

    public void show() {
        StackPane menuRoot = new StackPane();
        menuRoot.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        menuRoot.getStyleClass().add("highscore-root");
        audioManager.startMenuAudio();
        menuAnimation = new MainMenuAnimation(menuRoot);
        menuAnimation.play();

        VBox menuPane = new VBox(20);
        menuPane.getStyleClass().add("highscore-pane");

        Button newGameButton = new Button("Neues Spiel");
        Button highscoreButton = new Button("Highscores");
        Button debugButton = new Button("Debug");
        Button settingsButton = new Button("Einstellungen");
        Button exitButton = new Button("Verlassen");

        newGameButton.getStyleClass().add("dialog-button");
        highscoreButton.getStyleClass().add("dialog-button");
        debugButton.getStyleClass().add("dialog-button");
        settingsButton.getStyleClass().add("dialog-button");
        exitButton.getStyleClass().add("dialog-button");

        menuPane.getChildren().addAll(newGameButton, highscoreButton, debugButton, settingsButton, exitButton);
        menuRoot.getChildren().add(menuPane);

        Scene menuScene = new Scene(menuRoot, 800, 600);
        primaryStage.setScene(menuScene);
        UIUtils.setStageIcon(primaryStage);
        primaryStage.show();

        // Event-Handler
        newGameButton.setOnAction(e -> {
            menuAnimation.stop();
            if (!dbDAO.isIntroShown()) {
                new IntroScene(primaryStage, () -> jumpAndRun.startGame(primaryStage, 0, 0), dbDAO, false).show();
                jumpAndRun.getAudioManager().startIntroAudio(true);
            } else {
                jumpAndRun.startGame(primaryStage, jumpAndRun.getStartLevel(), jumpAndRun.getStartDesign());
            }
        });
        highscoreButton.setOnAction(e -> new HighscoreView(jumpAndRun, dbDAO, primaryStage).show());
        debugButton.setOnAction(e -> {
            JumpAndRun.debugMode = true;
            menuAnimation.stop();
            jumpAndRun.startGame(primaryStage, 0, 0);
        });
        settingsButton.setOnAction(e -> new SettingsScreen(jumpAndRun, jumpAndRun.getAudioManager(), dbDAO).show());
        exitButton.setOnAction(e -> {
            menuAnimation.stop();
            dbDAO.close();
            primaryStage.close();
        });
    }

    public int getStartLevel() {
        return jumpAndRun.getStartLevel();
    }

    public int getStartDesign() {
        return jumpAndRun.getStartDesign();
    }
}