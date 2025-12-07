package core.levels.stages;

import core.Mission;
import core.enemies.*;
import core.items.Corrupted;
import core.items.Decoy;
import core.items.Shards;
import core.levels.Level;
import core.listeners.Blocker;
import utilities.CLIUtils;
import utilities.CommandResult;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Stage11 extends Stage {
    private static final String configPath = "./src/stages/stage10.txt";
    private final String[] importantItems = {
            "ancient_scroll.txt", "artifact_fragment.txt", "spellbook.txt",
            "royal_decree.txt", "lost_artifact.txt", "ancient_tome.txt",
            "holy_relic.txt", "dragon_scale.txt"
    };

    public Stage11(Level level) {
        super(11, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 10 — A Dark Mysterious Aura that envelops the way"};
    }

    @Override
    public void play() {
        Mission mission = new Mission(level.player);

        Corrupted corrupted1 = new Corrupted("broken_pillar_01.txt", "broken_pillar_01", "archive_pillars", "pillar_01.txt");
        Corrupted corrupted2 = new Corrupted("corrupted_core_A.dat", "corrupted_core_A", "archive_cores", "core_A.dat");
        Corrupted corrupted3 = new Corrupted("fragment_seal_1.piece", "fragment_seal_1", "archive_seals", "seal_fragment_1.piece");
        Corrupted corrupted4 = new Corrupted("pillar_broken_02.txt", "pillar_broken_02", "archive_pillars", "pillar_02.txt");
        Corrupted corrupted5 = new Corrupted("core_corrupted_B.dat", "core_corrupted_B", "archive_cores", "core_B.dat");
        Corrupted corrupted6 = new Corrupted("seal_fragment_2.piece", "seal_fragment_2", "archive_seals", "seal_fragment_2.piece");
        Corrupted corrupted7 = new Corrupted("pillar_03.txt", "pillar_03", "archive_pillars", "pillar_03.txt");
        Corrupted corrupted8 = new Corrupted("core_C.dat", "core_C", "archive_cores", "core_C.dat");

        mission
                .addCorrupt(corrupted1)
                .addCorrupt(corrupted2)
                .addCorrupt(corrupted3)
                .addCorrupt(corrupted4)
                .addCorrupt(corrupted5)
                .addCorrupt(corrupted6)
                .addCorrupt(corrupted7)
                .addCorrupt(corrupted8);

        level.sandbox.getExecutor().addBlocker((Blocker[]) mission.getEnemies().toArray());

        CLIUtils.typewriter("\nYou enter the Grand Archive, a place of sacred knowledge now tainted by corruption.", 25);
        CLIUtils.typewriter("Ancient pillars of history stand cracked, their inscriptions scrambled by dark magic.", 25);
        CLIUtils.typewriter("Memory cores flicker with corrupted data, and sacred seals lie fragmented in the labyrinth.", 25);
        CLIUtils.typewriter("A spectral archivist materializes before you, its form shimmering with divine light.", 25);
        CLIUtils.typewriter("\"Paladin... the Grandmaster Knight's curse has ravaged our archives,\" it intones solemnly.", 25);
        CLIUtils.typewriter("\"Sacred pillars are broken, memory cores scrambled, and divine seals fragmented.\"", 25);
        CLIUtils.typewriter("The archivist gestures toward the restoration room. \"You must wield sudo authority to mend what is broken.\"", 25);
        CLIUtils.typewriter("Its form begins to fade. \"Navigate the deep vaults and catacombs. Restore order to the archives...\"", 25);
        CLIUtils.typewriter("\nGoal: Restore all corrupted archive files and organize them in their proper sections.", 25);
        CLIUtils.typewriter("Tip: Use 'sudo mv' to rename corrupted files, then move them to archive_pillars/, archive_cores/, or archive_seals/.", 25);
        CLIUtils.typewriter("Some paths are deeply nested—explore holy_sanctum/, divine_hall/, and restoration_room/ thoroughly.", 25);
        CLIUtils.typewriter("Type your command to begin the restoration...", 25);

        int purifiedItems;
        int returnedItems;

        boolean success = false;
        while (!success) {
            purifiedItems = (int) mission.getCorrupts().stream().filter(c -> !c.isCorrectName()).count();
            returnedItems = (int) mission.getCorrupts().stream().filter(c -> !c.isCorrectDir()).count();

            IO.println(purifiedItems < returnedItems ? purifiedItems: returnedItems  + " item/s remaining.");
            IO.print(">> ");
            String input = IO.readln().trim();

            if (input.equalsIgnoreCase("e") || input.equalsIgnoreCase("exit"))
                break;

            if (input.startsWith("cat") || input.startsWith("cd") || input.startsWith("pwd") || input.startsWith("tree") || input.startsWith("ls"))
                level.sandbox.getExecutor().executeCommand(input.split(" "));
            else if (input.startsWith("mv")) // this is for moving the items to designated area
                moveFunction(input, mission);
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

    public void moveFunction (String input, Mission mission) {
        boolean match = false;

        for (Corrupted c : mission.getCorrupts()) {
            if (input.contains(c.getCorrectName()) && input.contains(c.getName()))
                match = seeMatch(input, c, false);
            else if (input.contains(c.getTargetDir()) && input.contains(c.getName()))
                match = seeMatch(input, c,  true);

            if (match) break;
        }

        if (!match)
            IO.println("The spirits whisper: \"There are something wrong with your action.\"");
    }

    private boolean seeMatch (String input, Corrupted c, boolean isTargetDir) {
        CommandResult result = level.sandbox.getExecutor().executeCommand(input.split(" "));

        if (!result.success()) {
            IO.println("The spirits whisper: \"There are something wrong with your action.\"");
            return false;
        } else {
            if (isTargetDir && !input.contains(c.getCorrectName())) {
                c.setIsCorrectName(true);
                c.setCorrectDir(true);
            } else if (!isTargetDir && input.contains(c.getTargetDir())) {
                c.setIsCorrectName(true);
                c.setCorrectDir(true);
            } else if (!isTargetDir)
                c.setIsCorrectName(true);
            else
                c.setIsCorrectName(true);

            return true;
        }
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
