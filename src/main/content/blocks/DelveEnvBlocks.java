package main.content.blocks;

import main.blocks.environment.DefaultOreBlock;
import main.blocks.environment.ItemPile;
import main.blocks.environment.ItemStatePile;
import main.blocks.environment.OreBlock;
import main.content.DelveItems;
import mindustry.gen.Sounds;
import mindustry.graphics.Shaders;
import mindustry.world.blocks.environment.Floor;

public class DelveEnvBlocks {
    public static DefaultOreBlock oreBlock;
    public static ItemPile itemPile;
    public static ItemStatePile statePile;

    public static OreBlock concertumDeposit, placeriteDeposit;
    public static OreBlock concertumCluster, placithiteCluster;
    public static OreBlock graystone, oldstone;
    public static OreBlock CUT, scraps, glacialChunk, glacialBoulder;
    public static Floor scrapsFloor;

    public static Floor oldstoneFloor;

    public static void load(){
        oreBlock = new DefaultOreBlock("ore-block");
        itemPile = new ItemPile("item-pile");
        statePile = new ItemStatePile("item-state-pile");

        concertumDeposit = new OreBlock("concertum-deposit"){{
            variants = 3;
            health = 120;
            itemDropMap.put(this, DelveItems.geothite);
        }};

        placeriteDeposit = new OreBlock("placithite-deposit"){{
            variants = 2;
            health = 60;
            itemDropMap.put(this, DelveItems.malachite);
        }};

        concertumCluster = new OreBlock("concertum-cluster"){{
            variants = 2;
            health = 120;
            itemDropMap.put(this, DelveItems.geothite);
        }};

        placithiteCluster = new OreBlock("placithite-cluster"){{
            variants = 2;
            health = 60;
            itemDropMap.put(this, DelveItems.malachite);
        }};

        graystone = new OreBlock("graystone-wall"){{
            variants = 0;
            health = 300;
            itemDropMap.put(this, DelveItems.stone);
        }};

        oldstone = new OreBlock("oldstone-wall"){{
            variants = 2;
            health = 15;
            itemDropMap.put(this, DelveItems.oldstone);
            crackSprites = 4;
        }};

        CUT = new OreBlock("CUT"){{
            variants = 0;
            health = 15;
            itemDropMap.put(this, DelveItems.oldstone);
        }};

        scraps = new OreBlock("scraps"){{
            variants = 0;
            health = 48;
        }};

        glacialChunk = new OreBlock("glacial-chunk"){{
            variants = 0;
            health = 80;
            itemDropMap.put(this, DelveItems.azuritePowder);
            hitSound = Sounds.door;
        }};

        glacialBoulder = new OreBlock("glacial-boulder"){{
            variants = 3;
            health = 500;
            itemDropMap.put(this, DelveItems.azuritePowder);
        }};

        oldstoneFloor = new Floor("oldstone-floor", 0);
        scrapsFloor = new Floor("scraps-floor", 4);

    }
}
