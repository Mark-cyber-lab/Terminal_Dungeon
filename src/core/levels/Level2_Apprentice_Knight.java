package core.levels;

import core.Player;
import core.levels.stages.Stage;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import utilities.CommandResult;

public class Level2_Apprentice_Knight extends Level {

    private static final String configPath = "./src/stages/level2.txt";
    private static final String basePath = "/lv2";

    public Level2_Apprentice_Knight(Sandbox sandbox, Player player) {
        super(1, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        // level based setup, because there is nothing yet to do with stages
        sandbox.getDirGenerator().generateFromConfig(configPath, sandbox.getRootPath());
        Stage Stage4 = new Stage4();

        addStage(Stage4);
    }

    private class Stage4 extends Stage {
        Stage4() {
            super(4, Level2_Apprentice_Knight.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 4 — Orientation of the Young Squire"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("The old knight hands you a worn scroll.", 25);
            CLIUtils.typewriter("\"To gain wisdom in this dungeon, you must first understand the scrolls.\"", 25);
            CLIUtils.typewriter("These scrolls contain knowledge of enemies, hidden keys, and secret passages.", 25);

            IO.println("Type 'cat scroll.txt' to read the scroll.");
            IO.println();

            waitForStageCommand("cat scroll.txt");
        }


        @Override
        public void onSuccessPlay() {
            //        player.remember("Learned pwd (location awareness)");
            IO.println("Stage complete! Proceeding...\n");
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

    /**
     * Waits for the correct command for this stage.
     * Any other input will be rejected with a message.
     * Logs the player's command memory.
     */
    private CommandResult waitForStageCommand(String expectedCommand) {
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

            return sandbox.getExecutor().executeCommand(input.split(" "));
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
        return AsciiArt.getLevel2ApprenticeKnight();
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
