package com.eumel.data;

import java.util.Random;

public class TaskGenerator {
    public Task generateTask(int id, String questionTemplate) {
        Random rand = new Random();
        String unit;
        String answer;
        String question;

        if (id == 2) { // PNG-Aufgaben
            int width = rand.nextInt(1501) + 500;       // Breite: 500-2000 Pixel
            int height = rand.nextInt(1501) + 500;      // Höhe: 500-2000 Pixel
            int bitDepth = (rand.nextInt(3) + 1) * 8;   // Farbtiefe: 8, 16 oder 24 Bit

            double sizeInBytes = (width * height * bitDepth) / 8.0; // Unkomprimiert
            String[] units = {"KB", "MB", "KiB", "MiB"};
            double sizeInUnit;
            do {
                unit = units[rand.nextInt(units.length)];
                switch (unit) {
                    case "KB": sizeInUnit = sizeInBytes / 1000.0; break;
                    case "MB": sizeInUnit = sizeInBytes / (1000.0 * 1000.0); break;
                    case "KiB": sizeInUnit = sizeInBytes / 1024.0; break;
                    case "MiB": sizeInUnit = sizeInBytes / (1024.0 * 1024.0); break;
                    default: sizeInUnit = sizeInBytes / (1024.0 * 1024.0); unit = "MiB";
                }
            } while (sizeInUnit < 1.0);

            answer = String.format("%.2f", sizeInUnit);
            question = String.format(questionTemplate,
                    String.valueOf(width),      // %1$s
                    String.valueOf(height),     // %2$s
                    String.valueOf(bitDepth),   // %3$s
                    unit                        // %4$s
            );
        } else if (id == 1) { // JPG-Aufgaben
            int width = rand.nextInt(1501) + 500;       // Breite: 500-2000 Pixel
            int height = rand.nextInt(1501) + 500;      // Höhe: 500-2000 Pixel
            int bitDepth = (rand.nextInt(3) + 1) * 8;   // Farbtiefe: 8, 16 oder 24 Bit
            int compression = rand.nextInt(81) + 10;    // Kompression: 10-90%

            double sizeInBytes = (width * height * bitDepth) / 8.0 * (compression / 100.0); // Mit Kompression
            String[] units = {"KB", "MB", "KiB", "MiB"};
            double sizeInUnit;
            do {
                unit = units[rand.nextInt(units.length)];
                switch (unit) {
                    case "KB": sizeInUnit = sizeInBytes / 1000.0; break;
                    case "MB": sizeInUnit = sizeInBytes / (1000.0 * 1000.0); break;
                    case "KiB": sizeInUnit = sizeInBytes / 1024.0; break;
                    case "MiB": sizeInUnit = sizeInBytes / (1024.0 * 1024.0); break;
                    default: sizeInUnit = sizeInBytes / (1024.0 * 1024.0); unit = "MiB";
                }
            } while (sizeInUnit < 1.0);

            answer = String.format("%.2f", sizeInUnit);
            question = String.format(questionTemplate,
                    String.valueOf(width),      // %1$s
                    String.valueOf(height),     // %2$s
                    String.valueOf(bitDepth),   // %3$s
                    String.valueOf(compression),// %4$s
                    unit                        // %5$s
            );
        } else if (id == 3) { // STREAMING-Aufgabe
            int bitrate = rand.nextInt(10) + 1;         // Bitrate: 1-10 Mbit/s
            int hours = rand.nextInt(3) + 1;            // Stunden: 1-3
            int minutes = rand.nextInt(60);             // Minuten: 0-59

            double totalSeconds = (hours * 3600) + (minutes * 60);
            double sizeInBytes = (bitrate * 1_000_000 * totalSeconds) / 8.0;
            String[] units = {"MB", "GB"};
            double sizeInUnit;
            do {
                unit = units[rand.nextInt(units.length)];
                switch (unit) {
                    case "MB": sizeInUnit = sizeInBytes / (1000.0 * 1000.0); break;
                    case "GB": sizeInUnit = sizeInBytes / (1000.0 * 1000.0 * 1000.0); break;
                    default: sizeInUnit = sizeInBytes / (1000.0 * 1000.0 * 1000.0); unit = "GB";
                }
            } while (sizeInUnit < 1.0);

            answer = String.format("%.2f", sizeInUnit);
            question = String.format(questionTemplate,
                    String.valueOf(bitrate),    // %1$s
                    String.valueOf(hours),      // %2$s
                    String.valueOf(minutes),    // %3$s
                    unit                        // %4$s
            );
        } else {
            // Fallback: PNG
            int width = rand.nextInt(1501) + 500;
            int height = rand.nextInt(1501) + 500;
            int bitDepth = (rand.nextInt(3) + 1) * 8;

            double sizeInBytes = (width * height * bitDepth) / 8.0;
            String[] units = {"KB", "MB", "KiB", "MiB"};
            double sizeInUnit;
            do {
                unit = units[rand.nextInt(units.length)];
                switch (unit) {
                    case "KB": sizeInUnit = sizeInBytes / 1000.0; break;
                    case "MB": sizeInUnit = sizeInBytes / (1000.0 * 1000.0); break;
                    case "KiB": sizeInUnit = sizeInBytes / 1024.0; break;
                    case "MiB": sizeInUnit = sizeInBytes / (1024.0 * 1024.0); break;
                    default: sizeInUnit = sizeInBytes / (1024.0 * 1024.0); unit = "MiB";
                }
            } while (sizeInUnit < 1.0);

            answer = String.format("%.2f", sizeInUnit);
            question = String.format(questionTemplate,
                    String.valueOf(width),
                    String.valueOf(height),
                    String.valueOf(bitDepth),
                    unit
            );
        }

        return new Task(id, question, answer);
    }
}