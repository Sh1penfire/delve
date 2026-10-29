package main.io;

import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.items.Inventory;
import main.items.ItemAttributes;
import main.items.ItemConstructors;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.io.TypeIO;

public class ModdedTypeIO {

    public static ItemAttributes.AttributeData readAttributes(Reads read){
        ItemAttributes.AttributeData data = new ItemAttributes.AttributeData();
        int size = read.i();
        for (int i = 0; i < size; i++) {
            data.idPut(read.str(), read.f());
        }
        return data;
    }

    public static void writeAttributes(Writes write, ItemAttributes.AttributeData data){
        write.i(data.map.size);
        data.each((attrId, value) -> {
            write.str(attrId);
            write.f(value);
        });
    }

    public static Seq<ItemState> readStates(Reads read){
        Seq<ItemState> states = new Seq<>();
        int size = read.i();

        for(int i = 0; i < size; i++){
            ItemState entry = ItemConstructors.get(Vars.content.item(read.i()).name);
            entry.read(read);
            states.add(entry);
        }

        return states;
    };

    public static void writeStates(Writes writes, Seq<ItemState> states){
        writes.i(states.size);
        states.each(s -> {
            TypeIO.writeItem(writes, s.type.getBase().item(s));
            s.write(writes);
        });
    }

    public static Inventory readInventory(Reads read){
        Inventory inventory = new Inventory(read.f());
        inventory.size = read.f();
        inventory.weight = read.f();

        Seq<ItemState> states = new Seq<>();
        int size = read.i();

        for(int i = 0; i < size; i++){
            ItemState entry = ItemConstructors.get(TypeIO.readItem(read).name);
            entry.read(read);
            states.add(entry);
        }
        inventory.states = states;

        return inventory;
    };


    public static void writeInventory(Writes writes, Inventory inventory){
        writes.f(inventory.capacity);
        writes.f(inventory.size);
        writes.f(inventory.weight);

        writes.i(inventory.states.size);
        inventory.states.each(s -> {
            TypeIO.writeItem(writes, s.type.getBase().item(s));
            s.write(writes);
        });
    }
}
