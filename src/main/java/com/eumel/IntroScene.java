package com.eumel;

import com.eumel.data.DBDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.eumel.JumpAndRun.debugMode;

public class IntroScene {
    private final Stage stage;
    private final Pane introPane;
    private MediaPlayer introMediaPlayer;
    private List<Image> introImages;
    private List<Subtitle> subtitles;
    private ImageView introView;
    private Text subtitleText;
    private Timeline imageTimeline;
    private Font customFont;
    private final Runnable onComplete;
    private final DBDAO dbDao;
    private final boolean fromSettings;

    public IntroScene(Stage stage, Runnable onComplete, DBDAO dbDao, boolean fromSettings) {
        this.stage = stage;
        this.onComplete = onComplete;
        this.dbDao = dbDao;
        this.fromSettings = fromSettings;
        this.introPane = new Pane();
        loadResources();
        setupIntro();
    }

    private void loadResources() {
        customFont = Font.loadFont(getClass().getResourceAsStream("/fonts/Riffic.ttf"), 25);
        if (customFont == null) {
            System.err.println("Fehler beim Laden der Schriftart Riffic.ttf im Intro");
            customFont = Font.font("Arial", 20);
        }
        introImages = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            String path = "/intro/intro" + i + ".png";
            try {
                Image image = new Image(getClass().getResource(path).toExternalForm());
                introImages.add(image);
                if (debugMode) {
                    System.out.println("Intro-Bild geladen: " + path + " (" + image.getWidth() + "x" + image.getHeight() + ")");
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Laden von " + path + ": " + e.getMessage());
            }
        }
        if (introImages.isEmpty()) {
            System.err.println("Keine Intro-Bilder geladen!");
        }

        subtitles = loadSubtitlesFromSrt("/intro/story.srt");
        try {
            String audioPath = getClass().getResource("/intro/story.mp3").toExternalForm();
            Media introMedia = new Media(audioPath);
            introMediaPlayer = new MediaPlayer(introMedia);
            if (debugMode) {
                System.out.println("Intro-MP3 geladen: " + audioPath);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Intro-MP3: " + e.getMessage());
        }
    }

    private List<Subtitle> loadSubtitlesFromSrt(String srtPath) {
        List<Subtitle> subtitleList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(srtPath)))) {
            String line;
            int index = 0;
            double startTime = 0;
            StringBuilder text = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (text.length() > 0) {
                        subtitleList.add(new Subtitle(startTime, text.toString()));
                        text.setLength(0);
                    }
                    continue;
                }

                if (line.matches("\\d+")) {
                    index = Integer.parseInt(line);
                    continue;
                }

                if (line.contains("-->")) {
                    String[] times = line.split(" --> ");
                    startTime = parseSrtTime(times[0]);
                    continue;
                }

                if (text.length() > 0) {
                    text.append("\n");
                }
                text.append(line);
            }

            if (text.length() > 0) {
                subtitleList.add(new Subtitle(startTime, text.toString()));
            }
            if (debugMode) {
                System.out.println("Geladene Untertitel: " + subtitleList.size());
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der SRT-Datei " + srtPath + ": " + e.getMessage());
        }
        return subtitleList;
    }

    private double parseSrtTime(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        String[] secondsParts = parts[2].split(",");
        double seconds = Double.parseDouble(secondsParts[0]);
        double milliseconds = Double.parseDouble(secondsParts[1]) / 1000.0;
        return hours * 3600 + minutes * 60 + seconds + milliseconds;
    }

    private void setupIntro() {
        Scene introScene = new Scene(introPane, 800, 600);
        stage.setScene(introScene);
        stage.setTitle("Eumels Höllish Süßes Abenteuer - Intro");
        introView = new ImageView();
        if (!introImages.isEmpty()) {
            introView.setImage(introImages.get(0));
            introView.setFitWidth(800);
            introView.setFitHeight(600);
        } else {
            System.err.println("Keine Bilder verfügbar, IntroView bleibt leer.");
        }
        introPane.getChildren().add(introView);

        subtitleText = new Text();
        subtitleText.setFont(customFont);
        subtitleText.setFill(Color.WHITE);
        subtitleText.setStroke(Color.BLACK);
        subtitleText.setStrokeWidth(1);
        subtitleText.setTextAlignment(TextAlignment.CENTER);
        subtitleText.setWrappingWidth(400);
        subtitleText.setLayoutX((800 - 400) / 2);
        subtitleText.setLayoutY(600 - 100 - 50);
        introPane.getChildren().add(subtitleText);

        imageTimeline = new Timeline();
        if (!introImages.isEmpty()) {
            double duration = 125.0 / introImages.size();
            for (int i = 0; i < introImages.size(); i++) {
                final int index = i;
                imageTimeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(i * duration), e -> introView.setImage(introImages.get(index)))
                );
            }
            imageTimeline.setCycleCount(1);
        }

        if (introMediaPlayer != null) {
            introMediaPlayer.setOnReady(() -> {
                introMediaPlayer.play();
                if (!introImages.isEmpty()) {
                    imageTimeline.play();
                }
                updateSubtitles();
            });
            introMediaPlayer.setOnEndOfMedia(() -> {
                dbDao.saveSetting("introShown", "true");
                onComplete.run();
            });
        }

        introScene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ESCAPE") || event.getCode().toString().equals("SPACE")) {
                if (introMediaPlayer != null) {
                    introMediaPlayer.stop();
                }
                if (imageTimeline != null) {
                    imageTimeline.stop();
                }
                if (!fromSettings) { // Nur bei Spielstart in DB schreiben
                    dbDao.saveSetting("introShown", "true");
                }
                onComplete.run();
            }
        });
    }

    public void show() {
        stage.show();
        if (introMediaPlayer != null) {
            introMediaPlayer.play();
        }
    }

    public void stop() {
        if (introMediaPlayer != null) {
            introMediaPlayer.stop();
        }
        if (imageTimeline != null) {
            imageTimeline.stop();
        }
    }

    private void updateSubtitles() {
        Timeline subtitleTimeline = new Timeline();
        for (Subtitle subtitle : subtitles) {
            subtitleTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(subtitle.getStartTime()), e -> subtitleText.setText(subtitle.getText()))
            );
        }
        subtitleTimeline.setCycleCount(1);
        subtitleTimeline.play();
    }
}

class Subtitle {
    private final double startTime;
    private final String text;

    public Subtitle(double startTime, String text) {
        this.startTime = startTime;
        this.text = text;
    }

    public double getStartTime() {
        return startTime;
    }

    public String getText() {
        return text;
    }
}