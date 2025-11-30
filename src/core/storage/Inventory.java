package core.storage;

import core.items.ObtainableItem;
import utilities.DebugLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {

    private final String id;
    private final String label;
    private final Path basePath; // base directory for all items
    private final List<ObtainableItem> items = new ArrayList<>();
    private boolean locked;

    public Inventory(String label, String id, String path) {
        this.label = label;
        this.id = id;
        this.basePath = Path.of(path);   // direct path
        ensureBasePath();
    }

    // Constructor 2: sandboxRoot + relative path
    public Inventory(String label, String id, String path, String sandboxPath) {
        this.label = label;
        this.id = id;

        // SAFELY combine sandbox + relative path
        this.basePath = Path.of(sandboxPath).resolve(path);

        ensureBasePath();
    }

    private void ensureBasePath() {
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                DebugLogger.log("Inventory", "Created base path: " + basePath);
            }
        } catch (Exception e) {
            DebugLogger.log("Inventory", "Failed to create base path: " + basePath);
            e.printStackTrace();
        }
    }

    public Path getBasePath() {
        return basePath;
    }

    // Add a file from source path into the bag
    public boolean addItem(String sourceFilePath, String fileName) {
        try {
            Storage storage = new Storage(sourceFilePath, fileName);
            ObtainableItem item = new ObtainableItem(fileName, fileName, storage);
            storeThenAddToItemList(item);
            DebugLogger.log("Inventory", "Added item: " + fileName + " to inventory: " + label);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean addItem(ObtainableItem item) {
        storeThenAddToItemList(item);
        DebugLogger.log("Inventory", "Added item: " + item.getStorage().source.getFileName().toString() + " to inventory: " + label);
        return true;
    }

    private void storeThenAddToItemList(ObtainableItem item) {
        item.getStorage().storeItem(basePath);
        items.add(item);
    }

    public boolean removeItem(ObtainableItem item) {
        if (items.remove(item)) {
            item.getStorage().deleteItem();
            DebugLogger.log("Inventory", "Removed item: " + item.getLabel() + " from inventory: " + label);
            return true;
        }
        return false;
    }

    public List<ObtainableItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public List<ObtainableItem> access() {
        if (locked) {
            DebugLogger.log("Inventory", "Storage [" + label + "] is locked! Cannot access items.");
            return Collections.emptyList();
        }
        DebugLogger.log("Inventory", "Accessing storage: " + label);
        return getItems();
    }

    public void discardAll() {
        for (ObtainableItem item : items) {
            item.discard(); // deletes the file
        }
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "label='" + label + '\'' +
                ", id='" + id + '\'' +
                ", items=" + items.size() +
                ", locked=" + locked +
                '}';
    }
}
