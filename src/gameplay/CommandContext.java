package gameplay;

import java.nio.file.Path;
import java.util.*;

public class CommandContext {
    public Path startDir;
    public Path endDir;
    public String command;
    public List<Path> created = new ArrayList<>();
    public List<Path> deleted = new ArrayList<>();
    public List<Path> attemptedDelete = new ArrayList<>();
    public Map<Path, Path> renamed = new HashMap<>(); // old -> new
    public List<Path> modified = new ArrayList<>(); // files whose content changed
    public Path read;

    public long executionTimeMs;
}
