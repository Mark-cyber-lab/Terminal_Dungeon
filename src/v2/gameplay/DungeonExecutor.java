package v2.gameplay;

import java.io.IOException;
import java.nio.file.*;

public class DungeonExecutor extends LinuxCommandExecutor {

    private final Path playgroundDir;
    private final Path inventoryDir;

    public DungeonExecutor(Path playgroundDir, Path inventoryDir) {
        super();
        this.playgroundDir = playgroundDir.toAbsolutePath().normalize();
        this.inventoryDir = inventoryDir.toAbsolutePath().normalize();

        try {
            if (!Files.exists(this.playgroundDir))
                Files.createDirectories(this.playgroundDir);
            if (!Files.exists(this.inventoryDir))
                Files.createDirectories(this.inventoryDir);


            if (!Files.isDirectory(this.playgroundDir))
                throw new IllegalArgumentException("Playground exists but isn't a directory");

            if (!Files.exists(this.inventoryDir))
                Files.createDirectories(this.inventoryDir);

            if (!Files.isDirectory(this.inventoryDir))
                throw new IllegalArgumentException("inventoryDir exists but isn't a directory");

            // Initial directory
            CommandContext ctx = new CommandContext();
            super.handleCd(new String[]{this.playgroundDir.toString()}, ctx);

        } catch (IOException e) {
            throw new RuntimeException("Failed to init dungeon executor", e);
        }
    }

    /** Prevent leaving playground */
    private boolean isOutsidePlayground(Path target) {
        return !target.toAbsolutePath().normalize().startsWith(playgroundDir);
    }

    /** Returns true if path is EXACTLY the inventoryDir */
    private boolean isInventoryRoot(Path p) {
        return p.toAbsolutePath().normalize().equals(inventoryDir);
    }

    /** Returns true if path resides inside inventoryDir */
    private boolean isInsideInventory(Path p) {
        Path norm = p.toAbsolutePath().normalize();
        return norm.startsWith(inventoryDir);
    }

    // -----------------------------
    // CD
    // -----------------------------
    @Override
    protected CommandResult handleCd(String[] args, CommandContext ctx) {
        if (args.length == 0)
            return new CommandResult(false, "cd: missing operand", ctx);

        Path target = getCurrentDir().resolve(args[0]).normalize();

        if (!Files.isDirectory(target))
            return new CommandResult(false, "cd: no such directory: " + args[0], ctx);

        if (isOutsidePlayground(target))
            return new CommandResult(false, "cd: cannot leave playground", ctx);

        return super.handleCd(args, ctx);
    }

    // -----------------------------
    // MV
    // -----------------------------
    @Override
    protected CommandResult handleMove(String[] args, CommandContext ctx) throws IOException {
        if (args.length < 2)
            return new CommandResult(false, "mv: missing operand", ctx);

        Path src = getCurrentDir().resolve(args[0]).normalize();
        Path dst = getCurrentDir().resolve(args[1]).normalize();

        // ---- Root Inventory Directory cannot be moved
        if (isInventoryRoot(src))
            return new CommandResult(false, "mv: cannot move inventory directory", ctx);

        // ---- Target must remain inside playground
        if (isOutsidePlayground(src) || isOutsidePlayground(dst))
            return new CommandResult(false, "mv: cannot move files outside playground", ctx);

        // ---- If moving to a directory, append filename
        if (Files.exists(dst) && Files.isDirectory(dst)) {
            dst = dst.resolve(src.getFileName());
        }

        return super.handleMove(new String[]{src.toString(), dst.toString()}, ctx);
    }

    // -----------------------------
    // DELETE PROTECTION
    // -----------------------------
    @Override
    public CommandResult execute(String input) {
        String[] parts = input.trim().split("\\s+");
        String cmd = parts[0];

        if(cmd.equals("done")) {
            CommandContext ctx = new CommandContext();
            ctx.command = "done";
            return new CommandResult(true, null, ctx);
        }

        // rm protection
        if (cmd.equals("rm")) {
            for (String arg : parts) {

                if (arg.equals("-r")) continue;

                Path target = getCurrentDir().resolve(arg).normalize();

                // Inventory root cannot be deleted
                if (isInventoryRoot(target)) {
                    CommandContext ctx = new CommandContext();
                    return new CommandResult(false, "rm: cannot delete the inventory directory", ctx);
                }

                // Allow deleting contents of inventoryDir
                if (isOutsidePlayground(target)) {
                    CommandContext ctx = new CommandContext();
                    return new CommandResult(false, "rm: cannot delete outside playground", ctx);
                }
            }
        }

        // mkdir protection (cannot create outside)
        if (cmd.equals("mkdir")) {
            for (String arg : parts) {
                Path target = getCurrentDir().resolve(arg).normalize();
                if (isOutsidePlayground(target)) {
                    CommandContext ctx = new CommandContext();
                    return new CommandResult(false, "mkdir: cannot create directory outside playground", ctx);
                }
            }
        }

        return super.execute(input);
    }

    public CommandResult executeStrict(String expectedCommand) {
        while (true) {
            System.out.print(">> ");
            String input = IO.readln().trim();
            if(input.equals("done")) {
                return new CommandResult(true, null, new CommandContext());
            }

            if (input.equals(expectedCommand)) {
                CommandResult result = execute(expectedCommand);

                if(result.isSuccess()) return result;
            } else {
                // Inform the caller – we return a non-successful result and loop continues
                System.out.println("The spirits whisper: \"That is not the command you were meant to use.\"");
                System.out.println("Try again with: **" + expectedCommand + "**");
            }
        }
    }

}
