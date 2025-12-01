import core.enemies.Enemy;
import core.enemies.Goblin;
import utilities.LinuxCommandExecutorWithRegistry;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or

// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() throws IOException {

    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
     // to see how IntelliJ IDEA suggests fixing it.
    NewStage stage = new NewStage();

    stage.upStage();

//    LinuxCommandExecutorWithRegistry executor =
//            new LinuxCommandExecutorWithRegistry("./sandbox");
//
//    Enemy goblin = new Goblin("001", Path.of("./sandbox/guard_post/goblin.mob"));
//    Enemy goblin2 = new Goblin("002", Path.of("./sandbox/guard_post/goblin2.mob"));
//
//    // Register enemy as blocker and listener
//    executor.addBlocker(goblin);
//    executor.addBlocker(goblin2);
//
//    // Player tries to touch a file in guarded folder
//    executor.setCurrentDirectory("./guard_post");
//
//    // Player attacks goblin
//    executor.executeCommand("rm", "goblin.mob");
//    executor.executeCommand("cat", "dsfes.txt");  // Goblin blocks
//
//    // Remove listener
//    executor.removeCommandListener(goblin);
}

