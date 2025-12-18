package utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AsciiArt {

    private static String[] readAsciiFromFile(String filePath) {
        try {
            Path srcPath = Path.of("./src");

            if(Files.exists(srcPath.resolve(filePath))) {
                filePath = srcPath.resolve(filePath).toString();
            } 
            
            List<String> lines = Files.readAllLines(Path.of(filePath));

            return lines.stream()
                    .map(line -> {
                        if (line.length() >= 2 && line.startsWith("'") && line.endsWith("'")) {
                            return line.substring(1, line.length() - 1);
                        }
                        return line;
                    })
                    .toArray(String[]::new);

        } catch (IOException e) {
            e.printStackTrace();
            return new String[]{"Error: Could not read ASCII art from file."};
        }
    }


    public static String[] getTitleDungeon() {
        return readAsciiFromFile("./ascii/terminal_dungeon.txt");
    }


    public static String[] getLevelHeader() {
        return new String[]{
                "::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: ::: :::"
        };
    }

    public static String[] getLevel1Squire() {
        return readAsciiFromFile("./ascii/level_1_squire.txt");
    }

    public static String[] getLevel2ApprenticeKnight() {
        return readAsciiFromFile("./ascii/level_2_apprentice_knight.txt");
    }

    public static String[] getLevel3ScoutKnight() {
        return readAsciiFromFile("./ascii/level_3_scout_knight.txt");
    }

    public static String[] getLevel4WarriorKnight() {
        return readAsciiFromFile("./ascii/level_4_warrior_knight.txt");
    }

    public static String[] getLevel5GuardianKnight() {
        return readAsciiFromFile("./ascii/level_5_guardian_knight.txt");
    }

    public static String[] getLevel5ArcaneKnight() {
        return readAsciiFromFile("./ascii/level_5_arcane_knight.txt");
    }

    public static String[] getLevel6Paladin() {
        return readAsciiFromFile("./ascii/level_6_paladin.txt");
    }

    public static String[] getLevel6GrandmasterKnight() {
        return readAsciiFromFile("./ascii/level_6_grandmaster_knight.txt");
    }

    public static String[] getLevel7ArcaneKnight() {
        return readAsciiFromFile("./ascii/level_7_arcane_knight.txt");
    }

    public static String[] getGameOver() {
        return readAsciiFromFile("./ascii/game_over.txt");
    }

}
