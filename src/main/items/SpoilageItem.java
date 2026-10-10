package main.items;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.graphics.Layer;
import mindustry.type.Item;

public class SpoilageItem extends DelveItem{
    public SpoilageItem(String name, Color color) {
        super(name, color);
    }

    public SpoilageItem(String name) {
        this(name, Color.white);
    }

    public int spoilageSprites = 3;
    public TextureRegion[] regions;

    @Override
    public void load() {
        super.load();

        regions = new TextureRegion[spoilageSprites];
        for(int i = 0; i < spoilageSprites; i++){
            regions[i] = Core.atlas.find(name + (i + 1));
        }
    }

    //Default is 30 secconds just so that this is visible enough
    public float defaultFreshness = 60 * 30;


    @Override
    public void update(ItemState state, float x, float y) {
        float freshness = state.data.get("freshness", defaultFreshness);
        if(freshness == 0) state.parrent.removeState(state);
        state.data.put("freshness", Math.max(freshness - Time.delta, 0));
    }

    @Override
    public void draw(ItemState state, float x, float y) {
        float freshness = state.data.get("freshness", defaultFreshness);
        float freshFract = freshness/defaultFreshness;
        Draw.rect(regions[Mathf.clamp(Mathf.floor((1 - freshFract) * spoilageSprites), 0, spoilageSprites - 1)], x, y);

        Draw.z(Layer.overlayUI);
        float dy = y - Vars.tilesize/3f;
        float width = Vars.tilesize * 0.8f;
        Draw.color(Color.gray);
        Lines.stroke(2);
        Lines.line(x - width/2f, dy, x + width/2f, dy);
        Draw.color(Color.white);
        Lines.stroke(1);
        Lines.line(x - width/2f, dy, x + width * (freshFract - 0.5f), dy);
    }
}
