package main;

import arc.util.*;
import main.blocks.WallBlaster;
import main.content.DelveAspects;
import main.content.DelveItems;
import main.content.blocks.DelveEnvBlocks;
import main.content.blocks.DelveProdBlocks;
import main.content.DelveUnits;
import main.content.modifiers.VanillaCostModifier;
import mindustry.content.Blocks;
import mindustry.mod.*;
import mindustry.world.blocks.environment.StaticTree;
import mindustry.world.blocks.environment.StaticWall;

public class DelveMod extends Mod{

    public DelveMod(){

    }

    @Override
    public void loadContent(){

        WallBlaster.map.putAll(Blocks.duneWall, Blocks.stoneWall,
                Blocks.stoneWall, Blocks.sandWall,
                Blocks.sandWall, Blocks.air);

        WallBlaster.map.putAll(Blocks.iceWall, Blocks.snowWall,
                Blocks.snowWall, Blocks.air);

        new StaticWall("glacier-cube"){{
            variants = 2;
        }};
        new StaticTree("glacier-slab"){{
            variants = 0;
        }};

        DelveUnits.load();
        DelveAspects.load();
        DelveItems.load();
        DelveProdBlocks.load();
        DelveEnvBlocks.load();

        VanillaCostModifier.load();

        Log.info("FUCK YOU EGGMAN IM GOING TO COUNTER-PISS ON THE MOOOOOOOOOOON");
        //I touched intelij today :D
    }

}
