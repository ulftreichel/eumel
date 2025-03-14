package com.eumel;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class Projectile {
    private ImageView imageView;
    private double x, y;
    private double startX;
    private double speed = 1;
    private boolean movingRight;
    private boolean isActive = true;
    private double rotationAngle = 0;
    private double rotationSpeed = 3;
    private double maxRange;
    private int damage = 100;
    private Image projectileImage;
    private boolean shouldRotate;

    // Bilder für Spieler-Projektile
    private static final Image[] PLAYER_PROJECTILE_IMAGES = new Image[5];
    static {
        try {
            PLAYER_PROJECTILE_IMAGES[0] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/bonbon.png")));
            PLAYER_PROJECTILE_IMAGES[1] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/powerup1.png")));
            PLAYER_PROJECTILE_IMAGES[2] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/powerup2.png")));
            PLAYER_PROJECTILE_IMAGES[3] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/powerup3.png")));
            PLAYER_PROJECTILE_IMAGES[4] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/powerup4.png")));
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Spieler-Projektilbilder: " + e.getMessage());
            for (int i = 0; i < 3; i++) {
                PLAYER_PROJECTILE_IMAGES[i] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/bonbon.png")));
            }
        }
    }

    // Bilder für Gegner-Projektile
    private static final Image[] ENEMY_PROJECTILE_IMAGES = new Image[4];
    static {
        try {
            ENEMY_PROJECTILE_IMAGES[0] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/enemys/jack_o_lantern1.png"))); // damage < 100
            ENEMY_PROJECTILE_IMAGES[1] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/enemys/jack_o_lantern2.png"))); // 100 < damage < 200
            ENEMY_PROJECTILE_IMAGES[2] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/enemys/jack_o_lantern3.png"))); // damage > 200 < 300
            ENEMY_PROJECTILE_IMAGES[3] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/enemys/jack_o_lantern4.png"))); // damage < 300
        } catch (Exception e) {
            System.out.println("Fehler beim Laden der Gegner-Projektilbilder: " + e.getMessage());
            for (int i = 0; i < 4; i++) {
                ENEMY_PROJECTILE_IMAGES[i] = new Image(Objects.requireNonNull(Projectile.class.getResourceAsStream("/images/player/bonbon.png"))); // Fallback
            }
        }
    }

    public Projectile(double startX, double startY, boolean movingRight) {
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.movingRight = movingRight;
        this.maxRange = 180; // Standardreichweite für Spieler Projektile
        this.projectileImage = PLAYER_PROJECTILE_IMAGES[0];
        this.shouldRotate = true;
        imageView = new ImageView(projectileImage);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setRotate(rotationAngle);
    }

    public Projectile(double startX, double startY, boolean movingRight, Image customImage, int damage, boolean isEnemyProjectile, double maxRange) {
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.movingRight = movingRight;
        this.maxRange = maxRange > 0 ? maxRange : 180;
        this.damage = damage > 0 ? damage : 100;
        this.shouldRotate = !isEnemyProjectile; // Gegner-Projektile rotieren nicht

        if (isEnemyProjectile) {
            int imageIndex = 0;
            if (damage > 200) {
                imageIndex = 2;
            } else if (damage > 100) {
                imageIndex = 1;
            }
            this.projectileImage = customImage != null ? customImage : ENEMY_PROJECTILE_IMAGES[imageIndex];
        } else {
            this.projectileImage = customImage != null ? customImage : PLAYER_PROJECTILE_IMAGES[0];
        }

        imageView = new ImageView(projectileImage);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setRotate(rotationAngle);
    }

    public Projectile(double startX, double startY, boolean movingRight, Image customImage, int damage, boolean isEnemyProjectile, int powerupLevel) {
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.movingRight = movingRight;
        this.maxRange = 180;
        this.damage = damage > 0 ? damage : 100;
        this.shouldRotate = !isEnemyProjectile;

        if (isEnemyProjectile) {
            int imageIndex = 0;
            if (damage > 200) {
                imageIndex = 2;
            } else if (damage > 100) {
                imageIndex = 1;
            }
            this.projectileImage = customImage != null ? customImage : ENEMY_PROJECTILE_IMAGES[imageIndex];
        } else {
            int imageIndex = powerupLevel;
            this.projectileImage = customImage != null ? customImage : PLAYER_PROJECTILE_IMAGES[imageIndex > 2 ? 0 : imageIndex];
            this.damage = imageIndex == 0 ? 100 : (imageIndex == 1 ? 150 : 200);
        }

        imageView = new ImageView(projectileImage);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setRotate(rotationAngle);
    }

    public void update() {
        if (!isActive) return;
        x += movingRight ? speed : -speed;
        imageView.setX(x);

        // Rotation nur anwenden, wenn shouldRotate true ist
        if (shouldRotate) {
            rotationAngle += rotationSpeed;
            if (rotationAngle >= 360) rotationAngle -= 360;
            imageView.setRotate(rotationAngle);
        }

        double distanceTraveled = Math.abs(x - startX);
        if (distanceTraveled >= maxRange) {
            isActive = false;
            return;
        }
        if (x < 0 || x > 800) {
            isActive = false;
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setInactive() {
        isActive = false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return imageView.getFitWidth();
    }

    public double getHeight() {
        return imageView.getFitHeight();
    }

    public int getDamage() {
        return damage;
    }
}