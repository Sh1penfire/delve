package main.content.blocks;

import main.blocks.environment.DefaultOreBlock;
import main.blocks.environment.ItemPile;
import main.blocks.environment.ItemStatePile;
import main.blocks.environment.DelveOreBlock;
import main.content.DelveItems;
import main.items.ItemAttributes;
import mindustry.gen.Sounds;
import mindustry.world.blocks.environment.Floor;

public class DelveEnvBlocks {
    public static DefaultOreBlock oreBlock;
    public static ItemPile itemPile;
    public static ItemStatePile statePile;

    public static DelveOreBlock concertumDeposit, placeriteDeposit;
    public static DelveOreBlock concertumCluster, placithiteCluster;
    public static DelveOreBlock graystone, oldstone;
    public static DelveOreBlock CUT, scraps, glacialChunk, glacialBoulder;
    public static Floor scrapsFloor;

    public static Floor oldstoneFloor;

    public static void load(){
        oreBlock = new DefaultOreBlock("ore-block");
        itemPile = new ItemPile("item-pile");
        statePile = new ItemStatePile("item-state-pile");

        concertumDeposit = new DelveOreBlock("concertum-deposit"){{
            variants = 3;
            health = 120;

            dropAttributes.put(ItemAttributes.stone, 50).put(ItemAttributes.iron, 20);
        }};

        placeriteDeposit = new DelveOreBlock("placithite-deposit"){{
            variants = 2;
            health = 60;
        }};

        concertumCluster = new DelveOreBlock("concertum-cluster"){{
            variants = 2;
            health = 120;
        }};

        placithiteCluster = new DelveOreBlock("placithite-cluster"){{
            variants = 2;
            health = 60;
        }};

        graystone = new DelveOreBlock("graystone-wall"){{
            variants = 0;
            health = 300;

            dropAttributes.put(ItemAttributes.stone, 20);
        }};

        oldstone = new DelveOreBlock("oldstone-wall"){{
            variants = 2;
            health = 15;
            crackSprites = 4;
        }};

        CUT = new DelveOreBlock("CUT"){{
            variants = 0;
            health = 15;
        }};

        scraps = new DelveOreBlock("scraps"){{
            variants = 0;
            health = 48;
        }};

        glacialChunk = new DelveOreBlock("glacial-chunk"){{
            variants = 0;
            health = 80;
            hitSound = Sounds.door;
        }};

        glacialBoulder = new DelveOreBlock("glacial-boulder"){{
            variants = 3;
            health = 500;
        }};

        oldstoneFloor = new Floor("oldstone-floor", 0);
        scrapsFloor = new Floor("scraps-floor", 4);

    }
}
