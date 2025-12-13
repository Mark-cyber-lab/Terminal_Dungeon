package com.terminaldungeon.elements.enemies;

import java.nio.file.Path;

public class Ghoul extends Enemy {
    private  static final  String NAME = "ghoul";
    private  static final  int DAMAGE = 20;

    public Ghoul(String id, Path enemyPath, boolean spawnable) {
        super(NAME, NAME + id, enemyPath, DAMAGE, spawnable);
    }
    public Ghoul(String id, Path enemyPath) {
        super(NAME, NAME + id, enemyPath, DAMAGE);
    }
}
