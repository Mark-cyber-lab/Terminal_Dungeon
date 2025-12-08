package v2.levels;

import v2.Player;
import v2.levels.stages.Stage;
import v2.levels.stages.Stage10;
import v2.levels.stages.Stage9;
import v2.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;

public class Level5_Arcane_Knight extends Level {
    private static final String basePath = "./lv5";

    public Level5_Arcane_Knight(Sandbox sandbox, Player player) {
        super(5, player, sandbox, basePath);
    }

    @Override
    public void setup() {
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
        return AsciiArt.getLevel5ArcaneKnight();
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
