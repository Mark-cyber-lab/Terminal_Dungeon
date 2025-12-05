package core;

import core.enemies.Enemy;
import core.doors.HiddenDoor;
import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<HiddenDoor> hiddenDoors = new ArrayList<>();

    public Mission addEnemy(Enemy enemy) {
        this.enemies.add(enemy);
        return this;
    }

    public Mission addHiddenDoor(HiddenDoor door) {
        this.hiddenDoors.add(door);
        return this;
    }

    public List<Enemy> getEnemies() {
        return this.enemies;
    }

    public List<HiddenDoor> getHiddenDoors() {
        return this.hiddenDoors;
    }

    public boolean isFulfilled() {

        boolean enemiesCleared = enemies.isEmpty() ||
                enemies.stream().allMatch(Enemy::hasBeenDefeated);

        boolean doorsCleared = hiddenDoors.isEmpty() ||
                hiddenDoors.stream().allMatch(HiddenDoor::hasBeenUnlocked);

        return enemiesCleared && doorsCleared;
    }
}
