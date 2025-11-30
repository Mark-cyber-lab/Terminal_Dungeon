package core.items;

import core.storage.Storage;

public class ObtainableItem extends InventoryItem {

    protected final Storage storage;

    public ObtainableItem(String label, String id, Storage storage) {
        super(label, id);
        this.storage = storage;
    }

    public Storage getStorage() {
        return storage;
    }

    @Override
    public String[] retrieve() throws Exception {
        throw new Exception("Obtainable items content cannot be retrieved.");
    }

    // remove item from storage
    public void discard() {
        storage.deleteItem();
    }

    @Override
    public String toString() {
        return getLabel() + " (" + getId() + ") -> " + storage.getItemPath();
    }
}
