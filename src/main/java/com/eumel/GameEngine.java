package com.eumel;

import com.eumel.level.LevelManager;
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
    private LevelManager levelManager;

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
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Projectile> enemyProjectiles = new ArrayList<>();

    public GameEngine(JumpAndRun jumpAndRun, Stage primaryStage, Pane gamePane) {
        this.jumpAndRun = jumpAndRun;
        this.primaryStage = primaryStage;
        this.gamePane = gamePane;
        this.levelManager = new LevelManager(jumpAndRun);
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

        // Audio starten
        jumpAndRun.getAudioManager().startGameAudio(levelManager.getBoss() != null && levelManager.getBoss().isActive());

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

        playerImageView = new ImageView(playerStandImage);
        playerImageView.setFitWidth(PLAYER_WIDTH);
        playerImageView.setFitHeight(PLAYER_HEIGHT);
        playerImageView.setX(playerX);
        playerImageView.setY(playerY);
        gamePane.getChildren().add(playerImageView);

        // LevelManager initialisieren und Level laden
        this.previousScene = gameScene;
        levelManager.initializeLevels(startLevel, startDesign);
        levelManager.loadLevel(gamePane, JumpAndRun.debugMode);
        gamePane.getChildren().add(playerImageView); // Sicherstellen, dass der Spieler nach dem Laden hinzugefügt wird
        playerImageView.toFront();

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

                if (playerX > 800 - PLAYER_WIDTH && !isTransitioning && (levelManager.getBoss() == null || !levelManager.getBoss().isActive())) {
                    isTransitioning = true;
                    if (levelManager.getCurrentLevel() < 9 || levelManager.getCurrentLevelDesign().hasNextDesign()) {
                        levelManager.nextDesignOrLevel();
                        levelManager.loadLevel(gamePane, JumpAndRun.debugMode);
                        playerX = 0;
                        playerY = findSafeYPosition();
                        velocityX = 0;
                        velocityY = 0;
                        jumpCount = 0;
                        gamePane.getChildren().add(playerImageView);
                        playerImageView.toFront();
                        if (JumpAndRun.debugMode) {
                            System.out.println("Level/Design gewechselt: Level " + (levelManager.getCurrentLevel() + 1) + ", Design " + (levelManager.getCurrentDesignIndex() + 1));
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
                        Iterator<Enemy> enemyIterator = levelManager.getEnemies().iterator();
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

                            if (centerCollision) {
                                enemy.takeDamage(projectile.getDamage());
                                gamePane.getChildren().remove(projectile.getImageView());
                                projectileIterator.remove();
                                score += 20;
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                isScore = true;
                                if (!enemy.isActive()) {
                                    gamePane.getChildren().remove(enemy.getImageView());
                                    enemyIterator.remove();
                                    score += 100;
                                    jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                                }
                            }
                        }
                    }
                }

                // Gegner-Projektile
                Iterator<Projectile> enemyProjectileIterator = enemyProjectiles.iterator();
                while (enemyProjectileIterator.hasNext()) {
                    Projectile enemyProjectile = enemyProjectileIterator.next();
                    enemyProjectile.update();
                    if (!enemyProjectile.isActive()) {
                        gamePane.getChildren().remove(enemyProjectile.getImageView());
                        enemyProjectileIterator.remove();
                    } else {
                        double playerCenterX = playerX + PLAYER_WIDTH / 2;
                        double playerCenterY = playerY + PLAYER_HEIGHT / 2;
                        double projectileCenterX = enemyProjectile.getX() + enemyProjectile.getWidth() / 2;
                        double projectileCenterY = enemyProjectile.getY() + enemyProjectile.getHeight() / 2;
                        double tolerance = 10.0;
                        if (Math.abs(projectileCenterX - playerCenterX) <= tolerance &&
                                Math.abs(projectileCenterY - playerCenterY) <= tolerance) {
                            score -= enemyProjectile.getDamage();
                            jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                            gamePane.getChildren().remove(enemyProjectile.getImageView());
                            enemyProjectileIterator.remove();
                            if (score < 0) {
                                gameLoop.stop();
                                Platform.runLater(() -> new GameOverDialog(jumpAndRun, GameEngine.this, primaryStage).show());
                                return;
                            }
                        }
                    }
                }

                // Miniboss-Logik
                for (MiniBoss miniBoss : levelManager.getMiniBosses()) {
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
                Iterator<Powerup> powerupIterator = levelManager.getPowerups().iterator();
                while (powerupIterator.hasNext()) {
                    Powerup powerup = powerupIterator.next();
                    if (powerup.isActive() && playerImageView.getBoundsInParent().intersects(powerup.getImageView().getBoundsInParent())) {
                        if (powerup.getType() == Powerup.PowerupType.DAMAGE_BOOST) {
                            if (!canShoot) {
                                canShoot = true;
                                powerupLevel = 1;
                            } else {
                                damageMultiplier *= 1.25;
                                powerupLevel = Math.min(powerupLevel + 1, 4);
                            }
                        } else if (powerup.getType() == Powerup.PowerupType.SPEED_BOOST) {
                            MOVE_SPEED += 1;
                        }
                        powerup.setInactive();
                        gamePane.getChildren().remove(powerup.getImageView());
                        powerupIterator.remove();
                    }
                }

                // Schatztruhen
                for (TreasureChest chest : levelManager.getTreasureChests()) {
                    if (!chest.isActive()) continue;

                    double playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                    double playerCenterY = playerY + playerImageView.getFitHeight() / 2;
                    double chestCenterX = chest.getX() + chest.getImageView().getFitWidth() / 2;
                    double chestCenterY = chest.getY() + chest.getImageView().getFitHeight() / 2;

                    double playerChestHorizontalTolerance = 30.0;
                    double playerChestVerticalTolerance = 40.0;
                    boolean playerChestCollision = Math.abs(playerCenterX - chestCenterX) <= playerChestHorizontalTolerance &&
                            Math.abs(playerCenterY - chestCenterY) <= playerChestVerticalTolerance;

                    if (playerChestCollision) {
                        jumpAndRun.getAudioManager().playEffect("/sound/chest_open.mp3");
                        if (chest.isFull()) {
                            jumpAndRun.getAudioManager().playEffect("/sound/chest_full.mp3");
                            if (chest.givesPowerup()) {
                                Powerup.PowerupType powerupType = chest.getPowerupType();
                                activatePowerup(powerupType);
                            } else {
                                score += chest.getScoreReward();
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                            }
                        } else {
                            jumpAndRun.getAudioManager().playEffect("/sound/chest_empty.mp3");
                        }
                        chest.open();
                    }
                }

                // Boss-Logik
                if (levelManager.getBoss() != null && levelManager.getBoss().isActive()) {
                    initializeBossHealthBar();
                    levelManager.getBoss().update(playerX, playerY, facingRight, gamePane);
                    double healthRatio = (double) levelManager.getBoss().getHealth() / 5000.0;
                    double newWidth = 400 * healthRatio;
                    if (levelManager.getBossHealthBar() != null) {
                        levelManager.getBossHealthBar().setWidth(newWidth);
                        levelManager.getBossHealthBar().setX(levelManager.getBossHealthBarInitialX() + (400 - newWidth));
                    }

                    Iterator<Projectile> projIterator = projectiles.iterator();
                    while (projIterator.hasNext()) {
                        Projectile p = projIterator.next();
                        double projectileCenterX = p.getX() + p.getWidth() / 2;
                        double projectileCenterY = p.getY() + p.getHeight() / 2;
                        double bossCenterX = levelManager.getBoss().getImageView().getX() + levelManager.getBoss().getImageView().getFitWidth() / 2;
                        double bossCenterY = levelManager.getBoss().getImageView().getY() + levelManager.getBoss().getImageView().getFitHeight() / 2;

                        double horizontalTolerance = 10.0;
                        double verticalTolerance = 60.0;
                        boolean centerCollision = Math.abs(projectileCenterX - bossCenterX) <= horizontalTolerance &&
                                Math.abs(projectileCenterY - bossCenterY) <= verticalTolerance;

                        if (centerCollision) {
                            levelManager.getBoss().takeDamage(p.getDamage());
                            gamePane.getChildren().remove(p.getImageView());
                            projIterator.remove();
                            if (!levelManager.getBoss().isActive()) {
                                bossDefeated = true;
                                bossDefeatTime = System.nanoTime();
                                gamePane.getChildren().remove(levelManager.getBoss().getImageView());
                                gamePane.getChildren().remove(levelManager.getBossHealthBar());
                                score += 1000;
                                jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                            }
                        }
                    }

                    double playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                    double playerCenterY = playerY + playerImageView.getFitHeight() / 2;
                    double bossCenterX = levelManager.getBoss().getImageView().getX() + levelManager.getBoss().getImageView().getFitWidth() / 2;
                    double bossCenterY = levelManager.getBoss().getImageView().getY() + levelManager.getBoss().getImageView().getFitHeight() / 2;

                    double playerBossHorizontalTolerance = 40.0;
                    double playerBossVerticalTolerance = 90.0;
                    boolean playerBossCollision = Math.abs(playerCenterX - bossCenterX) <= playerBossHorizontalTolerance &&
                            Math.abs(playerCenterY - bossCenterY) <= playerBossVerticalTolerance;

                    if (playerBossCollision) {
                        score -= 500;
                        jumpAndRun.getScoreLabel().setText("Leben: " + score);
                        if (score < 0) {
                            gameLoop.stop();
                            Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
                            return;
                        }
                        if (velocityX > 0 && playerCenterX < bossCenterX) {
                            playerX = levelManager.getBoss().getImageView().getX() - playerImageView.getFitWidth();
                            velocityX = 0;
                        } else if (velocityX < 0 && playerCenterX > bossCenterX) {
                            playerX = levelManager.getBoss().getImageView().getX() + levelManager.getBoss().getImageView().getFitWidth();
                            velocityX = 0;
                        }
                    }

                    for (FallingObject stone : levelManager.getBoss().getFallingObjects()) {
                        double stoneCenterX = stone.getX() + stone.getImageView().getFitWidth() / 2;
                        double stoneCenterY = stone.getY() + stone.getImageView().getFitHeight() / 2;

                        double playerStoneHorizontalTolerance = 20.0;
                        double playerStoneVerticalTolerance = 30.0;
                        boolean playerStoneCollision = Math.abs(playerCenterX - stoneCenterX) <= playerStoneHorizontalTolerance &&
                                Math.abs(playerCenterY - stoneCenterY) <= playerStoneVerticalTolerance;

                        if (playerStoneCollision) {
                            if (!stone.isOnGround()) {
                                score -= 150;
                                jumpAndRun.getScoreLabel().setText("Leben: " + score);
                                if (score <= 0) {
                                    gameLoop.stop();
                                    Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
                                    return;
                                }
                            } else {
                                if (velocityX > 0 && playerCenterX < stoneCenterX) {
                                    playerX = stone.getX() - playerImageView.getFitWidth();
                                    velocityX = 0;
                                } else if (velocityX < 0 && playerCenterX > stoneCenterX) {
                                    playerX = stone.getX() + stone.getImageView().getFitWidth();
                                    velocityX = 0;
                                }
                            }
                        }
                    }

                    for (PumpkinProjectile pumpkin : new ArrayList<>(levelManager.getBoss().getProjectiles())) {
                        if (playerImageView.getBoundsInParent().intersects(pumpkin.getImageView().getBoundsInParent())) {
                            playerCenterX = playerX + playerImageView.getFitWidth() / 2;
                            double pumpkinCenterX = pumpkin.getX() + pumpkin.getImageView().getFitWidth() / 2;
                            score -= 100;
                            if (score < 0) score = 0;
                            jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                            levelManager.getBoss().getProjectiles().remove(pumpkin);
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

                        if (levelManager.getTunnelLeft() != null) {
                            levelManager.getTunnelLeft().setVisible(true);
                            levelManager.getTunnelLeft().setOpacity(opacity);
                        }
                        if (levelManager.getTunnelRight() != null) {
                            levelManager.getTunnelRight().setVisible(true);
                            levelManager.getTunnelRight().setOpacity(opacity);
                        }
                        if (levelManager.getLevelText() != null) {
                            levelManager.getLevelText().setVisible(true);
                            levelManager.getLevelText().setOpacity(opacity);
                        }
                    } else {
                        if (levelManager.getTunnelLeft() != null) {
                            levelManager.getTunnelLeft().setVisible(true);
                            levelManager.getTunnelLeft().setOpacity(1.0);
                        }
                        if (levelManager.getTunnelRight() != null) {
                            levelManager.getTunnelRight().setVisible(true);
                            levelManager.getTunnelRight().setOpacity(1.0);
                        }
                        if (levelManager.getLevelText() != null) {
                            levelManager.getLevelText().setVisible(true);
                            levelManager.getLevelText().setOpacity(1.0);
                        }
                        bossDefeated = false;
                    }
                }

                // Gegner aktualisieren
                for (Enemy enemy : levelManager.getEnemies()) {
                    enemy.updatePosition(levelManager.getPlatforms(), gamePane, enemyProjectiles, playerX, playerY);
                }

                boolean onPlatform = false;

                // Kollision mit Wänden
                for (Wall wall : levelManager.getCurrentLevelDesign().getWalls()) {
                    double wallLeft = wall.getX();
                    double wallRight = wall.getX() + wall.getWidth();
                    double wallTop = wall.getY();
                    double wallBottom = wall.getY() + wall.getHeight();

                    double playerBottom = playerY + PLAYER_HEIGHT;
                    double playerTop = playerY;
                    double playerLeft = playerX;
                    double playerRight = playerX + PLAYER_WIDTH;

                    if (playerBottom > wallTop && playerTop < wallBottom) {
                        if (velocityX > 0 && playerRight >= wallLeft && oldPlayerX + PLAYER_WIDTH <= wallLeft + MOVE_SPEED) {
                            playerX = wallLeft - PLAYER_WIDTH;
                            velocityX = 0;
                        } else if (velocityX < 0 && playerLeft <= wallRight && oldPlayerX >= wallRight - MOVE_SPEED) {
                            playerX = wallRight;
                            velocityX = 0;
                        }
                    }

                    if (playerRight > wallLeft && playerLeft < wallRight && playerBottom >= wallTop && oldPlayerY + PLAYER_HEIGHT <= wallTop && velocityY > 0) {
                        playerY = wallTop - PLAYER_HEIGHT;
                        velocityY = 0;
                        jumpCount = 0;
                        onPlatform = true;
                    }

                    if (playerRight > wallLeft && playerLeft < wallRight && playerTop <= wallBottom && oldPlayerY >= wallBottom && velocityY < 0) {
                        playerY = wallBottom;
                        velocityY = 0;
                    }
                }

                // Kollision mit Plattformen
                for (BreakablePlatform platform : levelManager.getPlatforms()) {
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
                        }
                    } else if (playerTop <= platformBottom && oldPlayerY >= platformBottom &&
                            playerRight > platformLeft && playerLeft < platformRight &&
                            velocityY < 0) {
                        playerY = platformBottom;
                        velocityY = 0;
                    } else if (playerBottom > platformTop && playerTop < platformBottom) {
                        if (velocityX > 0 && oldPlayerX + PLAYER_WIDTH <= platformLeft && playerRight >= platformLeft) {
                            playerX = platformLeft - PLAYER_WIDTH;
                            velocityX = 0;
                        } else if (velocityX < 0 && oldPlayerX >= platformRight && playerLeft <= platformRight) {
                            playerX = platformRight;
                            velocityX = 0;
                        }
                    }
                }

                // Kollision mit Boden
                boolean onGround = false;
                int tileWidth = (int) (levelManager.getGroundTileImage() != null ? levelManager.getGroundTileImage().getWidth() : 81);
                double groundSurfaceY = 460 + 1;
                for (int i = 0; i < levelManager.getGroundTiles().size(); i++) {
                    ImageView groundTile = levelManager.getGroundTiles().get(i);
                    double tileX = groundTile.getX();
                    double tileY = groundTile.getY();
                    double playerBottom = playerY + PLAYER_HEIGHT;
                    double playerLeft = playerX;
                    double playerRight = playerX + PLAYER_WIDTH;
                    boolean isHole = levelManager.getCurrentLevelDesign().getHolePositions().contains(i);
                    if (!isHole && playerBottom >= groundSurfaceY - 1 && oldPlayerY + PLAYER_HEIGHT > groundSurfaceY &&
                            playerX + PLAYER_WIDTH > tileX && playerX < tileX + tileWidth &&
                            velocityY >= 0) {
                        playerY = groundSurfaceY - PLAYER_HEIGHT;
                        velocityY = 0;
                        jumpCount = 0;
                        onGround = true;
                        break;
                    }
                }

                // Kollision mit Gegnern
                for (Enemy enemy : levelManager.getEnemies()) {
                    if (playerImageView.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                        double playerCenterX = playerX + (PLAYER_WIDTH / 2);
                        double playerCenterY = playerY + (PLAYER_HEIGHT / 2);
                        double enemyCenterX = enemy.getImageView().getX() + (enemy.getImageView().getFitWidth() / 2);
                        double enemyCenterY = enemy.getImageView().getY() + (enemy.getImageView().getFitHeight() / 2);
                        double distance = Math.sqrt(Math.pow(playerCenterX - enemyCenterX, 2) + Math.pow(playerCenterY - enemyCenterY, 2));
                        double minDistance = (PLAYER_WIDTH + enemy.getImageView().getFitWidth()) / 4;

                        if (distance < minDistance) {
                            gameLoop.stop();
                            Platform.runLater(() -> new TaskDialog(jumpAndRun, GameEngine.this, jumpAndRun.getDbDAO(), primaryStage).show());
                            return;
                        }
                    }
                }

                // Diamanten sammeln
                List<Diamond> collectedDiamonds = new ArrayList<>();
                for (Diamond diamond : levelManager.getDiamonds()) {
                    if (playerImageView.getBoundsInParent().intersects(diamond.getCollisionBox().getBoundsInParent())) {
                        collectedDiamonds.add(diamond);
                        score += 10;
                        isScore = true;
                        jumpAndRun.getScoreLabel().setText("Punkte: " + score);
                    }
                }
                levelManager.getDiamonds().removeAll(collectedDiamonds);
                gamePane.getChildren().removeAll(collectedDiamonds);

                // Game Over Bedingungen
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
        if (levelManager.getBossHealthBar() == null && levelManager.getBoss() != null && levelManager.getBoss().isActive()) {
            Rectangle bossHealthBar = new Rectangle(levelManager.getBossHealthBarInitialX(), 50, 400, 20);
            bossHealthBar.setFill(Color.RED);
            bossHealthBar.setStroke(Color.BLACK);
            gamePane.getChildren().add(bossHealthBar);
            levelManager.setBossHealthBar(bossHealthBar);
            if (JumpAndRun.debugMode) {
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
    }

    private double findSafeYPosition() {
        int tileWidth = (int) (levelManager.getGroundTileImage() != null ? levelManager.getGroundTileImage().getWidth() : 81);
        double groundSurfaceY = 460;
        for (ImageView groundTile : levelManager.getGroundTiles()) {
            double tileX = groundTile.getX();
            double tileY = groundTile.getY();
            if (playerX + PLAYER_WIDTH > tileX && playerX < tileX + tileWidth && !levelManager.getCurrentLevelDesign().getHolePositions().contains((int) (tileX / tileWidth))) {
                return groundSurfaceY - PLAYER_HEIGHT;
            }
        }
        for (BreakablePlatform platform : levelManager.getPlatforms()) {
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
        // Spielerposition und Zustand zurücksetzen
        playerX = 50;
        playerY = 480 - PLAYER_HEIGHT;
        velocityX = 0;
        velocityY = 0;
        jumpCount = 0;
        score = 0;
        jumpAndRun.getScoreLabel().setText("Punkte: " + score);
        isPaused = false;

        // Gedrückte Tasten zurücksetzen
        pressedKeys.clear();

        // Game Loop und FallingLeaves starten
        if (gameLoop != null) {
            gameLoop.start();
        }
        if (jumpAndRun.getFallingLeaves() != null) {
            jumpAndRun.getFallingLeaves().start();
        }

        // Spielerposition aktualisieren
        playerImageView.setX(playerX);
        playerImageView.setY(playerY);
        if (!gamePane.getChildren().contains(playerImageView)) {
            gamePane.getChildren().add(playerImageView);
        }

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
        return levelManager.getCurrentLevel();
    }

    public int getCurrentDesignIndex() {
        return levelManager.getCurrentDesignIndex();
    }

    public ImageView getPlayerImageView() {
        return playerImageView;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Projectile> getEnemyProjectiles() {
        return enemyProjectiles;
    }

    public List<BreakablePlatform> getPlatforms() {
        return levelManager.getPlatforms();
    }

    public List<Rectangle> getGroundSegments() {
        return levelManager.getGroundSegments();
    }

    public List<Diamond> getDiamonds() {
        return levelManager.getDiamonds();
    }

    public List<Enemy> getEnemies() {
        return levelManager.getEnemies();
    }

    public List<ImageView> getGroundTiles() {
        return levelManager.getGroundTiles();
    }

    public List<ImageView> getPlatformViews() {
        return levelManager.getPlatformViews();
    }

    public List<Powerup> getPowerups() {
        return levelManager.getPowerups();
    }

    public List<TreasureChest> getTreasureChests() {
        return levelManager.getTreasureChests();
    }

    public List<MiniBoss> getMiniBosses() {
        return levelManager.getMiniBosses();
    }

    public Boss getBoss() {
        return levelManager.getBoss();
    }

    public void setBoss(Boss boss) {
        levelManager.setBoss(boss);
    }
}