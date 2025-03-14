package com.eumel.opponent;

import com.eumel.Projectile;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MiniBoss {
    private double x, y;
    private double width = 64;
    private double height = 96;
    private int health;
    private double velocityX = 1.5;
    private double range = 150;
    private ImageView imageView;
    private boolean isActive = true;
    private long lastShotTime = 0;
    private static final long SHOOT_COOLDOWN = 1_500_000_000; // 1,5 Sekunden
    private List<Projectile> projectiles = new ArrayList<>();

    public MiniBoss(double x, double y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/enemys/miniboss_left.png")));
        imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setX(x);
        imageView.setY(y);
    }

    public void update(double playerX, double playerY, Pane gamePane) {
        if (!isActive) return;

        x += velocityX;
        if (x <= 0 || x >= range) velocityX = -velocityX;
        imageView.setX(x);

        if (System.nanoTime() - lastShotTime >= SHOOT_COOLDOWN) {
            shoot(playerX, playerY, gamePane);
            lastShotTime = System.nanoTime();
        }
    }

    private void shoot(double playerX, double playerY, Pane gamePane) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        boolean movingRight = playerX > centerX;
        Projectile projectile = new Projectile(centerX, centerY, movingRight, null, 100, true, 0); // 100 Schaden
        projectiles.add(projectile);
        gamePane.getChildren().add(projectile.getImageView());
    }

    public void takeDamage(int damage) {
        if (isActive) {
            health -= damage;
            if (health <= 0) {
                isActive = false;
                imageView.setVisible(false);
            }
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getHealth() {
        return health;
    }
}