package com.eumel.opponent;

import com.eumel.JumpAndRun;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class Boss {
    private double x, y;
    private double width = 170;
    private double height = 180;
    private int health;
    private double velocityX = 1.5;
    private double range = 200;
    private boolean isActive = true;
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 2_000_000_000L;
    private static final long INITIAL_DELAY = 1_000_000_000L;
    private List<PumpkinProjectile> projectiles = new ArrayList<>();
    private List<FallingObject> fallingObjects = new ArrayList<>();
    private ImageView imageView;
    private Map<String, Image[]> animations;
    private String currentAnimation = "idle";
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DURATION = 75_000_000L;
    private double velocityY = 0;
    private boolean facingRight = false;
    private boolean debugMode = JumpAndRun.debugMode;
    private long stonePhaseStart = Long.MIN_VALUE;
    private static final long STONE_PHASE_DURATION = 30_000_000_000L;
    private boolean[] stonePhasesTriggered = new boolean[]{false, false, false};
    private int hitCount = 0;
    private int shotsRemaining = 0;
    private boolean isMoving = false;
    private long lastStoneDropTime = 0;

    public Boss(double x, double y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.imageView = new ImageView();
        this.animations = new HashMap<>();
        loadAnimations();
        initializeAnimator();
        lastFrameTime = System.nanoTime();
    }

    private void initializeAnimator() {
        if (animations.get("idle") != null && animations.get("idle").length > 0) {
            imageView.setImage(animations.get("idle")[0]);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(false);
            imageView.setX(x);
            imageView.setY(y);
            imageView.setOpacity(1.0);
            imageView.setVisible(true);
            imageView.setScaleX(facingRight ? 1.0 : -1.0);
            if (debugMode) {
                System.out.println("Boss initialisiert: x=" + x + ", y=" + y + ", health=" + health + ", ScaleX=" + imageView.getScaleX());
            }
        } else {
            System.err.println("Fehler: Idle-Animation konnte nicht geladen werden.");
        }
    }

    private void loadAnimations() {
        animations.put("idle", new Image[]{
                loadImage("/images/enemys/boss/idle001.png"),
                loadImage("/images/enemys/boss/idle002.png"),
                loadImage("/images/enemys/boss/idle003.png"),
                loadImage("/images/enemys/boss/idle004.png"),
                loadImage("/images/enemys/boss/idle005.png"),
                loadImage("/images/enemys/boss/idle006.png"),
                loadImage("/images/enemys/boss/idle007.png"),
                loadImage("/images/enemys/boss/idle008.png"),
                loadImage("/images/enemys/boss/idle009.png"),
                loadImage("/images/enemys/boss/idle010.png"),
                loadImage("/images/enemys/boss/idle011.png"),
                loadImage("/images/enemys/boss/idle012.png"),
                loadImage("/images/enemys/boss/idle013.png"),
                loadImage("/images/enemys/boss/idle014.png"),
                loadImage("/images/enemys/boss/idle015.png"),
                loadImage("/images/enemys/boss/idle016.png"),
                loadImage("/images/enemys/boss/idle017.png"),
                loadImage("/images/enemys/boss/idle018.png"),
                loadImage("/images/enemys/boss/idle019.png"),
                loadImage("/images/enemys/boss/idle020.png"),
                loadImage("/images/enemys/boss/idle021.png"),
                loadImage("/images/enemys/boss/idle022.png"),
                loadImage("/images/enemys/boss/idle023.png"),
                loadImage("/images/enemys/boss/idle024.png"),
                loadImage("/images/enemys/boss/idle025.png"),
                loadImage("/images/enemys/boss/idle026.png"),
                loadImage("/images/enemys/boss/idle027.png"),
                loadImage("/images/enemys/boss/idle028.png"),
                loadImage("/images/enemys/boss/idle029.png"),
                loadImage("/images/enemys/boss/idle030.png"),
                loadImage("/images/enemys/boss/idle031.png"),
                loadImage("/images/enemys/boss/idle032.png"),
                loadImage("/images/enemys/boss/idle033.png"),
                loadImage("/images/enemys/boss/idle034.png"),
                loadImage("/images/enemys/boss/idle035.png"),
                loadImage("/images/enemys/boss/idle036.png"),
                loadImage("/images/enemys/boss/idle037.png"),
                loadImage("/images/enemys/boss/idle038.png"),
                loadImage("/images/enemys/boss/idle039.png"),
                loadImage("/images/enemys/boss/idle040.png"),
                loadImage("/images/enemys/boss/idle041.png"),
                loadImage("/images/enemys/boss/idle042.png"),
                loadImage("/images/enemys/boss/idle043.png"),
                loadImage("/images/enemys/boss/idle044.png"),
                loadImage("/images/enemys/boss/idle045.png"),
                loadImage("/images/enemys/boss/idle046.png"),
                loadImage("/images/enemys/boss/idle047.png"),
                loadImage("/images/enemys/boss/idle048.png"),
                loadImage("/images/enemys/boss/idle049.png"),
                loadImage("/images/enemys/boss/idle050.png"),
                loadImage("/images/enemys/boss/idle051.png"),
                loadImage("/images/enemys/boss/idle052.png"),
                loadImage("/images/enemys/boss/idle053.png"),
        });
        animations.put("walk", new Image[]{
                loadImage("/images/enemys/boss/player001.png"),
                loadImage("/images/enemys/boss/player002.png"),
                loadImage("/images/enemys/boss/player003.png"),
                loadImage("/images/enemys/boss/player004.png"),
                loadImage("/images/enemys/boss/player005.png"),
                loadImage("/images/enemys/boss/player006.png"),
                loadImage("/images/enemys/boss/player007.png"),
                loadImage("/images/enemys/boss/player008.png"),
                loadImage("/images/enemys/boss/player009.png"),
                loadImage("/images/enemys/boss/player010.png"),
                loadImage("/images/enemys/boss/player011.png"),
                loadImage("/images/enemys/boss/player012.png"),
                loadImage("/images/enemys/boss/player013.png"),
                loadImage("/images/enemys/boss/player014.png"),
                loadImage("/images/enemys/boss/player015.png"),
                loadImage("/images/enemys/boss/player016.png")
        });
        // Platzhalter für "jump"-Animation
        // "jump"-Animation muss noch erstellt werden
        animations.put("jump", new Image[]{
                loadImage("/images/enemys/boss/player001.png") // Platzhalter
        });
    }

    private Image loadImage(String path) {
        try {
            Image image = new Image(getClass().getResourceAsStream(path));
            if (image.isError()) {
                throw new Exception("Bild konnte nicht geladen werden: " + path);
            }
            return image;
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Bildes: " + path + " - " + e.getMessage());
            return null;
        }
    }

    public void update(double playerX, double playerY, boolean playerFacingRight, Pane gamePane) {
        if (!isActive) return;

        long now = System.nanoTime();

        if (isMoving) {
            x += velocityX;
            if (x <= 600 - range || x >= 600 + range) velocityX = -velocityX;
            velocityY = (y < playerY) ? 0.5 : -0.5;
        }

        updateAnimation(now);
        imageView.setX(x);
        imageView.setY(y);

        if (shotsRemaining > 0 && now - lastShotTime >= SHOOT_COOLDOWN && now > INITIAL_DELAY) {
            shoot(playerX, playerY, gamePane);
            lastShotTime = now;
            shotsRemaining--;
            if (debugMode) System.out.println("Boss schießt, verbleibende Schüsse: " + shotsRemaining);
        }

        // Steine bei 75%, 50%, 25% Gesundheit
        int maxHealth = 10000;
        double healthPercent = (double) health / maxHealth;

        // Steinphasen nur auslösen, wenn der Boss Schaden genommen hat
        if (hitCount > 0) {
            if (healthPercent <= 0.75 && !stonePhasesTriggered[0]) {
                stonePhaseStart = now;
                stonePhasesTriggered[0] = true;
                lastStoneDropTime = now; // Neuer Zeitstempel für Steine
                if (debugMode) System.out.println("Steinphase bei 75% gestartet");
            } else if (healthPercent <= 0.50 && !stonePhasesTriggered[1]) {
                stonePhaseStart = now;
                stonePhasesTriggered[1] = true;
                lastStoneDropTime = now;
                if (debugMode) System.out.println("Steinphase bei 50% gestartet");
            } else if (healthPercent <= 0.25 && !stonePhasesTriggered[2]) {
                stonePhaseStart = now;
                stonePhasesTriggered[2] = true;
                lastStoneDropTime = now;
                if (debugMode) System.out.println("Steinphase bei 25% gestartet");
            }
        }

        // Steine fallen lassen
        if (stonePhaseStart != Long.MIN_VALUE && now - stonePhaseStart <= STONE_PHASE_DURATION) {
            if (now - lastStoneDropTime >= 3_000_000_000L) {
                FallingObject fallingObject = new FallingObject(playerX + (Math.random() * 100 - 50));
                fallingObjects.add(fallingObject);
                gamePane.getChildren().add(fallingObject.getImageView());
                lastStoneDropTime = now;
                if (debugMode) System.out.println("Stein fällt bei x=" + fallingObject.getX());
            }
        } else if (stonePhaseStart != Long.MIN_VALUE && now - stonePhaseStart > STONE_PHASE_DURATION) {
            stonePhaseStart = Long.MIN_VALUE;
            if (debugMode) System.out.println("Steinphase beendet");
        }

        projectiles.removeIf(p -> !p.isActive());
        for (PumpkinProjectile p : projectiles) {
            p.update();
            if (!p.isActive()) gamePane.getChildren().remove(p.getImageView());
        }

        fallingObjects.removeIf(o -> !o.isActive());
        for (FallingObject o : fallingObjects) {
            o.update();
            if (!o.isActive()) gamePane.getChildren().remove(o.getImageView());
        }
    }

    private void shoot(double playerX, double playerY, Pane gamePane) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        currentAnimation = "attack1";
        currentFrame = 0;
        boolean movingRight = playerX > centerX;
        PumpkinProjectile projectile = new PumpkinProjectile(centerX, centerY, movingRight, JumpAndRun.GROUND_LEVEL);
        projectiles.add(projectile);
        gamePane.getChildren().add(projectile.getImageView());
    }

    public void takeDamage(int damage) {
        if (isActive) {
            health -= damage;
            hitCount++;
            if (debugMode) {
                System.out.println("Boss nimmt Schaden: " + damage + ", Treffer: " + hitCount + ", Gesundheit: " + health);
            }

            if (hitCount == 5) {
                isMoving = true;
                shotsRemaining = 5;
                if (debugMode) System.out.println("Boss beginnt zu laufen und schießt 5 Mal");
            } else if (hitCount > 5 && hitCount % 5 == 0) {
                shotsRemaining = 5 + ((hitCount / 5) - 1) * 5;
                if (debugMode) System.out.println("Boss schießt " + shotsRemaining + " Mal");
            }

            if (health <= 0) {
                isActive = false;
                imageView.setVisible(false);
                if (debugMode) System.out.println("Boss besiegt!");
            }
        }
    }

    private void updateAnimation(long now) {
        if (now - lastFrameTime >= FRAME_DURATION) {
            Image[] frames = animations.get(currentAnimation);
            if (frames != null && frames.length > 0 && frames[0] != null) {
                imageView.setImage(frames[currentFrame]);
                currentFrame = (currentFrame + 1) % frames.length;
                lastFrameTime = now;
                if (debugMode) System.out.println("Frame gewechselt: " + currentAnimation + ", Frame: " + currentFrame);
            } else {
                System.err.println("Fehler: Animation " + currentAnimation + " enthält keine gültigen Frames.");
            }
        }

        String newAnimation = "idle";
        if (velocityY < 0) {
            newAnimation = "jump";
        } else if (isMoving && Math.abs(velocityX) > 0) {
            newAnimation = "walk";
            facingRight = velocityX > 0;
        }

        if (!newAnimation.equals(currentAnimation)) {
            currentAnimation = newAnimation;
            currentFrame = 0;
            if (debugMode) System.out.println("Animation gewechselt zu: " + currentAnimation);
        }

        imageView.setScaleX(facingRight ? 1.0 : -1.0);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public List<PumpkinProjectile> getProjectiles() {
        return projectiles;
    }

    public List<FallingObject> getFallingObjects() {
        return fallingObjects;
    }

    public int getHealth() {
        return health;
    }

    public boolean isActive() {
        return isActive;
    }
}