package com.eumel.level.design;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BreakablePlatform extends Rectangle {
    private final boolean isBreakable;
    private final Timeline fallTimer;

    public BreakablePlatform(double x, double y, double width, double height, boolean isBreakable, ImageView platformView) {
        super(x, y, width, height);
        this.isBreakable = isBreakable;

        // Timer für den wegfall
        fallTimer = new Timeline(new KeyFrame(Duration.millis(1000), event -> {
            if (isBreakable) {
                System.out.println("Plattform bei x=" + getX() + ", y=" + getY() + " fällt weg!");
                this.setVisible(false);
                this.setDisable(true);
                if (platformView != null) {
                    platformView.setVisible(false);
                }
            }
        }));
        fallTimer.setCycleCount(1);
    }

    public void startFallTimer() {
        if (isBreakable && !fallTimer.getStatus().equals(Timeline.Status.RUNNING)) {
            fallTimer.play();
        }
    }

    public boolean isBreakable() {
        return isBreakable;
    }
}