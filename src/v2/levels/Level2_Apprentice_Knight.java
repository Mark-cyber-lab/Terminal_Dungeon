package v2.levels;

import v2.Player;
import v2.levels.stages.Stage;
import v2.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import v2.levels.stages.Stage3;
import v2.levels.stages.Stage4;

public class Level2_Apprentice_Knight extends Level {

    private static final String configPath = "./src/stages";
    private static final String basePath = "./lv2";

    public Level2_Apprentice_Knight(Sandbox sandbox, Player player) {
        super(2, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        Stage Stage3 = new Stage3(this);
        Stage Stage4 = new Stage4(this);

        addStage(Stage3);
        addStage(Stage4);
    }


    @Override
    public String getDescription() {
        return "Level 2 — Apprentice Knight (Scroll Reading)";
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
        IO.println("\nYou are now an Apprentice Knight — a learner of secrets and a seeker of deeper mastery.");
        IO.println("Your adventure begins...\n");
    }

    @Override
    public void onLevelComplete() {
        IO.println("Keep up the progress, you Apprentice Knight!");
        IO.println("You feel a surge of confidence as you prepare for Level 3...\n");
    }
}