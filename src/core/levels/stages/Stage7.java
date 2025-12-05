package core.levels.stages;

import core.Mission;
import core.doors.HiddenDoor;
import core.doors.HiddenKey;
import core.enemies.Enemy;
import core.enemies.Goblin;
import core.enemies.Kobold;
import core.enemies.Ogre;
import core.items.Letter;
import core.levels.Level;
import core.listeners.Blocker;
import utilities.CLIUtils;
import utilities.CommandResult;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

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

        Mission mission = new Mission();

        Goblin outerPatrolGoblin = (Goblin) new Goblin("outer_gate_patrol", Path.of("./sandbox/level_4/outer_gate/patrol/goblin.mob")).setPlayer(level.player);
        Goblin trainingHallGoblin = (Goblin) new Goblin("training_hall", Path.of("./sandbox/level_4/inner_courtyard/training_hall/goblin.mob")).setPlayer(level.player);
        Goblin commandHallGoblin = (Goblin) new Goblin("command_hall", Path.of("./sandbox/level_4/command_hall/goblin.mob")).setPlayer(level.player);
        Kobold guardRoomKobold = (Kobold) new Kobold("guard_room", Path.of("./sandbox/level_4/inner_courtyard/guard_room/kobold.mob")).setPlayer(level.player);
        Ogre ogre = (Ogre) new Ogre("master", Path.of("./sandbox/level_4/access_master/ogre.mob")).setPlayer(level.player);

        HiddenKey commandHallKey = new HiddenKey(".key.secr", "COMMAND_HALL_KEY", null);
        HiddenKey accessMasterKey = new HiddenKey(".key.secr", "ACCESS_MASTER_KEY", null);

        HiddenDoor commandHallDoor = new HiddenDoor("command_hall", "command_hall", Path.of("./sandbox/level_4/command_hall")).unlocksBy(commandHallKey).setPlayer(level.player);
        HiddenDoor accessMAsterDoor = new HiddenDoor("access_master", "access_master", Path.of("./sandbox/level_4/access_master")).unlocksBy(accessMasterKey).setPlayer(level.player);

        mission
                .addEnemy(outerPatrolGoblin)
                .addEnemy(trainingHallGoblin)
                .addEnemy(commandHallGoblin)
                .addEnemy(guardRoomKobold)
                .addEnemy(ogre)
                .addHiddenDoor(commandHallDoor)
                .addHiddenDoor(accessMAsterDoor);


        level.sandbox.getExecutor().addBlocker(mission.getEnemies());
        level.sandbox.getExecutor().addBlocker(mission.getHiddenDoors());

        CLIUtils.typewriter("You step into the outer gate; sentries patrol the area.", 30);
        CLIUtils.typewriter("Enemies prevent careless grabs — keys cannot be picked up if a mob is present.", 30);
        CLIUtils.typewriter("Use ls to inspect folders, and cd to move", 30);
        CLIUtils.typewriter("Check patrol paths, training halls, and secret chambers for hidden keys and scrolls.", 30);
        CLIUtils.typewriter("Read scroll.txt to find guides and hints.", 30);
        CLIUtils.typewriter("Advance carefully through inner courtyard to command hall.", 30);

        String input;

        while (true) {
            System.out.print(">> ");
            input = IO.readln().trim();

            if (Arrays.asList(input.split(" ")).contains("tree")) {
                IO.println("You are not allowed to use tree in this stage.");
            }

            CommandResult result = level.sandbox.getExecutor().executeCommand(input.split(" "));

            if (result.success() && mission.isFulfilled()) {
                level.sandbox.getExecutor().removeBlocker(mission.getEnemies());
                level.sandbox.getExecutor().removeBlocker(mission.getHiddenDoors());
                break;
            }

        }

        CLIUtils.typewriter("Keep that letter fellas, and let's continue our journey. Type done if you are ready.", 30);
        level.sandbox.getExecutor().executeStrict("done");

        CLIUtils.typewriter("The Old Knight nods: 'Your Stage 7 training is complete. Greater adventures await…'", 30);
        CLIUtils.typewriter("\"Stage 7 complete. Prepare for what comes next.\"", 30);

    }

    @Override
    public void onSuccessPlay() {
        //        player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 3...\n");
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
