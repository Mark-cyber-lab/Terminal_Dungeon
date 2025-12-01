package core;

import core.enemies.Enemy;
import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final List<Enemy> enemies = new ArrayList<>();

    public Mission addEnemy(Enemy enemy) {
        this.enemies.add(enemy);

        return this;
    }

    public List<Enemy> getEnemies() {
        return this.enemies;
    }

    public boolean isFulfilled() {
        return enemies.stream().allMatch(Enemy::hasBeenDefeated);
    }
}
