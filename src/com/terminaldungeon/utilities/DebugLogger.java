package com.terminaldungeon.utilities;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLogger {

    private static final String LOG_DIR = "./logs";
    private static final String LOG_FILE = "debug.log";
    private static final File logDirectory = new File(LOG_DIR);
    private static final File logFile = new File(LOG_DIR + "/" + LOG_FILE);

    static {
        try {
            if (!logDirectory.exists()) logDirectory.mkdirs();
            if (!logFile.exists()) logFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }

    // MAIN method that accepts a header
    public static void log(String header, String message) {
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            out.println("[" + timestamp + "] [" + header + "] " + message);

        } catch (IOException e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }

    // Convenience method for no header
    public static void log(String message) {
        log("GENERAL", message);
    }
}
