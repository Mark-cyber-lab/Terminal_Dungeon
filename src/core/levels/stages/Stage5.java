package core.levels.stages;

import core.Mission;
import core.enemies.*;
import core.levels.Level;
import core.listeners.Blocker;
import utilities.CLIUtils;
import utilities.CommandResult;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Stage5 extends Stage {
    private static final String configPath = "./src/stages/stage5.txt";

    public Stage5(Level level) {
        super(5, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 5 — Shadows on the corridor"};
    }

    @Override
    public void play() {
        Mission mission = new Mission(level.player);

        Goblin ordinaryGoblin = new Goblin("goblin_caves", Path.of("./sandbox/goblin_caves/goblin.mob"));
        Goblin wariorGoblin = new Goblin("arena", Path.of("./sandbox/arena/goblin.mob"));
        Goblin strongGoblin = new Goblin("combat_training", Path.of("./sandbox/combat_training/goblin.mob"));
        Zombie strongZombie = new Zombie("combat_training", Path.of("./sandbox/combat_training/zombie.mob"));

        mission
                .addEnemy(ordinaryGoblin)
                .addEnemy(wariorGoblin)
                .addEnemy(strongGoblin)
                .addEnemy(strongZombie);

        mission.initialize();

        CLIUtils.typewriter("From the darkness ahead, a low growl echoes, suggesting you are not alone.", 25);
        CLIUtils.typewriter("The knight's words ring in your ears: \"Use the knowledge you've gained, adventurer.\"", 25);
        CLIUtils.typewriter("Goal: Explore the corridor, defeat the monster, and find the key for the next stage.", 25);
        CLIUtils.typewriter("Tip: Use rm to defeat the hostile mob, example use \"rm zombie\".", 25);
        CLIUtils.typewriter("Type e or exit to exit the game...", 25);

        int enemyCount;

        boolean success = false;
        while (!success) {

            enemyCount = (int) mission.getEnemies().stream().filter(enemy -> !enemy.isCleared()).count();

            IO.println(enemyCount + " monster/s remaining.");
            IO.print(">> ");
            String input = IO.readln().trim();

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit"))
                break;
            else if (input.equals("Move Forward") && enemyCount == 0)
                success = true;
            else if (input.equals("Move Forward") && enemyCount != 0)
                IO.println("There are still" + enemyCount + " monsters.");

            if (input.startsWith("rm")) {
                String[] words = input.split(" ");
                int wordCount = words.length;

                if (wordCount == 1) {
                    IO.println("The spirits whisper: \"That is not how you use the command.\"");
                    continue;
                } else {
                    boolean passed = runCommand(input, words, wordCount);
                    if (passed) {
                        CommandResult result = level.sandbox.getExecutor().executeCommand(input.split(" "));

                        if (result.success() && mission.isFulfilled()) {
                            enemyCount = (int) mission.getEnemies().stream().filter(enemy -> !enemy.isCleared()).count();

                            if (enemyCount == 0) {
                                mission.cleanUp();
                                CLIUtils.typewriter("Congratulations!!\nYou defeated all the enemies", 25);
                                CLIUtils.typewriter("Teleporting you to the next stage\n...", 25);
                                success = true;
                            }
                        }
                    }
                }
            } else if (input.startsWith("cat") || input.startsWith("cd") || input.startsWith("ls") || input.startsWith("pwd") || input.startsWith("tree")) {
                level.sandbox.getExecutor().executeCommand(input.split(" "));
            }
            else {
                IO.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                continue;
            }

            // simple success condition: pwd inside a subfolder of basePath
            Path current = level.sandbox.getRootPath() != null ? Paths.get(level.sandbox.getRootPath()) : null;
            if (current != null && !current.toAbsolutePath().equals(Paths.get(level.sandbox.getRootPath()).toAbsolutePath()))
                success = true;
        }
    }

    public boolean runCommand(String input, String[] words, int wordCount) {
        int startingPoint = input.contains("-rf") ? 2 : 1; // determines if the command entered is "rm -rf"

        for (int i = startingPoint; i < wordCount; i++) {
            if (!words[i].contains(".")) {
                IO.println("The spirits whisper: \"You cannot remove directories\"");
                return false;
            }
        }

        return true;
    }

    @Override
    public void onSuccessPlay() {
        //        player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 6...\n");
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
