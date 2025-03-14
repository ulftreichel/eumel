package com.eumel.level.design;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Wall {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Image wallImage;
    private final Image platformImage;

    public Wall(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.wallImage = new Image(getClass().getResource("/images/level/wall.png").toExternalForm());
        this.platformImage = new Image(getClass().getResource("/images/level/plattform.png").toExternalForm());
        if (wallImage == null) {
            throw new RuntimeException("Fehler beim Laden des Bildes wall.png");
        }
        if (platformImage == null) {
            throw new RuntimeException("Fehler beim Laden des Bildes plattform.png");
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void render(Pane pane) {
        int tileWidth = 81;
        int tileHeight = 81;
        int numTilesX = (int) Math.ceil(width / tileWidth);
        int numTilesY = (int) Math.ceil(height / tileHeight);

        for (int i = 0; i < numTilesX; i++) {
            for (int j = 0; j < numTilesY; j++) {
                ImageView tile;
                if (j == 0) {
                    tile = new ImageView(platformImage);
                } else {
                    tile = new ImageView(wallImage);
                }
                tile.setFitWidth(tileWidth);
                tile.setFitHeight(tileHeight);
                tile.setX(x + i * tileWidth);
                tile.setY(y + j * tileHeight);
                pane.getChildren().add(tile);
            }
        }
    }
}