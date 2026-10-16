package main.items;

import arc.func.Prov;
import arc.struct.ObjectFloatMap;
import arc.util.Log;
import main.data.WorldModifyable;
import main.items.types.DelveItem;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.type.Item;

import java.util.HashMap;

public class ItemConstructors {

    public static Inventory internalInventory = new Inventory(-1);

    //Entries for the item states
    public static HashMap<String, BaseEntry> idMap = new HashMap<>();

    //Get a new item state instantiated by the ascociated entry
    public static ItemState get(String id){
        return idMap.get(id).get(Vars.world);
    }

    public static WorldModifyable<Prov<ItemState>> constructor(String id){
        return idMap.get(id).constructor;
    }
    public static BaseEntry getEntry(String id){
        return idMap.get(id);
    }

    //Put an id to an entry
    public static BaseEntry put(String id, BaseEntry entry){
        idMap.put(id, entry);
        return entry;
    }

    //Get all entries to reset their current state
    public static void reset(){
        idMap.forEach((id, entry) -> {
            entry.constructor.reset();
        });
    }

    public abstract static class BaseEntry<T extends ItemState>{

        //Used to reset the current state.
        public WorldModifyable<Prov<T>> constructor;

        public abstract ItemState get(World context);

        @Override
        public String toString() {
            return "Constructor: " + constructor;
        }
    }

    public static class VanillaEntry extends BaseEntry<ItemState>{
        public VanillaEntry(Item item){
            constructor = new WorldModifyable<>(() -> new ItemState(item, DelveItem.defaultSize));
        }

        @Override
        public ItemState get(World context) {
            return constructor.getModified(Vars.world).get();
        }
    }

    public static class ConstructorEntry extends BaseEntry<DelveItem.UpdateItemState> {

        public ConstructorEntry(DelveItem parrent, Prov<DelveItem.UpdateItemState> prov){
            constructor = new WorldModifyable<>(prov);
            type = new WorldModifyable<>(parrent);
            size = new WorldModifyable<>(parrent.size);

            mass = new WorldModifyable<>(parrent.mass);
            baseMass = new WorldModifyable<>(parrent.mass);

            volume = new WorldModifyable<>(parrent.volume);
            discrete = new WorldModifyable<>(parrent.discrete);

            data = new WorldModifyable<>(null);
            attributes = new WorldModifyable<>(ItemAttributes.get(parrent));
        }

        public WorldModifyable<DelveItem> type;

        public WorldModifyable<Float> size;
        public WorldModifyable<Float> mass;

        //Note: Only modify base mass if you know how this shit works. Seriously. Im leaving it open as an option for people to fuck with, but you BETTER know what you're fucking with.
        public WorldModifyable<Float> baseMass;
        public WorldModifyable<Float> volume;

        public WorldModifyable<ObjectFloatMap<String>> data;

        public WorldModifyable<ItemAttributes.AttributeData> attributes;

        public WorldModifyable<Boolean> discrete;

        @Override
        public DelveItem.UpdateItemState get(World context) {
            DelveItem.UpdateItemState state = constructor.getModified(context).get();

            state.type.setBase(type.getModified(context));
            state.size.setBase(size.getModified(context));
            state.mass.setBase(mass.getModified(context));

            state.baseMass.setBase(baseMass.getModified(context));

            state.volume.setBase(volume.getModified(context));
            state.data = data.getModified(context);
            state.attributes = attributes.getModified(context);
            state.discrete.setBase(discrete.getModified(context));

            return state;
        }
    }
}