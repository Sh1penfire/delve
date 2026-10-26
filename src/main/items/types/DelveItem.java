package main.items.types;

import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Nullable;
import main.data.Modifyable;
import main.items.ItemConstructors;
import main.meta.MeldStats;
import main.items.ItemAttributes;
import main.items.ItemState;
import mindustry.core.World;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import mindustry.type.Item;

public class DelveItem<T extends ItemConstructors.ConstructorEntry> extends Item {

    public DelveItem(String name) {
        this(name, new Color(Color.black));
    }

    public DelveItem(String name, Color color) {
        super(name, color);
    }

    public Runnable postInitRunnable;

    public static float defaultSize = 4;

    public int altNames = 0;
    public float nameSwitchChance = 0.001f;
    @Nullable public String altNamePrefix;
    public String[] namesList;


    //Whether or not the item can combine, or it comes in discrete packets that can't combine
    public boolean discrete = true;

    public Prov<ItemState> constructor = () -> new ItemState(this);

    @Override
    public void setStats() {
        super.setStats();
        MeldStats.itemAttributes(this, stats);
    }

    @Override
    public void init() {
        super.init();

        ItemConstructors.put(name, new ItemConstructors.ConstructorEntry(this, constructor));

        if(altNames > 0){
            namesList = new String[altNames];
            for (int i = 0; i < altNames; i++) {
                namesList[i] = Core.bundle.get(altNamePrefix + ".name" + (i + 1));
            }
            Events.run(EventType.Trigger.update, () -> {
                if(Mathf.chance(nameSwitchChance)){
                    localizedName = namesList[Mathf.random(0, namesList.length - 1)];
                }
            });
        }
    }

    @Override
    public void postInit() {
        super.postInit();
        if(postInitRunnable != null) postInitRunnable.run();
    }
}
