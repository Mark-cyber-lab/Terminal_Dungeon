package core.listeners;

import utilities.CommandResult;
import utilities.Loggable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ListenerRegistry implements Loggable {
    private final List<CommandListener> listeners = new ArrayList<>();
    private final List<Blocker> blockers = new ArrayList<>();

    public void registerListener(CommandListener listener) {
        listeners.add(listener);
        log("Registered listener: " + listener);
    }

    public void unregisterListener(CommandListener listener) {
        listeners.remove(listener);
        log("Unregistered listener: " + listener);
    }

    public void registerBlocker(Blocker blocker) {
        blockers.add(blocker);
        log("Registered blocker: " + blocker);
    }

    public void unregisterBlocker(Blocker blocker) {
        blockers.remove(blocker);
        log("Unregistered blocker: " + blocker);
    }

    public void notifyListeners(CommandResult result) {
        log("Notifying listeners about command: " + result.command());
        for (CommandListener listener : new ArrayList<>(listeners)) {
            listener.onCommand(result);
        }
    }

    public List<Blocker> activeBlockersInFolder(Path folder) {
        List<Blocker> active = new ArrayList<>();
        for (Blocker b : blockers) {
            log("Active blockers: " + b.getFilePath());
            if (!b.isCleared() && b.blocks(folder)) {
                active.add(b);
            }
        }
        log("Found " + active.size() + " active blockers in folder: " + folder);
        return active;
    }

    public List<Blocker> activeBlockersInFolder(Path folder, Path exceptPath) {
        Path absFolder = folder.toAbsolutePath().normalize();
        Path absExcept = exceptPath != null ? exceptPath.toAbsolutePath().normalize() : null;

        List<Blocker> active = new ArrayList<>();

        for (Blocker b : blockers) {

            Path blockerPath = b.getFilePath().toAbsolutePath().normalize();

            // Debug
//            IO.println("Except: " + absExcept);
//            IO.println("Checking blocker: " + blockerPath);

            if (blockerPath.equals(absExcept)) {
                log("Skipped (exceptPath matched): " + blockerPath);
                continue;
            }

            if (!b.isCleared() && b.blocks(absFolder)) {
                active.add(b);
            }
        }

        log("Found " + active.size() + " active blockers in folder: " + absFolder);
        return active;
    }

}
