package main.blocks;

import arc.graphics.g2d.Fill;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.util.Tmp;
import main.blocks.environment.DefaultOreBlock;
import main.blocks.environment.ItemStatePile;
import main.blocks.environment.OreBlock;
import main.content.blocks.DelveEnvBlocks;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.StaticWall;

public class WallBlaster extends Block {

    public static ObjectMap<Block, Item> itemDropMap = new ObjectMap<>();

    public float reload, range;

    public WallBlaster(String name) {
        super(name);
        update = true;

        reload = 60;
        range = 9;

        rotate = true;
        quickRotate = true;

    }

    public class BlasterBuild extends Building{
        public float time;

        static Tile lastValid;
        static Block replacement;

        @Override
        public boolean damaged() {
            return super.damaged();
        }

        @Override
        public void updateTile() {

            super.updateTile();

            time += edelta();

            if(time >= reload){
                time = 0;

                Point2 dir = Geometry.d4(rotation);

                lastValid = null;

                for(int i = 0; i < range; i++){
                    Tmp.v1.set(x, y).add(dir.x * i * Vars.tilesize, dir.y * i * Vars.tilesize);
                    Tile t = Vars.world.tileWorld(Tmp.v1.x, Tmp.v1.y);

                    Fx.smoke.at(t);

                    if(t.solid()) {

                        if(lastValid == null || lastValid.block() != Blocks.air) break;

                        //Switch static walls to ore block
                        if(t.build == null && t.block().solid){
                            Block wall = t.block();
                            t.setBlock(DelveEnvBlocks.oreBlock);
                            ((DefaultOreBlock.DefaultOreBuild) t.build).parrentBlock = wall;

                            //TODO: Switch to derelict when allowDerelictRepair:false actually works as intended
                            t.build.team = Vars.state.rules.defaultTeam;
                        };

                        if(t.build != null) t.build.damage(20);
                        break;
                    }
                    lastValid = t;
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            Tmp.v1.trns(rotation * 90, Vars.tilesize * 2);
            Fill.rect(x + Tmp.v1.x, y + Tmp.v1.y, 4, 4);
        }
    }
}
