
import player.PlayerConfig;
import player.PlayerStats;
import utilities.CLIUtils;
import utilities.AsciiArt;
import utilities.Loggable;
import player.Player;
import engine.Sandbox;
import engine.SandboxBackupManager;
import leaderboards.LeaderBoards;
import levels.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewStage implements Loggable {

    private PlayerConfig config;
    private String userName = "player";
    private static final String SANDBOX_ROOT = "sandbox";
    private static final String INVENTORY_ROOT = "sandbox/inventory";
    private final PlayerStats playerStats = new PlayerStats();
    private final Player player = new Player(playerStats, SANDBOX_ROOT);
    private final List<Level> levels = new ArrayList<>();
    private final Sandbox sandbox = new Sandbox(SANDBOX_ROOT, INVENTORY_ROOT, this.player.getStats());
    private boolean exitedNormally = false;

    private void initializeLevels() {
        levels.add(new Level1_Squire(sandbox, player));
        levels.add(new Level2_Apprentice_Knight(sandbox, player));
        levels.add(new Level3_Scout_Knight(sandbox, player));
        levels.add(new Level4_Warrior_Knight(sandbox, player));
        levels.add(new Level5_Arcane_Knight(sandbox, player));
        levels.add(new Level6_Grandmaster_Knight(sandbox, player));
    }

    // ----------------------
    // Helper Methods
    // ----------------------
    private void executeLevel(Level level) {
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
    }

    private void resetPlayerStats(PlayerStats stats) {
        stats.setStage(1);
        stats.setLevel(1);
        stats.setCurrentDir("");
        stats.setHealth(100);
        player.initialLevel = 1;
    }

    private String getPlayerPath() {
        return "" + userName + "_data.json";
    }

    public void upStage() throws IOException {

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

        IO.print(
                "Menu:\n[1] Start New Adventure\n[2] See Leaderboards\n[3] Exit\n\nPlease select an option: ");

        String choice;
        
        do {
            choice = IO.readln().trim();

            if (choice.equals("1")) { // starts a new game

                String playerPath;
                while (true) {
                    IO.print("Enter your adventurer name:  ");
                    String name = IO.readln().trim();
                    if (!name.isEmpty()) {
                        userName = name.toLowerCase();
                        break;
                    } else {
                        IO.println("Name cannot be empty. Please enter a valid name.");
                    }
                }

                playerPath = getPlayerPath();
                config = new PlayerConfig(playerPath, player);

                boolean load = sandbox.getBackupManager().confirmLoadBackup(userName);
                if (load)
                    config.load();
                else {
                    IO.println("We'll create the new adventure for you.");
                    resetPlayerStats(playerStats);
                }
            } else if (choice.equals("2")) { // shows the leaderboards
                CLIUtils.clearScreen();
                LeaderBoards.retrieveLeaderBoardData();
                String c = IO.readln().trim();

                if (c.equalsIgnoreCase("y")) {
                    LeaderBoards.clearLeaderboardData();
                    CLIUtils.typewriter("\nLeaderboards cleared.", 20);
                    CLIUtils.waitAnyKey();
                }
                upStage();
                return;
            } else if (choice.equals("3")) { // exit the game
                CLIUtils.typewriter("Farewell, adventurer.", 20);
                CLIUtils.waitAnyKey();
                return;
            }
        } while (!choice.equals("1"));

        CLIUtils.waitAnyKey();
        initializeLevels();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log("Saving player configuration before exit...");
            try {
                if (exitedNormally
                        || sandbox.getBackupManager().backup(SandboxBackupManager.BackupMode.BACKUP_ONLY_INVENTORY,
                                userName)) {
                    config.save();
                }
                log("Player configuration saved successfully!");
            } catch (Exception e) {
                log("Failed to save player configuration: " + e.getMessage());
            }
        }));

        do {
            if (playerStats.isAlive()) {
                CLIUtils.header("A NEW DUNGEON CYCLE BEGINS");
                CLIUtils.typewriter("Current rank: " + player.getRankName(), 20, true);
                CLIUtils.waitAnyKey();

                for (int i = player.initialLevel - 1; i < levels.size(); i++) {
                    if (!playerStats.isAlive())
                        break;

                    Level level = levels.get(i);
                    player.setLevelObj(level);

                    executeLevel(level);

                    if (!playerStats.isAlive())
                        break;

                    CLIUtils.header("LEVEL COMPLETE");
                    CLIUtils.sleep(300);
                    CLIUtils.typewriter("Energy flows through you...", 20);
                    player.promoteLevel();
                    CLIUtils.typewriter("New Rank: " + player.getRankName(), 20);
                    CLIUtils.waitAnyKey("Press any key to continue...");
                }
            }
            IO.println();
            if (playerStats.isAlive())
                CLIUtils.typewriter("You have conquered the dungeon cycle.", 20);

            String input = IO.readln("Retry adventure? (yes/no): ").trim().toLowerCase();
            boolean retry = input.equals("yes");

            LeaderBoards.addLeaderboardEntry(userName, playerStats.getHealth(),
                    playerStats.getLevel());

            if (retry) {
                resetPlayerStats(playerStats);
            }

            if (!retry)
                break;

        } while (true);

        exitedNormally = true;
        CLIUtils.typewriter("Your legend will echo in the Terminal Dungeon.", 20);
        CLIUtils.waitAnyKey();
    }
}
