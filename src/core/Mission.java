package core;

import core.enemies.Enemy;
import core.doors.HiddenDoor;
import core.listeners.Blocker;

import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final List<Blocker> enemies = new ArrayList<>();
    private final List<Blocker> hiddenDoors = new ArrayList<>();
    private final Player player;

    public Mission(Player player) {
        this.player = player;
    }

    public Mission addEnemy(Enemy enemy) {
        enemy.setPlayer(player);
        this.enemies.add(enemy);
        return this;
    }

    public Mission addHiddenDoor(HiddenDoor door) {
        door.setPlayer(player);
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

    public void initialize(){
        player.getLevelObj().sandbox.getExecutor().addBlocker(enemies);
        player.getLevelObj().sandbox.getExecutor().addBlocker(hiddenDoors);
    }

    public void cleanUp() {
        if(!isFulfilled()) return;

        player.getLevelObj().sandbox.getExecutor().removeBlocker(enemies);
        player.getLevelObj().sandbox.getExecutor().removeBlocker(hiddenDoors);
        enemies.clear();
        hiddenDoors.clear();
    }
}
