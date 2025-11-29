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

    public LinuxCommandExecutor(String startDir) {
        this.currentDirectory = Paths.get(startDir).toAbsolutePath();
        DebugLogger.log("COMMAND_EXECUTOR","Executor starting in: " + currentDirectory);
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public List<String> getCommandHistory() {
        return Collections.unmodifiableList(commandHistory);
    }

    /**
     * Execute a command just like a real shell (cd, pwd, ls, rm, etc.)
     */
    public boolean executeCommand(String... inputParts) {

        // join for history (user typed it this way)
        String raw = String.join(" ", inputParts);
        commandHistory.add(raw);

        // Parse command
        if (inputParts.length == 0) return false;
        String cmd = inputParts[0].toLowerCase();

        // INTERNAL COMMANDS
        return switch (cmd) {
            case "cd" -> internalCd(inputParts);
            case "pwd" -> internalPwd();
            case "history" -> internalHistory();
            default ->
                // run system command in currentDirectory
                    externalCommand(inputParts);
        };
    }

    // ============================
    // INTERNAL COMMANDS (LOCAL)
    // ============================

    private boolean internalCd(String[] parts) {
        if (parts.length < 2) {
            System.out.println("cd: missing argument");
            return false;
        }

        String target = parts[1];

        if (target.equals("..")) {
            Path parent = currentDirectory.getParent();
            if (parent != null) currentDirectory = parent;
        } else {
            Path newPath = currentDirectory.resolve(target).normalize();
            if (!Files.isDirectory(newPath)) {
                System.out.println("cd: " + target + ": No such directory");
                return false;
            }
            currentDirectory = newPath;
        }

        System.out.println("Moved to: " + currentDirectory);
        return true;
    }

    private boolean internalPwd() {
        System.out.println(currentDirectory);
        return true;
    }

    private boolean internalHistory() {
        for (int i = 0; i < commandHistory.size(); i++) {
            System.out.println((i + 1) + "  " + commandHistory.get(i));
        }
        return true;
    }

    // ============================
    // EXTERNAL OS COMMAND EXECUTION
    // ============================

    private boolean externalCommand(String[] command) {
        Process process = null;

        try {
            String[] adjusted = adjustCommandForOS(command);

            ProcessBuilder builder = new ProcessBuilder(adjusted);
            builder.directory(currentDirectory.toFile()); // ← MEMORY WORKS HERE
            builder.redirectErrorStream(true);

            process = builder.start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) System.out.println(line);
            }

            int exit = process.waitFor();
            return exit == 0;

        } catch (IOException e) {
            logger.severe("IO Error executing command: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.warning("Command execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private String[] adjustCommandForOS(String[] command) {
        if (!OS.contains("win")) return command;  // Linux/macOS stays the same

        String cmd = command[0].toLowerCase();

        switch (cmd) {
            case "ls":
                return new String[]{"cmd.exe", "/c", "dir"};
            case "pwd":
                return new String[]{"cmd.exe", "/c", "cd"};
            case "rm":
                return new String[]{"cmd.exe", "/c", "del", "/q"};
            default:
                return new String[]{"cmd.exe", "/c", String.join(" ", command)};
        }
    }
}
