package com.eumel.level.design;

import com.eumel.opponent.Boss;
import com.eumel.opponent.Enemy;
import com.eumel.opponent.MiniBoss;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public abstract class LevelDesign {
    protected int currentDesignIndex;
    protected List<List<PlatformInfo>> platformDesigns;
    protected List<List<Integer>> holeDesigns;
    protected List<List<Diamond>> diamondDesigns;
    protected List<List<Enemy>> enemyDesigns;
    protected List<List<Wall>> wallDesigns;
    protected List<List<MiniBoss>> miniBossDesigns;
    protected List<List<Boss>> bossDesigns;
    protected List<List<Powerup>> powerupDesigns;
    protected List<List<TreasureChest>> treasureChestDesigns;

    public LevelDesign() {
        platformDesigns = new ArrayList<>();
        holeDesigns = new ArrayList<>();
        diamondDesigns = new ArrayList<>();
        enemyDesigns = new ArrayList<>();
        wallDesigns = new ArrayList<>();
        miniBossDesigns = new ArrayList<>();
        bossDesigns = new ArrayList<>();
        powerupDesigns = new ArrayList<>();
        treasureChestDesigns = new ArrayList<>();
        currentDesignIndex = 0;
    }

    public List<PlatformInfo> getPlatforms() {
        return platformDesigns.get(currentDesignIndex);
    }

    public List<Rectangle> getGroundSegments() {
        return new ArrayList<>();
    }

    public List<Diamond> getDiamonds() {
        return diamondDesigns.get(currentDesignIndex);
    }

    public List<Enemy> getEnemies() {
        return enemyDesigns.get(currentDesignIndex);
    }

    public List<Wall> getWalls() {
        return wallDesigns.get(currentDesignIndex);
    }

    public List<Integer> getHolePositions() {
        return holeDesigns.get(currentDesignIndex);
    }

    public List<MiniBoss> getMiniBosses() {
        return miniBossDesigns.get(currentDesignIndex);
    }

    public List<Boss> getBosses() {
        return bossDesigns.get(currentDesignIndex);
    }

    public List<Powerup> getPowerups() {
        return powerupDesigns.get(currentDesignIndex);
    }

    public List<TreasureChest> getTreasureChests() {
        return treasureChestDesigns.get(currentDesignIndex);
    }

    public void nextDesign() {
        if (hasNextDesign()) {
            currentDesignIndex++;
        }
    }

    public void resetDesign() {
        currentDesignIndex = 0;
    }

    public boolean hasNextDesign() {
        return currentDesignIndex + 1 < platformDesigns.size();
    }

    public int getCurrentDesignIndex() {
        return currentDesignIndex;
    }
}