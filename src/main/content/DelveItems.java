package main.content;

import arc.graphics.Color;
import main.items.types.AttributeItem;
import main.items.types.DelveItem;
import main.items.types.SpoilageItem;
import main.items.types.TurretItem;
import main.meta.MeldStats;
import main.items.ItemAttributes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.type.Item;

public class DelveItems {

    public static DelveItem stone, rust, geothite, malachite, azuritePowder, shadesteel, progenate;

    public static DelveItem stoneDust;

    public static DelveItem bionorb;

    public static TurretItem duonium;

    public static Item[] propulites;

    public static AttributeItem crystallineStone, crystalisedIron, crystalisedCopper;

    public static void load(){

        crystalisedIron = new AttributeItem("crystalline-iron", ItemAttributes.iron);
        crystalisedCopper = new AttributeItem("crystalline-copper", ItemAttributes.copper);
        crystallineStone = new AttributeItem("crystalline-stone", ItemAttributes.stone);

        stone = new DelveItem("rubble", Color.gray){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 12);
            size = 16;
            volume = 12;
        }};

        stoneDust = new DelveItem("rubble-dust", Color.gray){{
            postInitRunnable = () -> {
            };
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 1);
            size = 0.5f;

            discrete = false;
        }};

        rust = new DelveItem("rust", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.iron, 3);
        }};

        geothite = new DelveItem("concertum", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 2).
                    put(ItemAttributes.iron, 5);
        }};

        malachite = new DelveItem("placithite", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 2).
                    put(ItemAttributes.copper, 5);
        }};

        azuritePowder = new DelveItem("glacial-dust", Color.valueOf("d5f5fc")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.copper, 1)
                    .put(ItemAttributes.sekos, 1);
            altNamePrefix = "blue-dust";
            altNames = 9;

            discrete = false;
        }};

        shadesteel = new DelveItem("shadesteel", Color.valueOf("9d755d")){{

        }};

        progenate = new DelveItem("procrium", Color.valueOf("9d755d")){{

        }};

        bionorb = new SpoilageItem("bionorb"){{
            size = 8;
            defaultFreshness = 60 * 10;
        }};

        duonium = new TurretItem("duonium"){{
            size = 8;
        }};


        Vars.content.items().each(i -> {
            if(ItemAttributes.get(i) != null) MeldStats.itemAttributes(i, i.stats);
        });
    }

    //Not sure if ill be using this but just to get it in for the day
    public static Item[] oreVariants(String id, Color color){
      Item[] list = new Item[3];

      for(int i = 0; i < 3; i++){
          list[i] = new Item(id + (i + 1), color);
      }
      return list;
    }
}
