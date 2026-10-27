package main.blocks.core;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import main.blocks.EnergyField;
import main.blocks.interfaces.ItemStateHolder;
import main.items.Inventory;
import main.items.ItemAttributes;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;

public class DelveCoreBlock extends CoreBlock {
    public DelveCoreBlock(String name) {
        super(name);
        update = true;
        consumesPower = false;
        hasPower = outputsPower = true;
    }

    public float powerPerTile = 10/45f;

    public float crystallineCost = 5;

    public float conversionTime = 60;

    public class DelveCoreBuild extends CoreBuild implements ItemStateHolder{

        static Seq<ItemState> transfering = new Seq();

        public ItemAttributes.AttributeData data = new ItemAttributes.AttributeData();

        boolean converting = false;
        float convertTimer = 0;

        @Override
        public void unitOn(Unit unit) {
            super.unitOn(unit);
            if(unit instanceof ItemStateHolder holder){
                transfering.set(holder.inventory().states);
                for(ItemState state: transfering){
                    if(!acceptsItemState(state)) break;

                    holder.removeState(state);
                    handleState(state);
                }
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if(converting){
                convertTimer += edelta();
                if(convertTimer >= conversionTime){
                    convertTimer -= conversionTime;
                    convertAttributes();
                }
            }
        }


        public void convertAttributes(){
            converting = false;
            data.each((attrId, value) -> {
                if(value >= crystallineCost){
                    value -= crystallineCost;
                    converting = converting || value >= crystallineCost;
                    items.add(ItemAttributes.pureMap.get(attrId), 1);
                }
                data.idPut(attrId, value);
            });
        }

        @Override
        public float getPowerProduction() {
            return EnergyField.total * powerPerTile/60f;
        }

        @Override
        public Inventory inventory() {
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
            float multiplier = state.mass.getModified(state.inventory);

            if(state.attributes != null) state.attributes.each((attrId, value) -> {
                Item item = ItemAttributes.pureMap.get(attrId);
                if(item == null) return;

                data.idAdd(attrId, value * multiplier);
                if(data.idGet(attrId, 0) >= crystallineCost) converting = true;
            });
            else handleItem(this, state.type.getBase());
        }

        @Override
        public void removeState(ItemState state) {
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.overlayUI);
            if(converting) Lines.arc(x, y, Vars.tilesize * size, convertTimer/conversionTime);
        }

        @Override
        public void display(Table table) {
            super.display(table);
            table.row();
            data.each((attrId, entry) -> {
                table.table(t -> {
                    t.image(ItemAttributes.pureMap.get(attrId).uiIcon);

                    t.label(() -> {
                        float amount = data.idGet(attrId, 0);
                        return Strings.format("@ : @ (@)", attrId, Strings.fixed(amount, 1), Mathf.floor(amount / crystallineCost));
                    });
                }).pad(0);
                table.row();
            });
        }
    }
}
