package com.eumel;

import com.eumel.data.DBDAO;
import com.eumel.level.design.FallingLeaves;
import com.eumel.ui.MainMenu;
import com.eumel.ui.PauseDialog;
import com.eumel.ui.UIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class JumpAndRun extends Application {

    private DBDAO dbDAO;
    private int startLevel = 1;
    private int startDesign = 0;
    public static boolean debugMode = true;
    public static boolean debugKollisionGround = false;
    public static boolean debugPlattform = false;
    public static boolean debugKollisionBox = false;
    public static boolean debugKollisionWall = false;
    public static boolean debugJump = false;
    public static boolean debugBossLive =  false;
    public static boolean debugGround = false;
    public static boolean debugLabel = false;
    public static boolean debugEnemy  = false;
    private boolean hasLoggedBossHealthBarError = false;
    public static boolean debugProjektil = false;
    public static boolean debugMusic = false;
    public static boolean debugMainFrame = false;
    private static boolean introShown = true;
    private final int MAX_LEVELS = 10;
    static Font customFont;
    private Label scoreLabel;
    private Label mousePositionLabel;
    private ImageView backgroundView;
    private Image[] backgroundImages;
    private Pane gamePane;
    private StackPane rootPane;
    private Scene gameScene;
    private MediaPlayer mediaPlayer;
    public static AudioManager audioManager;
    private FallingLeaves fallingLeaves;
    private static Stage primaryStage;
    private GameEngine gameEngine;

    @Override
    public void init() {
        customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/Riffic.ttf"), 20);
        if (customFont == null) {
            System.err.println("Fehler beim Laden der Schriftart Riffic.ttf");
        } else {
            System.out.println("Schriftart Riffic geladen: " + customFont.getName());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        JumpAndRun.primaryStage = primaryStage;
        UIUtils.setStageIcon(primaryStage);
        primaryStage.setTitle("Eumels Höllish Süßes Abenteuer");
        System.out.println("Startmethode beginnt...");
        dbDAO = new DBDAO();
        audioManager = new AudioManager(this, dbDAO);
        System.out.println("DBDAO initialisiert.");
        String[] args = getParameters().getRaw().toArray(new String[0]);
        if (args.length >= 2) {
            try {
                startLevel = Integer.parseInt(args[0]) - 1;
                startDesign = Integer.parseInt(args[1]) - 1;
                if (startLevel < 0 || startLevel >= MAX_LEVELS || startDesign < 0) {
                    System.out.println("Ungültige Startparameter. Starte normales Spiel.");
                    startLevel = 0;
                    startDesign = 0;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ungültige Parameter. Starte normales Spiel.");
                startLevel = 0;
                startDesign = 0;
            }
        }

        System.out.println("Rufe showMainMenu auf...");
        new MainMenu(this, dbDAO, primaryStage).show();
        System.out.println("showMainMenu abgeschlossen.");

        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Fenster wird geschlossen.");
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
    }

    public void startGame(Stage primaryStage, int startLevel, int startDesign) {
        this.startLevel = startLevel;
        this.startDesign = startDesign;

        gamePane = new Pane();
        rootPane = new StackPane(gamePane);
        gameScene = new Scene(rootPane, 800, 600);

        if (fallingLeaves == null) {
            fallingLeaves = new FallingLeaves(gamePane, 800, 600);
        } else {
            fallingLeaves.stop();
        }

        backgroundImages = new Image[3];
        try {
            backgroundImages[0] = new Image(getClass().getResource("/images/background/bg1.png").toExternalForm());
            backgroundImages[1] = new Image(getClass().getResource("/images/background/bg2.png").toExternalForm());
            backgroundImages[2] = new Image(getClass().getResource("/images/background/bg3.png").toExternalForm());
            if (debugMode) {
                System.out.println("Hintergrundbilder geladen:");
                for (int i = 0; i < backgroundImages.length; i++) {
                    System.out.println("bg" + (i + 1) + ".png Größe: " + backgroundImages[i].getWidth() + "x" + backgroundImages[i].getHeight());
                }
            }
        } catch (Exception e) {
            backgroundImages[0] = null;
            backgroundImages[1] = null;
            backgroundImages[2] = null;
        }

        backgroundView = new ImageView();
        if (backgroundImages[0] != null) {
            backgroundView.setImage(backgroundImages[0]);
            backgroundView.setFitWidth(800);
            backgroundView.setFitHeight(600);
            backgroundView.setX(0);
            backgroundView.setY(0);
        }

        scoreLabel = new Label("Punkte: " + 0);
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        mousePositionLabel = new Label("Maus: (0, 0)");
        mousePositionLabel.setLayoutX(650);
        mousePositionLabel.setLayoutY(10);
        mousePositionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        gamePane.getChildren().addAll(backgroundView, scoreLabel);
        if (startLevel > 0 || startDesign > 0) {
            gamePane.getChildren().add(mousePositionLabel);
        }

        gameEngine = new GameEngine(this, primaryStage, gamePane);
        gameEngine.initializeGame(startLevel, startDesign, gameScene);

        gameScene.setOnKeyPressed(event -> {
            gameEngine.getPressedKeys().add(event.getCode().toString());
            if (event.getCode().toString().equals("ESCAPE")) {
                if (!gameEngine.isPaused()) {
                    gameEngine.setPaused(true);
                    Platform.runLater(() -> new PauseDialog(this, gameEngine, primaryStage).show());
                }
            }
        });

        gameScene.setOnKeyReleased(event -> {
            gameEngine.getPressedKeys().remove(event.getCode().toString());
        });

        primaryStage.setScene(gameScene);
        primaryStage.show();

        fallingLeaves.start();
    }

    // Getter-Methoden
    public DBDAO getDbDAO() {
        return dbDAO;
    }

    public Label getScoreLabel() {
        return scoreLabel;
    }

    public Label getMousePositionLabel() {
        return mousePositionLabel;
    }

    public ImageView getBackgroundView() {
        return backgroundView;
    }

    public FallingLeaves getFallingLeaves() {
        return fallingLeaves;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public int getStartDesign() {
        return startDesign;
    }

    public boolean hasLoggedBossHealthBarError() {
        return hasLoggedBossHealthBarError;
    }

    public void setHasLoggedBossHealthBarError(boolean hasLogged) {
        this.hasLoggedBossHealthBarError = hasLogged;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}