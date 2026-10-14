package main.io;

import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.items.Inventory;
import main.items.ItemConstructors;
import main.type.ItemState;
import mindustry.Vars;

public class ModdedTypeIO {
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
            writes.i(s.type.getBase().id);
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
            ItemState entry = ItemConstructors.get(Vars.content.item(read.i()).name);
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
            writes.i(s.type.getBase().id);
            s.write(writes);
        });
    }
}
