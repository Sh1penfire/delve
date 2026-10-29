package main.items.types;

import arc.graphics.Color;
import main.items.ItemAttributes;
import main.items.ItemFunction;
import main.items.ItemTags;
import mindustry.Vars;

public class DustItem extends DelveItem{

    public DelveItem parent;

    public DustItem(String name, DelveItem parent) {
        super(name, parent.color);
        this.parent = parent;
        tags.add(ItemTags.dust);
        ItemAttributes.register(this).set(ItemAttributes.get(parent));
        discrete = false;
    }

    @Override
    public void init() {
        super.init();
        ItemFunction.CrushingItemFunction.crushingMap.put(parent, this);
    }
}
