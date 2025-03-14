package com.eumel;

import com.eumel.level.*;
import com.eumel.level.design.*;
import com.eumel.opponent.*;
import com.eumel.data.DBDAO;
import com.eumel.data.Highscore;
import com.eumel.data.Task;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.util.Duration;

import java.util.*;

public class JumpAndRun extends Application {

    private DBDAO dbDAO;
    private int startLevel = 0;
    private int startDesign = 0;
    public static boolean debugMode = false;
    private boolean debugKollisionGround = false;
    private boolean debugPlattform = false;
    private boolean debugKollisionBox = false;
    private boolean debugKollisionWall = false;
    private boolean debugJump = false;
    private boolean debugBossLive =  false;
    private boolean debugGround = false;
    private boolean debugLabel = false;
    private boolean debugEnemy  = false;
    private boolean jumpPressedLastFrame = false;
    private boolean isScore = false;
    private boolean isPaused = false;
    private boolean isTransitioning = false;
    private boolean facingRight = true;
    private boolean canShoot = false;
    private boolean bossDefeated = false;
    private boolean hasLoggedBossHealthBarError = false;
    public static boolean debugProjektil = false;
    public static boolean debugMusic = false;
    public static boolean debugMainFrame = false;
    private static boolean introShown = true;
    private static final long TUNNEL_ANIMATION_DURATION = 5_000_000_000L;
    private final double PLAYER_WIDTH = 32;
    private final double PLAYER_HEIGHT = 64;
    private double playerX = 50;
    private double playerY = 480;
    private double velocityX = 0;
    private double velocityY = 0;
    private double bossHealthBarInitialX = 300;
    private Rectangle bossHealthBar;
    private int jumpCount = 0;
    private int score = 0;
    private int playerDamage = 100;
    private double damageMultiplier = 1.0;
    private int powerupLevel = 0;
    private int currentLevel = 0;
    private int lastDirection = 1;
    private final double GRAVITY = 0.5;
    private final double JUMP_STRENGTH = -8;
    private final double DOUBLE_JUMP_STRENGTH = -11;
    private double MOVE_SPEED = 2.0;
    private int activeSpeedBoosts = 0;
    private static final double MAX_SPEED = 3.0;
    private static final int MAX_SPEED_BOOSTS = 3;
    private static final double SPEED_BOOST_MULTIPLIER = 1.5;
    public static final double GROUND_LEVEL = 475;
    private final Set<String> pressedKeys = new HashSet<>();
    private List<BreakablePlatform> platforms = new ArrayList<>();
    private List<Rectangle> groundSegments = new ArrayList<>();
    private List<Diamond> diamonds = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> enemyProjectiles = new ArrayList<>();
    private List<ImageView> groundTiles = new ArrayList<>();
    private List<ImageView> platformViews = new ArrayList<>();
    private List<Powerup> powerups = new ArrayList<>();
    private List<TreasureChest> treasureChests = new ArrayList<>();
    private List<MiniBoss> miniBosses = new ArrayList<>();
    private Boss boss;
    private long bossDefeatTime = 0;
    private long lastBossHealthUpdateTime = 0;
    private int lastBossHealth = -1;
    private final int MAX_LEVELS = 10;
    static Font customFont;
    private Text levelText;
    private Label scoreLabel;
    private Label mousePositionLabel;
    private String buttonStyle;
    private AnimationTimer gameLoop;
    private ImageView backgroundView;
    private Image[] backgroundImages;
    private LevelDesign[] levels;
    private int currentDesignIndex = 0;
    private ImageView playerImageView;
    private Image playerStandImage;
    private Image playerMoveImage;
    private Image playerStandLeftImage;
    private Image playerMoveLeftImage;
    private Image groundTileImage;
    private Image holeTileImage;
    private ImageView tunnelLeft;
    private ImageView tunnelRight;
    private Pane gamePane;
    private StackPane rootPane;
    private Pane pauseOverlay;
    private Scene gameScene;
    private FallingLeaves fallingLeaves;
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 500_000_000;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private IntroScene introScene;
    private MainMenuAnimation menuAnimation;
    private static Stage primaryStage;

    @Override
    public void init() {
        // Schriftart beim Initialisieren laden, alternativ würde auch noch PressStart2P.ttf zur verfügung stehen
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
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/player/bonbon.png"))));
        primaryStage.setTitle("Eumels Höllisch Süßes Abenteuer");
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
        showMainMenu(primaryStage);
        System.out.println("showMainMenu abgeschlossen.");

        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Fenster wird geschlossen.");
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
    }

    private void showMainMenu(Stage primaryStage) {
        System.out.println("Betrete showMainMenu...");
        if (fallingLeaves != null) {
            fallingLeaves.stop();
        }

        StackPane menuRoot = new StackPane();
        menuRoot.setStyle("-fx-background-color: #333333;");
        System.out.println("StackPane erstellt.");

        menuAnimation = new MainMenuAnimation(menuRoot);
        menuAnimation.play();

        VBox menuPane = new VBox(20);
        menuPane.setStyle("-fx-alignment: center;");
        Button newGameButton = new Button("Neues Spiel");
        Button highscoreButton = new Button("Highscores");
        Button debugButton = new Button("Debug");
        Button settingsButton = new Button("Einstellungen");
        Button exitButton = new Button("Verlassen");

        String buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + customFont.getName() + "';";
        newGameButton.setStyle(buttonStyle);
        highscoreButton.setStyle(buttonStyle);
        debugButton.setStyle(buttonStyle);
        settingsButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        menuPane.getChildren().addAll(newGameButton, highscoreButton, debugButton, settingsButton, exitButton);
        menuRoot.getChildren().add(menuPane);
        System.out.println("Menü-Buttons hinzugefügt.");

        Scene menuScene = new Scene(menuRoot, 800, 600);
        primaryStage.setScene(menuScene);
        primaryStage.show();
        System.out.println("Fenster gezeigt mit Animation.");
        audioManager.startMenuAudio();

        // Event-Handler
        newGameButton.setOnAction(e -> {
            menuAnimation.stop();
            if (!dbDAO.isIntroShown()) {
                introScene = new IntroScene(primaryStage, () -> startGame(primaryStage, 0, 0), dbDAO, false); // fromSettings = false
                introScene.show();
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                startGame(primaryStage, startLevel, startDesign);
            }
        });
        highscoreButton.setOnAction(e -> showHighscores(primaryStage));
        debugButton.setOnAction(e -> {
            debugMode = true;
            debugKollisionGround = true;
            debugPlattform = true;
            debugKollisionBox = true;
            debugKollisionWall = true;
            debugProjektil = true;
            debugJump = true;
            menuAnimation.stop();
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            startGame(primaryStage, 0, 0);
        });
        settingsButton.setOnAction(e -> new SettingsScreen(this, audioManager, dbDAO).show());
        exitButton.setOnAction(e -> {
            menuAnimation.stop();
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            dbDAO.close();
            primaryStage.close();
        });
    }

    // Starte das Spiel
    private void loadLevel(Pane root, LevelDesign level, boolean useDebug) {
        root.getChildren().clear();
        platforms.clear();
        groundSegments.clear();
        diamonds.clear();

        for (Enemy enemy : enemies) {
            if (enemy.getImageView() != null) {
                root.getChildren().remove(enemy.getImageView());
            }
        }
        enemies.clear();

        groundTiles.forEach(root.getChildren()::remove);
        groundTiles.clear();
        platformViews.forEach(root.getChildren()::remove);
        platformViews.clear();

        for (Projectile projectile : projectiles) {
            root.getChildren().remove(projectile.getImageView());
        }
        projectiles.clear();

        if (tunnelLeft != null) {
            root.getChildren().remove(tunnelLeft);
            tunnelLeft = null;
        }
        if (tunnelRight != null) {
            root.getChildren().remove(tunnelRight);
            tunnelRight = null;
        }
        if (levelText != null) {
            root.getChildren().remove(levelText);
            levelText = null;
        }

        if (backgroundView != null) {
            root.getChildren().add(backgroundView);
            backgroundView.toBack();
            if (debugMode) {
                System.out.println("Hintergrund nach dem Löschen wieder hinzugefügt und nach hinten verschoben.");
            }
        }

        if (scoreLabel != null) {
            root.getChildren().add(scoreLabel);
        }

        if (startLevel > 0 || startDesign > 0) {
            root.getChildren().add(mousePositionLabel);
        }

        if (playerImageView != null && !root.getChildren().contains(playerImageView)) {
            root.getChildren().add(playerImageView);
            playerImageView.toFront();
            if (debugMode) {
                System.out.println("Spieler wieder hinzugefügt und nach vorne verschoben. Anzahl Kinder: " + root.getChildren().size());
            }
        }

        miniBosses = level.getMiniBosses();
        for (MiniBoss miniBoss : miniBosses) {
            if (miniBoss != null) {
                root.getChildren().add(miniBoss.getImageView());
                if (debugEnemy) {
                    System.out.println("Miniboss hinzugefügt bei x=" + miniBoss.getImageView().getX());
                }
            }
        }

        if (boss != null) {
            root.getChildren().remove(boss.getImageView());
            if (bossHealthBar != null) {
                root.getChildren().remove(bossHealthBar);
                bossHealthBar = null;
            }
        }

        List<Boss> bosses = level.getBosses();
        boss = bosses.isEmpty() ? null : bosses.get(0);
        if (boss != null) {
            root.getChildren().add(boss.getImageView());
            bossHealthBar = new Rectangle(bossHealthBarInitialX, 50, 400, 20);
            bossHealthBar.setFill(Color.RED);
            bossHealthBar.setStroke(Color.BLACK);
            root.getChildren().add(bossHealthBar);
            if (useDebug) {
                System.out.println("Boss geladen und bossHealthBar initialisiert: x=" + bossHealthBarInitialX + ", Breite=400");
            }
        }

        // Powerups laden
        powerups = level.getPowerups();
        for (Powerup powerup : powerups) {
            root.getChildren().add(powerup.getImageView());
            if (useDebug) {
                System.out.println("Powerup hinzugefügt bei x=" + powerup.getX());
            }
        }

        treasureChests = level.getTreasureChests();
        for (TreasureChest chest : treasureChests) {
            root.getChildren().add(chest.getImageView());
            if (useDebug) {
                System.out.println("Schatztruhe hinzugefügt bei x=" + chest.getX());
            }
        }

        if (groundTileImage == null) {
            System.err.println("Fehler: groundTileImage ist null. Bodenfliesen können nicht geladen werden. holeTileImage: " + (holeTileImage != null));
            return;
        }

        int tileWidth = (int) groundTileImage.getWidth();
        int tileHeight = (int) groundTileImage.getHeight();
        int numTiles = (int) Math.ceil(800.0 / tileWidth);
        List<Integer> holePositions = level.getHolePositions();
        for (int i = 0; i < numTiles; i++) {
            ImageView groundTile = new ImageView(holePositions.contains(i) && holeTileImage != null ? holeTileImage : groundTileImage);
            groundTile.setFitWidth(tileWidth);
            groundTile.setFitHeight(tileHeight);
            groundTile.setX(i * tileWidth);
            groundTile.setY(460);
            root.getChildren().add(groundTile);
            groundTiles.add(groundTile);
            if (debugGround) {
                System.out.println("Bodenfliese hinzugefügt bei x=" + (i * tileWidth) + ", y=460, Typ=" + (holePositions.contains(i) ? "Loch" : "Boden") + ", Bild: " + (holePositions.contains(i) && holeTileImage != null ? "hole" : "ground"));
            }
        }

        // Wände
        for (Wall wall : level.getWalls()) {
            wall.render(root);
            if (debugLabel) {
                Label positionLabel = new Label("(" + (int)wall.getX() + ", " + (int)wall.getY() + ")");
                positionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                positionLabel.setLayoutX(wall.getX() + 5);
                positionLabel.setLayoutY(wall.getY() + (wall.getHeight() / 2) - 6);
                root.getChildren().add(positionLabel);
                System.out.println("Wand geladen bei x=" + wall.getX() + ", y=" + wall.getY() +
                        ", Breite=" + wall.getWidth() + ", Höhe=" + wall.getHeight());
            }
        }

        for (PlatformInfo platformInfo : level.getPlatforms()) {
            ImageView platformView = new ImageView(new Image(getClass().getResource("/images/level/plattform.png").toExternalForm()));
            platformView.setFitWidth(platformInfo.getWidth());
            platformView.setFitHeight(platformInfo.getHeight());
            platformView.setX(platformInfo.getX());
            platformView.setY(platformInfo.getY());

            BreakablePlatform breakablePlatform = new BreakablePlatform(
                    platformInfo.getX(),
                    platformInfo.getY(),
                    platformInfo.getWidth(),
                    platformInfo.getHeight(),
                    platformInfo.isBreakable(),
                    platformView
            );
            platforms.add(breakablePlatform);
            root.getChildren().add(platformView);
            platformViews.add(platformView);

            if (debugLabel) {
                Label positionLabel = new Label("(" + (int)platformInfo.getX() + ", " + (int)platformInfo.getY() + ")");
                positionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                positionLabel.setLayoutX(platformInfo.getX() + 5);
                positionLabel.setLayoutY(platformInfo.getY() + (platformInfo.getHeight() / 2) - 6);
                root.getChildren().add(positionLabel);
                System.out.println("Plattform geladen bei x=" + platformInfo.getX() + ", y=" + platformInfo.getY() +
                        ", Bounds: minY=" + platformView.getBoundsInParent().getMinY() +
                        ", maxY=" + platformView.getBoundsInParent().getMaxY());
            }
        }

        for (Rectangle ground : level.getGroundSegments()) {
            groundSegments.add(ground);
            root.getChildren().add(ground);
        }
        for (Diamond diamond : level.getDiamonds()) {
            diamonds.add(diamond);
            root.getChildren().add(diamond);
        }
        for (Enemy enemy : level.getEnemies()) {
            enemies.add(enemy);
            root.getChildren().add(enemy.getImageView());
            enemy.getImageView().setX(enemy.getX());
            enemy.getImageView().setY(enemy.getY());
            if (debugEnemy) {
                System.out.println("Gegner hinzugefügt bei x=" + enemy.getImageView().getX() + ", y=" + enemy.getImageView().getY() +
                        ", Breite=" + enemy.getImageView().getFitWidth() + ", Höhe=" + enemy.getImageView().getFitHeight());
            }
        }

        // Tunneleingang und Text hinzufügen
        if (!level.hasNextDesign()) {
            if (tunnelLeft != null) root.getChildren().remove(tunnelLeft);
            if (tunnelRight != null) root.getChildren().remove(tunnelRight);
            if (levelText != null) root.getChildren().remove(levelText);

            Image tunnelLeftImage = new Image(getClass().getResource("/images/level/tunnel_entrance_left.png").toExternalForm());
            tunnelLeft = new ImageView(tunnelLeftImage);
            tunnelLeft.setFitWidth(332);
            tunnelLeft.setFitHeight(444);
            tunnelLeft.setX(550);
            tunnelLeft.setY(95);
            tunnelLeft.setVisible(false);

            Image tunnelRightImage = new Image(getClass().getResource("/images/level/tunnel_entrance_right.png").toExternalForm());
            tunnelRight = new ImageView(tunnelRightImage);
            tunnelRight.setFitWidth(332);
            tunnelRight.setFitHeight(444);
            tunnelRight.setX(550);
            tunnelRight.setY(95);
            tunnelRight.setVisible(false);

            levelText = new Text("LEVEL " + (currentLevel + 2) + " ->");
            if (customFont == null) {
                System.err.println("customFont konnte nicht geladen werden!");
                levelText.setFont(Font.font("Arial", 80));
            } else {
                levelText.setFont(customFont);
            }
            levelText.setFill(Color.WHITE);
            levelText.setStroke(Color.BLACK);
            levelText.setStrokeWidth(1);
            levelText.setX(200);
            levelText.setY(200);
            levelText.setVisible(false);

            root.getChildren().add(tunnelLeft);
            root.getChildren().add(levelText);
            if (playerImageView != null && root.getChildren().contains(playerImageView)) {
                root.getChildren().remove(playerImageView);
                root.getChildren().add(playerImageView);
            }
            root.getChildren().add(tunnelRight);

            if (debugLabel) {
                System.out.println("Tunneleingang initialisiert (unsichtbar, letztes Design von Level " + (currentLevel + 1) + ")");
            }
        }

        System.out.println("Level geladen (Design " + (currentDesignIndex + 1) + " von Level " + (currentLevel + 1) + ")");
    }

    private void startGame(Stage primaryStage, int startLevel, int startDesign) {
        gamePane = new Pane();
        rootPane = new StackPane(gamePane);
        gameScene = new Scene(rootPane, 800, 600);

        playerX = 50;
        playerY = 480 - PLAYER_HEIGHT;
        velocityX = 0;
        velocityY = 0;
        jumpCount = 0;
        score = 0;
        playerDamage = 100;
        isScore = false;
        powerupLevel = 0;
        pressedKeys.clear();
        jumpPressedLastFrame = false;
        facingRight = true;
        projectiles.clear();
        enemyProjectiles.clear();
        powerups.forEach(powerup -> gamePane.getChildren().remove(powerup.getImageView()));
        powerups.clear();
        treasureChests.forEach(treasureChest -> gamePane.getChildren().remove(treasureChest.getImageView()));
        treasureChests.clear();
        miniBosses.forEach(miniBosse -> gamePane.getChildren().remove(miniBosse.getImageView()));
        miniBosses.clear();
        groundTiles.forEach(gamePane.getChildren()::remove);
        groundTiles.clear();
        platformViews.forEach(gamePane.getChildren()::remove);
        platformViews.clear();
        groundSegments.clear();
        platforms.clear();
        enemies.forEach(enemy -> gamePane.getChildren().remove(enemy.getImageView()));
        enemies.clear();
        diamonds.forEach(diamond -> gamePane.getChildren().remove(diamond));
        diamonds.clear();
        powerups.forEach(powerup -> gamePane.getChildren().remove(powerup.getImageView()));

        audioManager.startGameAudio(boss != null && boss.isActive());
        if (boss != null) {
            gamePane.getChildren().remove(boss.getImageView());
            boss = null;
        }

        if (fallingLeaves == null) {
            fallingLeaves = new FallingLeaves(gamePane, 800, 600);
        } else {
            fallingLeaves.stop();
        }

        // Ressourcen laden
        try {
            groundTileImage = new Image(getClass().getResource("/images/level/ground_tile.png").toExternalForm());
            holeTileImage = new Image(getClass().getResource("/images/level/hole_tile.png").toExternalForm());
            if (debugMode) {
                System.out.println("Bodenbild geladen: ground_tile.png (" + groundTileImage.getWidth() + "x" + groundTileImage.getHeight() + ")");
                if (holeTileImage != null) {
                    System.out.println("Lochbild geladen: hole_tile.png (" + holeTileImage.getWidth() + "x" + holeTileImage.getHeight() + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Laden des Bodenbildes: " + e.getMessage());
            groundTileImage = null;
            holeTileImage = null;
        }

        try {
            playerStandImage = new Image(getClass().getResource("/images/player/player1stand.png").toExternalForm());
            playerMoveImage = new Image(getClass().getResource("/images/player/player1move.png").toExternalForm());
            playerStandLeftImage = new Image(getClass().getResource("/images/player/player1standleft.png").toExternalForm());
            playerMoveLeftImage = new Image(getClass().getResource("/images/player/player1moveleft.png").toExternalForm());
            if (debugMode) {
                System.out.println("Spielerbilder geladen: player1stand.png (" + playerStandImage.getWidth() + "x" + playerStandImage.getHeight() + "), player1move.png (" + playerMoveImage.getWidth() + "x" + playerMoveImage.getHeight() + "), player1standleft.png (" + playerStandLeftImage.getWidth() + "x" + playerStandLeftImage.getHeight() + "), player1moveleft.png (" + playerMoveLeftImage.getWidth() + "x" + playerMoveLeftImage.getHeight() + ")");
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Spielerbilder: " + e.getMessage());
            return;
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

        scoreLabel = new Label("Punkte: " + score);
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        mousePositionLabel = new Label("Maus: (0, 0)");
        mousePositionLabel.setLayoutX(650);
        mousePositionLabel.setLayoutY(10);
        mousePositionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        playerImageView = new ImageView(playerStandImage);
        playerImageView.setFitWidth(PLAYER_WIDTH);
        playerImageView.setFitHeight(PLAYER_HEIGHT);
        playerImageView.setX(playerX);
        playerImageView.setY(playerY);

        gamePane.getChildren().addAll(backgroundView, playerImageView, scoreLabel);
        if (startLevel > 0 || startDesign > 0) {
            gamePane.getChildren().add(mousePositionLabel);
        }

        // Level-Initialisierung
        this.startLevel = startLevel;
        this.startDesign = startDesign;
        this.currentLevel = startLevel;
        this.currentDesignIndex = startDesign;
        levels = new LevelDesign[MAX_LEVELS];

        if (debugMode) {
            levels[0] = new Level1();
            currentLevel = 0;
            levels[currentLevel].resetDesign();
            currentDesignIndex = 0;
            loadLevel(gamePane, levels[currentLevel], true);
            System.out.println("Level1 geladen bei Level " + (currentLevel + 1) + ", Design " + (currentDesignIndex + 1) + " (Debug-Modus)");
        } else {
            levels[0] = new Level1();
            levels[1] = new Level2();
            levels[2] = new Level3();
            levels[3] = new Level4();
            levels[4] = new Level5();
            levels[5] = new Level6();
            levels[6] = new Level7();
            levels[7] = new Level8();
            levels[8] = new Level9();
            levels[9] = new Level10();

            if (startLevel == 0 && startDesign == 0) {
                currentLevel = 0;
                currentDesignIndex = 0;
                levels[currentLevel].resetDesign();
                loadLevel(gamePane, levels[currentLevel], false);
                System.out.println("Normales Spiel mit Level1 geladen.");
            } else {
                currentLevel = Math.min(startLevel, MAX_LEVELS - 1);
                levels[currentLevel].resetDesign();
                for (int i = 0; i < startDesign; i++) {
                    if (levels[currentLevel].hasNextDesign()) {
                        levels[currentLevel].nextDesign();
                    }
                }
                currentDesignIndex = startDesign;
                loadLevel(gamePane, levels[currentLevel], true);
                System.out.println("Level " + (currentLevel + 1) + ", Design " + (currentDesignIndex + 1) + " geladen.");
            }
        }

        if (boss != null) {
            gamePane.getChildren().remove(boss.getImageView());
            if (bossHealthBar != null) gamePane.getChildren().remove(bossHealthBar);
        }
        miniBosses = levels[currentLevel].getMiniBosses();
        List<Boss> bosses = levels[currentLevel].getBosses();
        boss = bosses.isEmpty() ? null : bosses.get(0);

        if (boss != null) {
            if (gamePane.getChildren().contains(boss.getImageView())) {
                gamePane.getChildren().remove(boss.getImageView());
            }
            if (bossHealthBar != null && gamePane.getChildren().contains(bossHealthBar)) {
                gamePane.getChildren().remove(bossHealthBar);
            }
            audioManager.startGameAudio(true);
            bossHealthBar = new Rectangle(0, 10, 400, 40);
            bossHealthBar.setFill(Color.RED);
            double paneWidth = 800;
            bossHealthBar.setX((paneWidth - bossHealthBar.getWidth()) / 2);
            bossHealthBarInitialX = bossHealthBar.getX();
            gamePane.getChildren().add(boss.getImageView());
            gamePane.getChildren().add(bossHealthBar);
            if (debugMode) {
                System.out.println("Boss hinzugefügt bei x=" + boss.getImageView().getX() + ", ScaleX=" + boss.getImageView().getScaleX());
                System.out.println("Boss-Lebensanzeige zentriert bei x=" + bossHealthBar.getX());
            }
            if (debugMode) {
                System.out.println("Boss und Healthbar erfolgreich hinzugefügt.");
            }
        }

        if (gameLoop == null) {
            gameLoop = new AnimationTimer() {
                private double lastLoggedY = playerY;
                private boolean goalReached = false;
                private long lastUpdateTime = 0;

                @Override
                public void handle(long now) {
                    if (isPaused) return;

                    if (Math.abs(playerY - lastLoggedY) >= 1.0) {
                        if (debugJump) {
                            System.out.println("Spielerposition: x=" + playerX + ", y=" + playerY + ", velocityY=" + velocityY);
                        }
                        lastLoggedY = playerY;
                    }

                    velocityX = 0;
                    // Bewegung nach links
                    if (pressedKeys.contains(dbDAO.getKeyBinding("move_left", true)) || pressedKeys.contains(dbDAO.getKeyBinding("move_left", false))) {
                        velocityX = -MOVE_SPEED;
                        facingRight = false;
                        playerX += velocityX;
                        playerImageView.setX(playerX);
                    } else if (pressedKeys.contains(dbDAO.getKeyBinding("move_right", true)) || pressedKeys.contains(dbDAO.getKeyBinding("move_right", false))) {
                        velocityX = MOVE_SPEED;
                        facingRight = true;
                        playerX += velocityX;
                        playerImageView.setX(playerX);
                    }

                    if (canShoot && (pressedKeys.contains(dbDAO.getKeyBinding("shoot", true)) || pressedKeys.contains(dbDAO.getKeyBinding("shoot", false))) && now - lastShotTime >= SHOOT_COOLDOWN) {
                        shoot(now);
                        lastShotTime = now;
                    }

                    boolean jumpPressed = pressedKeys.contains(dbDAO.getKeyBinding("jump", true)) || pressedKeys.contains(dbDAO.getKeyBinding("jump", false));
                    if (jumpPressed && !jumpPressedLastFrame) {
                        if (jumpCount < 2) {
                            velocityY = jumpCount == 0 ? JUMP_STRENGTH : DOUBLE_JUMP_STRENGTH;
                            jumpCount++;
                            if (debugJump) {
                                System.out.println("Sprung ausgelöst, jumpCount=" + jumpCount + ", velocityY=" + velocityY);
                            }
                        }
                    }
                    jumpPressedLastFrame = jumpPressed;

                    double oldPlayerX = playerX;
                    double oldPlayerY = playerY;

                    playerX += velocityX;
                    velocityY += GRAVITY;
                    playerY += velocityY;

                    if (playerX > 800 - PLAYER_WIDTH && !isTransitioning && (boss == null || !boss.isActive())) {
                        isTransitioning = true;
                        if (levels[currentLevel].hasNextDesign()) {
                            levels[currentLevel].nextDesign();
                            currentDesignIndex++;
                            loadLevel(gamePane, levels[currentLevel], debugMode);
                            playerX = 0;
                            playerY = findSafeYPosition();
                            velocityX = 0;
                            velocityY = 0;
                            jumpCount = 0;
                            if (debugMode) {
                                System.out.println("Neues Design geladen: Level " + (currentLevel + 1) + ", Design " + (currentDesignIndex + 1) + ", neue Position: x=" + playerX + ", y=" + playerY);
                            }
                        } else if (currentLevel < MAX_LEVELS - 1) {
                            currentLevel++;
                            currentDesignIndex = 0;
                            levels[currentLevel].resetDesign();
                            loadLevel(gamePane, levels[currentLevel], debugMode);
                            playerX = 0;
                            playerY = findSafeYPosition();
                            velocityX = 0;
                            velocityY = 0;
                            jumpCount = 0;
                            if (debugMode) {
                                System.out.println("Neues Level geladen: Level " + (currentLevel + 1) + ", Design 1, neue Position: x=" + playerX + ", y=" + playerY);
                            }
                        } else {
                            if (score > 0){
                                showHighscoreDialog(primaryStage);
                            }
                            gameLoop.stop();
                            return;
                        }
                        isTransitioning = false;
                    }

                    if (playerX < 0) {
                        playerX = 0;
                        velocityX = 0;
                    }

                    playerImageView.setX(playerX);
                    playerImageView.setY(playerY);
                    playerImageView.setScaleX(facingRight ? 1.0 : -1.0);

                    // Spieler-Projektile
                    Iterator<Projectile> projectileIterator = projectiles.iterator();
                    while (projectileIterator.hasNext()) {
                        Projectile projectile = projectileIterator.next();
                        projectile.update();
                        if (!projectile.isActive()) {
                            gamePane.getChildren().remove(projectile.getImageView());
                            projectileIterator.remove();
                        } else {
                            // Kollision mit Gegnern prüfen
                            Iterator<Enemy> enemyIterator = enemies.iterator();
                            while (enemyIterator.hasNext()) {
                                Enemy enemy = enemyIterator.next();
                                if (!enemy.isActive()) continue;

                                double projectileCenterX = projectile.getX() + projectile.getWidth() / 2;
                                double projectileCenterY = projectile.getY() + projectile.getHeight() / 2;
                                double enemyCenterX = enemy.getX() + enemy.getWidth() / 2;
                                double enemyCenterY = enemy.getY() + enemy.getHeight() / 2;

                                double horizontalTolerance = 10.0;
                                double verticalTolerance = enemy.getHeight() / 2 + projectile.getHeight() / 2;
                                boolean centerCollision = Math.abs(projectileCenterX - enemyCenterX) <= horizontalTolerance &&
                                        Math.abs(projectileCenterY - enemyCenterY) <= verticalTolerance;

                                // Debug-Ausgabe für Positionen
                                if (debugProjektil) {
                                    System.out.println("Kollisionsprüfung: Projektil (x=" + projectileCenterX + ", y=" + projectileCenterY + "), Gegner (x=" + enemyCenterX + ", y=" + enemyCenterY + ")");
                                    System.out.println("Abstand: x=" + Math.abs(projectileCenterX - enemyCenterX) + ", y=" + Math.abs(projectileCenterY - enemyCenterY) + ", centerCollision=" + centerCollision);
                                }

                                if (centerCollision) {
                                    enemy.takeDamage(projectile.getDamage());
                                    gamePane.getChildren().remove(projectile.getImageView());
                                    projectile.setInactive();
                                    projectileIterator.remove();
                                    score += 20;
                                    scoreLabel.setText("Punkte: " + score);
                                    isScore = true;
                                    if (debugMode) {
                                        System.out.println("Gegner getroffen! Lebenspunkte: " + enemy.getHealth() + ", Schaden: " + projectile.getDamage() + ", Neuer Score: " + score);
                                    }
                                    if (!enemy.isActive()) {
                                        gamePane.getChildren().remove(enemy.getImageView());
                                        enemyIterator.remove();
                                        score += 100;
                                        scoreLabel.setText("Punkte: " + score);
                                        if (debugMode) {
                                            System.out.println("Gegner besiegt! Neuer Score: " + score);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // Gegner-Projektile aktualisieren
                    Iterator<Projectile> enemyProjectileIterator = enemyProjectiles.iterator();
                    while (enemyProjectileIterator.hasNext()) {
                        Projectile enemyProjectile = enemyProjectileIterator.next();
                        enemyProjectile.update();
                        if (!enemyProjectile.isActive()) {
                            gamePane.getChildren().remove(enemyProjectile.getImageView());
                            enemyProjectileIterator.remove();
                            if (debugMode) {
                                System.out.println("Gegner-Projektil entfernt bei x=" + enemyProjectile.getX() + ", y=" + enemyProjectile.getY());
                            }
                        } else {
                            // Kollision mit Spielermitte prüfen
                            double playerCenterX = playerX + PLAYER_WIDTH / 2;
                            double playerCenterY = playerY + PLAYER_HEIGHT / 2;
                            double projectileCenterX = enemyProjectile.getX() + enemyProjectile.getWidth() / 2;
                            double projectileCenterY = enemyProjectile.getY() + enemyProjectile.getHeight() / 2;
                            double tolerance = 10.0; // Toleranzradius um die Mitte
                            if (Math.abs(projectileCenterX - playerCenterX) <= tolerance &&
                                    Math.abs(projectileCenterY - playerCenterY) <= tolerance) {
                                int enemyDamage = enemyProjectile.getDamage();
                                score -= enemyDamage; // Punkte reduzieren
                                scoreLabel.setText("Punkte: " + score);
                                gamePane.getChildren().remove(enemyProjectile.getImageView());
                                enemyProjectileIterator.remove();
                                if (debugMode) {
                                    System.out.println("Spieler getroffen! Neuer Punktestand: " + score + ", Schaden: " + enemyDamage);
                                }
                                if (score < 0) { // Spieler stirbt, wenn score unter 0
                                    gameLoop.stop();
                                    Platform.runLater(() -> showGameOver(primaryStage));
                                    return;
                                }
                            }
                        }
                    }

                    // Miniboss-Logik
                    for (MiniBoss miniBoss : miniBosses) {
                        if (miniBoss != null && miniBoss.isActive()) {
                            miniBoss.update(playerX, playerY, gamePane);
                            Iterator<Projectile> miniProjIterator = miniBoss.getProjectiles().iterator();
                            while (miniProjIterator.hasNext()) {
                                Projectile p = miniProjIterator.next();
                                p.update();
                                if (!p.isActive()) {
                                    gamePane.getChildren().remove(p.getImageView());
                                    miniProjIterator.remove();
                                } else if (playerImageView.getBoundsInParent().intersects(p.getImageView().getBoundsInParent())) {
                                    score -= p.getDamage();
                                    scoreLabel.setText("Punkte: " + score);
                                    gamePane.getChildren().remove(p.getImageView());
                                    miniProjIterator.remove();
                                    if (score < 0) {
                                        gameLoop.stop();
                                        Platform.runLater(() -> showGameOver(primaryStage));
                                        return;
                                    }
                                }
                            }

                            // Kollision Spieler-Projektil mit Miniboss
                            Iterator<Projectile> playerProjIterator = projectiles.iterator();
                            while (playerProjIterator.hasNext()) {
                                Projectile p = playerProjIterator.next();
                                if (miniBoss.isActive() && p.getImageView().getBoundsInParent().intersects(miniBoss.getImageView().getBoundsInParent())) {
                                    miniBoss.takeDamage(p.getDamage());
                                    gamePane.getChildren().remove(p.getImageView());
                                    playerProjIterator.remove();
                                    if (!miniBoss.isActive()) {
                                        gamePane.getChildren().remove(miniBoss.getImageView());
                                        score += 500;
                                        scoreLabel.setText("Punkte: " + score);
                                    }
                                }
                            }
                        }
                    }

                    // Powerups
                    Iterator<Powerup> powerupIterator = powerups.iterator();
                    while (powerupIterator.hasNext()) {
                        Powerup powerup = powerupIterator.next();
                        if (powerup.isActive() && playerImageView.getBoundsInParent().intersects(powerup.getImageView().getBoundsInParent())) {
                            if (powerup.getType() == Powerup.PowerupType.DAMAGE_BOOST) {
                                if (!canShoot) {
                                    canShoot = true;
                                    powerupLevel = 1;
                                    if (debugMode) {
                                        System.out.println("Schießen freigeschaltet! Powerup-Level: " + powerupLevel);
                                    }
                                } else {
                                    damageMultiplier *= 1.25;
                                    powerupLevel = Math.min(powerupLevel + 1, 4);
                                    if (debugMode) {
                                        System.out.println("Powerup gesammelt: DAMAGE_BOOST, Schaden: " + (100 * damageMultiplier) + ", Powerup-Level: " + powerupLevel);
                                    }
                                }
                            } else if (powerup.getType() == Powerup.PowerupType.SPEED_BOOST) {
                                MOVE_SPEED += 1;
                                if (debugMode) {
                                    System.out.println("Powerup gesammelt: SPEED_BOOST, Neue MOVE_SPEED: " + MOVE_SPEED);
                                }
                            }
                            powerup.setInactive();
                            gamePane.getChildren().remove(powerup.getImageView());
                            powerupIterator.remove();
                        }
                    }

                    // Projektile aktualisieren
                    projectiles.removeIf(projectile -> {
                        projectile.update();
                        if (!projectile.isActive()) {
                            gamePane.getChildren().remove(projectile.getImageView());
                            return true;
                        }
                        return false;
                    });

                    // Schatztruhen
                    for (TreasureChest chest : treasureChests) {
                        if (!chest.isActive()) continue;

                        double playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                        double playerCenterY = playerY + playerImageView.getFitHeight() / 2;
                        double chestCenterX = chest.getX() + chest.getImageView().getFitWidth() / 2;
                        double chestCenterY = chest.getY() + chest.getImageView().getFitHeight() / 2;

                        double playerChestHorizontalTolerance = 30.0;
                        double playerChestVerticalTolerance = 40.0;
                        boolean playerChestCollision = Math.abs(playerCenterX - chestCenterX) <= playerChestHorizontalTolerance &&
                                Math.abs(playerCenterY - chestCenterY) <= playerChestVerticalTolerance;

                        if (debugProjektil) {
                            System.out.println("Kollisionsprüfung Spieler-Schatztruhe: Spieler (x=" + playerCenterX + ", y=" + playerCenterY + "), Truhe (x=" + chestCenterX + ", y=" + chestCenterY + ")");
                            System.out.println("Abstand: x=" + Math.abs(playerCenterX - chestCenterX) + ", y=" + Math.abs(playerCenterY - chestCenterY) + ", playerChestCollision=" + playerChestCollision + ", isFull=" + chest.isFull());
                        }

                        if (playerChestCollision) {
                            // Sound: Truhe wird geöffnet
                            audioManager.playEffect("/sound/chest_open.mp3");
                            if (chest.isFull()) {
                                audioManager.playEffect("/sound/chest_full.mp3");
                                if (chest.givesPowerup()) {
                                    // Powerup vergeben
                                    Powerup.PowerupType powerupType = chest.getPowerupType();
                                    activatePowerup(powerupType);
                                    if (debugMode) {
                                        System.out.println("Schatztruhe geöffnet! Powerup erhalten: " + powerupType);
                                    }
                                } else {
                                    // Punkte vergeben
                                    score += chest.getScoreReward();
                                    scoreLabel.setText("Punkte: " + score);
                                    if (debugMode) {
                                        System.out.println("Schatztruhe geöffnet! Punkte erhalten: " + chest.getScoreReward() + ", Neuer Score: " + score);
                                    }
                                }
                            } else {
                                audioManager.playEffect("/sound/chest_empty.mp3");
                                if (debugMode) {
                                    System.out.println("Schatztruhe geöffnet! Truhe ist leer.");
                                }
                            }

                            chest.open();
                        }
                    }

                    // Boss-Logik
                    if (boss != null && boss.isActive()) {
                        boss.update(playerX, playerY, facingRight, gamePane);
                        double healthRatio = (double) boss.getHealth() / 5000.0;
                        double newWidth = 400 * healthRatio;
                        if (bossHealthBar != null) {
                            bossHealthBar.setWidth(newWidth);
                            bossHealthBar.setX(bossHealthBarInitialX + (400 - newWidth));
                        } else {
                            // Nur einmalig melden, um Spamming der Konsole zu vermeiden
                            if (debugMode && !hasLoggedBossHealthBarError) {
                                System.err.println("bossHealthBar ist null, aber Boss-Level aktiv! Initialisiere notfallmäßig.");
                                hasLoggedBossHealthBarError = true;
                                bossHealthBar = new Rectangle(bossHealthBarInitialX, 50, 400, 20);
                                bossHealthBar.setFill(Color.RED);
                                bossHealthBar.setStroke(Color.BLACK);
                                gamePane.getChildren().add(bossHealthBar);
                            }
                        }
                        // Debug-Ausgabe für Boss-Gesundheit
                        if (debugMode) {
                            long currentTime = System.nanoTime();
                            if (currentTime - lastBossHealthUpdateTime >= 1_000_000_000L || lastBossHealth != boss.getHealth()) {
                                System.out.println("Boss-Gesundheit: " + boss.getHealth() + " / 5000");
                                lastBossHealthUpdateTime = currentTime;
                                lastBossHealth = boss.getHealth();
                            }
                        }

                        // Kollision mit Spieler-Projektilen
                        Iterator<Projectile> projIterator = projectiles.iterator();
                        while (projIterator.hasNext()) {
                            Projectile p = projIterator.next();
                            double projectileCenterX = p.getX() + p.getWidth() / 2;
                            double projectileCenterY = p.getY() + p.getHeight() / 2;
                            double bossCenterX = boss.getImageView().getX() + boss.getImageView().getFitWidth() / 2;
                            double bossCenterY = boss.getImageView().getY() + boss.getImageView().getFitHeight() / 2;

                            double horizontalTolerance = 10.0;
                            double verticalTolerance = 60.0;
                            boolean centerCollision = Math.abs(projectileCenterX - bossCenterX) <= horizontalTolerance &&
                                    Math.abs(projectileCenterY - bossCenterY) <= verticalTolerance;

                            if (debugProjektil) {
                                System.out.println("Kollisionsprüfung Boss: Projektil (x=" + projectileCenterX + ", y=" + projectileCenterY + "), Boss (x=" + bossCenterX + ", y=" + bossCenterY + ")");
                                System.out.println("Abstand: x=" + Math.abs(projectileCenterX - bossCenterX) + ", y=" + Math.abs(projectileCenterY - bossCenterY) + ", centerCollision=" + centerCollision);
                            }

                            if (centerCollision) {
                                boss.takeDamage(p.getDamage());
                                gamePane.getChildren().remove(p.getImageView());
                                projIterator.remove();
                                if (debugMode) {
                                    System.out.println("Boss getroffen! Lebenspunkte: " + boss.getHealth() + ", Schaden: " + p.getDamage());
                                }
                                if (!boss.isActive()) {
                                    bossDefeated = true;
                                    bossDefeatTime = System.nanoTime();
                                    gamePane.getChildren().remove(boss.getImageView());
                                    gamePane.getChildren().remove(bossHealthBar);
                                    score += 1000;
                                    scoreLabel.setText("Punkte: " + score);
                                    if (debugMode) {
                                        System.out.println("Boss besiegt! Neuer Score: " + score);
                                    }
                                }
                            }
                        }

                        // Neue Kollisionsprüfung: Spieler mit Boss
                        double playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                        double playerCenterY = playerY + playerImageView.getFitHeight() / 2;
                        double bossCenterX = boss.getImageView().getX() + boss.getImageView().getFitWidth() / 2;
                        double bossCenterY = boss.getImageView().getY() + boss.getImageView().getFitHeight() / 2;

                        double playerBossHorizontalTolerance = 40.0; // Etwa 50 % der Breite des Bosses (170)
                        double playerBossVerticalTolerance = 90.0;  // Etwa 50 % der Höhe des Bosses (180)
                        boolean playerBossCollision = Math.abs(playerCenterX - bossCenterX) <= playerBossHorizontalTolerance &&
                                Math.abs(playerCenterY - bossCenterY) <= playerBossVerticalTolerance;

                        if (debugProjektil) {
                            System.out.println("Kollisionsprüfung Spieler-Boss: Spieler (x=" + playerCenterX + ", y=" + playerCenterY + "), Boss (x=" + bossCenterX + ", y=" + bossCenterY + ")");
                            System.out.println("Abstand: x=" + Math.abs(playerCenterX - bossCenterX) + ", y=" + Math.abs(playerCenterY - bossCenterY) + ", playerBossCollision=" + playerBossCollision);
                        }

                        if (playerBossCollision) {
                            if (debugMode) {
                                System.out.println("Spieler kollidiert mit Boss! Schaden zugefügt.");
                            }
                            score -= 500;
                            scoreLabel.setText("Leben: " + score);
                            if (score <= 0) {
                                gameLoop.stop();
                                Platform.runLater(() -> showTaskDialog(primaryStage));
                                return;
                            }
                            // Bewegung blockieren
                            if (velocityX > 0 && playerCenterX < bossCenterX) { // Spieler kommt von links
                                playerX = boss.getImageView().getX() - playerImageView.getFitWidth();
                                velocityX = 0;
                            } else if (velocityX < 0 && playerCenterX > bossCenterX) { // Spieler kommt von rechts
                                playerX = boss.getImageView().getX() + boss.getImageView().getFitWidth();
                                velocityX = 0;
                            }
                        }

                        // Kollision mit fallenden Steinen
                        for (FallingObject stone : boss.getFallingObjects()) {
                            double stoneCenterX = stone.getX() + stone.getImageView().getFitWidth() / 2;
                            double stoneCenterY = stone.getY() + stone.getImageView().getFitHeight() / 2;

                            double playerStoneHorizontalTolerance = 20.0;
                            double playerStoneVerticalTolerance = 30.0;
                            boolean playerStoneCollision = Math.abs(playerCenterX - stoneCenterX) <= playerStoneHorizontalTolerance &&
                                    Math.abs(playerCenterY - stoneCenterY) <= playerStoneVerticalTolerance;

                            if (debugProjektil) {
                                System.out.println("Kollisionsprüfung Spieler-Stein: Spieler (x=" + playerCenterX + ", y=" + playerCenterY + "), Stein (x=" + stoneCenterX + ", y=" + stoneCenterY + ")");
                                System.out.println("Abstand: x=" + Math.abs(playerCenterX - stoneCenterX) + ", y=" + Math.abs(playerCenterY - stoneCenterY) + ", playerStoneCollision=" + playerStoneCollision + ", onGround=" + stone.isOnGround());
                            }

                            if (playerStoneCollision) {
                                if (!stone.isOnGround()) {
                                    // Schaden, wenn der Stein fällt, später eigene Variable in FallingObject
                                    score -= 150;
                                    scoreLabel.setText("Leben: " + score);
                                    if (debugMode) {
                                        System.out.println("Spieler von fallendem Stein getroffen! Leben: " + score);
                                    }
                                    if (score <= 0) {
                                        gameLoop.stop();
                                        Platform.runLater(() -> showTaskDialog(primaryStage));
                                        return;
                                    }
                                } else {
                                    // Bewegung blockieren, wenn der Stein auf dem Boden liegt
                                    if (velocityX > 0 && playerCenterX < stoneCenterX) {
                                        playerX = stone.getX() - playerImageView.getFitWidth();
                                        velocityX = 0;
                                        if (debugMode) {
                                            System.out.println("Spieler von links gegen Stein gestoßen. Bewegung gestoppt.");
                                        }
                                    } else if (velocityX < 0 && playerCenterX > stoneCenterX) {
                                        playerX = stone.getX() + stone.getImageView().getFitWidth();
                                        velocityX = 0;
                                        if (debugMode) {
                                            System.out.println("Spieler von rechts gegen Stein gestoßen. Bewegung gestoppt.");
                                        }
                                    }
                                }
                            }
                        }

                        // Kollision mit Boss-Projektilen
                        for (PumpkinProjectile pumpkin : new ArrayList<>(boss.getProjectiles())) {
                            if (playerImageView.getBoundsInParent().intersects(pumpkin.getImageView().getBoundsInParent())) {
                                score -= 100;
                                if (score < 0) score = 0;
                                scoreLabel.setText("Punkte: " + score);
                                boss.getProjectiles().remove(pumpkin);
                                gamePane.getChildren().remove(pumpkin.getImageView());
                            }
                        }
                    } else if (bossDefeated) {
                        long currentTime = System.nanoTime();
                        if (currentTime - bossDefeatTime <= TUNNEL_ANIMATION_DURATION) {
                            long elapsedNanos = currentTime - bossDefeatTime;
                            boolean isVisible = (elapsedNanos / 500_000L) % 2 == 0;
                            if (tunnelLeft != null) tunnelLeft.setVisible(isVisible);
                            if (tunnelRight != null) tunnelRight.setVisible(isVisible);
                            if (levelText != null) levelText.setVisible(isVisible);
                        } else {
                            if (tunnelLeft != null) tunnelLeft.setVisible(true);
                            if (tunnelRight != null) tunnelRight.setVisible(true);
                            if (levelText != null) levelText.setVisible(true);
                            bossDefeated = false; // Animation beenden
                        }
                    }

                    // Gegner aktualisieren
                    for (Enemy enemy : enemies) {
                        enemy.updatePosition(platforms, gamePane, enemyProjectiles, playerX, playerY);
                    }

                    boolean onPlatform = false;

                    // Kollision mit Wänden
                    for (Wall wall : levels[currentLevel].getWalls()) {
                        double wallLeft = wall.getX();
                        double wallRight = wall.getX() + wall.getWidth();
                        double wallTop = wall.getY();
                        double wallBottom = wall.getY() + wall.getHeight();

                        double playerBottom = playerY + PLAYER_HEIGHT;
                        double playerTop = playerY;
                        double playerLeft = playerX;
                        double playerRight = playerX + PLAYER_WIDTH;

                        if (debugKollisionWall) {
                            System.out.println("Wandprüfung: wallLeft=" + wallLeft + ", wallRight=" + wallRight + ", wallTop=" + wallTop + ", wallBottom=" + wallBottom);
                            System.out.println("Spieler: playerLeft=" + playerLeft + ", playerRight=" + playerRight + ", playerTop=" + playerTop + ", playerBottom=" + playerBottom);
                        }

                        // Seitliche Kollision
                        if (playerBottom > wallTop && playerTop < wallBottom) {
                            if (velocityX > 0 && playerRight >= wallLeft && oldPlayerX + PLAYER_WIDTH <= wallLeft + MOVE_SPEED) {
                                playerX = wallLeft - PLAYER_WIDTH;
                                velocityX = 0;
                                if (debugKollisionWall) {
                                    System.out.println("Kollision mit Wand (rechts) bei x=" + playerX);
                                }
                            } else if (velocityX < 0 && playerLeft <= wallRight && oldPlayerX >= wallRight - MOVE_SPEED) {
                                playerX = wallRight;
                                velocityX = 0;
                                if (debugKollisionWall) {
                                    System.out.println("Kollision mit Wand (links) bei x=" + playerX);
                                }
                            }
                        }

                        // Obere Kollision
                        if (playerRight > wallLeft && playerLeft < wallRight && playerBottom >= wallTop && oldPlayerY + PLAYER_HEIGHT <= wallTop && velocityY > 0) {
                            playerY = wallTop - PLAYER_HEIGHT;
                            velocityY = 0;
                            jumpCount = 0;
                            onPlatform = true;
                            if (debugKollisionWall) {
                                System.out.println("Kollision mit Wand (oben) bei y=" + playerY);
                            }
                        }

                        // Untere Kollision
                        if (playerRight > wallLeft && playerLeft < wallRight && playerTop <= wallBottom && oldPlayerY >= wallBottom && velocityY < 0) {
                            playerY = wallBottom;
                            velocityY = 0;
                            if (debugKollisionWall) {
                                System.out.println("Kollision mit Wand (unten) bei y=" + playerY);
                            }
                        }
                    }

                    for (BreakablePlatform platform : platforms) {
                        if (platform.isDisable()) continue;
                        double playerBottom = playerY + PLAYER_HEIGHT;
                        double playerTop = playerY;
                        double playerLeft = playerX;
                        double playerRight = playerX + PLAYER_WIDTH;
                        double platformTop = platform.getY();
                        double platformBottom = platform.getY() + platform.getHeight();
                        double platformLeft = platform.getX();
                        double platformRight = platform.getX() + platform.getWidth();

                        if (playerBottom >= platformTop && oldPlayerY + PLAYER_HEIGHT <= platformTop &&
                                playerRight > platformLeft && playerLeft < platformRight &&
                                velocityY > 0) {
                            playerY = platformTop - PLAYER_HEIGHT;
                            velocityY = 0;
                            jumpCount = 0;
                            onPlatform = true;
                            if (platform.isBreakable()) {
                                platform.startFallTimer();
                                if (debugPlattform) {
                                    System.out.println("Timer gestartet für bruchbare Plattform bei x=" + platform.getX() + ", y=" + platform.getY());
                                }
                            }
                            if (debugPlattform) {
                                System.out.println("Kollision mit Plattform, neue y-Position: " + playerY + ", bruchbar: " + platform.isBreakable());
                            }
                            break;
                        } else if (playerTop <= platformBottom && oldPlayerY >= platformBottom &&
                                playerRight > platformLeft && playerLeft < platformRight &&
                                velocityY < 0) {
                            playerY = platformBottom;
                            velocityY = 0;
                            if (debugMode) {
                                System.out.println("Kollision mit Plattform (oben), neue y-Position: " + playerY);
                            }
                        } else if (playerBottom > platformTop && playerTop < platformBottom) {
                            if (velocityX > 0 && oldPlayerX + PLAYER_WIDTH <= platformLeft && playerRight >= platformLeft) {
                                playerX = platformLeft - PLAYER_WIDTH;
                                velocityX = 0;
                                if (debugMode) {
                                    System.out.println("Seitliche Kollision (rechts) bei x=" + playerX);
                                }
                            } else if (velocityX < 0 && oldPlayerX >= platformRight && playerLeft <= platformRight) {
                                playerX = platformRight;
                                velocityX = 0;
                                if (debugMode) {
                                    System.out.println("Seitliche Kollision (links) bei x=" + playerX);
                                }
                            }
                        }
                    }

                    boolean onGround = false;
                    int tileWidth = (int) (groundTileImage != null ? groundTileImage.getWidth() : 81);
                    double groundSurfaceY = 460 + 1;
                    for (int i = 0; i < groundTiles.size(); i++) {
                        ImageView groundTile = groundTiles.get(i);
                        double tileX = groundTile.getX();
                        double tileY = groundTile.getY();
                        double playerBottom = playerY + PLAYER_HEIGHT;
                        double playerLeft = playerX;
                        double playerRight = playerX + PLAYER_WIDTH;
                        if (debugKollisionGround) {
                            System.out.println("Kollisionsprüfung mit Boden bei x=" + tileX + ", y=" + tileY + ", playerBottom=" + playerBottom + ", oldPlayerY=" + oldPlayerY + ", newPlayerY=" + playerY + ", velocityY=" + velocityY);
                        }
                        boolean isHole = levels[currentLevel].getHolePositions().contains(i);
                        if (!isHole && playerBottom >= groundSurfaceY - 1 && oldPlayerY + PLAYER_HEIGHT > groundSurfaceY &&
                                playerX + PLAYER_WIDTH > tileX && playerX < tileX + tileWidth &&
                                velocityY >= 0) {
                            playerY = groundSurfaceY - PLAYER_HEIGHT;
                            velocityY = 0;
                            jumpCount = 0;
                            onGround = true;
                            if (debugKollisionGround) {
                                System.out.println("Kollision mit Boden, neue y-Position: " + playerY);
                            }
                            break;
                        }
                    }

                    for (Enemy enemy : enemies) {
                        if (debugKollisionBox) {
                            System.out.println("Spieler-Bounding-Box: minX=" + playerImageView.getBoundsInParent().getMinX() +
                                    ", minY=" + playerImageView.getBoundsInParent().getMinY() +
                                    ", maxX=" + playerImageView.getBoundsInParent().getMaxX() +
                                    ", maxY=" + playerImageView.getBoundsInParent().getMaxY());
                            System.out.println("Gegner-Bounding-Box: minX=" + enemy.getBoundsInParent().getMinX() +
                                    ", minY=" + enemy.getBoundsInParent().getMinY() +
                                    ", maxX=" + enemy.getBoundsInParent().getMaxX() +
                                    ", maxY=" + enemy.getBoundsInParent().getMaxY());
                        }
                        if (playerImageView.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                            double playerCenterX = playerX + (PLAYER_WIDTH / 2);
                            double playerCenterY = playerY + (PLAYER_HEIGHT / 2);
                            double enemyCenterX = enemy.getImageView().getX() + (enemy.getImageView().getFitWidth() / 2);
                            double enemyCenterY = enemy.getImageView().getY() + (enemy.getImageView().getFitHeight() / 2);
                            double distance = Math.sqrt(Math.pow(playerCenterX - enemyCenterX, 2) + Math.pow(playerCenterY - enemyCenterY, 2));
                            double minDistance = (PLAYER_WIDTH + enemy.getImageView().getFitWidth()) / 4;

                            if (distance < minDistance) {
                                if (debugMode) {
                                    System.out.println("Tatsächliche Kollision mit Gegner! Game Over! Distance=" + distance + ", minDistance=" + minDistance);
                                }
                                gameLoop.stop();
                                Platform.runLater(() -> showTaskDialog(primaryStage));
                                return;
                            }
                        }
                    }

                    List<Diamond> collectedDiamonds = new ArrayList<>();
                    for (Diamond diamond : diamonds) {
                        if (playerImageView.getBoundsInParent().intersects(diamond.getCollisionBox().getBoundsInParent())) {
                            collectedDiamonds.add(diamond);
                            score += 10;
                            isScore = true;
                            scoreLabel.setText("Punkte: " + score);
                            if (debugMode) {
                                System.out.println("Raute gesammelt, neuer Score: " + score);
                            }
                        }
                    }
                    diamonds.removeAll(collectedDiamonds);
                    gamePane.getChildren().removeAll(collectedDiamonds);

                    if (!isTransitioning && (!onGround && !onPlatform && playerY > 462)) {
                        System.out.println("Game Over! Spielerposition: x=" + playerX + ", y=" + playerY);
                        gameLoop.stop();
                        Platform.runLater(() -> showTaskDialog(primaryStage));
                        return;
                    }

                    if (isScore && score <= 0) {
                        System.out.println("Game Over! Score: " + score);
                        gameLoop.stop();
                        Platform.runLater(() -> showGameOver(primaryStage));
                        return;
                    }

                    updatePlayerImage();
                }
            };
            gameLoop.start();
        } else {
            gameLoop.start();
        }

        gameScene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode().toString());
            if (event.getCode().toString().equals("ESCAPE")) {
                if (!isPaused) {
                    pauseGame(primaryStage);
                } else {
                    resumeGame();
                }
            }
        });
        gameScene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode().toString()));

        gameScene.setOnMouseMoved(event -> {
            if (startLevel > 0 || startDesign > 0) {
                int mouseX = (int) event.getX();
                int mouseY = (int) event.getY();
                mousePositionLabel.setText("Maus: (" + mouseX + ", " + mouseY + ")");
            }
        });

        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Eumels Höllisch Süßes Abenteuer");

        if (backgroundView != null) {
            backgroundView.toBack();
            if (debugMode) {
                System.out.println("Hintergrund (backgroundView) nach hinten verschoben.");
            }
        }

        fallingLeaves.start();

        System.out.println("Bodenstücke im Level: " + groundSegments.size());
        System.out.println("Plattformen geladen: " + platforms.size());
    }

    private void updatePlayerImage() {
        if (velocityX > 0) {
            playerImageView.setImage(playerMoveImage);
            playerImageView.setScaleX(1);
            lastDirection = 1;
        } else if (velocityX < 0) {
            playerImageView.setImage(playerMoveLeftImage);
            playerImageView.setScaleX(1);
            lastDirection = -1;
        } else {
            if (lastDirection > 0) {
                playerImageView.setImage(playerStandImage);
                playerImageView.setScaleX(1);
            } else {
                playerImageView.setImage(playerStandLeftImage);
                playerImageView.setScaleX(1);
            }
        }
        playerImageView.setX(playerX);
        playerImageView.setY(playerY);
    }

    public static void showMessage(String message) {
        Platform.runLater(() -> {
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: '" + customFont.getName() + "';");
            StackPane root = (StackPane) primaryStage.getScene().getRoot();
            root.getChildren().add(messageLabel);
            messageLabel.setLayoutX(300);
            messageLabel.setLayoutY(200);

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> root.getChildren().remove(messageLabel));
            pause.play();
        });
    }

    private void shoot(long now) {
        double shootHeight = playerY + PLAYER_HEIGHT / 2;
        double startX = facingRight ? playerX + PLAYER_WIDTH : playerX;
        int currentDamage = (int) (playerDamage * damageMultiplier);
        Projectile projectile = new Projectile(startX, shootHeight, facingRight, null, currentDamage, false, powerupLevel);
        projectiles.add(projectile);
        gamePane.getChildren().add(projectile.getImageView());
        if (debugMode) {
            System.out.println("Schuss abgefeuert bei x=" + startX + ", y=" + shootHeight + ", Richtung: " + (facingRight ? "rechts" : "links") + ", Schaden: " + projectile.getDamage() + ", Powerup-Level: " + powerupLevel);
        }
    }

    private double findSafeYPosition() {
        int tileWidth = (int) (groundTileImage != null ? groundTileImage.getWidth() : 81);
        double groundSurfaceY = 460;
        for (ImageView groundTile : groundTiles) {
            double tileX = groundTile.getX();
            double tileY = groundTile.getY();
            if (playerX + PLAYER_WIDTH > tileX && playerX < tileX + tileWidth && !levels[currentLevel].getHolePositions().contains((int) (tileX / tileWidth))) {
                return groundSurfaceY - PLAYER_HEIGHT;
            }
        }
        for (BreakablePlatform platform : platforms) {
            if (!platform.isDisable()) {
                double platformTop = platform.getY();
                double platformLeft = platform.getX();
                double platformRight = platform.getX() + platform.getWidth();
                if (playerX + PLAYER_WIDTH > platformLeft && playerX < platformRight) {
                    return platformTop - PLAYER_HEIGHT;
                }
            }
        }
        return 480 - PLAYER_HEIGHT;
    }

    private void activatePowerup(Powerup.PowerupType powerupType) {
        switch (powerupType) {
            case DAMAGE_BOOST:
                int previousDamage = playerDamage;
                powerupLevel = (powerupLevel + 1) % 3;
                playerDamage = powerupLevel == 0 ? 100 : (powerupLevel == 1 ? 150 : 200);
                if (debugMode) {
                    System.out.println("DAMAGE_BOOST aktiviert! Neues Level: " + powerupLevel + ", Schaden: " + playerDamage + " (vorher: " + previousDamage + ")");
                }
                break;
            case SPEED_BOOST:
                if (activeSpeedBoosts >= MAX_SPEED_BOOSTS) {
                    if (debugMode) {
                        System.out.println("Maximale Anzahl an SPEED_BOOSTs erreicht (" + MAX_SPEED_BOOSTS + ")");
                    }
                    return;
                }

                activeSpeedBoosts++;
                double previousSpeed = MOVE_SPEED;
                MOVE_SPEED = Math.min(MAX_SPEED, MOVE_SPEED * SPEED_BOOST_MULTIPLIER);
                if (debugMode) {
                    System.out.println("SPEED_BOOST aktiviert! Neue Geschwindigkeit: " + MOVE_SPEED + " (vorher: " + previousSpeed + "), Aktive Boosts: " + activeSpeedBoosts);
                }

                PauseTransition speedPause = new PauseTransition(Duration.seconds(30));
                speedPause.setOnFinished(event -> {
                    activeSpeedBoosts--;
                    MOVE_SPEED = Math.max(1.0, MOVE_SPEED / SPEED_BOOST_MULTIPLIER);
                    if (debugMode) {
                        System.out.println("SPEED_BOOST abgelaufen! Geschwindigkeit zurückgesetzt auf: " + MOVE_SPEED + ", Aktive Boosts: " + activeSpeedBoosts);
                    }
                });
                speedPause.play();
                break;
        }
    }

    private void pauseGame(Stage primaryStage) {
        buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + customFont.getName() + "';";
        if (!isPaused) {
            isPaused = true;
            gameLoop.stop();
            if (fallingLeaves != null) {
                fallingLeaves.stop();
            }

            pauseOverlay = new StackPane();
            pauseOverlay.setStyle("-fx-background-color: rgba(51, 51, 51, 0.8);");
            pauseOverlay.setPrefSize(800, 600);

            MainMenuAnimation pauseAnimation = new MainMenuAnimation(pauseOverlay);
            pauseAnimation.play();

            StackPane pausePane = new StackPane();
            pausePane.setStyle("-fx-alignment: center; -fx-background-color: rgba(51, 51, 51, 0.8);");
            pausePane.setPrefWidth(300);
            pausePane.setPrefHeight(200);

            VBox menuBox = new VBox(20);
            menuBox.setStyle("-fx-alignment: center;");

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
            rootPane.getChildren().add(pauseOverlay);


            resumeButton.setOnAction(e -> {
                pauseAnimation.stop();
                resumeGame();
            });
            mainMenuButton.setOnAction(e -> {
                pauseAnimation.stop();
                rootPane.getChildren().remove(pauseOverlay);
                returnToMainMenu(primaryStage);
            });
            settingsButton.setOnAction(e -> new SettingsScreen(this, audioManager, dbDAO).show());
            exitButton.setOnAction(e -> {
                pauseAnimation.stop();
                dbDAO.close();
                primaryStage.close();
            });

            if (JumpAndRun.debugMode) {
                System.out.println("Pausemenü als Overlay zur rootPane hinzugefügt. Größe von rootPane: " + rootPane.getChildren().size());
            }
        }
    }

    private void resumeGame() {
        isPaused = false;
        if (pauseOverlay != null && rootPane.getChildren().contains(pauseOverlay)) {
            rootPane.getChildren().remove(pauseOverlay);
            pauseOverlay = null;
            if (debugMode) {
                System.out.println("Pausenmenü entfernt. Größe von rootPane: " + rootPane.getChildren().size());
            }
        } else {
            if (debugMode) {
                System.out.println("Kein Pausenmenü zum Entfernen gefunden.");
            }
        }
        gameLoop.start();
        if (fallingLeaves != null) {
            fallingLeaves.start();
        }
    }

    private void showTaskDialog(Stage primaryStage) {
        buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + customFont.getName() + "';";
        if (fallingLeaves != null) {
            fallingLeaves.stop();
        }

        Task task = dbDAO.getRandomTask();
        if (task == null) {
            showGameOver(primaryStage);
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Wiederbelebung");
        dialog.setHeaderText(null);

        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/player/bonbon.png")).toExternalForm()));

        Node titleBar = dialog.getDialogPane().lookup(".header-panel");
        if (titleBar != null) {
            titleBar.setVisible(false);
            titleBar.setManaged(false);
        }

        dialog.getDialogPane().setStyle("-fx-background-color: rgba(51, 51, 51, 0.8);");
        dialog.getDialogPane().setPrefSize(600, 400);

        Label questionLabel = new Label(task.getQuestion());
        questionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: '" + customFont.getName() + "';");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(550);
        TextField answerField = new TextField();
        answerField.setStyle("-fx-text-fill: white; -fx-font-family: '" + customFont.getName() + "'; -fx-background-color: rgba(0, 0, 0, 0.5);");
        answerField.setPrefWidth(200);
        VBox content = new VBox(15, questionLabel, answerField);
        content.setStyle("-fx-alignment: center;");
        dialog.getDialogPane().setContent(content);

        ButtonType submitButton = new ButtonType("Einreichen", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, cancelButton);

        dialog.getDialogPane().lookupButton(submitButton).setStyle(buttonStyle);
        dialog.getDialogPane().lookupButton(cancelButton).setStyle(buttonStyle);

        dialog.setResultConverter(button -> {
            if (button == submitButton) {
                return answerField.getText();
            }
            return null;
        });

        // Dialog anzeigen und verarbeiten
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String normalizedAnswer = result.get().trim().replace(",", ".");
            if (normalizedAnswer.equals(task.getAnswer())) {
                revivePlayer(primaryStage);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Falsche Antwort");
                alert.setHeaderText("Leider falsch!");
                alert.setContentText("Deine Antwort war '" + result.get() + "', aber es sollte '" + task.getAnswer() + "' sein. Spiel vorbei!");
                alert.showAndWait();
                handleGameEnd(primaryStage);
            }
        } else {
            handleGameEnd(primaryStage);
        }
    }

    private void showHighscores(Stage primaryStage) {
        buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + customFont.getName() + "';";
        if (fallingLeaves != null) {
            fallingLeaves.stop();
        }

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(51, 51, 51, 0.8);");

        MainMenuAnimation animation = new MainMenuAnimation(root);
        animation.play();

        VBox highscorePane = new VBox(20);
        highscorePane.setStyle("-fx-alignment: center;");

        Label highscoreLabel = new Label("Highscores");
        highscoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-family: '" + customFont.getName() + "';");

        StackPane listPane = new StackPane();
        listPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        listPane.setMaxWidth(300);
        listPane.setPrefHeight(400);

        ListView<String> highscoreList = new ListView<>();
        List<Highscore> highscores = dbDAO.getHighscores(10);

        // Debug-Ausgabe
        if (debugMode) {
            System.out.println("Geladene Highscores: " + highscores.size());
            for (Highscore highscore : highscores) {
                System.out.println("Highscore: " + highscore.getName() + " - " + highscore.getScore());
            }
        }

        if (highscores.size() < 1) {
            highscoreList.getItems().add("Kein Eintrag vorhanden");
        } else {
            for (int i = 0; i < highscores.size(); i++) {
                Highscore highscore = highscores.get(i);
                highscoreList.getItems().add(String.format("%d. %s - %d", i + 1, highscore.getName(), highscore.getScore()));
            }
        }
        highscoreList.setStyle("-fx-background-color: rgba(0, 0, 0, 0.0); -fx-text-fill: white; -fx-font-family: '" + customFont.getName() + "'; -fx-font-size: 16px; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        highscoreList.setMaxWidth(300);
        highscoreList.setPrefHeight(400);

        highscoreList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-alignment: center; -fx-background-color: transparent; -fx-font-family: '" + customFont.getName() + "'; -fx-font-size: 16px;");
                }
            }
        });

        listPane.getChildren().add(highscoreList);

        Button backButton = new Button("Zurück");
        backButton.setStyle(buttonStyle);
        backButton.setOnAction(e -> returnToMainMenu(primaryStage));

        highscorePane.getChildren().addAll(highscoreLabel, listPane, backButton);
        root.getChildren().add(highscorePane);

        Scene highscoreScene = new Scene(root, 800, 600);
        primaryStage.setScene(highscoreScene);
    }

    private void showHighscoreDialog(Stage primaryStage) {
        buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + customFont.getName() + "';";
        if (fallingLeaves != null) {
            fallingLeaves.stop();
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Highscore");
        dialog.setHeaderText("Herzlichen Glückwunsch! Du hast das Spiel geschafft!\nGib deinen Namen ein:");
        dialog.setContentText("Name:");

        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/player/bonbon.png")).toExternalForm()));

        Node titleBar = dialog.getDialogPane().lookup(".header-panel");
        if (titleBar != null) {
            titleBar.setVisible(false);
            titleBar.setManaged(false);
        }

        dialog.getDialogPane().setStyle("-fx-background-color: rgba(51, 51, 51, 0.8);");
        dialog.getDialogPane().setPrefSize(350, 200);

        Label headerLabel = (Label) dialog.getDialogPane().lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: '" + customFont.getName() + "';");
            headerLabel.setWrapText(true);
            headerLabel.setMaxWidth(550);
        }
        Label contentLabel = (Label) dialog.getDialogPane().lookup(".content .label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: '" + customFont.getName() + "';");
        }
        TextField textField = dialog.getEditor();
        textField.setStyle("-fx-text-fill: white; -fx-font-family: '" + customFont.getName() + "'; -fx-background-color: rgba(0, 0, 0, 0.5);");
        textField.setPrefWidth(200);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (okButton != null) okButton.setStyle(buttonStyle);
        if (cancelButton != null) cancelButton.setStyle(buttonStyle);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().trim();
            if (!name.isEmpty()) {
                dbDAO.saveHighscore(name, score);
                showHighscores(primaryStage);
            }
        } else {
            returnToMainMenu(primaryStage);
        }
    }

    private void revivePlayer(Stage primaryStage) {
        playerX = 50;
        playerY = 480;
        velocityX = 0;
        velocityY = 0;
        jumpCount = 0;

        AnimationTimer blinkTimer = new AnimationTimer() {
            private int blinkCount = 0;
            private final int MAX_BLINKS = 6;
            private boolean visible = true;

            @Override
            public void handle(long now) {
                if (blinkCount < MAX_BLINKS) {
                    visible = !visible;
                    playerImageView.setVisible(visible);
                    blinkCount++;
                } else {
                    playerImageView.setVisible(true);
                    this.stop();
                    startGame(primaryStage, currentLevel, currentDesignIndex);
                }
            }
        };
        blinkTimer.start();
    }

    private void handleGameEnd(Stage primaryStage) {
        boolean gameCompleted = currentLevel >= MAX_LEVELS - 1 && !levels[currentLevel].hasNextDesign();

        List<Highscore> highscores = dbDAO.getHighscores(10);
        boolean isHighscoreWorthy = highscores.size() < 10 || highscores.stream().anyMatch(hs -> score > hs.getScore());

        if (gameCompleted || isHighscoreWorthy) {
            if (score > 0){
                showHighscoreDialog(primaryStage);
            }
        } else {
            returnToMainMenu(primaryStage);
        }
    }

    private void returnToMainMenu(Stage primaryStage) {
        if (fallingLeaves != null) {
            fallingLeaves.stop();
        }
        showMainMenu(primaryStage);
    }

    private void showGameOver(Stage primaryStage) {
        buttonStyle = "-fx-font-size: 20px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.5); -fx-font-family: '" + customFont.getName() + "';";
        if (fallingLeaves != null) {
            fallingLeaves.stop();
        }
        VBox gameOverPane = new VBox(20);
        gameOverPane.setStyle("-fx-alignment: center; -fx-background-color: #333333;");
        Label gameOverLabel = new Label("Game Over!\nDein eumel ist leider gestorben\nPunkte: " + score);
        gameOverLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        Button startButton = new Button("Starten");
        Button highscoreButton = new Button("Highscores");
        Button exitButton = new Button("Verlassen");

        startButton.setStyle(buttonStyle);
        highscoreButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        gameOverPane.getChildren().addAll(gameOverLabel, startButton, highscoreButton, exitButton);

        Scene gameOverScene = new Scene(gameOverPane, 800, 600);
        primaryStage.setScene(gameOverScene);

        startButton.setOnAction(e -> {
            gameLoop.stop();
            debugMode = false;
            startGame(primaryStage, startLevel, startDesign);
        });
        highscoreButton.setOnAction(e -> showHighscores(primaryStage));
        exitButton.setOnAction(e -> {
            dbDAO.close();
            primaryStage.close();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}