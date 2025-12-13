package com.terminaldungeon.elements.items;

public class Decoy extends Movables {
    private boolean isDeleted = false;

    public Decoy(String name, String id) {
        super(name, id);
    }

    public boolean isDeleted() {return isDeleted;}
    public void setDeleted(boolean deleted) {isDeleted = deleted;}
}
