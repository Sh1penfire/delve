package main.items;

import arc.func.Cons;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import main.type.ItemState;
import mindustry.core.World;
import mindustry.world.Block;


public class Inventory {
    //The max capacity for this inventory
    public float capacity = 0;

    //The total size of this inventory.
    public float size = 0;

    //The total weight of this inventory.
    public float weight = 0;

    //Whether to allow combining behaviour
    public boolean combineDifferent = false;

    //Whether to allow combining the same type of item
    public boolean combineSame = true;

    public ItemState topState;

    public Seq<ItemState> states = new Seq<>();

    //World this inventory is in. If it is null, then this inventory isn't added to an entity.
    public @Nullable World world;

    public boolean isAdded(){
        return world != null;
    }

    public ItemState peek(){
        if(states.isEmpty()) return null;
        return states.peek();
    }

    public ItemState bottom(){
        return states.isEmpty() ? null : states.get(0);
    }

    public void each(Cons<ItemState> cons){
        states.each(cons);
    }

    public Inventory(float capacity){
        this.capacity = capacity;
    }

    public boolean isEmpty(){
        return states.isEmpty();
    }

    //How much room there is to accept items with
    public float roomFor(ItemState state){
        if(capacity == -1) return state.size.getModified(this) + 1;

        return capacity - size;
    }

    public void add(ItemState state){
        if(topState != null){
            float startingWeight = topState.mass.getModified(this);
            boolean shouldRemove = topState != null && topState.combine(state);
            if(shouldRemove){
                weight += startingWeight - topState.mass.getModified(this);
                return;
            }
        }

        states.add(state);
        weight += state.mass.getModified(this);
        size += state.size.getModified(this);
        state.inventory = this;
    }

    public void remove(ItemState state){
        weight -= state.mass.getModified(this);
        size -= state.size.getModified(this);
        states.remove(state);
        state.inventory = null;
    }
}
