package com.terminaldungeon.gameplay;

public interface CommandListener {
    /**
     * Called before command execution.
     *
     * @param command   The command name (e.g., "rm", "cd")
     * @param args      Arguments passed to the command
     * @param currentDir The directory where the command will execute
     */
    void beforeExecute(String command, String[] args, java.nio.file.Path currentDir);

    /**
     * Called after command execution.
     *
     * @param command   The command name
     * @param args      Arguments passed to the command
     * @param result    The CommandResult including success, output, execution time, FS changes, renamed/modified files
     */
    void afterExecute(String command, String[] args, CommandResult result);
}
