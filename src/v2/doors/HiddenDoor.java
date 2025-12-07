package v2.doors;

import v2.CommandContext;
import v2.CommandMiddleware;
import v2.CommandResult;
import v2.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HiddenDoor implements CommandMiddleware {

    private static final int HIDDEN_DOOR_DAMAGE = 1;

    private final String name;
    private final String id;
    private final Path doorPath;

    private HiddenKey hiddenKey;
    private boolean hasBeenUnlocked;
    private Player player;

    public HiddenDoor(String name, String id, Path doorPath) {
        this.name = name;
        this.id = id;
        this.doorPath = norm(doorPath);
        this.hasBeenUnlocked = false;
    }

    public HiddenDoor unlocksBy(HiddenKey key) {
        this.hiddenKey = key;
        return this;
    }

    public HiddenDoor setPlayer(Player player) {
        this.player = player;
        return this;
    }

    private void dealDamage(String reason) {
        IO.println("[HiddenDoor] Damage: -" + HIDDEN_DOOR_DAMAGE + "HP (" + reason + ")");
        if (player != null) {
            player.takeDamage(HIDDEN_DOOR_DAMAGE);
        } else {
            IO.println("[HiddenDoor] Missing Player instance!");
        }
    }

    public boolean hasBeenUnlocked() {
        return hasBeenUnlocked;
    }

    // ---------- Normalization helper ----------
    private static Path norm(Path p) {
        return p == null ? null : p.toAbsolutePath().normalize();
    }

    // ----------------------------------------------------
    // Prevent navigation *into* locked door directories
    // ----------------------------------------------------
    public boolean blocksNavigation(Path targetDir) {

        if (hasBeenUnlocked) return false;   // already unlocked
        if (hiddenKey == null) return false; // no key assigned
        if (doorPath == null) return false;

        Path normalizedTarget = norm(targetDir);
        Path normalizedDoor = norm(doorPath);

        // If targetDir starts with the hidden door path → you're going inside it
        return normalizedTarget.startsWith(normalizedDoor);
    }

    public boolean before(String command, String[] args, CommandContext ctx) {
        Path currentDir = ctx.startDir;

        if (!"cd".equals(command) && !"mv".equals(command))
            return true;

        if (args.length < 1) return true;

        String target = args[0];

        Path targetDir = currentDir.resolve(target).normalize();

        if (blocksNavigation(targetDir)) {
            IO.println("Cannot navigate: hidden door is still locked!");

            return false;
        }

        return true;
    }

    @Override
    public void after(String command, String[] args, CommandContext ctx, CommandResult result) {

        if (!"mv".equals(command)) return;
        if (hiddenKey == null) return;

        var renamedMap = result.getContext().renamed;
        if (renamedMap.isEmpty()) return;

        // --------- Extract original and new path ---------
        Path oldPath = norm(renamedMap.keySet().iterator().next());
        Path newPath = norm(renamedMap.values().iterator().next());

        String expectedKeyName = hiddenKey.getKeyName();

        // ----------------------------------------------------------
        // 0. HARD CHECK: Was the moved file the registered key file?
        // ----------------------------------------------------------
        if (!oldPath.getFileName().toString().equals(expectedKeyName)) {
//            dealDamage("Wrong key file used: expected '" + expectedKeyName + "'");
            return; // Do not process further
        }

        // ----------------------------------------------------------
        // 1. Validate expected target folder
        // ----------------------------------------------------------
        Path expectedFolder = norm(doorPath);

        if (!hasBeenUnlocked && expectedFolder != null) {
            if (!expectedFolder.getParent().equals(newPath.getParent())) {
                dealDamage("Key moved into wrong folder");
            }
        }

        // ----------------------------------------------------------
        // 2. Check that key file exists at destination
        // ----------------------------------------------------------
        Path keyPath = newPath.getParent().resolve(expectedKeyName).normalize();

        if (!Files.exists(keyPath)) {
            dealDamage("Key file does not exist: " + keyPath);
            return;
        }

        // ----------------------------------------------------------
        // 3. Validate key content
        // ----------------------------------------------------------
        try {
            String content = Files.readString(keyPath).trim();
            boolean correct = content.equals(hiddenKey.getKeyContent().trim());

            if (!hasBeenUnlocked && !correct) {
                dealDamage("Incorrect key content");
                return;
            }

            // SUCCESS → Unlock
            hasBeenUnlocked = true;
            IO.println("[HiddenDoor] Door unlocked successfully!");

        } catch (IOException e) {
            dealDamage("Failed to read key file: " + e.getMessage());
        }
    }

}
