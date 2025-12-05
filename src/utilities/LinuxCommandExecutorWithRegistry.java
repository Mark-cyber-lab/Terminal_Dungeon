package utilities;

import core.listeners.Blocker;
import core.listeners.CommandListener;
import core.listeners.ListenerRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LinuxCommandExecutorWithRegistry extends LinuxCommandExecutor {

    private final ListenerRegistry registry = new ListenerRegistry();

    public LinuxCommandExecutorWithRegistry(String startDir) {
        super(startDir);
    }

    public void addCommandListener(CommandListener listener) {
        registry.registerListener(listener);
    }

    public void removeCommandListener(CommandListener listener) {
        registry.unregisterListener(listener);
    }

    public LinuxCommandExecutorWithRegistry addBlocker(Blocker blocker) {
        registry.registerBlocker(blocker);
        // Optionally, auto-register as listener if it reacts to commands
        if (blocker instanceof CommandListener) {
            addCommandListener((CommandListener) blocker);
        }

        return this;
    }

    public LinuxCommandExecutorWithRegistry addBlocker(Blocker[] blockers) {
        for (Blocker blocker : blockers) {
            addBlocker(blocker);
        }

        return this;
    }

    public LinuxCommandExecutorWithRegistry addBlocker(List<Blocker> blockers) {
        for (Blocker blocker : blockers) {
            addBlocker(blocker);
        }

        return this;
    }


    public void removeBlocker(Blocker blocker) {
        registry.unregisterBlocker(blocker);
    }

    public void removeBlocker(Blocker[] blockers) {
        for (Blocker blocker : blockers) {
            removeBlocker(blocker);
        }
    }

    public void removeBlocker(List<Blocker> blockers) {
        for (Blocker blocker : blockers) {
            removeBlocker(blocker);
        }
    }

    @Override
    public CommandResult executeCommand(String... inputParts) {
        CommandResult result = super.executeCommand(inputParts);
        Path exceptPath = result.getFileFullPath();

        // Notify all listeners
        registry.notifyListeners(result);

        Path currentDir = getCurrentDirectory();
        List<Path> affectedPaths;
        if (result.affectedPaths() != null && !result.affectedPaths().isEmpty()) {
            affectedPaths = result.affectedPaths();
        } else {
            affectedPaths = new ArrayList<>();
            Path fullPath = result.getFileFullPath();
            if (fullPath != null) {
                affectedPaths.add(fullPath);
            }
        }

        for (Path p : affectedPaths) {
            List<Blocker> activeBlockers = registry.activeBlockersInFolder(currentDir, p);
            if (!activeBlockers.isEmpty()) {
                log("[Game Rule] Action blocked on: " + p);
                for (Blocker b : activeBlockers) {
                    log("                - " + b.getName());
                }
            }
        }
        return result;
    }

    public List<Blocker> blockersInCurrentFolder() {
        return registry.activeBlockersInFolder(getCurrentDirectory());
    }

    public void clearBlocker(String name) {
        for (Blocker b : registry.activeBlockersInFolder(getCurrentDirectory())) {
            if (b.getName().equalsIgnoreCase(name)) {
                b.clear();
                System.out.println("[Game] Blocker cleared: " + name);
            }
        }
    }
}
