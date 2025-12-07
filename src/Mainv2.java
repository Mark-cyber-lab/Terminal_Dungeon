import v2.*;
import v2.doors.HiddenDoor;
import v2.doors.HiddenKey;
import v2.enemies.Enemy;

import java.nio.file.Path;

void main(String[] args) throws IOException {
//    main2(args);
    NewStagev2 stage = new NewStagev2();

    stage.upStage();
}

void main1(String[] args) throws IOException {

    DungeonExecutor exec = getDungeonExecutor();

    // Add middleware
//        exec.useMiddleware(new CommandMiddleware() {
//            @Override
//            public boolean before(String command, String[] args, CommandContext context) {
//                if (command.equals("rm")) {
//                    for (String arg : args) {
//                        if (arg.equalsIgnoreCase("important.txt")) {
//                            System.out.println("[Middleware] Blocking deletion of important.txt");
//                            return false; // cancel deletion
//                        }
//                    }
//                }
//                return true;
//            }
//
//            @Override
//            public void after(String command, String[] args, CommandContext context, CommandResult result) {
//                if (result.isSuccess()) System.out.println("[Middleware] Command executed: " + command);
//            }
//        });

    // Run example commands
    exec.execute("pwd");
    exec.execute("mkdir test1 test2");
    exec.execute("cd test1");
    exec.execute("touch enemy1.txt enemy2.txt important.txt");

    Enemy enemy1 = new Enemy("001", "enemy1", Path.of("./playground/test1/enemy1.txt"), 5);
    Enemy enemy2 = new Enemy("002", "enemy2", Path.of("./playground/test1/enemy2.txt"), 5);

    Mission troupe = new Mission(exec, null);

    troupe.addEnemy(enemy1).addEnemy(enemy2);
    troupe.initialize();

//        exec.execute("ls");
//        exec.execute("ls -la");
//        exec.execute("cd ..");
//        exec.execute("rm -r test1 test2 important.txt");
//
//        exec.execute("cd test1 test2");
//        exec.execute("tortant.txt");
//        exec.execute("cd ..");
//exec.execute("cd sewfws");
    exec.execute("rm -r gg.mob asa.mob");

    troupe.cleanup();
//    List<CommandHistoryEntry> history = exec.getHistory();
//
//    for (CommandHistoryEntry entry : history) {
//        System.out.println(entry.command);
//        IO.println(entry.result);
//        IO.println(Arrays.toString(entry.args));
//    }
}

void main2(String[] args) throws IOException {

    DungeonExecutor exec = getDungeonExecutor();


    exec.execute("mkdir .test1 test2");
    exec.execute("cd test2");
    exec.execute("touch key.txt enemy.txt enemy2.txt");

    Enemy enemy1 = new Enemy("001", "enemy1", Path.of("./playground/test2/enemy.txt"), 5);
    Enemy enemy2 = new Enemy("002", "enemy1", Path.of("./playground/test2/enemy2.txt"), 5);
    HiddenKey key = new HiddenKey("key.txt", "", Path.of("./playground/test2/key.txt"));
    HiddenDoor door = new HiddenDoor("door", "door1", Path.of("./playground/.test1")).unlocksBy(key);

    Mission troupe = new Mission(exec, null);

    troupe.addEnemy(enemy1).addEnemy(enemy2);
    troupe.addDoor(door);
    troupe.initialize();

//    exec.execute("rm enemy.txt");
//    exec.execute("rm enemy2.txt");
    exec.execute("mv ./key.txt ..");

//        exec.execute("ls");
//        exec.execute("ls -la");
//        exec.execute("cd ..");
//        exec.execute("rm -r test1 test2 important.txt");
//
//        exec.execute("cd test1 test2");
//        exec.execute("tortant.txt");
    exec.execute("cd ../.test1");
//    exec.execute("rm -r ../.test1 ../test2");
//    List<CommandHistoryEntry> history = exec.getHistory();
//
//    for (CommandHistoryEntry entry : history) {
//        System.out.println(entry.command);
//        IO.println(entry.result);
//        IO.println(Arrays.toString(entry.args));
//    }
    troupe.cleanup();
}

private DungeonExecutor getDungeonExecutor() {
    DungeonExecutor exec = new DungeonExecutor(Path.of("./playground"), Path.of("./playground/inventory"));

    // Enable sandbox mode if needed
//         exec.setSandboxMode(true);

    // Add listener
    exec.addListener(new CommandListener() {
        @Override
        public void beforeExecute(String command, String[] args, Path currentDir) {
                System.out.println("[Before] " + command + " " + String.join(" ", args));
//                System.out.println("Current Dir: " + currentDir);
        }

        @Override
        public void afterExecute(String command, String[] args, CommandResult result) {
            IO.println(result.getOutput());
            IO.println("Start " + result.getContext().startDir);
            IO.println("End " + result.getContext().endDir);
//                System.out.println("[After] Command: " + command);
//                System.out.println("Success: " + result.isSuccess());
//                System.out.println("Output:\n" + result.getOutput());
//                System.out.println("Created: " + result.getContext().created);
//                System.out.println("Deleted: " + result.getContext().deleted);
//                System.out.println("Modified: " + result.getContext().modified);
//                System.out.println("Renamed: " + result.getContext().renamed);
//                System.out.println("Execution Time: " + result.getContext().executionTimeMs + " ms");
//                System.out.println("--------------------------------------------------");
        }
    });
    return exec;
}

