package core.listeners;

import java.nio.file.Path;

public interface Blocker {
    String getName();
    boolean blocks(Path folder);
    boolean isCleared();
    void clear();
    Path getFilePath();
}