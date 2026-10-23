package main.entities.units.weapons;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import main.blocks.environment.*;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveEnvBlocks;
import main.graphics.DelveFx;
import main.items.ItemFunction;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.control;
import static mindustry.Vars.tilesize;

public class JabberWeapon extends Weapon {

    public JabberWeapon(String name){
        super(name);
    }
    public JabberWeapon(){
        super();
    }

    public float tileDamage = 5;
    public float knockback = 600;

    public float length = tilesize * 3;

    public float pullStrength = 0.055f;
    public float grindEffectChance = 0.8f;
    public float crushingSpeedMulti = 0.15f;
    public float crushingDragMulti = 0.08f;

    public Sound weaponLoopSound = Sounds.none;

    public boolean onlyTiles = true;
    public boolean crushPiles = false;

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        unit.apply(shootStatus, shootStatusDuration);

        if(shoot.firstShotDelay > 0){
            mount.charging = true;
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            bullet.chargeEffect.at(shootX, shootY, rotation, bullet.keepVelocity || parentizeEffects ? unit : null);
        }

        if(!mirror || otherSide == 0){
            unit.speedMultiplier *= Mathf.lerp(1, crushingSpeedMulti, mount.warmup);
            unit.dragMultiplier *= Mathf.lerp(1, crushingDragMulti, mount.warmup);
        }

        //Bit of a failed attempt
        //control.sound.loop(weaponLoopSound,Tmp.v1.set(mountX, mountY), minWarmup);

        if(mount.shoot){
            bullet.shootEffect.at(shootX, shootY, rotation);
            Tmp.v1.trns(rotation, length).add(shootX, shootY);
            World.raycastEachNoDiagonalWorld(shootX, shootY, Tmp.v1.x, Tmp.v1.y, (tx, ty) -> {

                Tile t = Vars.world.tile(tx, ty);

                if(t == null) return true;

                //Switch static walls to ore block
                if(t.build == null && t.block().solid){
                    Block wall = t.block();

                    //TODO: Switch to derelict when allowDerelictRepair:false actually works as intended
                    Call.setTile(t, DelveEnvBlocks.oreBlock, Vars.state.rules.defaultTeam, 0);

                    ((DefaultOreBlock.DefaultOreBuild) t.build).parrentBlock = wall;

                };

                //if the build is still null then yeah
                if(t.build == null) return false;

                if(t.build instanceof ItemStatePile.ItemStatePileBuild pile){
                    if(!crushPiles) return false;

                    if(unit instanceof ItemStateHolder holder){
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
                    }
                    return false;
                }
                if(onlyTiles || t.build instanceof BreakableWallBuilding) {
                    t.build.damage(tileDamage);
                    unit.impulse(Tmp.v1.trns(t.angleTo(unit), knockback));
                    DelveFx.hitSpark.at(t.worldx(), t.worldy(), t.angleTo(unit));
                    return true;
                }

                return true;
            });
        }
    }

    @Override
    public void update(Unit unit, WeaponMount mount) {
        super.update(unit, mount);

        float weaponRotation = unit.rotation + (rotate ? mount.rotation : baseRotation);

        unit.vel.add(Tmp.v1.trns(weaponRotation, pullStrength * Time.delta * mount.warmup));
    }
}
