package core;

public class InventoryItem {
    private final String id;
    private final String label;

    public InventoryItem(String label, String id) {
        this.label = label;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
