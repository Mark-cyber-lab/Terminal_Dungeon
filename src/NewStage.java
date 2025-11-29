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

        while (player.isAlive()) {

            CLIUtils.header("A NEW DUNGEON CYCLE BEGINS");
            CLIUtils.typewriter("Current rank: " + player.getRankName(), 20, true);
            CLIUtils.waitAnyKey();

            for (Level level : levels) {
                CLIUtils.clearScreen();
                level.printLevelHeader();
                CLIUtils.center(level.getDescription());
                CLIUtils.sleep(500);
                CLIUtils.waitAnyKey("Press any key to start this level...");

                CLIUtils.loading("Preparing environment", 3, 500);
                level.setup();

                CLIUtils.typewriter("Environment ready.", 20);
                CLIUtils.sleep(300);

                level.execute();

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
            boolean retry = input.equals("yes");

            if (!retry) {
                player.setHealth(0);
            }
        }

        CLIUtils.typewriter("Your legend will echo in the Terminal Dungeon.", 20);
        CLIUtils.waitAnyKey();
    }
}
