package main.blocks.environment;

import arc.Graphics;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveEnvBlocks;
import main.io.ModdedTypeIO;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;

import java.util.Iterator;
import java.util.Random;

public class ItemStatePile extends Block{

    private static final int searchRange = 8;
    private static final Seq<Tile> searchQueue = new Seq<>(), searching = new Seq<>();

    private static Seq<Tile> targets;

    //I was going to do a spatial hash but ill optimise when needed

    public static boolean validDump(Tile t, ItemState stack){
        return t != null && (t.block() == Blocks.air || (t.build instanceof ItemStatePile.ItemStatePileBuild pile && pile.acceptsItemState(stack)));
    }

    static boolean validFound;

    //Dump items in the world, cannot dump overfill into anything but empty space or item piles
    public static ItemStatePileBuild dump(int x, int y, ItemState state){

        Tile tile = Vars.world.tile(x, y);

        validFound = validDump(tile, state);

        if(!validFound){
            for(int i = 0; i < Geometry.d4.length; i++){
                Point2 offset = Geometry.d4(i);
                tile = Vars.world.tile(x + offset.x, y + offset.y);
                validFound = validDump(tile, state);

                if(validFound) break;
            }
        }

        //If theres no space to dump anywhere, return null
        if(!validFound) return null;

        //Case where the valid tile is air
        if(tile.block() == Blocks.air){
            tile.setBlock(DelveEnvBlocks.statePile);
            tile.build.team = Vars.state.rules.defaultTeam;
            var pile = (ItemStatePileBuild) tile.build;
            pile.handleState(state);

            return (ItemStatePileBuild) tile.build;
        }

        boolean blocked = true;
        //Case where the tile is host to an item pile
        if(tile.build instanceof ItemStatePileBuild pile){
            blocked = !pile.acceptsItemState(state);
            if(!blocked){
                pile.handleState(state);
                return pile;
            }
        }

        //Not sure how we got here... return null
        return null;
    }

    public ItemStatePile(String name) {
        super(name);
        destructible = true;
        update = true;
        breakable = false;
        targetable = false;
        rebuildable = false;
        solid = false;

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

        public Inventory inventory = new Inventory(-1);

        @Override
        public Inventory inventory() {
            return inventory;
        }

        @Override
        public void update() {
            super.update();
            inventory.each(ItemState::update);
        }

        @Override
        public void handleState(ItemState state) {
            ItemStateHolder.super.handleState(state);
            state.x = x;
            state.y = y;
            Fx.pulverize.at(x, y);
        }

        @Override
        public void removeState(ItemState state) {
            inventory.remove(state);
            state.parrent = null;
            if(inventory.isEmpty()) {
                //Remove this building without triggering that damm char effect that I hate
                tile.setBlock(Blocks.air);
            }
        }

        @Override
        public ItemState topState() {
            return inventory.peek();
        }

        @Override
        public boolean collide(Bullet other) {
            return false;
        }

        int stackOffset;

        static Random random = new Random();
        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);

            random.setSeed(id);
            stackOffset = 0;

            inventory.each(state -> {
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

                inventory.each(item -> {
                    i++;
                    Image image = new Image(item.type.getModified(inventory).uiIcon);
                    image.rotateBy(imageoffset);
                    images.add(image);

                    imageoffset += 15;
                    itemName = item.localizedName();
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

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            inventory = ModdedTypeIO.readInventory(read);
            inventory.each(s -> {
                s.parrent = this;
            });
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            ModdedTypeIO.writeInventory(write, inventory);
        }
    }
}

