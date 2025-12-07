package core.items;

public class Corrupted extends Shards {
    private final String correctName;
    private boolean isCorrectName = false;
    public Corrupted(String name, String id, String targetDir, String correctName) {
        super(name, id, targetDir);
        this.correctName = correctName;
    }

    public String getCorrectName() {return this.correctName;}

    public void setIsCorrectName(boolean isCorrectName) {this.isCorrectName = isCorrectName;}

    public boolean isCorrectName() {return this.isCorrectName;}
}
