package main.content.blocks;

import main.blocks.distrib.DelveConveyorBlock;
import main.blocks.distrib.DelveGrabberBlock;
import main.content.DelveItems;
import mindustry.type.Category;
import mindustry.type.ItemStack;

public class DelveDistribBlocks {

    public static DelveConveyorBlock conveyor;

    public static DelveGrabberBlock grabber;


    public static void load(){
        conveyor = new DelveConveyorBlock("highway"){{
            requirements(Category.distribution, ItemStack.with(DelveItems.cryStone, 2));

            size = 1;
        }};

        grabber = new DelveGrabberBlock("grabber"){{
            requirements(Category.distribution, ItemStack.with(DelveItems.cryIron, 40));
            size = 3;
            consumePower(DelveBlocks.basePowerConsumption);
        }};
    }
}
