package main.blocks;

import arc.Events;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Log;
import jdk.jfr.Event;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.world.Tile;

public class EnergyField {
    public static int[][] coverage;
    public static int total;

    public static Seq<FieldCircle> circles = new Seq<>();
    protected static boolean initialised = false;

    public static class FieldCircle{
        int x, y;
        int radius;

        public void set(int x, int y, int radius){
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    public static void add(FieldCircle circle){
        circles.add(circle);
        if(initialised) {
            update(circle);
        }
        else {
            Log.info("Adding circle before initialisation!");
        }
    }

    public static void update(FieldCircle circle){

        Geometry.circle(circle.x, circle.y, circle.radius, (tx, ty) -> {
            Tile curTile = Vars.world.tile(tx, ty);
            if(curTile == null) return;

            int value = EnergyField.coverage[curTile.x][curTile.y];
            if(value == 0) EnergyField.total++;
            EnergyField.coverage[curTile.x][curTile.y] = value + 1;
        });
    }

    public static void remove(FieldCircle circle){

        Geometry.circle(circle.x, circle.y, circle.radius, (tx, ty) -> {
            Tile tile = Vars.world.tile(tx, ty);
            if(tile == null) return;

            int value = EnergyField.coverage[tx][ty];


            if(value == 0) return;

            EnergyField.coverage[tx][ty] = value - 1;
            if(value == 1) {
                EnergyField.total--;
            }
        });
    }

    public static void init(){
        Events.on(EventType.WorldLoadBeginEvent.class, e -> {
            circles = new Seq<>();
            initialised = false;
        });
        Events.on(EventType.WorldLoadEndEvent.class, e -> {
            total = 0;
            coverage = new int[Vars.world.width()][Vars.world.height()];
            circles.each(EnergyField::update);
            initialised = true;
        });
    }
}
