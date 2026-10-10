package main.blocks;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import main.blocks.environment.ItemPile;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;

//A vacuum that requires a clear LOS to its target item pile
public class Beamer extends Block {
    public Beamer(String name) {
        super(name);
        update = true;
        rotate = true;
        quickRotate = true;
        reload = 60;
        range = 8;
        strength = 20;
    }

    public float reload;

    public int range;
    public float strength;

    public class BeamerBuild extends Building {
        public float reloadCounter;

        @Override
        public void update() {
            super.update();
            dump();
            reloadCounter += edelta();

            if(reloadCounter >= reload){
                reloadCounter -= reload;

                Point2 dir = Geometry.d4(rotation);
                int trueOffset = 1 - sizeOffset;

                for(int i = 0; i < range; i++) {
                    Tile t = Vars.world.tile(tile.x + dir.x * (i + 1), tile.y + dir.y * (i + 1));

                    if(t == null) break;

                    Building b = t.build;
                    if (b instanceof ItemPile.ItemPileBuild) {

                        float total = b.items.total();
                        b.items.each((item, amount) -> {
                            float weight = ((float) amount) / total;

                            int itemsTakenOff = (int) Math.min(Math.ceil(strength * weight), amount);

                            b.removeStack(item, itemsTakenOff);
                            ItemPile.dumpItems(tile.x - dir.x * trueOffset, tile.y - dir.y * trueOffset, new ItemStack(item, itemsTakenOff));
                        });
                    }
                }
            }
        }
    }
}
