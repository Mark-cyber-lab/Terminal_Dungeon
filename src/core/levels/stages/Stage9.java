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

public class Stage9 extends Stage {
    private static final String configPath = "./src/stages/stage6txt";

    public Stage9(Level level) {
        super(6, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 6 — A Dark Mysterious Aura that envelops the way"};
    }

    @Override
    public void play() {
        Mission mission = new Mission();

        Goblin corridorGoblin = new Goblin("corridor_goblin", Path.of("./sandbox/crumbling_corridor/goblin.mob"));
        Goblin hiddenGoblin = new Goblin("goblin_trap_setter", Path.of("./sandbox/echoing_caverns/ancient_grotto/hidden_alcove/goblin.mob"));
        Kobold dustyKobold = new Kobold("dusty_kobold", Path.of("./sandbox/forgotten_library/dusty_archives/kobold.mob"));
        Kobold dungeonKobold = new Kobold("garbage_kobold", Path.of("./sandbox/dungeon_cache/.garbage/kobold.mob"));
        Zombie forgottenZombie = new Zombie("forgotten_zombie", Path.of("./sandbox/forgotten_library/zombie.mob"));
        Zombie hauntedZombie = new Zombie("haunted_zombie", Path.of("./sandbox/haunted_crypt/deep_catacombs/zombie.mob"));
        Zombie dungeonZombie = new Zombie("cache_zombie", Path.of("./sandbox/dungeon_cache/.cache/zombie.mob"));
        Ghoul hauntedGhoul = new Ghoul("haunted_ghoul", Path.of("./sandbox/haunted_crypt/ghoul.mob"));
        Ghoul vaultGhoul = new Ghoul("vault_ghoul", Path.of("./sandbox/spider_infested_vault/ghoul.mob"));
        Ogre cavernOgre = new Ogre("cavern_ogre", Path.of(("./sandbox/echoing_caverns/ogre.mob")));
        Ogre ancientOgre = new Ogre("ancient_ogre", Path.of(("./sandbox/group_battle/ogre.mob")));
        Vampire ancientVampire = new Vampire("ancient_vampire", Path.of(("./sandbox/spider_infested_vault/vampire.mob")));

        Shards firstShard = new Shards("map_piece_1.txt", "map_piece_1", "target_point");
        Shards secondShard = new Shards("map_piece_2.txt", "map_piece_2", "target_point");
        Shards thirdShard = new Shards("map_piece_3.txt", "map_piece_3", "target_point");
        Shards fourthShard = new Shards("map_piece_5.txt", "map_piece_5", "target_point");
        Shards fifthShard = new Shards("map_piece_8.txt", "map_piece_8", "target_point");
        Shards sixthShard = new Shards("map_piece_10.txt", "map_piece_10", "target_point");

        Decoy firstDecoy = new Decoy("map_piece_13.txt", "map_piece_13");
        Decoy secondDecoy = new Decoy("map_piece_A.txt", "map_piece_A");
        Decoy thirdDecoy = new Decoy("map_piece_0.txt", "map_piece_0");
        Decoy fourthDecoy = new Decoy("map_piece_99.txt", "map_piece_99");

        // add enemies
        mission
                .addEnemy(corridorGoblin)
                .addEnemy(dustyKobold)
                .addEnemy(forgottenZombie)
                .addEnemy(hauntedGhoul)
                .addEnemy(cavernOgre)
                .addEnemy(hauntedZombie)
                .addEnemy(ancientOgre)
                .addEnemy(vaultGhoul)
                .addEnemy(hiddenGoblin)
                .addEnemy(dungeonZombie)
                .addEnemy(dungeonKobold)
                .addEnemy(ancientVampire);

        // add shards
        mission
                .addShards(firstShard)
                .addShards(secondShard)
                .addShards(thirdShard)
                .addShards(fourthShard)
                .addShards(sixthShard)
                .addShards(fifthShard);

        // add decoys
        mission
                .addDecoys(firstDecoy)
                .addDecoys(secondDecoy)
                .addDecoys(thirdDecoy)
                .addDecoys(fourthDecoy);

        level.sandbox.getExecutor().addBlocker((Blocker[]) mission.getEnemies().toArray());

        CLIUtils.typewriter("\nYou step through the portal into utter darkness. The air is thick with the scent of damp stone and forgotten centuries.", 25);
        CLIUtils.typewriter("As your eyes adjust, you see you're in a crumbling corridor. Ancient torches sputter to life along the walls.", 25);
        CLIUtils.typewriter("Before you, a massive stone door lies shattered. Through the opening, you glimpse a chamber filled with... nothing.", 25);
        CLIUtils.typewriter("A spectral figure materializes—a ghostly cartographer, translucent and shimmering with pale blue light.", 25);
        CLIUtils.typewriter("\"Traveler... the Dungeon Map has been shattered!\" it whispers, voice echoing from all directions.", 25);
        CLIUtils.typewriter("\"Fragments are scattered through the labyrinth: forgotten library, haunted crypt, echoing caverns...\"", 25);
        CLIUtils.typewriter("The spirit gestures to the empty chamber. \"Only when all true pieces are assembled here will the path forward reveal itself.\"", 25);
        CLIUtils.typewriter("It fades, leaving behind one final warning: \"Beware false fragments... and the creatures that guard them.\"", 25);
        CLIUtils.typewriter("\nGoal: Rebuild the Deep Dungeon Map by finding real map pieces scattered through nested directories.", 25);
        CLIUtils.typewriter("Tip: Real map pieces contain 'FRAGMENT:' on the first line. Fake pieces do not.", 25);
        CLIUtils.typewriter("Use the skills that you've learned from the previous stages and this new skills\n Use 'mv' to move fragments, and 'mkdir' if you need to organize or create the blueprint.", 25);
        CLIUtils.typewriter("Remember: The fragments must be assembled in the \"target_point\" directory. Once you collect thme", 25);
        CLIUtils.typewriter("Type your command to begin exploring the dungeon...", 25);

        long enemyCount;
        long shardsNotCompleted;
        long decoyNotDeleted;

        boolean success = false;
        while (!success) {

            enemyCount = mission.getEnemies().stream().filter(enemy -> !enemy.hasBeenDefeated()).count();
            shardsNotCompleted = mission.getShards().stream().filter(shard -> !shard.isCorrectDir()).count();
            decoyNotDeleted = mission.getDecoyItems().stream().filter(decoy -> !decoy.isDeleted()).count();

            IO.println(enemyCount + " monster/s remaining.");
            IO.println(shardsNotCompleted + " shard/s remaining.");
            IO.println(decoyNotDeleted + " decoy/s remaining.");

            IO.print(">> ");
            String input = IO.readln().trim();

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit"))
                break;

            if (input.startsWith("rm")) { // this is for battling the mob files
                String[] words = input.split(" ");
                int wordCount = words.length;

                if (wordCount == 1) {
                    IO.println("The spirits whisper: \"That is not how you use the command.\"");
                    continue;
                } else {
                    boolean passed = runCommand(input, words, wordCount);
                    if (passed)
                        level.sandbox.getExecutor().executeCommand(input.split(" "));
                } // for traversing the dungeon corridor
            } else if (input.startsWith("ls")) {
                CommandResult result = level.sandbox.getExecutor().executeCommand("pwd");
                String[] wordsArr = result.path().split("/");

                // conditions to determine if the location is on the target point
                if (wordsArr[wordsArr.length - 1].equals("target_point"))
                    success = testMissionComplete(mission, input);
                else
                    level.sandbox.getExecutor().executeCommand(input.split(" "));

            }else if (input.startsWith("cat") || input.startsWith("cd") || input.startsWith("pwd") || input.startsWith("tree"))
                level.sandbox.getExecutor().executeCommand(input.split(" "));
            else if (input.startsWith("mv")) { // this is for moving the items to designated area
                String[] wordsArr = input.split(" ");

                if (wordsArr.length == 2) {
                    String[] subWordsArr = wordsArr[1].split("/");

                    if (subWordsArr[subWordsArr.length - 1].equals("target_point")) {
                        CommandResult result = level.sandbox.getExecutor().executeCommand(input.split(" "));

                        if (!result.success())
                            IO.println("The spirits whisper: \"There are something wrong with your action.\"");
                    }
                }
            } else if (input.startsWith("mkdir")) { // this is for creating the destroyed dungeon
                CommandResult result = level.sandbox.getExecutor().executeCommand("pwd");
                String[] wordsArr = result.path().split("/");

                if (wordsArr[wordsArr.length - 1].equals("sandbox"))
                    level.sandbox.getExecutor().executeCommand(input.split(" "));
            } else { // if the input command is wrong
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

    public boolean testMissionComplete(Mission mission, String input) {
        long enemyCount = mission.getEnemies().stream().filter(enemy -> !enemy.hasBeenDefeated()).count();
        if (!mission.decoyCompleted())
            IO.println("The spirits whisper: \"There are still decoys left to be deleted.\"");
        else if(!mission.shardCompleted())
            IO.println("The spirits whisper: \"Shards are still incomplete.\"");
        else if (enemyCount != 0)
            IO.println("The spirits whisper: \"There are still enemies to defeat.\"");
        else {
            level.sandbox.getExecutor().executeCommand(input.split(" "));
            CLIUtils.typewriter("Shards complete, proceeding to next stage...", 25);
            return true;
        }
        return false;
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
