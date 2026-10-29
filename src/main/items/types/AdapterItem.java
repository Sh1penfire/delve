package main.items.types;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.type.Item;

public class AdapterItem extends Item implements ItemType {

    public AdapterItem(String name) {
        super(name);
    }

    public AdapterItem(String name, Color color) {
        super(name, color);
    }

    @Override
    public void update(ItemState state) {

    }

    @Override
    public void draw(ItemState state, float x, float y, float width, float height, float originX, float originY, float rotation) {
        Item item = state.data.get("parent-item") instanceof Item target ? target : this;
        Draw.rect(item.uiIcon, x, y, width, height, originX, originY, rotation);
    }

    @Override
    public Item item(ItemState state) {
        return this;
    }

    @Override
    public String localizedName(ItemState state) {
        Item item = state.data.get("parent-item") instanceof Item target ? target : this;
        return item.localizedName;
    }
}
