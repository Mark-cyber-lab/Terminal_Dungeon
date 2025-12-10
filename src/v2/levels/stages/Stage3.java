package v2.levels.stages;

import utilities.CLIUtils;
import v2.CommandContext;
import v2.CommandResult;
import v2.levels.Level;

import java.nio.file.Path;

public class Stage3 extends Stage {
    private static final String stageConfigPath = "./src/stages/stage3.txt";

    public Stage3(Level level) {
        super(3, level);
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


        while (true) {
            IO.println("Type 'cat scroll.txt' to read the scroll.\n");
            // The "That is not the command..." message is already handled in executeStrict method
            CommandResult res = level.sandbox.getExecutor().executeStrict("cat scroll.txt");
            if(res.isSuccess()) break;
        }

        CLIUtils.typewriter("Correct." + "\nNow explore all the corridors of dungeon and find", 25);
        CLIUtils.typewriter("the key that contains the magic word to move on next stage.", 25);

        boolean seenKey = false;

        Path keyPath = Path.of("./sandbox/mastery_chamber/next_stage/forbidden_library/key.txt");

        while(true) {
            IO.print(">> ");

            String input = IO.readln().trim();

            CommandResult result = level.sandbox.getExecutor().execute(input);

            if(!result.isSuccess()) continue;

            CommandContext context = result.getContext();

            // checks if the player has executed the cat command for the spefified key path
            if(context != null && "cat".equals(context.command)) {
                // CommandContext.read contains the Path of the file that is being read
                if(context.read.toString().equals(keyPath.normalize().toAbsolutePath().toString())){
                    seenKey = true;
                }
            }

            // if key is seen and player prompted done
            // only terminates the loop when the user enters done and has seen the key
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
        level.sandbox.getDirGenerator().generateFromConfig(stageConfigPath, level.sandbox.getSandBoxPath().toString());
        level.sandbox.getGranter().grant("pwd");
        level.sandbox.getGranter().grant("ls");
        level.sandbox.getGranter().grant("cd");
        level.sandbox.getGranter().grant("done");
        level.sandbox.getGranter().grant("cat");
    }
}
