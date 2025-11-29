package utilities;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.Charset;

public class DirGenerator {

    private static final Set<String> HIDDEN_EXTENSIONS = Set.of(
            ".env", ".gitignore", ".gitattributes", ".dockerignore",
            ".npmignore", ".eslintignore", ".prettierignore"
    );

    public record GenerationResult(
            boolean success,
            String message,
            List<String> createdDirs,
            List<String> createdFiles,
            List<String> createdHiddenFiles,
            List<String> skippedPaths,
            List<String> errorPaths
    ) {
        public GenerationResult {
            createdDirs = createdDirs != null ? new ArrayList<>(createdDirs) : List.of();
            createdFiles = createdFiles != null ? new ArrayList<>(createdFiles) : List.of();
            createdHiddenFiles = createdHiddenFiles != null ? new ArrayList<>(createdHiddenFiles) : List.of();
            skippedPaths = skippedPaths != null ? new ArrayList<>(skippedPaths) : List.of();
            errorPaths = errorPaths != null ? new ArrayList<>(errorPaths) : List.of();
        }

        public int getTotalCreated() {
            return createdDirs.size() + createdFiles.size() + createdHiddenFiles.size();
        }
    }

    public record GenerationConfig(
            boolean overwriteExisting,
            boolean verbose,
            String lineSeparator,
            String encoding,
            boolean createHiddenFiles,
            Set<String> hiddenExtensions,
            String sandboxPath
    ) {
        public GenerationConfig {
            if (lineSeparator == null) lineSeparator = System.lineSeparator();
            if (encoding == null) encoding = "UTF-8";
            if (hiddenExtensions == null) hiddenExtensions = Set.of();
            if(sandboxPath == null) sandboxPath = "";
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
            private Set<String> hiddenExtensions = new HashSet<>(HIDDEN_EXTENSIONS);
            private String sandboxPath = "";

            public Builder overwriteExisting(boolean value) { overwriteExisting = value; return this; }
            public Builder verbose(boolean value) { verbose = value; return this; }
            public Builder sandboxPath(String value) { sandboxPath = value; return this; }
            public Builder lineSeparator(String value) { lineSeparator = value; return this; }
            public Builder encoding(String value) { encoding = value; return this; }
            public Builder createHiddenFiles(boolean value) { createHiddenFiles = value; return this; }
            public Builder hiddenExtensions(Set<String> value) { hiddenExtensions = value; return this; }

            public GenerationConfig build() {
                return new GenerationConfig(overwriteExisting, verbose, lineSeparator,
                        encoding, createHiddenFiles, hiddenExtensions, sandboxPath);
            }
        }
    }

    private record FileSystemEntry(
            String path,
            boolean isDirectory,
            String content,
            boolean hidden
    ) {
        public FileSystemEntry {
            if (path == null) path = "";
            if (content == null) content = "";
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
        List<String> skippedPaths = new ArrayList<>();
        List<String> errorPaths = new ArrayList<>();

        try {
            if (configFilePath == null || configFilePath.trim().isEmpty()) {
                DebugLogger.log("DIRGEN", "Config file path missing");
                return new GenerationResult(false, "Config file path is required",
                        createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
            }

            Path path = Paths.get(configFilePath);
            if (!Files.exists(path)) {
                DebugLogger.log("DIRGEN", "Config file not found: " + configFilePath);
                return new GenerationResult(false, "Config not found: " + configFilePath,
                        createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
            }

            DebugLogger.log("DIRGEN", "Reading config from: " + path.toAbsolutePath());

            List<FileSystemEntry> entries = parseConfigFile(path, config);

            for (FileSystemEntry entry : entries) {
                DebugLogger.log("DIRGEN", "Processing entry: " + entry.path());
                processEntry(entry, config, createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
            }

            String message = "Generated " +
                    createdDirs.size() + " dirs, " +
                    createdFiles.size() + " files, " +
                    createdHiddenFiles.size() + " hidden files";

            DebugLogger.log("DIRGEN", "Generation completed: " + message);

            return new GenerationResult(errorPaths.isEmpty(), message,
                    createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);

        } catch (Exception e) {
            DebugLogger.log("DIRGEN", "Generation FAILED: " + e.getMessage());
            return new GenerationResult(false, "Generation failed: " + e.getMessage(),
                    createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
        }
    }

    private void processEntry(FileSystemEntry entry, GenerationConfig config,
                              List<String> createdDirs, List<String> createdFiles,
                              List<String> createdHiddenFiles, List<String> skippedPaths,
                              List<String> errorPaths) {

        Path base = config.sandboxPath().isBlank()
                ? Paths.get(".")
                : Paths.get(config.sandboxPath());

        try {
            Path path = base.resolve(entry.path()).toAbsolutePath();

            DebugLogger.log("DIRGEN", "Resolved path: " + path);

            if (entry.isDirectory()) {
                processDirectory(path, config, createdDirs, skippedPaths);
            } else {
                processFile(entry, path, config, createdDirs, createdFiles, createdHiddenFiles, skippedPaths);
            }

        } catch (Exception ex) {
            errorPaths.add(entry.path() + ": " + ex.getMessage());
            DebugLogger.log("DIRGEN", "ERROR processing " + entry.path() + ": " + ex.getMessage());
        }
    }

    private void processDirectory(Path dir, GenerationConfig config,
                                  List<String> createdDirs, List<String> skipped) throws IOException {

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            createdDirs.add(dir.toString());
            DebugLogger.log("DIRGEN", "Created directory: " + dir);
        } else {
            skipped.add(dir.toString());
            DebugLogger.log("DIRGEN", "Skipped existing directory: " + dir);
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
            DebugLogger.log("DIRGEN", "Created parent folder: " + parent);
        }

        boolean shouldWrite = !Files.exists(file) || config.overwriteExisting();

        if (shouldWrite) {
            Files.write(file, entry.content().getBytes(config.encoding()));
            boolean hidden = entry.hidden() || shouldBeHidden(file, config);

            if (hidden && config.createHiddenFiles()) {
                makeFileHidden(file);
                createdHidden.add(file.toString());
                DebugLogger.log("DIRGEN", "Created hidden file: " + file);
            } else {
                createdFiles.add(file.toString());
                DebugLogger.log("DIRGEN", "Created file: " + file);
            }
        } else {
            skipped.add(file.toString());
            DebugLogger.log("DIRGEN", "Skipped existing file: " + file);
        }
    }

    private boolean shouldBeHidden(Path file, GenerationConfig config) {
        String name = file.getFileName().toString();
        if (name.startsWith(".")) return true;

        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return config.hiddenExtensions().contains(name.substring(idx).toLowerCase());
        }

        return false;
    }

    private void makeFileHidden(Path file) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("attrib", "+H", file.toAbsolutePath().toString()).start().waitFor();
            } else {
                if (!file.getFileName().toString().startsWith(".")) {
                    Files.move(file, file.resolveSibling("." + file.getFileName()));
                }
            }
        } catch (Exception e) {
            DebugLogger.log("DIRGEN", "Could not hide file: " + e.getMessage());
        }
    }

    private List<FileSystemEntry> parseConfigFile(Path configPath, GenerationConfig config) throws IOException {
        List<String> lines = Files.readAllLines(configPath, Charset.forName(config.encoding()));
        List<FileSystemEntry> entries = new ArrayList<>();

        FileSystemEntry current = null;
        StringBuilder builder = null;
        boolean explicitHidden = false;
        int lineNo = 0;

        for (String raw : lines) {
            lineNo++;
            String line = raw.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                DebugLogger.log("DIRGEN", "Ignored blank/comment line " + lineNo);
                continue;
            }

            if (line.startsWith("HIDDEN_DIR:")) {
                finish(entries, current, builder, explicitHidden);
                current = new FileSystemEntry(line.substring(11).trim(), true, "", true);
                builder = null;
                explicitHidden = true;
                DebugLogger.log("DIRGEN", "Parsed HIDDEN_DIR at line " + lineNo);
            }
            else if (line.startsWith("DIR:")) {
                finish(entries, current, builder, explicitHidden);
                current = new FileSystemEntry(line.substring(4).trim(), true, "", false);
                builder = null;
                explicitHidden = false;
                DebugLogger.log("DIRGEN", "Parsed DIR at line " + lineNo);
            }
            else if (line.startsWith("HIDDEN_FILE:")) {
                finish(entries, current, builder, explicitHidden);
                current = new FileSystemEntry(line.substring(12).trim(), false, "", true);
                builder = new StringBuilder();
                explicitHidden = true;
                DebugLogger.log("DIRGEN", "Parsed HIDDEN_FILE at line " + lineNo);
            }
            else if (line.startsWith("FILE:")) {
                finish(entries, current, builder, explicitHidden);
                current = new FileSystemEntry(line.substring(5).trim(), false, "", false);
                builder = new StringBuilder();
                explicitHidden = false;
                DebugLogger.log("DIRGEN", "Parsed FILE at line " + lineNo);
            }
            else if (line.equals("END") || line.equals("END_FILE")) {
                finish(entries, current, builder, explicitHidden);
                current = null;
                builder = null;
                explicitHidden = false;
                DebugLogger.log("DIRGEN", "Parsed END at line " + lineNo);
            }
            else if (builder != null) {
                builder.append(raw).append(config.lineSeparator());
            }
            else {
                finish(entries, current, builder, explicitHidden);
                boolean isDir = line.endsWith("/");
                boolean isHidden = line.startsWith(".") ||
                        config.hiddenExtensions().stream().anyMatch(line::endsWith);

                entries.add(new FileSystemEntry(line, isDir, "", isHidden));
                current = null;
                explicitHidden = false;
                DebugLogger.log("DIRGEN", "Parsed standalone entry at line " + lineNo + ": " + line);
            }
        }

        finish(entries, current, builder, explicitHidden);
        DebugLogger.log("DIRGEN", "Finished parsing config. Entries=" + entries.size());

        return entries;
    }

    private void finish(List<FileSystemEntry> entries, FileSystemEntry current,
                        StringBuilder builder, boolean explicitHidden) {

        if (current != null) {
            if (builder != null && !builder.isEmpty()) {
                entries.add(new FileSystemEntry(current.path(),
                        false,
                        builder.toString().trim(),
                        explicitHidden || current.hidden()
                ));
            } else {
                entries.add(current);
            }
        }
    }
}
