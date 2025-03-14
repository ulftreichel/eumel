package com.eumel.level;

import com.eumel.level.design.*;
import com.eumel.opponent.Boss;
import com.eumel.opponent.Enemy;
import com.eumel.opponent.MiniBoss;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level1 extends LevelDesign {
    private final List<List<PlatformInfo>> platformDesigns = new ArrayList<>();
    private final List<List<Integer>> holeDesigns = new ArrayList<>();
    private final List<List<Diamond>> diamondDesigns = new ArrayList<>();
    private final List<List<Enemy>> enemyDesigns = new ArrayList<>();
    private final List<List<Wall>> wallDesigns = new ArrayList<>();
    private final List<List<Powerup>> powerupDesigns = new ArrayList<>();
    private final List<List<TreasureChest>> treasureChestDesigns = new ArrayList<>();
    private final List<List<MiniBoss>> miniBossDesigns = new ArrayList<>();
    private final List<List<Boss>> bossDesigns = new ArrayList<>();
    private int currentDesignIndex = 0;
    private final Image platformImage;

    public Level1() {
        platformImage = new Image(getClass().getResource("/images/level/plattform.png").toExternalForm());
        if (platformImage == null) {
            throw new RuntimeException("Fehler beim Laden des Bildes plattform.png");
        }

        // Initialisiere die 15 Designs für alle Listen
        for (int i = 0; i < 15; i++) {
            platformDesigns.add(new ArrayList<>());
            holeDesigns.add(new ArrayList<>());
            diamondDesigns.add(new ArrayList<>());
            enemyDesigns.add(new ArrayList<>());
            wallDesigns.add(new ArrayList<>());
            powerupDesigns.add(new ArrayList<>());
            treasureChestDesigns.add(new ArrayList<>());
            miniBossDesigns.add(new ArrayList<>());
            bossDesigns.add(new ArrayList<>());
        }

        // Design 0
        platformDesigns.get(0).add(new PlatformInfo(223, 300, 40, 30, false));
        platformDesigns.get(0).add(new PlatformInfo(520, 350, 30, 30, true));
        holeDesigns.get(0).addAll(Arrays.asList(2, 3, 6));
        diamondDesigns.get(0).add(new Diamond(235, 270));
        diamondDesigns.get(0).add(new Diamond(527, 320));
        enemyDesigns.get(0).add(new Enemy(316, 393, 60, 72, 0.75, 120, 1, true, 200, false, 0, 0, 0));
        enemyDesigns.get(0).add(new Enemy(555, 393, 60, 72, 1, 200, 3, true, 200, true, 100, 300, 400));


        // Design 1
        holeDesigns.get(1).addAll(Arrays.asList(2, 3, 6));
        diamondDesigns.get(1).add(new Diamond(330, 280));
        diamondDesigns.get(1).add(new Diamond(394, 280));
        diamondDesigns.get(1).add(new Diamond(458, 280));
        enemyDesigns.get(1).add(new Enemy(310, 250, 60, 72, 0.5, 130, 3, true, 200, true, 50, 100, 150));
        wallDesigns.get(1).add(new Wall(324, 320, 160, 280));
        powerupDesigns.get(1).add(new Powerup(394, 150, Powerup.PowerupType.DAMAGE_BOOST));

        // Design 2
        platformDesigns.get(2).add(new PlatformInfo(395, 190, 30, 20, true));
        platformDesigns.get(2).add(new PlatformInfo(515, 270, 30, 20, true));
        holeDesigns.get(2).addAll(Arrays.asList(1, 2, 4, 5, 7, 8));
        diamondDesigns.get(2).add(new Diamond(275, 420));
        diamondDesigns.get(2).add(new Diamond(400, 150));
        diamondDesigns.get(2).add(new Diamond(515, 420));
        enemyDesigns.get(2).add(new Enemy(225, 393, 60, 72, 0.4, 60, 4, true, 200, true, 50, 100, 150));
        enemyDesigns.get(2).add(new Enemy(475, 393, 60, 72, 0.4, 55, 6, true, 200, true, 50, 100, 150));

        // Design 3
        platformDesigns.get(3).add(new PlatformInfo(273, 287, 25, 20, true));
        platformDesigns.get(3).add(new PlatformInfo(438, 287, 25, 20, true));
        holeDesigns.get(3).addAll(Arrays.asList(1, 3, 5, 7));
        diamondDesigns.get(3).add(new Diamond(275, 190));
        diamondDesigns.get(3).add(new Diamond(440, 190));
        enemyDesigns.get(3).add(new Enemy(150, 393, 60, 72, 0.35, 45, 5, true, 200, false, 50, 100, 150));
        enemyDesigns.get(3).add(new Enemy(310, 393, 60, 72, 0.40, 50, 3, true, 200, true, 100, 250, 400));
        enemyDesigns.get(3).add(new Enemy(465, 395, 60, 72, 0.35, 50, 2, true, 200, true, 100, 300, 450));

        // Design 4
        platformDesigns.get(4).add(new PlatformInfo(160, 330, 81, 20, true));
        platformDesigns.get(4).add(new PlatformInfo(360, 200, 81, 20, true));
        platformDesigns.get(4).add(new PlatformInfo(570, 330, 81, 20, true));
        holeDesigns.get(4).addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        diamondDesigns.get(4).add(new Diamond(190, 300));
        diamondDesigns.get(4).add(new Diamond(390, 170));
        diamondDesigns.get(4).add(new Diamond(600, 300));
        enemyDesigns.get(4).add(new Enemy(150, 260, 60, 72, 0.22, 50, 1, true, 200, true, 50, 100, 150));
        enemyDesigns.get(4).add(new Enemy(350, 128, 60, 72, 0.23, 50, 6, false, 200, true, 50, 100, 150));
        enemyDesigns.get(4).add(new Enemy(560, 262, 60, 72, 0.21, 50, 2, true, 200, true, 50, 100, 150));

        // Design 5
        platformDesigns.get(5).add(new PlatformInfo(280, 305, 81, 20, false));
        holeDesigns.get(5).addAll(Arrays.asList(1, 3, 7, 8));
        diamondDesigns.get(5).add(new Diamond(220, 410));
        diamondDesigns.get(5).add(new Diamond(370, 270));
        enemyDesigns.get(5).add(new Enemy(150, 393, 60, 72, 0.3, 50, 1, true, 750, true, 50, 200, 200));
        wallDesigns.get(5).add(new Wall(324, 130, 240, 450));

        // Design 6
        platformDesigns.get(6).add(new PlatformInfo(130, 290, 81, 20, true));
        platformDesigns.get(6).add(new PlatformInfo(395, 343, 250, 20, false));
        treasureChestDesigns.get(6).add(new TreasureChest(580,295));
        holeDesigns.get(6).addAll(Arrays.asList(4));
        diamondDesigns.get(6).add(new Diamond(410, 280));
        diamondDesigns.get(6).add(new Diamond(450, 280));
        diamondDesigns.get(6).add(new Diamond(490, 280));
        diamondDesigns.get(6).add(new Diamond(530, 280));
        diamondDesigns.get(6).add(new Diamond(570, 280));
        diamondDesigns.get(6).add(new Diamond(610, 280));
        enemyDesigns.get(6).add(new Enemy(350, 500, 20, 20, 3, 30, 2, false, 200, true, 50, 100, 150));
        wallDesigns.get(6).add(new Wall(162, 120, 160, 450));
        wallDesigns.get(6).add(new Wall(320, 120, 404, 80));
        wallDesigns.get(6).add(new Wall(644, 120, 80, 230));

        // Design 7
        platformDesigns.get(7).add(new PlatformInfo(200, 400, 50, 20, true));
        platformDesigns.get(7).add(new PlatformInfo(300, 300, 50, 20, true));
        platformDesigns.get(7).add(new PlatformInfo(400, 200, 50, 20, true));
        platformDesigns.get(7).add(new PlatformInfo(580, 148, 50, 20, true));
        holeDesigns.get(7).addAll(Arrays.asList(2, 4));
        diamondDesigns.get(7).add(new Diamond(210, 370));
        diamondDesigns.get(7).add(new Diamond(310, 270));
        diamondDesigns.get(7).add(new Diamond(410, 170));
        enemyDesigns.get(7).add(new Enemy(390, 393, 60, 72, 0.8, 350, 6, true, 250, false, 100, 400, 250));
        enemyDesigns.get(7).add(new Enemy(390, 393, 60, 72, 0.5, 350, 3, true, 200, true, 200, 400, 250));
        treasureChestDesigns.get(7).add(new TreasureChest(580, 100));
        //miniBoss muss noch überarbeitet werden
        //miniBossDesigns.get(7).add(new MiniBoss(500, 250, 2000));

        // Design 8
        platformDesigns.get(8).add(new PlatformInfo(150, 350, 50, 20, true));
        platformDesigns.get(8).add(new PlatformInfo(350, 250, 50, 20, false));
        platformDesigns.get(8).add(new PlatformInfo(550, 300, 50, 20, true));
        holeDesigns.get(8).addAll(Arrays.asList(1, 3, 6, 8));
        diamondDesigns.get(8).add(new Diamond(160, 320));
        diamondDesigns.get(8).add(new Diamond(360, 220));
        enemyDesigns.get(8).add(new Enemy(310, 393, 60, 72, 0.3, 135, 2, true, 350, true, 350, 100, 150));
        powerupDesigns.get(8).add(new Powerup(560, 270, Powerup.PowerupType.DAMAGE_BOOST));

        // Design 9
        platformDesigns.get(9).add(new PlatformInfo(200, 350, 50, 20, true));
        platformDesigns.get(9).add(new PlatformInfo(400, 300, 50, 20, false));
        platformDesigns.get(9).add(new PlatformInfo(600, 350, 50, 20, true));
        holeDesigns.get(9).addAll(Arrays.asList(1, 2, 3, 4));
        diamondDesigns.get(9).add(new Diamond(215, 320));
        diamondDesigns.get(9).add(new Diamond(615, 320));
        enemyDesigns.get(9).add(new Enemy(390, 393, 60, 72, 0.9, 350, 1, true, 400, true, 175, 100, 150));
        treasureChestDesigns.get(9).add(new TreasureChest(400, 250));


        // Design 10
        platformDesigns.get(10).add(new PlatformInfo(130, 300, 50, 20, true));
        platformDesigns.get(10).add(new PlatformInfo(460, 200, 50, 20, false));
        platformDesigns.get(10).add(new PlatformInfo(590, 300, 50, 20, true));
        diamondDesigns.get(10).add(new Diamond(190, 95));
        enemyDesigns.get(10).add(new Enemy(235, 393, 60, 72, 0.4, 320, 5, true, 800, true, 200, 100, 150));
        enemyDesigns.get(10).add(new Enemy(235, 393, 60, 72, 0.3, 320, 2, true, 850, true, 100, 200, 250));
        powerupDesigns.get(10).add(new Powerup(480, 165, Powerup.PowerupType.SPEED_BOOST));
        treasureChestDesigns.get(10).add(new TreasureChest(635, 75));
        wallDesigns.get(10).add(new Wall(162, 130, 80, 450));
        wallDesigns.get(10).add(new Wall(400, -100, 80, 450));
        wallDesigns.get(10).add(new Wall(620, 130, 80, 450));

        // Design 11
        holeDesigns.get(11).addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        enemyDesigns.get(11).add(new Enemy(235, 243, 60, 72, 0.3, 100, 1, true, 1000, false, 0, 0, 0));
        enemyDesigns.get(11).add(new Enemy(420, 242, 60, 72, 0.3, 100, 3, true, 1500, true, 150, 100, 150));
        wallDesigns.get(11).add(new Wall(242, 310, 300, 300));
        treasureChestDesigns.get(11).add(new TreasureChest(376, 260));

        // Design 12
        platformDesigns.get(12).add(new PlatformInfo(0, 170, 50, 20, true));
        platformDesigns.get(12).add(new PlatformInfo(580, 350, 50, 20, true));
        enemyDesigns.get(12).add(new Enemy(10, 238, 60, 72, 0.5, 430, 2, true, 3200, true, 200, 700, 800));
        treasureChestDesigns.get(12).add(new TreasureChest(25, 254));
        treasureChestDesigns.get(12).add(new TreasureChest(376, 73));
        wallDesigns.get(12).add(new Wall(0, 300, 485, 80));
        wallDesigns.get(12).add(new Wall(120, 120, 500, 80));
        wallDesigns.get(12).add(new Wall(620, 120, 80, 450));

        // Design 13
        platformDesigns.get(13).add(new PlatformInfo(200, 400, 50, 20, true));
        platformDesigns.get(13).add(new PlatformInfo(300, 300, 50, 20, true));
        platformDesigns.get(13).add(new PlatformInfo(400, 200, 50, 20, true));
        holeDesigns.get(13).addAll(Arrays.asList(2, 4));
        diamondDesigns.get(13).add(new Diamond(210, 370));
        diamondDesigns.get(13).add(new Diamond(310, 270));
        diamondDesigns.get(13).add(new Diamond(410, 170));
        enemyDesigns.get(13).add(new Enemy(250, 393, 60, 72, 0.8, 120, 1, true, 200, true, 50, 100, 150));

        // Design 14
        bossDesigns.get(14).add(new Boss(500, 295, 5000));

    }

    @Override
    public List<PlatformInfo> getPlatforms() {
        return platformDesigns.get(currentDesignIndex);
    }

    @Override
    public List<Diamond> getDiamonds() {
        return diamondDesigns.get(currentDesignIndex);
    }

    @Override
    public List<Enemy> getEnemies() {
        return enemyDesigns.get(currentDesignIndex);
    }

    @Override
    public List<Integer> getHolePositions() {
        return holeDesigns.get(currentDesignIndex);
    }

    @Override
    public List<Wall> getWalls() {
        return wallDesigns.get(currentDesignIndex);
    }

    @Override
    public List<Powerup> getPowerups() { return powerupDesigns.get(currentDesignIndex); }

    @Override
    public List<TreasureChest> getTreasureChests() { return treasureChestDesigns.get(currentDesignIndex); }

    @Override
    public List<MiniBoss> getMiniBosses() { return miniBossDesigns.get(currentDesignIndex); }

    @Override
    public List<Boss> getBosses() { return bossDesigns.get(currentDesignIndex); }

    @Override
    public void nextDesign() {
        if (currentDesignIndex < platformDesigns.size() - 1) {
            currentDesignIndex++;
        }
    }

    @Override
    public void resetDesign() {
        currentDesignIndex = 0;
    }

    @Override
    public boolean hasNextDesign() {
        return currentDesignIndex < platformDesigns.size() - 1;
    }

    @Override
    public int getCurrentDesignIndex() {
        return currentDesignIndex;
    }
}