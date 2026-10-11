package main.content.blocks;

import arc.graphics.Pixmap;
import main.blocks.crafters.EnergyFieldNode;
import main.content.DelveItems;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.power.PowerNode;

public class DelvePowerBlocks {

    public static PowerNode node, relay;

    public static void load(){
        node = new EnergyFieldNode("collector"){{
            requirements(Category.power, ItemStack.with(DelveItems.crystalisedCopper, 5));
            size = 1;
            maxNodes = 8;
            laserRange = 6;
            fieldRange = 4;
        }};

        relay = new PowerNode("relay"){{
            requirements(Category.power, ItemStack.with(DelveItems.crystalisedCopper, 20));
            size = 1;
            maxNodes = 2;
            laserRange = 20;
        }};
    }
}
