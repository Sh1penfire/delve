package main.blocks.interfaces;

import arc.util.Log;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.game.Team;
import mindustry.gen.Posc;

//Interface for anything which can hold item states.
public interface ItemStateHolder {

    Team team();

    Inventory inventory();

    default ItemState topState(){
        return inventory().peek();
    }

    //Whether this entity can accept this item state
    default boolean acceptsItemState(ItemState state){
        float room = inventory().roomFor(state);
        return room > state.volume.getModified(state.inventory) || room > 0 && !state.discrete.getModified(state.inventory);
    };

    //Utility method to handle transferring states automatically. Returns the % transfered
    static float handleTransfer(ItemStateHolder parent, ItemState state, ItemStateHolder target){

        float room = target.inventory().roomFor(state);
        Log.info("Room: @, State volume: @", room, state.volume.getModified(state.inventory));

        if(room >= state.volume.getModified(state.inventory)){
            parent.removeState(state);
            target.handleState(state);
            return 1;
        }
        if(room > 0 && !state.discrete.getModified(state.inventory)){
            float insertRatio = room/state.volume.getModified(state.inventory);
            float remainderf = 1 - insertRatio;


            float startVolume = state.volume.getBase();
            float startMass = state.mass.getBase();

            ItemState split = state.copy();
            split.volume.setBase(startVolume * insertRatio);
            split.mass.setBase(startMass * insertRatio);

            state.volume.setBase(startVolume * remainderf);
            state.mass.setBase(startMass * remainderf);

            Log.info("Insert ratio: @\nState volume: @\nSplit volume: @", insertRatio, state.volume.getBase(), split.volume.getBase());

            target.handleState(split);
            return insertRatio;
        }
        return 0;
    }

    /**
     * Handle the logic of accepting the item state<br><br>
     * -Set the state's parrent to this<br>
     * -Add it to the inventory<br>
     * @param state
     */
    default void handleState(ItemState state){
        state.parrent = this;
        inventory().add(state);
    };


    //Remove the item state
    default void removeState(ItemState state){
        inventory().remove(state);
    };
}
