package core.enemies;

import core.Player;
import core.listeners.Blocker;
import core.listeners.CommandListener;
import utilities.CommandResult;
import utilities.LinuxCommandExecutorWithRegistry;
import utilities.Loggable;

import java.nio.file.Path;

/**
 * Represents an enemy within the terminal dungeon environment.
 * <p>
 * Enemies act as blockers for certain directories and listen for specific commands
 * (like {@code rm}) to allow the player to defeat them. They can be registered
 * with a {@link LinuxCommandExecutorWithRegistry} as both a blocker and a command listener.
 * </p>
 * <p><b>Example usage:</b></p>
 * <pre>
 *  // if you are in side the Level or Stage class
 *  level.sandbox.getExecutor().addBlocker(goblin);\
 *
 * LinuxCommandExecutorWithRegistry executor =
 *         new LinuxCommandExecutorWithRegistry("sandbox/root");
 *
 * // Create enemy in a folder
 * // Register player so that it can take damage
 * Enemy goblin = new Enemy("GoblinGuard", "goblin_01", 5, Path.of("sandbox/root/guard_post/goblin.mob"))
 *                  .setPlayer(player);
 *
 * // Register enemy as blocker and listener
 * executor.addBlocker(goblin);
 *
 * // Player tries to touch a file in guarded folder
 * executor.setCurrentDirectory("guard_post");
 * executor.executeCommand("touch", "secret.txt");  // Goblin blocks
 *
 * // Player attacks goblin
 * executor.executeCommand("rm", "goblin.mob");
 *
 * // Remove listener
 * executor.removeCommandListener(goblin);
 * </pre>
 */
public class Enemy implements Blocker, CommandListener, Loggable {

    /**
     * Enemy's display name.
     */
    private final String name;

    /**
     * Unique enemy identifier.
     */
    private final String id;

    /**
     * Path to the file representing the enemy in the sandbox.
     */
    private final Path enemyFilePath;

    /**
     * Whether the enemy has been defeated.
     */
    private boolean hasBeenDefeated;

    /**
     * Cached check for file path equality on command execution.
     */
    private boolean isSameFilePath;

    /**
     * Base HP assumed for player. Can be adjusted externally.
     */
    private static final int BASE_HP = 100;

    /**
     * Damage dealt by this enemy type.
     */
    private final int damagePerUnit;

    private Player player;

    /**
     * Represents an enemy within the terminal dungeon environment. * <p> * Enemies act as blockers for certain directories and listen for specific commands * (like {@code rm}) to allow the player to defeat them. They can be registered * with a {@link LinuxCommandExecutorWithRegistry} as both a blocker and a command listener. * </p> * <p><b>Example usage:</b></p> * <pre> * LinuxCommandExecutorWithRegistry executor = * new LinuxCommandExecutorWithRegistry("sandbox/root"); * * // Create enemy in a folder * Enemy goblin = new Enemy("GoblinGuard", "goblin_01", Path.of("sandbox/root/guard_post/goblin.mob")); * * // Register enemy as blocker and listener * executor.addBlocker(goblin); * * // Player tries to touch a file in guarded folder * executor.setCurrentDirectory("guard_post"); * executor.executeCommand("touch", "secret.txt"); // Goblin blocks * * // Player attacks goblin * executor.executeCommand("rm", "goblin.mob"); * * // Remove listener * executor.removeCommandListener(goblin); * </pre>
     */
    public Enemy(String name, String id, Path enemyPath, int damagePerUnit) {
        this.name = name;
        this.id = id;
        this.enemyFilePath = enemyPath;
        this.hasBeenDefeated = false;
        this.damagePerUnit = damagePerUnit;
    }

    public Enemy setPlayer(Player player) {
        this.player = player;
        return this;
    }

    // --- Blocker interface ---

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Path getFilePath() {
        return this.enemyFilePath;
    }

    @Override
    public boolean blocks(Path folder) {
        Path absoluteFolder = folder.toAbsolutePath().normalize();
        Path enemyFolder = enemyFilePath.getParent().toAbsolutePath().normalize();

//        IO.println("enemyfilePath= " + enemyFilePath);
//        IO.println("folder       = " + absoluteFolder);
//        IO.println("enemyFolder  = " + enemyFolder);

        return !hasBeenDefeated && absoluteFolder.equals(enemyFolder);
    }

    @Override
    public boolean isCleared() {
        return hasBeenDefeated;
    }

    @Override
    public void clear() {
        hasBeenDefeated = true;
        log("[Enemy defeated] " + name + " at: " + enemyFilePath.toAbsolutePath());
    }

    // --- CommandListener interface ---

    @Override
    public void onCommand(CommandResult result) {
        String cmd = result.command().toLowerCase();

        Path targetPath = result.getFileFullPath();
        assert targetPath != null;
        isSameFilePath = targetPath.toAbsolutePath().equals(enemyFilePath.toAbsolutePath().normalize());

//        IO.println("11 target " + " at: " + targetPath.toAbsolutePath());
//        IO.println("11 enemy" + enemyFilePath.normalize().toAbsolutePath());
        // Wrong command while in same folder triggers damage
        if (!hasBeenDefeated && (!result.success() || (!cmd.equals("rm") && blocks(targetPath.getParent())))) {
            dealDamage("Wrong command executed in folder with enemy");
        }

        // Attempt to defeat enemy
        executeConditions(result, isSameFilePath);

        // After command, remaining enemies in folder deal damage
//        if (!hasBeenDefeated && blocks(targetPath)) {
//            dealDamage("Remaining enemies deal damage after command");
//        }
    }

    protected void executeConditions(CommandResult result, boolean isSameFilePath) {
        String cmd = result.command().toLowerCase();

        if (!hasBeenDefeated && cmd.startsWith("rm") && result.subject() != null) {
            if (isSameFilePath) {
                clear();
            }
        }
    }

    // --- Damage logic ---

    private void dealDamage(String reason) {
        log("[Enemy damage] " + name + " deals " + damagePerUnit + " HP damage. Reason: " + reason);
        if (player == null)
            log("Player instance not found. Cannot take damage to player");
        else
            player.takeDamage(damagePerUnit);
    }

    // --- Additional helpers ---

    public boolean hasBeenDefeated() {
        return hasBeenDefeated;
    }

    public void removeFromExecutor(LinuxCommandExecutorWithRegistry executor) {
        if (hasBeenDefeated) {
            executor.removeBlocker(this);
            executor.removeCommandListener(this);
            log("[Listener removed] " + name + " will no longer block actions or react.");
        }
    }

    public int getDamagePerUnit() {
        return damagePerUnit;
    }
}
