package core.levels.stages;

import core.Mission;
import core.enemies.*;
import core.levels.Level;
import core.listeners.Blocker;
import utilities.CLIUtils;
import utilities.CommandResult;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Stage6 extends Stage {
    private static final String configPath = "./src/stages/stage6txt";

    public Stage6(Level level) {
        super(6, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 6 — A Dark Mysterious Aura that envelops the way"};
    }

    @Override
    public void play() {
        Mission mission = new Mission();

        Goblin ordinaryGoblin = new Goblin("hunter_goblin", Path.of("./sandbox/advanced_combat/goblin.mob"));
        Kobold wariorKobold = new Kobold("warior_kobold", Path.of("./sandbox/warrior_hall/kobold.mob"));
        Zombie wariorZombie = new Zombie("warior_zombie", Path.of("./sandbox/warior_hall/zombie.mob"));
        Vampire vampireBoss = new Vampire("combat_champion", Path.of(("./sandbox/combat_champion/vampire.mob")));
        Ghoul ghoul1 = new Ghoul("first_ghoul", Path.of("./sandbox/group_battle/ghoul.mob"));
        Ogre ogreGeneral = new Ogre("group_battle", Path.of(("./sandbox/group_battle/ogre.mob")));

        mission
                .addEnemy(ordinaryGoblin)
                .addEnemy(wariorKobold)
                .addEnemy(wariorZombie)
                .addEnemy(ghoul1)
                .addEnemy(ogreGeneral)
                .addEnemy(vampireBoss);

        level.sandbox.getExecutor().addBlocker((Blocker[]) mission.getEnemies().toArray());

        CLIUtils.typewriter("\nThe corridor narrows and the air grows cold. The scent of aged earth and old blood fills your nostrils.", 25);
        CLIUtils.typewriter("Before you stands a tall, pale figure with eyes that glow crimson in the gloom—a vampire noble.", 25);
        CLIUtils.typewriter("Its cloak billows without wind, and it reveals sharp fangs in a predatory smile.", 25);
        CLIUtils.typewriter("\"Ah, fresh blood has wandered into my domain. You will make a fine addition to my collection.\"", 25);
        CLIUtils.typewriter("The vampire's form seems to shift, as if part shadow. It moves with unnatural speed.", 25);
        CLIUtils.typewriter("\nGoal: Vanquish the vampire using the correct command before it drains your life!", 25);
        CLIUtils.typewriter("Tip: Vampires are creatures of darkness. To defeat one, you might need something that brings light.", 25);
        CLIUtils.typewriter("Perhaps you could use something like: \"use torch\" or \"wield stake\"...", 25);
        CLIUtils.typewriter("Or maybe a more direct approach: \"rm vampire\" if you're feeling bold.", 25);
        CLIUtils.typewriter("Type e or exit to retreat from the encounter...", 25);

        int enemyCount;
        int defeatedEnemiesCount;

        boolean success = false;
        while (!success) {

            enemyCount = mission.getEnemies().size();

            IO.println(enemyCount + " monster/s remaining.");
            IO.print(">> ");
            String input = IO.readln().trim();

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit"))
                break;

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
                            level.sandbox.getExecutor().removeBlocker((Blocker[]) mission.getEnemies().toArray());

                            defeatedEnemiesCount = (int) mission.getEnemies().stream().filter(Enemy::hasBeenDefeated).count();

                            if (defeatedEnemiesCount == 0) {
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
        int startingPoint = input.contains("-rf") ? 2 : 1;

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
        IO.println("Stage complete! Proceeding to Stage 7...\n");
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
