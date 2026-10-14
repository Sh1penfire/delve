package main.data;

import mindustry.core.World;

public class WorldModifyable<T> extends Modifyable<World, T> {

    public WorldModifyable(T t) {
        super(t);
    }
}
