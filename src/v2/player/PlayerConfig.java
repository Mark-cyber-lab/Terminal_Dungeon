package v2.player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Handles saving and loading player configuration (progress) to a JSON file.
 */
public class PlayerConfig {

    private final Path configFile;
    private final Player player;

    public PlayerConfig(String filePath,  Player player) {
        this.configFile = Path.of(filePath);
        this.player = player;
    }

    /**
     * Saves the player's current state to a JSON file.
     */
    public void save() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("level", player.getStats().getLevel());
        data.put("stage", player.getStats().getStage());
        data.put("currentDir", player.getStats().getCurrentDir());
        data.put("health", player.getStats().getHealth());
        data.put("granted", player.getStats().getGrantedCommands());

        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            writer.write(toJson(data));
        } catch (IOException e) {
            System.err.println("Failed to save player config: " + e.getMessage());
        }
    }

    /**
     * Loads the player state from a JSON file.
     */
    public void load() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!Files.exists(configFile)) return;

        try (BufferedReader reader = Files.newBufferedReader(configFile)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }

            Map<String, String> jsonMap = parseJson(sb.toString());
            jsonMap.forEach((key, value) -> {
                switch (key) {
                    case "level" -> player.promoteLevelTo(Integer.parseInt(value));
                    case "health" -> player.setHealth(Integer.parseInt(value));
                    case "stage" -> player.getStats().setStage(Integer.parseInt(value));
                    case "currentDir" -> player.getStats().setCurrentDir(value);
                    case "granted" -> {
                        // Parse the string as a JSON array of commands
                        List<String> commands = parseJsonArray(value);
                        player.getStats().setGrantedCommands(new HashSet<>(commands));
                    }
                }
            });

        } catch (IOException e) {
            System.err.println("Failed to load player config: " + e.getMessage());
        }

    }

    // -----------------------
    // Simple JSON builder
    // -----------------------
    private String toJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{\n");
        int count = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": ");
            Object val = entry.getValue();
            if (val instanceof Number) sb.append(val);
            else sb.append("\"").append(val).append("\"");
            count++;
            if (count < data.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Very simple JSON parser for flat key-value pairs.
     * Expects: {"key1":"val1","key2":123}
     */
    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().replaceAll("\"", "");
            String val = kv[1].trim().replaceAll("\"", "");
            map.put(key, val);
        }

        return map;
    }

    /**
     * Parses a simple JSON array string: ["cmd1","cmd2"]
     */
    private List<String> parseJsonArray(String jsonArray) {
        List<String> list = new ArrayList<>();
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[")) jsonArray = jsonArray.substring(1);
        if (jsonArray.endsWith("]")) jsonArray = jsonArray.substring(0, jsonArray.length() - 1);

        // Split by comma outside quotes
        String[] items = jsonArray.split(",");
        for (String item : items) {
            String val = item.trim().replaceAll("^\"|\"$", ""); // remove surrounding quotes
            if (!val.isEmpty()) list.add(val);
        }
        return list;
    }
}

