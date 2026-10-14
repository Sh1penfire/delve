package main.blocks.interfaces;

import arc.struct.Seq;
import main.items.Inventory;
import main.type.ItemState;
import mindustry.game.Team;

//Interface for anything which can hold item states.
public interface ItemStateHolder {

    Team team();

    Inventory inventory();

    default ItemState topState(){
        return inventory().isEmpty() ? null : inventory().peek();
    }

    //Whether this entity can accept this item state
    default boolean acceptsItemState(ItemState state){
        return false;
    };

    /**
     * Accept the item state<br><br>
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
