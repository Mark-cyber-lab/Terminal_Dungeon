package core.levels;

import core.Player;
import core.levels.stages.Stage;
import core.levels.stages.Stage1;
import core.levels.stages.Stage2;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;

public class Level1_Squire extends Level {

    private static final String basePath = "/level_1";

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
        IO.println("\n🏅 You are now a Squire — the lowest but bravest rank of Terminal Knights.");
        IO.println("Your training begins...\n");
    }

    @Override
    public void onLevelComplete() {
        IO.println("🎉 You have mastered the fundamentals, young Squire!");
        IO.println("You feel a surge of confidence as you prepare for Level 2...\n");
    }
}
