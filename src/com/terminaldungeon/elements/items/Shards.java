package com.terminaldungeon.elements.items;

public class Shards extends Movables {
    private final String targetDir;
    private boolean correctDir = false;


    public Shards(String name, String id, String targetDir) {
        super(name, id);
        this.targetDir = targetDir;
    }

    public String getTargetDir() {return targetDir;}
    public boolean isCorrectDir() {return correctDir;}

    public void setCorrectDir(boolean correctDir) {this.correctDir = correctDir;}
}
