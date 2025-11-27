package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class LinuxCommandExecutor {
    private static final Logger logger = Logger.getLogger(LinuxCommandExecutor.class.getName());

    public static boolean executeCommand(String... command) {
        Process process = null;
        BufferedReader reader = null;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Command completed with exit code: " + exitCode);
            return exitCode == 0;

        } catch (IOException e) {
            logger.severe("IO Error executing command: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.warning("Command execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warning("Error closing reader: " + e.getMessage());
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }
}