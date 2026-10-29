package main.blocks.crafters;

import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.util.Log;
import main.blocks.environment.ItemStatePile;
import main.blocks.interfaces.ItemStateHolder;
import main.items.Inventory;
import main.items.ItemFunction;
import main.items.ItemState;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;

public class RecipeCrafter extends Block {
    public RecipeCrafter(String name) {
        super(name);
        update = true;
        rotate = true;
        quickRotate = true;
        hasItems = false;
        itemCapacity = 60;
    }

    public float craftInterval = 60;

    public ItemFunction process = ItemFunction.crushing;

    public class RecipeCrafterBuild extends Building implements ItemStateHolder {

        public Inventory inventory = new Inventory(itemCapacity);

        public float craftCounter = 0;

        public ItemStateHolder front;

        @Override
        public Inventory inventory() {
            return inventory;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            front = null;
            if(front() instanceof ItemStateHolder holder){
                front = holder;
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();

            craftCounter += edelta();
            if(craftCounter >= craftInterval){
                craftCounter -= craftInterval;

                proximity.each(b -> {
                    if(b.relativeTo(this) != rotation) return;

                    Fx.fire.at(b);
                    if(b instanceof ItemStatePile.ItemStatePileBuild pile){
                        ItemState top = pile.topState();
                        ItemState processed = process.process(top);

                        if(top == processed){
                            ItemStateHolder.handleTransfer(pile, top, this);
                        }
                        else{
                            pile.removeState(top);
                            handleState(processed);
                        }
                    }
                });
            }

            if(front != null){
                while (true){
                    ItemState top = inventory.peek();

                    if(top == null) break;

                    if(front.acceptsItemState(top)){
                        removeState(top);
                        front.handleState(top);
                    }
                    else break;
                }
            }
        }
    }
}
