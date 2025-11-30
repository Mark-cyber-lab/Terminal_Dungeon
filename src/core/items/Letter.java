package core.items;

import core.storage.Inventory;
import utilities.CommandResult;

import java.nio.file.Path;

public class Letter extends Scroll {

    private final static String EXTENSION = "_letter.txt";

    public Letter(String name, String sourceFilePath) {
        super(name, sourceFilePath, EXTENSION);
    }

    public Letter(String name, CommandResult result) {
//        IO.println("Path " + result.path());
//        IO.println("Subject " +result.subject());
//        Path /mnt/c/Users/asus/IdeaProjects/Terminal_Dungeon/sandbox/level_1
//        Subject navigation_test/archives/dear_squire.txt
        super(
                name,
                Path.of(result.path()).resolve(result.subject()).toString(),
                EXTENSION
        );
    }
}
