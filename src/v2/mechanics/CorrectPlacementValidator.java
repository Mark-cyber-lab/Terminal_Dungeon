package v2.mechanics;

import v2.gameplay.CommandContext;
import v2.gameplay.CommandMiddleware;
import v2.gameplay.CommandResult;
import v2.player.Player;

import java.nio.file.Path;

public class CorrectPlacementValidator implements CommandMiddleware {

    private static final int PENALTY_DAMAGE = 1;

    private final Path targetDirectory;
    private final String targetFileName;

    private boolean correctlyPlaced;
    private Player player;

    public CorrectPlacementValidator(String targetFileName, Path targetDirectory) {
        this.targetDirectory = normalize(targetDirectory);
        this.targetFileName = targetFileName;
        this.correctlyPlaced = false;
    }

    public CorrectPlacementValidator(String targetFileName, Path targetDirectory, Boolean correctlyPlaced) {
        this.targetDirectory = normalize(targetDirectory);
        this.targetFileName = targetFileName;
        this.correctlyPlaced = correctlyPlaced;
    }

    public CorrectPlacementValidator setPlayer(Player player) {
        this.player = player;
        return this;
    }

    private void applyPenalty(String reason) {
        IO.println("[PlacementCheck] -" + PENALTY_DAMAGE + "HP (" + reason + ")");
        if (player != null) {
            player.takeDamage(PENALTY_DAMAGE);
        } else {
            IO.println("[PlacementCheck] Player instance not set!");
        }
    }

    public boolean isCorrectlyPlaced() {
        return correctlyPlaced;
    }

    // ---------- Normalization helper ----------
    private static Path normalize(Path p) {
        return p == null ? null : p.toAbsolutePath().normalize();
    }

    @Override
    public boolean before(String command, String[] args, CommandContext ctx) {

        if (!"mv".equals(command))
            return true;

        if (args.length < 2)
            return true;

        // Extract paths like in after()
        Path currentDir = normalize(ctx.startDir);
        Path sourcePath = normalize(currentDir.resolve(args[0]));
        Path destinationPath = normalize(currentDir.resolve(args[1]));

        String sourceName = sourcePath.getFileName() == null ? "" : sourcePath.getFileName().toString();
        String destName = destinationPath.getFileName() == null ? "" : destinationPath.getFileName().toString();

        // Ignore unrelated mv operations
        if (!sourceName.equals(targetFileName) && !destName.equals(targetFileName)) {
            return true;
        }

        Path destParent = destinationPath.getParent() == null ? null : normalize(destinationPath.getParent());
        boolean isFinalLocation = destParent != null && destParent.equals(targetDirectory);
        boolean isFinalName = destName.equals(targetFileName);

        // ============================================================
        //      CASE A — FILE ALREADY CORRECT, USER TRIES TO MISPLACE
        // ============================================================
        if (correctlyPlaced) {

            // A1 — Attempt to move out of correct directory
            if (!isFinalLocation) {
                applyPenalty("You attempted to remove a correctly placed file!");
                IO.println("[PlacementCheck] Move blocked.");
                return false; // BLOCK mv execution
            }

            // A2 — Attempt to rename incorrectly inside correct folder
            if (!isFinalName) {
                applyPenalty("You attempted to rename a correctly placed file incorrectly!");
                IO.println("[PlacementCheck] Rename blocked.");
                return false; // BLOCK mv execution
            }

            // If both location and name still match → allow
            return true;
        }

        return true;
    }

    @Override
    public void after(String command, String[] args, CommandContext ctx, CommandResult result) {

        if (!"mv".equals(command))
            return;

        var renamed = result.getContext().renamed;
        if (renamed.isEmpty())
            return;

        Path sourcePath = normalize(renamed.keySet().iterator().next());
        Path destinationPath = normalize(renamed.values().iterator().next());

        if (sourcePath == null || destinationPath == null)
            return;

        String sourceName = sourcePath.getFileName() == null ? "" : sourcePath.getFileName().toString();
        String destName = destinationPath.getFileName() == null ? "" : destinationPath.getFileName().toString();

        // ---------- IGNORE unrelated mv operations ----------
        if (!sourceName.equals(targetFileName) && !destName.equals(targetFileName)) {
            return;
        }

        Path sourceParent = sourcePath.getParent() == null ? null : normalize(sourcePath.getParent());
        Path destParent = destinationPath.getParent() == null ? null : normalize(destinationPath.getParent());

        boolean isFinalLocation = destParent != null && destParent.equals(targetDirectory);
        boolean isFinalName = destName.equals(targetFileName);

        // ============================================================
        //      CASE B — USER PLACING OR PREPARING TO PLACE
        // ============================================================

        // ---------- ALLOW SAME-FOLDER RENAMES ----------
        if (sourceParent != null && sourceParent.equals(destParent)) {

            // rename inside correct folder INTO the target name
            if (isFinalLocation && isFinalName) {
                correctlyPlaced = true;
                IO.println("[PlacementCheck] ✔ Correct file created in the right folder!");
            }

            // renaming but not affecting validation
            return;
        }

        // ---------- Validate final file name ----------
        if (!isFinalName) {
            applyPenalty("Wrong file placed: " + destName);
            return;
        }

        // ---------- Validate folder ----------
        if (!isFinalLocation) {
            applyPenalty("Placed in incorrect folder: " + destParent);
            return;
        }

        // ---------- Correct placement ----------
        correctlyPlaced = true;
        IO.println("[PlacementCheck] ✔ File correctly placed!");
    }
}
