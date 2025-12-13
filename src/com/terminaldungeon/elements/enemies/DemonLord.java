package com.terminaldungeon.elements.enemies;

import com.terminaldungeon.gameplay.Mission;
import com.terminaldungeon.gameplay.CommandResult;

import java.nio.file.Path;

public class DemonLord extends Enemy {
    private static final String NAME = "demon lord";
    private static final int DAMAGE = 40;

    // DemonLord itself must not be spawnable
    public DemonLord(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }

    public static void spawnEnemies(Mission mission, int loopCount, int multiplier, CommandResult result) {
        switch (loopCount) {
            case 1:
                Goblin goblin = new Goblin(
                        "goblin" + multiplier * loopCount,
                        result.getContext().endDir.resolve("goblin" + multiplier * loopCount + ".mob"), true
                );
                mission.addEnemy(goblin);
                break;
            case 2:
                Kobold kobold = new Kobold(
                        "kobold" + multiplier * loopCount,
                        result.getContext().endDir.resolve("kobold" + multiplier * loopCount + ".mob"), true
                );
                mission.addEnemy(kobold);
                break;
            case 3:
                Zombie zombie = new Zombie(
                        "zombie" + multiplier * loopCount,
                        result.getContext().endDir.resolve("zombie" + multiplier * loopCount + ".mob"), true
                );
                mission.addEnemy(zombie);
                break;
            case 4:
                Ghoul ghoul = new Ghoul(
                        "ghoul" + multiplier * loopCount,
                        result.getContext().endDir.resolve("ghoul" + multiplier * loopCount + ".mob"), true
                );
                mission.addEnemy(ghoul);
                break;
            case 5:
                Ogre ogre = new Ogre(
                        "ogre" + multiplier * loopCount,
                        result.getContext().endDir.resolve("ogre" + multiplier * loopCount + ".mob"), true
                );
                mission.addEnemy(ogre);
        }
    }
}
