package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class LinuxCommandExecutor {

    private static final Logger logger = Logger.getLogger(LinuxCommandExecutor.class.getName());
    private static final String OS = System.getProperty("os.name").toLowerCase();

    // === MEMORY / STATE ===
    private Path currentDirectory;
    private final List<String> commandHistory = new ArrayList<>();

    // New record to hold command execution results
    public record CommandExecutionResult(
            boolean success,
            String output,
            String error,
            int exitCode
    ) {
        public CommandExecutionResult {
            output = output != null ? output : "";
            error = error != null ? error : "";
        }

        // Helper method to get full result as string
        public String getFullResult() {
            if (success) {
                return output;
            } else {
                return error.isEmpty() ? "Command failed with exit code: " + exitCode : error;
            }
        }
    }

    public LinuxCommandExecutor(String startDir) {
        this.currentDirectory = Paths.get(startDir).toAbsolutePath();
        DebugLogger.log("COMMAND_EXECUTOR","Executor starting in: " + currentDirectory);
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Direct setter replaces current directory
     */
    public void setCurrentDirectory(String path) {
        Path newPath = Paths.get(path).toAbsolutePath();
        if (Files.exists(newPath) && Files.isDirectory(newPath)) {
            currentDirectory = newPath;
            DebugLogger.log("COMMAND_EXECUTOR", "Current directory updated to: " + currentDirectory);
        } else {
            DebugLogger.log("COMMAND_EXECUTOR", "Failed to update directory (does not exist): " + newPath);
        }
    }

    /**
     * MAIN METHOD: Execute command, display output on terminal, and return string output
     */
    public String executeCommand(String... inputParts) {
        CommandExecutionResult result = executeCommandWithResult(inputParts);

        // Display the output on terminal
        if (!result.output().isEmpty()) {
            System.out.println(result.output());
        }
        if (!result.error().isEmpty()) {
            System.out.println(result.error());
        }

        return result.getFullResult();
    }

    /**
     * Execute command and return detailed result (without displaying to terminal)
     */
    public CommandExecutionResult executeCommandWithResult(String... inputParts) {
        String raw = String.join(" ", inputParts);
        commandHistory.add(raw);

        if (inputParts.length == 0) {
            return new CommandExecutionResult(false, "", "No command provided", -1);
        }

        String cmd = inputParts[0].toLowerCase();

        return switch (cmd) {
            case "cd" -> internalCdWithResult(inputParts);
            case "pwd" -> internalPwdWithResult();
            case "history" -> internalHistoryWithResult();
            case "cat" -> internalCatWithResult(inputParts);
            case "ls" -> internalLsWithResult(inputParts);
            default -> externalCommandWithResult(inputParts);
        };
    }

    /**
     * Convenience method to execute a single command string
     */
    public String executeCommand(String command) {
        return executeCommand(command.split("\\s+"));
    }

    public CommandExecutionResult executeCommandWithResult(String command) {
        return executeCommandWithResult(command.split("\\s+"));
    }

    // ============================
    // INTERNAL COMMANDS WITH STRING OUTPUT
    // ============================

    private CommandExecutionResult internalLsWithResult(String[] parts) {
        try {
            Path targetDir = currentDirectory;
            if (parts.length > 1) {
                targetDir = currentDirectory.resolve(parts[1]).normalize();
            }

            if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
                return new CommandExecutionResult(false, "", "ls: cannot access '" + parts[1] + "': No such file or directory", -1);
            }

            StringBuilder output = new StringBuilder();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
                for (Path entry : stream) {
                    String fileName = entry.getFileName().toString();
                    if (Files.isDirectory(entry)) {
                        output.append(fileName).append("/\n");
                    } else {
                        output.append(fileName).append("\n");
                    }
                }
            }
            return new CommandExecutionResult(true, output.toString().trim(), "", 0);

        } catch (IOException e) {
            return new CommandExecutionResult(false, "", "ls: cannot open directory: " + e.getMessage(), -1);
        }
    }

    private CommandExecutionResult internalCatWithResult(String[] parts) {
        if (parts.length < 2) {
            return new CommandExecutionResult(false, "", "cat: missing file argument", -1);
        }

        StringBuilder output = new StringBuilder();
        boolean success = true;

        for (int i = 1; i < parts.length; i++) {
            Path filePath = currentDirectory.resolve(parts[i]).normalize();
            if (!Files.exists(filePath)) {
                output.append("cat: ").append(parts[i]).append(": No such file\n");
                success = false;
                continue;
            }

            if (Files.isDirectory(filePath)) {
                output.append("cat: ").append(parts[i]).append(": Is a directory\n");
                success = false;
                continue;
            }

            try {
                List<String> lines = Files.readAllLines(filePath);
                for (String line : lines) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                output.append("cat: ").append(parts[i]).append(": Error reading file\n");
                success = false;
            }
        }

        return new CommandExecutionResult(success, output.toString().trim(), "", success ? 0 : -1);
    }

    private CommandExecutionResult internalCdWithResult(String[] parts) {
        if (parts.length < 2) {
            return new CommandExecutionResult(false, "", "cd: missing argument", -1);
        }

        String target = parts[1];
        Path newPath = target.equals("..")
                ? currentDirectory.getParent()
                : currentDirectory.resolve(target).normalize();

        if (newPath == null || !Files.isDirectory(newPath)) {
            return new CommandExecutionResult(false, "", "cd: " + target + ": No such directory", -1);
        }

        currentDirectory = newPath;
        return new CommandExecutionResult(true, "Directory changed to: " + currentDirectory, "", 0);
    }

    private CommandExecutionResult internalPwdWithResult() {
        return new CommandExecutionResult(true, currentDirectory.toString(), "", 0);
    }

    private CommandExecutionResult internalHistoryWithResult() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < commandHistory.size(); i++) {
            output.append(i + 1).append("  ").append(commandHistory.get(i)).append("\n");
        }
        return new CommandExecutionResult(true, output.toString().trim(), "", 0);
    }

    // ============================
    // EXTERNAL OS COMMAND EXECUTION WITH STRING OUTPUT
    // ============================

    private CommandExecutionResult externalCommandWithResult(String[] command) {
        Process process = null;

        try {
            String[] adjusted = adjustCommandForOS(command);
            ProcessBuilder builder = new ProcessBuilder(adjusted);
            builder.directory(currentDirectory.toFile());
            builder.redirectErrorStream(true); // Combine stdout and stderr

            process = builder.start();

            StringBuilder output = new StringBuilder();

            // Read the combined output stream and display it in real-time
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    // Display each line in real-time as the command executes
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            boolean success = (exitCode == 0);

            DebugLogger.log("COMMAND_EXECUTOR",
                    "Command executed: " + String.join(" ", command) +
                            " | Exit code: " + exitCode + " | Success: " + success);

            return new CommandExecutionResult(success, output.toString().trim(), "", exitCode);

        } catch (IOException | InterruptedException e) {
            String errorMsg = "Command execution failed: " + e.getMessage();
            logger.severe(errorMsg);
            System.out.println(errorMsg); // Display error on terminal
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new CommandExecutionResult(false, "", errorMsg, -1);
        } finally {
            if (process != null) process.destroy();
        }
    }

    private String[] adjustCommandForOS(String[] command) {
        if (!OS.contains("win")) return command;

        String cmd = command[0].toLowerCase();
        return switch (cmd) {
            case "ls" -> new String[]{"cmd.exe", "/c", "dir"};
            case "pwd" -> new String[]{"cmd.exe", "/c", "cd"};
            case "rm" -> new String[]{"cmd.exe", "/c", "del", "/q"};
            default -> new String[]{"cmd.exe", "/c", String.join(" ", command)};
        };
    }

    // ============================
    // BACKWARD COMPATIBILITY METHODS
    // (Only if you need to maintain old code)
    // ============================

    /**
     * @deprecated Use executeCommand() instead which returns string output AND displays it
     */
    @Deprecated
    public boolean executeCommandLegacy(String... inputParts) {
        CommandExecutionResult result = executeCommandWithResult(inputParts);
        // Print output to maintain old behavior
        if (!result.output().isEmpty()) {
            System.out.println(result.output());
        }
        if (!result.error().isEmpty()) {
            System.out.println(result.error());
        }
        return result.success();
    }

    public List<String> getCommandHistory() {
        return Collections.unmodifiableList(commandHistory);
    }
}