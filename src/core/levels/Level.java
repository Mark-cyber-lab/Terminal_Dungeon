package core.levels;

import core.Player;
import core.levels.stages.Stage;
import engine.Sandbox;

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
        onBeforeInit();
        int currentStage = player.getStats().getStage(); // start from current stage
        int lastStageNumber = stages.stream()
                .mapToInt(Stage::getStageNumber)
                .max()
                .orElse(currentStage);

        for (int stageNumber = currentStage; stageNumber <= lastStageNumber; stageNumber++) {
            int finalStageNumber = stageNumber; // now matches stage.getStageNumber()

            stages.stream()
                    .filter(stage -> stage.getStageNumber() == finalStageNumber)
                    .findFirst()
                    .ifPresent(stage -> {
                        stage.execute(
                                () -> {}, // Before setup lambda
                                () -> sandbox.updateRootDir(prev -> prev + basePath) // After setup lambda
                        );
                        // Update player stage to next stage
                        player.getStats().setStage(finalStageNumber + 1);
                    });
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
