package core;

import core.listeners.Blocker;
import core.listeners.CommandListener;
import utilities.CommandResult;
import utilities.LinuxCommandExecutorWithRegistry;
import utilities.Loggable;

import java.nio.file.Path;

public class Enemy implements Blocker, CommandListener, Loggable {

    private final String name;
    private final String id;
    private final Path enemyPath;
    private boolean hasBeenDefeated;

    public Enemy(String name, String id, Path enemyPath) {
        this.name = name;
        this.id = id;
        this.enemyPath = enemyPath;
        this.hasBeenDefeated = false;
    }

    // --- Blocker interface ---
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean blocks(Path folder) {
        // Enemy blocks actions if it's in the folder and not beaten
        return !hasBeenDefeated && folder.equals(enemyPath);
    }

    @Override
    public boolean isCleared() {
        return hasBeenDefeated;
    }

    @Override
    public void clear() {
        hasBeenDefeated = true;
        log("[Enemy defeated] " + name + " in folder: " + enemyPath);
    }

    // --- CommandListener interface ---
    @Override
    public void onCommand(CommandResult result) {
        // Enemy reacts if player tries to pick up or delete items
        String cmd = result.command().toLowerCase();
        if (!hasBeenDefeated && (cmd.equals("touch") || cmd.equals("rm"))) {
            log("[Enemy blocks action] " + name + " prevents this in " + enemyPath);
        }

        // Enemy can be defeated if a special command is used
        if (!hasBeenDefeated && cmd.equals("rm") && result.subject() != null) {
            if (result.subject().equalsIgnoreCase(name)) {
                clear();
            }
        }
    }

    // --- Additional helper ---
    public boolean hasBeenDefeated() {
        return hasBeenDefeated;
    }

    public void removeFromExecutor(LinuxCommandExecutorWithRegistry executor) {
        if (hasBeenDefeated) {
            executor.removeBlocker(this);          // removes blocking behavior
            executor.removeCommandListener(this);  // removes command listening behavior
            log("[Listener removed] " + name + " will no longer block actions or react.");
        }
    }
}
