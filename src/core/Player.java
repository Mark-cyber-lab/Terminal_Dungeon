package core;

import core.levels.Level;
import core.storage.Inventory;
import utilities.AsciiArt;
import utilities.CLIUtils;

public class Player extends Alive {

    private final PlayerStats playerStats;
    public int initialLevel = 1;
    private final int maxLevel = 7;
    private final Inventory inventory;
    private Level levelObj;
    private final String sandboxRoot;

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

    public Player(PlayerStats stats, String sandboxPath) {
        super(stats);
        this.playerStats = stats;
        this.sandboxRoot = sandboxPath;
        this.inventory = new Inventory("inventory", "inventory1", "./inventory", sandboxPath);
    }

    public String getSandboxRoot() {
        return sandboxRoot;
    }

    public PlayerStats getStats() {
        return playerStats;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Level getLevelObj() {
        return levelObj;
    }

    public void setLevelObj(Level levelObj) {
        this.levelObj = levelObj;
    }

    public String getRankName() {
        int level = playerStats.getLevel();
        if (level >= 1 && level < RANKS.length) {
            return RANKS[level];
        }
        return "Unknown Rank";
    }

    public void promoteLevel() {
        int level = playerStats.getLevel();
        if (level < maxLevel) {
            playerStats.setLevel(level + 1);
            IO.println("You have been promoted to: " + getRankName() + "!");
        } else {
            IO.println("You have already reached maximum level!");
        }
    }

    public void promoteLevelTo(int newLevel) {
        int level = playerStats.getLevel();
        if (level < maxLevel) {
            playerStats.setLevel(newLevel);
            initialLevel = newLevel;
        } else {
            throw new IllegalArgumentException("You have already reached maximum level!");
        }
    }

    /**
     * --- Alive abstract hooks implementation ---
     */

    @Override
    protected void onDamage(int amount) {
        IO.println("You took " + amount + " damage! Health: " + playerStats.getHealth() + "/" + playerStats.MAX_HEALTH);
    }

    @Override
    protected void onHeal(int amount) {
        IO.println("You healed " + amount + " HP! Health: " + playerStats.getHealth() + "/" + playerStats.MAX_HEALTH);
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
