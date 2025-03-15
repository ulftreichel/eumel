package com.eumel.ui;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class UIUtils {
    private static final String ICON_PATH = "/images/player/bonbon.png";

    public static void setStageIcon(Stage stage) {
        stage.getIcons().add(new Image(Objects.requireNonNull(UIUtils.class.getResource(ICON_PATH)).toExternalForm()));
    }
}