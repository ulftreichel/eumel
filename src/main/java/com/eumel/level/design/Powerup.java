package com.eumel.level.design;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Powerup {
    private double x, y;
    private ImageView imageView;
    private boolean isActive = true;
    private PowerupType type;

    public enum PowerupType {
        DAMAGE_BOOST("/images/player/powerup_damage.png", 1),
        SPEED_BOOST("/images/player/powerup_speed.png", 2);

        private final String imagePath;
        private final int value;

        PowerupType(String imagePath, int value) {
            this.imagePath = imagePath;
            this.value = value;
        }
    }

    public Powerup(double x, double y, PowerupType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        Image image = new Image(getClass().getResourceAsStream(type.imagePath));
        imageView = new ImageView(image);
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setX(x);
        imageView.setY(y);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setInactive() {
        isActive = false;
        imageView.setVisible(false);
    }

    public PowerupType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}