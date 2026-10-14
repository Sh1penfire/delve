package main.data;

import mindustry.core.World;

public class WorldModifyable<T> extends Modifyable<World, T> {

    public String id;

    public WorldModifyable(T t) {
        super(t);
    }

    @Override
    public T getModified(World context) {
        return super.getModified(context);
    }
}
