package main.items;

import arc.graphics.Color;
import main.type.ItemAttributes;

public class AttributeItem extends DelveItem{
    public AttributeItem(String name, ItemAttributes.ItemAttribute attribute) {
        super(name);
        ItemAttributes.register(this).put(attribute, 1);
        ItemAttributes.pureMap.put(attribute.name, this);
    }
}
