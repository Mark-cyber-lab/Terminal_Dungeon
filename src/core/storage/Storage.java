package core.storage;

import utilities.DebugLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Storage {
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
                DebugLogger.log("Storage", "Created directory: " + parentDir);
            }

            Files.copy(source, this.itemPath);  // copy TO the file, not folder
            DebugLogger.log("Storage", "Stored item: " + this.itemPath);

        } catch (IOException e) {
            DebugLogger.log("Storage", "Failed to store item: " + this.itemPath);
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
                DebugLogger.log("Storage", "Retrieved item: " + itemPath);
                return Files.readAllLines(itemPath).toArray(new String[0]);
            } catch (IOException e) {
                DebugLogger.log("Storage", "Failed to read item: " + itemPath);
                e.printStackTrace();
            }
        } else {
            DebugLogger.log("Storage", "Item does not exist: " + itemPath);
        }
        return null;
    }

    public void deleteItem() {
        if (itemPath != null) {
            try {
                Files.deleteIfExists(itemPath);
                DebugLogger.log("Storage", "Deleted item: " + itemPath);
                itemPath = null;
            } catch (IOException e) {
                DebugLogger.log("Storage", "Failed to delete item: " + itemPath);
            }
        }
    }

    public Path getItemPath() {
        return itemPath;
    }
}
