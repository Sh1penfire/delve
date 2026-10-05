package main.type;

import arc.func.Cons;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import mindustry.type.Item;

public class ItemAttributes {

    public static ObjectMap<Item, AttributeData> map = new ObjectMap<>();

    public static AttributeData register(Item item){
        AttributeData data = new AttributeData();
        map.put(item, data);
        return data;
    }

    public static AttributeData get(Item item){
        return map.get(item);
    }

    public static void eachAttribute(Item item, Attributef dataCons){
        AttributeData data = map.get(item);
        if(data == null) return;
        data.map.each((a) -> {
            dataCons.get(a.key, a.value);
        });
    }

    public static class AttributeData{
        public ObjectFloatMap<String> map = new ObjectFloatMap<>();

        public AttributeData put(ItemAttribute attribute, float amount){
            map.put(attribute.name, amount);
            return this;
        };
    }

    public static class ItemAttribute{
        public ItemAttribute(String id){
            this.name = id;
        }

        public String name;
    }

    public interface Attributef{
        void get(String attributeId, float amount);
    }

    public static ItemAttribute
    stone = new ItemAttribute("stone"),
            iron = new ItemAttribute("iron"),
            copper = new ItemAttribute("copper"),
            sekos = new ItemAttribute("sekos");
}
