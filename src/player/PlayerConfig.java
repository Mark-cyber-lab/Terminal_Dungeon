package player;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

public class PlayerConfig {

    private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    private final Path dataFile;
    private final Path hashFile;
    private final Player player;
    private final SecretKeySpec secretKey;

    public PlayerConfig(String fileName, Player player, String encryptionKey) {
        this.player = player;
        this.secretKey = deriveKey(encryptionKey);

        Path basePath = Path.of(fileName);
        Path parent = basePath.getParent();

        Path cacheDir = (parent != null)
                ? parent.resolve("cache").resolve("player")
                : Path.of("cache").resolve("player");

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize player cache directory", e);
        }

        this.dataFile = cacheDir.resolve(fileName + ".dat");
        this.hashFile = cacheDir.resolve(fileName + ".dat.sha256");
    }

    /* ==========================
       SAVE
       ========================== */

    public void save() {
        try {
            String json = toJson(buildDataMap());

            byte[] encrypted = encrypt(json.getBytes(StandardCharsets.UTF_8));
            Files.write(dataFile, encrypted,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            String hash = hash(encrypted);
            Files.writeString(hashFile, hash,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception e) {
            System.err.println("Failed to save player config: " + e.getMessage());
        }
    }

    /* ==========================
       LOAD
       ========================== */

    private void resetPlayerStats() {
        player.getStats().setStage(1);
        player.getStats().setLevel(1);
        player.getStats().setCurrentDir("");
        player.getStats().setHealth(100);
        player.initialLevel = 1;
    }

    public void load() {
        if (!Files.exists(dataFile) || !Files.exists(hashFile)) {
            resetPlayerStats();
            return;
        }

        try {
            byte[] encrypted = Files.readAllBytes(dataFile);
            String storedHash = Files.readString(hashFile).trim();
            String computedHash = hash(encrypted);

            if (!computedHash.equals(storedHash)) {
                System.err.println("Integrity check failed. Resetting player stats.");
                resetPlayerStats();
                return;
            }

            byte[] decrypted = decrypt(encrypted);
            Map<String, String> data = parseJson(
                    new String(decrypted, StandardCharsets.UTF_8));

            applyData(data);

        } catch (Exception e) {
            System.err.println("Failed to load player config. Resetting stats.");
            resetPlayerStats();
        }
    }

    /* ==========================
       CRYPTO
       ========================== */

    private SecretKeySpec deriveKey(String key) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashed = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hashed, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }

    private byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);

        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(data);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(iv);
        out.write(cipherText);
        return out.toByteArray();
    }

    private byte[] decrypt(byte[] encrypted) throws Exception {
        byte[] iv = Arrays.copyOfRange(encrypted, 0, IV_SIZE);
        byte[] payload = Arrays.copyOfRange(encrypted, IV_SIZE, encrypted.length);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        return cipher.doFinal(payload);
    }

    private String hash(byte[] data) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        return Base64.getEncoder().encodeToString(sha.digest(data));
    }

    /* ==========================
       DATA HANDLING
       ========================== */

    private Map<String, Object> buildDataMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("level", player.getStats().getLevel());
        data.put("stage", player.getStats().getStage());
        data.put("currentDir",
                Path.of(player.getStats().getCurrentDir()).normalize().toString());
        data.put("health", player.getStats().getHealth());
        data.put("granted", player.getStats().getGrantedCommands());
        return data;
    }

    private void applyData(Map<String, String> data) {
        data.forEach((key, value) -> {
            switch (key) {
                case "level" -> player.promoteLevelTo(Integer.parseInt(value));
                case "health" -> player.setHealth(Integer.parseInt(value));
                case "stage" -> player.getStats().setStage(Integer.parseInt(value));
                case "currentDir" -> player.getStats().setCurrentDir(value);
                case "granted" -> {
                    List<String> cmds = parseJsonArray(value);
                    player.getStats().setGrantedCommands(new HashSet<>(cmds));
                }
            }
        });
    }

    /* ==========================
       JSON (unchanged)
       ========================== */

    private String toJson(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{\n");
        int count = 0;

        for (var entry : data.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": ");
            Object val = entry.getValue();

            if (val instanceof Number) sb.append(val);
            else if (val instanceof Collection<?> col) {
                sb.append("[");
                sb.append(col.stream()
                        .map(v -> "\"" + v + "\"")
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""));
                sb.append("]");
            } else sb.append("\"").append(val).append("\"");

            if (++count < data.size()) sb.append(",");
            sb.append("\n");
        }
        return sb.append("}").toString();
    }

    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        json = json.trim().replaceAll("^\\{|}$", "");

        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2)
                map.put(kv[0].trim().replace("\"", ""),
                        kv[1].trim().replaceAll("^\"|\"$", ""));
        }
        return map;
    }

    private List<String> parseJsonArray(String jsonArray) {
        List<String> list = new ArrayList<>();
        jsonArray = jsonArray.trim().replaceAll("^\\[|]$", "");
        if (jsonArray.isBlank()) return list;

        for (String item : jsonArray.split(",")) {
            list.add(item.trim().replaceAll("^\"|\"$", ""));
        }
        return list;
    }
}
