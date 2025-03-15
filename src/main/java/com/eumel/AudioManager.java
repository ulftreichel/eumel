package com.eumel;

import com.eumel.data.DBDAO;
import javafx.animation.AnimationTimer;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;
import java.util.Random;

public class AudioManager {
    private MediaPlayer menuPlayer;
    private MediaPlayer effectPlayer;
    private MediaPlayer bossPlayer;
    private MediaPlayer gamePlayer;
    private MediaPlayer introPlayer;
    private Random random = new Random();
    private long lastEffectTime = 0;
    private long nextEffectInterval = 0;
    private boolean isMenuActive = false;
    private boolean isBossFight = false;
    private boolean isIntro = false;
    private JumpAndRun game;
    private DBDAO dbDAO;
    private double gameVolumeMultiplier = 0.5;
    private double effectVolumeMultiplier = 1.0;
    private AnimationTimer effectTimer;

    public AudioManager(JumpAndRun game, DBDAO dbDAO) {
        this.game = game;
        this.dbDAO = dbDAO;
        initializeAudioFiles();
    }

    // Soundeffekte für das normale Spiel
    private final String[] gameEffectSounds = {
            "/sound/laught.mp3",
            "/sound/wolf.mp3",
            "/sound/witch.mp3",
            "/sound/witch2.mp3"
    };

    private void initializeAudioFiles() {
        try {
            String menuPath = Objects.requireNonNull(getClass().getResource("/sound/mainmenu.mp3")).toExternalForm();
            Media menuMedia = new Media(menuPath);
            menuPlayer = new MediaPlayer(menuMedia);
            menuPlayer.setVolume(dbDAO.getVolume());
            menuPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            String bossPath = Objects.requireNonNull(getClass().getResource("/sound/heartbeat.mp3")).toExternalForm();
            Media bossMedia = new Media(bossPath);
            bossPlayer = new MediaPlayer(bossMedia);
            bossPlayer.setVolume(dbDAO.getVolume());
            bossPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            String gamePath = Objects.requireNonNull(getClass().getResource("/sound/game.mp3")).toExternalForm();
            Media gameMedia = new Media(gamePath);
            gamePlayer = new MediaPlayer(gameMedia);
            gamePlayer.setVolume(dbDAO.getVolume());
            gamePlayer.setCycleCount(MediaPlayer.INDEFINITE);

            if (JumpAndRun.debugMusic) {
                System.out.println("Audio-Dateien initialisiert: mainmenu, heartbeat");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Audio-Dateien: " + e.getMessage());
        }

        nextEffectInterval = (10 + random.nextInt(16)) * 1_000_000_000L;
    }

    public void startMenuAudio() {
        stopAllAudio();
        isMenuActive = true;
        menuPlayer.setVolume(dbDAO.getVolume());
        menuPlayer.play();
        if (JumpAndRun.debugMusic) {
            System.out.println("Menü-Audio gestartet");
        }

        effectTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isMenuActive) {
                    stop();
                    return;
                }
                if (now - lastEffectTime >= nextEffectInterval) {
                    playEffect("/sound/wolf.mp3");
                    lastEffectTime = now;
                    nextEffectInterval = (20 + random.nextInt(16)) * 1_000_000_000L;
                }
            }
        };
        effectTimer.start();
    }

    public void startGameAudio(boolean isBossFight) {
        stopAllAudio();
        isMenuActive = false;
        this.isBossFight = isBossFight;

        menuPlayer.stop();

        gamePlayer.setVolume(dbDAO.getVolume() * gameVolumeMultiplier);
        gamePlayer.play();
        if (JumpAndRun.debugMusic) {
            System.out.println("Spiel-Audio gestartet mit MainMenu-Song (Lautstärke: " + (dbDAO.getVolume() * gameVolumeMultiplier) + ")");
        }

        if (isBossFight) {
            gamePlayer.stop();
            bossPlayer.setVolume(dbDAO.getVolume());
            bossPlayer.play();
            if (!JumpAndRun.debugMusic) {
                System.out.println("Boss-Audio gestartet");
            }
        } else {
            effectTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    if (now - lastEffectTime >= nextEffectInterval) {
                        int randomIndex = random.nextInt(gameEffectSounds.length);
                        playEffect(gameEffectSounds[randomIndex]);
                        lastEffectTime = now;
                        nextEffectInterval = (5 + random.nextInt(10)) * 1_000_000_000L;
                    }
                }
            };
            effectTimer.start();
        }
    }

    public void startIntroAudio(boolean isIntro){
        stopAllAudio();
        isMenuActive = false;
        this.isIntro = isIntro;
        menuPlayer.stop();
        gamePlayer.stop();
        if (JumpAndRun.debugMusic) {
            System.out.println("Intro-Audio gestartet");
        }
    }

    void playEffect(String path) {
        if (dbDAO.getVolume() == 0) {
            if (JumpAndRun.debugMusic) {
                System.out.println("Effekt nicht abgespielt (Lautstärke = 0): " + path);
            }
            return;
        }

        if (effectPlayer != null) {
            effectPlayer.stop();
        }
        try {
            Media effectMedia = new Media(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
            effectPlayer = new MediaPlayer(effectMedia);
            effectPlayer.setVolume(dbDAO.getVolume() * effectVolumeMultiplier);
            effectPlayer.play();
            if (JumpAndRun.debugMusic) {
                System.out.println("Effekt abgespielt: " + path + " (Lautstärke: " + (dbDAO.getVolume() * effectVolumeMultiplier) + ")");
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Abspielen des Effekts: " + e.getMessage());
        }
    }

    public void stopAllAudio() {
        if (effectPlayer != null) effectPlayer.stop();
        if (menuPlayer != null) menuPlayer.stop();
        if (gamePlayer != null) gamePlayer.stop();
        if (bossPlayer != null) bossPlayer.stop();
        if (introPlayer != null) introPlayer.stop();
        if (effectTimer != null) {
            effectTimer.stop();
            if (JumpAndRun.debugMusic) {
                System.out.println("EffectTimer gestoppt");
            }
        }
        isMenuActive = false;
        isBossFight = false;
        isIntro = false;
    }

    public void setVolume(double volume) {
        dbDAO.saveSetting("volume", String.valueOf(volume));
        if (menuPlayer != null) {
            menuPlayer.setVolume(isMenuActive ? volume : volume * gameVolumeMultiplier);
        }
        if (gamePlayer != null) {
            gamePlayer.setVolume(volume * gameVolumeMultiplier);
        }
        if (bossPlayer != null) {
            bossPlayer.setVolume(volume * gameVolumeMultiplier);
        }
        if (effectPlayer != null) {
            effectPlayer.setVolume(volume * effectVolumeMultiplier);
        }
    }
}