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

import static main.content.blocks.DelveBlocks.basePowerConsumption;

public class DelveProdBlocks {
    public static Block blaster;

    public static RecipeCrafter crusher;

    public static void load(){
        blaster = new WallBlaster("blaster"){{
            requirements(Category.production, ItemStack.with(DelveItems.crystalisedIron, 10, DelveItems.crystalisedCopper, 10));
            consumePower(basePowerConsumption);
        }};

        crusher = new RecipeCrafter("crusher"){{
            requirements(Category.production, ItemStack.with(DelveItems.crystalisedIron, 10, DelveItems.crystalisedCopper, 10));
            consumePower(basePowerConsumption * 4);
        }};
    }
}
