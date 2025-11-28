package core.levels;

import core.Player;

/**
 * Abstract base class for all Levels in Terminal Dungeon.
 * Each level should define its own stages and progression.
 */
public abstract class Level {

    /** Level number, e.g., 1, 2, 3 */
    protected final int levelNumber;
    protected Player player;

    public Level(int levelNumber, Player player) {
        this.levelNumber = levelNumber;
        this.player = player;
    }

    /** Return the level number */
    public int getLevelNumber() {
        return levelNumber;
    }

    public abstract String getDescription();

    public abstract void play();

    public abstract void setupEnvironment();
}
