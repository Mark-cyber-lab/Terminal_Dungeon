package core;

import core.enemies.Enemy;
import java.util.ArrayList;
import java.util.List;

public class Mission {

    public final List<Enemy> enemies = new ArrayList<>();

    public void addEnemy(Enemy enemy) {
        this.enemies.add(enemy);
    }

    public boolean isFulfilled() {
        return enemies.stream().allMatch(Enemy::hasBeenDefeated);
    }
}
