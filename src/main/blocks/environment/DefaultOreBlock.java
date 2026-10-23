package main.blocks.environment;

import arc.graphics.g2d.Draw;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.content.blocks.DelveEnvBlocks;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.io.TypeIO;
import mindustry.world.Block;

//Default ore block which uses its parrent block draw instead of its own
public class DefaultOreBlock extends OreBlock{
    public DefaultOreBlock(String name) {
        super(name);
    }

    public class DefaultOreBuild extends OreBlockBuild{

        public Block parrentBlock;

        @Override
        public Block parent() {
            return parrentBlock;
        }

        @Override
        public void placed() {
            super.placed();
            if(block == DelveEnvBlocks.oreBlock) {
                parrentBlock = Blocks.stone;
            }
            else parrentBlock = block;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            TypeIO.writeBlock(write, parrentBlock);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            parrentBlock = TypeIO.readBlock(read);
            if(healthf() == 1 && parrentBlock == null) tile.setBlock(parrentBlock);
        }
    }
}
