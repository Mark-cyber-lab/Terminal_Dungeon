package v2.levels.stages;

import v2.Mission;
import v2.doors.*;
import v2.enemies.*;
import v2.levels.Level;
import utilities.CLIUtils;
import v2.CommandResult;
import v2.mechanics.CorrectPlacementValidator;

import java.nio.file.Path;

public class Stage8 extends Stage {
    private static final String configPath = "./src/stages/stage7.txt";

    public Stage8(Level level) {
        super(8, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 8 — Order Restoration to the Fractured Archive"};
    }

    @Override
    public void play() {

        Mission mission = new Mission(level.sandbox.getExecutor(), level.player);

        CorrectPlacementValidator w1 = new CorrectPlacementValidator("orb_fragment_w1.frg", Path.of("./sandbox/level_4/fractured_archive/west_wing/low_shelves"), true);
        CorrectPlacementValidator w2 = new CorrectPlacementValidator("orb_fragment_w2.frg", Path.of("./sandbox/level_4/fractured_archive/east_wing/middle_shelves"));
        CorrectPlacementValidator e1 = new CorrectPlacementValidator("orb_fragment_e1.frg", Path.of("./sandbox/level_4/fractured_archive/east_wing/.quiet_rack"));
        CorrectPlacementValidator f1 = new CorrectPlacementValidator("orb_fragment_f1.frg", Path.of("./sandbox/level_4/fractured_archive/central_hub"));
        CorrectPlacementValidator e2 = new CorrectPlacementValidator("orb_fragment_e2.frg", Path.of("./sandbox/level_4/fractured_archive/west_wing/low_shelves"));

        Goblin nimbleGoblin = new Goblin("nimble", Path.of("./sandbox/level_4/fractured_archive/west_wing/low_shelves/goblin.mob"));
        Kobold cunningKobold = new Kobold("cunning", Path.of("./sandbox/level_4/fractured_archive/west_wing/upper_shelves/kobold.mob"));
        Ogre hulkingOgre = new Ogre("hulking", Path.of("./sandbox/level_4/fractured_archive/east_wing/high_shelves/ogre.mob"));
        Ogre massiveOgre = new Ogre("massiveOgre", Path.of("./sandbox/level_4/fractured_archive/central_hub/ogre.mob"));
        Ghoul ghastlyGhoul = new Ghoul("ghastly", Path.of("./sandbox/level_4/fractured_archive/misaligned_stacks/ghoul.mob"));

        HiddenKey quietRackKey = new HiddenKey("quiet_rack", "QUIET_RACK_KEY", Path.of("./sandbox/level_4/storage_bay/.key.secr"));
        HiddenDoor quietRack = new HiddenDoor("quiet_rack", "1", Path.of("./sandbox/level_4/fractured_archive/east_wing/.quiet_rack")).unlocksBy(quietRackKey);

        mission.initialize();

        mission.addPlacementValidator(w1)
                .addPlacementValidator(w2)
                .addPlacementValidator(e1)
                .addPlacementValidator(f1)
                .addPlacementValidator(e2);

        mission.addEnemy(nimbleGoblin)
                .addEnemy(cunningKobold)
                .addEnemy(hulkingOgre)
                .addEnemy(massiveOgre)
                .addEnemy(ghastlyGhoul);

        mission.addHiddenDoor(quietRack);

        CLIUtils.typewriter("You step into the Fractured Archive. Shelves tremble and fragments hum.", 30);
        CLIUtils.typewriter("Keeper Arvian appears from the shadows: \"Warrior Knight, the archive quivers. Listen to their hums and beware the guardians.\"", 30);
        CLIUtils.typewriter("Some fragments are real, some are decoys. Use ls, ls -a, cd, and cat to explore and visualize their pulses.", 30);
        CLIUtils.typewriter("Move only real fragments and organize them into their correct shelves.", 30);

        String input;

        int totalEnemies = mission.totalEnemies();
        int totalHiddenDoors = mission.totalHiddenDoors();
        int totalFragments = mission.totalPlacementValidators();

        do {
            IO.println(mission.remainingEnemies() + "/" + totalEnemies + " monster/s remaining.");
            IO.println(mission.remainingLockedDoors() + "/" + totalHiddenDoors + " hidden door/s unlocked.");
            IO.println(mission.remainingIncorrectPlacementValidators() + "/" + totalFragments + " misplaced fragments/s remained.");

            IO.print(">> ");
            input = IO.readln().trim();

            try {
                CommandResult result = level.sandbox.getExecutor().execute(input);
            } catch (Exception e) {
            }

        } while (!mission.isFullyCleared());

        mission.cleanup();

        CLIUtils.typewriter("A stabilizing resonance echoes through the archive. Type done to move forward.", 30);
        level.sandbox.getExecutor().executeStrict("done");

        CLIUtils.typewriter("The Old Knight nods: 'Stage 8 complete. The fractured memories align once more.'", 30);
        CLIUtils.typewriter("\"Your precision strengthens the archive, Warrior Knight.\"", 30);

    }

    @Override
    public void onSuccessPlay() {
        //        player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 9...\n");
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
    }
}
