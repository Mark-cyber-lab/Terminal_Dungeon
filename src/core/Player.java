package core;

public class Player {

    private int level = 1;
    private final Inventory inventory = new Inventory();

    // Rank names for each level (1–7)
    private static final String[] RANKS = {
            "Unknown",          // index 0 (unused)
            "Squire",           // Level 1
            "Apprentice Knight",// Level 2
            "Scout Knight",     // Level 3
            "Warrior Knight",   // Level 4
            "Guardian Knight",  // Level 5
            "Paladin",          // Level 6
            "Arcane Knight"     // Level 7
    };

    public Player() {}

    public Inventory getInventory() {
        return inventory;
    }

    public int getLevel() {
        return level;
    }

    /** Returns the rank name based on current level. */
    public String getRankName() {
        if (level >= 1 && level < RANKS.length) {
            return RANKS[level];
        }
        return "Unknown Rank";
    }

    /** Promote player to the next level (max = 7). */
    public void promoteLevel() {
        int MAX_LEVEL = 7;

        if (level < MAX_LEVEL) {
            level++;
            IO.println("✨ You have been promoted to: " + getRankName() + "!");
        } else {
            IO.println("⚠️ You have already reached maximum level: " + getRankName());
        }
    }
}
