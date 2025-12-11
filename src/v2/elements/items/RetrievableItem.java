package v2.elements.items;

import v2.storage.Storage;

public class RetrievableItem  extends ObtainableItem {
    public RetrievableItem(String label, String id, Storage storage) {
        super(label, id, storage);
    }

    @Override
    public String[] retrieve() {
        return storage.retrieveItem();
    }
}
