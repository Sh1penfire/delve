package main.blocks;

import arc.util.Log;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;

public class ThingBlock extends Block {
    public ThingBlock(String name){
        super(name);
        update = false;
        destructible = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        unloadable = false;
        rotate = true;
        quickRotate = true;
    }

    @Override
    public boolean outputsItems() {
        return super.outputsItems();
    }

    public class ThingBuild extends Building {
        @Override
        public boolean acceptItem(Building source, Item item){
            final var target = getTarget(source, item);

            return target != null && team == target.team && target.acceptItem(this, item);
        }

        @Override
        public void handleItem(Building source, Item item){
            final var target = getTarget(source, item);
            if(target == null) return; // realistically should not happen but yk
            target.handleItem(source, item);
        }

        private Building getTarget(Building source, Item item){
            final var dir = source.relativeTo(tile);
            if(dir == -1) return null;

            if((dir + rotation) % 2 == 0) return nearby(dir);
            return nearby(rotation);
        }
    }
}