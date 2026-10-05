package main.content.blocks;

import main.blocks.Beamer;
import main.blocks.Vacuum;
import main.blocks.WallBlaster;
import main.blocks.crafters.RandomDumperBlock;
import main.blocks.crafters.RecipeCrafter;
import main.blocks.distrib.DelveGraphBlock;
import main.content.DelveAspects;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class DelveProdBlocks {
    public static Block blaster, vacuum, beamer, gasKiln;

    public static DelveGraphBlock graph;

    public static void load(){

        blaster = new WallBlaster("blaster"){{
            requirements(Category.production, ItemStack.with());
            consumeLiquid(DelveAspects.boundAspect, 10f/60);
        }};

        vacuum = new Vacuum("vacuum"){{
            requirements(Category.production, ItemStack.with());
            size = 3;
            hasLiquids = true;
            consumeLiquid(DelveAspects.boundAspect, 30f/60);
        }};

        beamer = new Beamer("beamer"){{
            requirements(Category.production, ItemStack.with());
            size = 1;
        }};

        gasKiln = new RecipeCrafter("gas-kiln"){{
            requirements(Category.production, ItemStack.with());
            size = 2;
        }};

        RandomDumperBlock dumpie = new RandomDumperBlock("dumpie :D"){{
            requirements(Category.production, ItemStack.with());
            size = 2;
        }};

        graph = new DelveGraphBlock("graph-block"){{
            requirements(Category.production, ItemStack.with());
            size = 1;
        }};
    }

}
