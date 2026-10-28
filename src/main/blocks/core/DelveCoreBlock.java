package main.blocks.core;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.event.EventListener;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.blocks.EnergyField;
import main.blocks.interfaces.ItemStateHolder;
import main.io.ModdedTypeIO;
import main.items.Inventory;
import main.items.ItemAttributes;
import main.items.ItemState;
import main.type.TimeString;
import main.ui.GrinderBar;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;

public class DelveCoreBlock extends CoreBlock {
    public DelveCoreBlock(String name) {
        super(name);
        update = true;
        consumesPower = false;
        hasPower = outputsPower = true;
        itemCapacity = 500;
    }

    public float powerPerTile = 10/45f;

    //How much conversion/tick to give
    public float conversionRate = 1/60f;

    public float crystallineCost = 5;

    public class DelveCoreBuild extends CoreBuild implements ItemStateHolder{

        static Seq<ItemState> transfering = new Seq();

        public ItemAttributes.AttributeData data = new ItemAttributes.AttributeData();

        final static ItemAttributes.AttributeData tableData = new ItemAttributes.AttributeData();


        public Inventory inventory = new Inventory(itemCapacity);

        boolean converting = false;
        float convertTimer = 0;
        float currentVol = 0;

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
            updateConversion();
        }

        public void updateConversion(){
            if(converting){
                if(inventory.bottom() == null) {
                    converting = false;
                    return;
                }
                currentVol = inventory.bottom().volume.getModified(inventory);
                convertTimer += edelta() * conversionRate;
                if(convertTimer >= currentVol){
                    convertTimer -= currentVol;
                    convertAttributes();
                }
            }
        }


        public void convertAttributes(){

            ItemState state = inventory.bottom();
            if(state == null) {
                converting = false;
                return;
            }

            removeState(state);

            float multiplier = state.mass.getModified(state.inventory);

            if(state.attributes != null) state.attributes.each((attrId, value) -> {
                Item item = ItemAttributes.pureMap.get(attrId);
                if(item == null) return;

                data.idAdd(attrId, value * multiplier);
                if(data.idGet(attrId, 0) >= crystallineCost) converting = true;
            });

            data.each((attrId, value) -> {
                if(value >= crystallineCost){
                    value -= crystallineCost;
                    items.add(ItemAttributes.pureMap.get(attrId), 1);
                }
                data.idPut(attrId, value);
            });
        }

        @Override
        public void handleState(ItemState state) {
            ItemStateHolder.super.handleState(state);
            converting = true;
        }

        @Override
        public float getPowerProduction() {
            return EnergyField.total * powerPerTile/60f;
        }

        @Override
        public Inventory inventory() {
            return inventory;
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.overlayUI);
            if(converting) Lines.arc(x, y, Vars.tilesize * size, convertTimer/currentVol);
        }

        float imageoffset = 0;
        int i = 0;
        float ETT;

        @Override
        public void display(Table table) {
            super.display(table);

            table.row();

            table.table((bars) -> {
                bars.defaults().growX().height(Vars.iconMed).pad(4.0F);

                GrinderBar EVILHAII = new GrinderBar(Color.orange, () -> 1, inventory);
                bars.add(EVILHAII).growX();
                bars.row();
            }).growX();
            table.row();


            //Attempt to display the inventory...
            table.table((t) -> {
                t.label(() -> {
                    if(inventory.topState != null) return Strings.format("[accent]Processing! @ left[]", inventory.states.size);
                    StringBuilder idle = new StringBuilder("[gray]Idle");
                    for(int i = 0; i < Time.globalTime/30 % 3; i++){
                        idle.append(".");
                    }
                    return idle.append("[]");
                }).growX().left();
                t.row();
                t.label(() -> {
                    ETT = 0;
                    inventory.each(state -> {
                        ETT += state.volume.getModified(inventory);
                    });
                    ETT = ETT/conversionRate/60f;

                    if(ETT <= 0) return "[gray]ETT: [accent]Done![][]";
                    return Strings.format("[gray]ETT: ~@ []", TimeString.format(ETT - convertTimer, 10, 1));
                }).growX().left();
            }).growX();
            table.row();

            table.table(attributeTable -> {

                attributeTable.update(() -> {
                    tableData.clear();
                    inventory.each(itemState -> {
                        if(itemState.attributes != null) tableData.addData(itemState.attributes, itemState.mass.getModified(itemState.inventory));
                    });
                });

                ItemAttributes.pureMap.each((attrId, entry) -> {
                    attributeTable.table(t -> {
                        t.image(ItemAttributes.pureMap.get(attrId).uiIcon);

                        t.label(() -> {
                            float amount = tableData.idGet(attrId, 0);
                            float cur = data.idGet(attrId, 0);
                            return Strings.format("@ : @[gray] + @[] [accent](@)[]", attrId, Strings.fixed(cur, 1), Strings.fixed(amount, 1), Mathf.floor((cur + amount) / crystallineCost));
                        });
                    }).growX().left();
                    attributeTable.row();
                });
            }).growX();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            ModdedTypeIO.writeInventory(write, inventory);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            inventory = ModdedTypeIO.readInventory(read);
            inventory.each(s -> {
                s.parrent = this;
            });
        }
    }
}
