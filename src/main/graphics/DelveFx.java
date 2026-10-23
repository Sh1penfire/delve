package main.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import java.util.Random;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;

public class DelveFx {

    static float rockEffectRange = Vars.tilesize * 1;

    static Random heightRand = new Random();
    static int i;

    public static Effect

    lidarShockwave = new Effect(60 * 30, e -> {
        Draw.z(Layer.fogOfWar + 1);
        e.scaled(120, e1 -> {
            Draw.color(Color.acid, Color.clear, Mathf.clamp(e1.finpow() * 5 - 4));
            Lines.circle(e.x, e.y, Interp.pow5Out.apply(Mathf.clamp(e1.fin() * 2)) * e1.rotation * 5 + 0.5f);
        });
        Draw.color(Color.acid, Color.clear, Mathf.clamp(e.finpow() * 5 - 4));
        Lines.circle(e.x, e.y, e.fin(Interp.pow5In) * e.rotation * 3 + 0.5f);
    }),

    hitSpark = new Effect(15, e -> {
        color(Pal.redSpark);
        stroke(e.fout() * 1.5f);

        randLenVectors(e.id, 8, e.finpow() * 17f, e.rotation, 15, (x, y) -> {
            float ang = Mathf.angle(x, y);
            lineAngle(e.x + x, e.y + y, ang, e.fout() * 4 + 1f);
        });
    }),

    blastLine = new Effect(20f, e -> {
        if(!(e.data instanceof Vec2 v)) return;

        color(e.color);
        stroke(e.fout() * 0.9f + 0.6f);

        Fx.rand.setSeed(e.id);
        for(int i = 0; i < 7; i++){
            Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
            Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
        }

        e.scaled(14f, b -> {
            stroke(b.fout() * 1.5f);
            color(e.color);
            Lines.line(e.x, e.y, v.x, v.y);
        });
    }),

    rocks = new Effect(60 * 3, e -> {
        float alpha = Mathf.clamp(e.fout() * 5);

        i = 0;
        heightRand.setSeed(e.id);
        Angles.randLenVectors(e.id, 3, e.fin() * Vars.tilesize * 2, e.rotation, rockEffectRange, (x, y) -> {
            float bounceOffset = heightRand.nextFloat();
            Draw.rect(Blocks.boulder.region, e.x + x, e.y + y);
        });
    });
}
