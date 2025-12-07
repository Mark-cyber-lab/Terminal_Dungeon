package utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @param affectedPaths nullable
 */
public record CommandResult(String command, boolean success, String output, String path, String subject, int exitCode,
                            List<Path> affectedPaths) implements Loggable {

    // Constructor
    public CommandResult(String command, boolean success, String output, String path, String subject, int exitCode, List<Path> affectedPaths) {
        this.command = command;
        this.success = success;
        this.output = (output == null) ? "" : output;
        this.path = (path == null) ? "" : path;
        this.subject = subject;
        this.exitCode = exitCode;
        this.affectedPaths = affectedPaths; // can be null

        if (!success) {
            log("Command failed: " + this.toString());
        }
    }

    public CommandResult(String command, boolean success, String output, String path, String subject, int exitCode) {
        this(command, success, (output == null) ? "" : output, (path == null) ? "" : path, subject, exitCode, new ArrayList<Path>()); // can be null

        if (!success) {
            log("Command failed: " + this.toString());
        }
    }

    // Utility
    public Path getFileFullPath() {
        if (subject == null || subject.isBlank()) {
            return null;
        }

        Path fullPath = Path.of(path).resolve(subject).normalize();
        return Files.exists(fullPath) ? fullPath : null;
    }


    @Override
    public String toString() {
        return "CommandResult{" + "command='" + command + '\'' + ", success=" + success + ", exitCode=" + exitCode + ", path='" + path + '\'' + ", subject='" + subject + '\'' + ", output='" + output + '\'' + ", affectedPaths=" + affectedPaths + '}';
    }
}
