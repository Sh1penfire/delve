package main.entities.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Strings;
import arc.util.Tmp;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveStorageBlocks;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.entities.EntityCollisions;
import mindustry.gen.TankUnit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

public class HaulerUnit extends TankUnit implements ItemStateHolder {

    public Inventory inventory = new Inventory(itemCapacity());

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

    float totalOffset = 0;
    @Override
    public boolean handleState(ItemState state) {

        boolean output = ItemStateHolder.super.handleState(state);

        totalOffset = hitSize;

        totalOffset += inventory.capacity;

        Tmp.v1.trns(rotation, totalOffset).add(x, y);
        state.x = Tmp.v1.x;
        state.y = Tmp.v1.y;
        return output;
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

        float dx = x, dy = y - Vars.tilesize * 2.5f;


        Drawf.text(Strings.format("@/@", inventory.size, inventory.capacity), dx, dy, Color.white);
        dy -= Vars.tilesize/2f;
        float width = 8;

        Draw.z(Layer.overlayUI);
        Lines.stroke(2);
        Draw.color(Color.gray);
        Lines.line(dx - width, dy, dx - width + width * 2, dy);
        Lines.stroke(1);
        Draw.color(Color.white);
        Lines.line(dx - width, dy, dx - width + width * inventory.fullness() * 2, dy);
    }
}
