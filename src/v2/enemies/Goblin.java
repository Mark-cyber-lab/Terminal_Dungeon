package v2.enemies;

import java.nio.file.Path;

public class Goblin extends Enemy {
    private static final String NAME = "goblin";
    private static final int DAMAGE = 5;

    public Goblin(String id, Path enemyPath, boolean spawnable) {
        super(NAME, NAME + id, enemyPath, DAMAGE, spawnable);
    }
    public Goblin(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }
}
