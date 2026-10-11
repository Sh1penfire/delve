package main.blocks.environment;

import arc.graphics.g2d.Draw;
import mindustry.graphics.Layer;

//Default ore block which uses its parrent block draw instead of its own
public class DefaultOreBlock extends OreBlock{
    public DefaultOreBlock(String name) {
        super(name);
    }

    public class DefaultOreBuild extends OreBlockBuild{

        //Literally all of the shit below is visual until the read/write
        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);
            if(parrent != block) {
                parrent.drawBase(tile);
                return;
            }
            super.draw();
        }
    }
}
