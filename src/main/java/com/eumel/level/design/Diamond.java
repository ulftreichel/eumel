package com.eumel.level.design;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Diamond extends ImageView {
    private static final Image DIAMOND_IMAGE;
    private static final double WIDTH = 20; // Feste Breite
    private static final double HEIGHT = 20; // Feste Höhe
    private final Rectangle collisionBox;

    // Statischer Initialisierungsblock, um das Bild zu laden
    static {
        try {
            DIAMOND_IMAGE = new Image(Objects.requireNonNull(Diamond.class.getResource("/images/level/diamond.png")).toExternalForm());
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Laden des Bildes diamond.png: " + e.getMessage());
        }
    }

    public Diamond(double x, double y) {
        super(DIAMOND_IMAGE);
        setFitWidth(WIDTH);
        setFitHeight(HEIGHT);
        setX(x);
        setY(y);

        collisionBox = new Rectangle(x, y, WIDTH, HEIGHT);
        collisionBox.setVisible(false);

        layoutXProperty().addListener((obs, oldVal, newVal) -> collisionBox.setX(newVal.doubleValue()));
        layoutYProperty().addListener((obs, oldVal, newVal) -> collisionBox.setY(newVal.doubleValue()));
    }

    public Rectangle getCollisionBox() {
        return collisionBox;
    }
}