package core.levels;

import core.Player;
import engine.Sandbox;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Abstract base class for all Levels in Terminal Dungeon.
 * Each level should define its own stages and progression.
 */
public abstract class Level {

    /**
     * Level number, e.g., 1, 2, 3
     */
    protected final Sandbox sandbox;
    protected Player player;
    protected final ArrayList<Stage> stages = new ArrayList<>();
    protected final String basePath;

    public Level(int levelNumber, Player player, Sandbox sandbox, String basePath) {
        player.getStats().setLevel(levelNumber);
        this.player = player;
        this.basePath = basePath;
        this.sandbox = sandbox;
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void execute() {
        sandbox.updateRootDir(prev -> prev + basePath);
        onBeforeInit();
        for (int stageNumber = player.getStats().getStage() - 1; stageNumber < (stages.size() * player.getStats().getLevel()); stageNumber++) {
            player.getStats().setStage(stageNumber + 1);
            stages.get(stageNumber).execute();
        }
        onLevelComplete();
        if (!basePath.isEmpty()) sandbox.updateRootDir(prev -> prev + "/../");
    }

    public abstract String getDescription();

    public abstract void printLevelHeader();

    public abstract String[] getLevelHeader();

    public abstract void onBeforeInit();

    public abstract void onLevelComplete();

    public abstract void setup();
}
