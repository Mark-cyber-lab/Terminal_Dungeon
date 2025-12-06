package core.levels;

import core.Player;
import core.levels.stages.Stage;
import core.levels.stages.Stage10;
import core.levels.stages.Stage9;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import utilities.DebugLogger;

public class Level5_Arcane_Knight extends Level {
    private static final String basePath = "./lv5";

    public Level5_Arcane_Knight(Sandbox sandbox, Player player) {
        super(5, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        try {
            sandbox.flush();
        } catch (Exception e) {
            DebugLogger.log(e.getMessage());
        }

        Stage Stage9 = new Stage9(this);
        Stage Stage10 = new Stage10(this);

        addStage(Stage9);
        addStage(Stage10);
    }

    @Override
    public String getDescription() {
        return "Level 5 — Arcane Knight (Master Spell Knight)";
    }

    @Override
    public void printLevelHeader() {
        CLIUtils.header(getLevelHeader(), 1);
    }

    @Override
    public String[] getLevelHeader() {
        return AsciiArt.getLevel2ApprenticeKnight();
    }

    @Override
    public void onBeforeInit() {
        IO.println("\nYou are now an Arcane Knight — a Veteran Spellblade.");
        IO.println("Your adventure begins...\n");
    }

    @Override
    public void onLevelComplete() {
        IO.println("🎉 You have mastered the fundamentals, you Arcane Knight!");
        IO.println("You feel a surge of confidence as you prepare for Level 6...\n");
    }
}
