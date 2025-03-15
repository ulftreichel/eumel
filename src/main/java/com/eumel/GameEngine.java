package com.eumel;

import com.eumel.level.*;
import com.eumel.level.design.*;
import com.eumel.opponent.*;
import com.eumel.ui.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;

import java.util.*;

public class GameEngine {
    public final JumpAndRun jumpAndRun;
    private final Stage primaryStage;
    private final Pane gamePane;
    private Scene previousScene;
    private final Set<String> pressedKeys = new HashSet<>();
    private AnimationTimer gameLoop;

    // Spieler-Status
    private double playerX = 50;
    private double playerY = 480;
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean facingRight = true;
    private boolean canShoot = false;
    private boolean bossDefeated = false;
    private boolean isPaused = false;
    private boolean isTransitioning = false;
    private boolean isScore = false;
    private boolean jumpPressedLastFrame = false;
    private int jumpCount = 0;
    private int score = 0;
    private int playerDamage = 100;
    private double damageMultiplier = 1.0;
    private int powerupLevel = 0;
    private int lastDirection = 1;
    private int activeSpeedBoosts = 0;
    private long lastShotTime = 0;
    private long bossDefeatTime = 0;
    private long lastBossHealthUpdateTime = 0;
    private int lastBossHealth = -1;

    // Spielkonstanten
    private static final double PLAYER_WIDTH = 32;
    private static final double PLAYER_HEIGHT = 64;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_STRENGTH = -8;
    private static final double DOUBLE_JUMP_STRENGTH = -11;
    private static final long SHOOT_COOLDOWN = 500_000_000;
    private static final long TUNNEL_ANIMATION_DURATION = 5_000_000_000L;
    private static final double MAX_SPEED = 3.0;
    private static final int MAX_SPEED_BOOSTS = 3;
    private static final double SPEED_BOOST_MULTIPLIER = 1.5;
    public static final double GROUND_LEVEL = 475;
    private double MOVE_SPEED = 2.0;

    // Spielobjekte
    private ImageView playerImageView;
    private Image playerStandImage;
    private Image playerMoveImage;
    private Image playerStandLeftImage;
    private Image playerMoveLeftImage;
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
    private Image groundTileImage;
    private Image holeTileImage;
    private ImageView tunnelLeft;
    private ImageView tunnelRight;
    private Text levelText;
    private Rectangle bossHealthBar;
    private double bossHealthBarInitialX = 300;
    private LevelDesign[] levels;
    private int currentLevel = 0;
    private int currentDesignIndex = 0;
    private final int MAX_LEVELS = 10;

    public GameEngine(JumpAndRun jumpAndRun, Stage primaryStage, Pane gamePane) {
        this.jumpAndRun = jumpAndRun;
        this.primaryStage = primaryStage;
        this.gamePane = gamePane;
    }

    public void initializeGame(int startLevel, int startDesign, Scene gameScene) {
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

        // Audio starten
        jumpAndRun.getAudioManager().startGameAudio(boss != null && boss.isActive());
        if (boss != null) {
            gamePane.getChildren().remove(boss.getImageView());
            boss = null;
        }

        // Spielerbilder laden (später in ResourceManager auslagern)
        try {
            playerStandImage = new Image(getClass().getResource("/images/player/player1stand.png").toExternalForm());
            playerMoveImage = new Image(getClass().getResource("/images/player/player1move.png").toExternalForm());
            playerStandLeftImage = new Image(getClass().getResource("/images/player/player1standleft.png").toExternalForm());
            playerMoveLeftImage = new Image(getClass().getResource("/images/player/player1moveleft.png").toExternalForm());
            if (JumpAndRun.debugMode) {
                System.out.println("Spielerbilder geladen: player1stand.png (" + playerStandImage.getWidth() + "x" + playerStandImage.getHeight() + "), player1move.png (" + playerMoveImage.getWidth() + "x" + playerMoveImage.getHeight() + "), player1standleft.png (" + playerStandLeftImage.getWidth() + "x" + playerStandLeftImage.getHeight() + "), player1moveleft.png (" + playerMoveLeftImage.getWidth() + "x" + playerMoveLeftImage.getHeight() + ")");
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Spielerbilder: " + e.getMessage());
            return;
        }

        // Bodenbilder laden (später in ResourceManager auslagern)
        try {
            groundTileImage = new Image(getClass().getResource("/images/level/ground_tile.png").toExternalForm());
            holeTileImage = new Image(getClass().getResource("/images/level/hole_tile.png").toExternalForm());
            if (JumpAndRun.debugMode) {
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

        playerImageView = new ImageView(playerStandImage);
        playerImageView.setFitWidth(PLAYER_WIDTH);
        playerImageView.setFitHeight(PLAYER_HEIGHT);
        playerImageView.setX(playerX);
        playerImageView.setY(playerY);
        gamePane.getChildren().add(playerImageView);

        // Level-Initialisierung (wird später in LevelManager ausgelagert)
        this.currentLevel = startLevel;
        this.currentDesignIndex = startDesign;
        this.previousScene = gameScene;
        levels = new LevelDesign[MAX_LEVELS];

        if (JumpAndRun.debugMode) {
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

        initializeGameLoop();
    }

    private void initializeGameLoop() {
        gameLoop = new AnimationTimer() {
            private double lastLoggedY = playerY;
            private long lastUpdateTime = 0;

            @Override
            public void handle(long now) {
                if (isPaused) return;

                if (Math.abs(playerY - lastLoggedY) >= 1.0) {
                    if (JumpAndRun.debugJump) {
                        System.out.println("Spielerposition: x=" + playerX + ", y=" + playerY + ", velocityY=" + velocityY);
                    }
                    lastLoggedY = playerY;
                }

                velocityX = 0;
                // Bewegung nach links
                if (pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("move_left", true)) || pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("move_left", false))) {
                    velocityX = -MOVE_SPEED;
                    facingRight = false;
                    playerX += velocityX;
                    playerImageView.setX(playerX);
                } else if (pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("move_right", true)) || pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("move_right", false))) {
                    velocityX = MOVE_SPEED;
                    facingRight = true;
                    playerX += velocityX;
                    playerImageView.setX(playerX);
                }

                if (canShoot && (pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("shoot", true)) || pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("shoot", false))) && now - lastShotTime >= SHOOT_COOLDOWN) {
                    shoot(now);
                    lastShotTime = now;
                }

                boolean jumpPressed = pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("jump", true)) || pressedKeys.contains(jumpAndRun.getDbDAO().getKeyBinding("jump", false));
                if (jumpPressed && !jumpPressedLastFrame) {
                    if (jumpCount < 2) {
                        velocityY = jumpCount == 0 ? JUMP_STRENGTH : DOUBLE_JUMP_STRENGTH;
                        jumpCount++;
                        if (JumpAndRun.debugJump) {
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
                        loadLevel(gamePane, levels[currentLevel], JumpAndRun.debugMode);
                        playerX = 0;
                        playerY = findSafeYPosition();
                        velocityX = 0;
                        velocityY = 0;
                        jumpCount = 0;
                        if (JumpAndRun.debugMode) {
                            System.out.println("Neues Design geladen: Level " + (currentLevel + 1) + ", Design " + (currentDesignIndex + 1) + ", neue Position: x=" + playerX + ", y=" + playerY);
                        }
                    } else if (currentLevel < MAX_LEVELS - 1) {
                        currentLevel++;
                        currentDesignIndex = 0;
                        levels[currentLevel].resetDesign();
                        loadLevel(gamePane, levels[currentLevel], JumpAndRun.debugMode);
                        playerX = 0;
                        playerY = findSafeYPosition();
                        velocityX = 0;
                        velocityY = 0;
                        jumpCount = 0;
                        if (JumpAndRun.debugMode) {
                            System.out.println("Neues Level geladen: Level " + (currentLevel + 1) + ", Design 1, neue Position: x=" + playerX + ", y=" + playerY);
                        }
                    } else {
                        if (score > 0) {
                            Platform.runLater(() -> new HighscoreDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
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

                            if (JumpAndRun.debugProjektil) {
                                System.out.println("Kollisionsprüfung: Projektil (x=" + projectileCenterX + ", y=" + projectileCenterY + "), Gegner (x=" + enemyCenterX + ", y=" + enemyCenterY + ")");
                                System.out.println("Abstand: x=" + Math.abs(projectileCenterX - enemyCenterX) + ", y=" + Math.abs(projectileCenterY - enemyCenterY) + ", centerCollision=" + centerCollision);
                            }

                            if (centerCollision) {
                                enemy.takeDamage(projectile.getDamage());
                                gamePane.getChildren().remove(projectile.getImageView());
                                projectile.setInactive();
                                projectileIterator.remove();
                                score += 20;
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                isScore = true;
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Gegner getroffen! Lebenspunkte: " + enemy.getHealth() + ", Schaden: " + projectile.getDamage() + ", Neuer Score: " + score);
                                }
                                if (!enemy.isActive()) {
                                    gamePane.getChildren().remove(enemy.getImageView());
                                    enemyIterator.remove();
                                    score += 100;
                                    jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                    if (JumpAndRun.debugMode) {
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
                        if (JumpAndRun.debugMode) {
                            System.out.println("Gegner-Projektil entfernt bei x=" + enemyProjectile.getX() + ", y=" + enemyProjectile.getY());
                        }
                    } else {
                        double playerCenterX = playerX + PLAYER_WIDTH / 2;
                        double playerCenterY = playerY + PLAYER_HEIGHT / 2;
                        double projectileCenterX = enemyProjectile.getX() + enemyProjectile.getWidth() / 2;
                        double projectileCenterY = enemyProjectile.getY() + enemyProjectile.getHeight() / 2;
                        double tolerance = 10.0;
                        if (Math.abs(projectileCenterX - playerCenterX) <= tolerance &&
                                Math.abs(projectileCenterY - playerCenterY) <= tolerance) {
                            int enemyDamage = enemyProjectile.getDamage();
                            score -= enemyDamage;
                            jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                            gamePane.getChildren().remove(enemyProjectile.getImageView());
                            enemyProjectileIterator.remove();
                            if (JumpAndRun.debugMode) {
                                System.out.println("Spieler getroffen! Neuer Punktestand: " + score + ", Schaden: " + enemyDamage);
                            }
                            if (score < 0) {
                                gameLoop.stop();
                                Platform.runLater(() -> new GameOverDialog(jumpAndRun, GameEngine.this, primaryStage).show());
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
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                gamePane.getChildren().remove(p.getImageView());
                                miniProjIterator.remove();
                                if (score < 0) {
                                    gameLoop.stop();
                                    Platform.runLater(() -> new GameOverDialog(jumpAndRun, GameEngine.this, primaryStage).show());
                                    return;
                                }
                            }
                        }

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
                                    jumpAndRun.getScoreLabel().setText("Punkte: " + score);
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
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Schießen freigeschaltet! Powerup-Level: " + powerupLevel);
                                }
                            } else {
                                damageMultiplier *= 1.25;
                                powerupLevel = Math.min(powerupLevel + 1, 4);
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Powerup gesammelt: DAMAGE_BOOST, Schaden: " + (100 * damageMultiplier) + ", Powerup-Level: " + powerupLevel);
                                }
                            }
                        } else if (powerup.getType() == Powerup.PowerupType.SPEED_BOOST) {
                            MOVE_SPEED += 1;
                            if (JumpAndRun.debugMode) {
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

                    if (JumpAndRun.debugProjektil) {
                        System.out.println("Kollisionsprüfung Spieler-Schatztruhe: Spieler (x=" + playerCenterX + ", y=" + playerCenterY + "), Truhe (x=" + chestCenterX + ", y=" + chestCenterY + ")");
                        System.out.println("Abstand: x=" + Math.abs(playerCenterX - chestCenterX) + ", y=" + Math.abs(playerCenterY - chestCenterY) + ", playerChestCollision=" + playerChestCollision + ", isFull=" + chest.isFull());
                    }

                    if (playerChestCollision) {
                        jumpAndRun.getAudioManager().playEffect("/sound/chest_open.mp3");
                        if (chest.isFull()) {
                            jumpAndRun.getAudioManager().playEffect("/sound/chest_full.mp3");
                            if (chest.givesPowerup()) {
                                Powerup.PowerupType powerupType = chest.getPowerupType();
                                activatePowerup(powerupType);
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Schatztruhe geöffnet! Powerup erhalten: " + powerupType);
                                }
                            } else {
                                score += chest.getScoreReward();
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Schatztruhe geöffnet! Punkte erhalten: " + chest.getScoreReward() + ", Neuer Score: " + score);
                                }
                            }
                        } else {
                            jumpAndRun.getAudioManager().playEffect("/sound/chest_empty.mp3");
                            if (JumpAndRun.debugMode) {
                                System.out.println("Schatztruhe geöffnet! Truhe ist leer.");
                            }
                        }

                        chest.open();
                    }
                }

                // Boss-Logik
                if (boss != null && boss.isActive()) {
                    initializeBossHealthBar();
                    boss.update(playerX, playerY, facingRight, gamePane);
                    double healthRatio = (double) boss.getHealth() / 5000.0;
                    double newWidth = 400 * healthRatio;
                    if (bossHealthBar != null) {
                        bossHealthBar.setWidth(newWidth);
                        bossHealthBar.setX(bossHealthBarInitialX + (400 - newWidth));
                    } else {
                        if (JumpAndRun.debugMode && !jumpAndRun.hasLoggedBossHealthBarError()) {
                            System.err.println("bossHealthBar ist null, aber Boss-Level aktiv! Initialisiere notfallmäßig.");
                            jumpAndRun.setHasLoggedBossHealthBarError(true);
                            bossHealthBar = new Rectangle(bossHealthBarInitialX, 50, 400, 20);
                            bossHealthBar.setFill(Color.RED);
                            bossHealthBar.setStroke(Color.BLACK);
                            gamePane.getChildren().add(bossHealthBar);
                        }
                    }
                    if (JumpAndRun.debugMode) {
                        long currentTime = System.nanoTime();
                        if (currentTime - lastBossHealthUpdateTime >= 1_000_000_000L || lastBossHealth != boss.getHealth()) {
                            System.out.println("Boss-Gesundheit: " + boss.getHealth() + " / 5000");
                            lastBossHealthUpdateTime = currentTime;
                            lastBossHealth = boss.getHealth();
                        }
                    }

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

                        if (JumpAndRun.debugProjektil) {
                            System.out.println("Kollisionsprüfung Boss: Projektil (x=" + projectileCenterX + ", y=" + projectileCenterY + "), Boss (x=" + bossCenterX + ", y=" + bossCenterY + ")");
                            System.out.println("Abstand: x=" + Math.abs(projectileCenterX - bossCenterX) + ", y=" + Math.abs(projectileCenterY - bossCenterY) + ", centerCollision=" + centerCollision);
                        }

                        if (centerCollision) {
                            boss.takeDamage(p.getDamage());
                            gamePane.getChildren().remove(p.getImageView());
                            projIterator.remove();
                            if (JumpAndRun.debugMode) {
                                System.out.println("Boss getroffen! Lebenspunkte: " + boss.getHealth() + ", Schaden: " + p.getDamage());
                            }
                            if (!boss.isActive()) {
                                bossDefeated = true;
                                bossDefeatTime = System.nanoTime();
                                gamePane.getChildren().remove(boss.getImageView());
                                gamePane.getChildren().remove(bossHealthBar);
                                score += 1000;
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Boss besiegt! Neuer Score: " + score);
                                }
                            }
                        }
                    }

                    double playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                    double playerCenterY = playerY + playerImageView.getFitHeight() / 2;
                    double bossCenterX = boss.getImageView().getX() + boss.getImageView().getFitWidth() / 2;
                    double bossCenterY = boss.getImageView().getY() + boss.getImageView().getFitHeight() / 2;

                    double playerBossHorizontalTolerance = 40.0;
                    double playerBossVerticalTolerance = 90.0;
                    boolean playerBossCollision = Math.abs(playerCenterX - bossCenterX) <= playerBossHorizontalTolerance &&
                            Math.abs(playerCenterY - bossCenterY) <= playerBossVerticalTolerance;

                    if (JumpAndRun.debugProjektil) {
                        System.out.println("Kollisionsprüfung Spieler-Boss: Spieler (x=" + playerCenterX + ", y=" + playerCenterY + "), Boss (x=" + bossCenterX + ", y=" + bossCenterY + ")");
                        System.out.println("Abstand: x=" + Math.abs(playerCenterX - bossCenterX) + ", y=" + Math.abs(playerCenterY - bossCenterY) + ", playerBossCollision=" + playerBossCollision);
                    }

                    if (playerBossCollision) {
                        if (JumpAndRun.debugMode) {
                            System.out.println("Spieler kollidiert mit Boss! Schaden zugefügt.");
                        }
                        score -= 500;
                        jumpAndRun.getScoreLabel().setText("Leben: " + score);
                        if (score < 0) {
                            gameLoop.stop();
                            Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
                            return;
                        }
                        if (velocityX > 0 && playerCenterX < bossCenterX) {
                            playerX = boss.getImageView().getX() - playerImageView.getFitWidth();
                            velocityX = 0;
                        } else if (velocityX < 0 && playerCenterX > bossCenterX) {
                            playerX = boss.getImageView().getX() + boss.getImageView().getFitWidth();
                            velocityX = 0;
                        }
                    }

                    for (FallingObject stone : boss.getFallingObjects()) {
                        double stoneCenterX = stone.getX() + stone.getImageView().getFitWidth() / 2;
                        double stoneCenterY = stone.getY() + stone.getImageView().getFitHeight() / 2;

                        double playerStoneHorizontalTolerance = 20.0;
                        double playerStoneVerticalTolerance = 30.0;
                        boolean playerStoneCollision = Math.abs(playerCenterX - stoneCenterX) <= playerStoneHorizontalTolerance &&
                                Math.abs(playerCenterY - stoneCenterY) <= playerStoneVerticalTolerance;

                        if (JumpAndRun.debugProjektil) {
                            System.out.println("Kollisionsprüfung Spieler-Stein: Spieler (x=" + playerCenterX + ", y=" + playerCenterY + "), Stein (x=" + stoneCenterX + ", y=" + stoneCenterY + ")");
                            System.out.println("Abstand: x=" + Math.abs(playerCenterX - stoneCenterX) + ", y=" + Math.abs(playerCenterY - stoneCenterY) + ", playerStoneCollision=" + playerStoneCollision + ", onGround=" + stone.isOnGround());
                        }

                        if (playerStoneCollision) {
                            if (!stone.isOnGround()) {
                                score -= 150;
                                jumpAndRun.getScoreLabel().setText("Leben: " + score);
                                if (JumpAndRun.debugMode) {
                                    System.out.println("Spieler von fallendem Stein getroffen! Leben: " + score);
                                }
                                if (score <= 0) {
                                    gameLoop.stop();
                                    Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
                                    return;
                                }
                            } else {
                                if (velocityX > 0 && playerCenterX < stoneCenterX) {
                                    playerX = stone.getX() - playerImageView.getFitWidth();
                                    velocityX = 0;
                                    if (JumpAndRun.debugMode) {
                                        System.out.println("Spieler von links gegen Stein gestoßen. Bewegung gestoppt.");
                                    }
                                } else if (velocityX < 0 && playerCenterX > stoneCenterX) {
                                    playerX = stone.getX() + stone.getImageView().getFitWidth();
                                    velocityX = 0;
                                    if (JumpAndRun.debugMode) {
                                        System.out.println("Spieler von rechts gegen Stein gestoßen. Bewegung gestoppt.");
                                    }
                                }
                            }
                        }
                    }

                    for (PumpkinProjectile pumpkin : new ArrayList<>(boss.getProjectiles())) {
                        if (playerImageView.getBoundsInParent().intersects(pumpkin.getImageView().getBoundsInParent())) {
                            playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                            double pumpkinCenterX = pumpkin.getX() + pumpkin.getImageView().getFitWidth() / 2;
                            score -= 100;
                            if (score < 0) score = 0;
                            jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                            boss.getProjectiles().remove(pumpkin);
                            gamePane.getChildren().remove(pumpkin.getImageView());
                            if (velocityX > 0 && playerCenterX < pumpkinCenterX) {
                                playerX = pumpkin.getX() - playerImageView.getFitWidth();
                                velocityX = 0;
                            } else if (velocityX < 0 && playerCenterX > pumpkinCenterX) {
                                playerX = pumpkin.getX() + pumpkin.getImageView().getFitWidth();
                                velocityX = 0;
                            }
                        }
                    }
                } else if (bossDefeated) {
                    long currentTime = System.nanoTime();
                    if (currentTime - bossDefeatTime <= TUNNEL_ANIMATION_DURATION) {
                        long elapsedNanos = currentTime - bossDefeatTime;
                        double progress = (double) elapsedNanos / TUNNEL_ANIMATION_DURATION;
                        progress = Math.min(1.0, Math.max(0.0, progress));
                        double opacity = progress * progress;

                        // Setze die Transparenz der Elemente
                        if (tunnelLeft != null) {
                            tunnelLeft.setVisible(true);
                            tunnelLeft.setOpacity(opacity);
                        }
                        if (tunnelRight != null) {
                            tunnelRight.setVisible(true);
                            tunnelRight.setOpacity(opacity);
                        }
                        if (levelText != null) {
                            levelText.setVisible(true);
                            levelText.setOpacity(opacity);
                        }
                    } else {
                        // Animation abgeschlossen: Elemente vollständig sichtbar machen
                        if (tunnelLeft != null) {
                            tunnelLeft.setVisible(true);
                            tunnelLeft.setOpacity(1.0);
                        }
                        if (tunnelRight != null) {
                            tunnelRight.setVisible(true);
                            tunnelRight.setOpacity(1.0);
                        }
                        if (levelText != null) {
                            levelText.setVisible(true);
                            levelText.setOpacity(1.0);
                        }
                        bossDefeated = false;
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

                    if (JumpAndRun.debugKollisionWall) {
                        System.out.println("Wandprüfung: wallLeft=" + wallLeft + ", wallRight=" + wallRight + ", wallTop=" + wallTop + ", wallBottom=" + wallBottom);
                        System.out.println("Spieler: playerLeft=" + playerLeft + ", playerRight=" + playerRight + ", playerTop=" + playerTop + ", playerBottom=" + playerBottom);
                    }

                    if (playerBottom > wallTop && playerTop < wallBottom) {
                        if (velocityX > 0 && playerRight >= wallLeft && oldPlayerX + PLAYER_WIDTH <= wallLeft + MOVE_SPEED) {
                            playerX = wallLeft - PLAYER_WIDTH;
                            velocityX = 0;
                            if (JumpAndRun.debugKollisionWall) {
                                System.out.println("Kollision mit Wand (rechts) bei x=" + playerX);
                            }
                        } else if (velocityX < 0 && playerLeft <= wallRight && oldPlayerX >= wallRight - MOVE_SPEED) {
                            playerX = wallRight;
                            velocityX = 0;
                            if (JumpAndRun.debugKollisionWall) {
                                System.out.println("Kollision mit Wand (links) bei x=" + playerX);
                            }
                        }
                    }

                    if (playerRight > wallLeft && playerLeft < wallRight && playerBottom >= wallTop && oldPlayerY + PLAYER_HEIGHT <= wallTop && velocityY > 0) {
                        playerY = wallTop - PLAYER_HEIGHT;
                        velocityY = 0;
                        jumpCount = 0;
                        onPlatform = true;
                        if (JumpAndRun.debugKollisionWall) {
                            System.out.println("Kollision mit Wand (oben) bei y=" + playerY);
                        }
                    }

                    if (playerRight > wallLeft && playerLeft < wallRight && playerTop <= wallBottom && oldPlayerY >= wallBottom && velocityY < 0) {
                        playerY = wallBottom;
                        velocityY = 0;
                        if (JumpAndRun.debugKollisionWall) {
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
                            if (JumpAndRun.debugPlattform) {
                                System.out.println("Timer gestartet für bruchbare Plattform bei x=" + platform.getX() + ", y=" + platform.getY());
                            }
                        }
                        if (JumpAndRun.debugPlattform) {
                            System.out.println("Kollision mit Plattform, neue y-Position: " + playerY + ", bruchbar: " + platform.isBreakable());
                        }
                        break;
                    } else if (playerTop <= platformBottom && oldPlayerY >= platformBottom &&
                            playerRight > platformLeft && playerLeft < platformRight &&
                            velocityY < 0) {
                        playerY = platformBottom;
                        velocityY = 0;
                        if (JumpAndRun.debugMode) {
                            System.out.println("Kollision mit Plattform (oben), neue y-Position: " + playerY);
                        }
                    } else if (playerBottom > platformTop && playerTop < platformBottom) {
                        if (velocityX > 0 && oldPlayerX + PLAYER_WIDTH <= platformLeft && playerRight >= platformLeft) {
                            playerX = platformLeft - PLAYER_WIDTH;
                            velocityX = 0;
                            if (JumpAndRun.debugMode) {
                                System.out.println("Seitliche Kollision (rechts) bei x=" + playerX);
                            }
                        } else if (velocityX < 0 && oldPlayerX >= platformRight && playerLeft <= platformRight) {
                            playerX = platformRight;
                            velocityX = 0;
                            if (JumpAndRun.debugMode) {
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
                    if (JumpAndRun.debugKollisionGround) {
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
                        if (JumpAndRun.debugKollisionGround) {
                            System.out.println("Kollision mit Boden, neue y-Position: " + playerY);
                        }
                        break;
                    }
                }

                for (Enemy enemy : enemies) {
                    if (JumpAndRun.debugKollisionBox) {
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
                            if (JumpAndRun.debugMode) {
                                System.out.println("Tatsächliche Kollision mit Gegner! Game Over! Distance=" + distance + ", minDistance=" + minDistance);
                            }
                            gameLoop.stop();
                            Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
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
                        jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                        if (JumpAndRun.debugMode) {
                            System.out.println("Raute gesammelt, neuer Score: " + score);
                        }
                    }
                }
                diamonds.removeAll(collectedDiamonds);
                gamePane.getChildren().removeAll(collectedDiamonds);

                if (!isTransitioning && (!onGround && !onPlatform && playerY > 462)) {
                    System.out.println("Game Over! Spielerposition: x=" + playerX + ", y=" + playerY);
                    gameLoop.stop();
                    Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
                    return;
                }

                if (isScore && score <= 0) {
                    System.out.println("Game Over! Score: " + score);
                    gameLoop.stop();
                    Platform.runLater(() -> new GameOverDialog(jumpAndRun, GameEngine.this, primaryStage).show());
                    return;
                }

                updatePlayerImage();
            }
        };
        gameLoop.start();
    }

    private void initializeBossHealthBar() {
        if (bossHealthBar == null && boss != null && boss.isActive()) {
            bossHealthBar = new Rectangle(bossHealthBarInitialX, 50, 400, 20);
            bossHealthBar.setFill(Color.RED);
            bossHealthBar.setStroke(Color.BLACK);
            gamePane.getChildren().add(bossHealthBar);
            if (jumpAndRun.debugMode) {
                System.out.println("bossHealthBar notfallmäßig initialisiert.");
            }
        }
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

    private void shoot(long now) {
        double shootHeight = playerY + PLAYER_HEIGHT / 2;
        double startX = facingRight ? playerX + PLAYER_WIDTH : playerX;
        int currentDamage = (int) (playerDamage * damageMultiplier);
        Projectile projectile = new Projectile(startX, shootHeight, facingRight, null, currentDamage, false, powerupLevel);
        projectiles.add(projectile);
        gamePane.getChildren().add(projectile.getImageView());
        if (JumpAndRun.debugMode) {
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
                if (JumpAndRun.debugMode) {
                    System.out.println("DAMAGE_BOOST aktiviert! Neues Level: " + powerupLevel + ", Schaden: " + playerDamage + " (vorher: " + previousDamage + ")");
                }
                break;
            case SPEED_BOOST:
                if (activeSpeedBoosts >= MAX_SPEED_BOOSTS) {
                    if (JumpAndRun.debugMode) {
                        System.out.println("Maximale Anzahl an SPEED_BOOSTs erreicht (" + MAX_SPEED_BOOSTS + ")");
                    }
                    return;
                }

                activeSpeedBoosts++;
                double previousSpeed = MOVE_SPEED;
                MOVE_SPEED = Math.min(MAX_SPEED, MOVE_SPEED * SPEED_BOOST_MULTIPLIER);
                if (JumpAndRun.debugMode) {
                    System.out.println("SPEED_BOOST aktiviert! Neue Geschwindigkeit: " + MOVE_SPEED + " (vorher: " + previousSpeed + "), Aktive Boosts: " + activeSpeedBoosts);
                }

                PauseTransition speedPause = new PauseTransition(Duration.seconds(30));
                speedPause.setOnFinished(event -> {
                    activeSpeedBoosts--;
                    MOVE_SPEED = Math.max(1.0, MOVE_SPEED / SPEED_BOOST_MULTIPLIER);
                    if (JumpAndRun.debugMode) {
                        System.out.println("SPEED_BOOST abgelaufen! Geschwindigkeit zurückgesetzt auf: " + MOVE_SPEED + ", Aktive Boosts: " + activeSpeedBoosts);
                    }
                });
                speedPause.play();
                break;
        }
    }

    public void revivePlayer(Stage primaryStage) {
        playerX = 50;
        playerY = 480 - PLAYER_HEIGHT;
        velocityX = 0;
        velocityY = 0;
        jumpCount = 0;
        score = 0;
        jumpAndRun.getScoreLabel().setText("Punkte: " + score);
        isPaused = false;
        if (gameLoop != null) {
            gameLoop.start();
        }
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().start();
        }
        playerImageView.setX(playerX);
        playerImageView.setY(playerY);
        gamePane.getChildren().add(playerImageView);
        Platform.runLater(() -> primaryStage.setScene(previousScene));
    }

    // Getter und Setter
    public boolean isPaused() {
        return isPaused;
    }

    public Set<String> getPressedKeys() {
        return pressedKeys;
    }

    public AnimationTimer getGameLoop() {
        return gameLoop;
    }

    public int getScore() {
        return score;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentDesignIndex() {
        return currentDesignIndex;
    }

    public LevelDesign[] getLevels() {
        return levels;
    }

    public ImageView getPlayerImageView() {
        return playerImageView;
    }

    public List<BreakablePlatform> getPlatforms() {
        return platforms;
    }

    public List<Rectangle> getGroundSegments() {
        return groundSegments;
    }

    public List<Diamond> getDiamonds() {
        return diamonds;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Projectile> getEnemyProjectiles() {
        return enemyProjectiles;
    }

    public List<ImageView> getGroundTiles() {
        return groundTiles;
    }

    public List<ImageView> getPlatformViews() {
        return platformViews;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }

    public List<TreasureChest> getTreasureChests() {
        return treasureChests;
    }

    public List<MiniBoss> getMiniBosses() {
        return miniBosses;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public Image getGroundTileImage() {
        return groundTileImage;
    }

    public Image getHoleTileImage() {
        return holeTileImage;
    }

    public ImageView getTunnelLeft() {
        return tunnelLeft;
    }

    public ImageView getTunnelRight() {
        return tunnelRight;
    }

    public Text getLevelText() {
        return levelText;
    }

    public Rectangle getBossHealthBar() {
        return bossHealthBar;
    }

    public void setBossHealthBar(Rectangle bossHealthBar) {
        this.bossHealthBar = bossHealthBar;
    }

    public double getBossHealthBarInitialX() {
        return bossHealthBarInitialX;
    }

    public void setBossHealthBarInitialX(double bossHealthBarInitialX) {
        this.bossHealthBarInitialX = bossHealthBarInitialX;
    }

    // loadLevel (wird später in LevelManager ausgelagert)
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

        if (jumpAndRun.getBackgroundView() != null) {
            root.getChildren().add(jumpAndRun.getBackgroundView());
            jumpAndRun.getBackgroundView().toBack();
            if (JumpAndRun.debugMode) {
                System.out.println("Hintergrund nach dem Löschen wieder hinzugefügt und nach hinten verschoben.");
            }
        }

        if (jumpAndRun.getScoreLabel() != null) {
            root.getChildren().add(jumpAndRun.getScoreLabel());
        }

        if (jumpAndRun.getStartLevel() > 0 || jumpAndRun.getStartDesign() > 0) {
            root.getChildren().add(jumpAndRun.getMousePositionLabel());
        }

        if (playerImageView != null && !root.getChildren().contains(playerImageView)) {
            root.getChildren().add(playerImageView);
            playerImageView.toFront();
            if (JumpAndRun.debugMode) {
                System.out.println("Spieler wieder hinzugefügt und nach vorne verschoben. Anzahl Kinder: " + root.getChildren().size());
            }
        }

        miniBosses = level.getMiniBosses();
        for (MiniBoss miniBoss : miniBosses) {
            if (miniBoss != null) {
                root.getChildren().add(miniBoss.getImageView());
                if (JumpAndRun.debugEnemy) {
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
            boss = new Boss(boss.getX(), boss.getY(), boss.getHealth(), this);
            boss.setGameEngine(this);
            root.getChildren().add(boss.getImageView());
            bossHealthBar = new Rectangle(bossHealthBarInitialX, 50, 400, 20);
            bossHealthBar.setFill(Color.RED);
            bossHealthBar.setStroke(Color.BLACK);
            root.getChildren().add(bossHealthBar);
            if (useDebug) {
                System.out.println("Boss geladen und bossHealthBar initialisiert: x=" + bossHealthBarInitialX + ", Breite=400");
            }
        }

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
            if (JumpAndRun.debugGround) {
                System.out.println("Bodenfliese hinzugefügt bei x=" + (i * tileWidth) + ", y=460, Typ=" + (holePositions.contains(i) ? "Loch" : "Boden") + ", Bild: " + (holePositions.contains(i) && holeTileImage != null ? "hole" : "ground"));
            }
        }

        for (Wall wall : level.getWalls()) {
            wall.render(root);
            if (JumpAndRun.debugLabel) {
                javafx.scene.control.Label positionLabel = new javafx.scene.control.Label("(" + (int)wall.getX() + ", " + (int)wall.getY() + ")");
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

            if (JumpAndRun.debugLabel) {
                javafx.scene.control.Label positionLabel = new javafx.scene.control.Label("(" + (int)platformInfo.getX() + ", " + (int)platformInfo.getY() + ")");
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
            if (JumpAndRun.debugEnemy) {
                System.out.println("Gegner hinzugefügt bei x=" + enemy.getImageView().getX() + ", y=" + enemy.getImageView().getY() +
                        ", Breite=" + enemy.getImageView().getFitWidth() + ", Höhe=" + enemy.getImageView().getFitHeight());
            }
        }

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
            if (JumpAndRun.customFont == null) {
                System.err.println("customFont konnte nicht geladen werden!");
                levelText.setFont(javafx.scene.text.Font.font("Arial", 80));
            } else {
                levelText.setFont(JumpAndRun.customFont);
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

            if (JumpAndRun.debugLabel) {
                System.out.println("Tunneleingang initialisiert (unsichtbar, letztes Design von Level " + (currentLevel + 1) + ")");
            }
        }

        System.out.println("Level geladen (Design " + (currentDesignIndex + 1) + " von Level " + (currentLevel + 1) + ")");
    }
}