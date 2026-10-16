package main.content.blocks;

import main.blocks.debug.LightWallAlt;
import main.blocks.debug.LightWallBlock;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;

public class DelveBlocks {
    public static float basePowerConsumption = 10/60f;


    public static LightWallBlock wall, wall2;

    public static void load(){
        wall = new LightWallBlock("wall") {{
            requirements(Category.effect, ItemStack.with());
            buildVisibility = BuildVisibility.sandboxOnly;
        }};

        wall2 = new LightWallAlt("wall2"){{
            requirements(Category.effect, ItemStack.with());
            buildVisibility = BuildVisibility.sandboxOnly;
        }};
    }
}
