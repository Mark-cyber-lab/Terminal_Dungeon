package com.terminaldungeon.levels.stages;

import com.terminaldungeon.utilities.CLIUtils;
import com.terminaldungeon.gameplay.CommandContext;
import com.terminaldungeon.gameplay.CommandResult;
import com.terminaldungeon.levels.Level;

import java.nio.file.Path;

public class Stage4 extends Stage {
    private static final String stageConfigPath = "./src/resources/stages/stage4.txt";

    public Stage4(Level level) {
        super(4, level);
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

            commandResult = level.sandbox.getExecutor().execute(input);

            if (!commandResult.isSuccess()) continue;

            CommandContext context = commandResult.getContext();

            if ("cd".equals(context.command)) {
                // CommandContext.endDir contains the Path of the file that is navigated
                if (context.endDir.toString().equals(portalPath.toAbsolutePath().normalize().toString())) break;
            }
        }
    }

    @Override
    public void onSuccessPlay() {
        IO.println("Stage complete! Proceeding...\n");
        CLIUtils.waitAnyKey();
    }

    @Override
    public void onFailedPlay(Exception exception) {

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
