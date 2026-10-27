package main.content;

import arc.graphics.Color;
import main.items.types.AttributeItem;
import main.items.types.DelveItem;
import main.items.types.SpoilageItem;
import main.meta.MeldStats;
import main.items.ItemAttributes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.type.Item;

public class DelveItems {

    public static DelveItem stone, rust, geothite, malachite, sekos, shadesteel, progenate, oldstone;

    public static DelveItem stoneDust, oldstoneDust, rustDust, geothiteDust, malachiteDust;

    public static DelveItem bionorb;

    public static Item[] propulites;

    public static AttributeItem crystallineStone, crystalisedIron, crystalisedCopper;

    public static void load(){
        //Crystalline Items
        crystalisedIron = new AttributeItem("crystalline-iron", ItemAttributes.iron);
        crystalisedCopper = new AttributeItem("crystalline-copper", ItemAttributes.copper);
        crystallineStone = new AttributeItem("crystalline-stone", ItemAttributes.stone);

        bionorb = new SpoilageItem("bionorb"){{
            ItemAttributes.register(this).put(ItemAttributes.biochem, 1);
            ItemAttributes.pureMap.put(ItemAttributes.biochem.name, this);
        }};

        //Basic items
        stone = new DelveItem("rubble", Color.gray){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 1);
        }};

        oldstone = new DelveItem("oldstone", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 0.5f).
                    put(ItemAttributes.iron, 0.25f);
        }};

        rust = new DelveItem("rust", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.iron, 1);
        }};

        geothite = new DelveItem("geothite", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 1).
                    put(ItemAttributes.iron, 1);

        }};

        malachite = new DelveItem("malachite", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 2).
                    put(ItemAttributes.copper, 5);
        }};

        sekos = new DelveItem("sekos", Color.valueOf("d5f5fc")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.copper, 1)
                    .put(ItemAttributes.sekos, 1);
            altNamePrefix = "blue-dust";
            altNames = 9;

            discrete = false;
        }};


        //Dusts
        stoneDust = new DelveItem("rubble-dust", Color.gray){{
            postInitRunnable = () -> {
            };
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 1);

            discrete = false;
        }};

        oldstoneDust = new DelveItem("oldstone-dust", Color.gray){{
            postInitRunnable = () -> {
            };
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 0.5f)
                    .put(ItemAttributes.iron, 0.25f);

            discrete = false;
        }};

        rustDust = new DelveItem("rust-dust", Color.gray){{
            ItemAttributes.register(this).put(ItemAttributes.iron, 1);

            discrete = false;
        }};

        geothiteDust = new DelveItem("geothite-dust", Color.gray){{
            ItemAttributes.register(this).put(ItemAttributes.iron, 1).put(ItemAttributes.stone, 1);

            discrete = false;
        }};

        malachiteDust = new DelveItem("malachite-dust", Color.green){{
            ItemAttributes.register(this).put(ItemAttributes.copper, 1).put(ItemAttributes.stone, 1);

            discrete = false;
        }};

        //Refined
        shadesteel = new DelveItem("shadesteel", Color.valueOf("9d755d")){{

        }};

        progenate = new DelveItem("procrium", Color.valueOf("9d755d")){{

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
