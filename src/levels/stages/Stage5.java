package levels.stages;

import gameplay.CommandContext;
import gameplay.Mission;
import elements.enemies.*;
import levels.Level;
import utilities.CLIUtils;
import gameplay.CommandResult;

import java.nio.file.Path;

public class Stage5 extends Stage {
    private static final String configPath = "stages/stage5.txt";

    public Stage5(Level level) {
        super(5, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[] { "Stage 5 — Shadows on the corridor" };
    }

    @Override
    public void play() {
        Mission mission = new Mission(level.sandbox.getExecutor(), level.player);

        Goblin ordinaryGoblin = new Goblin("goblin_caves", Path.of("sandbox/goblin_caves/goblin.mob"));
        Goblin wariorGoblin = new Goblin("arena", Path.of("sandbox/arena/goblin.mob"));
        Goblin strongGoblin = new Goblin("combat_training", Path.of("sandbox/combat_training/goblin.mob"));
        Zombie strongZombie = new Zombie("combat_training", Path.of("sandbox/combat_training/zombie.mob"));

        mission
                .addEnemy(ordinaryGoblin)
                .addEnemy(wariorGoblin)
                .addEnemy(strongGoblin)
                .addEnemy(strongZombie);

        mission.initialize();

        CLIUtils.typewriter("From the darkness ahead, a low growl echoes, suggesting you are not alone.", 25);
        CLIUtils.typewriter("The knight's words ring in your ears: \"Use the knowledge you've gained, adventurer.\"",
                25);
        CLIUtils.typewriter("Goal: Explore the corridor, defeat the monster, and find the key for the next stage.", 25);
        CLIUtils.typewriter("Tip: Use rm to defeat the hostile mob, example use \"rm zombie\".", 25);

        int totalEnemies = mission.totalEnemies();

        do {
            IO.println(mission.remainingEnemies() + "/" + totalEnemies + " monster/s remaining.");

            IO.print(">> ");
            String input = IO.readln().trim();

            CommandResult result = level.sandbox.getExecutor().execute(input);

        } while (mission.remainingEnemies() != 0 && level.player.getStats().isAlive());

        if (!level.player.getStats().isAlive()) {
            mission.cleanup();
            return;
        }

        CLIUtils.typewriter("All enemies are defeated! Find the key to the next stage", 30);

        boolean seenKey = false;

        Path keyPath = Path.of("sandbox/combat_master/key.txt");

        while (true) {
            IO.print(">> ");

            String input = IO.readln().trim();

            CommandResult result = level.sandbox.getExecutor().execute(input);

            if (!result.isSuccess())
                continue;

            CommandContext context = result.getContext();

            // checks if the player has executed the cat command for the spefified key path
            if (context != null && "cat".equals(context.command)) {
                if (context.read.toString().equals(keyPath.normalize().toAbsolutePath().toString())) {
                    seenKey = true;
                }
            }

            // if key is seen and player prompted done
            if (context != null && "done".equals(context.command) && seenKey)
                break;
        }

        mission.cleanup();
    }

    @Override
    public void onSuccessPlay() {
        // player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 6...\n");
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
        level.sandbox.getGranter().grant("done");
        level.sandbox.getGranter().grant("cat");
        level.sandbox.getGranter().grant("rm");
    }
}
