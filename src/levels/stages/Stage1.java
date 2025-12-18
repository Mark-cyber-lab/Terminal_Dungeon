package levels.stages;

import utilities.CLIUtils;
import levels.Level;

public class Stage1 extends Stage {
    private static final String configPath = "stages/stage1.txt";

    public Stage1(Level level) {
        super(1, level);
    }

    @Override
    public String[] getStageHeader() {
        return new String[]{"Stage 1 — Training of the Young Squire"};
    }

    @Override
    public void play() {
        // Level 1: Introduction sequence
        CLIUtils.typewriter("The cold wind sweeps through the stone hall as torches ignite one by one.", 30);
        CLIUtils.typewriter("An armored figure steps forward — the Old Knight.", 30);
        CLIUtils.typewriter("\"Squire... you awaken in the Dungeon of Paths. Every warrior who survived began here.\"", 30);
        CLIUtils.typewriter("\"Before you lift blade or spell, you must first command the dungeon itself.\"", 30);
        CLIUtils.typewriter("\"Speak the words: pwd to sense your current location.\"", 30);
        level.sandbox.getExecutor().executeStrict("pwd"); // input handling
        CLIUtils.sleep(300);
        // After 'pwd'
        CLIUtils.typewriter("The knight nods: 'Good. A warrior who knows his ground cannot be lost.'", 30);
        CLIUtils.typewriter("\"Now, observe the surrounding chambers: ls\"", 30);
        level.sandbox.getExecutor().executeStrict("ls");
        CLIUtils.sleep(300);
        // After 'ls'
        CLIUtils.typewriter("Four paths reveal themselves: basic_training, courtyard, hall_of_movement, and navigation_test.", 30);
        CLIUtils.typewriter("\"Your first task: enter the Chamber of Beginnings: cd basic_training\"", 30);
        level.sandbox.getExecutor().executeStrict("cd basic_training");
        CLIUtils.sleep(300);
        // Inside basic_training/
        CLIUtils.typewriter("Dust stirs as you enter. Old banners sway gently.", 30);
        CLIUtils.typewriter("\"List the notes here: ls\"", 30);
        level.sandbox.getExecutor().executeStrict("ls");
        CLIUtils.sleep(300);
        CLIUtils.typewriter("See the contents? Explore the structure using: tree", 30);
        level.sandbox.getExecutor().executeStrict("tree");
        CLIUtils.sleep(300);
        CLIUtils.typewriter("\"Return to the central hall: cd ..\"", 30);
        level.sandbox.getExecutor().executeStrict("cd ..");
        CLIUtils.sleep(300);
        // Back at level_1/
        CLIUtils.typewriter("The knight gestures toward the Courtyard of Awareness.", 30);
        CLIUtils.typewriter("\"Enter it now: cd courtyard\"", 30);
        level.sandbox.getExecutor().executeStrict("cd courtyard");
        CLIUtils.sleep(300);
        // Inside courtyard/
        CLIUtils.typewriter("Birds scatter as you step inside. Look around: ls", 30);
        level.sandbox.getExecutor().executeStrict("ls");
        CLIUtils.sleep(300);
        CLIUtils.typewriter("Check the courtyard structure visually with: tree", 30);
        level.sandbox.getExecutor().executeStrict("tree");
        CLIUtils.sleep(300);
        CLIUtils.typewriter("\"Return to the central hall: cd ..\"", 30);
        level.sandbox.getExecutor().executeStrict("cd ..");
        CLIUtils.sleep(300);
        // Back at level_1/
        CLIUtils.typewriter("Now — the Hall of Movement. Enter: cd hall_of_movement", 30);
        level.sandbox.getExecutor().executeStrict("cd hall_of_movement");
        CLIUtils.sleep(300);
        // Inside hall_of_movement/
        CLIUtils.typewriter("Lanterns sway. The paths stretch before you. Inspect: ls paths", 30);
        level.sandbox.getExecutor().executeStrict("ls paths");
        CLIUtils.sleep(300);
        CLIUtils.typewriter("Understand each path's direction with: tree paths", 30);
        level.sandbox.getExecutor().executeStrict("tree paths");
        CLIUtils.sleep(300);
        CLIUtils.typewriter("\"Return to the central hall: cd ..\"", 30);
        level.sandbox.getExecutor().executeStrict("cd ..");
        CLIUtils.sleep(300);
        // Back at level_1/
        CLIUtils.typewriter("A deep rumble shakes the chamber as the Navigation Test door glows faintly.", 30);
        CLIUtils.typewriter("\"You have done well, squire. You have explored the training halls with patience.\"", 30);
        CLIUtils.typewriter("\"Much remains hidden, but your first lesson in navigation is complete.\"", 30);
        CLIUtils.typewriter("\"When ready, speak the word: done\"", 30);
        level.sandbox.getExecutor().executeStrict("done");
    }

    @Override
    public void onSuccessPlay() {
        //        player.remember("Learned pwd (location awareness)");
        IO.println("Stage complete! Proceeding to Stage 2...\n");
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
        level.sandbox.getGranter().grant("tree");
        level.sandbox.getGranter().grant("done");
    }
}
