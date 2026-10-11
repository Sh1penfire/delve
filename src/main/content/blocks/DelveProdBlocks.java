package main.content.blocks;

import main.blocks.Beamer;
import main.blocks.Vacuum;
import main.blocks.WallBlaster;
import main.blocks.crafters.RandomDumperBlock;
import main.blocks.crafters.RecipeCrafter;
import main.blocks.distrib.DelveConveyorBlock;
import main.content.DelveAspects;
import main.content.DelveItems;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class DelveProdBlocks {
    public static Block blaster;

    public static void load(){
        blaster = new WallBlaster("blaster"){{
            requirements(Category.production, ItemStack.with(DelveItems.crystalisedIron, 10, DelveItems.crystalisedCopper, 10));
            consumePower(0.25f);
        }};
    }
}
