package utilities;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.Charset;

public class DirGenerator implements Loggable {

    private static final Set<String> HIDDEN_EXTENSIONS = Set.of(
            ".secr");

    public record GenerationResult(
            boolean success,
            String message,
            List<String> createdDirs,
            List<String> createdFiles,
            List<String> createdHiddenFiles,
            List<String> createdLockedDirs,
            List<String> skippedPaths,
            List<String> errorPaths) {
        public GenerationResult {
            createdDirs = createdDirs != null ? new ArrayList<>(createdDirs) : List.of();
            createdFiles = createdFiles != null ? new ArrayList<>(createdFiles) : List.of();
            createdHiddenFiles = createdHiddenFiles != null ? new ArrayList<>(createdHiddenFiles) : List.of();
            createdLockedDirs = createdLockedDirs != null ? new ArrayList<>(createdLockedDirs) : List.of();
            skippedPaths = skippedPaths != null ? new ArrayList<>(skippedPaths) : List.of();
            errorPaths = errorPaths != null ? new ArrayList<>(errorPaths) : List.of();
        }

        public int getTotalCreated() {
            return createdDirs.size() + createdFiles.size() + createdHiddenFiles.size() + createdLockedDirs.size();
        }
    }

    public record GenerationConfig(
            boolean overwriteExisting,
            boolean verbose,
            String lineSeparator,
            String encoding,
            boolean createHiddenFiles,
            boolean createLockedDoors,
            Set<String> hiddenExtensions,
            String sandboxPath) {
        public GenerationConfig {
            if (lineSeparator == null)
                lineSeparator = System.lineSeparator();
            if (encoding == null)
                encoding = "UTF-8";
            if (hiddenExtensions == null)
                hiddenExtensions = Set.of();
            if (sandboxPath == null)
                sandboxPath = "";
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean overwriteExisting = false;
            private boolean verbose = true;
            private String lineSeparator = System.lineSeparator();
            private String encoding = "UTF-8";
            private boolean createHiddenFiles = true;
            private boolean createLockedDoors = true;
            private Set<String> hiddenExtensions = new HashSet<>(HIDDEN_EXTENSIONS);
            private String sandboxPath = "";

            public Builder overwriteExisting(boolean value) {
                overwriteExisting = value;
                return this;
            }

            public Builder verbose(boolean value) {
                verbose = value;
                return this;
            }

            public Builder sandboxPath(String value) {
                sandboxPath = value;
                return this;
            }

            public Builder lineSeparator(String value) {
                lineSeparator = value;
                return this;
            }

            public Builder encoding(String value) {
                encoding = value;
                return this;
            }

            public Builder createHiddenFiles(boolean value) {
                createHiddenFiles = value;
                return this;
            }

            public Builder createLockedDoors(boolean value) {
                createLockedDoors = value;
                return this;
            }

            public Builder hiddenExtensions(Set<String> value) {
                hiddenExtensions = value;
                return this;
            }

            public GenerationConfig build() {
                return new GenerationConfig(overwriteExisting, verbose, lineSeparator,
                        encoding, createHiddenFiles, createLockedDoors, hiddenExtensions, sandboxPath);
            }
        }
    }

    private record FileSystemEntry(
            String path,
            boolean isDirectory,
            String content,
            boolean hidden,
            boolean locked) {
        public FileSystemEntry {
            if (path == null)
                path = "";
            if (content == null)
                content = "";
        }
    }

    public GenerationResult generateFromConfig(String configFilePath, String sandboxPath) {
        return generateFromConfig(configFilePath,
                GenerationConfig.builder().sandboxPath(sandboxPath).build());
    }

    public GenerationResult generateFromConfig(String configFilePath, GenerationConfig config) {
        List<String> createdDirs = new ArrayList<>();
        List<String> createdFiles = new ArrayList<>();
        List<String> createdHiddenFiles = new ArrayList<>();
        List<String> createdLockedDirs = new ArrayList<>();
        List<String> skippedPaths = new ArrayList<>();
        List<String> errorPaths = new ArrayList<>();

        try {
            if (configFilePath == null || configFilePath.trim().isEmpty()) {
                log("Config file path missing");
                return new GenerationResult(false, "Config file path is required",
                        createdDirs, createdFiles, createdHiddenFiles, createdLockedDirs, skippedPaths, errorPaths);
            }

            List<String> lines;

            // Check for dev environment (local file exists)
            Path devPath = Paths.get(configFilePath);
            if (Files.exists(devPath)) {
                log("Reading config from development path: " + devPath.toAbsolutePath());
                lines = Files.readAllLines(devPath, Charset.forName(config.encoding()));
            } else {
                // Load from resource inside JAR
                log("Reading config from JAR resource: " + configFilePath);
                InputStream is = DirGenerator.class.getResourceAsStream("/" + configFilePath);
                if (is == null) {
                    return new GenerationResult(false, "Config resource not found in JAR: " + configFilePath,
                            createdDirs, createdFiles, createdHiddenFiles, createdLockedDirs, skippedPaths, errorPaths);
                }
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, Charset.forName(config.encoding())));
                lines = reader.lines().toList();
            }

            List<FileSystemEntry> entries = parseConfigLines(lines, config);

            // ... then continue the rest of your current generateFromConfig logic
            // processing entries, creating dirs/files, locking, etc.

            for (FileSystemEntry entry : entries) {
                if (entry.locked()) {
                    processEntry(entry, config, createdDirs, createdFiles,
                            createdHiddenFiles, createdLockedDirs, skippedPaths, errorPaths);
                } else {
                    processEntryWithoutLocking(entry, config, createdDirs, createdFiles,
                            createdHiddenFiles, createdLockedDirs, skippedPaths, errorPaths);
                }
            }

            
            log("Generation completed successfully with " + entries.size() + " entries.");
            return new GenerationResult(true, "Config generated successfully",
                    createdDirs, createdFiles, createdHiddenFiles, createdLockedDirs, skippedPaths, errorPaths);

        } catch (Exception e) {
            log("Generation FAILED: " + e.getMessage());
            return new GenerationResult(false, "Generation failed: " + e.getMessage(),
                    createdDirs, createdFiles, createdHiddenFiles, createdLockedDirs, skippedPaths, errorPaths);
        }
    }

    // Split parseConfigFile into parseConfigLines to accept List<String>
    private List<FileSystemEntry> parseConfigLines(List<String> lines, GenerationConfig config) {
        List<FileSystemEntry> entries = new ArrayList<>();
        FileSystemEntry current = null;
        StringBuilder builder = null;
        boolean explicitHidden = false;
        boolean explicitLocked = false;
        int lineNo = 0;

        for (String raw : lines) {
            lineNo++;
            String line = raw.trim();

            if (line.isEmpty() || line.startsWith("#"))
                continue;

            if (line.startsWith("LOCKED_DIR:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(11).trim(), true, "", false, true);
                builder = null;
                explicitHidden = false;
                explicitLocked = true;
            } else if (line.startsWith("HIDDEN_DIR:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(11).trim(), true, "", true, false);
                builder = null;
                explicitHidden = true;
                explicitLocked = false;
            } else if (line.startsWith("DIR:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(4).trim(), true, "", false, false);
                builder = null;
                explicitHidden = false;
                explicitLocked = false;
            } else if (line.startsWith("HIDDEN_FILE:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(12).trim(), false, "", true, false);
                builder = new StringBuilder();
                explicitHidden = true;
                explicitLocked = false;
            } else if (line.startsWith("FILE:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(5).trim(), false, "", false, false);
                builder = new StringBuilder();
                explicitHidden = false;
                explicitLocked = false;
            } else if (line.equals("END") || line.equals("END_FILE")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = null;
                builder = null;
                explicitHidden = false;
                explicitLocked = false;
            } else if (builder != null) {
                builder.append(raw).append(config.lineSeparator());
            } else {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                boolean isDir = line.endsWith("/");
                boolean isHidden = line.startsWith(".") ||
                        config.hiddenExtensions().stream().anyMatch(line::endsWith);
                boolean isLocked = line.contains("[LOCKED]");

                String cleanPath = line.replace("[LOCKED]", "").trim();
                entries.add(new FileSystemEntry(cleanPath, isDir, "", isHidden, isLocked));
                current = null;
                explicitHidden = false;
                explicitLocked = false;
            }
        }

        finish(entries, current, builder, explicitHidden, explicitLocked);
        return entries;
    }

    private void processEntry(FileSystemEntry entry, GenerationConfig config,
            List<String> createdDirs, List<String> createdFiles,
            List<String> createdHiddenFiles, List<String> createdLockedDirs,
            List<String> skippedPaths, List<String> errorPaths) {

        Path base = config.sandboxPath().isBlank()
                ? Paths.get(".")
                : Paths.get(config.sandboxPath());

        try {
            Path path = base.resolve(entry.path()).toAbsolutePath();

            log("Resolved path: " + path);

            if (entry.isDirectory()) {
                processDirectory(entry, path, config, createdDirs, createdLockedDirs, skippedPaths, createdHiddenFiles);
            } else {
                processFile(entry, path, config, createdDirs, createdFiles, createdHiddenFiles, skippedPaths);
            }

        } catch (Exception ex) {
            errorPaths.add(entry.path() + ": " + ex.getMessage());
            log("ERROR processing " + entry.path() + ": " + ex.getMessage());
        }
    }

    private void processEntryWithoutLocking(FileSystemEntry entry, GenerationConfig config,
            List<String> createdDirs, List<String> createdFiles,
            List<String> createdHiddenFiles, List<String> createdLockedDirs,
            List<String> skippedPaths, List<String> errorPaths) {

        Path base = config.sandboxPath().isBlank()
                ? Paths.get(".")
                : Paths.get(config.sandboxPath());

        try {
            Path path = base.resolve(entry.path()).toAbsolutePath();

            log("Resolved path (without locking): " + path);

            if (entry.isDirectory()) {
                processDirectoryWithoutLocking(entry, path, config, createdDirs, skippedPaths, createdHiddenFiles);
            } else {
                processFile(entry, path, config, createdDirs, createdFiles, createdHiddenFiles, skippedPaths);
            }

        } catch (Exception ex) {
            errorPaths.add(entry.path() + ": " + ex.getMessage());
            log("ERROR processing " + entry.path() + " (without locking): " + ex.getMessage());
        }
    }

    private void processDirectory(FileSystemEntry entry, Path dir, GenerationConfig config,
            List<String> createdDirs, List<String> createdLockedDirs,
            List<String> skipped, List<String> createdHiddenDirs) throws IOException {

        Path finalPath = dir;
        String os = System.getProperty("os.name").toLowerCase();

        // Handle hidden directories on Linux/Unix
        if (entry.hidden() && config.createHiddenFiles()) {
            if (!os.contains("win") && !dir.getFileName().toString().startsWith(".")) {
                finalPath = dir.resolveSibling("." + dir.getFileName());
                createdHiddenDirs.add(finalPath.toString());
                log("Creating hidden directory (added dot prefix): " + finalPath);
            } else if (os.contains("win")) {
                // On Windows, we'll create it normally first, then hide it
                createdHiddenDirs.add(dir.toString());
            }
        }

        if (!Files.exists(finalPath)) {
            Files.createDirectories(finalPath);
            createdDirs.add(finalPath.toString());
            log("Created directory: " + finalPath);

            // Hide directory on Windows after creation
            if (entry.hidden() && config.createHiddenFiles() && os.contains("win")) {
                try {
                    new ProcessBuilder("attrib", "+H", finalPath.toAbsolutePath().toString()).start().waitFor();
                    log("Hidden directory on Windows: " + finalPath);
                } catch (Exception e) {
                    log("Could not hide directory on Windows: " + e.getMessage());
                }
            }

            if (entry.locked() && config.createLockedDoors()) {
                lockDirectoryWithChmod(finalPath);
                createdLockedDirs.add(finalPath.toString());
                log("Created locked door: " + finalPath);
            }
        } else {
            skipped.add(finalPath.toString());
            log("Skipped existing directory: " + finalPath);
        }
    }

    private void processDirectoryWithoutLocking(FileSystemEntry entry, Path dir, GenerationConfig config,
            List<String> createdDirs, List<String> skipped,
            List<String> createdHiddenDirs) throws IOException {

        Path finalPath = dir;
        String os = System.getProperty("os.name").toLowerCase();

        // Handle hidden directories on Linux/Unix
        if (entry.hidden() && config.createHiddenFiles()) {
            if (!os.contains("win") && !dir.getFileName().toString().startsWith(".")) {
                finalPath = dir.resolveSibling("." + dir.getFileName());
                createdHiddenDirs.add(finalPath.toString());
                log("Creating hidden directory without locking (added dot prefix): " + finalPath);
            } else if (os.contains("win")) {
                // On Windows, we'll create it normally first, then hide it
                createdHiddenDirs.add(dir.toString());
            }
        }

        if (!Files.exists(finalPath)) {
            Files.createDirectories(finalPath);
            createdDirs.add(finalPath.toString());
            log("Created directory (without locking): " + finalPath);

            // Hide directory on Windows after creation
            if (entry.hidden() && config.createHiddenFiles() && os.contains("win")) {
                try {
                    new ProcessBuilder("attrib", "+H", finalPath.toAbsolutePath().toString()).start().waitFor();
                    log("Hidden directory on Windows: " + finalPath);
                } catch (Exception e) {
                    log("Could not hide directory on Windows: " + e.getMessage());
                }
            }
        } else {
            skipped.add(finalPath.toString());
            log("Skipped existing directory: " + finalPath);
        }
    }

    private boolean lockDirectoryWithChmod(Path dir) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) {
                // Use chmod 700 instead of 000 for testing purposes
                // 000 would prevent even the owner from accessing
                // 700 allows owner access but prevents others
                Process process = new ProcessBuilder("chmod", "700", dir.toAbsolutePath().toString()).start();

                // Read the error stream to see if there are any issues
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }

                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    log("Successfully locked directory with chmod 700: " + dir);
                    return true;
                } else {
                    log("Failed to lock directory with chmod. Error: " + errorOutput.toString());
                    return false;
                }
            } else {
                Process process = new ProcessBuilder("attrib", "+R", dir.toAbsolutePath().toString()).start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log("Applied read-only to directory on Windows: " + dir);
                    return true;
                } else {
                    log("Failed to lock directory on Windows: " + dir);
                    return false;
                }
            }
        } catch (Exception e) {
            log("Could not lock directory: " + e.getMessage());
            return false;
        }
    }

    private void processFile(FileSystemEntry entry, Path file,
            GenerationConfig config,
            List<String> createdDirs, List<String> createdFiles,
            List<String> createdHidden, List<String> skipped) throws IOException {

        Path parent = file.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
            createdDirs.add(parent.toString());
            log("Created parent folder: " + parent);
        }

        boolean shouldWrite = !Files.exists(file) || config.overwriteExisting();

        if (shouldWrite) {
            boolean hidden = entry.hidden() || shouldBeHidden(file, config);

            Path finalPath = file;
            String os = System.getProperty("os.name").toLowerCase();
            if (hidden && config.createHiddenFiles() && !os.contains("win")
                    && !file.getFileName().toString().startsWith(".")) {
                finalPath = file.resolveSibling("." + file.getFileName());
            }

            Files.write(finalPath, entry.content().getBytes(config.encoding()));

            if (hidden && config.createHiddenFiles()) {
                if (os.contains("win")) {
                    try {
                        new ProcessBuilder("attrib", "+H", finalPath.toAbsolutePath().toString()).start().waitFor();
                    } catch (Exception e) {
                        log("Could not hide file on Windows: " + e.getMessage());
                    }
                }
                createdHidden.add(finalPath.toString());
                log("Created hidden file: " + finalPath);
            } else {
                createdFiles.add(finalPath.toString());
                log("Created file: " + finalPath);
            }
        } else {
            skipped.add(file.toString());
            log("Skipped existing file: " + file);
        }
    }

    private boolean shouldBeHidden(Path file, GenerationConfig config) {
        String name = file.getFileName().toString();
        if (name.startsWith("."))
            return true;

        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            String extension = name.substring(idx).toLowerCase();
            return config.hiddenExtensions().contains(extension);
        }

        return false;
    }

    private List<FileSystemEntry> parseConfigFile(Path configPath, GenerationConfig config) throws IOException {
        List<String> lines = Files.readAllLines(configPath, Charset.forName(config.encoding()));
        List<FileSystemEntry> entries = new ArrayList<>();

        FileSystemEntry current = null;
        StringBuilder builder = null;
        boolean explicitHidden = false;
        boolean explicitLocked = false;
        int lineNo = 0;

        for (String raw : lines) {
            lineNo++;
            String line = raw.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                log("Ignored blank/comment line " + lineNo);
                continue;
            }

            if (line.startsWith("LOCKED_DIR:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(11).trim(), true, "", false, true);
                builder = null;
                explicitHidden = false;
                explicitLocked = true;
                log("Parsed LOCKED_DIR at line " + lineNo);
            } else if (line.startsWith("HIDDEN_DIR:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(11).trim(), true, "", true, false);
                builder = null;
                explicitHidden = true;
                explicitLocked = false;
                log("Parsed HIDDEN_DIR at line " + lineNo);
            } else if (line.startsWith("DIR:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(4).trim(), true, "", false, false);
                builder = null;
                explicitHidden = false;
                explicitLocked = false;
                log("Parsed DIR at line " + lineNo);
            } else if (line.startsWith("HIDDEN_FILE:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(12).trim(), false, "", true, false);
                builder = new StringBuilder();
                explicitHidden = true;
                explicitLocked = false;
                log("Parsed HIDDEN_FILE at line " + lineNo);
            } else if (line.startsWith("FILE:")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = new FileSystemEntry(line.substring(5).trim(), false, "", false, false);
                builder = new StringBuilder();
                explicitHidden = false;
                explicitLocked = false;
                log("Parsed FILE at line " + lineNo);
            } else if (line.equals("END") || line.equals("END_FILE")) {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                current = null;
                builder = null;
                explicitHidden = false;
                explicitLocked = false;
                log("Parsed END at line " + lineNo);
            } else if (builder != null) {
                builder.append(raw).append(config.lineSeparator());
            } else {
                finish(entries, current, builder, explicitHidden, explicitLocked);
                boolean isDir = line.endsWith("/");
                boolean isHidden = line.startsWith(".") ||
                        config.hiddenExtensions().stream().anyMatch(line::endsWith);
                boolean isLocked = line.contains("[LOCKED]");

                String cleanPath = line.replace("[LOCKED]", "").trim();

                entries.add(new FileSystemEntry(cleanPath, isDir, "", isHidden, isLocked));
                current = null;
                explicitHidden = false;
                explicitLocked = false;
                log("Parsed standalone entry at line " + lineNo + ": " + cleanPath);
            }
        }

        finish(entries, current, builder, explicitHidden, explicitLocked);
        log("Finished parsing config. Entries=" + entries.size());

        return entries;
    }

    private void finish(List<FileSystemEntry> entries, FileSystemEntry current,
            StringBuilder builder, boolean explicitHidden, boolean explicitLocked) {

        if (current != null) {
            if (builder != null && !builder.isEmpty()) {
                entries.add(new FileSystemEntry(current.path(),
                        false,
                        builder.toString().trim(),
                        explicitHidden || current.hidden(),
                        explicitLocked || current.locked()));
            } else {
                entries.add(new FileSystemEntry(
                        current.path(),
                        current.isDirectory(),
                        current.content(),
                        explicitHidden || current.hidden(),
                        explicitLocked || current.locked()));
            }
        }
    }

    public boolean unlockDirectory(Path dir) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win")) {
                Process process = new ProcessBuilder("chmod", "755", dir.toAbsolutePath().toString()).start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log("Successfully unlocked directory: " + dir);
                    return true;
                }
            } else {
                new ProcessBuilder("attrib", "-R", dir.toAbsolutePath().toString()).start().waitFor();
                log("Removed read-only from directory on Windows: " + dir);
                return true;
            }
        } catch (Exception e) {
            log("Could not unlock directory: " + e.getMessage());
        }
        return false;
    }

    // Helper method to check if a directory is accessible
    public boolean isDirectoryAccessible(Path dir) {
        try {
            // Try to list the directory contents
            Files.list(dir).limit(1).close();
            return true;
        } catch (AccessDeniedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}