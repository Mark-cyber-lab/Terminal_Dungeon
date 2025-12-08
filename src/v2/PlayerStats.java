package v2;

import v2.AliveStats;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class representing persistent stats for any game entity.
 * Includes level, stage, current directory, and health.
 */
public class PlayerStats extends AliveStats {

    /** Current level of the entity */
    protected int level = 1;

    /** Current stage number */
    protected int stage = 1;

    /** Current working directory (sandbox context) */
    protected String currentDir = "./";

    /** Granted commands */
    protected Set<String> grantedCommands = new HashSet<>();

    // ---------------------------
    // LEVEL
    // ---------------------------
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
//        IO.println("Player stats level: " + level);
        if (level < 1) level = 1;
        this.level = level;
    }

    // ---------------------------
    // STAGE
    // ---------------------------
    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        if (stage < 1) stage = 1;
        this.stage = stage;
    }

    // ---------------------------
    // CURRENT DIRECTORY
    // ---------------------------
    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        if (currentDir != null && !currentDir.isBlank()) {
            this.currentDir = currentDir;
        }
    }

    // ---------------------------
    // GRANTED COMMANDS
    // ---------------------------
    @FunctionalInterface
    public interface GrantedCallback {
        Set<String> onUpdate(Set<String> previous);
    }

    public Set<String> getGrantedCommands() {
        return Set.copyOf(grantedCommands);
    }

    public void setGrantedCommands(Set<String> grantedCommands) {
        this.grantedCommands = new HashSet<>(grantedCommands);
    }

    /**
     * Updates granted commands via a callback.
     * The callback receives the previous set and returns a new set.
     */
    public void updateGrantedCommands(GrantedCallback callback) {
        if (callback != null) {
            this.grantedCommands = new HashSet<>(callback.onUpdate(this.grantedCommands));
        }
    }

    @Override
    public String toString() {
        return "PlayerStats {" +
                "level=" + level +
                ", stage=" + stage +
                ", currentDir='" + currentDir + '\'' +
                ", grantedCommands=" + grantedCommands +
                '}';
    }
}
