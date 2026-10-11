package main.items;

import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.util.Nullable;
import main.graphs.ConveyorGraph;
import main.meta.MeldStats;
import main.type.ItemAttributes;
import main.type.ItemState;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import mindustry.type.Item;

public class DelveItem extends Item {

    public static float defaultSize = 4;

    public int altNames = 0;
    public float nameSwitchChance = 0.001f;
    @Nullable public String altNamePrefix;
    public String[] namesList;

    public int size = 4;

    public boolean stackable, runsOut;

    public Prov<ItemState> constructor = () -> new UpdateItemState(this, size);

    public void update(ItemState state, float x, float y){

    }

    public void draw(ItemState state, float x, float y){
        Draw.rect(state.type.fullIcon, x, y);
    };

    @Override
    public void setStats() {
        super.setStats();
        MeldStats.itemAttributes(this, stats);
    }

    public class UpdateItemState extends ItemState {

        public DelveItem item;

        public UpdateItemState(DelveItem item, float size) {
            super(item, size);
            attributes = ItemAttributes.get(item);
            this.item = item;
            data = new ObjectFloatMap<>();
        }
        public UpdateItemState(float size) {
            super(size);
            data = new ObjectFloatMap<>();
        }

        @Override
        public void tweakCondition(float offset) {
            super.tweakCondition(offset);
        }

        @Override
        public void update() {
            item.update(this, x, y);
        }

        @Override
        public boolean canCombine(ItemState other) {
            return stackable && super.canCombine(other);
        }

        @Override
        public void draw() {
            Draw.z(Layer.blockOver);
            item.draw(this, x, y);
            Draw.reset();
        }
    }

    public DelveItem(String name, Color color) {
        super(name, color);
    }

    @Override
    public void init() {
        super.init();
        ItemEntryIDMap.put(this, constructor);

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
