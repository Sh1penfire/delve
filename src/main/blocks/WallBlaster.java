package main.blocks;

import arc.graphics.g2d.Fill;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Tmp;
import main.DelveGeometry;
import main.content.blocks.DelveEnvBlocks;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;

public class WallBlaster extends Block {

    public static ObjectMap<Block, Block> map = new ObjectMap<>();

    public float reload, range;

    public WallBlaster(String name) {
        super(name);
        update = true;

        reload = 300;
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

                    if(t.solid() && !(t.build instanceof ItemPile.ItemPileBuild)) {

                        if(lastValid == null || lastValid.block() != Blocks.air) break;

                        if(!(t.build instanceof OreBlock.OreBlockBuild)){
                            Block wall = t.block();
                            t.setBlock(DelveEnvBlocks.oreBlock);
                            ((OreBlock.OreBlockBuild) t.build).parent = wall;

                            //TODO: Switch to derelict when allowDerelictRepair:false actually works as intended
                            t.build.team = Vars.state.rules.defaultTeam;
                        };

                        t.build.damage(20);
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
