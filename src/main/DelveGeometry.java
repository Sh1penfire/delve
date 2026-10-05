package main;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;

public class DelveGeometry {
    public static void cone(int x, int y, int dir, int range, Pointf lambda){
        Point2 direction = Geometry.d4(dir);
        Point2 offset = Geometry.d4(dir + 1);

        for(int i = 0; i < range; i++){
            for(int j = -i; j < i + 1; j++){
                int ox = x + direction.x * i + offset.x * j;

                int oy = y + direction.y * i + offset.y * j;

                lambda.get(ox, oy);
            }
        }
    };

    public interface Pointf{
        void get(int x, int y);
    }
}
