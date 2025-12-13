package com.terminaldungeon.elements.doors;

import com.terminaldungeon.gameplay.CommandContext;
import com.terminaldungeon.gameplay.CommandMiddleware;
import com.terminaldungeon.gameplay.CommandResult;
import com.terminaldungeon.player.Player;

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
    private boolean keyWasPlacedCorrectly = false;
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

    @Override
    public boolean before(String command, String[] args, CommandContext ctx) {

        // -----------------------------------------
        // Block navigation into door folder if locked
        // -----------------------------------------
        Path currentDir = ctx.startDir;

        if ("cd".equals(command)) {
            if (args.length >= 1) {
                Path targetDir = currentDir.resolve(args[0]).normalize();
                if (blocksNavigation(targetDir)) {
                    IO.println("Cannot navigate: hidden door is still locked!");
                    return false;
                }
            }
            return true;
        }

        // -----------------------------------------
        // Protect key from being moved/renamed
        // -----------------------------------------
        if (!"mv".equals(command))
            return true;

        if (args.length < 2)
            return true;

        // Compute source/destination paths
        Path sourcePath = norm(currentDir.resolve(args[0]));
        Path destinationPath = norm(currentDir.resolve(args[1]));

        if (hiddenKey == null)
            return true;

        String expectedKeyName = hiddenKey.getKeyName();
        String sourceName = sourcePath.getFileName().toString();
        String destName = destinationPath.getFileName().toString();

        boolean isKeySource = sourceName.equals(expectedKeyName);
        boolean isKeyDest = destName.equals(expectedKeyName);

        // If mv doesn't involve the key, ignore
        if (!isKeySource && !isKeyDest)
            return true;

        Path destParent = destinationPath.getParent();
        Path correctParent = doorPath.getParent();

        // =======================================================
        // CASE A — KEY ALREADY PLACED CORRECTLY (DOOR UNLOCKED)
        // =======================================================
        if (keyWasPlacedCorrectly) {

            // A1 — Attempt to move the key out of the correct folder
            if (!destParent.equals(correctParent)) {
                dealDamage("You attempted to remove the key after unlocking the door!");
                IO.println("[HiddenDoor] Key movement blocked.");
                return false;
            }

            // A2 — Attempt to rename key
            if (!destName.equals(expectedKeyName)) {
                dealDamage("You attempted to rename the key after unlocking the door!");
                IO.println("[HiddenDoor] Key rename blocked.");
                return false;
            }

            // Correct → allow
            return true;
        }

        // =======================================================
        // CASE B — KEY NOT YET PLACED CORRECTLY
        // (Prevent obvious misplacement attempts)
        // =======================================================

        // B1 — Wrong folder
        if (!destParent.equals(correctParent)) {
            dealDamage("Key moved to an incorrect folder.");
            return false;
        }

        // If passed these checks → allow
        return true;
    }

    @Override
    public void after(String command, String[] args, CommandContext ctx, CommandResult result) {

        if (!"mv".equals(command)) return;
        if (hiddenKey == null) return;

        var renamedMap = result.getContext().renamed;
        if (renamedMap.isEmpty()) return;

        Path oldPath = norm(renamedMap.keySet().iterator().next());
        Path newPath = norm(renamedMap.values().iterator().next());
        String expectedKeyName = hiddenKey.getKeyName();

        if (!oldPath.getFileName().toString().equals(expectedKeyName)) {
            return;
        }

        Path keyPath = newPath.getParent().resolve(expectedKeyName).normalize();

        try {
            String content = Files.readString(keyPath).trim();
            boolean correct = content.equals(hiddenKey.getKeyContent().trim());

            if (!correct) {
                dealDamage("Incorrect key content");
                return;
            }

            hasBeenUnlocked = true;
            keyWasPlacedCorrectly = true;  // <<*** IMPORTANT LINE ***>>
            IO.println("[HiddenDoor] Door unlocked successfully!");

        } catch (IOException e) {
            dealDamage("Failed to read key file: " + e.getMessage());
        }
    }

}
