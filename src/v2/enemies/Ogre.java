package v2.enemies;

import java.nio.file.Path;

public class Ogre extends Enemy {
    private  static final  String NAME = "ogre";
    private  static final  int DAMAGE = 25;

    public Ogre(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }
}
