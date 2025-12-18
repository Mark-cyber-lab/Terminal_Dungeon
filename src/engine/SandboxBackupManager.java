package engine;

import utilities.Loggable;
import storage.Inventory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public class SandboxBackupManager implements Loggable {

    private final Path sandBoxPath;
    private final Path cacheDir;
    private final Inventory inventory;

    public enum BackupMode {
        BACKUP_ALL,
        BACKUP_ONLY_INVENTORY,
        BACKUP_SPECIFIC_DIR
    }

    public enum FlushMode {
        ALL,
        EXCEPT_INVENTORY,
        SPECIFIC_DIR
    }

    public SandboxBackupManager(Path sandboxPath, Inventory inventory) {
        this.sandBoxPath = sandboxPath;
        this.inventory = inventory;

        // cache folder beside sandbox
        this.cacheDir = sandboxPath.getParent().resolve("cache");

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize cache directory", e);
        }
    }

    // ---------------- BACKUP ----------------

    public boolean backup(BackupMode mode, String identifier) throws IOException {
        return backup(mode, null, identifier);
    }

    public boolean backup(BackupMode mode, Path specificDir, String identifier) {
        try {
            if (!Files.exists(sandBoxPath))
                throw new IOException("Sandbox root does not exist: " + sandBoxPath);

            Path backupDir = getBackupDir(identifier);

            if (Files.exists(backupDir)) {
                log("Backup folder exists. Overwriting...");
                deleteDirectoryRecursively(backupDir);
            }

            log("Creating backup at: " + backupDir.toAbsolutePath());
            Files.createDirectories(backupDir);

            switch (mode) {

                case BACKUP_ALL -> {
                    log("Backing up entire sandbox...");
                    copyDirectoryRecursively(sandBoxPath, backupDir);
                }

                case BACKUP_ONLY_INVENTORY -> {
                    Path source = sandBoxPath.resolve("inventory");
                    if (!Files.exists(source))
                        throw new IOException("Inventory folder missing: " + source);

                    Path target = backupDir.resolve("inventory");
                    Files.createDirectories(target);
                    copyDirectoryRecursively(source, target);
                }

                case BACKUP_SPECIFIC_DIR -> {
                    if (specificDir == null)
                        throw new IOException("BACKUP_SPECIFIC_DIR requires a path");

                    Path src = sandBoxPath.resolve(specificDir).normalize();
                    if (!Files.exists(src))
                        throw new IOException("Directory does not exist: " + src);

                    Path dest = backupDir.resolve(src.getFileName());
                    Files.createDirectories(dest);
                    copyDirectoryRecursively(src, dest);
                }
            }

            deleteDirectoryRecursively(sandBoxPath);
            log("Backup completed!");
            return true;

        } catch (Exception e) {
            log(e.getMessage());
            return false;
        }
    }

    // ---------------- FLUSH ----------------

    public void flush(FlushMode mode) throws IOException {
        flush(mode, null);
    }

    public void flush(FlushMode mode, String specificDirName) throws IOException {

        switch (mode) {

            case ALL -> {
                log("Flushing ALL contents...");
                try (var stream = Files.list(sandBoxPath)) {
                    for (Path p : stream.toList()) {
                        deleteDirectoryRecursively(p);
                    }
                }
            }

            case EXCEPT_INVENTORY -> {
                log("Flushing all EXCEPT inventory...");
                try (var stream = Files.list(sandBoxPath)) {
                    for (Path p : stream.toList()) {
                        if (p.getFileName().toString().equals("inventory")) continue;
                        deleteDirectoryRecursively(p);
                    }
                }
            }

            case SPECIFIC_DIR -> {
                if (specificDirName == null || specificDirName.isBlank())
                    throw new IllegalArgumentException("specificDirName required.");

                Path target = sandBoxPath.resolve(specificDirName);
                if (!Files.exists(target)) return;

                deleteDirectoryRecursively(target);
                Files.createDirectories(target);
            }
        }

        log("Flush complete!");
    }

    // ---------------- LOAD BACKUP ----------------

    public void loadBackup(String identifier) throws IOException {
        Path backupDir = getBackupDir(identifier);

        if (!Files.exists(backupDir))
            throw new IOException("Backup directory does not exist: " + backupDir);

        if (Files.exists(sandBoxPath))
            deleteDirectoryRecursively(sandBoxPath);

        copyDirectoryRecursively(backupDir, sandBoxPath);
        log("Backup restored to sandbox!");
    }

    // ---------------- UTILS ----------------

    private Path getBackupDir(String identifier) {
        return cacheDir.resolve(sandBoxPath.getFileName() + "_" + identifier + "_backup");
    }

    private void copyDirectoryRecursively(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> {
            try {
                Path target = dest.resolve(src.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy: " + source, e);
            }
        });
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;

        // Protect inventory root and cache directory
        if (path.toRealPath().equals(inventory.getBasePath().toRealPath())) return;
        if (path.toRealPath().startsWith(cacheDir.toRealPath())) return;

        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete: " + p, e);
                    }
                });
    }

    private boolean waitForYes() {
        System.out.print(">> ");
        String input = IO.readln().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }

    public boolean confirmLoadBackup(String identifier) throws IOException {
        Path backupDir = getBackupDir(identifier);
        if (!Files.exists(backupDir)) return false;

        IO.print("Your journey was left unfinished. Resume your progress? (yes/no): ");
        if (!waitForYes()) return false;

        loadBackup(identifier);
        return true;
    }
}
