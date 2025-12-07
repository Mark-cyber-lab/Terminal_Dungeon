package v2.levels;

import core.storage.Inventory;
import v2.Player;
import v2.levels.stages.Stage;
import v2.Sandbox;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Abstract base class for all Levels in Terminal Dungeon.
 * Each level should define its own stages and progression.
 */
public abstract class Level {

    /**
     * Level number, e.g., 1, 2, 3
     */
    public final Sandbox sandbox;
    public Player player;
    protected final ArrayList<Stage> stages = new ArrayList<>();
    protected final String basePath;

    public Level(int levelNumber, Player player, Sandbox sandbox, String basePath) {
        player.getStats().setLevel(levelNumber);
        this.player = player;
        this.basePath = basePath;
        this.sandbox = sandbox;
    }

    public String getSandboxPath() {
        return player.getSandboxRoot();
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void execute() {
        onBeforeInit();
        int currentStage = player.getStats().getStage(); // player's current stage
        int stagesPerLevel = 2; // 2 stages per level
        int currentLevel = player.getStats().getLevel();

        // Determine first and last stage number of this level
        int firstStageOfLevel = (currentLevel - 1) * stagesPerLevel + 1;
        int lastStageOfLevel = currentLevel * stagesPerLevel;

//        IO.println("Current Stage: " + currentStage);
//        IO.println("Current Level: " + currentLevel);
        // Ensure we start from the player's current stage if already in the middle of level
        int startStage = Math.max(currentStage, firstStageOfLevel);
//        IO.println("Current Stage: " + startStage);
        for (int stageNumber = startStage; stageNumber <= lastStageOfLevel; stageNumber++) {
            int finalStageNumber = stageNumber;
//            IO.println("Final Stage: " + finalStageNumber);
            stages.stream()
                    .filter(stage -> stage.getStageNumber() == finalStageNumber)
                    .findFirst()
                    .ifPresent(stage -> {
                        stage.execute(
                                () -> {
                                }, // Before setup lambda
                                () -> {
//                                    IO.println("base path is " + basePath);
                                    sandbox.getExecutor().execute("cd "+ Path.of(basePath));
                                } // After setup lambda
                        );

                        // Update player stage to next stage
                        player.getStats().setStage(finalStageNumber + 1);
                    });
        }
        onLevelComplete();
        if (!basePath.isEmpty()) sandbox.getExecutor().execute("cd "+ sandbox.getSandBoxPath().toAbsolutePath().normalize());
    }

    public abstract String getDescription();

    public abstract void printLevelHeader();

    public abstract String[] getLevelHeader();

    public abstract void onBeforeInit();

    public abstract void onLevelComplete();

    public abstract void setup();
}
