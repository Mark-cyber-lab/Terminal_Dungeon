package engine;

import utilities.CommandValidator;
import utilities.LinuxCommandExecutor;
import utilities.DirGenerator;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Sandbox {

    private final String rootPath;
    private final CommandValidator validator;
    private final DirGenerator dirGenerator;
    private final LinuxCommandExecutor executor;

    public Sandbox(String rootPath) {
        this.rootPath = rootPath;

        this.executor = new LinuxCommandExecutor(rootPath);

        // Initialize validator with whitelist of safe commands
        Set<String> safeCommands = new HashSet<>();
        safeCommands.add("pwd");
        safeCommands.add("ls");
        safeCommands.add("tree");
        safeCommands.add("cat");
        safeCommands.add("rm");      // controlled deletion for enemies
        safeCommands.add("sudo");    // for special commands like sudo rm

        this.validator = new CommandValidator.Builder()
                .withAllowedCommands(safeCommands)
                .useWhitelist(true)
                .build();

        this.dirGenerator = new DirGenerator();

        // Ensure root sandbox exists
        File rootDir = new File(rootPath);

        if (!rootDir.exists()) {
            if (rootDir.mkdirs()) {
                System.out.println("Sandbox folder created at: " + rootDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create sandbox folder at: " + rootDir.getAbsolutePath());
            }
        } else {
            System.out.println("Sandbox folder already exists at: " + rootDir.getAbsolutePath());
        }

    }

    /**
     * Safely execute a command in the sandbox.
     */
    public String safeExecute(String command) {
        CommandValidator.ValidationResult result = validator.validate(command);

        if (!result.isValid()) {
            return "[Sandbox] ❌ Invalid command: " + result.getErrorMessage();
        }

        try {
            // Commands that require Linux execution
            if (command.startsWith("rm") || command.startsWith("sudo")) {
                String[] parts = command.split("\\s+");
                boolean success = this.executor.executeCommand(parts);
                return success ? "[Sandbox] ✅ Command executed successfully"
                        : "[Sandbox] ❌ Command failed";
            }

            this.executor.executeCommand(command);

            return "Success";

        } catch (Exception e) {
            return "[Sandbox] ❌ Exception: " + e.getMessage();
        }
    }

    public DirGenerator.GenerationResult generateStructure(String configFilePath) {
        return dirGenerator.generateFromConfig(configFilePath, rootPath);
    }

    public String getRootPath() {
        return rootPath;
    }

    public CommandValidator getValidator() {
        return validator;
    }

    public DirGenerator getDirGenerator() {
        return dirGenerator;
    }
}
