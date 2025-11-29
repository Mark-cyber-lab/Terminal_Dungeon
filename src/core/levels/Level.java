package core.levels;

import core.Player;
import engine.Sandbox;

import java.util.ArrayList;

/**
 * Abstract base class for all Levels in Terminal Dungeon.
 * Each level should define its own stages and progression.
 */
public abstract class Level {

    /** Level number, e.g., 1, 2, 3 */
    protected final Sandbox sandbox;
    protected final int levelNumber;
    protected Player player;
    protected final ArrayList<Stage> stages = new ArrayList<>();
    protected final String basePath;

    public Level(int levelNumber, Player player, Sandbox sandbox, String basePath) {
        this.levelNumber = levelNumber;
        this.player = player;
        this.basePath = basePath;
        this.sandbox = sandbox;
    }

    /** Return the level number */
    public int getLevelNumber() {
        return levelNumber;
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void execute() {
        sandbox.updateRootDir(prev -> prev + basePath);
        onBeforeInit();
        for (Stage stage : stages) {
            stage.execute();
        }
        onLevelComplete();
        sandbox.updateRootDir(prev -> prev + "/../");
    }

    public abstract String getDescription();
    public abstract void printLevelHeader();
    public abstract String[] getLevelHeader();
    public abstract void onBeforeInit();
    public abstract void onLevelComplete();
    public abstract void setup();
}
