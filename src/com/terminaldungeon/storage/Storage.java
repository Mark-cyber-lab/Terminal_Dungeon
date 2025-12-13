package com.terminaldungeon.storage;

import com.terminaldungeon.utilities.Loggable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Storage implements Loggable {

    private String fileName; // full path inside the bag
    public Path source;
    public Path itemPath;

    /**
     * Copy the source file into the bag directory using a specified fileName.
     *
     * @param sourceFilePath original file path
     * @param fileName       name for the copied file in the bag
     */
    public Storage(String sourceFilePath, String fileName) {
        Path source = Path.of(sourceFilePath);
        if (!Files.exists(source)) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFilePath);
        }

        this.fileName = fileName;
        this.source = source;
    }

    public void storeItem(Path bagBasePath) {
        this.itemPath = bagBasePath.resolve(fileName); // full destination path

        try {
            // Create directory for the item file
            Path parentDir = this.itemPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log("Created directory: " + parentDir);
            }

            Files.copy(source, this.itemPath);  // copy TO the file, not folder
            log("Stored item: " + this.itemPath);

        } catch (IOException e) {
            log("Failed to store item: " + this.itemPath + " | Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the contents of the item as a String array.
     * Returns null if file does not exist or cannot be read.
     */
    public String[] retrieveItem() {
        if (itemPath != null && Files.exists(itemPath)) {
            try {
                log("Retrieved item: " + itemPath);
                return Files.readAllLines(itemPath).toArray(new String[0]);
            } catch (IOException e) {
                log("Failed to read item: " + itemPath + " | Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            log("Item does not exist: " + itemPath);
        }
        return null;
    }

    public void deleteItem() {
        if (itemPath != null) {
            try {
                Files.deleteIfExists(itemPath);
                log("Deleted item: " + itemPath);
                itemPath = null;
            } catch (IOException e) {
                log("Failed to delete item: " + itemPath + " | Error: " + e.getMessage());
            }
        }
    }

    public Path getItemPath() {
        return itemPath;
    }
}
