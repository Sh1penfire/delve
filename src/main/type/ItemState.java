package main.type;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.struct.StringMap;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.blocks.interfaces.ItemStateHolder;
import mindustry.io.TypeIO;
import mindustry.type.Item;

public class ItemState {

    //Type of item, used for ui and such
    public Item type;

    public ItemStateHolder parrent;

    //Size on conveyor belts
    public float size;

    //Position in the world
    public float x, y;

    public ObjectFloatMap<String> data;

    public ItemAttributes.AttributeData attributes;

    //What state the item is in from 0% to 100% condition.
    public float condition = 1;

    public ItemState(Item item, float defaultSize) {
        type = item;
        size = defaultSize;
    }

    public ItemState(float defaultSize) {
        size = defaultSize;
    }

    //Attempt to combine the two states. Return if the other state should be consumed
    public void combine(ItemState other){
        if(other.type == this.type) {
            float newCondition = this.condition + other.condition;

            //Remove the condition required to bring this item's condition up to 100%
            other.condition -= 1 - this.condition;

            this.condition = Mathf.clamp(newCondition, 0, 1);

        }
    }

    public void tweakCondition(float offset){
        condition += offset;
        if(condition == 0) parrent.removeState(this);
    }

    public boolean canCombine(ItemState other){
        return type == other.type;
    }

    public void update(){};

    public void draw(){
        Draw.rect(type.uiIcon, x, y);
    };

    public String localizedName(){
        return type.localizedName;
    }

    public void read(Reads read){
        type = TypeIO.readItem(read);
        x = read.f();
        y = read.f();
        size = read.f();
        boolean customData = read.bool();

        if(customData){
            data = new ObjectFloatMap<>();
            int dataSize = read.i();

            for (int i = 0; i < dataSize; i++) {
                data.put(read.str(), read.f());
            }
        }
        else data = ItemAttributes.get(type).map;
    }

    public void write(Writes write){
        TypeIO.writeItem(write, type);
        write.f(x);
        write.f(y);
        write.f(size);

        write.bool(data != null);
        if(data != null) {
            write.i(data.size);
            data.each(d -> {
                write.str(d.key);
                write.f(d.value);
            });
        }
    }
}
