package com.terminaldungeon.gameplay;

public class CommandResult {

    private final boolean success;
    private final String output;
    private final CommandContext context;

    /**
     * @param success Whether the command executed successfully
     * @param output  The command output (stdout/stderr combined)
     * @param context The full CommandContext including FS changes, execution time, etc.
     */
    public CommandResult(boolean success, String output, CommandContext context) {
        this.success = success;
        this.output = output;
        this.context = context;
    }

    /** Returns true if the command succeeded */
    public boolean isSuccess() {
        return success;
    }

    /** Returns the stdout/stderr output */
    public String getOutput() {
        return output;
    }

    /** Returns the full context (FS changes, execution time, renamed/modified files) */
    public CommandContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Success: ").append(success).append("\n");
        sb.append("Output: ").append(output).append("\n");
        sb.append("Start Dir: ").append(context.startDir).append("\n");
        sb.append("End Dir: ").append(context.endDir).append("\n");
        sb.append("Created: ").append(context.created).append("\n");
        sb.append("Deleted: ").append(context.deleted).append("\n");
        sb.append("Modified: ").append(context.modified).append("\n");
        sb.append("Renamed: ").append(context.renamed).append("\n");
        sb.append("Execution Time: ").append(context.executionTimeMs).append("ms\n");
        return sb.toString();
    }
}
