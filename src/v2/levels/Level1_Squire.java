package v2.levels;

import v2.Player;
import v2.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import utilities.DebugLogger;
import v2.levels.stages.Stage;
import v2.levels.stages.Stage1;
import v2.levels.stages.Stage2;

public class Level1_Squire extends Level {

    private static final String basePath = "./level_1";

    public Level1_Squire(Sandbox sandbox, Player player) {
        super(1, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        Stage Stage1 = new Stage1(this);
        Stage Stage2 = new Stage2(this);
        addStage(Stage1);
        addStage(Stage2);
    }

    @Override
    public String getDescription() {
        return "Level 1 — Squire (Navigation Training)";
    }

    @Override
    public void printLevelHeader() {
        CLIUtils.header(getLevelHeader(), 1);
    }

    @Override
    public String[] getLevelHeader() {
        return AsciiArt.getLevel1Squire();
    }

    @Override
    public void onBeforeInit() {

    }

    @Override
    public void onLevelComplete() {
        IO.println("🎉 You have mastered the fundamentals, young Squire!");
        IO.println("You feel a surge of confidence as you prepare for Level 2...\n");
    }
}
