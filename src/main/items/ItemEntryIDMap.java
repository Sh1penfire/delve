package main.items;

import arc.func.Prov;
import arc.struct.IntMap;
import main.graphs.ConveyorGraph;
import main.type.ItemState;
import mindustry.type.Item;

public class ItemEntryIDMap {
    public static IntMap<Prov<ItemState>> idMap = new IntMap<>();

    public static ItemState getState(Item item){
        Prov<ItemState> cons = idMap.get(item.id);
        if(cons == null) {
            putDefault(item);
            cons = idMap.get(item.id);
        }
        return cons.get();
    }

    public static void put(Item item, Prov<ItemState> entry){
        idMap.put(item.id, entry);
    }

    public static void putDefault(Item item){
        idMap.put(item.id, () -> new ItemState(item, DelveItem.defaultSize));
    }
}
