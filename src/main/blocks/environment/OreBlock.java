package main.blocks.environment;

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
import main.content.DelveItems;
import main.content.blocks.DelveEnvBlocks;
import main.items.ItemEntryIDMap;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
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

public class OreBlock extends Block {

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

    public OreBlock(String name) {
        super(name);
        destructible = true;
        breakable = false;
        targetable = false;
        solid = true;
        rebuildable = false;

        buildVisibility = BuildVisibility.sandboxOnly;
        category = Category.effect;


        allowDerelictRepair = false;
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
    public class OreBlockBuild extends Building{
        public ItemStack[] material;
        public Block parent;

        @Override
        public boolean collide(Bullet other) {
            return false;
        }

        @Override
        public void killed() {
            super.killed();
            var pile = ItemStatePile.create(tile.x, tile.y, ItemEntryIDMap.getState(DelveItems.bionorb));
        }

        //Literally all of the shit below is visual until the read/write
        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);
            parent.drawBase(tile);
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

                tmpTile.setBlock(parent);

                t.add(new Image(this.parent.getDisplayIcon(tmpTile))).scaling(Scaling.fit).size(32.0F);
                t.labelWrap(this.parent.getDisplayName(tmpTile) + "[lightgray] (Ore)[]").left().width(190.0F).padLeft(5.0F);
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

        @Override
        public void placed() {
            super.placed();
            parent = Blocks.stone;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            TypeIO.writeBlock(write, parent);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            parent = TypeIO.readBlock(read);
            if(healthf() == 1) tile.setBlock(parent);
        }
    }
}
