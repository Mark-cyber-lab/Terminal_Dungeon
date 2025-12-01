package utilities;

public record CommandResult(
        String command,
        boolean success,
        String output,
        String path,
        String subject,
        int exitCode
) implements Loggable {

    // Compact constructor
    public CommandResult {
        output = (output == null) ? "" : output;
        path = (path == null) ? "" : path;

        // Log all details if success is false
        if (!success) {
            log( "Command failed: " + this.toString());
        }
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "command='" + command + '\'' +
                ", success=" + success +
                ", exitCode=" + exitCode +
                ", path='" + path + '\'' +
                ", subject='" + subject + '\'' +
                ", output='" + output + '\'' +
                '}';
    }
}
