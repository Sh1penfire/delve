package main.type;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.blocks.interfaces.ItemStateHolder;
import main.data.InventoryModifyable;
import main.items.Inventory;
import mindustry.io.TypeIO;
import mindustry.type.Item;

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
     * Size on conveyor belts/inventories decided dynamically based on weight
     */

    public InventoryModifyable<Float> size = new InventoryModifyable<>(1f);

    /**
     * Mass of the item. Measured in "u" for unit.
     */

    public InventoryModifyable<Float> mass = new InventoryModifyable<>(1f);

    /**
     * How much physical space an item takes up. Measured in v
     * Used instead of stack size to determine how much of an item should fit in a container
     */
    public InventoryModifyable<Float> volume = new InventoryModifyable<>(1f);

    //Position in the world
    public float x, y;

    //Data specific to this item that it carries
    public ObjectFloatMap<String> data;

    //Attributes this item carries
    //Used to understand the composition of an item
    public ItemAttributes.AttributeData attributes;

    public ItemState(Item item, float defaultSize) {
        type = new InventoryModifyable<>(item);
        size.setBase(defaultSize);
    }

    public ItemState(){}

    //Attempt to combine the two states. Return if the other state should be consumed
    public boolean combine(ItemState other){
        if(other.type == this.type) {
            float totalVolume = this.volume.getBase() + other.volume.getBase();

            //Remove the condition required to bring this item's condition up to 100%
            other.volume.setBase(1 - volume.getBase());

            this.volume.setBase(Mathf.clamp(totalVolume, 0, 1));
            return true;
        }
        return false;
    }

    public boolean canCombine(ItemState other){
        return type == other.type;
    }

    public void update(){};

    public void draw(){
        Draw.rect(type.getBase().uiIcon, x, y);
    };

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

    public void read(Reads read){
        type.setBase(TypeIO.readItem(read));
        x = read.f();
        y = read.f();
        size.setBase(read.f());
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
        write.f(size.getBase());

        write.bool(data != null);
        if(data != null) {
            write.i(data.size);
            data.each(d -> {
                write.str(d.key);
                write.f(d.value);
            });
        }
    }

    @Override
    public ItemState clone() {
        try {
            ItemState clone = (ItemState) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
