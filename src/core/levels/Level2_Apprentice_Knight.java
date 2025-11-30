package core.levels;

import core.Player;
import core.levels.stages.Stage;
import engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import utilities.CommandResult;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Level2_Apprentice_Knight extends Level {

    private static final String configPath = "./src/stages";
    private static final String basePath = "./lv2";

    public Level2_Apprentice_Knight(Sandbox sandbox, Player player) {
        super(2, player, sandbox, basePath);
    }

    @Override
    public void setup() {
        // level based setup, because there is nothing yet to do with stages
        sandbox.getDirGenerator().generateFromConfig(configPath, sandbox.getRootPath());
        Stage Stage3 = new Stage3();
        Stage Stage4 = new Stage4();

        addStage(Stage3);
        addStage(Stage4);
    }

    private class Stage3 extends Stage {
        private static final String stageConfigPath = "./src/stages/stage3.txt";

        Stage3() {
            super(3, Level2_Apprentice_Knight.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 3 — Secrets of the Lower Keep"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("The old knight hands you a worn scroll.", 25);
            CLIUtils.typewriter("\"To gain wisdom in this dungeon, you must first understand the scrolls.\"", 25);
            CLIUtils.typewriter("These scrolls contain knowledge of enemies, hidden keys, and secret passages.", 25);

            boolean success = false;
            boolean correct = false;
            while (!success) {
                IO.println("Type 'cat scroll.txt' to read the scroll.");
                IO.println();

                System.out.print(">> ");
                String input = IO.readln().trim();

                if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit")) {
                    break;
                }

                if (input.startsWith("cat")) {
                    sandbox.getExecutor().executeCommand(input.split(" "));
                    if(!correct) {
                        CLIUtils.typewriter("Correct. now explore the all the corridors of dungeon", 25);
                        CLIUtils.typewriter("you might uncover some secrets of the world.", 25);
                        correct = true;
                    }
                    CLIUtils.typewriter("Type \"Done\" if you want to move on to the next stage.", 25);
                } else {
                    IO.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                    IO.println("Try using **cat** ./<file_name.txt>**");
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
            IO.println("Stage complete! You have learned to read through files.\n");
            CLIUtils.waitAnyKey();
        }

        @Override
        public void onFailedPlay(Exception exception) {
            IO.println("Something went wrong while exploring. Try again.");
        }

        @Override
        public void setupEnvironment() {
            // Generate stage-specific environment from stage3.txt
            sandbox.getDirGenerator().generateFromConfig(stageConfigPath, sandbox.getRootPath());
        }
    }

    private class Stage4 extends Stage {
        private static final String stageConfigPath = "./src/stages/stage4.txt";

        Stage4() {
            super(4, Level2_Apprentice_Knight.this);
        }

        @Override
        public String[] getStageHeader() {
            return new String[]{"Stage 4 — Orientation of the Apprentice Knight"};
        }

        @Override
        public void play() {
            CLIUtils.typewriter("The knight nods with approval as you finish studying the scroll.", 25);
            CLIUtils.typewriter("\"Good. Now it's time to put that knowledge to use.\"", 25);
            CLIUtils.typewriter("He gestures toward the dim corridor ahead, urging you forward.", 25);
            CLIUtils.typewriter("Enter all the commands that you learned on past levels.", 25);
            CLIUtils.typewriter("Goal: Go to the next_stage/portal to move on the next stage", 25);
            CommandResult commandResult;

            boolean success = false;
            while (!success) {
                System.out.print(">> ");
                String input = IO.readln().trim();

                if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit")) {
                    break;
                }

                if (input.startsWith("cat") || input.startsWith("cd") || input.startsWith("ls") || input.startsWith("pwd")) {
                    commandResult = sandbox.getExecutor().executeCommand(input.split(" "));
                    if (input.startsWith("cd")){
                         String[] splittedResult = commandResult.path().split("\\\\");
                         int stringLength = splittedResult.length;

                         if (stringLength > 0) {
                             if (splittedResult[stringLength - 1].equalsIgnoreCase("portal")) {
                                 success = true;
                             }
                         }
                    }
                } else {
                    IO.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                    IO.println("Try using **cat** ./<file_name.txt>**");
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
            //        player.remember("Learned pwd (location awareness)");
            IO.println("Stage complete! Proceeding...\n");
            CLIUtils.waitAnyKey();
        }

        @Override
        public void onFailedPlay(Exception exception) {

        }

        @Override
        public void setupEnvironment() {
            // Generate stage-specific environment from stage4.txt
            sandbox.getDirGenerator().generateFromConfig(stageConfigPath, sandbox.getRootPath());
        }
    }

    /*
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
        IO.println("🎉 You have mastered the fundamentals, you Apprentice Knight!");
        IO.println("You feel a surge of confidence as you prepare for Level 3...\n");
    }
}