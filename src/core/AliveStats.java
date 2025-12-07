package core;

public class AliveStats {
    /** Current health points */
    protected int health = 100;

    /** Maximum allowed health */
    public final int MAX_HEALTH = 100;

    /** Minimum allowed health */
    protected final int MIN_HEALTH = 0;

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        if (health < MIN_HEALTH) health = MIN_HEALTH;
        if (health > MAX_HEALTH) health = MAX_HEALTH;
        this.health = health;
    }

    /** Returns true if the entity is alive */
    public boolean isAlive() {
        return health > MIN_HEALTH;
    }

    /** Returns the health percentage (0–100) */
    public int getHealthPercent() {
        return (int) ((health / (double) MAX_HEALTH) * 100);
    }

    /**
     * Returns a simple ASCII health bar.
     *
     * @param width width of the bar in characters
     * @return ASCII health bar
     */
    public String getHealthBar(int width) {
        double ratio = health / (double) MAX_HEALTH;
        int filled = (int) Math.round(ratio * width);
        int empty = width - filled;

        return "[" +
                "█".repeat(Math.max(0, filled)) +
                "░".repeat(Math.max(0, empty)) +
                "] " + getHealthPercent() + "%";
    }

    /** Default width health bar */
    public String getHealthBar() {
        return getHealthBar(30);
    }

}
