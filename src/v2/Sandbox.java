package v2;

import utilities.CommandValidator;
import utilities.DirGenerator;
import utilities.Loggable;
import v2.storage.Inventory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Sandbox implements Loggable {
    private final Path sandBoxPath;
    private final CommandValidator validator;
    private final DirGenerator dirGenerator;
    private final DungeonExecutor executor;
    private final CommandGranterMiddleware granter;
    private final Inventory inventory;

    public Sandbox(String sandboxPath, String inventoryPath) {
        this.sandBoxPath = Path.of(sandboxPath);
        Path inventoryPath1 = Path.of(inventoryPath);
        this.executor = new DungeonExecutor(this.sandBoxPath, inventoryPath1);
        this.granter = new CommandGranterMiddleware();
        this.inventory = new Inventory("inventory", "inventory", inventoryPath);
        executor.useMiddleware(granter);
        executor.addListener(new CommandListener() {
            @Override
            public void beforeExecute(String command, String[] args, Path currentDir) {
            }

            @Override
            public void afterExecute(String command, String[] args, CommandResult result) {
                if (result.getOutput() != null) IO.println(result.getOutput());
            }
        });

        // Initialize validator with whitelist of safe commands
        Set<String> safeCommands = new HashSet<>();
        safeCommands.add("pwd");
        safeCommands.add("ls");
        safeCommands.add("tree");
        safeCommands.add("cat");
        safeCommands.add("rm");      // controlled deletion for enemies
        safeCommands.add("sudo");    // for special commands like sudo rm

        this.validator = new CommandValidator.Builder().withAllowedCommands(safeCommands).useWhitelist(true).build();

        this.dirGenerator = new DirGenerator();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public DungeonExecutor getExecutor() {
        return executor;
    }

    public Path getSandBoxPath() {
        return sandBoxPath;
    }

    public CommandGranterMiddleware getGranter() {
        return granter;
    }

    @FunctionalInterface
    public interface UpdateCallback {
        String onUpdate(String prevVal);
    }


    public DirGenerator.GenerationResult generateStructure(String configFilePath) {
        return dirGenerator.generateFromConfig(configFilePath, sandBoxPath.toString());
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
        File srcDir = new File(sandBoxPath.toString());
        if (!srcDir.exists()) {
            throw new IOException("Sandbox root does not exist: " + sandBoxPath.toString());
        }

        File backupDir = new File(sandBoxPath.toString() + "_backup");
        if (backupDir.exists()) {
            log("Backup folder already exists, overwriting...");
            deleteDirectoryRecursively(backupDir.toPath());
        }

        log("Creating backup at: " + backupDir.getAbsolutePath());
        copyDirectoryRecursively(srcDir.toPath(), backupDir.toPath());
        log("Backup completed successfully!");
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
        if(path.toAbsolutePath().normalize().equals(inventory.getBasePath().toAbsolutePath().normalize())) return;
        Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
            try {
                Files.delete(p);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete: " + p, e);
            }
        });
    }

    public void flush() throws IOException {
        File backupDir = new File(sandBoxPath + "_backup");
        if (!backupDir.exists()) return; // nothing to restore

        File rootDir = new File(sandBoxPath.toString());
        if (rootDir.exists()) deleteDirectoryRecursively(rootDir.toPath());
    }

    public void loadBackup() throws IOException {
        File backupDir = new File(sandBoxPath + "_backup");
        if (!backupDir.exists()) return; // nothing to restore

        File rootDir = new File(sandBoxPath.toString());
        if (rootDir.exists()) deleteDirectoryRecursively(rootDir.toPath());

        copyDirectoryRecursively(backupDir.toPath(), rootDir.toPath());
        log("Backup restored to: " + rootDir.getAbsolutePath());
    }
}
