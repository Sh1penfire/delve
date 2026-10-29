package main.items;

import arc.graphics.g2d.Draw;
import arc.struct.*;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.blocks.interfaces.ItemStateHolder;
import main.data.InventoryModifyable;
import mindustry.entities.abilities.Ability;
import mindustry.io.TypeIO;
import mindustry.type.Item;

import java.util.HashMap;
import java.util.Map;

public class ItemState implements Cloneable{

    /**
     * The type of item this is, used mainly for drawing and crafting with specific material types
     */
    public InventoryModifyable<Item> type;

    /**
     * The entity that this is being kept inside.<br>
     * Handled by ItemStateHolder(s)
     */
    public ItemStateHolder parrent;

    /**
     * The inventory this item state is in.<br>
     * Handled by Inventory(s)
     */
    public Inventory inventory;

    /**
     * Current mass of the item, which attributes scale off of. Measured in "mu" for mass unit.
     */

    public InventoryModifyable<Float> mass = new InventoryModifyable<>(1f);

    /**
     * How much physical space an item takes up. Measured in "vu" for volume unit
     * Used instead of stack size to determine how much of an item should fit in a container
     */
    public InventoryModifyable<Float> volume = new InventoryModifyable<>(1f);

    /**
    *Whether this item is allowed to combine with the same type of item
     */
    public InventoryModifyable<Boolean> discrete = new InventoryModifyable<>(true);

    //Position in the world
    public float x, y;

    //Data specific to this item that it carries
    public ObjectFloatMap<String> data;

    //The tags this item carries with it. Ex: crystalline, ingot, etc...
    private final ObjectSet<String> tags = new ObjectSet<>();

    //Attributes this item carries
    //Used to understand the composition of an item
    //Null if this item has no attributes
    @Nullable
    public ItemAttributes.AttributeData attributes;

    public ItemState(Item item) {
        type = new InventoryModifyable<>(item);
        volume.setBase(1f);
        mass.setBase(1f);
    }

    public ItemState(){}

    //Attempt to combine the two states. Return if the other state should be consumed
    public boolean combine(ItemState other){
        if(!discrete.getModified(inventory) && !other.discrete.getModified(other.inventory) && other.type.getModified(other.inventory) == this.type.getModified(other.inventory)) {
            float totalVolume = this.volume.getBase() + other.volume.getBase();
            float totalMass = this.mass.getBase() + other.mass.getBase();

            //Remove the condition required to bring this item's condition up to 100%
            volume.setBase(totalVolume);
            mass.setBase(totalMass);
            return true;
        }
        return false;
    }

    public boolean canCombine(ItemState other){
        return type.getModified(inventory) == other.type.getModified(other.inventory) && !discrete.getModified(inventory) && !other.discrete.getModified(other.inventory);
    }

    public void update(){};

    public void draw(){
        Draw.rect(type.getBase().uiIcon, x, y);
    }

    public void draw(float x, float y, float width, float height){
        Draw.rect(type.getBase().uiIcon, x, y, width, height);
    }

    public void draw(float x, float y, float width, float height, float originX, float originY, float rotation){
        Draw.rect(type.getBase().uiIcon, x, y, width, height, originX, originY, rotation);
    }

    public String localizedName(){
        return type.getBase().localizedName;
    }


    /**
     * Density of the item. Measured in u/v<br>
     * Used to determine certain properties of the item when colliding/mixing
     */
    public float baseDensity(){
        return mass.getBase()/volume.getBase();
    }

    public float modifiedDensity(Inventory inventory){
        return mass.getBase()/volume.getBase();
    }

    //If this state exists in the world and should be updated
    public boolean isAdded(){
        return inventory.isAdded();
    }

    public ObjectSet<String> getTags() {
        return tags;
    }

    public void setTags(Eachable<String> map) {
        tags.clear();
        map.each(tags::add);
    }

    public boolean hasTag(String id){
        return tags.contains(id);
    }

    public void putTag(String id){
        tags.add(id);
    }

    public void removeTag(String id){
        tags.remove(id);
    }

    public void read(Reads read){
        type.setBase(TypeIO.readItem(read));
        x = read.f();
        y = read.f();
        volume.setBase(read.f());
        boolean customData = read.bool();

        if(customData){
            data = new ObjectFloatMap<>();
            int dataSize = read.i();

            for (int i = 0; i < dataSize; i++) {
                data.put(read.str(), read.f());
            }
        }
        else data = ItemAttributes.get(type.getBase()).map;
    }

    public void write(Writes write){
        TypeIO.writeItem(write, type.getBase());
        write.f(x);
        write.f(y);
        write.f(volume.getBase());

        write.bool(data != null);
        if(data != null) {
            write.i(data.size);
            data.each(d -> {
                write.str(d.key);
                write.f(d.value);
            });
        }
    }

    public ItemState copy(){
        try{
            return clone();
        }catch(CloneNotSupportedException e){
            //I am disbrusted
            throw new RuntimeException("java sucks", e);
        }
    }

    @Override
    protected ItemState clone() throws CloneNotSupportedException {
        ItemState state = (ItemState) super.clone();
        state.type = new InventoryModifyable<>(type.getBase());
        state.mass = new InventoryModifyable<>(mass.getBase());
        state.volume = new InventoryModifyable<>(volume.getBase());
        state.discrete = new InventoryModifyable<>(discrete.getBase());
        if(data != null) {
            state.data = new ObjectFloatMap<>();
            state.data.set(data);
        }

        return state;
    }
}
