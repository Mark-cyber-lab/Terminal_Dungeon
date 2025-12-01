package core.enemies;

import java.nio.file.Path;

public class Vampire extends Enemy {
    private  static final  String NAME = "vampire";
    private  static final  int DAMAGE = 30;

    public Vampire(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }
}
