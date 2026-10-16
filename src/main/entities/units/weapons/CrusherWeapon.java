package main.entities.units.weapons;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.math.geom.Geometry;
import arc.util.Tmp;
import main.blocks.environment.*;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveEnvBlocks;
import main.items.ItemFunction;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.control;
import static mindustry.Vars.tilesize;

public class CrusherWeapon extends Weapon {

    float length = tilesize * 2;

    public float pullStrength = 0.055f;
    public float grindEffectChance = 0.8f;
    public float crushingSpeedMulti = 0.15f;
    public float crushingDragMulti = 0.08f;

    public Sound weaponLoopSound = Sounds.none;

    @Override
    public void update(Unit unit, WeaponMount mount) {
        super.update(unit, mount);
        if(!mirror || otherSide == 0){
            unit.speedMultiplier *= Mathf.lerp(1, crushingSpeedMulti, mount.warmup);
            unit.dragMultiplier *= Mathf.lerp(1, crushingDragMulti, mount.warmup);
        }

        float
                mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
                mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);
        if(Mathf.chanceDelta(mount.warmup * grindEffectChance)) bullet.shootEffect.at(mountX, mountY);

        float weaponRotation = unit.rotation + (rotate ? mount.rotation : baseRotation);

        unit.vel.add(Tmp.v1.trns(weaponRotation, pullStrength * Time.delta * mount.warmup));

        control.sound.loop(weaponLoopSound,Tmp.v1.set(mountX, mountY), minWarmup);

        if(mount.shoot){
            Tmp.v1.trns(unit.rotation, length).add(mountX, mountY);
            World.raycastEachWorld(mountX, mountY, Tmp.v1.x, Tmp.v1.y, (tx, ty) -> {

                Tile t = Vars.world.tile(tx, ty);

                if(t == null) return true;

                //Switch static walls to ore block
                if(t.build == null && t.block().solid){
                    Block wall = t.block();
                    t.setBlock(DelveEnvBlocks.oreBlock);
                    ((DefaultOreBlock.DefaultOreBuild) t.build).parrentBlock = wall;

                    //TODO: Switch to derelict when allowDerelictRepair:false actually works as intended
                    t.build.team = Vars.state.rules.defaultTeam;
                };

                //if the build is still null then yeah
                if(t.build == null) return false;

                if(t.build instanceof ItemStatePile.ItemStatePileBuild pile && unit instanceof ItemStateHolder holder){

                    //Crush the pile's top entry
                    ItemState state = pile.topState();
                    if(state == null) return true;

                    ItemState crushed = ItemFunction.crushing.process(state);
                    if(state != crushed) {
                        pile.handleState(crushed);
                        pile.removeState(state);
                    }

                    if(holder.acceptsItemState(crushed)){
                        float transferred = ItemStateHolder.handleTransfer(pile, crushed, holder);
                        if (transferred > 0 && transferred < 1) {
                            Log.info("Splitting!");
                        }
                    }
                    return false;
                }
                if(t.build instanceof BreakableWallBuilding) {
                    if(Mathf.chanceDelta(bullet.trailChance)) bullet.hitEffect.at(tx * tilesize, ty * tilesize);
                    t.build.damage(bullet.damage);
                }

                return true;
            });
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount) {
        float
                mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
                mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

        Draw.color(Color.green);
        Fill.circle(mountX, mountY, 2);
    }
}
