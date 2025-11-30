package engine;

import utilities.CommandValidator;
import utilities.DebugLogger;
import utilities.LinuxCommandExecutor;
import utilities.DirGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Sandbox {

    private String rootPath;
    private String initialRootPath;
    private final CommandValidator validator;
    private final DirGenerator dirGenerator;
    private final LinuxCommandExecutor executor;

    public Sandbox(String rootPath) {
        this.rootPath = rootPath;
        this.initialRootPath = rootPath;

        this.executor = new LinuxCommandExecutor(rootPath);

        // Initialize validator with whitelist of safe commands
        Set<String> safeCommands = new HashSet<>();
        safeCommands.add("pwd");
        safeCommands.add("ls");
        safeCommands.add("tree");
        safeCommands.add("cat");
        safeCommands.add("rm");      // controlled deletion for enemies
        safeCommands.add("sudo");    // for special commands like sudo rm

        this.validator = new CommandValidator.Builder()
                .withAllowedCommands(safeCommands)
                .useWhitelist(true)
                .build();

        this.dirGenerator = new DirGenerator();

        // Ensure root sandbox exists
        File rootDir = new File(rootPath);

        if (!rootDir.exists()) {
            if (rootDir.mkdirs()) {
                DebugLogger.log("SANDBOX","Sandbox folder created at: " + rootDir.getAbsolutePath());
            } else {
                DebugLogger.log("SANDBOX","Failed to create sandbox folder at: " + rootDir.getAbsolutePath());
            }
        } else {
            DebugLogger.log("SANDBOX","Sandbox folder already exists at: " + rootDir.getAbsolutePath());
        }

    }

    public LinuxCommandExecutor getExecutor() {
        return executor;
    }

    public void updateRootDir(String path) {
        rootPath = path;
        executor.setCurrentDirectory(rootPath);
    }

    @FunctionalInterface
    public interface UpdateCallback {
        String onUpdate(String prevVal);
    }

    public void updateRootDir(UpdateCallback callback) {
        String path = new String(rootPath);
        updateRootDir(callback.onUpdate(path));
    }

    public DirGenerator.GenerationResult generateStructure(String configFilePath) {
        return dirGenerator.generateFromConfig(configFilePath, rootPath);
    }

    public String getRootPath() {
        return rootPath;
    }

    public CommandValidator getValidator() {
        return validator;
    }

    public DirGenerator getDirGenerator() {
        return dirGenerator;
    }

    /**
     * Creates a backup of the current sandbox root directory.
     * The backup folder will be named rootPath + "_backup".
     *
     * @throws IOException if any file operations fail
     */
    public void backup() throws IOException {
        File srcDir = new File(initialRootPath);
        if (!srcDir.exists()) {
            throw new IOException("Sandbox root does not exist: " + initialRootPath);
        }

        File backupDir = new File(initialRootPath + "_backup");
        if (backupDir.exists()) {
            DebugLogger.log("Backup folder already exists, overwriting...");
            deleteDirectoryRecursively(backupDir.toPath());
        }

        DebugLogger.log("Creating backup at: " + backupDir.getAbsolutePath());
        copyDirectoryRecursively(srcDir.toPath(), backupDir.toPath());
        DebugLogger.log("Backup completed successfully!");
    }

    private void copyDirectoryRecursively(Path src, Path dest) throws IOException {
        Files.walk(src)
                .forEach(source -> {
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

    public void flush () throws IOException {
        File backupDir = new File(initialRootPath + "_backup");
        if (!backupDir.exists()) return; // nothing to restore

        File rootDir = new File(initialRootPath);
        if (rootDir.exists()) deleteDirectoryRecursively(rootDir.toPath());
    }

    public void loadBackup() throws IOException {
        File backupDir = new File(initialRootPath + "_backup");
        if (!backupDir.exists()) return; // nothing to restore

        File rootDir = new File(initialRootPath);
        if (rootDir.exists()) deleteDirectoryRecursively(rootDir.toPath());

        copyDirectoryRecursively(backupDir.toPath(), rootDir.toPath());
        DebugLogger.log("Backup restored to: " + rootDir.getAbsolutePath());
    }
}
