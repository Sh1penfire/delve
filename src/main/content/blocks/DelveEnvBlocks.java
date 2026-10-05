package main.content.blocks;

import main.blocks.ItemPile;
import main.blocks.OreBlock;
import main.blocks.ThingBlock;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class DelveEnvBlocks {
    public static OreBlock oreBlock;
    public static ItemPile itemPile;

    public static void load(){
        oreBlock = new OreBlock("ore-block");
        itemPile = new ItemPile("item-pile");

        new ThingBlock("thingblock"){{
            requirements(Category.distribution, ItemStack.with());
        }};
    }
}
