package core.levels;

import core.Player;
import engine.Sandbox;

public class Level1_Squire extends Level {

    private final Sandbox sandbox;
    private final String configPath = "./src/stages/stage1.txt";

    public Level1_Squire(Sandbox sandbox, Player player) {
        super(1, player);
        this.sandbox = sandbox;
    }

    @Override
    public void setupEnvironment() {
        IO.println("\n⚔️ Preparing your training grounds...");
        sandbox.getDirGenerator().generateFromConfig(configPath, sandbox.getRootPath());
        IO.println("📁 A mysterious training room materializes around you...");
    }

    /** Stage 1: Learn 'pwd' */
    private void stage1() {
        IO.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        IO.println("📜 Stage 1 — Orientation of the Young Squire");
        IO.println("The old knight approaches you...");
        IO.println("\"To survive this dungeon lad, you must first know **where** you stand.\"");
        IO.println("➡️   Type **pwd** to sense your current location.");
        IO.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        waitForStageCommand("pwd");
//        player.remember("Learned pwd (location awareness)");
    }

    /** Stage 2: Learn 'ls' */
    private void stage2() {
        IO.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        IO.println("📜 Stage 2 — Sight of the Squire");
        IO.println("A glowing orb hovers beside you...");
        IO.println("\"Now look around you, Squire. The dungeon hides much.\"");
        IO.println("➡️   Type **ls** to reveal what lies in this chamber.");
        IO.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        waitForStageCommand("ls");
//        player.remember("Learned ls (item awareness)");
    }

    /** Stage 3: Learn 'tree' */
    private void stage3() {
        IO.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        IO.println("📜 Stage 3 — Map of the Training Grounds");
        IO.println("A parchment unfolds in front of you.");
        IO.println("\"Before you enter deeper floors, learn to **visualize** the rooms.\"");
        IO.println("➡️   Type **tree** to inspect the room structure.");
        IO.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        waitForStageCommand("tree");
//        player.remember("Learned tree (map awareness)");
    }


    /**
     * Waits for the correct command for this stage.
     * Any other input will be rejected with a message.
     * Logs the player's command memory.
     */
    private void waitForStageCommand(String expectedCommand) {
        String input;
        while (true) {
            System.out.print(">> ");
            input = IO.readln().trim();

            // Log command to player's memory
//            player.addCommandHistory(input);

            if (!input.equals(expectedCommand)) {
                IO.println("❌ The spirits whisper: \"That is not the command you were meant to use.\"");
                IO.println("💡 Try again with: **" + expectedCommand + "**");
                continue;
            }

            // Execute the command safely in the sandbox and show output
            String output = sandbox.safeExecute(input);
            IO.println("\n📤 Command Output:");
            IO.println(output);

            IO.println("✅ Stage complete!\n");
            break;
        }
    }

    @Override
    public void play() {
        IO.println("\n🏅 You are now a Squire — the lowest but bravest rank of Terminal Knights.");
        IO.println("Your training begins...\n");

        stage1();
        stage2();
        stage3();

        IO.println("🎉 You have mastered the fundamentals, young Squire!");
        IO.println("You feel a surge of confidence as you prepare for Level 2...\n");
    }

    @Override
    public String getDescription() {
        return "Level 1 — Squire (Navigation Training)";
    }
}
