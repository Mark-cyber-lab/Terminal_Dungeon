package v2;

import core.items.Decoy;
import core.items.Shards;
import v2.doors.HiddenDoor;
import v2.enemies.Enemy;
import v2.mechanics.CorrectPlacementValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Mission {

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<HiddenDoor> hiddenDoors = new ArrayList<>();
    private final List<CorrectPlacementValidator> placementValidators = new ArrayList<>();
    private final List<Decoy> decoys = new ArrayList<>();
    private final List<Shards> shards = new ArrayList<>();

    private final LinuxCommandExecutor linuxCommandExecutor;
    private final Player player;

    public Mission(LinuxCommandExecutor linuxCommandExecutor, Player player) {
        this.linuxCommandExecutor = linuxCommandExecutor;
        this.player = player;
    }

    public Mission addDecoys(Decoy item) {
        this.decoys.add(item);
        return this;
    }

    public Mission addShards(Shards shards) {
        this.shards.add(shards);
        return this;
    }

    public List<Decoy> getDecoyItems() {
        return this.decoys;
    }

    public List<Shards> getShards() {
        return this.shards;
    }

    public boolean shardCompleted() {
        return shards.isEmpty() || shards.stream().allMatch(Shards::isCorrectDir);
    }

    public boolean decoyCompleted() {
        return decoys.isEmpty() || decoys.stream().allMatch(Decoy::isDeleted);
    }

    // ---------------------------------------------------------
    // Add enemy to mission
    // ---------------------------------------------------------
    public Mission addEnemy(Enemy enemy) {
        enemies.add(enemy);
        return this;
    }

    // ---------------------------------------------------------
    // Add placement validator to mission
    // ---------------------------------------------------------
    public Mission addPlacementValidator(CorrectPlacementValidator placementValidator) {
        placementValidators.add(placementValidator);
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
        for (CorrectPlacementValidator p : placementValidators) {
            p.setPlayer(player);
            linuxCommandExecutor.useMiddleware(p);
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
        return enemies.stream().filter(e -> !e.hasBeenDefeated()).count();
    }

    public boolean allEnemiesDefeated() {
        return enemies.stream().allMatch(Enemy::hasBeenDefeated);
    }

    public Enemy getEnemyById(String id) {
        return enemies.stream().filter(e -> Objects.equals(e.getId(), id)).findFirst().orElse(null);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    // ---------------------------------------------------------
    // Hidden Door utilities
    // ---------------------------------------------------------
    public int totalHiddenDoors() {
        return hiddenDoors.size();
    }

    public long remainingLockedDoors() {
        return hiddenDoors.stream().filter(d -> !d.hasBeenUnlocked()).count();
    }

    public boolean allDoorsUnlocked() {
        return hiddenDoors.stream().allMatch(HiddenDoor::hasBeenUnlocked);
    }

    public List<HiddenDoor> getHiddenDoors() {
        return hiddenDoors;
    }

    // ---------------------------------------------------------
    // Placement Validator utilities
    // ---------------------------------------------------------
    public int totalPlacementValidators() {
        return placementValidators.size();
    }

    public long remainingIncorrectPlacementValidators() {
        return placementValidators.stream().filter(d -> !d.isCorrectlyPlaced()).count();
    }

    public boolean allCorrectPlacementValidators() {
        return placementValidators.stream().allMatch(CorrectPlacementValidator::isCorrectlyPlaced);
    }

    public List<CorrectPlacementValidator> getPlacementValidators() {
        return placementValidators;
    }

    // ---------------------------------------------------------
    // Mission fully cleared when: all enemies defeated AND all doors unlocked
    // ---------------------------------------------------------
    public boolean isFullyCleared() {
        return allEnemiesDefeated() && allDoorsUnlocked() && allCorrectPlacementValidators() && shardCompleted() && decoyCompleted();
    }

    public void cleanup() {
        hiddenDoors.clear();
        enemies.clear();
        placementValidators.clear();

        for (HiddenDoor d : hiddenDoors) {
            linuxCommandExecutor.removeMiddleware(d);
        }
        for (CorrectPlacementValidator p : placementValidators) {
            linuxCommandExecutor.removeMiddleware(p);
        }
        for (Enemy e : enemies) {
            e.setEnemyTroupe(enemies);
            linuxCommandExecutor.removeMiddleware(e);
        }
    }
}
