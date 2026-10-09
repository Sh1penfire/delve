package main.items;

import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Nullable;
import main.graphs.ConveyorGraph;
import main.type.ItemState;
import mindustry.game.EventType;
import mindustry.type.Item;

public class DelveItem extends Item {

    public static float defaultSize = 4;

    public int altNames = 0;
    public float nameSwitchChance = 0.001f;
    @Nullable public String altNamePrefix;
    public String[] namesList;

    public int size = 4;

    public Prov<ItemState> constructor = () -> new UpdateItemState(this, size);

    public void update(float x, float y){

    }
    public void draw(float x, float y){

    };

    public class UpdateItemState extends ItemState {

        public DelveItem item;

        public UpdateItemState(DelveItem item, float size) {
            super(item, size);
            this.item = item;
        }

        @Override
        public void update() {
            item.update(x, y);
        }
    }

    public DelveItem(String name, Color color) {
        super(name, color);
        ItemEntryIDMap.put(this, constructor);
    }

    @Override
    public void init() {
        super.init();

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

    public DelveItem(String name) {
        this(name, new Color(Color.black));
    }
}
