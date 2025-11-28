package core;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private final List<InventoryItem> items = new ArrayList<>();

    // Utility: convert label → snake_case id
    private static String toSnakeCase(String input) {
        if (input == null) return "";
        return input.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toLowerCase();
    }

    // Add item WITH id
    public void add(String id, String label) {
        items.add(new InventoryItem(id, label));
    }

    // Add item WITHOUT id (auto-generate from label)
    public void add(String label) {
        String generatedId = toSnakeCase(label);
        items.add(new InventoryItem(generatedId, label));
    }

    // Count all items
    public int count() {
        return items.size();
    }

    // Count by ID
    public int countById(String id) {
        return (int) items.stream()
                .filter(item -> item.getId().equals(id))
                .count();
    }

    // Count by label
    public int countByLabel(String label) {
        return (int) items.stream()
                .filter(item -> item.getLabel().equalsIgnoreCase(label))
                .count();
    }

    // Count by ID prefix (e.g. enemy_, scroll_)
    public int countPrefix(String prefix) {
        return (int) items.stream()
                .filter(item -> item.getId().startsWith(prefix))
                .count();
    }

    public List<InventoryItem> getItems() {
        return items;
    }
}
