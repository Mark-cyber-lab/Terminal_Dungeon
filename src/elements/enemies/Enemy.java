package elements.enemies;

import gameplay.CommandContext;
import gameplay.CommandMiddleware;
import gameplay.CommandResult;
import player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Enemy implements CommandMiddleware {

    private final String name;
    private final String id;
    private final Path enemyFilePath;
    private final int damagePerUnit;

    private boolean defeated = false;
    private Player player;
    private List<Enemy> troupe;

    // -------------------------------------------------
    // Constructor + setters
    // -------------------------------------------------
    public Enemy(String name, String id, Path enemyPath, int damagePerUnit, boolean spawnable) {
        this.name = name;
        this.id = id;
        this.enemyFilePath = enemyPath;
        this.damagePerUnit = damagePerUnit;

        if (spawnable) {
            try {
                Files.createDirectories(enemyPath.getParent());

                String content = "id=" + id + "\nname=" + name + "\n";
                Files.writeString(enemyPath, content);
            } catch (IOException e) {
                System.err.println("[Enemy Spawn Error] Failed to create enemy file: " + e.getMessage());
            }
        }
    }

    public Enemy(String name, String id, Path enemyPath, int damagePerUnit) {
        this(name, id, enemyPath, damagePerUnit, false); // call main constructor
    }


    public Enemy setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public void setEnemyTroupe(List<Enemy> troupe) {
        this.troupe = troupe;
    }

    public boolean hasBeenDefeated() {
        return defeated;
    }

    public int getDamagePerUnit() {
        return damagePerUnit;
    }

    // -------------------------------------------------
    // Utility: absolute normalized path
    // -------------------------------------------------
    private static Path norm(Path p) {
        return p.toAbsolutePath().normalize();
    }

    // -------------------------------------------------
    // Helper: all alive enemies in the same directory
    // -------------------------------------------------
    private List<Enemy> aliveEnemiesInSameFolder() {
        if (troupe == null) return List.of();

        Path folder = enemyFilePath.getParent();

        if (!troupe.contains(this))
            troupe.add(this);
        return troupe.stream()
//                .filter(e -> !e.id.equals(this.id))
                .filter(e -> !e.defeated)
                .filter(e -> Objects.equals(e.enemyFilePath.getParent(), folder))
                .toList();
    }

    // -------------------------------------------------
    // Logic: block rm command if other enemies alive
    // -------------------------------------------------
    public boolean blocksCommand(String command) {
        return "rm".equals(command)
                && !aliveEnemiesInSameFolder().isEmpty();
    }

    // -------------------------------------------------
    // Navigation block (cd/mv)
    // -------------------------------------------------
    public boolean blocksNavigation(Path targetDir) {
        if (troupe == null) return false;

        Path normalizedTarget = norm(targetDir);

        return troupe.stream()
                .filter(e -> !e.defeated)
                .anyMatch(e -> normalizedTarget.startsWith(norm(e.enemyFilePath.getParent())));
    }

    // -------------------------------------------------
    // Damage
    // -------------------------------------------------
    private void damage(String reason) {
        System.out.println("[Enemy damage] " + name +
                " deals " + damagePerUnit + " damage. Reason: " + reason);

        if (player != null) player.takeDamage(damagePerUnit);
        else System.out.println("[WARNING] Player is null.");
    }

    // -------------------------------------------------
    // CommandListener: beforeExecute
    // -------------------------------------------------
    public boolean before(String command, String[] args, CommandContext ctx) {
        Path currentDir = ctx.startDir;
        // Only intercept cd or mv
        if (!"cd".equals(command) && !"mv".equals(command)) {
            return true;
        }

        // No arguments → no navigation
        if (args.length < 1) return true;

        String target = args[0];

        // ---------------------------
        // 1. Allow backward movement
        // ---------------------------
        // If user does: cd .. , cd ../.., cd ../../something
        // → DO NOT block.
        if (target.startsWith("..")) {
            return true; // always allow upward navigation
        }

        // Compute actual target path
        Path targetDir = currentDir.resolve(target).normalize();

        // ---------------------------
        // 2. Block only if going FROM an enemy-held area outside
        // ---------------------------
        if (!hasBeenDefeated() && blocksNavigation(currentDir)) {
            damage("Cannot navigate: enemies are still present!");
            return false;
        }

        return true;
    }


    // -------------------------------------------------
    // CommandListener: afterExecute
    // -------------------------------------------------
    @Override
    public void after(String command, String[] args, CommandContext context, CommandResult result) {

        if (!result.getContext().endDir.toAbsolutePath().normalize().equals(enemyFilePath.getParent().normalize().toAbsolutePath()))
            return;

        // Failed command always deals damage
        if (!hasBeenDefeated() && !result.isSuccess() && norm(result.getContext().endDir).equals(norm(enemyFilePath.getParent()))) {
            damage("Command failed");
            return;
        }

        // If nothing deleted, no further logic
        List<Path> deleted = result.getContext().deleted;
        if (deleted.isEmpty()) return;

        // Normalize deleted paths
        Set<Path> deletedNorm = deleted.stream()
                .map(Enemy::norm)
                .collect(Collectors.toSet());

        Path myPathNorm = norm(enemyFilePath);

        // 1. Mark enemy defeated
        if ("rm".equals(command) && deletedNorm.contains(myPathNorm) && !hasBeenDefeated()) {
            defeated = true;
            System.out.println("[Enemy defeated] " + name + " has been defeated!");
        }

        if (!hasBeenDefeated() && blocksCommand(command)) {
            damage("Blocks by enemy");
        }
    }

    public String getId() {
        return id;
    }
}
