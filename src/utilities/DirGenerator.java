package utilities;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.nio.charset.Charset;

public class DirGenerator {

    // Set of file extensions that should be hidden
    private static final Set<String> HIDDEN_EXTENSIONS = Set.of(
            ".env", ".gitignore", ".gitattributes", ".dockerignore",
            ".npmignore", ".eslintignore", ".prettierignore"
    );

    // Record for generation result
    public record GenerationResult(
            boolean success,
            String message,
            List<String> createdDirs,
            List<String> createdFiles,
            List<String> createdHiddenFiles,
            List<String> skippedPaths,
            List<String> errorPaths
    ) {
        // Compact constructor for defensive copying
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

        @Override
        public String toString() {
            return String.format(
                    "GenerationResult[success=%s, totalCreated=%d, dirs=%d, files=%d, hidden=%d, skipped=%d, errors=%d]",
                    success, getTotalCreated(), createdDirs.size(), createdFiles.size(),
                    createdHiddenFiles.size(), skippedPaths.size(), errorPaths.size());
        }
    }

    // Record for generation configuration
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

        // Builder method for convenience
        public static Builder builder() {
            return new Builder();
        }

        // Builder class
        public static class Builder {
            private boolean overwriteExisting = false;
            private boolean verbose = true;
            private String lineSeparator = System.lineSeparator();
            private String encoding = "UTF-8";
            private boolean createHiddenFiles = true;
            private Set<String> hiddenExtensions = new HashSet<>(HIDDEN_EXTENSIONS);
            private String sandboxPath = "";

            public Builder overwriteExisting(boolean overwriteExisting) {
                this.overwriteExisting = overwriteExisting;
                return this;
            }

            public Builder verbose(boolean verbose) {
                this.verbose = verbose;
                return this;
            }

            public Builder sandboxPath(String sandboxPath) {
                this.sandboxPath = sandboxPath;
                return this;
            }

            public Builder lineSeparator(String lineSeparator) {
                this.lineSeparator = lineSeparator;
                return this;
            }

            public Builder encoding(String encoding) {
                this.encoding = encoding;
                return this;
            }

            public Builder createHiddenFiles(boolean createHiddenFiles) {
                this.createHiddenFiles = createHiddenFiles;
                return this;
            }

            public Builder hiddenExtensions(Set<String> hiddenExtensions) {
                this.hiddenExtensions = new HashSet<>(hiddenExtensions);
                return this;
            }

            public Builder addHiddenExtension(String extension) {
                this.hiddenExtensions.add(extension);
                return this;
            }

            public GenerationConfig build() {
                return new GenerationConfig(overwriteExisting, verbose, lineSeparator,
                        encoding, createHiddenFiles, hiddenExtensions, sandboxPath);
            }
        }
    }

    // Record for file system entries
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
            // Validate inputs
            if (configFilePath == null || configFilePath.trim().isEmpty()) {
                return new GenerationResult(false, "Config file path is required",
                        createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
            }

            Path configPath = Paths.get(configFilePath);
            if (!Files.exists(configPath)) {
//                IO.println("Config not found" + configPath.toAbsolutePath());
                return new GenerationResult(false, "Config file not found: " + configFilePath,
                        createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
            }

            // Parse and process entries
            List<FileSystemEntry> entries = parseConfigFile(configPath, config);

            for (FileSystemEntry entry : entries) {
                processEntry(entry, config, createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
//                IO.println("Processing entry: " + entry.path);

                // Use sandboxPath as base if specified, otherwise default to current entry path
                Path basePath = (config.sandboxPath() != null && !config.sandboxPath().isBlank())
                        ? Paths.get(config.sandboxPath())
                        : Paths.get("."); // default base path

                Path absolutePath = basePath.resolve(entry.path()).toAbsolutePath();

//                if (entry.isDirectory()) {
//                    System.out.println("Directory processed at: " + absolutePath);
//                } else {
//                    System.out.println("File processed at: " + absolutePath);
//                }
            }


            String message = String.format(
                    "Generated %d directories, %d files, %d hidden files (%d skipped, %d errors)",
                    createdDirs.size(), createdFiles.size(), createdHiddenFiles.size(),
                    skippedPaths.size(), errorPaths.size());

            return new GenerationResult(errorPaths.isEmpty(), message,
                    createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);

        } catch (Exception e) {
            return new GenerationResult(false, "Generation failed: " + e.getMessage(),
                    createdDirs, createdFiles, createdHiddenFiles, skippedPaths, errorPaths);
        }
    }

    private void processEntry(FileSystemEntry entry, GenerationConfig config,
                              List<String> createdDirs, List<String> createdFiles,
                              List<String> createdHiddenFiles, List<String> skippedPaths,
                              List<String> errorPaths) {
        try {
            Path basePath = (config.sandboxPath() != null && !config.sandboxPath().isBlank())
                    ? Paths.get(config.sandboxPath())
                    : Paths.get("."); // default base path

            Path absolutePath = basePath.resolve(entry.path()).toAbsolutePath();

            if (entry.isDirectory()) {
                processDirectory(absolutePath, config, createdDirs, skippedPaths);
            } else {
                processFile(entry, absolutePath, config, createdDirs, createdFiles, createdHiddenFiles, skippedPaths);
            }
        } catch (Exception e) {
            String error = entry.path() + " - " + e.getMessage();
            errorPaths.add(error);
            if (config.verbose()) {
                System.err.println("✗ Failed: " + error);
            }
        }
    }

    private void processDirectory(Path dirPath, GenerationConfig config,
                                  List<String> createdDirs, List<String> skippedPaths) throws IOException {
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            createdDirs.add(dirPath.toString());
//            if (config.verbose()) {
//                System.out.println("✓ Created directory: " + dirPath);
//            }
        } else {
            skippedPaths.add(dirPath.toString());
//            if (config.verbose()) {
//                System.out.println("⤳ Directory exists: " + dirPath);
//            }
        }
    }

    private void processFile(FileSystemEntry entry, Path filePath, GenerationConfig config,
                             List<String> createdDirs, List<String> createdFiles,
                             List<String> createdHiddenFiles, List<String> skippedPaths) throws IOException {
        // Create parent directories if needed
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            createdDirs.add(parentDir.toString());
        }

        // Create or overwrite file
        if (!Files.exists(filePath) || config.overwriteExisting()) {
            Files.write(filePath, entry.content().getBytes(config.encoding()));

            // Make file hidden if configured and applicable
            boolean isHidden = entry.hidden() || shouldBeHidden(filePath, config);
            if (isHidden && config.createHiddenFiles()) {
                makeFileHidden(filePath);
                createdHiddenFiles.add(filePath.toString());
                if (config.verbose()) {
                    System.out.println("✓ Created hidden file: " + filePath +
                            " (" + entry.content().length() + " chars)");
                }
            } else {
                createdFiles.add(filePath.toString());
//                if (config.verbose()) {
//                    System.out.println("✓ Created file: " + filePath +
//                            " (" + entry.content().length() + " chars)");
//                }
            }
        } else {
            skippedPaths.add(filePath.toString());
//            if (config.verbose()) {
//                System.out.println("⤳ File exists: " + filePath);
//            }
        }
    }

    private boolean shouldBeHidden(Path filePath, GenerationConfig config) {
        if (!config.createHiddenFiles()) {
            return false;
        }

        String fileName = filePath.getFileName().toString();

        // Check if file starts with dot (standard hidden file convention)
        if (fileName.startsWith(".")) {
            return true;
        }

        // Check if file extension is in hidden extensions list
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String extension = fileName.substring(lastDotIndex);
            return config.hiddenExtensions().contains(extension.toLowerCase());
        }

        return false;
    }

    private void makeFileHidden(Path filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows: use attrib command
                ProcessBuilder pb = new ProcessBuilder("attrib", "+H", filePath.toAbsolutePath().toString());
                pb.start().waitFor();
            } else {
                // Unix-like systems: rename to start with dot
                if (!filePath.getFileName().toString().startsWith(".")) {
                    Path hiddenPath = filePath.resolveSibling("." + filePath.getFileName());
                    Files.move(filePath, hiddenPath, StandardCopyOption.REPLACE_EXISTING);
                }
                // File already starts with dot, no need to rename
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not hide file " + filePath + ": " + e.getMessage());
        }
    }

    private List<FileSystemEntry> parseConfigFile(Path configPath, GenerationConfig config) throws IOException {
        List<FileSystemEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(configPath, Charset.forName(config.encoding()));

        FileSystemEntry currentEntry = null;
        StringBuilder contentBuilder = null;
        boolean explicitHidden = false;

        int lineNumber = 0;
        for (String rawLine : lines) {
            lineNumber++;
            String line = rawLine.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                IO.println("Line is empty.");
                continue; // Skip empty lines and comments
            }

            if (line.startsWith("HIDDEN_DIR:")) {
                finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
                String dirPath = line.substring(11).trim();
                currentEntry = new FileSystemEntry(dirPath, true, "", true);
                contentBuilder = null;
                explicitHidden = true;
//                System.out.println("[DEBUG] Line " + lineNumber + ": HIDDEN_DIR -> " + dirPath);
            } else if (line.startsWith("DIR:")) {
                finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
                String dirPath = line.substring(4).trim();
                currentEntry = new FileSystemEntry(dirPath, true, "", false);
                contentBuilder = null;
                explicitHidden = false;
//                System.out.println("[DEBUG] Line " + lineNumber + ": DIR -> " + dirPath);
            } else if (line.startsWith("HIDDEN_FILE:")) {
                finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
                String filePath = line.substring(12).trim();
                currentEntry = new FileSystemEntry(filePath, false, "", true);
                contentBuilder = new StringBuilder();
                explicitHidden = true;
                System.out.println("[DEBUG] Line " + lineNumber + ": HIDDEN_FILE -> " + filePath);
            } else if (line.startsWith("FILE:")) {
                finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
                String filePath = line.substring(5).trim();
                currentEntry = new FileSystemEntry(filePath, false, "", false);
                contentBuilder = new StringBuilder();
                explicitHidden = false;
//                System.out.println("[DEBUG] Line " + lineNumber + ": FILE -> " + filePath);
            } else if (line.equals("END_FILE") || line.equals("END")) {
                finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
//                System.out.println("[DEBUG] Line " + lineNumber + ": END -> " + (currentEntry != null ? currentEntry.path() : "null"));
                currentEntry = null;
                contentBuilder = null;
                explicitHidden = false;
            } else if (contentBuilder != null) {
                // We're in a file content block
                contentBuilder.append(rawLine).append(config.lineSeparator());
            } else {
                // Standalone entry (file or directory)
                finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
                boolean isDir = line.endsWith("/");
                boolean isHidden = line.startsWith(".") ||
                        config.hiddenExtensions().stream().anyMatch(line::endsWith);
                entries.add(new FileSystemEntry(line, isDir, "", isHidden));
                currentEntry = null;
                explicitHidden = false;
//                System.out.println("[DEBUG] Line " + lineNumber + ": Standalone entry -> " + line +
//                        " | isDir=" + isDir + " | hidden=" + isHidden);
            }
        }

        finishCurrentEntry(entries, currentEntry, contentBuilder, explicitHidden);
//        System.out.println("[DEBUG] Finished parsing config. Total entries: " + entries.size());
        return entries;
    }

    private void finishCurrentEntry(List<FileSystemEntry> entries, FileSystemEntry currentEntry,
                                    StringBuilder contentBuilder, boolean explicitHidden) {
        if (currentEntry != null) {
            if (contentBuilder != null && !contentBuilder.isEmpty()) {
                // File with content
                String content = contentBuilder.toString().trim();
                boolean hidden = explicitHidden || currentEntry.hidden();
                entries.add(new FileSystemEntry(currentEntry.path(), false, content, hidden));
            } else {
                // Directory or file without content
                entries.add(currentEntry);
            }
        }
    }
}