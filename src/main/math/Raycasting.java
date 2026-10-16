package main.math;

import arc.math.geom.Vec2;
import arc.util.Log;

public class Raycasting {

    public static boolean end = false;

    //Returns the point at which two lines intersect, or null if not valid
    public static Vec2 intersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){

        float det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4))/det;
        float u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3))/det;

        if (t < 0 || t > 1 || u < 0 || u > 1) {
            //Lines are parallel (somehow)
            return null;
        } else {
            return new Vec2(x1 + t * (x2 - x1), y1 + t * (y2 - y1));
        }
    }

    //Returns the point at which a ray and a line intersect, or null if not valid
    //The ray in the direction of (x1, y1) to (x2, y2)
    public static Vec2 rayIntersect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4){
        end = false;

        float det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4))/det;
        float u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3))/det;

        if (t < 0 || u < 0 || u > 1) {
            //Point doesn't land on target segment, or point is behind the ray's starting point
            return null;
        } else {
            end = u == 0 || u == 1;
            return new Vec2(x1 + t * (x2 - x1), y1 + t * (y2 - y1));
        }
    }
}
