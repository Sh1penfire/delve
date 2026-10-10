package main.blocks.interfaces;

import main.type.ItemState;

//Interface for anything which can hold item states.
public interface ItemStateHolder {

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
