package main.items.types;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.graphics.Layer;

public class SpoilageItem extends DelveItem {
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
}
