package main.type;

import arc.graphics.g2d.Draw;
import arc.struct.ObjectFloatMap;
import mindustry.type.Item;

public class ItemState {

    //Type of item, used for ui and such
    public Item type;

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
}
