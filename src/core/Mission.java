package core;

import core.enemies.Enemy;
import core.doors.HiddenDoor;
import core.items.Decoy;
import core.items.Shards;

import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<HiddenDoor> hiddenDoors = new ArrayList<>();
    private final List<Decoy> decoys = new ArrayList<>();
    private final List<Shards> shards = new ArrayList<>();

    public Mission addEnemy(Enemy enemy) {
        this.enemies.add(enemy);
        return this;
    }

    public Mission addDecoys(Decoy item) {
        this.decoys.add(item);
        return this;
    }

    public Mission addShards(Shards shards) {
        this.shards.add(shards);
        return this;
    }

    public Mission addHiddenDoor(HiddenDoor door) {
        this.hiddenDoors.add(door);
        return this;
    }

    public List<Enemy> getEnemies() {
        return this.enemies;
    }

    public  List<Decoy> getDecoyItems() {return this.decoys;}

    public List<Shards>  getShards() {return this.shards;}

    public List<HiddenDoor> getHiddenDoors() {
        return this.hiddenDoors;
    }

    public boolean shardCompleted() {
        return shards.isEmpty() ||
                shards.stream().allMatch(Shards::isCorrectDir);
    }

    public boolean decoyCompleted() {
        return decoys.isEmpty() ||
                decoys.stream().allMatch(Decoy::isDeleted);
    }

    public boolean isFulfilled() {

        boolean enemiesCleared = enemies.isEmpty() ||
                enemies.stream().allMatch(Enemy::hasBeenDefeated);

        boolean doorsCleared = hiddenDoors.isEmpty() ||
                hiddenDoors.stream().allMatch(HiddenDoor::hasBeenUnlocked);

        boolean deletedDecoys = decoys.isEmpty() ||
                decoys.stream().allMatch(Decoy::isDeleted);

        boolean movedShards = shards.isEmpty() ||
                shards.stream().allMatch(Shards::isCorrectDir);

        return enemiesCleared && doorsCleared && deletedDecoys && movedShards;
    }
}
