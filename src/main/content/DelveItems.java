package main.content;

import arc.graphics.Color;
import main.items.ItemTags;
import main.items.types.*;
import main.meta.MeldStats;
import main.items.ItemAttributes;
import mindustry.Vars;
import mindustry.type.Item;

public class DelveItems {

    public static AdapterItem adapterItem;

    public static DelveItem stone, rust, geothite, malachite, sekos, shadesteel, progenate, oldstone;

    public static DelveItem stoneDust, oldstoneDust, rustDust, geothiteDust, malachiteDust;

    public static DelveItem bionorb;

    public static Item[] propulites;

    public static AttributeItem cryCarbon, cryStone, cryIron, cryCopper;

    public static DustItem cryCarbonDust, cryStoneDust, cryIronDust, cryCopperDust, bionorbDust;

    public static void load(){
        //Internal
        adapterItem = new AdapterItem("adapter");

        //Crystalline Items
        cryIron = new AttributeItem("crystalline-iron", ItemAttributes.iron);
        cryCopper = new AttributeItem("crystalline-copper", ItemAttributes.copper);
        cryStone = new AttributeItem("crystalline-stone", ItemAttributes.stone);
        cryCarbon = new AttributeItem("crystalline-carbon", ItemAttributes.carbon);

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


        //Dusts

        sekos = new DelveItem("sekos", Color.valueOf("d5f5fc")){{
            ItemAttributes.register(this).
                    put(ItemAttributes.copper, 1)
                    .put(ItemAttributes.sekos, 1);
            altNamePrefix = "blue-dust";
            altNames = 9;

            tags.add(ItemTags.dust);

            discrete = false;
        }};

        //Dust
        stoneDust = createDust("rubble", stone);
        oldstoneDust = createDust("oldstone", oldstone);
        rustDust = createDust("rust", rust);
        geothiteDust = createDust("geothite", geothite);
        malachiteDust = createDust("malachite", malachite);

        //Crystalline Dusts
        cryCarbonDust = crystalDust("crystalline-carbon", cryCarbon);
        cryCarbonDust = crystalDust("crystalline-stone", cryStone);
        cryIronDust = crystalDust("crystalline-iron", cryIron);
        cryCopperDust = crystalDust("crystalline-copper", cryCopper);
        bionorbDust = crystalDust("bionorb", bionorb);


        //Refined
        shadesteel = new DelveItem("shadesteel", Color.valueOf("9d755d")){{

        }};

        progenate = new DelveItem("procrium", Color.valueOf("9d755d")){{

        }};

        Vars.content.items().each(i -> {
            if(ItemAttributes.get(i) != null) MeldStats.itemAttributes(i, i.stats);
        });
    }

    public static DustItem createDust(String name, DelveItem delveItem){
        return new DustItem(name + "-dust", delveItem);
    }
    public static DustItem crystalDust(String name, DelveItem delveItem){
        return new DustItem(name + "-dust", delveItem){{
            tags.add(ItemTags.crystalline);
        }};
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
