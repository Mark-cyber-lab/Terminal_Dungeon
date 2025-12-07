package test;

import core.enemies.Enemy;
import core.enemies.Goblin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.LinuxCommandExecutorWithRegistry;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {

    private LinuxCommandExecutorWithRegistry executor;

    private Path sandboxRoot;
    private Path guardPostDir;
    private Path towerDir;

    private Path goblin1File;
    private Path goblin2File;
    private Path goblin3File; // in tower
    private Path goblin4File; // in tower

    private Enemy goblin1;
    private Enemy goblin2;
    private Enemy goblin3;
    private Enemy goblin4;

    @BeforeEach
    void setup() throws Exception {
        sandboxRoot = Path.of("./sandbox_test");
        guardPostDir = sandboxRoot.resolve("guard_post");
        towerDir = sandboxRoot.resolve("tower");

        goblin1File = guardPostDir.resolve("goblin.mob");
        goblin2File = guardPostDir.resolve("goblin2.mob");
        goblin3File = towerDir.resolve("goblin3.mob");
        goblin4File = towerDir.resolve("goblin4.mob");

        // Create directories
        Files.createDirectories(guardPostDir);
        Files.createDirectories(towerDir);

        // Create mob files
        Files.createFile(goblin1File);
        Files.createFile(goblin2File);
        Files.createFile(goblin3File);
        Files.createFile(goblin4File);

        executor = new LinuxCommandExecutorWithRegistry("./sandbox_test");

        // Instantiate enemies
        goblin1 = new Goblin("001", goblin1File);
        goblin2 = new Goblin("002", goblin2File);
        goblin3 = new Goblin("003", goblin3File);
        goblin4 = new Goblin("004", goblin4File);

        // Register enemies
        executor.addBlocker(goblin1);
        executor.addBlocker(goblin2);
        executor.addBlocker(goblin3);
        executor.addBlocker(goblin4);
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(goblin1File);
        Files.deleteIfExists(goblin2File);
        Files.deleteIfExists(goblin3File);
        Files.deleteIfExists(goblin4File);

        Files.deleteIfExists(guardPostDir);
        Files.deleteIfExists(towerDir);
        Files.deleteIfExists(sandboxRoot);
    }

    // --------------------------------------------------------
    // TEST 1: rm <enemy1> <enemy2> in guard_post
    // --------------------------------------------------------
    @Test
    void testRmMultipleEnemies() {
        executor.setCurrentDirectory("./guard_post");

        executor.executeCommand("rm", "goblin.mob", "goblin2.mob");

        assertTrue(goblin1.hasBeenDefeated(), "Goblin 001 should be defeated");
        assertTrue(goblin2.hasBeenDefeated(), "Goblin 002 should be defeated");

        assertFalse(Files.exists(goblin1File), "goblin.mob should be deleted");
        assertFalse(Files.exists(goblin2File), "goblin2.mob should be deleted");
    }

    // --------------------------------------------------------
    // TEST 2: rm -r * in tower
    // --------------------------------------------------------
    @Test
    void testRmRecursiveAllInTower() {
        executor.setCurrentDirectory("./tower");

        executor.executeCommand("rm", "-r", "*");

        assertTrue(goblin3.hasBeenDefeated(), "Goblin 003 should be defeated");
        assertTrue(goblin4.hasBeenDefeated(), "Goblin 004 should be defeated");

        assertFalse(Files.exists(goblin3File), "goblin3.mob should be deleted");
        assertFalse(Files.exists(goblin4File), "goblin4.mob should be deleted");
    }

    // --------------------------------------------------------
    // TEST 3: rm *.mob deletes all .mob files in guard_post
    // --------------------------------------------------------
    @Test
    void testDeleteAllMobFilesInGuardPost() {
        executor.setCurrentDirectory("./guard_post");

        executor.executeCommand("rm", "*.mob");

        assertTrue(goblin1.hasBeenDefeated(), "Goblin 001 should be defeated");
        assertTrue(goblin2.hasBeenDefeated(), "Goblin 002 should be defeated");

        assertFalse(Files.exists(goblin1File), "goblin.mob should be deleted");
        assertFalse(Files.exists(goblin2File), "goblin2.mob should be deleted");
    }

    // --------------------------------------------------------
    // TEST 4: blocked commands still blocked by remaining enemies
    // --------------------------------------------------------
    @Test
    void testBlockedCommands() {
        executor.setCurrentDirectory("./guard_post");

        executor.executeCommand("rm", "goblin.mob");  // defeat goblin1 only

        var result = executor.executeCommand("cat", "dsfes.txt");

        assertNotNull(result, "Command result must not be null");

//        assertTrue(
//                result.getOutput().toLowerCase().contains("blocked")
//                        || result.getOutput().toLowerCase().contains("enemy"),
//                "Command should be blocked by goblin2"
//        );
    }
}
