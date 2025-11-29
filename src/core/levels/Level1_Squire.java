package core.levels;

import core.Player;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Level1_Squire extends Level {

    private static final String configPath = "./src/stages/level1.txt";
    private static final String basePath = "/lv1";

    public Level1_Squire(Sandbox sandbox, Player player) {
        super(1, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        // level based setup, because there is nothing yet to do with stages
        sandbox.getDirGenerator().generateFromConfig(configPath, sandbox.getRootPath());
        Stage Stage1 = new Stage1();
        Stage Stage2 = new Stage2();
        Stage Stage3 = new Stage3();
        Stage Stage4 = new Stage4();
        addStage(Stage1);
        addStage(Stage2);
        addStage(Stage3);
        addStage(Stage4);
    }

    private class Stage1 extends Stage {
        Stage1() {
            super(1, Level1_Squire.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 1 — Orientation of the Young Squire"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("The old knight approaches you...", 30);
            CLIUtils.typewriter("\"To survive this dungeon lad, you must first know **where** you stand.\"", 30);
            IO.println("Type **pwd** to sense your current location.");
            IO.println();
            waitForStageCommand("pwd");
        }

        @Override
        public void onSuccessPlay() {
            //        player.remember("Learned pwd (location awareness)");
            IO.println("Stage complete! Proceeding to Stage 2...\n");
            CLIUtils.waitAnyKey();
        }

        @Override
        public void onFailedPlay(Exception exception) {

        }

        @Override
        public void setupEnvironment() {
            // No environments to set for this stage
        }
    }
    private class Stage2 extends Stage {
        Stage2() {
            super(2, Level1_Squire.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 2 — Sight of the Squire"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("A glowing orb hovers beside you...", 30);
            CLIUtils.typewriter("\"Now look around you, Squire. The dungeon hides much.\"", 30);
            IO.println("Type **ls** to reveal what lies in this chamber.");
            IO.println();
            waitForStageCommand("ls");
        }

        @Override
        public void onSuccessPlay() {
            //        player.remember("Learned pwd (location awareness)");
            IO.println("Stage complete! Proceeding to Stage 3...\n");
            CLIUtils.waitAnyKey();
        }

        @Override
        public void onFailedPlay(Exception exception) {

        }

        @Override
        public void setupEnvironment() {
            // No environments to set for this stage
        }
    }
    private class Stage3 extends Stage {
        Stage3() {
            super(3, Level1_Squire.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 3 — Map of the Training Grounds"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("A parchment unfolds in front of you.", 30);
            CLIUtils.typewriter("\"Before you enter deeper floors, learn to **visualize** the rooms.\"", 30);
            IO.println("Type **tree** to inspect the room structure.");
            IO.println();
            waitForStageCommand("tree");
        }

        @Override
        public void onSuccessPlay() {
            // Nothing to do, this is the last stage
        }

        @Override
        public void onFailedPlay(Exception exception) {

        }

        @Override
        public void setupEnvironment() {
            // No environments to set for this stage
        }
    }

    private class Stage4 extends Stage {
        Stage4() {
            super(4, Level1_Squire.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 4 — Exploration of the Dungeon Chambers"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("Now, brave Squire, you must explore the dungeon chambers.", 30);
            CLIUtils.typewriter("\"Move through the folders to discover their secrets.\"", 30);
            IO.println("Type **ls** to see what folders are here.");
            IO.println("Then type **cd ./<folder_name>** to enter a folder.");
            IO.println("Finally, type **pwd** to confirm your location.\n");
            IO.println("Type 'E' or 'Exit' if you are ready to exit the dungeon.\n");

            boolean success = false;
            while (!success) {
                System.out.print(">> ");
                String input = IO.readln().trim();

                if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit")) {
                    break;
                }
                if (input.startsWith("cd ")) {
                    sandbox.getExecutor().executeCommand(input.split(" "));
                } else if (input.equals("ls") || input.equals("pwd")) {
                    sandbox.getExecutor().executeCommand(input);
                } else {
                    IO.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                    IO.println("Try using **ls** or **cd ./<folder_name>** and **pwd**");
                    continue;
                }

                // simple success condition: pwd inside a subfolder of basePath
                Path current = sandbox.getRootPath() != null ? Paths.get(sandbox.getRootPath()) : null;
                if (current != null && !current.toAbsolutePath().equals(Paths.get(sandbox.getRootPath()).toAbsolutePath())) {
                    success = true;
                }
            }
        }

        @Override
        public void onSuccessPlay() {
            IO.println("Stage complete! You have learned to navigate through folders.\n");
            CLIUtils.waitAnyKey();
        }

        @Override
        public void onFailedPlay(Exception exception) {
            IO.println("Something went wrong while exploring. Try again.");
        }

        @Override
        public void setupEnvironment() {
            // no need to set up for this stage
        }
    }

    /**
     * Waits for the correct command for this stage.
     * Any other input will be rejected with a message.
     * Logs the player's command memory.
     */
    private void waitForStageCommand(String expectedCommand) {
        String input;
        while (true) {
            System.out.print(">> ");
            input = IO.readln().trim();

            // Log command to player's memory
//            player.addCommandHistory(input);

            if (!input.equals(expectedCommand)) {
                IO.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                IO.println("Try again with: **" + expectedCommand + "**");
                continue;
            }

            sandbox.getExecutor().executeCommand(input);

            break;
        }
    }

    @Override
    public String getDescription() {
        return "Level 1 — Squire (Navigation Training)";
    }

    @Override
    public void printLevelHeader() {
        CLIUtils.header(getLevelHeader(), 1);
    }

    @Override
    public String[] getLevelHeader() {
        return AsciiArt.getLevel1Squire();
    }

    @Override
    public void onBeforeInit() {
        IO.println("\n🏅 You are now a Squire — the lowest but bravest rank of Terminal Knights.");
        IO.println("Your training begins...\n");
    }

    @Override
    public void onLevelComplete() {
        IO.println("🎉 You have mastered the fundamentals, young Squire!");
        IO.println("You feel a surge of confidence as you prepare for Level 2...\n");
    }
}
