package main.blocks.distrib;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import main.DelveGeometry;
import main.blocks.environment.ItemStatePile;
import main.blocks.interfaces.ItemStateHolder;
import main.graphics.TileDrawers;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;

public class DelveGrabberBlock extends Block {

    public int hands = 2;
    public float handSize = 1;

    //Speed in WU/t
    public float handSpeed = 1f;

    public float searchInterval = 5;

    public int range = 8;

    final int searchTimer = timers++;

    public DelveGrabberBlock(String name) {
        super(name);
        update = true;
        solid = true;
        destructible = true;

        itemCapacity = 60;

        rotate = quickRotate = drawArrow = true;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation) {
        super.drawOverlay(x, y, rotation);
        Tile tile = Vars.world.tileWorld(x, y);
        Point2 dir = Geometry.d4(rotation);
        int trueOffset = 1 - sizeOffset;

        DelveGeometry.cone(tile.x + dir.x * trueOffset, tile.y + dir.y * trueOffset, rotation, range, (tx, ty) -> {
            if(Vars.fogControl.isVisibleTile(Vars.player.team(), tx, ty)){
                TileDrawers.drawAccent(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);
                return false;
            }

            TileDrawers.drawAccent(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);

            return false;
        });

    }

    public class DelveGrabberBuild extends Building implements ItemStateHolder {

        public Inventory inventory = new Inventory(itemCapacity);

        public ItemStateHolder back;


        //Targeting map for all grabbers on the same team
        public static IntMap<Seq<Tile>> targetMap = new IntMap<Seq<Tile>>();

        @Override
        public Inventory inventory() {
            return inventory;
        }

        @Override
        public boolean acceptsItemState(ItemState state) {
            return false;
        }

        @Override
        public ItemState topState() {
            return inventory.peek();
        }

        @Override
        public void removeState(ItemState state) {
            inventory.remove(state);
            state.parrent = null;
        }

        public class Hand{

            public Hand(Building parrent){
                pos = new Vec2(x, y);
                grabbed = new Seq<>();
            };

            Building parrent;

            public float capacity = 0;

            public Vec2 pos = new Vec2();
            public Seq<ItemState> grabbed = new Seq<>();
            public float targetInterval = 0;

            public @Nullable ItemStatePile.ItemStatePileBuild targ;
            public MovmeentStates movementState = MovmeentStates.IDLE;
        };
        public Hand[] handList;

        public enum MovmeentStates{
            GOING_HOME,
            CHASING_TARGET,
            IDLE
        }

        @Override
        public void created() {
            handList = new Hand[hands];

            for (int i = 0; i < hands; i++) {
                handList[i] = new Hand(this);
            }

            if(!targetMap.containsKey(team.id)) targetMap.put(team.id, new Seq<>());
            currentTeamMap = targetMap.get(team.id);
        }

        Seq<Tile> currentTeamMap;
        Point2 dir;
        Vec2 targetPos = Tmp.v1;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            back = null;
            if(back() instanceof ItemStateHolder holder){
                back = holder;
            }
        }

        @Override
        public void update() {
            super.update();
            update2();

            if(back != null){
                while (true){
                    ItemState top = inventory.peek();

                    if(top == null) break;

                    if(back.acceptsItemState(top)){
                        removeState(top);
                        back.handleState(top);
                    }
                    else break;
                }
            }
        }

        public void update2(){

            dir = Geometry.d4(rotation);

            for (int i = 0; i < hands; i++){
                updateHand(i);
            };
        }

        public void updateHand(int handIndex){
            Hand hand = handList[handIndex];
            updateTargeting(hand);
            updateMovement(hand);
        }

        public void updateMovement(Hand hand){
            switch (hand.movementState){
                case IDLE -> {
                    break;
                }
                case GOING_HOME -> {
                    hand.pos.approach(Tmp.v1.set(this), handSpeed);

                    if(hand.pos.within(Tmp.v1.set(this), size * Vars.tilesize/2f)){
                        //Dump resources at block

                        if(inventory.size >= inventory.capacity) return;

                        hand.grabbed.each(state -> {
                            inventory.add(state);
                        });
                        hand.grabbed.clear();
                        hand.capacity = 0;
                    }
                }
                case CHASING_TARGET -> {
                    hand.pos.approach(Tmp.v1.set(hand.targ), handSpeed);

                    if(hand.pos.within(hand.targ, Vars.tilesize/2f)){

                        while (hand.capacity < handSize && !hand.targ.inventory.isEmpty()){
                            ItemState state = hand.targ.topState();
                            hand.grabbed.add(state);
                            hand.targ.removeState(state);
                            hand.capacity += state.size.getBase();
                        }

                        hand.targ = null;
                    }
                }
            }
        }

        public void updateTargeting(Hand hand){
            hand.movementState = MovmeentStates.IDLE;

            if(hand.capacity >= handSize){
                hand.movementState = MovmeentStates.GOING_HOME;
            }
            else {
                boolean targetValid = hand.targ != null && hand.targ.isAdded();

                //If the current target isn't added or it's null then find a new target
                if(!targetValid){
                    hand.targ = null;

                    //Search for target
                    int trueOffset = 1 - sizeOffset;
                    hand.targetInterval += edelta();

                    if(hand.targetInterval >= searchInterval){
                        hand.targetInterval -= searchInterval;

                        DelveGeometry.cone(tile.x + dir.x * trueOffset, tile.y + dir.y * trueOffset, rotation, range, (x, y) -> {
                            var currentTarg = Vars.world.tile(x, y);
                            if(currentTeamMap.contains(currentTarg) || currentTarg == null) return false;

                            if(currentTarg.build instanceof ItemStatePile.ItemStatePileBuild pile){
                                hand.targ = pile;
                                hand.targetInterval = searchInterval;

                                currentTeamMap.add(hand.targ.tile);

                                return true;
                            }

                            return false;
                        });
                    }

                }

                //If our target is not null, chase the target
                if(hand.targ != null){
                    hand.movementState = DelveGrabberBuild.MovmeentStates.CHASING_TARGET;
                }
            }
            //If the target is null and we already have stuff, just go home
            if(hand.targ == null && hand.capacity > 0){
                hand.movementState = DelveGrabberBuild.MovmeentStates.GOING_HOME;
            }
        }

        @Override
        public void draw() {
            super.draw();
            for (Hand curHand : handList) {

                if(curHand.targ == null){
                    targetPos.set(this);
                }
                else targetPos.set(curHand.targ);

                Draw.color(Color.black);
                Fill.circle(curHand.pos.x, curHand.pos.y, 4);
                Draw.color(Color.yellow);
                Lines.stroke(0.5f);
                Lines.line(curHand.pos.x, curHand.pos.y, targetPos.x, targetPos.y);
                Draw.color(Color.red);
                Fill.circle(targetPos.x, targetPos.y, 2);
            }
        }
    }
}
