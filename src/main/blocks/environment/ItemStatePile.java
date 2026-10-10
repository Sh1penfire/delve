package main.blocks.environment;

import arc.Graphics;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveEnvBlocks;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;

import java.util.Iterator;

public class ItemStatePile extends Block{

    public static ItemStatePileBuild create(int x, int y, ItemState state){
        Tile tile = Vars.world.tile(x, y);
        tile.setBlock(DelveEnvBlocks.statePile);
        tile.build.team = Vars.state.rules.defaultTeam;
        var pile = (ItemStatePileBuild) tile.build;
        pile.handleState(state);

        return (ItemStatePileBuild) tile.build;
    }

    public ItemStatePile(String name) {
        super(name);
        destructible = true;
        update = true;
        breakable = false;
        targetable = false;
        solid = true;
        rebuildable = false;

        buildVisibility = BuildVisibility.sandboxOnly;
        category = Category.effect;

        allowDerelictRepair = false;

        destroyEffect = Fx.none;
        baseShake = 0;
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

    //Physical item states in the world
    public class ItemStatePileBuild extends Building implements ItemStateHolder {

        public Seq<ItemState> states = new Seq<>();

        @Override
        public void update() {
            super.update();
            states.each(ItemState::update);
        }

        @Override
        public void handleState(ItemState state) {
            states.add(state);
            state.parrent = this;
            state.x = x;
            state.y = y;
        }

        @Override
        public void removeState(ItemState state) {
            states.remove(state);
            state.parrent = null;
            if(states.isEmpty()) {
                //Remove this building without triggering that damm char effect that I hate
                tile.setBlock(Blocks.air);
            }
        }

        @Override
        public ItemState topState() {
            return states.size == 0 ? null : states.peek();
        }

        @Override
        public boolean collide(Bullet other) {
            return false;
        }

        int stackOffset;
        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);

            Mathf.rand.setSeed(id);
            stackOffset = 0;

            states.each(state -> {
                stackOffset++;
                state.draw();
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

                states.each(item -> {
                    i++;
                    Image image = new Image(item.type.uiIcon);
                    image.rotateBy(imageoffset);
                    images.add(image);

                    imageoffset += 15;
                    itemName = item.type.localizedName;
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

