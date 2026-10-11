package main.blocks.core;

import arc.struct.Seq;
import main.blocks.EnergyField;
import main.blocks.crafters.EnergyFieldNode;
import main.blocks.interfaces.ItemStateHolder;
import main.type.ItemAttributes;
import main.type.ItemState;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;

public class DelveCoreBlock extends CoreBlock {
    public DelveCoreBlock(String name) {
        super(name);
        consumesPower = false;
        hasPower = outputsPower = true;
    }

    public float powerPerTile = 10/45f;

    public class DelveCoreBuild extends CoreBuild implements ItemStateHolder{

        @Override
        public float getPowerProduction() {
            return EnergyField.total * powerPerTile/60f;
        }

        @Override
        public Seq<ItemState> list() {
            return null;
        }

        @Override
        public ItemState topState() {
            return ItemStateHolder.super.topState();
        }

        @Override
        public boolean acceptsItemState(ItemState state) {
            return true;
        }

        @Override
        public void handleState(ItemState state) {
            if(state.attributes != null) state.attributes.map.each(entry -> {
                Item item = ItemAttributes.pureMap.get(entry.key);
                if(item == null) return;
                items.add(item, (int) entry.value);
            });
            else handleItem(this, state.type);
        }

        @Override
        public void removeState(ItemState state) {
        }
    }
}
