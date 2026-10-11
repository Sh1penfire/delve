package main.blocks;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;

public class EnergyField {
    public static int[][] coverage;
    public static int total;

    public static void init(){
        Events.on(EventType.WorldLoadEvent.class, e -> {
            total = 0;
            coverage = new int[Vars.world.width()][Vars.world.height()];
        });
    }
}
