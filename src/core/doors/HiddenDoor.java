package core.doors;

import core.Player;
import core.listeners.Blocker;
import core.listeners.CommandListener;
import utilities.CommandResult;
import utilities.LinuxCommandExecutorWithRegistry;
import utilities.Loggable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HiddenDoor implements Blocker, CommandListener, Loggable {

    private static final int HIDDEN_DOOR_DAMAGE = 1;

    private final String name;
    private final String id;
    private final Path doorPath;

    private HiddenKey hiddenKey;   // <--- NEW

    private boolean hasBeenUnlocked;
    private Player player;

    public HiddenDoor(String name, String id, Path doorPath) {
        this.name = name;
        this.id = id;
        this.doorPath = doorPath;
        this.hasBeenUnlocked = false;
    }

    // Assign the key that unlocks this door
    public HiddenDoor unlocksBy(HiddenKey key) {
        this.hiddenKey = key;
        return this;
    }

    public HiddenDoor setPlayer(Player player) {
        this.player = player;
        return this;
    }

    // ---------------- Blocker ----------------

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean blocks(Path folder) {
        if (hasBeenUnlocked) return false;
        if (hiddenKey == null) return true; // no key assigned → always blocked

        Path absoluteFolder = folder.toAbsolutePath().normalize();
        Path doorFolder = doorPath.toAbsolutePath().normalize();

//        IO.println("absolute folder: " + absoluteFolder);
//        IO.println("door folder: " + doorFolder);

        if(!absoluteFolder.equals(doorFolder) || !absoluteFolder.startsWith(doorFolder)) return false;

        // If the key has an expected folder, ensure we're in it
        if (hiddenKey.getExpectedFolder() != null &&
                !hiddenKey.getExpectedFolder().normalize().equals(folder.normalize())) {
            return true;
        }

        Path keyPath = folder.resolve(hiddenKey.getKeyName());

//        IO.println("existing key: " + keyPath);
//        IO.println("exists? " + Files.exists(keyPath));
        if (!Files.exists(keyPath)) {
            IO.println("key does not exist: " + keyPath);
            return true;
        }

        // Check key content
        try {
            String content = Files.readString(keyPath).trim();
            return !content.equals(hiddenKey.getKeyContent().trim());
        } catch (IOException e) {
            log("[HiddenDoor] Failed to read key file: " + e.getMessage());
            return true;
        }
    }

    @Override
    public boolean isCleared() {
        return hasBeenUnlocked;
    }

    @Override
    public void clear() {
        hasBeenUnlocked = true;
        IO.println("[HiddenDoor] Unlocked: " + name + " at " + doorPath.toAbsolutePath().normalize());
    }

    @Override
    public Path getFilePath() {
        return doorPath;
    }

    // ---------------- Command Listener ----------------

    @Override
    public void onCommand(CommandResult result) {
        String cmd = result.command().toLowerCase();
        Path targetPath = result.getFileFullPath();
        if (targetPath == null) return;

        boolean isSamePath =
                targetPath.toAbsolutePath().normalize()
                        .equals(doorPath.toAbsolutePath().normalize());
//        IO.println("here");

        boolean isBlocked = blocks(targetPath.getParent());

//        IO.println(!hasBeenUnlocked);
//        IO.println("isBlocked: " + isBlocked);
//        IO.println(!cmd.equals("ls"));
//        IO.println(!result.success()
//        );

        // Wrong command while locked → damage
        if (!hasBeenUnlocked &&
                ((!cmd.equals("ls") && isBlocked))) {
            dealDamage("Wrong command executed near hidden door");
            player.getLevelObj().sandbox.getExecutor().executeCommand("cd", doorPath.getParent().toAbsolutePath().normalize().toString());
        }

        executeConditions(result, isSamePath);
    }

    protected void executeConditions(CommandResult result, boolean isSameFilePath) {
        if(isSameFilePath) return;

        Path targetPath = result.getFileFullPath();
        if (targetPath == null) return;

        Path absoluteFolder = targetPath.getParent().toAbsolutePath().normalize();
        Path doorFolder = doorPath.toAbsolutePath().normalize();

//        IO.println("absolute folder: " + absoluteFolder);
//        IO.println("door folder: " + doorFolder);

        if(absoluteFolder.equals(doorFolder) || !absoluteFolder.startsWith(doorFolder)) return;


        // Unlock when correct key is in folder
        if (!hasBeenUnlocked && !result.command().equalsIgnoreCase("ls") && !blocks(doorPath.getParent())) {
            clear();
        }
    }

    // ---------------- Damage ----------------

    private void dealDamage(String reason) {
        IO.println("[HiddenDoor] Damage: -" + HIDDEN_DOOR_DAMAGE + "HP (" + reason + ")");
        if (player != null) {
            player.takeDamage(HIDDEN_DOOR_DAMAGE);
        } else {
            log("[HiddenDoor] Cannot damage player — missing instance");
        }
    }

    // ---------------- Helpers ----------------

    public boolean hasBeenUnlocked() { return hasBeenUnlocked; }

    public void removeFromExecutor(LinuxCommandExecutorWithRegistry executor) {
        if (hasBeenUnlocked) {
            executor.removeBlocker(this);
            executor.removeCommandListener(this);
            log("[HiddenDoor] Removed listeners for: " + name);
        }
    }
}
