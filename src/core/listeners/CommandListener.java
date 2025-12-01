package core.listeners;
import utilities.CommandResult;

@FunctionalInterface
public interface CommandListener {
    void onCommand(CommandResult result);
}
