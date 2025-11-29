package core;

import utilities.AsciiArt;
import utilities.CLIUtils;

public class Player extends Alive {

    private int level = 1;
    private final Inventory inventory = new Inventory();

    private static final String[] RANKS = {
            "Unknown",
            "Squire",
            "Apprentice Knight",
            "Scout Knight",
            "Warrior Knight",
            "Guardian Knight",
            "Paladin",
            "Arcane Knight"
    };

    public Player() {}

    public Inventory getInventory() {
        return inventory;
    }

    public int getLevel() {
        return level;
    }

    public String getRankName() {
        if (level >= 1 && level < RANKS.length) {
            return RANKS[level];
        }
        return "Unknown Rank";
    }

    public void promoteLevel() {
        int MAX_LEVEL = 7;

        if (level < MAX_LEVEL) {
            level++;
            IO.println("You have been promoted to: " + getRankName() + "!");
        } else {
            IO.println("You have already reached maximum level!");
        }
    }

    /** --- Alive abstract hooks implementation --- */

    @Override
    protected void onDamage(int amount) {
        IO.println("You took " + amount + " damage! Health: " + health + "/" + MAX_HEALTH);
    }

    @Override
    protected void onHeal(int amount) {
        IO.println("You healed " + amount + " HP! Health: " + health + "/" + MAX_HEALTH);
    }

    @Override
    protected void onDeath() {
        CLIUtils.clearScreen();
        IO.println();
        CLIUtils.printCentered(AsciiArt.getGameOver());
        IO.println();
        IO.println(CLIUtils.center("⚠You have fallen in battle..."));
        IO.println();
    }
}
