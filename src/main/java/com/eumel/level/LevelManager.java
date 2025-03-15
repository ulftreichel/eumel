package com.eumel.level;

import com.eumel.JumpAndRun;
import com.eumel.level.design.*;
import com.eumel.opponent.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private final JumpAndRun jumpAndRun;
    private LevelDesign[] levels;
    private int currentLevel;
    private int currentDesignIndex;
    private final int MAX_LEVELS = 10;

    // Spielobjekte (werden von GameEngine übernommen)
    private List<BreakablePlatform> platforms;
    private List<Rectangle> groundSegments;
    private List<Diamond> diamonds;
    private List<Enemy> enemies;
    private List<ImageView> groundTiles;
    private List<ImageView> platformViews;
    private List<Powerup> powerups;
    private List<TreasureChest> treasureChests;
    private List<MiniBoss> miniBosses;
    private Boss boss;
    private Image groundTileImage;
    private Image holeTileImage;
    private ImageView tunnelLeft;
    private ImageView tunnelRight;
    private Text levelText;
    private Rectangle bossHealthBar;
    private double bossHealthBarInitialX;

    public LevelManager(JumpAndRun jumpAndRun) {
        this.jumpAndRun = jumpAndRun;
        this.levels = new LevelDesign[MAX_LEVELS];
        this.currentLevel = 0;
        this.currentDesignIndex = 0;
        this.platforms = new ArrayList<>();
        this.groundSegments = new ArrayList<>();
        this.diamonds = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.groundTiles = new ArrayList<>();
        this.platformViews = new ArrayList<>();
        this.powerups = new ArrayList<>();
        this.treasureChests = new ArrayList<>();
        this.miniBosses = new ArrayList<>();
        this.bossHealthBarInitialX = 300;
    }

    public void initializeLevels(int startLevel, int startDesign) {
        this.currentLevel = startLevel;
        this.currentDesignIndex = startDesign;

        if (JumpAndRun.debugMode) {
            levels[0] = new Level1();
            currentLevel = 0;
            levels[currentLevel].resetDesign();
            currentDesignIndex = 0;
            System.out.println("Level1 initialisiert (Debug-Modus)");
        } else {
            levels[0] = new Level1();
            levels[1] = new Level2();
            levels[2] = new Level3();
            levels[3] = new Level4();
            levels[4] = new Level5();
            levels[5] = new Level6();
            levels[6] = new Level7();
            levels[7] = new Level8();
            levels[8] = new Level9();
            levels[9] = new Level10();

            if (startLevel == 0 && startDesign == 0) {
                currentLevel = 0;
                currentDesignIndex = 0;
                levels[currentLevel].resetDesign();
                System.out.println("Normales Spiel mit Level1 initialisiert.");
            } else {
                currentLevel = Math.min(startLevel, MAX_LEVELS - 1);
                levels[currentLevel].resetDesign();
                for (int i = 0; i < startDesign; i++) {
                    if (levels[currentLevel].hasNextDesign()) {
                        levels[currentLevel].nextDesign();
                    }
                }
                currentDesignIndex = startDesign;
                System.out.println("Level " + (currentLevel + 1) + ", Design " + (currentDesignIndex + 1) + " initialisiert.");
            }
        }
    }

    public void loadLevel(Pane root, boolean useDebug) {
        root.getChildren().clear();
        platforms.clear();
        groundSegments.clear();
        diamonds.clear();
        enemies.clear();
        groundTiles.clear();
        platformViews.clear();

        // UI-Elemente wiederherstellen
        if (jumpAndRun.getBackgroundView() != null) {
            root.getChildren().add(jumpAndRun.getBackgroundView());
            jumpAndRun.getBackgroundView().toBack();
        }
        if (jumpAndRun.getScoreLabel() != null) {
            root.getChildren().add(jumpAndRun.getScoreLabel());
        }
        if (jumpAndRun.getStartLevel() > 0 || jumpAndRun.getStartDesign() > 0) {
            root.getChildren().add(jumpAndRun.getMousePositionLabel());
        }

        // Bodenbilder laden
        try {
            groundTileImage = new Image(getClass().getResource("/images/level/ground_tile.png").toExternalForm());
            holeTileImage = new Image(getClass().getResource("/images/level/hole_tile.png").toExternalForm());
        } catch (Exception e) {
            System.out.println("Fehler beim Laden des Bodenbildes: " + e.getMessage());
            groundTileImage = null;
            holeTileImage = null;
        }

        if (groundTileImage == null) {
            System.err.println("Fehler: groundTileImage ist null.");
            return;
        }

        // Bodenfliesen hinzufügen
        int tileWidth = (int) groundTileImage.getWidth();
        int tileHeight = (int) groundTileImage.getHeight();
        int numTiles = (int) Math.ceil(800.0 / tileWidth);
        List<Integer> holePositions = levels[currentLevel].getHolePositions();
        for (int i = 0; i < numTiles; i++) {
            ImageView groundTile = new ImageView(holePositions.contains(i) && holeTileImage != null ? holeTileImage : groundTileImage);
            groundTile.setFitWidth(tileWidth);
            groundTile.setFitHeight(tileHeight);
            groundTile.setX(i * tileWidth);
            groundTile.setY(460);
            root.getChildren().add(groundTile);
            groundTiles.add(groundTile);
        }

        // Wände, Plattformen, Gegner, etc. laden
        for (Wall wall : levels[currentLevel].getWalls()) {
            wall.render(root);
        }
        for (PlatformInfo platformInfo : levels[currentLevel].getPlatforms()) {
            ImageView platformView = new ImageView(new Image(getClass().getResource("/images/level/plattform.png").toExternalForm()));
            platformView.setFitWidth(platformInfo.getWidth());
            platformView.setFitHeight(platformInfo.getHeight());
            platformView.setX(platformInfo.getX());
            platformView.setY(platformInfo.getY());
            BreakablePlatform breakablePlatform = new BreakablePlatform(
                    platformInfo.getX(), platformInfo.getY(), platformInfo.getWidth(), platformInfo.getHeight(),
                    platformInfo.isBreakable(), platformView
            );
            platforms.add(breakablePlatform);
            root.getChildren().add(platformView);
            platformViews.add(platformView);
        }
        for (Rectangle ground : levels[currentLevel].getGroundSegments()) {
            groundSegments.add(ground);
            root.getChildren().add(ground);
        }
        for (Diamond diamond : levels[currentLevel].getDiamonds()) {
            diamonds.add(diamond);
            root.getChildren().add(diamond);
        }
        for (Enemy enemy : levels[currentLevel].getEnemies()) {
            enemies.add(enemy);
            root.getChildren().add(enemy.getImageView());
            enemy.getImageView().setX(enemy.getX());
            enemy.getImageView().setY(enemy.getY());
        }

        // Powerups, Schatztruhen und Minibosse
        powerups = levels[currentLevel].getPowerups();
        powerups.forEach(powerup -> root.getChildren().add(powerup.getImageView()));
        treasureChests = levels[currentLevel].getTreasureChests();
        treasureChests.forEach(chest -> root.getChildren().add(chest.getImageView()));
        miniBosses = levels[currentLevel].getMiniBosses();
        miniBosses.forEach(miniBoss -> root.getChildren().add(miniBoss.getImageView()));

        // Boss laden
        List<Boss> bosses = levels[currentLevel].getBosses();
        boss = bosses.isEmpty() ? null : new Boss(bosses.get(0).getX(), bosses.get(0).getY(), bosses.get(0).getHealth(), null);
        if (boss != null) {
            root.getChildren().add(boss.getImageView());
            bossHealthBar = new Rectangle(bossHealthBarInitialX, 50, 400, 20);
            bossHealthBar.setFill(Color.RED);
            bossHealthBar.setStroke(Color.BLACK);
            root.getChildren().add(bossHealthBar);
        }

        // Tunneleingang
        if (!levels[currentLevel].hasNextDesign()) {
            tunnelLeft = new ImageView(new Image(getClass().getResource("/images/level/tunnel_entrance_left.png").toExternalForm()));
            tunnelLeft.setFitWidth(332);
            tunnelLeft.setFitHeight(444);
            tunnelLeft.setX(550);
            tunnelLeft.setY(95);
            tunnelLeft.setVisible(false);

            tunnelRight = new ImageView(new Image(getClass().getResource("/images/level/tunnel_entrance_right.png").toExternalForm()));
            tunnelRight.setFitWidth(332);
            tunnelRight.setFitHeight(444);
            tunnelRight.setX(550);
            tunnelRight.setY(95);
            tunnelRight.setVisible(false);

            levelText = new Text("LEVEL " + (currentLevel + 2) + " ->");
            levelText.setFont(JumpAndRun.customFont != null ? JumpAndRun.customFont : javafx.scene.text.Font.font("Arial", 80));
            levelText.setFill(Color.WHITE);
            levelText.setStroke(Color.BLACK);
            levelText.setStrokeWidth(1);
            levelText.setX(200);
            levelText.setY(200);
            levelText.setVisible(false);

            root.getChildren().addAll(tunnelLeft, levelText, tunnelRight);
        }

        System.out.println("Level geladen (Design " + (currentDesignIndex + 1) + " von Level " + (currentLevel + 1) + ")");
    }

    public void nextDesignOrLevel() {
        if (levels[currentLevel].hasNextDesign()) {
            levels[currentLevel].nextDesign();
            currentDesignIndex++;
        } else if (currentLevel < MAX_LEVELS - 1) {
            currentLevel++;
            currentDesignIndex = 0;
            levels[currentLevel].resetDesign();
        }
    }

    // Getter für GameEngine
    public LevelDesign getCurrentLevelDesign() {
        return levels[currentLevel];
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrentDesignIndex() {
        return currentDesignIndex;
    }

    public List<BreakablePlatform> getPlatforms() {
        return platforms;
    }

    public List<Rectangle> getGroundSegments() {
        return groundSegments;
    }

    public List<Diamond> getDiamonds() {
        return diamonds;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<ImageView> getGroundTiles() {
        return groundTiles;
    }

    public List<ImageView> getPlatformViews() {
        return platformViews;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }

    public List<TreasureChest> getTreasureChests() {
        return treasureChests;
    }

    public List<MiniBoss> getMiniBosses() {
        return miniBosses;
    }

    public Boss getBoss() {
        return boss;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public Image getGroundTileImage() {
        return groundTileImage;
    }

    public Image getHoleTileImage() {
        return holeTileImage;
    }

    public ImageView getTunnelLeft() {
        return tunnelLeft;
    }

    public ImageView getTunnelRight() {
        return tunnelRight;
    }

    public Text getLevelText() {
        return levelText;
    }

    public Rectangle getBossHealthBar() {
        return bossHealthBar;
    }

    public void setBossHealthBar(Rectangle bossHealthBar) {
        this.bossHealthBar = bossHealthBar;
    }

    public double getBossHealthBarInitialX() {
        return bossHealthBarInitialX;
    }

    public void setBossHealthBarInitialX(double bossHealthBarInitialX) {
        this.bossHealthBarInitialX = bossHealthBarInitialX;
    }
}