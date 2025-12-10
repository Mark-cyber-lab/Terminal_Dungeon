package v2.enemies;

import java.nio.file.Path;

public class Zombie extends Enemy {
    private static final String NAME = "zombie";
    private static final int DAMAGE = 15;

    public Zombie(String id, Path enemyPath, boolean spawnable) {
        super(NAME, NAME + id, enemyPath, DAMAGE, spawnable);
    }

    public Zombie(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }
}
