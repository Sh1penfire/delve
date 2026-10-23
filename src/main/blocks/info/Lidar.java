package main.blocks.info;

import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import main.DelveMod;
import main.blocks.environment.BreakableWallBuilding;
import main.graphics.DelveFx;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.core.World;
import mindustry.entities.units.UnitController;
import mindustry.game.FogControl;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.distribution.Router;

import static mindustry.Vars.world;

public class Lidar extends Block {

    public float reload = 20;

    public float rotateSpeed = 0.25f;

    public float range = Vars.tilesize * 100;

    public Interp stopChanceScaling = Interp.pow3In;
    public float pulseStrength = 5;

    public Lidar(String name) {
        super(name);
        update = true;
    }

    static Vec2 foundPos = new Vec2();
    public class LidarBuild extends Building implements ControlBlock {
        public @Nullable BlockUnitc unit;

        @Override
        public Unit unit(){
            if(unit == null){
                unit = (BlockUnitc) UnitTypes.block.create(team);
                unit.tile(this);
            }
            return (Unit)unit;
        }

        @Override
        public boolean canControl(){
            return size == 1;
        }

        @Override
        public boolean shouldAutoTarget(){
            return false;
        }

        public float time = 0;
        public float rotation = 0;

        boolean inAir = false;
        boolean hitWall = false;

        float pulsePower = 0;

        static Seq<Tile> hitCandidates = new Seq<>();
        @Override
        public void update() {
            super.update();
            time += edelta();
            float targRotation = rotation + rotateSpeed;
            if(unit != null && isControlled()){
                targRotation = Tmp.v1.set(unit.aimX(), unit.aimY()).sub(x, y).angle();
                unit.health(health);
                unit.team(team);
                unit.set(x, y);
            }
            rotation = Angles.moveToward(rotation, targRotation, rotateSpeed);

            if(time > reload && (!isControlled() || unit.isShooting())){
                time = 0;

                hitWall = true;
                inAir = true;
                pulsePower = pulseStrength;
                hitCandidates.clear();

                boolean found = World.raycast(tile.x, tile.y, (int)Tmp.v1.trns(rotation, range).add(x, y).scl(1f/Vars.tilesize).x, (int)Tmp.v1.y, (tx, ty) -> {

                            Tile targTile = world.tile(tx, ty);
                            if(targTile == null) return true;
                            if(targTile == tile) return false;

                            boolean currentAir = !world.solid(tx, ty);

                            if(!currentAir && Mathf.chance(stopChanceScaling.apply(1 - pulsePower/pulseStrength))) return false;

                            if(!currentAir) pulsePower -= 0.5f;

                            foundPos.set(tx, ty).scl(Vars.tilesize);

                            if(inAir != currentAir){
                                hitWall = !currentAir || hitWall;
                                hitCandidates.add(targTile);
                                inAir = currentAir;
                            }
                            return false;
                        }
                );

                Tile hit = hitCandidates.sort(t -> Mathf.random()).firstOpt();
                if(hit != null) DelveFx.lidarShockwave.at(hit.worldx(), hit.worldy(), pulsePower/pulseStrength * 2);
            }
        }

        @Override
        public void draw() {
            super.draw();
            Tmp.v1.trns(rotation, range).add(x, y);
            Lines.line(x, y, Tmp.v1.x, Tmp.v1.y);

            Lines.arc(x, y, Vars.tilesize * 2, time/reload);
        }
    }
}
