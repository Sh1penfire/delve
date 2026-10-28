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
public class DuoBar extends Bar {
    private static Rect scissor = new Rect();

    private Floatp fraction;
    private float value, lastValue, blink, outlineRadius;
    private ItemState bottom;
    private Inventory inventory = null;
    private Color blinkColor = new Color(), outlineColor = new Color();

    private Drawable SLOOP;

    public DuoBar(Color color, Floatp fraction, Inventory inventory){
        this.fraction = fraction;
        lastValue = value = fraction.get();
        this.blinkColor.set(color);
        setColor(color);
        this.inventory = inventory;

        SLOOP = new TextureRegionDrawable(Blocks.duo.region);

    }

    public DuoBar(Prov<Color> color, Floatp fraction, Inventory inventory){
        this.fraction = fraction;
        lastValue = value = Mathf.clamp(fraction.get());
        update(() -> {
            setColor(color.get());
            this.blinkColor.set(color.get());
        });
        this.inventory = inventory;

        SLOOP = new TextureRegionDrawable(Blocks.duo.region);
    }

    public void reset(float value){
        this.value = lastValue = blink = value;
    }

    public void set(Prov<String> name, Floatp fraction, Color color){
        this.fraction = fraction;
        this.lastValue = fraction.get();
        setColor(color);
        this.blinkColor.set(color);
    }

    public void snap(){
        lastValue = value = fraction.get();
    }

    public Bar outline(Color color, float stroke){
        outlineRadius = Scl.scl(stroke);
        return this;
    }

    public void flash(){
        blink = 1f;
    }

    public Bar blink(Color color){
        blinkColor.set(color);
        return this;
    }

    private static ItemState newBottom;
    @Override
    public void draw(){
        if(fraction == null) return;

        float computed = Mathf.clamp(fraction.get());

        if(lastValue > computed){
            blink = 1f;
            lastValue = computed;
        }

        if(lastValue > computed){
            lastValue = computed;
        }

        if(Float.isNaN(lastValue)) lastValue = 0;
        if(Float.isInfinite(lastValue)) lastValue = 1f;
        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        if(Float.isNaN(computed)) computed = 0;
        if(Float.isInfinite(computed)) computed = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        if(outlineRadius > 0){
            Draw.color(outlineColor);
            bar.draw(x - outlineRadius, y - outlineRadius, width + outlineRadius*2, height + outlineRadius*2);
        }


        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);
        Draw.alpha(parentAlpha);

        Drawable top = Tex.barTop;
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("bar-top").width){
            top.draw(x, y, topWidth, height);

            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                SLOOP.draw(x + Mathf.absin(1, width), y, Blocks.duo.region.width, Blocks.duo.region.height);
                ScissorStack.pop();
            }
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("bar-top").width, height);

                SLOOP.draw(x + Mathf.absin(1, width), y, Blocks.duo.region.width, Blocks.duo.region.height);
                ScissorStack.pop();
            }
        }

        Draw.color();
    }
}
