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

    /**
     * Handle the logic of accepting the item state<br><br>
     * -Set the state's parrent to this<br>
     * -Add it to the inventory<br>
     * -Remove it from its source inventory if aplicable
     * -Split it if applicable
     * @param state
     * @return returns if the original state should be removed
     */
    default boolean handleState(ItemState state){
        float room = inventory().roomFor(state);

        if(inventory().roomFor(state) > state.volume.getModified(state.inventory)){
            addState(state);
            return true;
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

            Log.info("Insert ratio: @\nState volume: @", insertRatio, state.volume.getModified(state.inventory));

            addState(split);
            return false;
        }
        return false;
    };

    default void addState(ItemState state){
        state.parrent = this;
        inventory().add(state);
    };


    //Remove the item state
    default void removeState(ItemState state){
        inventory().remove(state);
    };
}
