package main.blocks.crafters;

import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;

public class RecipeCrafter extends Block {
    public RecipeCrafter(String name) {
        super(name);
        update = true;
        rotate = true;
        quickRotate = true;
    }

    public float craftInterval = 60;

    public static ObjectFloatMap<Item> totalItems = new ObjectFloatMap<>();

    public static void add(Building build){
        build.items.each((i, amount) -> {
            float current = totalItems.get(i, 0);
            current += amount;
            totalItems.put(i, current);
        });
    }

    public static void reset(){
        totalItems.clear();
    }

    public class RecipeCrafterBuild extends Building{

        public float craftCounter = 0;

        @Override
        public void updateTile() {
            super.updateTile();

            craftCounter += edelta();
            if(craftCounter >= craftInterval){
                craftCounter -= craftInterval;

                RecipeCrafter.reset();

                proximity.each(b -> {
                    if(b.relativeTo(this) != rotation) return;

                    Fx.fire.at(b);
                    RecipeCrafter.add(b);
                });
                Log.info(totalItems);
            }

        }
    }
}
