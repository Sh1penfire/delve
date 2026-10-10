package main.content.blocks;

import main.blocks.environment.ItemPile;
import main.blocks.environment.ItemStatePile;
import main.blocks.environment.OreBlock;

public class DelveEnvBlocks {
    public static OreBlock oreBlock;
    public static ItemPile itemPile;
    public static ItemStatePile statePile;

    public static void load(){
        oreBlock = new OreBlock("ore-block");
        itemPile = new ItemPile("item-pile");
        statePile = new ItemStatePile("item-state-pile");
    }
}
