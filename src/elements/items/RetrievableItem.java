package elements.items;

import storage.Storage;

public class RetrievableItem  extends ObtainableItem {
    public RetrievableItem(String label, String id, Storage storage) {
        super(label, id, storage);
    }

    @Override
    public String[] retrieve() {
        return storage.retrieveItem();
    }
}
