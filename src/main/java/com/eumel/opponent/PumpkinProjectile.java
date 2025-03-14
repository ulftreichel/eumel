package com.eumel.opponent;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class PumpkinProjectile {
    private ImageView imageView;
    private double x, y;
    private double velocityX;
    private double velocityY;
    private double initialY;
    private double bounceLevel = 500;
    private boolean active = true;
    private static final double GRAVITY = 0.5;
    private static final double BOUNCE_FACTOR = 0.7;

    public PumpkinProjectile(double startX, double startY, boolean movingRight) {
        this.x = startX;
        this.y = startY;
        this.initialY = startY;
        this.velocityX = movingRight ? 5 : -5;
        this.velocityY = 0;
        Image pumpkinImage = new Image(Objects.requireNonNull(PumpkinProjectile.class.getResource("/images/enemys/pumpkin.png")).toExternalForm());
        imageView = new ImageView(pumpkinImage);
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setX(x);
        imageView.setY(y);
    }

    public PumpkinProjectile(double startX, double startY, boolean movingRight, double bounceLevel) {
        this(startX, startY, movingRight);
        this.bounceLevel = bounceLevel;
    }

    public void update() {
        if (!active) return;

        x += velocityX;
        velocityY += GRAVITY;
        y += velocityY;

        // Abprall vom Boden
        if (y >= bounceLevel - imageView.getFitHeight()) {
            y = bounceLevel - imageView.getFitHeight();
            velocityY = -velocityY * BOUNCE_FACTOR;
            if (Math.abs(velocityY) < 1) velocityY = 0;
        }

        imageView.setX(x);
        imageView.setY(y);

        if (x < 0 || x > 800) {
            active = false;
        }
    }

    public ImageView getImageView() {
        return imageView;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isActive() {
        return active;
    }
}