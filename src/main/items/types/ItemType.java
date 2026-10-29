package main.items.types;

import arc.graphics.g2d.Draw;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.type.Item;

/**
 * The interface for an item state's type. Should be able to update the state and draw it.
 */
public interface ItemType {

    //TODO: Change so this has more context about the world in v9
    void update(ItemState state);

    Item item(ItemState item);

    default void draw(ItemState state, float x, float y){
        draw(state, x, y, Vars.tilesize, Vars.tilesize, 0, 0, 0);
    }

    default void draw(ItemState state, float x, float y, float width, float height){
        draw(state, x, y, width, height, 0, 0, 0);
    }

    default void draw(ItemState state, float x, float y, float width, float height, float originX, float originY, float rotation){
        Draw.rect(item(state).uiIcon, x, y, width, height, originX, originY, rotation);
    }

    default String localizedName(ItemState state){
        return item(state).localizedName;
    }

}
