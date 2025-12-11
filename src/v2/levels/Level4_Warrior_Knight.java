package v2.levels;

import v2.player.Player;
import v2.levels.stages.Stage;
import v2.levels.stages.Stage7;
import v2.engine.Sandbox;
import utilities.AsciiArt;
import utilities.CLIUtils;
import v2.levels.stages.Stage8;

public class Level4_Warrior_Knight extends Level {

    public Level4_Warrior_Knight(Sandbox sandbox, Player player) {
        super(4, player, sandbox);
    }

    @Override
    public void setup() {
        Stage Stage7 = new Stage7(this);
        Stage Stage8 = new Stage8(this);
        addStage(Stage7);
        addStage(Stage8);
    }

    @Override
    public String getDescription() {
        return "Level 4 — Warrior Knight (Orbs and Hidden Doors Exploration)";
    }

    @Override
    public void printLevelHeader() {
        CLIUtils.header(getLevelHeader(), 1);
    }

    @Override
    public String[] getLevelHeader() {
        return AsciiArt.getLevel4WarriorKnight();
    }

    @Override
    public void onBeforeInit() {

    }

    @Override
    public void onLevelComplete() {
        IO.println("You have mastered this level, Warrior Knight!");
        IO.println("You feel a surge of confidence as you prepare for Level 5...\n");
    }
}