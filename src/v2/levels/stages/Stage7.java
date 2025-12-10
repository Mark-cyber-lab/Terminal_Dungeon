package v2.levels.stages;

import v2.Mission;
import v2.doors.*;
import v2.enemies.*;
import v2.levels.Level;
import utilities.CLIUtils;
import v2.CommandResult;

import java.nio.file.Path;

public class Stage7 extends Stage {
    private static final String configPath = "./src/stages/stage7.txt";

    public Stage7(Level level) {
        super(7, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 7 — The Hidden Doors"};
    }

    @Override
    public void play() {

        Mission mission = new Mission(level.sandbox.getExecutor(), level.player);

        Goblin outerPatrolGoblin = new Goblin("outer_gate_patrol", Path.of("./sandbox/outer_gate/patrol/goblin.mob"));
        Goblin trainingHallGoblin = new Goblin("training_hall", Path.of("./sandbox/inner_courtyard/training_hall/goblin.mob"));
        Goblin commandHallGoblin = new Goblin("command_hall", Path.of("./sandbox/.command_hall/goblin.mob"));
        Kobold guardRoomKobold = new Kobold("guard_room", Path.of("./sandbox/inner_courtyard/guard_room/kobold.mob"));
        Ogre ogre = new Ogre("master", Path.of("./sandbox/.access_master/ogre.mob"));

        HiddenKey commandHallKey = new HiddenKey(".key.secr", "COMMAND_HALL_KEY", null);
        HiddenKey accessMasterKey = new HiddenKey(".key.secr", "ACCESS_MASTER_KEY", null);

        HiddenDoor commandHallDoor = new HiddenDoor("command_hall", "command_hall", Path.of("./sandbox/.command_hall")).unlocksBy(commandHallKey);
        HiddenDoor accessMAsterDoor = new HiddenDoor("access_master", "access_master", Path.of("./sandbox/.access_master")).unlocksBy(accessMasterKey);

        mission
                .addEnemy(outerPatrolGoblin)
                .addEnemy(trainingHallGoblin)
                .addEnemy(commandHallGoblin)
                .addEnemy(guardRoomKobold)
                .addEnemy(ogre)
                .addHiddenDoor(commandHallDoor)
                .addHiddenDoor(accessMAsterDoor);


        mission.initialize();

        CLIUtils.typewriter("You step into the outer gate; sentries patrol the area.", 30);
        CLIUtils.typewriter("Enemies prevent careless grabs — keys cannot be picked up if a mob is present.", 30);
        CLIUtils.typewriter("Use ls to inspect folders, and cd to move", 30);
        CLIUtils.typewriter("Check patrol paths, training halls, and secret chambers for hidden keys and scrolls.", 30);
        CLIUtils.typewriter("Read scroll.txt to find guides and hints.", 30);
        CLIUtils.typewriter("Advance carefully through inner courtyard to command hall.", 30);

        String input;

        int totalEnemies = mission.totalEnemies();
        int totalHiddenDoors = mission.totalHiddenDoors();

        do {
            IO.println(mission.remainingEnemies() + "/" + totalEnemies + " monster/s remaining.");
            IO.println(mission.remainingLockedDoors() + "/" + totalHiddenDoors + " hidden door/s unlocked.");

            IO.print(">> ");
            input = IO.readln().trim();

            try {
                CommandResult result = level.sandbox.getExecutor().execute(input);
            } catch (Exception e) {
            }

        } while (mission.remainingEnemies() != 0 && level.player.getStats().isAlive());

        mission.cleanup();

        if(!level.player.getStats().isAlive()) return;

        CLIUtils.typewriter("All doors unlocked and enemies are already defeated, and let's continue our journey. Type done if you are ready.", 30);
        level.sandbox.getExecutor().executeStrict("done");

        CLIUtils.typewriter("The Old Knight nods: 'Your Stage 7 training is complete. Greater adventures await…'", 30);
        CLIUtils.typewriter("\"Stage 7 complete. Prepare for what comes next.\"", 30);

    }

    @Override
    public void onSuccessPlay() {
        IO.println("Stage complete! Proceeding to Stage 8...\n");
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
