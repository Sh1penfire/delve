package main.ui;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.ScissorStack;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Scl;
import arc.util.pooling.Pools;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.content.Blocks;
import mindustry.gen.Tex;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

//I learned this trick from a plumber
public class GrinderBar extends Bar {
    private static Rect scissor = new Rect();

    private Floatp fraction;
    private float value, lastValue, startGap, outlineRadius;
    private ItemState bottom;
    private Inventory inventory = null;
    private Color outlineColor = new Color();

    private Drawable SLOOP;

    public GrinderBar(Color color, Floatp fraction, Inventory inventory){
        this.fraction = fraction;
        lastValue = value = fraction.get();
        setColor(color);
        this.inventory = inventory;

        SLOOP = new TextureRegionDrawable(Blocks.duo.region);

    }

    public GrinderBar(Prov<Color> color, Floatp fraction, Inventory inventory){
        this.fraction = fraction;
        lastValue = value = Mathf.clamp(fraction.get());
        update(() -> {
            setColor(color.get());
        });
        this.inventory = inventory;

        SLOOP = new TextureRegionDrawable(Blocks.duo.region);
    }

    public void reset(float value){
        this.value = lastValue = startGap = value;
    }

    public void set(Prov<String> name, Floatp fraction, Color color){
        this.fraction = fraction;
        this.lastValue = fraction.get();
        setColor(color);
    }

    public void snap(){
        lastValue = value = fraction.get();
    }

    public Bar outline(Color color, float stroke){
        outlineRadius = Scl.scl(stroke);
        return this;
    }

    private static ItemState newBottom;
    private static float offset;
    @Override
    public void draw(){
        if(fraction == null) return;

        newBottom = inventory.bottom();
        if(bottom != newBottom){
            startGap = 1;
            bottom = newBottom;
        }
        bottom = inventory.bottom();

        float computed = Mathf.clamp(fraction.get());


        if(lastValue > computed){
            lastValue = computed;
        }

        if(Float.isNaN(lastValue)) lastValue = 0;
        if(Float.isInfinite(lastValue)) lastValue = 1f;
        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        if(Float.isNaN(computed)) computed = 0;
        if(Float.isInfinite(computed)) computed = 1f;

        startGap = Mathf.lerpDelta(startGap, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        if(outlineRadius > 0){
            Draw.color(outlineColor);
            bar.draw(x - outlineRadius, y - outlineRadius, width + outlineRadius*2, height + outlineRadius*2);
        }


        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        bar.draw(x, y, width, height);
        Draw.color(Color.white);
        Draw.alpha(parentAlpha);

        Drawable top = Tex.barTop;
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("bar-top").width){
            top.draw(x, y, topWidth, height);

            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){

                offset = 0;
                inventory.each(state -> {
                    state.draw(x + offset + startGap * height + height/2f, y + height/2f, height, height);
                    offset += height;
                });

                SLOOP.draw(x + Mathf.absin(100, width), y, Blocks.duo.region.width, Blocks.duo.region.height);
                ScissorStack.pop();
            }
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("bar-top").width, height);

                offset = 0;
                inventory.each(state -> {
                    state.draw(x + offset + startGap * height + height/2f, y + height/2f, height, height);
                    offset += height;
                });

                SLOOP.draw(x + Mathf.absin(100, width), y, Blocks.duo.region.width, Blocks.duo.region.height);
                ScissorStack.pop();
            }
        }

        Draw.color();
    }
}
