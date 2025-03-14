package com.eumel.data;

import com.eumel.JumpAndRun;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DBDAO {
    private Connection connection;
    private TaskGenerator taskGenerator;

    public DBDAO() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:jumpandrun.db");
            taskGenerator = new TaskGenerator();
            initializeDatabase();
        } catch (SQLException e) {
            System.out.println("Fehler bei der Datenbankverbindung: " + e.getMessage());
        }
    }

    private void initializeDatabase() throws SQLException {
        System.out.println("Initialisiere Datenbank...");

        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS highscores (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, score INTEGER NOT NULL, date TEXT NOT NULL)");
        stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, question_template TEXT NOT NULL UNIQUE)");
        stmt.execute("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)");
        System.out.println("Tabellen überprüft/erzeugt.");

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tasks");
        rs.next();
        int taskCount = rs.getInt(1);
        System.out.println("Anzahl der Aufgaben in der Tabelle: " + taskCount);

        if (taskCount == 0) {
            stmt.execute("INSERT INTO tasks (question_template) VALUES ('Ein Fotograf speichert ein Bild im JPG-Format mit einer Auflösung von \"%1$s\"x\"%2$s\" Pixel und %3$s-Bit-Farbtiefe.\nDie Kompression beträgt %4$s%%. Wie groß ist die Datei nach der Kompression in %5$s?')");
            stmt.execute("INSERT INTO tasks (question_template) VALUES ('Ein Grafiker speichert ein Bild im PNG-Format mit einer Auflösung von \"%1$s\"x\"%2$s\" Pixel und %3$s-Bit-Farbtiefe.\nWie groß ist die Datei in %4$s?')");
            stmt.execute("INSERT INTO tasks (question_template) VALUES ('Ein Streaming-Dienst überträgt Sportübertragungen mit einer Bitrate von %1$s Mbit/s.\nEin Nutzer schaut ein Spiel mit einer Laufzeit von %2$s Stunden %3$s Minuten. Wie viele %4$s Daten werden verbraucht?')");
            System.out.println("Initialdaten in tasks-Tabelle eingefügt.");
        }

        ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM settings");
        rs2.next();
        int settingCount = rs2.getInt(1);
        System.out.println("Anzahl der Settings in der Tabelle: " + settingCount);

        if (settingCount == 0) {
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('volume', '0.25')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('move_left_primary', 'A')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('move_left_secondary', 'LEFT')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('move_right_primary', 'D')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('move_right_secondary', 'RIGHT')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('jump_primary', 'SPACE')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('jump_secondary', 'UP')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('shoot_primary', 'W')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('shoot_secondary', 'ENTER')");
            stmt.execute("INSERT OR IGNORE INTO settings (key, value) VALUES ('introShown', 'false')");
        }

        stmt.close();
    }

    public void saveHighscore(String name, int score) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO highscores (name, score, date) VALUES (?, ?, ?)");
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern des Highscores: " + e.getMessage());
        }
    }

    public List<Highscore> getHighscores(int limit) {
        List<Highscore> highscores = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, score, date FROM highscores ORDER BY score DESC LIMIT " + limit);
            while (rs.next()) {
                highscores.add(new Highscore(rs.getInt("id"), rs.getString("name"), rs.getInt("score"), rs.getString("date")));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Fehler beim Laden der Highscores: " + e.getMessage());
        }
        return highscores;
    }

    public Task getRandomTask() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, question_template FROM tasks ORDER BY RANDOM() LIMIT 1");
            if (rs.next()) {
                int id = rs.getInt("id");
                String template = rs.getString("question_template");
                Task task = taskGenerator.generateTask(id, template);
                rs.close();
                stmt.close();
                return task;
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Laden der Aufgabe: " + e.getMessage());
        }
        return null;
    }

    public void saveSetting(String key, String value) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)");
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
            pstmt.close();
            if (JumpAndRun.debugMode) {
                System.out.println("Einstellung gespeichert: " + key + " = " + value);
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern der Einstellung: " + e.getMessage());
        }
    }

    public String getSetting(String key, String defaultValue) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT value FROM settings WHERE key = ?");
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Fehler beim Abrufen der Einstellung: " + e.getMessage());
        }
        return defaultValue;
    }

    public double getVolume() {
        return Double.parseDouble(getSetting("volume", "0.25"));
    }

    public String getKeyBinding(String action, boolean primary) {
        String key = action + (primary ? "_primary" : "_secondary");
        return getSetting(key, switch (action) {
            case "move_left" -> primary ? "A" : "LEFT";
            case "move_right" -> primary ? "D" : "RIGHT";
            case "jump" -> primary ? "SPACE" : "UP";
            case "shoot" -> primary ? "W" : "ENTER";
            default -> "";
        });
    }

    public List<String> getAllKeyBindings() {
        List<String> bindings = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT value FROM settings WHERE key LIKE '%_primary' OR key LIKE '%_secondary'");
            while (rs.next()) {
                bindings.add(rs.getString("value"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Fehler beim Abrufen der Tastenbelegungen: " + e.getMessage());
        }
        return bindings;
    }

    public boolean isIntroShown() {
        return Boolean.parseBoolean(getSetting("introShown", "false"));
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Fehler beim Schließen der Datenbankverbindung: " + e.getMessage());
        }
    }
}