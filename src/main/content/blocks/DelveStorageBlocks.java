package main.content.blocks;

import main.blocks.core.DelveCoreBlock;
import main.content.DelveItems;
import main.items.DelveItem;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class DelveStorageBlocks {

    public static DelveCoreBlock warpCore;

    public static void load(){
        warpCore = new DelveCoreBlock("warp-core"){{
            requirements(Category.effect, ItemStack.with(DelveItems.crystalisedCopper, 500, DelveItems.crystalisedIron, 800));
            size = 3;
            health = 800;
            unitType = UnitTypes.alpha;
            itemCapacity = 4000;
        }};
    }
}
