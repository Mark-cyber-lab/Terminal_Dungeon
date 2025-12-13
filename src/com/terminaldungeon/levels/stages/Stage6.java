package com.terminaldungeon.levels.stages;

import com.terminaldungeon.elements.enemies.*;
import com.terminaldungeon.gameplay.CommandContext;
import com.terminaldungeon.gameplay.Mission;
import com.terminaldungeon.elements.enemies.*;
import com.terminaldungeon.levels.Level;
import com.terminaldungeon.utilities.CLIUtils;
import com.terminaldungeon.gameplay.CommandResult;

import java.nio.file.Path;

public class Stage6 extends Stage {
    private static final String configPath = "./src/resources/stages/stage6.txt";

    public Stage6(Level level) {
        super(6, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 6 — A Dark Mysterious Aura that envelops the way"};
    }

    @Override
    public void play() {
        Mission mission = new Mission(level.sandbox.getExecutor(), level.player);

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

        mission.initialize();

        CLIUtils.typewriter("\nThe corridor narrows and the air grows cold. The scent of aged earth and old blood fills your nostrils.", 25);
        CLIUtils.typewriter("Before you stands a tall, pale figure with eyes that glow crimson in the gloom—a vampire noble.", 25);
        CLIUtils.typewriter("Its cloak billows without wind, and it reveals sharp fangs in a predatory smile.", 25);
        CLIUtils.typewriter("\"Ah, fresh blood has wandered into my domain. You will make a fine addition to my collection.\"", 25);
        CLIUtils.typewriter("The vampire's form seems to shift, as if part shadow. It moves with unnatural speed.", 25);
        CLIUtils.typewriter("\nGoal: Vanquish the vampire using the correct command before it drains your life!", 25);
        CLIUtils.typewriter("Tip: Vampires are creatures of darkness. To defeat one, you might need something that brings light.", 25);

        // Pano ung use neto bro
        CLIUtils.typewriter("Perhaps you could use something like: \"use torch\" or \"wield stake\"...", 25);

        CLIUtils.typewriter("Or maybe a more direct approach: \"rm vampire\" if you're feeling bold.", 25);

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

        Path keyPath = Path.of("./sandbox/combat_master/key.txt");

        while (true) {
            IO.print(">> ");

            String input = IO.readln().trim();

            CommandResult result = level.sandbox.getExecutor().execute(input);

            if (!result.isSuccess()) continue;

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
        //        player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 7...\n");
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
