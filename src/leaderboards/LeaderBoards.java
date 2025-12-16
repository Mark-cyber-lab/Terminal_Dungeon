package leaderboards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LeaderBoards {
    private static final Path FILE_PATH = Paths.get("./db/leaderboards.txt");
    
    public static void retrieveLeaderBoardData() {
        if (!Files.exists(FILE_PATH)) {
            System.err.println("File does not exist: " + FILE_PATH);
            return;
        }
        
        if (!Files.isReadable(FILE_PATH)) {
            System.err.println("File is not readable: " + FILE_PATH);
            return;
        }
        
        IO.println("\n\n============ LEADERBOARD DATA ============\n\n");
        IO.println("Name      Health      Level      Date and Time");
        
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
            String line;
            boolean hasContent = false;
            
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {  // Skip empty lines
                    String[] stringArr = line.split(",");
                    line = String.format("\n%-10s %-11s %-10s %s\n\n\n\n", 
                                       stringArr[0], stringArr[1], stringArr[2], stringArr[3]);
                    System.out.println(line);
                    hasContent = true;
                }
            }
            
            if (!hasContent)
                System.out.println("\n\nNo leaderboard data found!\n\n\n");
            else 
                System.out.println("\n=========================================\n\nClear leaderboards? [y/n]: ");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static void addLeaderboardEntry(String name, int health, int level, String dateTime) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Error: Name cannot be empty");
            return;
        }
        
        if (health < 0) {
            System.err.println("Error: Health cannot be negative");
            return;
        }
        
        if (level < 0) {
            System.err.println("Error: Level cannot be negative");
            return;
        }
        
        if (dateTime == null || dateTime.trim().isEmpty()) {
            System.err.println("Error: Date and time cannot be empty");
            return;
        }
        
        try {
            Files.createDirectories(FILE_PATH.getParent());
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
            return;
        }
        
        String entry = String.format("%s,%d,%d,%s%n", name.trim(), health, level, dateTime.trim());
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                FILE_PATH, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND)) {
            
            writer.write(entry);
            System.out.println("Leaderboard entry added successfully!");
            
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    
    public static void addLeaderboardEntry(String name, int health, int level) {
        String currentDateTime = java.time.LocalDateTime.now().toString();
        addLeaderboardEntry(name, health, level, currentDateTime);
    }

    public static void clearLeaderboardData() {
        try {
            if (Files.exists(FILE_PATH)) {
                Files.delete(FILE_PATH);
                System.out.println("\n\n\nLeaderboard data cleared successfully!\n\n\n");
            } else {
                System.out.println("\n\n\nNo leaderboard data to clear.\n\n\n");
            }
        } catch (IOException e) {
            System.err.println("Error clearing leaderboard data: " + e.getMessage());
        }
    }
}