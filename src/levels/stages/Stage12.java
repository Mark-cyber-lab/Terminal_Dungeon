package levels.stages;

import gameplay.Mission;
import elements.enemies.*;
import levels.Level;
import utilities.CLIUtils;
import gameplay.CommandResult;

import java.nio.file.Path;

public class Stage12 extends Stage {
    private static final String configPath = "./src/stages/stage12.txt";
    private final String[] mobs = {
            "goblin", "kobold", "zombie", "ghoul", "ogre"
    };

    public Stage12(Level level) {
        super(12, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 12 — A Dark Mysterious Aura that envelops the way"};
    }

    @Override
    public void play() {
        Mission mission = new Mission(level.sandbox.getExecutor(), level.player);

        Goblin normalGoblin = new Goblin("normal_goblin", Path.of(("./sandbox/eldritch_realms/corrupted_caverns/goblin.mob")));
        Zombie slowZombie = new Zombie("slow_zombie", Path.of(("./sandbox/eldritch_realms/corrupted_caverns/zombie.mob")));
        Kobold cunningKobold = new Kobold("cunning_kobold", Path.of(("./sandbox/eldritch_realms/forbidden_crypt/kobold.mob")));
        Ogre powerfulOgre = new Ogre("powerful_ogre", Path.of(("./sandbox/eldritch_realms/void_chamber/ogre.mob")));
        Vampire ancientVampire = new Vampire("ancient_vampire", Path.of(("./sandbox/eldritch_realms/purging_grounds/vampire.mob")));
        DemonLord demonLord = new DemonLord("demon_lord", Path.of(("./sandbox/eldritch_realms/overlord_sanctum/demon_lord%9jdi#$@#2JSmk9.mob")));

        mission
                .addEnemy(normalGoblin)
                .addEnemy(slowZombie)
                .addEnemy(cunningKobold)
                .addEnemy(powerfulOgre)
                .addEnemy(ancientVampire)
                .addEnemy(demonLord);

        mission.initialize();

        CLIUtils.typewriter("\n'NO!' bellows the Demon Lord. 'Those archives were meant to be forgotten! Their knowledge was MINE to erase!'", 25);
        CLIUtils.typewriter("'You restored order where I sowed chaos... This changes nothing!'", 25);
        CLIUtils.typewriter("\n**Clogged Pathways:** The corruption remains, hindering your abilities.", 25);
        CLIUtils.typewriter("Your movements are sluggish", 25);
        CLIUtils.typewriter("Energy flows inefficiently to you", 25);
        CLIUtils.typewriter("Defeat all the enemy.\nRemember that if you make mistakes, then the demon lord will spawn monsters!.", 25);

        int enemyCount;
        int multiplier = 1;

        boolean success = false;
        for (int i = 1; !success; i++) {
            enemyCount = (int) mission.remainingEnemies();

            IO.println(enemyCount + " enemy/s remaining.");
            IO.print(">> ");
            String input = IO.readln().trim();

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit"))
                break;

            if (i == 6) {
                i = 1;
                ++multiplier;
            }

            if (input.startsWith("rm")) // this is for battling the mob files
                success = rmFunction(input, mission, i, multiplier);
            else if (input.startsWith("cat") || input.startsWith("cd") || input.startsWith("pwd") || input.startsWith("tree") || input.startsWith("ls"))
                level.sandbox.getExecutor().execute(input);
            else { // if the input command is wrong
                IO.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                continue;
            }

            if(!level.player.getStats().isAlive()) break;
        }

        mission.cleanup();
    }

    private boolean rmFunction(String input, Mission mission, int loopCount, int multiplier) {
        String[] words = input.split(" ");
        int wordCount = words.length;

        if (wordCount == 1)
            IO.println("The spirits whisper: \"That is not how you use the command.\"");
        else {
            boolean passed = runCommand(input, words, wordCount);
            if (passed) {
                CommandResult result = level.sandbox.getExecutor().execute(input);

                if (!result.isSuccess())
                    IO.println("The spirits whisper: \"There is something wrong with your command.\"");
                else
                    DemonLord.spawnEnemies(mission, loopCount, multiplier, result);
            }
        }
        return mission.allEnemiesDefeated();
    }

    private boolean runCommand(String input, String[] words, int wordCount) {
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
        IO.println("Stage complete! Congratulation for clearing this challenge!...\n");
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
        level.sandbox.getGranter().grant("mv");
        level.sandbox.getGranter().grant("mkdir");
    }
}
