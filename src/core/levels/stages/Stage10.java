package core.levels.stages;

import core.Mission;
import core.enemies.*;
import core.items.Decoy;
import core.items.Shards;
import core.levels.Level;
import core.listeners.Blocker;
import utilities.CLIUtils;
import utilities.CommandResult;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Stage10 extends Stage {
    private static final String configPath = "./src/stages/stage10.txt";
    private final String[] importantItems = {
            "ancient_scroll.txt", "artifact_fragment.txt", "spellbook.txt",
            "royal_decree.txt", "lost_artifact.txt", "ancient_tome.txt",
            "holy_relic.txt", "dragon_scale.txt"
    };

    public Stage10(Level level) {
        super(6, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 10 — A Dark Mysterious Aura that envelops the way"};
    }

    @Override
    public void play() {
        Mission mission = new Mission(level.player);

        Decoy firstDecoy = new Decoy("junk_log_1.tmp", "junk_log_1");
        Decoy secondDecoy = new Decoy("combat_cache.tmp", "combat_cache");
        Decoy thirdDecoy = new Decoy("old_potions.tmp", "old_potions");
        Decoy fourthDecoy = new Decoy("system_cache.cache", "system_cache");
        Decoy fifthDecoy = new Decoy("user_cache.cache", "user_cache");
        Decoy sixthDecoy = new Decoy("map_backup.cache", "map_backup");
        Decoy seventhDecoy = new Decoy("food_waste.garbage", "food_waste");
        Decoy eigthDecoy = new Decoy("broken_gear.garbage", "broken_gear");
        Decoy ninthDecoy = new Decoy("rusty_swords.garbage", "rusty_swords");
        Decoy tenthDecoy = new Decoy("old_bones.garbage", "old_bones");

        Goblin garbageGoblin = new Goblin("garbage_goblin", Path.of("./sandbox/dungeon_cache/.tmp/trash_pit/goblin.mob"));
        Zombie cacheGuardianZombie = new Zombie("cache_guardian_zombie", Path.of("./sandbox/dungeon_cache/.cache/important_vault/zombie.mob"));
        Kobold koboldHoarder = new Kobold("kobold_hoarder", Path.of("./sandbox/dungeon_cache/.cache/hidden_chamber/kobold.mob"));
        Ghoul ghoulFeaster = new Ghoul("ghoul_feaster", Path.of("./sandbox/dungeon_cache/.garbage/rotten_pile/ghoul.mob"));
        Ogre junkOgre = new Ogre("junk_ogre", Path.of(("./sandbox/dungeon_cache/.garbage/scrap_heap/ogre.mob")));
        Vampire cacheVampireLord = new Vampire("cache_vampire_lord", Path.of(("./sandbox/dungeon_cache/vampire.mob")));

        // add decoys
        mission
                .addDecoys(firstDecoy)
                .addDecoys(secondDecoy)
                .addDecoys(thirdDecoy)
                .addDecoys(fourthDecoy)
                .addDecoys(fifthDecoy)
                .addDecoys(sixthDecoy)
                .addDecoys(seventhDecoy)
                .addDecoys(eigthDecoy)
                .addDecoys(ninthDecoy)
                .addDecoys(tenthDecoy);

        // add enemies
        mission
                .addEnemy(garbageGoblin)
                .addEnemy(cacheGuardianZombie)
                .addEnemy(koboldHoarder)
                .addEnemy(ghoulFeaster)
                .addEnemy(junkOgre)
                .addEnemy(cacheVampireLord);

        level.sandbox.getExecutor().addBlocker((Blocker[]) mission.getEnemies().toArray());

        CLIUtils.typewriter("\nYou step into the dungeon cache, and the stench of old data hits immediately. Dusty code hangs in the air like fog.", 25);
        CLIUtils.typewriter("As your eyes adjust, faint lights flicker across heaps of junk files scattered through cramped corridors.", 25);
        CLIUtils.typewriter("Shredded .tmp scraps cling to the walls, and broken .cache fragments crunch under your boots.", 25);
        CLIUtils.typewriter("A ghostly custodian drifts forward, its form glitching with static.", 25);
        CLIUtils.typewriter("\"Traveler... the cache is overflowing with decoys and trash,\" it warns softly.", 25);
        CLIUtils.typewriter("\"Real relics are buried among the garbage. Clear the junk, but do not destroy the artifacts.\"", 25);
        CLIUtils.typewriter("The specter fades, leaving only the faint echo: \"Remove the trash... but choose wisely.\"", 25);
        CLIUtils.typewriter("\nGoal: Purge useless files in .tmp, .cache, and .garbage without harming valuable scrolls and relics.", 25);
        CLIUtils.typewriter("Tip: Junk looks important, and important things look like junk. Use rm and rm -rf carefully.", 25);
        CLIUtils.typewriter("Type your command to begin the cleanup...", 25);

        long decoyNotDeleted;
        long enemyCount;

        boolean success = false;
        while (!success) {
            decoyNotDeleted = mission.getDecoyItems().stream().filter(decoy -> !decoy.isDeleted()).count();
            enemyCount = mission.getEnemies().stream().filter(enemy -> !enemy.isCleared()).count();

            IO.println(enemyCount + " monster/s remaining.");
            IO.println(decoyNotDeleted + " decoy/s remaining.");

            IO.print(">> ");
            String input = IO.readln().trim();

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit"))
                break;

            if (input.startsWith("rm")) // this is for battling the mob files
                success = rmFunction(input, mission);
            else if (input.startsWith("ls"))
                lsFunction(input, mission);
            else if (input.startsWith("cat") || input.startsWith("cd") || input.startsWith("pwd") || input.startsWith("tree"))
                level.sandbox.getExecutor().executeCommand(input.split(" "));
            else if (input.startsWith("mkdir") || input.startsWith("mv")) // this is for creating the destroyed dungeon
                IO.println("The spirits whisper: \"Cannot use that command in this stage.\"");
            else { // if the input command is wrong
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

    public boolean testMissionComplete(Mission mission) {
        long enemyCount = mission.getEnemies().stream().filter(enemy -> !enemy.isCleared()).count();
        if (!mission.decoyCompleted())
            IO.println("The spirits whisper: \"There are still decoys left to be deleted.\"");
        else if (enemyCount != 0)
            IO.println("The spirits whisper: \"There are still enemies to defeat.\"");
        else {
            CLIUtils.typewriter("Shards complete, proceeding to next stage...", 25);
            return true;
        }
        return false;
    }

    public void lsFunction (String input, Mission mission) {
         level.sandbox.getExecutor().executeCommand(input.split(" "));
    }

    public boolean rmFunction (String input, Mission mission) {
        String[] words = input.split(" ");
        int wordCount = words.length;

        if (wordCount == 1)
            IO.println("The spirits whisper: \"That is not how you use the command.\"");
        else {
            boolean passed = runCommand(input, words, wordCount);

            if (passed) {
                level.sandbox.getExecutor().executeCommand(input.split(" "));
                for (Decoy d : mission.getDecoyItems()) {
                    if (input.contains(d.getName())) {
                        d.setDeleted(true);
                        break;
                    }
                }
            }
        }

        return testMissionComplete(mission);
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
