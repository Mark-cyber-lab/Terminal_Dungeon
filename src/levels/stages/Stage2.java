package levels.stages;

import utilities.CLIUtils;
import gameplay.CommandResult;
import elements.items.Letter;
import levels.Level;

public class Stage2 extends Stage {
    private static final String configPath = "./src/stages/stage2.txt";

    public Stage2(Level level) {
        super(2, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[] { "Stage 2 — The Lost Letter" };
    }

    @Override
    public void play() {
        // Stage 3: Lost Letter / Navigation Test
        CLIUtils.typewriter("The dungeon is silent, yet filled with hidden secrets.", 30);
        CLIUtils.typewriter("The Old Knight appears: 'A lost letter awaits you, hidden beyond familiar paths.'", 30);
        CLIUtils.typewriter("'Use ls, cd, and tree wisely—strange paths often hide the truth.'", 30);
        CLIUtils.typewriter("Find and read the letter to complete Stage 3. Use: cat <filename>.txt", 30);

        String input;

        while (true) {
            System.out.print(">> ");
            input = IO.readln().trim();

            CommandResult result = level.sandbox.getExecutor().execute(input);

            if (result.isSuccess() && "cat".equals(result.getContext().command)
                    && result.getContext().read.toString().endsWith("dear_squire.txt")) {
                // obtain letter
                Letter letter = new Letter("dear_squire", result);
                level.sandbox.getInventory().addItem(letter);
                break;
            }

        }

        CLIUtils.typewriter("Keep that letter fellas, and let's continue our journey. Type done if you are ready.", 30);
        level.sandbox.getExecutor().executeStrict("done");

        CLIUtils.typewriter("The Old Knight nods: 'Your Stage 2 training is complete. Greater adventures await…'", 30);
        CLIUtils.typewriter("\"Stage 2 complete. Prepare for what comes next.\"", 30);
    }

    @Override
    public void onSuccessPlay() {
        // player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 3...\n");
        CLIUtils.waitAnyKey();
    }

    @Override
    public void onFailedPlay(Exception exception) {

    }

    @Override
    public void setupEnvironment() {
        level.sandbox.getDirGenerator().generateFromConfig(configPath, level.sandbox.getSandBoxPath().toString());
        level.sandbox.getGranter().grant("pwd");
        level.sandbox.getGranter().grant("ls");
        level.sandbox.getGranter().grant("cd");
        level.sandbox.getGranter().revoke("tree");
        level.sandbox.getGranter().grant("done");
        level.sandbox.getGranter().grant("cat");
    }
}
