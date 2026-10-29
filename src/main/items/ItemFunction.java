package main.items;

import arc.struct.ObjectMap;
import main.content.DelveItems;
import main.items.types.ItemType;

public abstract class ItemFunction {

    public static ItemFunction
            crushing = new CrushingItemFunction();

    public abstract ItemState process(ItemState input);

    public static class CrushingItemFunction extends ItemFunction{

        public static ObjectMap<ItemType, ItemType> crushingMap = new ObjectMap<ItemType, ItemType>();

        @Override
        public ItemState process(ItemState input) {
            ItemState output = input;

            ItemType out = crushingMap.get(input.type.getModified(input.inventory));
            if(out != null){
                output = ItemConstructors.get(out.item(input).name);
            }

            output.mass.setBase(input.mass.getModified(input.inventory));
            output.volume.setBase(input.volume.getModified(input.inventory));

            return output;
        }
    }
}
