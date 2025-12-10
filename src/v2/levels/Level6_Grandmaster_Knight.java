package v2.levels;

import v2.Player;
import v2.levels.stages.Stage;
import v2.levels.stages.Stage11;
import v2.levels.stages.Stage12;
import v2.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;

public class Level6_Grandmaster_Knight extends Level {

    public Level6_Grandmaster_Knight(Sandbox sandbox, Player player) {
        super(6, player, sandbox);
    }

    @Override
    public void setup() {
        Stage Stage11 = new Stage11(this);
        Stage stage12 = new Stage12(this);

        addStage(Stage11);
        addStage(stage12);
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
        return AsciiArt.getLevel6GrandmasterKnight();
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
