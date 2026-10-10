package main.type;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.struct.ObjectFloatMap;
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

    public ItemState(Item item, float defaultSize) {
        type = item;
        size = defaultSize;
    }


    public void update(){};

    public void draw(){
        Draw.rect(type.uiIcon, x, y);
    };

    public void read(Reads read){
        type = TypeIO.readItem(read);
    }

    public void write(Writes write){
        TypeIO.writeItem(write, type);
        write.f(x);
        write.f(y);
        write.f(size);
        data.each(d -> {
            write.str(d.key);
            write.f(d.value);
        });
    }
}
