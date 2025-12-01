package core.enemies;


import java.nio.file.Path;

public class Kobold extends Enemy{
    private  static final  String NAME = "kobold";
    private  static final  int DAMAGE = 10;

    public Kobold(String id, Path enemyPath) {
        super(NAME,NAME + id, enemyPath, DAMAGE);
    }
}
