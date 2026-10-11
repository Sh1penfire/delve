package main.blocks.interfaces;

import arc.struct.Seq;
import main.type.ItemState;
import mindustry.game.Team;

//Interface for anything which can hold item states.
public interface ItemStateHolder {

    Team team();

    Seq<ItemState> list();

    default ItemState topState(){
        return null;
    }

    //Whether this entity can accept this item state
    default boolean acceptsItemState(ItemState state){
        return false;
    };

    //Accept the item state
    default void handleState(ItemState state){};

    //Remove the item state
    default void removeState(ItemState state){
    };
}
