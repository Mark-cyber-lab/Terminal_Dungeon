package core;

import core.enemies.Enemy;
import core.doors.HiddenDoor;
import core.listeners.Blocker;

import core.items.Decoy;
import core.items.Shards;
import core.items.Corrupted;

import java.util.ArrayList;
import java.util.List;

public class Mission {

    private final List<Blocker> enemies = new ArrayList<>();
    private final List<Blocker> hiddenDoors = new ArrayList<>();
    private final Player player;

    public Mission(Player player) {
        this.player = player;
    }
    private final List<Decoy> decoys = new ArrayList<>();
    private final List<Shards> shards = new ArrayList<>();
    private final List<Corrupted> corrupts = new ArrayList<>();

    public Mission addEnemy(Enemy enemy) {
        enemy.setPlayer(player);
        this.enemies.add(enemy);
        return this;
    }

    public Mission addCorrupt(Corrupted corrupted) {
        this.corrupts.add(corrupted);
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
        door.setPlayer(player);
        this.hiddenDoors.add(door);
        return this;
    }

    public List<Blocker> getEnemies() {
        return this.enemies;
    }

    public List<Blocker> getHiddenDoors() {return this.hiddenDoors;}

    public  List<Decoy> getDecoyItems() {return this.decoys;}

    public List<Shards>  getShards() {return this.shards;}

    public List<Corrupted> getCorrupts() {return this.corrupts;}

    public boolean shardCompleted() {
        return shards.isEmpty() ||
                shards.stream().allMatch(Shards::isCorrectDir);
    }

    public boolean decoyCompleted() {
        return decoys.isEmpty() ||
                decoys.stream().allMatch(Decoy::isDeleted);
    }

    public boolean corruptPurified() {
        return corrupts.isEmpty() ||
                corrupts.stream().allMatch(Corrupted::isCorrectName) &&
                        corrupts.stream().allMatch(Corrupted::isCorrectDir);
    }

    public boolean isFulfilled() {

        boolean enemiesCleared = enemies.isEmpty() ||
                enemies.stream().allMatch(Blocker::isCleared);

        boolean doorsCleared = hiddenDoors.isEmpty() ||
                hiddenDoors.stream().allMatch(Blocker::isCleared);

        boolean deletedDecoys = this.decoyCompleted();

        boolean movedShards = this.shardCompleted();

        boolean purified = this.corruptPurified();

        return enemiesCleared && doorsCleared &&
                deletedDecoys && movedShards && purified;
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
