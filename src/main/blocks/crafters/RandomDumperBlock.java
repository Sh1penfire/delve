package main.blocks.crafters;

import arc.math.Mathf;
import arc.math.Rand;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;

public class RandomDumperBlock extends Block {

    public RandomDumperBlock(String name) {
        super(name);
        update = true;
        hasItems = true;
    }

    public static Rand rand = new Rand();

    public class RandomDumperBuild extends Building{

        protected int[] weights = new int[Vars.content.items().size];
        public int seed;
        int total;

        @Override
        public void created() {
            super.created();
            seed = tile.pos();
        }

        //Dumps items weighted towards te highest item amounts
        public boolean dumpWeighted() {
            rand.setSeed(seed);
            total = 0;

            proximity.each(b -> {

                Vars.content.items().each(item -> {
                    int amount = items.get(item);
                    if(amount == 0 || !b.acceptItem(this, item) || !canDump(b, item)) return;
                    weights[item.id] = amount;
                    total += amount;
                });
                if(total == 0) return;
                int number = rand.nextInt(0, total);

                for(int i = 0; i < weights.length; i++){
                    number -= weights[i];
                    if(number < 0){
                        dump(Vars.content.item(i));
                        seed += items.total();
                        break;
                    }
                }

            });

            return super.dump();
        }

        @Override
        public void updateTile() {
            if(items.total() > 0) dumpWeighted();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(seed);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            seed = read.i();
        }
    }
}
