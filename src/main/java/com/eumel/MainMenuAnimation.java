package com.eumel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.eumel.JumpAndRun.debugMainFrame;

public class MainMenuAnimation {
    private final Pane pane;
    private final List<Image> frames;
    private final ImageView frameView;
    private final Timeline animation;

    public MainMenuAnimation(Pane pane) {
        this.pane = pane;
        this.frames = new ArrayList<>();
        this.frameView = new ImageView();
        this.animation = new Timeline();
        loadFrames();
        setupAnimation();
    }

    private void loadFrames() {
        for (int i = 1; i <= 52; i++) {
            String path = String.format("/video/intro%03d.png", i);
            try {
                Image frame = new Image(getClass().getResource(path).toExternalForm());
                frames.add(frame);
                if (debugMainFrame){
                    System.out.println("Main-Frame geladen: " + path + " (" + frame.getWidth() + "x" + frame.getHeight() + ")");
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Laden von " + path + ": " + e.getMessage());
            }
        }
        if (frames.isEmpty()) {
            System.err.println("Keine Frames für Hauptmenü-Animation geladen!");
        }
    }

    private void setupAnimation() {
        frameView.setFitWidth(800);
        frameView.setFitHeight(600);
        if (!frames.isEmpty()) {
            frameView.setImage(frames.get(0));
            double frameDuration = 5.0 / frames.size(); // 5 Sekunden
            for (int i = 0; i < frames.size(); i++) {
                final int index = i;
                animation.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(i * frameDuration), e -> frameView.setImage(frames.get(index)))
                );
            }
            animation.setCycleCount(Timeline.INDEFINITE);
            pane.getChildren().add(frameView);
        }
    }

    public void play() {
        if (!frames.isEmpty()) {
            animation.play();
        }
    }

    public void stop() {
        animation.stop();
    }
}