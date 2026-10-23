package main.entities.units;

import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.EntityCollisions;
import mindustry.gen.Hitboxc;

import static mindustry.Vars.tilesize;

public class DelveEntityCollisions extends EntityCollisions {

    private Rect r1 = new Rect(), r2 = new Rect(), tmp = new Rect();
    private Vec2 v = new Vec2();
    private boolean hitWall = false;

    private float hitWallx, hitWallY;

    @Override
    public void moveDelta(Hitboxc entity, float deltax, float deltay, int r, boolean x, SolidPred solidCheck) {
        entity.hitboxTile(r1);
        entity.hitboxTile(r2);
        r1.x += deltax;
        r1.y += deltay;
        hitWall= false;

        int tilex = Math.round((r1.x + r1.width / 2) / tilesize), tiley = Math.round((r1.y + r1.height / 2) / tilesize);

        //Check for tiles in a square with 2r as its side lengths, centered on tilex/tiley
        for(int dx = -r; dx <= r; dx++){
            for(int dy = -r; dy <= r; dy++){

                //The world x and world y of the tiles we check
                int wx = dx + tilex, wy = dy + tiley;
                if(solidCheck.solid(wx, wy)){

                    //The tile we're on
                    tmp.setSize(tilesize).setCenter(wx * tilesize, wy * tilesize);

                    if(tmp.overlaps(r1)){

                        v.set(Geometry.overlap(r1, tmp, x));
                        hitWall = true;

                        r1.x += v.x;
                        r1.y += v.y;
                    }
                }
            }
        }

        entity.trns(r1.x - r2.x, r1.y - r2.y);

        if(hitWall && entity instanceof CollisionComp collider){
            collider.collision(v, hitWallx, hitWallY);
        }
    }
}
