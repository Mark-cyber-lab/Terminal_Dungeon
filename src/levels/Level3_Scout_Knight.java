package levels;

import player.Player;
import levels.stages.Stage;
import levels.stages.Stage5;
import levels.stages.Stage6;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;

public class Level3_Scout_Knight extends Level {

    public Level3_Scout_Knight(Sandbox sandbox, Player player) {
        super(3, player, sandbox);
    }

    @Override
    public void setup() {
        Stage Stage5 = new Stage5(this);
        Stage Stage6 = new Stage6(this);

        addStage(Stage5);
        addStage(Stage6);
    }

    @Override
    public String getDescription() {
        return "Level 3 — Scout Knight (Amateur Force)";
    }

    @Override
    public void printLevelHeader() {
        CLIUtils.header(getLevelHeader(), 1);
    }

    @Override
    public String[] getLevelHeader() {
        return AsciiArt.getLevel3ScoutKnight();
    }

    @Override
    public void onBeforeInit() {
        IO.println("\nYou are now a Scout Knight — a hunter of monsters.");
        IO.println("Your adventure begins...\n");
    }

    @Override
    public void onLevelComplete() {
        IO.println("🎉 You have mastered the fundamentals, you Scout Knight!");
        IO.println("You feel a surge of confidence as you prepare for Level 4...\n");
    }
}