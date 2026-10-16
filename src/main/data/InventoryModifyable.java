package main.data;

import main.items.Inventory;
import mindustry.core.World;

//Uses the current inventory and its world its in as the context
public class InventoryModifyable<T> extends Modifyable<Inventory, T> {

    public InventoryModifyable(T value) {
        super(value);
    }

}
