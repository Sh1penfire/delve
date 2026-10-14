package main.items;

import main.content.DelveItems;
import main.type.ItemState;

public abstract class ItemFunction {

    public static ItemFunction
            crushing = new CrushingItemFunction();

    public abstract ItemState process(ItemState input);

    public static class CrushingItemFunction extends ItemFunction{

        @Override
        public ItemState process(ItemState input) {
            if(input.type.getBase() == DelveItems.malachite){
                return ItemConstructors.get(DelveItems.azuritePowder.name);
            }
            return input;
        }
    }
}
