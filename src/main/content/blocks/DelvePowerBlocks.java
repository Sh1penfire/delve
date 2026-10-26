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
            requirements(Category.power, ItemStack.with(DelveItems.crystalisedCopper, 50));
            size = 3;
            maxNodes = 20;
            laserRange = 4;
            fieldRange = 10;
        }};

        relay = new PowerNode("relay"){{
            requirements(Category.power, ItemStack.with(DelveItems.crystalisedCopper, 20));
            size = 1;
            maxNodes = 2;
            laserRange = 20;
        }};
    }
}
