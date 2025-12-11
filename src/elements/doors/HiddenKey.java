package elements.doors;

import java.nio.file.Path;

public class HiddenKey {

    private final String keyName;
    private final String keyContent;

    // Optional: expected folder (if you want stricter logic)
    private final Path expectedFolder;

    public HiddenKey(String keyName, String keyContent, Path expectedFolder) {
        this.keyName = keyName;
        this.keyContent = keyContent;
        this.expectedFolder = expectedFolder;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getKeyContent() {
        return keyContent;
    }

    public Path getExpectedFolder() {
        return expectedFolder;
    }
}
