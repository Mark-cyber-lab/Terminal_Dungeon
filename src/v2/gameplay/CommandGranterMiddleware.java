package v2.gameplay;

import v2.player.PlayerStats;

import java.util.*;

public class CommandGranterMiddleware implements CommandMiddleware {

    private final PlayerStats playerStats;

    public CommandGranterMiddleware(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    // ---------------------------
    // GRANTING
    // ---------------------------

    public void grant(String command) {
        playerStats.updateGrantedCommands(commands -> {
            commands.add(command);
            return commands;
        });
    }

    public void revoke(String command) {
        playerStats.updateGrantedCommands(commands -> {
            commands.remove(command);
            return commands;
        });
    }

    public boolean isGranted(String command) {
        return playerStats.getGrantedCommands().contains(command);
    }

    public Set<String> getGranted() {
        return Set.copyOf(playerStats.getGrantedCommands());
    }

    @Override
    public boolean before(String command, String[] args, CommandContext ctx) {
        if (!playerStats.getGrantedCommands().contains(command)) {
            IO.println("Command '" + command + "' is not yet granted.");
            return false;
        }
        return true;
    }

    @Override
    public void after(String command, String[] args, CommandContext ctx, CommandResult result) { }
}
