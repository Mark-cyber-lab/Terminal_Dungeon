package com.terminaldungeon.engine;

import com.terminaldungeon.utilities.CommandValidator;
import com.terminaldungeon.utilities.DirGenerator;
import com.terminaldungeon.utilities.Loggable;
import com.terminaldungeon.gameplay.CommandGranterMiddleware;
import com.terminaldungeon.gameplay.CommandListener;
import com.terminaldungeon.gameplay.CommandResult;
import com.terminaldungeon.gameplay.DungeonExecutor;
import com.terminaldungeon.player.PlayerStats;
import com.terminaldungeon.storage.Inventory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Sandbox implements Loggable {
    private final Path sandBoxPath;
    private final CommandValidator validator;
    private final DirGenerator dirGenerator;
    private final DungeonExecutor executor;
    private final CommandGranterMiddleware granter;
    private final Inventory inventory;
    private final SandboxBackupManager backupManager;


    public Sandbox(String sandboxPath, String inventoryPath, PlayerStats playerStats) {
        this.sandBoxPath = Path.of(sandboxPath);
        Path inventoryPath1 = Path.of(inventoryPath);
        this.executor = new DungeonExecutor(this.sandBoxPath, inventoryPath1);
        this.granter = new CommandGranterMiddleware(playerStats);
        this.inventory = new Inventory("inventory", "inventory", inventoryPath);
        this.backupManager = new SandboxBackupManager(this.sandBoxPath, this.inventory);
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

    public SandboxBackupManager getBackupManager() {
        return backupManager;
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
}
