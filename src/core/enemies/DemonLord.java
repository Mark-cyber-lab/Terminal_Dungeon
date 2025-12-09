package core.enemies;

import core.Mission;
import utilities.CommandResult;

import java.nio.file.Path;

public class DemonLord extends Enemy{
    private static final  String NAME = "demon lord";
    private static final int DAMAGE = 40;

    public DemonLord(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }

    public static void spawnEnemiies (Mission mission, int loopCount, int multiplier, CommandResult result) {
        switch (loopCount) {
            case 1:
                Goblin goblin = new Goblin(
                        "goblin" + multiplier * loopCount,
                        Path.of(result.path() + "goblin" + multiplier * loopCount + ".mob")
                );
                mission.addEnemy(goblin);
                break;
            case 2:
                Kobold kobold = new Kobold(
                        "kobold" + multiplier * loopCount,
                        Path.of(result.path() + "kobold" + multiplier * loopCount + ".mob")
                );
                mission.addEnemy(kobold);
                break;
            case 3:
                Zombie zombie = new Zombie(
                        "zombie" + multiplier * loopCount,
                        Path.of(result.path() + "zombie" + multiplier * loopCount + ".mob")
                );
                mission.addEnemy(zombie);
                break;
            case 4:
                Ghoul ghoul = new Ghoul(
                        "ghoul" + multiplier * loopCount,
                        Path.of(result.path() + "ghoul" + multiplier * loopCount + ".mob")
                );
                mission.addEnemy(ghoul);
                break;
            case 5:
                Ogre ogre = new Ogre(
                        "ogre" + multiplier * loopCount,
                        Path.of(result.path() + "ogre" + multiplier * loopCount + ".mob")
                );
                mission.addEnemy(ogre);
        }
    }
}
