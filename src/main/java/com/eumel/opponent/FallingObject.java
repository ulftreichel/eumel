package com.eumel.opponent;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class FallingObject {
    private ImageView imageView;
    private double x, y;
    private double velocityY = 5;
    private boolean active = true;
    private boolean onGround = false;
    private long groundTime;
    private static final long LIFETIME = 4_000_000_000L;
    private static final double GROUND_LEVEL = 475;

    public FallingObject(double x) {
        this.x = x;
        this.y = 0; // Startet oben
        Image stoneImage = new Image(Objects.requireNonNull(FallingObject.class.getResource("/images/enemys/stone.png")).toExternalForm());
        imageView = new ImageView(stoneImage);
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setX(x);
        imageView.setY(y);
    }

    public void update() {
        if (!active) return;

        if (!onGround) {
            y += velocityY;
            if (y >= GROUND_LEVEL - imageView.getFitHeight()) {
                y = GROUND_LEVEL - imageView.getFitHeight();
                onGround = true;
                groundTime = System.nanoTime();
            }
        } else {
            if (System.nanoTime() - groundTime >= LIFETIME) {
                active = false;
            }
        }
        imageView.setY(y);
        imageView.setX(x);
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

    public boolean isOnGround() {
        return onGround;
    }
}