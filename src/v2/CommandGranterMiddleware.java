package v2;

import java.time.LocalDateTime;
import java.util.*;

public class CommandGranterMiddleware implements CommandMiddleware {

    private final Set<String> granted = new HashSet<>();
    private final Map<Integer, List<String>> stageGrants = new HashMap<>();

    // ---------------------------
    // GRANTING
    // ---------------------------

    public void grant(String command) {
        granted.add(command);
    }

    public void revoke(String command) {
        granted.remove(command);
    }

    public boolean isGranted(String command) {
        return granted.contains(command);
    }

    public Set<String> getGranted() {
        return Set.copyOf(granted);
    }

    @Override
    public boolean before(String command, String[] args, CommandContext ctx) {
        if (!granted.contains(command)) {
            IO.println("Command '" + command + "' is not yet granted.");
            return false;
        }
        return true;
    }

    @Override
    public void after(String command, String[] args, CommandContext ctx, CommandResult result) { }
}
