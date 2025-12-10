package v2;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class LinuxCommandExecutor {

    private Path currentDir = Paths.get(System.getProperty("user.dir"));
    private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    private final List<CommandListener> listeners = new ArrayList<>();
    private final List<CommandMiddleware> middleware = new ArrayList<>();
    private final List<CommandHistoryEntry> history = new ArrayList<>();
    private boolean sandboxMode = false; // simulate FS changes

    public void setSandboxMode(boolean enabled) {
        this.sandboxMode = enabled;
    }

    public void addListener(CommandListener l) {
        listeners.add(l);
    }

    public void removeListener(CommandListener l) {
        listeners.remove(l);
    }

    public void useMiddleware(CommandMiddleware mw) {
        middleware.add(mw);
    }

    public boolean removeMiddleware(CommandMiddleware mw) {
        return middleware.remove(mw);
    }

    public <T extends CommandMiddleware> boolean removeMiddlewareByClass(Class<T> clazz) {
        boolean removed = false;
        Iterator<CommandMiddleware> it = middleware.iterator();
        while (it.hasNext()) {
            CommandMiddleware mw = it.next();
            if (clazz.isAssignableFrom(mw.getClass())) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    public List<CommandHistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public Path getCurrentDir() {
        return currentDir;
    }

    // ----- Snapshot & Smart FS tracking -----
    private Map<Path, FileMetadata> snapshotSmart(Path dir) throws Exception {
        Map<Path, FileMetadata> map = new HashMap<>();
        if (!Files.exists(dir)) return map;

        Files.walk(dir).forEach(p -> {
            try {
                if (Files.isRegularFile(p)) {
                    long size = Files.size(p);
                    long lastModified = Files.getLastModifiedTime(p).toMillis();
                    map.put(dir.relativize(p), new FileMetadata(size, lastModified, null));
                }
            } catch (Exception ignored) {
            }
        });

        return map;
    }

    private static class FileMetadata {
        long size;
        long lastModified;
        String checksum;

        FileMetadata(long size, long lastModified, String checksum) {
            this.size = size;
            this.lastModified = lastModified;
            this.checksum = checksum;
        }

        String getChecksum(Path fullPath) throws Exception {
            if (checksum == null) {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                try (InputStream is = Files.newInputStream(fullPath)) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = is.read(buf)) != -1) digest.update(buf, 0, r);
                }
                byte[] hash = digest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : hash) sb.append(String.format("%02x", b));
                checksum = sb.toString();
            }
            return checksum;
        }

        boolean contentEquals(FileMetadata other, Path thisPath, Path otherPath) throws Exception {
            if (this.size != other.size || this.lastModified != other.lastModified) return false;
            return getChecksum(thisPath).equals(other.getChecksum(otherPath));
        }
    }

    private void detectFsChangesSmart(Map<Path, FileMetadata> before, CommandContext ctx) throws Exception {
        Map<Path, FileMetadata> after = snapshotSmart(currentDir);

        // Created and modified
        for (Path p : after.keySet()) {
            Path absPath = currentDir.resolve(p).toAbsolutePath().normalize();
            if (!before.containsKey(p)) ctx.created.add(absPath);
            else if (!after.get(p).contentEquals(before.get(p), currentDir.resolve(p), currentDir.resolve(p)))
                ctx.modified.add(absPath);
        }

        // Deleted
        for (Path p : before.keySet()) {
            if (!after.containsKey(p)) ctx.deleted.add(currentDir.resolve(p).toAbsolutePath().normalize());
        }

        // Renamed (heuristic: deleted + created with same checksum)
        for (Path d : new ArrayList<>(ctx.deleted)) {
            FileMetadata deletedMeta = before.get(d);
            for (Path c : new ArrayList<>(ctx.created)) {
                if (deletedMeta.contentEquals(after.get(c), currentDir.resolve(d), currentDir.resolve(c))) {
                    ctx.renamed.put(currentDir.resolve(d).toAbsolutePath().normalize(),
                            currentDir.resolve(c).toAbsolutePath().normalize());
                    ctx.deleted.remove(d);
                    ctx.created.remove(c);
                    break;
                }
            }
        }
    }

    private String[] expandWildcards(String[] args, String cmd) throws IOException {
        if (args.length == 0) return args;

        List<String> expanded = new ArrayList<>();
        int lastIndex = args.length - 1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // For mv/cp, last argument is destination; do not expand it
            if ((cmd.equals("mv") || cmd.equals("cp")) && i == lastIndex) {
                expanded.add(arg);
                continue;
            }

            // For rm, expand all args
            if (arg.contains("*") || arg.contains("?")) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir, arg)) {
                    for (Path p : stream) {
                        expanded.add(p.getFileName().toString());
                    }
                }
            } else {
                expanded.add(arg);
            }
        }

        return expanded.toArray(new String[0]);
    }

    // ----- Main execute -----
    public CommandResult execute(String input) {
        String[] parts = input.trim().split("\\s+");
        String cmdName = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        CommandContext ctx = new CommandContext();
        ctx.startDir = currentDir;
        ctx.endDir = currentDir; // default, auto-updates later
        ctx.command = cmdName;
        try {


            // ----------------------------------------------------------------------
            // 2) Expand wildcards for all commands *except* "cd" and "mv"
            // ----------------------------------------------------------------------
            if (!cmdName.equals("cd") && !cmdName.equals("mv")) {
                args = expandWildcards(args, cmdName);
            }

            // ----------------------------------------------------------------------
            // 3) Middleware (before)
            // ----------------------------------------------------------------------
            for (CommandMiddleware mw : middleware) {
                if (!mw.before(cmdName, args, ctx)) {
                    return new CommandResult(false, "Execution canceled by middleware", ctx);
                }
            }

            // ----------------------------------------------------------------------
            // 4) Listeners (before)
            // ----------------------------------------------------------------------
            for (CommandListener l : listeners) {
                l.beforeExecute(cmdName, args, currentDir);
            }

            // ----------------------------------------------------------------------
            // 5) Snapshot FS (only for modifying commands)
            // ----------------------------------------------------------------------
            Map<Path, FileMetadata> beforeSnapshot = null;
            boolean trackFs = Arrays.asList("rm", "touch", "mkdir", "mv").contains(cmdName);
            if (trackFs) beforeSnapshot = snapshotSmart(currentDir);

            long startTime = System.currentTimeMillis();
            CommandResult result = switch (cmdName) {
                case "cd" -> handleCd(args, ctx);
                case "mv" -> handleMove(args, ctx);
                default -> runRawCommand(cmdName, args, ctx);
            };

            // ----------------------------------------------------------------------
            // 6) Execute actual command
            // ----------------------------------------------------------------------

            ctx.executionTimeMs = System.currentTimeMillis() - startTime;

            if (cmdName.equals("cat")) {
                ctx.read = currentDir.resolve(args[0]).toAbsolutePath().normalize();
            }

            // ----------------------------------------------------------------------
            // 7) Auto-detect endDir when command touches paths
            // ----------------------------------------------------------------------
            if (!cmdName.equals("cd")) {
                for (String a : args) {
                    if (a.startsWith("-")) continue; // skip flags
                    Path p = currentDir.resolve(a).normalize();
                    Path parent = p.getParent();
                    if (parent != null)
                        ctx.endDir = parent.toAbsolutePath().normalize();
                }
            }

            // ----------------------------------------------------------------------
            // 8) Detect FS changes
            // ----------------------------------------------------------------------
            if (trackFs) detectFsChangesSmart(beforeSnapshot, ctx);

            // ----------------------------------------------------------------------
            // 9) Middleware (after)
            // ----------------------------------------------------------------------
            for (CommandMiddleware mw : middleware) {
                mw.after(cmdName, args, ctx, result);
            }

            // ----------------------------------------------------------------------
            // 10) Listeners (after)
            // ----------------------------------------------------------------------
            for (CommandListener l : listeners) {
                l.afterExecute(cmdName, args, result);
            }

            // ----------------------------------------------------------------------
            // 11) Save history
            // ----------------------------------------------------------------------
            history.add(new CommandHistoryEntry(cmdName, args, result));

            return result;

        } catch (Exception e) {
            CommandResult err = new CommandResult(false, cmdName + ": " + e.getMessage(), ctx);
            for (CommandListener l : listeners) l.afterExecute(cmdName, args, err);
            return err;
        }
    }

    // ----- CD -----
    protected CommandResult handleCd(String[] args, CommandContext ctx) {
        if (args.length == 0) return new CommandResult(false, "cd: missing operand", ctx);
        Path target = currentDir.resolve(args[0]).normalize();
        if (!Files.isDirectory(target)) return new CommandResult(false, "cd: no such directory: " + args[0], ctx);
        currentDir = target;
        ctx.endDir = currentDir;
        return new CommandResult(true, null, ctx);
    }

    // ----- MV -----
    protected CommandResult handleMove(String[] args, CommandContext ctx) throws IOException {
        if (args.length < 2) return new CommandResult(false, "mv: missing operand", ctx);

        // Resolve source and destination relative to currentDir, normalize to absolute path
        Path src = currentDir.resolve(args[0]).normalize();
        Path dst = currentDir.resolve(args[1]).normalize();

        if (!Files.exists(src)) return new CommandResult(false, "mv: source not found: " + src, ctx);

        // If destination is a directory, move inside it with the same file name
        if (Files.exists(dst) && Files.isDirectory(dst)) {
            dst = dst.resolve(src.getFileName());
        }

        if (!sandboxMode) Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);

        // Update context: track renamed file
        ctx.renamed.put(src.toAbsolutePath(), dst.toAbsolutePath());

        return new CommandResult(true, null, ctx);
    }

    // ----- Raw command (cat, ls, etc.) -----
    private CommandResult runRawCommand(String cmdName, String[] args, CommandContext ctx) throws Exception {
        List<String> command = translateIfWindows(cmdName, args);

        if (sandboxMode) return new CommandResult(true, "[SANDBOX] Command skipped: " + cmdName, ctx);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(currentDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");

        int exitCode = process.waitFor();
        return new CommandResult(exitCode == 0, output.toString().trim(), ctx);
    }

    private List<String> translateIfWindows(String cmd, String[] args) {
        if (!isWindows) {
            List<String> c = new ArrayList<>();
            c.add(cmd);
            Collections.addAll(c, args);
            return c;
        }

        List<String> command = new ArrayList<>();

        switch (cmd) {
            case "ls" -> {
                command.add("cmd");
                command.add("/c");
                command.add("dir");
                Collections.addAll(command, args);
            }
            case "cat" -> {
                command.add("cmd");
                command.add("/c");
                command.add("type");
                Collections.addAll(command, args);
            }
            case "pwd" -> {
                command.add("cmd");
                command.add("/c");
                command.add("cd");
            }
            case "tree" -> {
                command.add("cmd");
                command.add("/c");
                command.add("tree");
                command.add("/F");
            }
            case "mkdir" -> {
                command.add("cmd");
                command.add("/c");
                command.add("mkdir");
                Collections.addAll(command, args);
            }
            case "touch" -> {
                command.add("cmd");
                command.add("/c");
                StringBuilder sb = new StringBuilder();
                for (String file : args) sb.append("type nul > \"").append(file).append("\" & ");
                sb.setLength(sb.length() - 3); // remove last &
                command.add(sb.toString());
            }
            case "rm" -> {
                command.add("cmd");
                command.add("/c");
                boolean recursive = Arrays.asList(args).contains("-r");
                StringBuilder sb = new StringBuilder();

                for (String a : args) {
                    if (a.equals("-r")) continue;

                    Path resolved = currentDir.resolve(a);

                    if (!Files.exists(resolved)) {
                        sb.append("echo Skipping missing: ").append(a).append(" & ");
                        continue;
                    }

                    if (Files.isDirectory(resolved)) {
                        if (recursive) sb.append("rmdir /s /q \"").append(a).append("\" & ");
                        else sb.append("echo Cannot remove directory: ").append(a).append(" & ");
                    } else {
                        sb.append("del /q \"").append(a).append("\" & ");
                    }
                }

                if (sb.length() > 3) sb.setLength(sb.length() - 3); // remove last &
                command.add(sb.toString());
            }
            default -> {
                command.add(cmd);
                Collections.addAll(command, args);
            }
        }

        return command;
    }
}
