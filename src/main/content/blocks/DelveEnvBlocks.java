package main.content.blocks;

import main.blocks.environment.DefaultOreBlock;
import main.blocks.environment.ItemPile;
import main.blocks.environment.ItemStatePile;
import main.blocks.environment.OreBlock;
import main.content.DelveItems;
import main.items.DelveItem;
import mindustry.world.blocks.environment.StaticWall;

public class DelveEnvBlocks {
    public static OreBlock oreBlock;
    public static ItemPile itemPile;
    public static ItemStatePile statePile;

    public static OreBlock concertumDeposit, placeriteDeposit;

    public static void load(){
        oreBlock = new DefaultOreBlock("ore-block");
        itemPile = new ItemPile("item-pile");
        statePile = new ItemStatePile("item-state-pile");

        concertumDeposit = new OreBlock("concertum-deposit"){{
            variants = 2;
            health = 120;
            itemDropMap.put(this, DelveItems.geothite);
        }};

        placeriteDeposit = new OreBlock("placithite-deposit"){{
            variants = 2;
            health = 60;
            itemDropMap.put(this, DelveItems.malachite);
        }};
    }
}
