package core;

/**
 * Represents any living entity in the game world that has health,
 * can take damage, be healed, and eventually die.
 * <p>
 * This class provides core health handling and an ASCII-based health bar.
 * Subclasses define specific behaviors upon damage, healing, or death.
 */
public abstract class Alive {

    /** Current health of the entity (0–100). */
    protected int health = 100;

    /** Maximum possible health. */
    protected final int MAX_HEALTH = 100;

    /** Minimum possible health, representing death. */
    protected final int MIN_HEALTH = 0;

    /**
     * Returns the current health value.
     *
     * @return current health (0–100)
     */
    public int getHealth() {
        return health;
    }

    /**
     * Checks if the entity is still alive.
     *
     * @return {@code true} if health is above 0, otherwise false.
     */
    public boolean isAlive() {
        return health > MIN_HEALTH;
    }

    /**
     * Returns the current health percentage (0–100%).
     *
     * @return percentage representing remaining health
     */
    public int getHealthPercent() {
        return (int) ((health / (double) MAX_HEALTH) * 100);
    }

    /**
     * Returns an ASCII health bar using the default width of 30 characters.
     *
     * @return formatted health bar string
     */
    public String getHealthBar() {
        return buildHealthBar(30);
    }

    /**
     * Returns an ASCII health bar using a custom width.
     *
     * @param width total width of the bar in characters
     * @return formatted health bar string
     */
    public String getHealthBar(int width) {
        return buildHealthBar(width);
    }

    /**
     * Constructs an ASCII health bar based on current HP.
     * Filled segments use '█' and empty segments use '░'.
     *
     * @param width total number of segments in the bar
     * @return string containing the rendered health bar
     */
    private String buildHealthBar(int width) {
        double ratio = health / (double) MAX_HEALTH;
        int filled = (int) Math.round(ratio * width);
        int empty = width - filled;

        return "[" +
                "█".repeat(Math.max(0, filled)) +
                "░".repeat(Math.max(0, empty)) +
                "] " + getHealthPercent() + "%";
    }

    // ----------------------------------------------------------------------
    // DAMAGE HANDLING
    // ----------------------------------------------------------------------

    /**
     * Applies damage to this entity.
     * <p>
     * Triggers the {@link #onDamage(int)} hook and automatically prints
     * an updated health bar. If health reaches zero, {@link #onDeath()}
     * is invoked.
     *
     * @param amount amount of damage taken (must be > 0)
     */
    public void takeDamage(int amount) {
        if (amount <= 0) return;

        int old = health;

        health -= amount;
        if (health < MIN_HEALTH) health = MIN_HEALTH;

        onDamage(amount);

        IO.println("HP: " + getHealthBar());

        if (health == MIN_HEALTH) {
            onDeath();
        }
    }

    // ----------------------------------------------------------------------
    // HEALING
    // ----------------------------------------------------------------------

    /**
     * Heals the entity by a given amount.
     * <p>
     * Triggers the {@link #onHeal(int)} hook and prints the new health bar.
     *
     * @param amount amount to heal (must be > 0)
     */
    public void heal(int amount) {
        if (amount <= 0) return;

        int old = health;

        health += amount;
        if (health > MAX_HEALTH) health = MAX_HEALTH;

        onHeal(amount);

        IO.println("HP: " + getHealthBar());
    }

    // ----------------------------------------------------------------------
    // DIRECT HEALTH SETTER
    // ----------------------------------------------------------------------

    /**
     * Directly sets the health value.
     * <p>
     * Primarily used for debugging, admin commands, or special effects.
     * Automatically prints the updated health bar, and triggers death if set to 0.
     *
     * @param value new health value (clamped between MIN_HEALTH and MAX_HEALTH)
     */
    public void setHealth(int value) {
        if (value < MIN_HEALTH) value = MIN_HEALTH;
        if (value > MAX_HEALTH) value = MAX_HEALTH;

        health = value;

        IO.println("HP: " + getHealthBar());

        if (health == MIN_HEALTH) {
            onDeath();
        }
    }

    // ----------------------------------------------------------------------
    // ABSTRACT HOOKS (SUBCLASS BEHAVIOR)
    // ----------------------------------------------------------------------

    /**
     * Called when the entity takes damage.
     *
     * @param amount amount of damage received
     */
    protected abstract void onDamage(int amount);

    /**
     * Called when the entity heals.
     *
     * @param amount amount of HP restored
     */
    protected abstract void onHeal(int amount);

    /**
     * Called when the entity's health reaches zero.
     * Implement death behavior here.
     */
    protected abstract void onDeath();
}
