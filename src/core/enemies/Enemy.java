package core.enemies;

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
 * LinuxCommandExecutorWithRegistry executor =
 *         new LinuxCommandExecutorWithRegistry("sandbox/root");
 *
 * // Create enemy in a folder
 * Enemy goblin = new Enemy("GoblinGuard", "goblin_01", Path.of("sandbox/root/guard_post/goblin.mob"));
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

    /** Enemy's display name. */
    private final String name;

    /** Unique enemy identifier. */
    private final String id;

    /** Path to the file representing the enemy in the sandbox. */
    private final Path enemyFilePath;

    /** Whether the enemy has been defeated. */
    private boolean hasBeenDefeated;

    /** Cached check for file path equality on command execution. */
    private boolean isSameFilePath;

    /**
     * Constructs a new Enemy.
     *
     * @param name      The display name of the enemy
     * @param id        Unique identifier for the enemy
     * @param enemyPath Path to the file representing the enemy
     */
    public Enemy(String name, String id, Path enemyPath) {
        this.name = name;
        this.id = id;
        this.enemyFilePath = enemyPath;
        this.hasBeenDefeated = false;
    }

    // --- Blocker interface ---

    /**
     * Returns the display name of the enemy.
     *
     * @return enemy name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Determines whether this enemy blocks access to a given folder.
     *
     * @param folder folder path to check
     * @return true if enemy is active and blocks the folder, false otherwise
     */
    @Override
    public boolean blocks(Path folder) {
        return !hasBeenDefeated && folder.equals(enemyFilePath);
    }

    /**
     * Checks if the enemy has been defeated.
     *
     * @return true if defeated, false otherwise
     */
    @Override
    public boolean isCleared() {
        return hasBeenDefeated;
    }

    /**
     * Marks the enemy as defeated and logs the event.
     */
    @Override
    public void clear() {
        hasBeenDefeated = true;
        log("[Enemy defeated] " + name + " at: " + enemyFilePath.toAbsolutePath());
    }

    // --- CommandListener interface ---

    /**
     * Reacts to player commands in the sandbox.
     * <p>
     * Enemies block actions like {@code touch} and can be defeated using {@code rm}.
     * </p>
     *
     * @param result the command executed by the player
     */
    @Override
    public void onCommand(CommandResult result) {
        String cmd = result.command().toLowerCase();

        if (!hasBeenDefeated && cmd.equals("touch")) {
            log("[Enemy blocks action] " + name + " prevents this at " + enemyFilePath.toAbsolutePath());
        }

        Path targetPath = Path.of(result.path()); // assume CommandResult provides a string path
        isSameFilePath = targetPath.toAbsolutePath().equals(enemyFilePath.toAbsolutePath());
        executeConditions(result, isSameFilePath);
    }

    /**
     * Executes conditions to determine if the enemy should be defeated.
     *
     * @param result       the player command
     * @param isSameFilePath true if the command's target path matches the enemy's path
     */
    protected void executeConditions(CommandResult result, boolean isSameFilePath) {
        String cmd = result.command().toLowerCase();

        if (!hasBeenDefeated && cmd.equals("rm") && result.subject() != null) {
            if (isSameFilePath) {
                clear();
            }
        }
    }

    // --- Additional helpers ---

    /**
     * Returns whether the enemy has been defeated.
     *
     * @return true if defeated, false otherwise
     */
    public boolean hasBeenDefeated() {
        return hasBeenDefeated;
    }

    /**
     * Removes this enemy from the executor, so it no longer blocks actions or reacts to commands.
     *
     * @param executor the LinuxCommandExecutorWithRegistry instance
     */
    public void removeFromExecutor(LinuxCommandExecutorWithRegistry executor) {
        if (hasBeenDefeated) {
            executor.removeBlocker(this);
            executor.removeCommandListener(this);
            log("[Listener removed] " + name + " will no longer block actions or react.");
        }
    }
}
