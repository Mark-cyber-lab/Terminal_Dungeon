package utilities;

import java.util.*;
import java.util.regex.Pattern;

public class CommandValidator {

    private final Set<String> allowedCommands;
    private final Set<String> blockedCommands;
    private final boolean useWhitelist;

    // Constructor without whitelist (defaults to blacklist mode)
    public CommandValidator() {
        this.allowedCommands = new HashSet<>();
        this.blockedCommands = new HashSet<>();
        this.useWhitelist = false;
        initializeDefaultBlockedCommands();
    }

    // Constructor with fixed whitelist setting
    public CommandValidator(Set<String> allowedCommands, boolean useWhitelist) {
        this.allowedCommands = allowedCommands != null ? new HashSet<>(allowedCommands) : new HashSet<>();
        this.blockedCommands = new HashSet<>();
        this.useWhitelist = useWhitelist;
        initializeDefaultBlockedCommands();
    }

    // Builder pattern for flexible configuration
    public static class Builder {
        private Set<String> allowedCommands = new HashSet<>();
        private Set<String> blockedCommands = new HashSet<>();
        private boolean useWhitelist = false;

        public Builder withAllowedCommands(Set<String> allowedCommands) {
            this.allowedCommands = new HashSet<>(allowedCommands);
            return this;
        }

        public Builder withBlockedCommands(Set<String> blockedCommands) {
            this.blockedCommands = new HashSet<>(blockedCommands);
            return this;
        }

        public Builder useWhitelist(boolean useWhitelist) {
            this.useWhitelist = useWhitelist;
            return this;
        }

        public Builder addAllowedCommand(String command) {
            this.allowedCommands.add(command);
            return this;
        }

        public Builder addBlockedCommand(String command) {
            this.blockedCommands.add(command);
            return this;
        }

        public CommandValidator build() {
            return new CommandValidator(this);
        }
    }

    private CommandValidator(Builder builder) {
        this.allowedCommands = builder.allowedCommands;
        this.blockedCommands = builder.blockedCommands;
        this.useWhitelist = builder.useWhitelist;
        initializeDefaultBlockedCommands();
    }

    private void initializeDefaultBlockedCommands() {
        // Add default dangerous commands
        blockedCommands.addAll(Arrays.asList(
                "rm -rf /", "rm -rf /*", "mkfs", "dd if=/dev/zero",
                ":(){ :|:& };:", "chmod -R 777 /", "chown -R root:root /"
        ));
    }

    public ValidationResult validate(String command) {
        if (command == null || command.trim().isEmpty()) {
            return ValidationResult.invalid("Command cannot be empty");
        }

        command = command.trim();

        // Check for empty command after trim
        if (command.isEmpty()) {
            return ValidationResult.invalid("Command cannot be empty");
        }

        // Check for dangerous commands
        if (isExplicitlyBlocked(command)) {
            return ValidationResult.invalid("Command is explicitly blocked");
        }

        // Check for command injection
        if (hasCommandInjection(command)) {
            return ValidationResult.invalid("Command injection detected");
        }

        // Check for suspicious patterns
        if (hasSuspiciousPatterns(command)) {
            return ValidationResult.invalid("Suspicious command pattern");
        }

        // Validate syntax
        if (!hasValidSyntax(command)) {
            return ValidationResult.invalid("Invalid command syntax");
        }

        // Check whitelist/blacklist
        if (useWhitelist && !isWhitelisted(command)) {
            return ValidationResult.invalid("Command not in allowed list");
        }

        if (!useWhitelist && isBlacklisted(command)) {
            return ValidationResult.invalid("Command is blacklisted");
        }

        return ValidationResult.valid();
    }

    private boolean isExplicitlyBlocked(String command) {
        return blockedCommands.stream().anyMatch(command::contains);
    }

    private boolean hasCommandInjection(String command) {
        String[] injectionPatterns = {";", "&&", "||", "`", "$(", "|", ">"};
        return Arrays.stream(injectionPatterns).anyMatch(command::contains);
    }

    private boolean hasSuspiciousPatterns(String command) {
        // Patterns that indicate dangerous operations
        Pattern dangerousPatterns = Pattern.compile(
                "rm\\s+-rf\\s+/($|\\s|\\*)|" +           // rm -rf /
                        "chmod\\s+-[Rr]\\s+777\\s+/|" +         // chmod -R 777 /
                        "chown\\s+-[Rr]\\s+root:root\\s+/|" +   // chown -R root:root /
                        "dd\\s+if=.*of=/dev/",                  // dd to device
                Pattern.CASE_INSENSITIVE
        );

        return dangerousPatterns.matcher(command).find();
    }

    private boolean hasValidSyntax(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length == 0) return false;

        String firstPart = parts[0];
        // Command should start with alphanumeric characters, not special chars or paths
        return firstPart.matches("[a-zA-Z0-9_-]+");
    }

    private boolean isWhitelisted(String command) {
        String firstWord = command.split("\\s+")[0];
        return allowedCommands.contains(firstWord);
    }

    private boolean isBlacklisted(String command) {
        // Add custom blacklist logic here if needed
        return false;
    }

    // Getters (since fields are final)
    public Set<String> getAllowedCommands() { return new HashSet<>(allowedCommands); }
    public Set<String> getBlockedCommands() { return new HashSet<>(blockedCommands); }
    public boolean isUseWhitelist() { return useWhitelist; }

    // Validation Result class
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public boolean hasError() { return !valid; }
    }
}