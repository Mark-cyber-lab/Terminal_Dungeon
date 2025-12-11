package player;

/**
 * Represents any living entity in the game world that has health,
 * can take damage, be healed, and eventually die.
 * <p>
 * This class provides core health handling and an ASCII-based health bar.
 * Subclasses define specific behaviors upon damage, healing, or death.
 */
public abstract class Alive {
    private final AliveStats stats;

    protected Alive(AliveStats stats) {
        this.stats = stats;
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

        int old = this.stats.getHealth();
        int newHealth = old - amount;

        this.stats.setHealth(newHealth);

        if (newHealth < this.stats.MIN_HEALTH) setHealth(this.stats.MIN_HEALTH);

        onDamage(amount);

        IO.println("HP: " + this.stats.getHealthBar());

        if (newHealth == this.stats.MIN_HEALTH) {
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

        int old = this.stats.getHealth();
        int newHealth = old + amount;

        this.stats.setHealth(newHealth);

        if (newHealth > this.stats.MIN_HEALTH) setHealth(this.stats.MIN_HEALTH);

        onHeal(amount);

        IO.println("HP: " + this.stats.getHealthBar());
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
        if (value < stats.MIN_HEALTH) stats.setHealth(stats.MIN_HEALTH);
        if (value > stats.MAX_HEALTH) stats.setHealth(stats.MAX_HEALTH);

        stats.setHealth(value);

        if (stats.getHealth() == stats.MIN_HEALTH) {
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
