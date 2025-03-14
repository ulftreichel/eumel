module com.example.eumel {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires java.sql;
    requires json.simple;
    requires javafx.media;

    opens com.eumel to javafx.fxml;
    exports com.eumel;
    exports com.eumel.level.design;
    opens com.eumel.level.design to javafx.fxml;
    exports com.eumel.opponent;
    opens com.eumel.opponent to javafx.fxml;
}