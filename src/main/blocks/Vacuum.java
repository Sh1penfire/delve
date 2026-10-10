package main.blocks;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import main.DelveGeometry;
import main.blocks.environment.ItemPile;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.world.Block;

//Sucks all item piles in range into itself
public class Vacuum extends Block {

    public Vacuum(String name) {
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

    public class VacuumBuild extends Building{
        public float reloadCounter;

        @Override
        public void update() {
            super.update();
            dump();
            reloadCounter += edelta();

            if(reloadCounter >= reload){
                reloadCounter -= reload;

                Point2 offset = Geometry.d4(rotation);
                int trueOffset = 1 - sizeOffset;

                DelveGeometry.cone(tile.x + offset.x * trueOffset, tile.y + offset.y * trueOffset, rotation, range, (x, y) -> {
                    Fx.explosion.at(x * Vars.tilesize, y * Vars.tilesize);

                    Building b = Vars.world.build(x, y);
                    if (b instanceof ItemPile.ItemPileBuild) {

                        float total = b.items.total();
                        b.items.each((item, amount) -> {
                            float weight = ((float) amount) / total;

                            int itemsTakenOff = (int) Math.min(Math.ceil(strength * weight), amount);

                            b.removeStack(item, itemsTakenOff);
                            ItemPile.dumpItems(tile.x - offset.x * trueOffset, tile.y - offset.y * trueOffset, new ItemStack(item, itemsTakenOff));
                        });
                    }
                });
            }
        }
    }
}
