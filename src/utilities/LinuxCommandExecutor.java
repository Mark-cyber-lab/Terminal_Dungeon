package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A sandbox-aware command executor that implements internal versions of common shell commands
 * (cd, pwd, ls, tree, cat, history, rm, touch, mkdir) and forwards unknown commands to the OS.
 * <p>
 * All commands return a CommandResult rather than printing directly — this keeps logic testable
 * and allows the game engine to decide what to display.
 */
public class LinuxCommandExecutor implements Loggable {

    private static final Logger logger = Logger.getLogger(LinuxCommandExecutor.class.getName());
    private static final String OS = System.getProperty("os.name").toLowerCase();

    // === MEMORY / STATE ===
    private Path currentDirectory;
    private final Path rootDirectory; // sandbox root; cannot escape above this
    private final List<String> commandHistory = new ArrayList<>();

    public LinuxCommandExecutor(String startDir) {
        Path start = Paths.get(startDir).toAbsolutePath().normalize();
        if (!Files.exists(start) || !Files.isDirectory(start)) {
            throw new IllegalArgumentException("startDir must exist and be a directory: " + startDir);
        }
        this.rootDirectory = start;
        this.currentDirectory = start;
        log( "Executor starting in: " + currentDirectory);
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }

    public String getCurrentDirectoryString() {
        return currentDirectory.toString();
    }

    /**
     * Direct setter for currentDirectory — still sandbox validated.
     * Returns CommandResult so caller knows if move succeeded.
     */
    public CommandResult setCurrentDirectory(Path newDir) {
        if (newDir == null) {
            return new CommandResult("setCd", false, "Null directory", currentDirectory.toString(), null, 1);
        }

        Path normalized = newDir.toAbsolutePath().normalize();

        if (!normalized.startsWith(rootDirectory)) {
            return new CommandResult("setCd", false,
                    "Access denied (outside sandbox): " + normalized,
                    currentDirectory.toString(), normalized.toString(), 1);
        }

        if (!Files.exists(normalized) || !Files.isDirectory(normalized)) {
            return new CommandResult("setCd", false,
                    "No such directory: " + normalized,
                    currentDirectory.toString(), normalized.toString(), 1);
        }

        this.currentDirectory = normalized;

        log( "Sandbox update root at: " + currentDirectory);
        return new CommandResult("setCd", true,
                "Directory set to: " + currentDirectory,
                currentDirectory.toString(), null, 0);
    }

    /**
     * Overload: set using string path (relative to currentDirectory).
     */
    public CommandResult setCurrentDirectory(String pathStr) {
        Path candidate = currentDirectory.resolve(pathStr).normalize();
        IO.println("curr dir" + currentDirectory);
        return setCurrentDirectory(candidate);
    }

    public List<String> getCommandHistory() {
        return Collections.unmodifiableList(commandHistory);
    }

    /**
     * Functional interface used to transform the current directory.
     */
    @FunctionalInterface
    public interface UpdateCallback {
        String onUpdate(String prevVal);
    }

    /**
     * Update current directory using a callback that returns a new path string.
     * This applies sandbox checks and returns a CommandResult for inspection.
     */
    public CommandResult updateCurrentDirectory(UpdateCallback callback) {
        String prevPath = currentDirectory.toString();
        String newPathStr;
        try {
            newPathStr = callback.onUpdate(prevPath);
            if (newPathStr == null) {
                return new CommandResult("updateCd", false, "Callback returned null path", currentDirectory.toString(), null, 1);
            }
            Path candidate = Paths.get(newPathStr).toAbsolutePath().normalize();
            if (!candidate.startsWith(rootDirectory)) {
                return new CommandResult("updateCd", false, "Access denied: cannot escape sandbox root", currentDirectory.toString(), candidate.toString(), 1);
            }
            if (!Files.exists(candidate) || !Files.isDirectory(candidate)) {
                return new CommandResult("updateCd", false, "No such directory: " + candidate, currentDirectory.toString(), candidate.toString(), 1);
            }
            currentDirectory = candidate;
            return new CommandResult("updateCd", true, "Moved to: " + currentDirectory, currentDirectory.toString(), null, 0);
        } catch (Exception e) {
            logger.warning("Exception in updateCurrentDirectory callback: " + e.getMessage());
            return new CommandResult("updateCd", false, "Error: " + e.getMessage(), currentDirectory.toString(), null, 1);
        }
    }

    /**
     * Execute a command (internal or external). Returns a CommandResult describing outcome.
     * Example use: executeCommand("cat", "file.txt")
     */
    public CommandResult executeCommand(String... inputParts) {
        String raw = String.join(" ", inputParts).trim();
        if (!raw.isEmpty()) commandHistory.add(raw);

        if (inputParts.length == 0) {
            return new CommandResult("", false, "No command provided", currentDirectory.toString(), null, 1);
        }

        String cmd = inputParts[0].toLowerCase(Locale.ROOT);

        // --- Check for sudo ---
        if (cmd.equals("sudo")) {
            if (inputParts.length < 2) {
                return new CommandResult("sudo", false, "sudo: missing command", currentDirectory.toString(), null, 1);
            }
            // Pass everything after 'sudo' to internalSudo for privilege check
            String[] sudoCommand = Arrays.copyOfRange(inputParts, 1, inputParts.length);
            boolean allowed = internalSudo(sudoCommand);
            if (!allowed) {
                String msg = "sudo: privilege check failed for command: " + String.join(" ", sudoCommand);
                IO.println(msg);
                return new CommandResult("sudo", false, msg, currentDirectory.toString(), String.join(" ", sudoCommand), 1);
            }

            // Remove 'sudo' and continue to normal execution below
            inputParts = sudoCommand;
            cmd = inputParts[0].toLowerCase(Locale.ROOT);
        }

        // Normal command handling
        return switch (cmd) {
            case "cd" -> internalCd(inputParts);
            case "pwd" -> internalPwd();
            case "history" -> internalHistory();
            case "cat" -> internalCat(inputParts);
            case "ls" -> internalLs(inputParts);
            case "tree" -> internalTree(inputParts);
            case "rm" -> internalRm(inputParts);
            case "touch" -> internalTouch(inputParts);
            case "mkdir" -> internalMkdir(inputParts);
            default -> externalCommand(inputParts);
        };
    }

    /**
     * An "interactive" helper that forces the user to type an expected command. Uses executeCommand
     * and returns the CommandResult of the executed command.
     */
    public CommandResult executeStrict(String expectedCommand) {
        while (true) {
            System.out.print(">> ");
            String input = IO.readln().trim();
            if(input.equals("done")) {
                return new CommandResult("", true, "Done", currentDirectory.toString(), null, 1);
            }

            if (input.equals(expectedCommand)) {
                String[] parts = input.split("\\s+");
                return executeCommand(parts);
            } else {
                // Inform the caller – we return a non-successful result and loop continues
                System.out.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                System.out.println("Try again with: **" + expectedCommand + "**");
            }
        }
    }

    // -----------------------
    // INTERNAL COMMANDS
    // -----------------------

    private CommandResult internalPwd() {
        String out = currentDirectory.toString();
        IO.println(out);
        return new CommandResult("pwd", true, out, currentDirectory.toString(), null, 0);
    }

    private CommandResult internalHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandHistory.size(); i++) {
            sb.append(i + 1).append("  ").append(commandHistory.get(i)).append("\n");
        }
        return new CommandResult("history", true, sb.toString().trim(), currentDirectory.toString(), null, 0);
    }

    private CommandResult internalCd(String[] parts) {
        if (parts.length < 2) {
            IO.println("cd: missing argument"); // simulated stderr
            return new CommandResult("cd", false, "cd: missing argument", currentDirectory.toString(), null, 1);
        }

        String target = parts[1];
        Path candidate;

        if (target.equals("~")) {
            candidate = rootDirectory;
        } else if (target.equals("..")) {
            candidate = currentDirectory.getParent();
            if (candidate == null) candidate = currentDirectory;
        } else {
            candidate = currentDirectory.resolve(target).normalize();
        }

        if (candidate == null) {
            IO.println("cd: invalid path " + currentDirectory.toString()); // simulated stderr
            return new CommandResult("cd", false, "cd: invalid path", currentDirectory.toString(), target, 1);
        }

        // Sandbox: do not allow escape above rootDirectory
        if (!candidate.startsWith(rootDirectory)) {
            IO.println("cd: permission denied - cannot escape sandbox"); // simulated stderr
            return new CommandResult("cd", false, "cd: permission denied - cannot escape sandbox", currentDirectory.toString(), candidate.toString(), 1);
        }

        if (!Files.exists(candidate) || !Files.isDirectory(candidate)) {
            IO.println("cd: " + target + ": No such directory"); // simulated stderr
            return new CommandResult("cd", false, "cd: " + target + ": No such directory", currentDirectory.toString(), candidate.toString(), 1);
        }

        currentDirectory = candidate;

        // Success can remain just in the CommandResult
        return new CommandResult("cd", true, "Moved to: " + currentDirectory, currentDirectory.toString(), target, 0);
    }

    private CommandResult internalCat(String[] parts) {
        if (parts.length < 2) {
            IO.println("cat: missing file argument"); // simulated stderr
            return new CommandResult("cat", false, "cat: missing file argument", currentDirectory.toString(), null, 1);
        }

        StringBuilder output = new StringBuilder();
        boolean success = true;
        String subject = null;

        for (int i = 1; i < parts.length; i++) {
            String arg = parts[i];
            subject = arg;
            Path filePath = currentDirectory.resolve(arg).normalize();

            // Sandbox: ensure filePath is inside root
            if (!filePath.startsWith(rootDirectory)) {
                String msg = "cat: " + arg + ": Permission denied";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                success = false;
                continue;
            }

            if (!Files.exists(filePath)) {
                String msg = "cat: " + arg + ": No such file";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                success = false;
                continue;
            }

            if (Files.isDirectory(filePath)) {
                String msg = "cat: " + arg + ": Is a directory";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                success = false;
                continue;
            }

            try {
                List<String> lines = Files.readAllLines(filePath);
                for (String line : lines) {
                    IO.println(line); // normal stdout
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                String msg = "cat: " + arg + ": Error reading file";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                logger.warning("Error reading file " + filePath + " : " + e.getMessage());
                success = false;
            }
        }

        return new CommandResult("cat", success, output.toString().trim(), currentDirectory.toString(), subject, success ? 0 : 1);
    }

    private CommandResult internalLs(String[] parts) {
        Path target = currentDirectory;
        boolean showAll;      // -a
        boolean longFormat;    // -l

        // Parse options
        int argIndex = 1;
        if (parts.length >= 2 && parts[1].startsWith("-")) {
            String opts = parts[1];
            showAll = opts.contains("a");
            longFormat = opts.contains("l");
            argIndex = 2; // next argument may be the path
        } else {
            longFormat = false;
            showAll = false;
        }

        // Optional argument: ls [options] [path]
        if (parts.length > argIndex) {
            target = currentDirectory.resolve(parts[argIndex]).normalize();
        }

        // Sandbox check
        if (!target.startsWith(rootDirectory)) {
            String msg = "ls: permission denied";
            IO.println(msg);
            return new CommandResult("ls", false, msg, currentDirectory.toString(), target.toString(), 1);
        }

        // Existence check
        if (!Files.exists(target)) {
            String msg = "ls: no such file or directory: " + target.getFileName();
            IO.println(msg);
            return new CommandResult("ls", false, msg, currentDirectory.toString(), target.toString(), 1);
        }

        try {
            List<String> names;
            if (Files.isDirectory(target)) {
                names = Files.list(target)
                        .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                        .filter(p -> showAll || !p.getFileName().toString().startsWith("."))
                        .map(p -> {
                            if (longFormat) {
                                try {
                                    String type = Files.isDirectory(p) ? "d" : "-";
                                    long size = Files.size(p);
                                    return String.format("%s %10d %s", type, size, p.getFileName());
                                } catch (IOException e) {
                                    return p.getFileName().toString();
                                }
                            } else {
                                return Files.isDirectory(p) ? p.getFileName().toString() + "/" : p.getFileName().toString();
                            }
                        })
                        .collect(Collectors.toList());

                for (String line : names) {
                    IO.println(line);
                }

                String output = String.join("\n", names);
                return new CommandResult("ls", true, output, currentDirectory.toString(), target.toString(), 0);
            }

            // It's a file
            String fileName = target.getFileName().toString();
            if (longFormat) {
                long size = Files.size(target);
                String type = "-";
                fileName = String.format("%s %10d %s", type, size, fileName);
            }
            IO.println(fileName);
            return new CommandResult("ls", true, fileName, currentDirectory.toString(), target.toString(), 0);

        } catch (IOException e) {
            String msg = "ls: error listing directory";
            IO.println(msg);
            logger.warning("ls error: " + e.getMessage());
            return new CommandResult("ls", false, msg, currentDirectory.toString(), target.toString(), 1);
        }
    }

    private CommandResult internalTree(String[] parts) {
        Path target = currentDirectory;
        boolean showAll = false; // corresponds to -a option

        // Handle optional argument: `tree [options] [foldername]`
        int argIndex = 1;
        if (parts.length >= 2 && parts[1].startsWith("-")) {
            String opts = parts[1];
            showAll = opts.contains("a");
            argIndex = 2; // next argument may be the folder
        }

        if (parts.length > argIndex) {
            target = currentDirectory.resolve(parts[argIndex]).normalize();
        }

        // Sandbox restriction
        if (!target.startsWith(rootDirectory)) {
            String msg = "tree: permission denied";
            IO.println(msg);
            return new CommandResult("tree", false, msg, currentDirectory.toString(), target.toString(), 1);
        }

        // Existence check
        if (!Files.exists(target)) {
            String msg = "tree: no such file or directory: " + target.getFileName();
            IO.println(msg);
            return new CommandResult("tree", false, msg, currentDirectory.toString(), target.toString(), 1);
        }

        StringBuilder sb = new StringBuilder();
        try {
            // Build tree text
            buildTree(target, target, sb, "", false, showAll); // pass showAll flag

            // Print each line
            for (String line : sb.toString().split("\n")) {
                IO.println(line);
            }

            return new CommandResult("tree", true, sb.toString().trim(),
                    currentDirectory.toString(), target.toString(), 0);

        } catch (IOException e) {
            String msg = "tree: error reading directory";
            IO.println(msg);
            logger.warning("tree error: " + e.getMessage());
            return new CommandResult("tree", false,
                    "tree: error reading " + target,
                    currentDirectory.toString(), target.toString(), 1);
        }
    }

    private void buildTree(Path base, Path path, StringBuilder sb, String indent, boolean isLast, boolean showAll) throws IOException {
        // Print current folder/file
        String name = path.equals(base)
                ? path.getFileName() != null ? path.getFileName().toString() : path.toString()
                : path.getFileName().toString();

        if (!path.equals(base)) {
            sb.append(indent)
                    .append(isLast ? "└── " : "├── ")
                    .append(name)
                    .append(Files.isDirectory(path) ? "/" : "")
                    .append("\n");
        } else {
            sb.append(name).append("\n"); // root folder
        }

        if (!Files.isDirectory(path)) return;

        // List children, optionally skipping hidden files/folders
        List<Path> children = Files.list(path)
                .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                .filter(p -> showAll || !p.getFileName().toString().startsWith("."))
                .toList();

        for (int i = 0; i < children.size(); i++) {
            Path child = children.get(i);
            boolean last = (i == children.size() - 1);
            buildTree(base, child, sb, indent + (path.equals(base) ? "" : (isLast ? "    " : "│   ")), last, showAll);
        }
    }

    /**
     * Checks if the user has privileges to execute a sudo command.
     * Prompts for password using PasswordManager.
     *
     * @param inputParts the command arguments after 'sudo'
     * @return true if password is correct and privileges are granted, false otherwise
     */
    private boolean internalSudo(String[] inputParts) {
        // Prompt user for password silently
//        String enteredPassword = passwordManager.promptPassword("[sudo] Enter password: ");
//
//        // Verify password
//        if (!passwordManager.verifyPassword(enteredPassword)) {
//            IO.println("sudo: authentication failure");
//            return false;
//        }
//
//        IO.println("[sudo] Privilege check passed for: " + String.join(" ", inputParts));
        return true;
    }

    private CommandResult internalRm(String[] parts) {
        if (parts.length < 2) {
            String msg = "rm: missing operand";
            IO.println(msg);
            return new CommandResult("rm", false, msg, currentDirectory.toString(), null, 1, new ArrayList<>());
        }

        boolean recursive = false;
        List<String> targets = new ArrayList<>();
        List<Path> removedPaths = new ArrayList<>();

        // Parse flags and targets
        for (int i = 1; i < parts.length; i++) {
            if ("-r".equals(parts[i]) || "-R".equals(parts[i])) {
                recursive = true;
            } else {
                targets.add(parts[i]);
            }
        }

        String subjectForResult = String.join(",", targets);
        List<Path> pathsToDelete = new ArrayList<>();

        // Expand globs and wildcards
        for (String t : targets) {
            try {
                if (t.contains("*") || t.contains("?") || t.contains("[")) {
                    PathMatcher matcher = currentDirectory.getFileSystem().getPathMatcher("glob:" + t);
                    Files.list(currentDirectory)
                            .filter(matcher::matches)
                            .forEach(pathsToDelete::add);
                } else {
                    pathsToDelete.add(currentDirectory.resolve(t).normalize());
                }
            } catch (IOException e) {
                IO.println("rm: error listing directory for " + t);
            }
        }

        StringBuilder output = new StringBuilder();
        boolean success = true;

        for (Path targetPath : pathsToDelete) {
            if (!targetPath.startsWith(rootDirectory)) {
                String msg = "rm: " + targetPath.getFileName() + ": Permission denied";
                IO.println(msg);
                output.append(msg).append("\n");
                success = false;
                continue;
            }
            if (!Files.exists(targetPath)) {
                String msg = "rm: " + targetPath.getFileName() + ": No such file or directory";
                IO.println(msg);
                output.append(msg).append("\n");
                success = false;
                continue;
            }

            try {
                if (Files.isDirectory(targetPath)) {
                    if (!recursive) {
                        String msg = "rm: " + targetPath.getFileName() + ": Is a directory (use -r to remove)";
                        IO.println(msg);
                        output.append(msg).append("\n");
                        success = false;
                    } else {
                        Files.walk(targetPath)
                                .sorted(Comparator.reverseOrder())
                                .forEach(p -> {
                                    try {
                                        Files.deleteIfExists(p);
                                        removedPaths.add(p); // ✅ Track all deleted paths
                                    } catch (IOException ignored) {}
                                });
                    }
                } else {
                    Files.deleteIfExists(targetPath);
                    removedPaths.add(targetPath); // ✅ Track deleted file
                }
            } catch (IOException e) {
                String msg = "rm: " + targetPath.getFileName() + ": error deleting";
                IO.println(msg);
                output.append(msg).append("\n");
                logger.warning("rm error: " + e.getMessage());
                success = false;
            }
        }

        // For rm * only, subject points to current directory
        if (targets.size() == 1 && "*".equals(targets.get(0))) {
            subjectForResult = currentDirectory.toString();
        }

        return new CommandResult(
                "rm",
                success,
                output.toString().trim(),
                currentDirectory.toString(),
                subjectForResult,
                success ? 0 : 1,
                removedPaths // ✅ Affected paths now correctly filled
        );
    }

    private CommandResult internalTouch(String[] parts) {
        if (parts.length < 2) {
            String msg = "touch: missing file operand";
            IO.println(msg); // simulated stderr
            return new CommandResult("touch", false, msg, currentDirectory.toString(), null, 1);
        }

        StringBuilder output = new StringBuilder();
        boolean success = true;
        for (int i = 1; i < parts.length; i++) {
            Path filePath = currentDirectory.resolve(parts[i]).normalize();
            if (!filePath.startsWith(rootDirectory)) {
                String msg = "touch: " + parts[i] + ": Permission denied";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                success = false;
                continue;
            }
            try {
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                    String msg = "Created file: " + parts[i];
                    IO.println(msg); // stdout
                    output.append(msg).append("\n");
                } else {
                    // update timestamp
                    Files.setLastModifiedTime(filePath, FileTime.fromMillis(System.currentTimeMillis()));
                    String msg = "Updated timestamp: " + parts[i];
                    IO.println(msg); // stdout
                    output.append(msg).append("\n");
                }
            } catch (IOException e) {
                String msg = "touch: " + parts[i] + ": error";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                logger.warning("touch error: " + e.getMessage());
                success = false;
            }
        }

        return new CommandResult(
                "touch",
                success,
                output.toString().trim(),
                currentDirectory.toString(),
                parts.length > 1 ? parts[1] : null,
                success ? 0 : 1
        );
    }

    private CommandResult internalMkdir(String[] parts) {
        if (parts.length < 2) {
            String msg = "mkdir: missing operand";
            IO.println(msg); // simulated stderr
            return new CommandResult("mkdir", false, msg, currentDirectory.toString(), null, 1);
        }

        boolean success = true;
        StringBuilder output = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            Path dirPath = currentDirectory.resolve(parts[i]).normalize();
            if (!dirPath.startsWith(rootDirectory)) {
                String msg = "mkdir: " + parts[i] + ": Permission denied";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                success = false;
                continue;
            }
            try {
                Files.createDirectories(dirPath);
                String msg = "Created directory: " + parts[i];
                IO.println(msg); // stdout
                output.append(msg).append("\n");
            } catch (IOException e) {
                String msg = "mkdir: " + parts[i] + ": error";
                IO.println(msg); // simulated stderr
                output.append(msg).append("\n");
                logger.warning("mkdir error: " + e.getMessage());
                success = false;
            }
        }

        return new CommandResult(
                "mkdir",
                success,
                output.toString().trim(),
                currentDirectory.toString(),
                parts[1],
                success ? 0 : 1
        );
    }

    // -----------------------
    // EXTERNAL OS COMMAND EXECUTION
    // -----------------------

    private CommandResult externalCommand(String[] command) {
        Process process = null;
        String raw = String.join(" ", command);
        try {
            String[] adjusted = adjustCommandForOS(command);
            ProcessBuilder builder = new ProcessBuilder(adjusted);
            builder.directory(currentDirectory.toFile());
            builder.redirectErrorStream(true);

            process = builder.start();
            StringBuilder outSb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) outSb.append(line).append("\n");
            }
            int exit = process.waitFor();
            boolean success = exit == 0;
            return new CommandResult(command[0], success, outSb.toString().trim(), currentDirectory.toString(), null, exit);
        } catch (IOException e) {
            logger.severe("IO Error executing command: " + e.getMessage());
            return new CommandResult(raw, false, "IO Error: " + e.getMessage(), currentDirectory.toString(), null, 1);
        } catch (InterruptedException e) {
            logger.warning("Command execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return new CommandResult(raw, false, "Interrupted: " + e.getMessage(), currentDirectory.toString(), null, 1);
        } finally {
            if (process != null) process.destroy();
        }
    }

    private String[] adjustCommandForOS(String[] command) {
        if (!OS.contains("win")) return command;

        String cmd = command[0].toLowerCase(Locale.ROOT);
        return switch (cmd) {
            case "ls" -> new String[]{"cmd.exe", "/c", "dir"};
            case "pwd" -> new String[]{"cmd.exe", "/c", "cd"};
            case "rm" -> new String[]{"cmd.exe", "/c", "del", "/q"};
            default -> new String[]{"cmd.exe", "/c", String.join(" ", command)};
        };
    }
}