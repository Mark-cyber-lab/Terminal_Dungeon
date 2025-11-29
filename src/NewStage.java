import core.levels.Level1_Squire;
import engine.Sandbox;
import core.*;
import core.levels.Level;
import utilities.CLIUtils;
import utilities.AsciiArt;

import java.util.ArrayList;
import java.util.List;

public class NewStage {

    private static final String SANDBOX_ROOT = "./sandbox";
    private final Player player = new Player();

    public void upStage() {
        Sandbox sandbox = new Sandbox(SANDBOX_ROOT);

        CLIUtils.clearScreen();

        CLIUtils.printCentered(AsciiArt.getTitleDungeon());

        CLIUtils.sleep(600);

        IO.println();
        CLIUtils.typewriter("You awaken inside a dark cavern...", 20, true);
        CLIUtils.sleep(100);
        CLIUtils.typewriter("A distant voice whispers:", 30, true);
        CLIUtils.sleep(100);
        CLIUtils.typewriter("\"Only those who master the Terminal may survive.\"", 30, true);
        IO.println();
        CLIUtils.waitAnyKey();

        List<Level> levels = new ArrayList<>();
        levels.add(new Level1_Squire(sandbox, player));

        boolean retry = true;

        while (retry) {

            CLIUtils.header("A NEW DUNGEON CYCLE BEGINS");
            CLIUtils.typewriter("Current rank: " + player.getRankName(), 20);
            CLIUtils.waitAnyKey();

            for (Level level : levels) {

                CLIUtils.header(level.getLevelHeader(), 1);
                CLIUtils.center(level.getDescription());
                CLIUtils.sleep(500);
                CLIUtils.waitAnyKey("Press any key to start this level...");

                CLIUtils.loading("Preparing environment", 3, 500);
                level.setupEnvironment();

                CLIUtils.typewriter("Environment ready.", 20);
                CLIUtils.sleep(300);

                level.play();

                CLIUtils.header("LEVEL COMPLETE");
                CLIUtils.sleep(300);

                CLIUtils.typewriter("Energy flows through you...", 20);
                player.promoteLevel();
                CLIUtils.typewriter("New Rank: " + player.getRankName(), 20);
                CLIUtils.waitAnyKey("Press any key to continue...");
            }

            System.out.println();
            CLIUtils.typewriter("You have conquered the dungeon cycle.", 20);

            String input = IO.readln("Retry adventure? (yes/no): ").trim().toLowerCase();
            retry = input.equals("yes");

            if (retry) {
                CLIUtils.loading("Resetting dungeon", 3, 300);
                CLIUtils.waitAnyKey("Press any key to restart...");
                CLIUtils.clearScreen();
            }
        }

        CLIUtils.clearScreen();

        CLIUtils.printCentered(AsciiArt.getGameOver());

        CLIUtils.typewriter("Your legend will echo in the Terminal Dungeon.", 20);
        CLIUtils.waitAnyKey();
    }
}
