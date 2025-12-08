package v2;

import utilities.Loggable;
import v2.storage.Inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public class SandboxBackupManager implements Loggable {

    private final Path sandBoxPath;
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
    }

    // ---------------- BACKUP ----------------

    public boolean backup(BackupMode mode) throws IOException {
        return backup(mode, null);
    }

    public boolean backup(BackupMode mode, Path specificDir) throws IOException {
        try {
            File srcDir = sandBoxPath.toFile();
            if (!srcDir.exists()) {
                throw new IOException("Sandbox root does not exist: " + sandBoxPath);
            }

            File backupDir = new File(sandBoxPath + "_backup");

            if (backupDir.exists()) {
                log("Backup folder exists. Overwriting...");
                deleteDirectoryRecursively(backupDir.toPath());
            }

            log("Creating backup at: " + backupDir.getAbsolutePath());
            Files.createDirectories(backupDir.toPath());

            switch (mode) {

                case BACKUP_ALL -> {
                    log("Backing up entire sandbox...");
                    copyDirectoryRecursively(srcDir.toPath(), backupDir.toPath());
                }

                case BACKUP_ONLY_INVENTORY -> {
                    Path source = sandBoxPath.resolve("inventory");
                    if (!Files.exists(source))
                        throw new IOException("Inventory folder missing: " + source);

                    Path target = backupDir.toPath().resolve("inventory");
                    log("Backing up inventory folder...");
                    Files.createDirectories(target);
                    copyDirectoryRecursively(source, target);
                }

                case BACKUP_SPECIFIC_DIR -> {
                    if (specificDir == null)
                        throw new IOException("BACKUP_SPECIFIC_DIR requires a path");

                    Path src = sandBoxPath.resolve(specificDir).normalize();
                    if (!Files.exists(src))
                        throw new IOException("Directory does not exist: " + src);

                    Path dest = backupDir.toPath().resolve(src.getFileName().toString());
                    log("Backing up specific directory: " + src);

                    Files.createDirectories(dest);
                    copyDirectoryRecursively(src, dest);
                }
            }

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
                        if (p.getFileName().toString().equals("inventory")) {
                            log("Keeping inventory directory.");
                            continue;
                        }
                        deleteDirectoryRecursively(p);
                    }
                }
            }

            case SPECIFIC_DIR -> {
                if (specificDirName == null || specificDirName.isBlank())
                    throw new IllegalArgumentException("specificDirName required.");

                Path target = sandBoxPath.resolve(specificDirName);

                if (!Files.exists(target)) {
                    log("Directory does not exist: " + target);
                    return;
                }

                log("Flushing directory: " + target);
                deleteDirectoryRecursively(target);
                Files.createDirectories(target);
            }
        }

        log("Flush complete!");
    }

    // ---------------- LOAD BACKUP ----------------

    public void loadBackup() throws IOException {
        File backupDir = getBackupDir();
        loadBackup(backupDir);
    }

    public void loadBackup(File backupDir) throws IOException {
        if (!backupDir.exists())
            throw new IOException("Backup directory does not exist: " + backupDir);

        File rootDir = sandBoxPath.toFile();

        if (rootDir.exists())
            deleteDirectoryRecursively(rootDir.toPath());

        copyDirectoryRecursively(backupDir.toPath(), rootDir.toPath());
        log("Backup restored to sandbox!");
    }

    // ---------------- UTILS ----------------

    private File getBackupDir() {
        return new File(sandBoxPath + "_backup");
    }

    private void copyDirectoryRecursively(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> {
            try {
                Path target = dest.resolve(src.relativize(source));
                if (Files.isDirectory(source)) {
                    if (!Files.exists(target)) Files.createDirectory(target);
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

        // Never delete the real inventory root
        if (path.toRealPath().equals(inventory.getBasePath().toRealPath()))
            return;

        Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
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

    public boolean confirmLoadBackup() throws IOException {
        File backupDir = getBackupDir();

        if (!backupDir.exists()) return false;

        IO.print("Your journey was left unfinished. Resume your progress? (yes/no): ");
        if (!waitForYes()) return false;

        loadBackup(backupDir);
        return true;
    }
}
