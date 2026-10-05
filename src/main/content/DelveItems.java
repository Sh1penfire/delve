package main.content;

import arc.graphics.Color;
import main.items.DelveItem;
import main.meta.MeldStats;
import main.type.ItemAttributes;
import mindustry.Vars;
import mindustry.type.Item;

public class DelveItems {

    public static Item stone, rust, geothite, malachite, azuritePowder, shadesteel, progenate;

    public static Item[] propulites;

    public static void load(){
        stone = new Item("rubble", Color.gray){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 1);
        }};

        rust = new Item("rust", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 2).
                    put(ItemAttributes.iron, 5);
        }};

        geothite = new Item("concertum", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 2).
                    put(ItemAttributes.iron, 5);
        }};

        malachite = new Item("placithite", Color.valueOf("9d755d")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.stone, 2).
                    put(ItemAttributes.copper, 5);
        }};

        azuritePowder = new DelveItem("glacial-dust", Color.valueOf("d5f5fc")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.copper, 1)
                    .put(ItemAttributes.sekos, 1);
            altNames = 9;
        }};

        shadesteel = new Item("shadesteel", Color.valueOf("9d755d")){{

        }};

        progenate = new Item("procrium", Color.valueOf("9d755d")){{

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
