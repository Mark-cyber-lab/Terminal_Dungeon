import core.levels.Level1_Squire;
import engine.Sandbox;
import core.*;
import core.levels.Level;

import java.util.ArrayList;
import java.util.List;

public class NewStage {

    private static final String SANDBOX_ROOT = "./sandbox";
    private final Player player = new Player();

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void upStage() {
        Sandbox sandbox = new Sandbox(SANDBOX_ROOT);

        IO.println("==============================================");
        IO.println("        ⚔️  WELCOME TO TERMINAL DUNGEON  ⚔️");
        IO.println("==============================================");
        sleep(500);
        IO.println("You awaken inside a mysterious cavern...");
        sleep(700);
        IO.println("A whisper echoes through the stone walls:");
        sleep(900);
        IO.println("\"Only those who command the Terminal may survive.\"");
        IO.println();
        sleep(1000);

        // Available levels
        List<Level> levels = new ArrayList<>();
        levels.add(new Level1_Squire(sandbox, player));

        boolean retry = true;

        while (retry) {

            IO.println("🏰 A new dungeon cycle begins...");
            sleep(700);
            IO.println("Your current rank: " + player.getRankName());
            IO.println("----------------------------------------------");
            sleep(700);

            for (Level level : levels) {

                IO.println();
                IO.println("==============================================");
                IO.println("          🛡️ ENTERING LEVEL " + level.getLevelNumber());
                IO.println("==============================================");
                sleep(700);

                IO.println(level.getDescription());
                IO.println();
                sleep(900);

                IO.println("🌌 Preparing your training ground...");
                sleep(1000);

                level.setupEnvironment();
                IO.println("✨ Environment ready!");
                sleep(600);

                IO.println("The dungeon stirs as you step inside...");
                IO.println();
                sleep(900);

                // Play the level
                level.play();

                IO.println();
                IO.println("🎉 LEVEL COMPLETE!");
                sleep(600);
                IO.println("The ancient energies surge through you...");
                sleep(800);
                IO.println("You are promoted to the next rank!");
                IO.println();

                player.promoteLevel();
                sleep(500);

                IO.println("🆙 New Rank Obtained: " + player.getRankName());
                IO.println("----------------------------------------------");
                sleep(800);
            }

            IO.println();
            IO.println("🏁 You have conquered the cycle of the dungeon.");
            sleep(800);
            IO.println("Would you dare to relive the fate again?");
            sleep(600);

            String input = IO.readln("🔁 Retry the adventure? (yes/no): ").trim().toLowerCase();
            retry = input.equals("yes");

            if (retry) {
                IO.println();
                IO.println("🌙 The dungeon resets...");
                sleep(1000);
                IO.println("Your memories fade, but your spirit persists.");
                IO.println();
                sleep(900);
                // player.reset(); // optional if you plan it later
            }
        }

        IO.println();
        IO.println("==============================================");
        IO.println("          🏆 THANK YOU FOR PLAYING! 🏆");
        IO.println("==============================================");
        sleep(900);
        IO.println("Your legend will echo through the Terminal Dungeon forever.");
    }
}
