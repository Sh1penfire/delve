package main.entities.units;

import arc.math.geom.Vec2;

public interface CollisionComp {
    /**
     * Called when an entity collides with a wall
     * @param delta The offset applied to the entity to keep it out of the wall's bounds
     * @param wallX The tile x of the wall
     * @param wallY The tile y of the wall
     */
    void collision(Vec2 delta, float wallX, float wallY);
}
