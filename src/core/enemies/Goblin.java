package core.enemies;

import utilities.LinuxCommandExecutorWithRegistry;

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
 * Enemy goblin = new Goblin("01", Path.of("sandbox/root/guard_post/goblin.mob"))
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
public class Goblin extends Enemy {
    private static final String NAME = "goblin";
    private static final int DAMAGE = 5;

    public Goblin(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }
}
