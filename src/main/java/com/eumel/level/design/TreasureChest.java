package com.eumel.level.design;

import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.util.Objects;
import java.util.Random;

public class TreasureChest {
    private double x, y;
    private ImageView imageView;
    private boolean isActive = true;
    private boolean isFull;
    private boolean givesPowerup;
    private Powerup.PowerupType powerupType;
    private int scoreReward;
    private Image closedImage, emptyImage, fullImage;
    private static final Random random = new Random();

    public TreasureChest(double x, double y) {
        this.x = x;
        this.y = y;

        closedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/level/treasure_closed.png")));
        emptyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/level/treasure_empty.png")));
        fullImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/level/treasure_full.png")));

        isFull = random.nextBoolean();
        givesPowerup = random.nextBoolean();

        if (isFull) {
            if (givesPowerup) {
                powerupType = random.nextBoolean() ? Powerup.PowerupType.DAMAGE_BOOST : Powerup.PowerupType.SPEED_BOOST;
                scoreReward = 0;
            } else {
                scoreReward = 50 + random.nextInt(151);
                powerupType = null;
            }
        } else {
            scoreReward = 0;
            powerupType = null;
        }

        imageView = new ImageView(closedImage);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        imageView.setX(x);
        imageView.setY(y);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public boolean isActive() {
        return isActive;
    }

    public void open() {
        if (!isActive) return;

        isActive = false;
        displayReward();

        if (isFull) {
            imageView.setImage(fullImage);
        } else {
            imageView.setImage(emptyImage);
        }

        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> imageView.setVisible(false));
        pause.play();
    }

    private void displayReward() {
        String message;
        if (isFull) {
            if (givesPowerup) {
                message = "Du hast ein Powerup erhalten: " + powerupType.name();
            } else {
                message = "Du hast " + scoreReward + " Punkte erhalten!";
            }
        } else {
            message = "Die Truhe war leer...";
        }
        System.out.println(message);
        // Für UI: JumpAndRun.showMessage(message);
    }

    public int getScoreReward() {
        return scoreReward;
    }

    public Powerup.PowerupType getPowerupType() {
        return powerupType;
    }

    public boolean givesPowerup() {
        return givesPowerup;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isFull() {
        return isFull;
    }
}