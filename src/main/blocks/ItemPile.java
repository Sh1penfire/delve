package main.blocks;

import arc.math.Mathf;
import arc.scene.ui.layout.Stack;
import arc.struct.Seq;
import arc.util.Log;
import main.content.blocks.DelveEnvBlocks;
import mindustry.content.Fx;
import mindustry.game.Team;
import mindustry.gen.Teamc;
import mindustry.gen.UnitEntity;
import mindustry.world.Block;

import arc.Core;
import arc.Graphics;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.MobileInput;
import mindustry.io.TypeIO;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;

import java.util.Iterator;

public class ItemPile extends Block {

    public static ModTile tmpTile = new ModTile(-1, -1);

    public static class ModTile extends Tile{

        public ModTile(int x, int y) {
            super(x, y);
        }

        @Override
        public void setBlock(Block type) {
            block = type;
        }
    }

    public static boolean dumpItems(int x, int y, ItemStack stack){
        Tile t = Vars.world.tile(x, y);
        if(t == null) return false;
        Building build = t.build;

        if(build == null){
            t.setBlock(DelveEnvBlocks.itemPile);
            build = t.build;
            t.build.team = Vars.state.rules.defaultTeam;
        }
        if(!(build instanceof ItemPileBuild)) {
            build.items.add(stack.item, stack.amount);
            return false;
        }

        build.acceptStack(stack.item, stack.amount, null);
        return true;
    };

    public ItemPile(String name) {
        super(name);
        destructible = true;
        breakable = false;
        targetable = false;
        solid = true;
        rebuildable = false;

        buildVisibility = BuildVisibility.sandboxOnly;
        category = Category.effect;

        hasItems = true;
        allowDerelictRepair = false;

        destroyEffect = Fx.none;
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("health");
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
    public class ItemPileBuild extends Building{
        public ItemStack[] material;

        @Override
        public boolean collide(Bullet other) {
            return false;
        }

        @Override
        public int removeStack(Item item, int amount) {
            int removed = super.removeStack(item, amount);

            if(items.total() <= 0) kill();

            return removed;
        }


        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            if(source != null) return 0;
            items.add(item, amount);
            return amount;
        }


        int stackOffset;
        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);

            Mathf.rand.setSeed(id);
            stackOffset = 0;

            items.each((item, amount) -> {
                for(int i = 0; i < Math.min(amount, 10); i++){
                    stackOffset++;
                    Draw.rect(item.fullIcon, x + stackOffset * 0.01f, y + stackOffset * 0.05f, Mathf.rand.range(360));
                }
            });
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

        String itemName = null;
        float imageoffset = 0;
        int i = 0;

        @Override
        public void display(Table table) {
            imageoffset = 0;
            table.table((t) -> {
                t.left();


                Seq<Image> images = Seq.with();
                i = 0;

                items.each((item, amount) -> {
                    i++;
                    Image image = new Image(item.uiIcon);
                    image.rotateBy(imageoffset);
                    images.add(image);

                    imageoffset += 15;
                    itemName = item.localizedName;
                });
                t.table(itemTable -> {

                    Stack stack = new Stack();
                    images.each(stack::addChild);
                    itemTable.add(stack);

                }).scaling(Scaling.fit).size(32);

                t.labelWrap(i == 1 ? itemName + "[lightgray] Stack[]" : "[lightgray] Mixed Stack[]").left().width(190).padLeft(5);
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
