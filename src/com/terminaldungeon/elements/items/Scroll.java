package com.terminaldungeon.elements.items;

import com.terminaldungeon.storage.Storage;

public class Scroll extends RetrievableItem {

    private final static String EXTENSION = "_scroll.txt";

    private static Storage initStorage(String name, String sourceFilePath, String extension) {
        return new Storage(sourceFilePath, name + extension);
    }

    public Scroll(String name, String sourceFilePath) {
        Storage storage = initStorage(name, sourceFilePath, EXTENSION);
        super(sourceFilePath, name, storage);
    }

    public Scroll(String name, String sourceFilePath, String extension) {
        Storage storage = initStorage(name, sourceFilePath, extension);
        super(sourceFilePath, name, storage);
    }

    public void read() {
        for (String line : retrieve()) {
            IO.println(line);
        }
    }
}
