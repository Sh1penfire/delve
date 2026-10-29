package main.items;

import arc.struct.IntMap;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.type.Item;

import java.util.HashMap;

public class ItemAttributes {

    static Seq<ItemAttribute> attributes = new Seq<>();

    //Setup dynamically
    static ObjectMap<String, ItemAttribute> idMap = new ObjectMap<>();

    public static ObjectMap<Item, AttributeData> map = new ObjectMap<>();

    //Map of each item attribute's id to an item
    public static ObjectMap<String, Item> pureMap = new ObjectMap<>();

    public static void register(ItemAttribute attribute){
        attributes.add(attribute);
        idMap.put(attribute.name, attribute);
    }

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

        public AttributeData(){

        }

        public void clear(){
            map.clear();
        }

        public void each(AttributeEntry cons){
            map.each(entry -> {
                cons.get(entry.key, entry.value);
            });
        }

        public float get(ItemAttribute attr, float defaultVal){
            return map.get(attr.name, defaultVal);
        }

        public float idGet(String id, float defaultVal){
            return map.get(id, defaultVal);
        }

        public void set(AttributeData data){
            map.set(data.map);
        }

        public AttributeData put(ItemAttribute attribute, float amount){
            map.put(attribute.name, amount);
            return this;
        };

        public AttributeData idPut(String attributeId, float amount){
            map.put(attributeId, amount);
            return this;
        };

        public AttributeData add(ItemAttribute attribute, float amount){
            map.put(attribute.name, map.get(attribute.name, 0) + amount);
            return this;
        };

        public AttributeData idAdd(String attribute, float amount){
            map.put(attribute, map.get(attribute, 0) + amount);
            return this;
        };

        //Add another attribute data onto this
        public AttributeData addData(AttributeData other, float scale){
            other.each((attrId, amount) -> {
                map.put(attrId, map.get(attrId, 0) + amount * scale);
            });
            return this;
        };

        public AttributeData mul(ItemAttribute attribute, float amount){
            map.put(attribute.name, map.get(attribute.name, 0) * amount);
            return this;
        };
    }

    public interface AttributeEntry{
        void get(String id, float value);
    }

    //The actual item attribute class used to create attributes
    public static class ItemAttribute{
        public ItemAttribute(String id){
            this.name = id;
            register(this);
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
        biochem = new ItemAttribute("biochem"),
        sekos = new ItemAttribute("sekos"),
        ice = new ItemAttribute("ice");
}
