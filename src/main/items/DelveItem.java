package main.items;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Nullable;
import mindustry.game.EventType;
import mindustry.type.Item;

public class DelveItem extends Item {
    public int altNames = 0;
    public float nameSwitchChance = 0.001f;
    @Nullable public String altNamePrefix;
    public String[] namesList;


    public DelveItem(String name, Color color) {
        super(name, color);
        altNamePrefix = "blue-dust";
    }

    @Override
    public void init() {
        super.init();

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

    public DelveItem(String name) {
        this(name, new Color(Color.black));
    }
}
