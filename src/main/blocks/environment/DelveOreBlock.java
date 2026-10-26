package main.blocks.environment;

import arc.Core;
import arc.Graphics;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import main.content.DelveItems;
import main.data.lootables.AttributeDropTableBase;
import main.data.lootables.AttributeDropTableBase.AtributeDropTable;
import main.items.ItemAttributes;
import main.items.ItemAttributes.AttributeData;
import main.items.ItemConstructors;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;

import java.util.Iterator;

public class DelveOreBlock extends Block {

    public static ModTile tmpTile = new ModTile(-1, -1);

    public static ObjectMap<Block, Item> itemDropMap = new ObjectMap<>();

    public static class ModTile extends Tile{

        public ModTile(int x, int y) {
            super(x, y);
        }

        @Override
        public void setBlock(Block type) {
            block = type;
        }
    }

    public static AtributeDropTable defaultDroptable = new AtributeDropTable(){{
        mainEntry = new AttributeDropTableBase.TableDropEntry(){{

            entries.addAll(
                    new TableDropEntry(){{
                        baseline = new AttributeData().put(ItemAttributes.iron, 10).put( ItemAttributes.stone, 10);

                        prefs.add(new MinCostPref(){{
                            cost.set(baseline);
                        }});

                        entries.addAll(
                                new ItemDropEntry(){{
                                    type = DelveItems.geothite;

                                    prefs.add(new MinCostPref(){{
                                        cost.put(ItemAttributes.iron, 4).put( ItemAttributes.stone, 4);
                                    }});
                                    cost.put(ItemAttributes.iron, 1).put( ItemAttributes.stone, 1);
                                    minCostScale = 3;
                                    maxCostScale = 4;
                                }},
                                new ItemDropEntry(){{

                                    type = DelveItems.concertumDust;

                                    cost.put(ItemAttributes.iron, 1).put(ItemAttributes.stone, 1);
                                    minCostScale = 1;
                                    maxCostScale = 3;
                                }}
                        );
                    }},
                    new TableDropEntry(){{
                        prefs.add(new AboveZeroPref(ItemAttributes.stone));

                        entries.addAll(
                                new ItemDropEntry(){{
                                    type = DelveItems.stone;

                                    prefs.add(new MinCostPref(){{
                                        cost.put( ItemAttributes.stone, 8);
                                    }});
                                    cost.put( ItemAttributes.stone, 1);
                                    minCostScale = 8;
                                    maxCostScale = 12;
                                }},
                                new ItemDropEntry(){{
                                    type = DelveItems.stone;

                                    prefs.add(new MinCostPref(){{
                                        cost.put( ItemAttributes.stone, 4);
                                    }});
                                    cost.put(ItemAttributes.stone, 1);
                                    minCostScale = 4;
                                    maxCostScale = 5;
                                }},
                                new ItemDropEntry(){{
                                    type = DelveItems.stoneDust;

                                    cost.put(ItemAttributes.stone, 1);
                                    minCostScale = 1;
                                    maxCostScale = 3;
                                }}
                        );
                    }},
                    new TableDropEntry(){{
                        prefs.add(new AboveZeroPref(ItemAttributes.iron));

                        entries.addAll(
                                new ItemDropEntry(){{
                                    type = DelveItems.rust;

                                    prefs.add(new MinCostPref(){{
                                        cost.put(ItemAttributes.iron, 4);
                                    }});
                                    cost.put(ItemAttributes.iron, 1);
                                    minCostScale = 3;
                                    maxCostScale = 4;
                                }},
                                new ItemDropEntry(){{

                                    type = DelveItems.rustDust;

                                    cost.put(ItemAttributes.iron, 1);
                                    minCostScale = 1;
                                    maxCostScale = 3;
                                }}
                        );
                    }}
            );
        }};
    }};

    public AtributeDropTable dropTable = defaultDroptable;
    public AttributeData dropAttributes = new AttributeData();

    public float flashDecay = 0.25f;
    public float exponentialDecay = 0.92f;
    public boolean mixCol = false;

    public Sound hitSound = Sounds.rockBreak;

    public TextureRegion[] crackRegions;
    public String crackName = "delve-wall-cracks";
    public int crackSprites = 4;

    public DelveOreBlock(String name) {
        super(name);
        destructible = true;
        update = true;
        breakable = false;
        targetable = false;
        solid = true;
        rebuildable = false;
        forceDark = false;
        drawCracks = false;

        buildVisibility = BuildVisibility.sandboxOnly;
        category = Category.effect;

        drawCached = drawDynamic = true;


        allowDerelictRepair = false;
        drawDisabled = false;
        mixCol = true;
    }

    @Override
    public void load() {
        super.load();
        crackRegions = new TextureRegion[crackSprites];
        for (int i = 0; i < crackSprites; i++) {
            crackRegions[i] = Core.atlas.find(name + "-cracks" + (i + 1), Core.atlas.find(crackName + (i + 1)));
        }
    }

    @Override
    public boolean isDarkened(Tile tile) {
        return super.isDarkened(tile);
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("health");
        addBar("stability", b -> new Bar(() -> "Layer Health: " + b.health, () -> Pal.accent, b::healthf).blink(Color.black));
    }

    @Override
    public boolean canBreak(Tile tile) {
        return accessible();
    }

    @Override
    public boolean canReplace(Block other) {
        return true;
    }

    @Override
    public boolean checkForceDark(Tile tile) {
        return super.checkForceDark(tile);
    }

    public boolean accessible() {
        return Vars.state.rules.editor || Vars.state.playtestingMap != null || Vars.state.rules.infiniteResources;
    }

    //regular walls get converted to these when drill blasts hit them.
    public class OreBlockBuild extends Building implements BreakableWallBuilding{
        public ItemStack[] material;


        TextureRegion baseRegion;
        float rotOffset = 0;
        float hitMag = 0;
        int crackState = 0;

        @Override
        public void updateTile() {
            super.updateTile();

            if(enabled){
                hitTime = Mathf.lerp(hitTime, hitTime * exponentialDecay, Time.delta);
                hitTime = Mathf.maxZero(hitTime - flashDecay * Time.delta);

                if(hitTime <= 0) enabled = false;
            }
        }

        @Override
        public void damage(float damage) {
            super.damage(damage);
            enabled = true;
            hitTime += damage;
            rotOffset = Mathf.random(-15, 15);
            if(crackSprites > 0) {
                int last = crackState;
                crackState = Mathf.ceil((1 - healthf()) * crackSprites);
                if(last != crackState) recache();
            }
            hitSound.at(x, y, 1 + hitTime/15);
        }

        public TextureRegion getRegion(){

            if (parent().variants != 0 && parent().variantRegions != null) {
                return parent().variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, parent().variantRegions.length - 1))];
            }
            return parent().region;
        }

        public void draw() {
            if(hitTime <= 0) return;
            baseRegion = getRegion();
            float flashProg = Mathf.maxZero(Mathf.log(15, hitTime + 1));

            float sizeScale = (1 + flashProg * 0.15f);
            float offsetRot = Mathf.lerp(drawrot(), rotOffset, flashProg);

            //Mix with white all the way through, then start making it clear close to the end
            float alphaProg = Mathf.maxZero(1 - flashProg * 3);

            if(mixCol) Draw.mixcol(Tmp.c1.set(Color.white), flashProg);
            Draw.alpha(1 - alphaProg);

            Draw.rect(baseRegion, x, y, baseRegion.width * sizeScale/4, baseRegion.height * sizeScale/4, offsetRot);
            Draw.mixcol();
            Draw.color();

            if(crackSprites > 0){
                Draw.rect(crackRegions[crackState - 1], x, y, baseRegion.width * sizeScale/4, baseRegion.height * sizeScale/4, offsetRot);
            }
        }

        public void drawCached() {
            baseRegion = getRegion();
            Draw.rect(baseRegion, this.x, this.y, this.drawrot());

            if(crackSprites > 0 && crackState > 0){
                Draw.rect(crackRegions[crackState - 1], x, y, baseRegion.width/4f, baseRegion.height/4f);
            }
        }

        @Override
        public boolean displayable() {
            return Vars.fogControl.isVisibleTile(Vars.player.team(), tile.x, tile.y) && Vars.world.getWallDarkness(tile) <= 1;
        }

        public Block parent(){
            return block;
        }

        @Override
        public boolean collide(Bullet other) {
            return false;
        }

        @Override
        public void killed() {
            super.killed();
            Seq<ItemState> stateList = new Seq<>();

            dropTable.get(stateList, dropAttributes, 20);
            Mathf.rand.setSeed(AttributeDropTableBase.rand.seed0);
            stateList.shuffle();

            for(ItemState state: stateList){
                var pile = ItemStatePile.dump(tile.x, tile.y, state);
            }
        }

        @Override
        public void drawTeam() {

        }

        @Override
        public void drawDisabled() {

        }

        public Graphics.Cursor getCursor() {
            return (!accessible() ? Graphics.Cursor.SystemCursor.arrow : super.getCursor());
        }

        @Override
        public void display(Table table) {
            table.table((t) -> {
                t.left();

                tmpTile.setBlock(parent());

                t.add(new Image(this.parent().getDisplayIcon(tmpTile))).scaling(Scaling.fit).size(32.0F);
                t.labelWrap(this.parent().getDisplayName(tmpTile) + "[lightgray] (Ore)[]").left().width(190.0F).padLeft(5.0F);
            }).growX().left();
            table.row();

            table.table((bars) -> {
                bars.defaults().growX().height(18.0F).pad(4.0F);
                this.displayBars(bars);
            }).growX();
            table.row();
            table.table(this::displayConsumption).growX();
            boolean displayFlow = (this.block.category == Category.distribution || this.block.category == Category.liquid) && this.block.displayFlow;
            if (displayFlow) {
                String ps = " " + StatUnit.perSecond.localized();
                ItemModule flowItems = this.flowItems();
                if (flowItems != null) {
                    table.row();
                    table.left();
                    table.table((l) -> {
                        Bits current = new Bits();
                        Runnable rebuild = () -> {
                            l.clearChildren();
                            l.left();
                            Iterator var3 = Vars.content.items().iterator();

                            while(var3.hasNext()) {
                                Item item = (Item)var3.next();
                                if (flowItems.hasFlowItem(item)) {
                                    l.image(item.uiIcon).scaling(Scaling.fit).padRight(3.0F);
                                    l.label(() -> {
                                        return flowItems.getFlowRate(item) < 0.0F ? "..." : Strings.fixed(flowItems.getFlowRate(item), 1) + ps;
                                    }).color(Color.lightGray);
                                    l.row();
                                }
                            }

                        };
                        rebuild.run();
                        l.update(() -> {
                            Iterator var3 = Vars.content.items().iterator();

                            while(var3.hasNext()) {
                                Item item = (Item)var3.next();
                                if (flowItems.hasFlowItem(item) && !current.get(item.id)) {
                                    current.set(item.id);
                                    rebuild.run();
                                }
                            }

                        });
                    }).left();
                }

                if (this.liquids != null) {
                    table.row();
                    table.left();
                    table.table((l) -> {
                        Bits current = new Bits();
                        Runnable rebuild = () -> {
                            l.clearChildren();
                            l.left();
                            Iterator var3 = Vars.content.liquids().iterator();

                            while(var3.hasNext()) {
                                Liquid liquid = (Liquid)var3.next();
                                if (this.liquids.hasFlowLiquid(liquid)) {
                                    l.image(liquid.uiIcon).scaling(Scaling.fit).size(32.0F).padRight(3.0F);
                                    l.label(() -> {
                                        return this.liquids.getFlowRate(liquid) < 0.0F ? "..." : Strings.fixed(this.liquids.getFlowRate(liquid), 1) + ps;
                                    }).color(Color.lightGray);
                                    l.row();
                                }
                            }

                        };
                        rebuild.run();
                        l.update(() -> {
                            Iterator var3 = Vars.content.liquids().iterator();

                            while(var3.hasNext()) {
                                Liquid liquid = (Liquid)var3.next();
                                if (this.liquids.hasFlowLiquid(liquid) && !current.get(liquid.id)) {
                                    current.set(liquid.id);
                                    rebuild.run();
                                }
                            }

                        });
                    }).left();
                }
                table.marginBottom(-5.0F);
            }
        }
    }
}
