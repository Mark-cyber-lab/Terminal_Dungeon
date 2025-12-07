package v2;

import v2.doors.HiddenDoor;
import v2.enemies.Enemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Mission {

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<HiddenDoor> hiddenDoors = new ArrayList<>();
    private final LinuxCommandExecutor linuxCommandExecutor;
    private final Player player;

    public Mission(LinuxCommandExecutor linuxCommandExecutor, Player player) {
        this.linuxCommandExecutor = linuxCommandExecutor;
        this.player = player;
    }

    // ---------------------------------------------------------
    // Add enemy to mission
    // ---------------------------------------------------------
    public Mission addEnemy(Enemy enemy) {
        enemies.add(enemy);
        return this;
    }

    // ---------------------------------------------------------
    // Add hidden door to mission
    // ---------------------------------------------------------
    public Mission addHiddenDoor(HiddenDoor door) {
        hiddenDoors.add(door);
        return this;
    }

    public void initialize() {
        for (HiddenDoor d : hiddenDoors) {
            d.setPlayer(player);
            linuxCommandExecutor.useMiddleware(d);
        }
        for (Enemy e : enemies) {
            e.setPlayer(player);
            e.setEnemyTroupe(enemies);
            linuxCommandExecutor.useMiddleware(e);
        }
    }

    // ---------------------------------------------------------
    // Enemy utilities
    // ---------------------------------------------------------
    public int totalEnemies() {
        return enemies.size();
    }

    public long remainingEnemies() {
        return enemies.stream()
                .filter(e -> !e.hasBeenDefeated())
                .count();
    }

    public boolean allEnemiesDefeated() {
        return enemies.stream().allMatch(Enemy::hasBeenDefeated);
    }

    public Enemy getEnemyById(String id) {
        return enemies.stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElse(null);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    // ---------------------------------------------------------
    // Hidden Door utilities
    // ---------------------------------------------------------
    public Mission addDoor(HiddenDoor door) {
        hiddenDoors.add(door);
        return this;
    }

    public int totalHiddenDoors() {
        return hiddenDoors.size();
    }

    public long remainingLockedDoors() {
        return hiddenDoors.stream()
                .filter(d -> !d.hasBeenUnlocked())
                .count();
    }

    public boolean allDoorsUnlocked() {
        return hiddenDoors.stream().allMatch(HiddenDoor::hasBeenUnlocked);
    }

    public List<HiddenDoor> getHiddenDoors() {
        return hiddenDoors;
    }

    // ---------------------------------------------------------
    // Mission fully cleared when: all enemies defeated AND all doors unlocked
    // ---------------------------------------------------------
    public boolean isFullyCleared() {
        return allEnemiesDefeated() && allDoorsUnlocked();
    }

    public void cleanup() {
        hiddenDoors.clear();
        enemies.clear();

        for (HiddenDoor d : hiddenDoors) {
            linuxCommandExecutor.removeMiddleware(d);
        }
        for (Enemy e : enemies) {
            e.setEnemyTroupe(enemies);
            linuxCommandExecutor.removeMiddleware(e);
        }
    }
}
