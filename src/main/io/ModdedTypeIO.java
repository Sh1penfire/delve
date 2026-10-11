package main.io;

import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.items.ItemEntryIDMap;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class ModdedTypeIO {
    public static Seq<ItemState> readStates(Reads read){
        Seq<ItemState> states = new Seq<>();
        int size = read.i();

        for(int i = 0; i < size; i++){
            ItemState entry = ItemEntryIDMap.getState(Vars.content.item(read.i()));
            entry.read(read);
            states.add(entry);
        }

        return states;
    };

    public static void writeStates(Writes writes, Seq<ItemState> states){
        writes.i(states.size);
        states.each(s -> {
            writes.i(s.type.id);
            s.write(writes);
        });
    }
}
