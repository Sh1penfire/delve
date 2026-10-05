package main.units.weapons;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import main.blocks.ItemPile;
import main.blocks.OreBlock;
import main.content.blocks.DelveEnvBlocks;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.bullet.RailBulletType;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.units.RepairTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class LaserGun extends Weapon {
    public boolean targetBuildings = false, targetUnits = true;

    public float repairSpeed = 0.3f;
    public float fractionRepairSpeed = 0f;
    public float beamWidth = 1f;
    public float pulseRadius = 6f;
    public float pulseStroke = 2f;
    public float widthSinMag = 0f, widthSinScl = 4f;

    public TextureRegion laser, laserEnd, laserTop, laserTopEnd;

    public Color laserColor = Color.valueOf("98ffa9"), laserTopColor = Color.white.cpy();
    //only for blocks
    public Color healColor = Pal.heal;

    public LaserGun(String name){
        super(name);
    }

    public LaserGun(){
    }

    {
        //must be >0 to prevent various bugs
        reload = 1f;
        predictTarget = false;
        autoTarget = true;
        controllable = false;
        rotate = true;
        mountType = HealBeamMount::new;
        recoil = 0f;
        noAttack = true;
        useAttackRange = false;
    }

    @Override
    public void addStats(UnitType u, Table w){
        w.row();
        w.add("[lightgray]" + Stat.repairSpeed.localized() + ": " + (mirror ? "2x " : "") + "[white]" + (int)(repairSpeed * 60) + " " + StatUnit.perSecond.localized());
    }

    @Override
    public float dps(){
        return 0f;
    }

    @Override
    public void load(){
        super.load();

        laser = Core.atlas.find("laser-white");
        laserEnd = Core.atlas.find("laser-white-end");
        laserTop = Core.atlas.find("laser-top");
        laserTopEnd = Core.atlas.find("laser-top-end");
    }

    @Override
    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
        var out = targetUnits ? Units.closest(unit.team, x, y, range, u -> u != unit && u.damaged()) :  null;
        if(out != null || !targetBuildings) return out;
        return Units.findAllyTile(unit.team, x, y, range, Building::damaged);
    }

    @Override
    protected boolean checkTarget(Unit unit, Teamc target, float x, float y, float range){
        return !(target.within(unit, range + unit.hitSize/2f) && target.team() == unit.team && target instanceof Healthc u && u.damaged() && u.isValid());
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
        //does nothing, shooting is handled in update()
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        super.update(unit, mount);

        float lastReload = mount.reload;
        mount.reload = Math.max(mount.reload - Time.delta * unit.reloadMultiplier, 0);
        mount.smoothReload = Mathf.lerpDelta(mount.smoothReload, mount.reload / reload, smoothReloadSpeed);

        float
                weaponRotation = unit.rotation - 90,
                wx = unit.x + Angles.trnsx(weaponRotation, x, y),
                wy = unit.y + Angles.trnsy(weaponRotation, x, y);

        HealBeamMount heal = (HealBeamMount)mount;
        boolean canShoot = mount.shoot;

        Item targetItem = unit.hasItem() ? unit.item() : null;

        if(!autoTarget){
            heal.target = null;
            if(canShoot){
                heal.lastEnd.set(heal.aimX, heal.aimY);

                if(!rotate && !Angles.within(Angles.angle(wx, wy, heal.aimX, heal.aimY), unit.rotation, shootCone)){
                    canShoot = false;
                }
            }

            //limit range
            heal.lastEnd.sub(wx, wy).limit(range()).add(wx, wy);

            if(targetBuildings && targetItem == null || unit.maxAccepted(targetItem) > 0){
                //snap to closest building

                World.raycastEachWorld(wx, wy, heal.lastEnd.x, heal.lastEnd.y, (x, y) -> {

                    Tile t = Vars.world.tile(x, y);
                    if(t.solid() && !(t.build instanceof ItemPile.ItemPileBuild)) {

                        if(t.block() != null && t.build == null){
                            Block wall = t.block();
                            t.setBlock(DelveEnvBlocks.oreBlock);
                            ((OreBlock.OreBlockBuild) t.build).parent = wall;

                            //TODO: Switch to derelict when allowDerelictRepair:false actually works as intended
                            t.build.team = Vars.state.rules.defaultTeam;
                        }
                        heal.target = t.build;
                        heal.lastEnd.set(x * tilesize, y * tilesize);

                        return true;
                    }
                    return false;
                });
            }
            if(targetUnits){
                //TODO does not support healing units manually yet
            }
        }
        heal.strength = Mathf.lerpDelta(heal.strength, (float)Mathf.num(this.autoTarget ? mount.target != null : canShoot), 0.2F);
        //create heal effect periodically
        if(canShoot && mount.target instanceof Building b && b.damaged() && (heal.effectTimer += Time.delta) >= reload){
            heal.effectTimer = 0f;
        }

        if(canShoot && mount.target instanceof Building build){
            build.damageContinuous(0.5f);
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        super.draw(unit, mount);

        HealBeamMount heal = (HealBeamMount)mount;

        if(unit.canShoot()){
            float
                    weaponRotation = unit.rotation - 90,
                    wx = unit.x + Angles.trnsx(weaponRotation, x, y),
                    wy = unit.y + Angles.trnsy(weaponRotation, x, y),
                    z = Draw.z();
            RepairTurret.drawBeam(wx, wy, unit.rotation + mount.rotation, shootY, unit.id, mount.target == null || controllable ? null : (Sized)mount.target, unit.team, heal.strength,
                    pulseStroke, pulseRadius, beamWidth + Mathf.absin(widthSinScl, widthSinMag), heal.lastEnd, heal.offset, laserColor, laserTopColor,
                    laser, laserEnd, laserTop, laserTopEnd);
            Draw.z(z);
        }
    }

    @Override
    public void init(){
        super.init();
        bullet.healPercent = fractionRepairSpeed;
    }

    public static class HealBeamMount extends WeaponMount{
        public Vec2 offset = new Vec2(), lastEnd = new Vec2();
        public float strength, effectTimer;

        public HealBeamMount(Weapon weapon){
            super(weapon);
        }
    }
}
