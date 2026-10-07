package main.items;

import arc.func.Prov;
import arc.struct.IntMap;
import main.graphs.ConveyorGraph;
import mindustry.type.Item;

public class ItemEntryIDMap {
    public static IntMap<Prov<ConveyorGraph.ConveyorEntry>> idMap = new IntMap<>();

    public static ConveyorGraph.ConveyorEntry getEntry(Item item){
        Prov<ConveyorGraph.ConveyorEntry> cons = idMap.get(item.id);
        if(cons == null) {
            putDefault(item);
            cons = idMap.get(item.id);
        }
        return cons.get();
    }

    public static void put(Item item, Prov<ConveyorGraph.ConveyorEntry> entry){
        idMap.put(item.id, entry);
    }

    public static void putDefault(Item item){
        idMap.put(item.id, () -> new ConveyorGraph.ConveyorEntry(item, DelveItem.defaultSize));
    }
}
