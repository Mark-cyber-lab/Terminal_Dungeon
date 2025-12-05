package core;

import core.enemies.Enemy;
import core.doors.HiddenDoor;
import core.listeners.Blocker;

import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final List<Blocker> enemies = new ArrayList<>();
    private final List<Blocker> hiddenDoors = new ArrayList<>();

    public Mission addEnemy(Enemy enemy) {
        this.enemies.add(enemy);
        return this;
    }

    public Mission addHiddenDoor(HiddenDoor door) {
        this.hiddenDoors.add(door);
        return this;
    }

    public List<Blocker> getEnemies() {
        return this.enemies;
    }

    public List<Blocker> getHiddenDoors() {
        return this.hiddenDoors;
    }

    public boolean isFulfilled() {

        boolean enemiesCleared = enemies.isEmpty() ||
                enemies.stream().allMatch(Blocker::isCleared);

        boolean doorsCleared = hiddenDoors.isEmpty() ||
                hiddenDoors.stream().allMatch(Blocker::isCleared);

        return enemiesCleared && doorsCleared;
    }
}
