package v2;

import core.Alive;
import core.PlayerStats;
import core.storage.Inventory;
import utilities.AsciiArt;
import utilities.CLIUtils;
import v2.levels.Level;

public class Player extends Alive {

    private final PlayerStats playerStats;
    public int initialLevel = 1;
    private final int maxLevel = 6;
    private Level levelObj;
    private final String sandboxRoot;

    private static final String[] RANKS = {
            "Unknown",
            "Squire",
            "Apprentice Knight",
            "Scout Knight",
            "Warrior Knight",
            "Guardian Knight",
            "Arcane Knight"
    };

    public Player(PlayerStats stats, String sandboxPath) {
        super(stats);
        this.playerStats = stats;
        this.sandboxRoot = sandboxPath;
    }

    public String getSandboxRoot() {
        return sandboxRoot;
    }

    public PlayerStats getStats() {
        return playerStats;
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
