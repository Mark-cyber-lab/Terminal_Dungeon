package gameplay;

public interface CommandMiddleware {
    /**
     * Can modify input args or context
     * Return false to cancel command execution (sandbox / validation)
     */
    boolean before(String command, String[] args, CommandContext context);

    /**
     * After execution hook
     */
    void after(String command, String[] args, CommandContext context, CommandResult result);
}
