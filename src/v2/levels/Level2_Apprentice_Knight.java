package v2.levels;

import v2.CommandContext;
import v2.Player;
import v2.levels.stages.Stage;
import v2.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import v2.CommandResult;

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
        sandbox.getDirGenerator().generateFromConfig(configPath, sandbox.getSandBoxPath().toString());
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

            while (true) {
                IO.println("Type 'cat scroll.txt' to read the scroll.\n");
                CommandResult res = sandbox.getExecutor().executeStrict("cat scroll.txt");
                if(res.isSuccess()) break;
            }

            CLIUtils.typewriter("Correct." + "\nNow explore all the corridors of dungeon and find", 25);
            CLIUtils.typewriter("the key that contains the magic word to move on next stage.", 25);

            boolean seenKey = false;

            Path keyPath = Path.of("./sandbox/mastery_chamber/next_stage/forbidden_library/key.txt");

            while(true) {
                IO.print(">> ");

                String input = IO.readln().trim();

                CommandResult result = sandbox.getExecutor().execute(input);

                if(!result.isSuccess()) continue;

                CommandContext context = result.getContext();

                // checks if the player has executed the cat command for the spefified key path
                if(context != null && "cat".equals(context.command)) {
                    if(context.read.toString().equals(keyPath.normalize().toAbsolutePath().toString())){
                        seenKey = true;
                    }
                }

                // if key is seen and player prompted done
                if(context != null && "done".equals(context.command) && seenKey)
                    break;
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
            sandbox.getDirGenerator().generateFromConfig(stageConfigPath, sandbox.getSandBoxPath().toString());
            level.sandbox.getGranter().grant("pwd");
            level.sandbox.getGranter().grant("ls");
            level.sandbox.getGranter().grant("cd");
            level.sandbox.getGranter().grant("done");
            level.sandbox.getGranter().grant("cat");
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

            Path portalPath = Path.of("./sandbox/lore_master/next_stage/portal");

            while (true) {
                IO.print(">> ");
                String input = IO.readln().trim();

                commandResult = sandbox.getExecutor().execute(input);

                if(!commandResult.isSuccess()) continue;

                CommandContext context = commandResult.getContext();

                if("cd".equals(context.command)) {
                    if(context.endDir.toString().equals(portalPath.toAbsolutePath().normalize().toString())) break;
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
            sandbox.getDirGenerator().generateFromConfig(stageConfigPath, sandbox.getSandBoxPath().toString());
            level.sandbox.getGranter().grant("pwd");
            level.sandbox.getGranter().grant("ls");
            level.sandbox.getGranter().grant("cd");
            level.sandbox.getGranter().grant("done");
            level.sandbox.getGranter().grant("cat");
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