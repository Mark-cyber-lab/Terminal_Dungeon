package core.levels;

import core.Player;
import core.levels.stages.Stage;
import core.levels.stages.Stage7;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import utilities.DebugLogger;

public class Level4_Warrior_Knight extends Level {

    private static final String basePath = "./level_4";

    public Level4_Warrior_Knight(Sandbox sandbox, Player player) {
        super(4, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        try {
            sandbox.flush();
        } catch (Exception e) {
            DebugLogger.log(e.getMessage());
        }

        Stage Stage7 = new Stage7(this);
//        Stage Stage2 = new Stage2(this);
        addStage(Stage7);
//        addStage(Stage2);
    }

    @Override
    public String getDescription() {
        return "Level 4 — Warrior Knight (Orbs and Hidden Doors Exploration)";
    }

    @Override
    public void printLevelHeader() {
        CLIUtils.header(getLevelHeader(), 1);
    }

    @Override
    public String[] getLevelHeader() {
        return AsciiArt.getLevel4WarriorKnight();
    }

    @Override
    public void onBeforeInit() {

    }

    @Override
    public void onLevelComplete() {
        IO.println("You have mastered this level, Warrior Knight!");
        IO.println("You feel a surge of confidence as you prepare for Level 5...\n");
    }
}
