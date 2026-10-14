package main.entities.units;

import arc.struct.Seq;
import arc.util.Tmp;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveStorageBlocks;
import main.items.Inventory;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.entities.EntityCollisions;
import mindustry.gen.TankUnit;

public class HaulerUnit extends TankUnit implements ItemStateHolder {

    public Inventory inventory = new Inventory(itemCapacity());
    public float capacity = 0;

    @Override
    public EntityCollisions.SolidPred solidity() {
        return !this.isFlying() && !this.ignoreSolids() ? (x, y) -> EntityCollisions.solid(x, y) && !(Vars.world.tile(x, y).block() == DelveStorageBlocks.warpCore) : null;
    }

    @Override
    public int classId() {
        return super.classId();
    }

    @Override
    public Inventory inventory() {
        return inventory;
    }

    @Override
    public ItemState topState() {
        return inventory.size == 0 ? null : inventory.peek();
    }

    @Override
    public boolean acceptsItemState(ItemState state) {
        return state.size.getModified(inventory) <= inventory.roomFor(state);
    }

    float totalOffset = 0;
    @Override
    public void handleState(ItemState state) {
        inventory.add(state);
        state.parrent = this;

        totalOffset = hitSize;

        totalOffset += inventory.capacity;

        Tmp.v1.trns(rotation, totalOffset).add(x, y);
        state.x = Tmp.v1.x;
        state.y = Tmp.v1.y;
    }

    @Override
    public void removeState(ItemState state) {
        inventory.remove(state);
        state.parrent = null;
        updateStatePositions();
    }

    public void updateStatePositions(){
        totalOffset = hitSize;
        inventory.each(entry -> {
            totalOffset += entry.size.getModified(inventory);
            Tmp.v1.trns(rotation, totalOffset).add(x, y);
            entry.x = Tmp.v1.x;
            entry.y = Tmp.v1.y;
        });
    }

    @Override
    public void update() {
        super.update();
        updateStatePositions();
        inventory.each(ItemState::update);
    }

    @Override
    public void draw() {
        super.draw();
        inventory.each(ItemState::draw);
    }
}
