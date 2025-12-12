package gameplay;

import java.time.Instant;

public class CommandHistoryEntry {
    public final String command;
    public final String[] args;
    public final Instant timestamp;
    public final CommandResult result;

    public CommandHistoryEntry(String command, String[] args, CommandResult result) {
        this.command = command;
        this.args = args;
        this.timestamp = Instant.now();
        this.result = result;
    }
}
