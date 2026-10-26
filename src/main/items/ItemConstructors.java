package main.items;

import arc.func.Prov;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Nullable;
import main.data.WorldModifyable;
import main.items.types.DelveItem;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.type.Item;

import java.util.HashMap;

//A list of default item constructors
public class ItemConstructors {

    public static Inventory internalInventory = new Inventory(-1);

    public static ItemData defaultData = new ItemData(){{
        volume = mass = 1;
    }};

    //Entries for the item states
    public static HashMap<String, BaseEntry> idMap = new HashMap<>();

    //Get a new item state instantiated by the ascociated entry
    public static ItemState get(String id){
        return idMap.get(id).get(Vars.world, defaultData, 1);
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

    //Data used to tell constructors what info to create items with.
    public static class ItemData{

        //The mass of the resulting item state, unmodified
        public float mass = 1;

        //The volume of the resulting item state, unmodified
        public float volume = 1;

        //Data which gets passed onto the item state
        @Nullable
        public ObjectFloatMap<String> stateData;
    }

    public abstract static class BaseEntry<T extends ItemState>{

        //Used to reset the current state.
        public WorldModifyable<Prov<T>> constructor;

        public abstract ItemState get(World context, ItemData data, float scale);

        @Override
        public String toString() {
            return "Constructor: " + constructor;
        }
    }

    public static class VanillaEntry extends BaseEntry<ItemState>{
        public VanillaEntry(Item item){
            constructor = new WorldModifyable<>(() -> new ItemState(item));
        }

        @Override
        public ItemState get(World context, ItemData data, float scale) {
            ItemState state = constructor.getModified(Vars.world).get();
            state.mass.setBase(data.mass * scale);
            state.mass.setBase(data.volume * scale);
            if(data.stateData != null) state.data.set(data.stateData);
            return state;
        }
    }

    public static class ConstructorEntry extends BaseEntry<ItemState> {

        public ConstructorEntry(DelveItem parrent, Prov<ItemState> prov){
            constructor = new WorldModifyable<>(prov);
            type = new WorldModifyable<>(parrent);

            discrete = new WorldModifyable<>(parrent.discrete);

            attributes = new WorldModifyable<>(ItemAttributes.get(parrent));
        }

        public WorldModifyable<DelveItem> type;

        public WorldModifyable<ItemAttributes.AttributeData> attributes;

        public WorldModifyable<Boolean> discrete;

        @Override
        public ItemState get(World context, ItemData data, float scale) {
            ItemState state = constructor.getModified(context).get();

            state.type.setBase(type.getModified(context));

            state.attributes = attributes.getModified(context);
            state.discrete.setBase(discrete.getModified(context));

            state.mass.setBase(data.mass * scale);
            state.volume.setBase(data.volume * scale);
            if(data.stateData != null) state.data.set(data.stateData);

            return state;
        }
    }
}