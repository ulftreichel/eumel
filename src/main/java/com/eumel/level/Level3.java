package com.eumel.level;

import com.eumel.level.design.*;
import com.eumel.opponent.Boss;
import com.eumel.opponent.Enemy;
import com.eumel.opponent.MiniBoss;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level3 extends LevelDesign {
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

    public Level3() {
        platformImage = new Image(getClass().getResource("/images/level/plattform.png").toExternalForm());
        if (platformImage == null) {
            throw new RuntimeException("Fehler beim Laden des Bildes plattform.png");
        }

        // Initialisiere die 15 Designs
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
        platformDesigns.get(0).add(new PlatformInfo(223, 300, 40, 30, false)); // Nicht bruchbar
        platformDesigns.get(0).add(new PlatformInfo(520, 350, 30, 30, true));  // Bruchbar
        holeDesigns.get(0).addAll(Arrays.asList(2, 3, 6));
        diamondDesigns.get(0).add(new Diamond(235, 270));
        diamondDesigns.get(0).add(new Diamond(527, 320));
        enemyDesigns.get(0).add(new Enemy(316, 393, 60, 72, 0.75, 120, 1, true, 200, false, 0, 0,0));
        enemyDesigns.get(0).add(new Enemy(555, 393, 60, 72, 1, 200, 3, true, 200, true, 100, 100, 150));

        // Design 1
        platformDesigns.get(1).add(new PlatformInfo(324, 320, 160, 20, false)); // Nicht bruchbar
        holeDesigns.get(1).addAll(Arrays.asList(2, 3, 6));
        diamondDesigns.get(1).add(new Diamond(330, 280));
        diamondDesigns.get(1).add(new Diamond(394, 280));
        diamondDesigns.get(1).add(new Diamond(458, 280));
        enemyDesigns.get(1).add(new Enemy(310, 250, 60, 72, 0.5, 130, 3, true, 200, true, 50, 100, 150));

        // Design 2
        platformDesigns.get(2).add(new PlatformInfo(395, 190, 30, 20, true));  // Bruchbar
        platformDesigns.get(2).add(new PlatformInfo(515, 270, 30, 20, true)); // Nicht bruchbar
        holeDesigns.get(2).addAll(Arrays.asList(1, 2, 4, 5, 7, 8));
        diamondDesigns.get(2).add(new Diamond(275, 420));
        diamondDesigns.get(2).add(new Diamond(400, 150));
        diamondDesigns.get(2).add(new Diamond(515, 420));
        enemyDesigns.get(2).add(new Enemy(225, 393, 60, 72, 0.4, 60, 4, true, 200, true, 50, 100, 150)); // ,1000
        enemyDesigns.get(2).add(new Enemy(475, 393, 60, 72, 0.4, 55, 6, true, 200, true, 50, 100, 150));

        // Design 3
        platformDesigns.get(3).add(new PlatformInfo(280, 220, 10, 20, true));
        platformDesigns.get(3).add(new PlatformInfo(445, 220, 10, 20, true));
        holeDesigns.get(3).addAll(Arrays.asList(1, 3, 5, 7));
        diamondDesigns.get(3).add(new Diamond(275, 190));
        diamondDesigns.get(3).add(new Diamond(440, 190));
        enemyDesigns.get(3).add(new Enemy(150, 393, 60, 72, 0.35, 45, 5, true, 200, true, 50, 100, 150));
        enemyDesigns.get(3).add(new Enemy(310, 393, 60, 72, 0.40, 50, 3, true, 200, true, 50, 100, 150));
        enemyDesigns.get(3).add(new Enemy(465, 395, 60, 72, 0.35, 50, 2, true, 200, true, 50, 100, 150));

        // Design 4
        platformDesigns.get(4).add(new PlatformInfo(160, 330, 81, 20, true));
        platformDesigns.get(4).add(new PlatformInfo(360, 200, 81, 20, true));
        platformDesigns.get(4).add(new PlatformInfo(570, 330, 81, 20, true));
        holeDesigns.get(4).addAll(Arrays.asList(1,2,3,4,5,6,7,8,9));
        diamondDesigns.get(4).add(new Diamond(190, 300));
        diamondDesigns.get(4).add(new Diamond(390, 170));
        diamondDesigns.get(4).add(new Diamond(600, 300));
        enemyDesigns.get(4).add(new Enemy(150, 260, 60, 72, 0.22, 50, 1, true, 200, true, 50, 100, 150));
        enemyDesigns.get(4).add(new Enemy(350, 128, 60, 72, 0.23, 50, 6, false, 200, true, 50, 100, 150));
        enemyDesigns.get(4).add(new Enemy(560, 262, 60, 72, 0.21, 50, 2, true, 200, true, 50, 100, 150));

        // Design 5
        platformDesigns.get(5).add(new PlatformInfo(200, 430, 81, 20, false));
        platformDesigns.get(5).add(new PlatformInfo(350, 290, 81, 20, true));
        platformDesigns.get(5).add(new PlatformInfo(500, 370, 81, 20, false));
        holeDesigns.get(5).addAll(Arrays.asList(3, 7));
        diamondDesigns.get(5).add(new Diamond(220, 410));
        diamondDesigns.get(5).add(new Diamond(370, 270));
        enemyDesigns.get(5).add(new Enemy(300, 480, 20, 20, 2.5, 50, 1, false, 200, true, 50, 100, 150));

        // Design 6
        platformDesigns.get(6).add(new PlatformInfo(240, 410, 81, 20, false));
        platformDesigns.get(6).add(new PlatformInfo(390, 300, 81, 20, true));
        platformDesigns.get(6).add(new PlatformInfo(540, 340, 81, 20, false));
        holeDesigns.get(6).addAll(Arrays.asList(0, 4));
        diamondDesigns.get(6).add(new Diamond(260, 390));
        diamondDesigns.get(6).add(new Diamond(410, 280));
        enemyDesigns.get(6).add(new Enemy(350, 500, 20, 20, 3, 30, 2, false, 200, true, 50, 100, 150));

        // Design 7
        platformDesigns.get(7).add(new PlatformInfo(160, 390, 81, 20, true));
        platformDesigns.get(7).add(new PlatformInfo(320, 320, 81, 20, false));
        platformDesigns.get(7).add(new PlatformInfo(460, 360, 81, 20, false));
        holeDesigns.get(7).addAll(Arrays.asList(0, 4));
        diamondDesigns.get(7).add(new Diamond(180, 370));
        diamondDesigns.get(7).add(new Diamond(340, 300));
        enemyDesigns.get(7).add(new Enemy(200, 480, 20, 20, 2, 60, 3, false, 200, true, 50, 100, 150));

        // Design 8
        platformDesigns.get(8).add(new PlatformInfo(200, 200, 81, 20, false));

        // Design 9
        platformDesigns.get(9).add(new PlatformInfo(220, 420, 81, 20, true));
        platformDesigns.get(9).add(new PlatformInfo(380, 280, 81, 20, false));
        platformDesigns.get(9).add(new PlatformInfo(520, 360, 81, 20, false));
        holeDesigns.get(9).addAll(Arrays.asList(2, 8));
        diamondDesigns.get(9).add(new Diamond(240, 400));
        diamondDesigns.get(9).add(new Diamond(400, 260));
        enemyDesigns.get(9).add(new Enemy(250, 480, 20, 20, 3, 50, 5, false, 200, true, 50, 100, 150));

        // Design 10
        platformDesigns.get(10).add(new PlatformInfo(180, 400, 81, 20, false));
        platformDesigns.get(10).add(new PlatformInfo(340, 310, 81, 20, true));
        platformDesigns.get(10).add(new PlatformInfo(480, 350, 81, 20, false));
        holeDesigns.get(10).addAll(Arrays.asList(1, 6));
        diamondDesigns.get(10).add(new Diamond(200, 380));
        diamondDesigns.get(10).add(new Diamond(360, 290));
        enemyDesigns.get(10).add(new Enemy(300, 500, 20, 20, 2, 40, 6, false, 200, true, 50, 100, 150));

        // Design 11
        platformDesigns.get(11).add(new PlatformInfo(200, 430, 81, 20, false));
        platformDesigns.get(11).add(new PlatformInfo(350, 290, 81, 20, true));
        platformDesigns.get(11).add(new PlatformInfo(500, 370, 81, 20, false));
        holeDesigns.get(11).addAll(Arrays.asList(1, 6));
        diamondDesigns.get(11).add(new Diamond(220, 410));
        diamondDesigns.get(11).add(new Diamond(370, 270));
        enemyDesigns.get(11).add(new Enemy(350, 480, 20, 20, 2.5, 50, 1, false, 200, true, 50, 100, 150));

        // Design 12
        platformDesigns.get(12).add(new PlatformInfo(240, 410, 81, 20, true));
        platformDesigns.get(12).add(new PlatformInfo(390, 300, 81, 20, false));
        platformDesigns.get(12).add(new PlatformInfo(540, 340, 81, 20, false));
        holeDesigns.get(12).addAll(Arrays.asList(3, 5));
        diamondDesigns.get(12).add(new Diamond(260, 390));
        diamondDesigns.get(12).add(new Diamond(410, 280));
        enemyDesigns.get(12).add(new Enemy(400, 500, 20, 20, 3, 30, 2, false, 200, true, 50, 100, 150));

        // Design 13
        platformDesigns.get(13).add(new PlatformInfo(160, 390, 81, 20, false));
        platformDesigns.get(13).add(new PlatformInfo(320, 320, 81, 20, true));
        platformDesigns.get(13).add(new PlatformInfo(460, 360, 81, 20, false));
        holeDesigns.get(13).addAll(Arrays.asList(3, 5));
        diamondDesigns.get(13).add(new Diamond(180, 370));
        diamondDesigns.get(13).add(new Diamond(340, 300));
        enemyDesigns.get(13).add(new Enemy(200, 480, 20, 20, 2, 60, 3, false, 200, true, 50, 100, 150));

        // Design 14
        holeDesigns.get(14).addAll(Arrays.asList(3, 5));
        enemyDesigns.get(14).add(new Enemy(650, 500, 20, 20, 3, 60, 4, false, 200, true, 50, 100, 150));
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