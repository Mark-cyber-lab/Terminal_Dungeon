package utilities;

import core.listeners.Blocker;
import core.listeners.CommandListener;
import core.listeners.ListenerRegistry;

import java.nio.file.Path;
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

    public void addBlocker(Blocker blocker) {
        registry.registerBlocker(blocker);
        // Optionally, auto-register as listener if it reacts to commands
        if (blocker instanceof CommandListener) {
            addCommandListener((CommandListener) blocker);
        }
    }

    public void removeBlocker(Blocker blocker) {
        registry.unregisterBlocker(blocker);
    }

    @Override
    public CommandResult executeCommand(String... inputParts) {
        CommandResult result = super.executeCommand(inputParts);

        // Notify all listeners
        registry.notifyListeners(result);

        // Check for active blockers in current folder
        Path currentDir = getCurrentDirectory();
        List<Blocker> activeBlockers = registry.activeBlockersInFolder(currentDir);
        if (!activeBlockers.isEmpty()) {
            System.out.println("[Game Rule] Action blocked by:");
            for (Blocker b : activeBlockers) {
                System.out.println(" - " + b.getName());
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
