package main.items;

import main.content.DelveItems;

public abstract class ItemFunction {

    public static ItemFunction
            crushing = new CrushingItemFunction();

    public abstract ItemState process(ItemState input);

    public static class CrushingItemFunction extends ItemFunction{

        @Override
        public ItemState process(ItemState input) {
            ItemState output = input;

            if(input.type.getBase() == DelveItems.malachite){
                output = ItemConstructors.get(DelveItems.azuritePowder.name);
            }
            if(input.type.getBase() == DelveItems.stone){
                output = ItemConstructors.get(DelveItems.stoneDust.name);
            }
            output.mass.setBase(input.mass.getModified(input.inventory));
            output.volume.setBase(input.volume.getModified(input.inventory));

            return output;
        }
    }
}
