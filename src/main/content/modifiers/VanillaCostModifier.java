package main.content.modifiers;

import main.content.DelveItems;
import mindustry.content.Blocks;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class VanillaCostModifier {
    public static void load(){
        Blocks.boulder.instantDeconstruct = false;
        Blocks.boulder.buildTime = 30;
        Blocks.boulder.requirements(Category.effect, ItemStack.with(DelveItems.stone, 5));
        Blocks.boulder.solid = true;
        Blocks.boulder.replaceable = false;
    }
}
