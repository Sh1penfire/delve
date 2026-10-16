package main.items.types;

import main.items.ItemAttributes;

public class AttributeItem extends DelveItem{
    public AttributeItem(String name, ItemAttributes.ItemAttribute attribute) {
        super(name);
        ItemAttributes.register(this).put(attribute, 1);
        ItemAttributes.pureMap.put(attribute.name, this);
    }
}
