package main.items.types;

import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.util.Nullable;
import main.items.ItemConstructors;
import main.meta.MeldStats;
import main.items.ItemAttributes;
import main.items.ItemState;
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

    //Size in VU, or volume units
    public float size = 4;

    //Mass in MU, or mass units
    public float mass = 1;

    public float volume = 1;

    //Whether or not the item can combine, or it comes in discrete packets that can't combine
    public boolean discrete = true;

    public Prov<UpdateItemState> constructor = () -> new UpdateItemState(this, size);

    public T entry;

    public void update(ItemState state, float x, float y){

    }

    public void draw(ItemState state, float x, float y){
        Draw.rect(state.type.getBase().fullIcon, x, y);
    };

    @Override
    public void setStats() {
        super.setStats();
        MeldStats.itemAttributes(this, stats);
    }

    public static class UpdateItemState extends ItemState {

        public DelveItem item;

        public boolean stackable = true;

        public UpdateItemState(DelveItem item, float size) {
            super(item, size);
            attributes = ItemAttributes.get(item);
            this.item = item;
            data = new ObjectFloatMap<>();
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

    @Override
    public void init() {
        super.init();

        entry = (T) ItemConstructors.put(name, new ItemConstructors.ConstructorEntry(this, constructor));

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
