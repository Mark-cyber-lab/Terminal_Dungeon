import core.levels.Level1_Squire;
import engine.Sandbox;
import core.*;
import core.levels.Level;
import utilities.CLIUtils;
import utilities.AsciiArt;
import utilities.DebugLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewStage {

    private static final String SANDBOX_ROOT = "./sandbox";
    private final PlayerStats playerStats = new PlayerStats();
    private final Player player = new Player(playerStats);
    private final List<Level> levels = new ArrayList<>();
    private final Sandbox sandbox = new Sandbox(SANDBOX_ROOT);

    private void initializeLevels () {
        levels.add(new Level1_Squire(sandbox, player));
    }

    public void upStage() throws IOException {

        CLIUtils.clearScreen();

        CLIUtils.printCentered(AsciiArt.getTitleDungeon());

        CLIUtils.sleep(600);

        initializeLevels();

        PlayerConfig config = new PlayerConfig("./player.json", player);
        config.load();
        sandbox.loadBackup();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DebugLogger.log("Saving player configuration before exit...");
            try {
                config.save();
                sandbox.backup();
                DebugLogger.log("Player configuration saved successfully!");
            } catch (Exception e) {
                DebugLogger.log("Failed to save player configuration: " + e.getMessage());
            }
        }));

        IO.println();
        CLIUtils.typewriter("You awaken inside a dark cavern...", 20, true);
        CLIUtils.sleep(100);
        CLIUtils.typewriter("A distant voice whispers:", 30, true);
        CLIUtils.sleep(100);
        CLIUtils.typewriter("\"Only those who master the Terminal may survive.\"", 30, true);
        IO.println();
        CLIUtils.waitAnyKey();


        while (playerStats.isAlive()) {

            CLIUtils.header("A NEW DUNGEON CYCLE BEGINS");
            CLIUtils.typewriter("Current rank: " + player.getRankName(), 20, true);
            CLIUtils.waitAnyKey();

            for (int i = player.initialLevel - 1; i < levels.size(); i++) {
                Level level = levels.get(i);
                player.setLevelObj(level);

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
            } else {
                playerStats.setStage(1);
                playerStats.setLevel(1);
                playerStats.setCurrentDir("");
            }
        }

        CLIUtils.typewriter("Your legend will echo in the Terminal Dungeon.", 20);
        CLIUtils.waitAnyKey();
    }
}
