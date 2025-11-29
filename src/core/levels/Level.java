package core.levels;

import core.Player;

import java.util.ArrayList;

/**
 * Abstract base class for all Levels in Terminal Dungeon.
 * Each level should define its own stages and progression.
 */
public abstract class Level {

    /** Level number, e.g., 1, 2, 3 */
    protected final int levelNumber;
    protected Player player;
    protected final ArrayList<Stage> stages = new ArrayList<>();

    public Level(int levelNumber, Player player) {
        this.levelNumber = levelNumber;
        this.player = player;
    }

    /** Return the level number */
    public int getLevelNumber() {
        return levelNumber;
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void execute() {
        onBeforePlay();
        for (Stage stage : stages) {
            stage.execute();
        }
        onAfterPlay();
    }

    public abstract String getDescription();
    public abstract void printLevelHeader();
    public abstract String[] getLevelHeader();
    public abstract void onBeforePlay();
    public abstract void onAfterPlay();
    public abstract void setup();
}
