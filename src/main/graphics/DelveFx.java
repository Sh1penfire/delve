package main.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;

public class DelveFx {

    public static Effect

    lidarShockwave = new Effect(360, e -> {
        Draw.z(Layer.fogOfWar + 1);
        Draw.color(Color.acid, Color.clear, Mathf.clamp(e.finpow() * 5 - 4));
        Lines.circle(e.x, e.y, e.fin(Interp.pow5In) * e.rotation * 3 + 0.5f);
    });
}
