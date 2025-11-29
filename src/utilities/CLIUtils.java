package utilities;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

/**
 * Utility class providing enhanced console/terminal features such as
 * screen clearing, typewriter text output, ASCII centering, and user
 * input pauses. Designed to work both in real terminals and IDE consoles.
 */
public class CLIUtils {

    /**
     * Clears the terminal screen using the best available method.
     * <p>
     * Behavior:
     * <ul>
     *     <li>Real Windows terminals → uses {@code cls} command</li>
     *     <li>Real Unix/macOS terminals → uses ANSI escape codes</li>
     *     <li>IDE terminals (IntelliJ, Eclipse, VS Code) → overwrites screen manually</li>
     *     <li>Fallback → prints multiple newlines</li>
     * </ul>
     */
    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isConsole = System.console() != null;

            if (isConsole) {
                // Real terminal
                if (os.contains("win")) {
                    // Windows cmd/powershell
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    // Unix/Linux/macOS
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
            } else {
                // IDE terminal (IntelliJ, Eclipse, VS Code)
                int terminalHeight = 30; // approximate visible lines
                String blankLine = " ".repeat(80); // assume 80 chars per line
                StringBuilder sb = new StringBuilder();

                // Move cursor to top-left
                sb.append("\033[H");

                // Overwrite visible area
                for (int i = 0; i < terminalHeight; i++) {
                    sb.append(blankLine).append("\n");
                }

                // Move cursor back to top-left
                sb.append("\033[H");

                System.out.print(sb);
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: just print multiple newlines
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    /**
     * Sleeps the current thread for the specified number of milliseconds.
     * Will restore the interrupt flag if interrupted.
     *
     * @param ms number of milliseconds to sleep
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Typewriter effect with optional centering.
     * Leading spaces (from centering) print instantly for responsiveness.
     *
     * @param output Text to display
     * @param delay  Delay per visible character
     * @param center true = centered, false = normal, null = normal
     */
    public static void typewriter(String output, int delay, Boolean center) {

        int index = 0;

        if (center) {
            int width = getTerminalWidth();
            if (!(output.length() >= width)) {
                int pad = (width - output.length()) / 2;
                IO.print(" ".repeat(pad));
            }
        }

        for (int i = index; i < output.length(); i++) {
            IO.print(output.charAt(i));
            sleep(delay);
        }

        System.out.println();
    }

    public static void typewriter(String output, int delay) {
        typewriter(output, delay, false);
    }

    /**
     * Prints a centered header with ASCII horizontal borders.
     *
     * @param text the header title
     */
    public static void header(String text) {
        String levelHeader = AsciiArt.getLevelHeader()[0];
        System.out.println(center(levelHeader));
        System.out.println(center(text));
        System.out.println(center(levelHeader));
    }

    /**
     * Prints a centered header with ASCII horizontal borders.
     *
     * @param text the header title
     */
    public static void header(String[] text) {
        String levelHeader = AsciiArt.getLevelHeader()[0];
        System.out.println(center(levelHeader));
        for(String line: text) {
            System.out.println(center(line));
        }
        System.out.println(center(levelHeader));
    }

    public static void header(String[] text, int padding) {
        header(text, padding, padding);
    }
    public static void header(String[] text, int paddingTop, int paddingBottom) {
        String levelHeader = AsciiArt.getLevelHeader()[0];
        System.out.println(center(levelHeader));
        for (int p = 0; p < paddingTop; p++) {
            IO.println();
        }
        for(String line: text) {
            System.out.println(center(line));
        }
        for (int p = 0; p < paddingBottom; p++) {
            IO.println();
        }
        System.out.println(center(levelHeader));
    }

    /**
     * Prints a loading animation using dots.
     *
     * @param label description text
     * @param dots  number of dots to print
     * @param delay delay (in milliseconds) between each dot
     */
    public static void loading(String label, int dots, int delay) {
        System.out.print(label);
        for (int i = 0; i < dots; i++) {
            System.out.print(".");
            sleep(delay);
        }
        System.out.println();
    }

    /**
     * Prints a scene transition marker and pauses execution.
     *
     * @param ms duration of the pause in milliseconds
     */
    public static void transition(int ms) {
        System.out.println("\n...\n");
        sleep(ms);
    }

    /**
     * Displays a message prompting the user to press Enter.
     * After input is received, clears the screen.
     * <p>
     * Also consumes any extra buffered input.
     *
     * @param message custom prompt (null for default)
     */
    public static void waitAnyKey(String message) {
        System.out.println(message != null ? message : "Press Enter to continue...");
        try {
            do System.in.read();
            while (System.in.available() > 0);
        } catch (IOException ignored) {
        }
    }

    /**
     * Same as {@link #waitAnyKey(String)}, using a default message.
     */
    public static void waitAnyKey() {
        waitAnyKey(null);
    }

    /**
     * Attempts to detect terminal width using {@code tput cols}.
     * <p>
     * If detection fails (common in IDE terminals), a default width of 100 is used.
     *
     * @return terminal width in characters
     */
    public static int getTerminalWidth() {
        // 1. Try COLUMNS env var (works in many shells)
        String columnsEnv = System.getenv("COLUMNS");
        if (columnsEnv != null) {
            try {
                int width = Integer.parseInt(columnsEnv);
                DebugLogger.log("CLIUtils"," Terminal width from COLUMNS env: " + width);
                return width;
            } catch (NumberFormatException ignored) {}
        }

        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", "stty size 2>/dev/null || echo 0 0").start();
            try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream())) {
                if (scanner.hasNextInt()) {
                    scanner.nextInt(); // rows
                    int cols = scanner.nextInt(); // columns
                    if (cols > 0) {
                        DebugLogger.log("CLIUtils", "Terminal width from stty: " + cols);
                        return cols;
                    }
                }
            }
        } catch (Exception ignored) {}


        int fallback = 125;
        DebugLogger.log("CLIUtils", "Using fallback terminal width: " + fallback);
        return fallback;
    }



    /**
     * Centers a single line of text using the detected terminal width.
     * <p>
     * If the line is longer than the terminal width, it is returned unchanged.
     *
     * @param line the text to center
     * @return centered text with left/right padding
     */
    public static String center(String line) {
        int width = getTerminalWidth();
        if (line.length() >= width) return line;
        int pad = (width - line.length()) / 2;
        int rightPad = width - line.length() - pad;
        return " ".repeat(pad) + line + " ".repeat(rightPad);
    }

    /**
     * Centers every line in an ASCII art array.
     *
     * @param asciiArt array of ASCII lines
     * @return new array with each line centered
     */
    public static String[] centerAscii(String[] asciiArt) {
        String[] centered = new String[asciiArt.length];
        for (int i = 0; i < asciiArt.length; i++) {
            centered[i] = center(asciiArt[i]);
        }
        return centered;
    }

    /**
     * Prints ASCII art centered according to terminal width.
     *
     * @param asciiArt ASCII art lines to print
     */
    public static void printCentered(String[] asciiArt) {
        for (String line : centerAscii(asciiArt)) {
            System.out.println(line);
        }
    }

    /**
     * Prints a character or string repeatedly to fill the terminal width.
     *
     * @param content the character(s) or string to repeat
     */
    public static void repeat(String content) {
        int width = getTerminalWidth();
        if (content == null || content.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        while (sb.length() + content.length() <= width) {
            sb.append(content);
        }
        // Print any leftover if necessary
        if (sb.length() < width) {
            sb.append(content, 0, width - sb.length());
        }
        System.out.println(sb);
    }

    /**
     * Overload for repeating a single character.
     *
     * @param ch character to repeat across the terminal width
     */
    public static void repeat(char ch) {
        repeat(String.valueOf(ch));
    }

}
