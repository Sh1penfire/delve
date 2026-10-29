package main.items;

import arc.graphics.g2d.Draw;
import arc.struct.*;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.blocks.interfaces.ItemStateHolder;
import main.data.InventoryModifyable;
import main.items.types.ItemType;
import mindustry.entities.abilities.Ability;
import mindustry.io.TypeIO;
import mindustry.type.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * An item state. Only stores info, not responsible for updating it. <br>
 * Classes implementing {@link ItemType} are responsible for updating and drawing <br>
 * {@link main.items.types.AdapterItem} is used as the type for when there isn't a parent item type found.
 */
public class ItemState implements Cloneable{

    /**
     * The type of item this is, used mainly for drawing and crafting with specific material types
     */
    public InventoryModifyable<ItemType> type;

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

    //Numerical data specific to this item that it carries that doesn't fall into the existing fields. Used specifically for floats.
    public ObjectFloatMap<String> floatData = new ObjectFloatMap<>();

    //The tags this item carries with it. Ex: crystalline, ingot, etc...
    private final ObjectSet<String> tags = new ObjectSet<>();

    //Generalised data map. Used for non float and tag values.
    //Ex: Internal inventories, items,
    public ObjectMap<String, Object> data = new ObjectMap<>();


    //Attributes this item carries
    //Used to understand the composition of an item
    //Null if this item has no attributes
    @Nullable
    public ItemAttributes.AttributeData attributes;

    public ItemState(ItemType item) {
        type = new InventoryModifyable<>(item);
        volume.setBase(1f);
        mass.setBase(1f);
    }

    public void draw(){
        type.getModified(inventory).draw(this, x, y);
    }

    public void update(){
        type.getModified(inventory).update(this);
    }

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
        type.setBase(ItemConstructors.typeMap.get(TypeIO.readItem(read).name));
        x = read.f();
        y = read.f();
        volume.setBase(read.f());
        boolean customData = read.bool();

        if(customData){
            floatData = new ObjectFloatMap<>();
            int dataSize = read.i();

            for (int i = 0; i < dataSize; i++) {
                floatData.put(read.str(), read.f());
            }
        }
    }

    public void write(Writes write){
        TypeIO.writeItem(write, type.getBase().item(this));
        write.f(x);
        write.f(y);
        write.f(volume.getBase());

        write.bool(floatData != null);
        if(floatData != null) {
            write.i(floatData.size);
            floatData.each(d -> {
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
        if(floatData != null) {
            state.floatData = floatData.copy();
        }

        return state;
    }
}
