package com.eumel.level;

import com.eumel.level.design.*;
import com.eumel.opponent.Boss;
import com.eumel.opponent.Enemy;
import com.eumel.opponent.MiniBoss;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Level2 extends LevelDesign {
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

    public Level2() {
        platformImage = new Image(getClass().getResource("/images/level/plattform.png").toExternalForm());
        if (platformImage == null) {
            throw new RuntimeException("Fehler beim Laden des Bildes plattform.png");
        }

        // Initialisiere nur 2 Designs zum Boss Test
        for (int i = 0; i < 2; i++) {
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
        diamondDesigns.get(0).add(new Diamond(235, 270));
        powerupDesigns.get(0).add(new Powerup(100, 415, Powerup.PowerupType.DAMAGE_BOOST));
        treasureChestDesigns.get(0).add(new TreasureChest(150, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(200, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(250, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(300, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(350, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(400, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(450, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(500, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(550, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(600, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(650, 415));
        treasureChestDesigns.get(0).add(new TreasureChest(700, 415));
        powerupDesigns.get(0).add(new Powerup(750, 425, Powerup.PowerupType.SPEED_BOOST));
        // Design 1
        bossDesigns.get(1).add(new Boss(500, 295, 100));
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