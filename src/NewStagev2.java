
import v2.PlayerConfig;
import core.PlayerStats;
import utilities.CLIUtils;
import utilities.AsciiArt;
import utilities.Loggable;
import v2.Player;
import v2.Sandbox;
import v2.levels.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewStagev2 implements Loggable {

    private static final String SANDBOX_ROOT = "./sandbox";
    private static final String INVENTORY_ROOT = "./sandbox/inventory";
    private final PlayerStats playerStats = new PlayerStats();
    private final Player player = new Player(playerStats, SANDBOX_ROOT);
    private final List<Level> levels = new ArrayList<>();
    private final Sandbox sandbox = new Sandbox(SANDBOX_ROOT, INVENTORY_ROOT);
    private boolean exitedNormally = false;

    private void initializeLevels() {
        levels.add(new Level1_Squire(sandbox, player));
        levels.add(new Level2_Apprentice_Knight(sandbox, player));
        levels.add(new Level3_Scout_Knight(sandbox, player));
        levels.add(new Level4_Warrior_Knight(sandbox,player));
        levels.add(new Level5_Arcane_Knight(sandbox,player));
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
            log("Saving player configuration before exit...");
            try {
                if (!exitedNormally)
                    sandbox.backup();
                config.save();
                log("Player configuration saved successfully!");
            } catch (Exception e) {
                log("Failed to save player configuration: " + e.getMessage());
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

            player.getStats().setStage(1);
            player.getStats().setLevel(1);
            player.getStats().setCurrentDir("");
            player.getStats().setHealth(100);

            if (!retry) {
                break;
            }
        }

        exitedNormally = true;
        CLIUtils.typewriter("Your legend will echo in the Terminal Dungeon.", 20);
        CLIUtils.waitAnyKey();
    }
}
