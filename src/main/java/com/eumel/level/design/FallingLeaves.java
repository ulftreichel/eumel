package com.eumel.level.design;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FallingLeaves {
    private final Pane gamePane;
    private final List<ImageView> leaves = new ArrayList<>();
    private final Image leafImage;
    private final Random random = new Random();
    private final double sceneWidth;
    private final double sceneHeight;
    private final int maxLeaves = 10;
    private AnimationTimer leafTimer;

    public FallingLeaves(Pane gamePane, double sceneWidth, double sceneHeight) {
        this.gamePane = gamePane;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        try {
            leafImage = new Image(Objects.requireNonNull(getClass().getResource("/images/level/leaf.png")).toExternalForm());
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Laden des Blattbildes: " + e.getMessage());
        }
    }

    public void start() {
        if (leafTimer == null) {
            startFalling();
        } else {
            leafTimer.start();
        }
    }

    private void startFalling() {
        leafTimer = new AnimationTimer() {
            private long lastSpawnTime = 0;

            @Override
            public void handle(long now) {
                // Spawn neues Blatt
                // Ein Blatt pro Sekunde
                long spawnInterval = 1_000_000_000;
                if (now - lastSpawnTime >= spawnInterval && leaves.size() < maxLeaves) {
                    spawnLeaf();
                    lastSpawnTime = now;
                }

                List<ImageView> leavesToRemove = new ArrayList<>();
                for (ImageView leaf : leaves) {
                    double currentY = leaf.getY();
                    double currentX = leaf.getX();
                    double velocityY = leaf.getUserData() != null ? (double) leaf.getUserData() : 1.0; // Fallgeschwindigkeit
                    double wind = Math.sin(currentY / 50) * 1.5; // Simuliere Wind

                    leaf.setY(currentY + velocityY);
                    leaf.setX(currentX + wind);
                    leaf.setRotate(leaf.getRotate() + random.nextDouble(-2, 2)); // Leichte Rotation

                    // Entferne Blatt, wenn es den Boden erreicht
                    if (leaf.getY() > sceneHeight) {
                        leavesToRemove.add(leaf);
                    }
                }

                // Entferne Blätter die den Boden erreicht haben
                leaves.removeAll(leavesToRemove);
                gamePane.getChildren().removeAll(leavesToRemove);
            }
        };
        leafTimer.start();
    }

    private void spawnLeaf() {
        ImageView leaf = new ImageView(leafImage);
        leaf.setFitWidth(16);
        leaf.setFitHeight(16);
        leaf.setX(random.nextDouble(sceneWidth));
        leaf.setY(-16); // Starte oberhalb des Bildschirms
        leaf.setUserData(random.nextDouble(1.0, 2.0)); // Zufällige Fallgeschwindigkeit
        leaf.setRotate(random.nextDouble(360)); // Zufällige Startrotation

        leaves.add(leaf);
        gamePane.getChildren().add(leaf);
    }

    public void stop() {
        if (leafTimer != null) {
            leafTimer.stop();
        }
        gamePane.getChildren().removeAll(leaves);
        leaves.clear();
    }
}