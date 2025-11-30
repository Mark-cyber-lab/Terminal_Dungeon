package core.levels.stages;

import core.levels.Level;
import utilities.CLIUtils;
import utilities.CommandResult;

import java.util.Objects;

public class Stage2 extends Stage {
    private static final String configPath = "./src/stages/revised/stage2.txt";

    public Stage2(Level level) {
        super(2, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 2 — The Lost Letter"};
    }

    @Override
    public void play() {
        // Stage 3: Lost Letter / Navigation Test
        CLIUtils.typewriter("The dungeon is quiet, yet the air hums with hidden secrets.", 30);
        CLIUtils.typewriter("The Old Knight appears, his armor creaking softly:", 30);
        CLIUtils.typewriter("'Squire… a lost letter awaits your discovery. It does not dwell in familiar paths. Your mission is to find it and read its contents.'", 30);
        CLIUtils.typewriter("'Use all you have learned: ls to observe, cd to move and tree to inspect structures. Perhaps a hallway you explored before holds a clue, or a chamber you rarely inspect. Strange paths sometimes hide the greatest truths.'", 30);
        CLIUtils.typewriter("'Remember your lessons from the training halls. Apply your knowledge and explore wisely.'", 30);
        CLIUtils.typewriter("'Pay attention to the names of files — the lost letter bears a name meant for you…'", 30);
        CLIUtils.typewriter("You begin exploring, tracing familiar halls and venturing into less obvious corners...", 30);
        CLIUtils.typewriter("Once you have located the letter, you must read it to complete your Stage 3 trial.", 30);
        CLIUtils.typewriter("Use the command: cat <filename>.txt", 30);

        String input;

        while (true) {
            System.out.print(">> ");
            input = IO.readln().trim();

            CommandResult result = level.sandbox.getExecutor().executeCommand(input.split(" "));

            if(Objects.equals(result.command(), "cat") && result.subject().contains("dear_squire.txt")){
                // sample
                break;
            }

        }

        CLIUtils.typewriter("A warm glow fills the archives as you read the letter:", 30);
        CLIUtils.typewriter("The Old Knight nods: 'Your Stage 2 training is complete. Greater adventures await…'", 30);
        CLIUtils.typewriter("\"Stage 2 complete. Prepare for what comes next.\"", 30);

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
        level.sandbox.getDirGenerator().generateFromConfig(configPath, level.sandbox.getRootPath());
    }
}
