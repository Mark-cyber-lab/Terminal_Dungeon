package core.levels;

import core.Player;
import core.levels.stages.Stage;
import core.levels.stages.Stage11;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import utilities.DebugLogger;

public class Level6_Grandmaster_Knight extends Level {
    private static final String basePath = "./lv6";

    public Level6_Grandmaster_Knight(Sandbox sandbox, Player player) {
        super(6, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        try {
            sandbox.flush();
        } catch (Exception e) {
            DebugLogger.log(e.getMessage());
        }
        Stage Stage11 = new Stage11(this);
        addStage(Stage11);
    }

    @Override
    public String getDescription() {
        return "Level 6 — Grandmaster Knight (Strongest Knight)";
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
        IO.println("\nYou are now a Grandmaster Knight — The strongest of today.");
        IO.println("Your adventure begins...\n");
    }

    @Override
    public void onLevelComplete() {
        IO.println("🎉 You have finished your journey, Grandmaster Knight!");
        IO.println("You feel excited for your next adventure, new challenges awaits you...\n");
    }
}
