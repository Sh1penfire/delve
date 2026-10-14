package main.entities.units.weapons;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Tmp;
import main.blocks.environment.ItemPile;
import main.blocks.environment.ItemStatePile;
import main.blocks.environment.OreBlock;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveEnvBlocks;
import main.items.ItemFunction;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.tilesize;

public class CrusherWeapon extends Weapon {

    float length = tilesize * 2;

    @Override
    public void update(Unit unit, WeaponMount mount) {
        super.update(unit, mount);

        float
                mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
                mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);
        bullet.shootEffect.at(mountX, mountY);

        if(mount.shoot){
            Tmp.v1.trns(unit.rotation, length).add(mountX, mountY);
            World.raycastEachWorld(mountX, mountY, Tmp.v1.x, Tmp.v1.y, (tx, ty) -> {
                if(Mathf.chance(bullet.trailChance))bullet.hitEffect.at(tx * tilesize, ty * tilesize);

                Tile t = Vars.world.tile(tx, ty);

                if(t == null) return true;

                //Switch static walls to ore block
                if(t.build == null && t.block().solid){
                    Block wall = t.block();
                    t.setBlock(DelveEnvBlocks.oreBlock);
                    ((OreBlock.OreBlockBuild) t.build).parrent = wall;

                    //TODO: Switch to derelict when allowDerelictRepair:false actually works as intended
                    t.build.team = Vars.state.rules.defaultTeam;
                };

                //if the build is still null then yeah
                if(t.build == null) return false;

                if(t.build instanceof ItemStatePile.ItemStatePileBuild pile){
                    ItemState state = pile.topState();
                    if(unit instanceof ItemStateHolder holder && holder.acceptsItemState(state)){
                        pile.removeState(state);


                        holder.handleState(ItemFunction.crushing.process(state));
                    }
                    return false;
                }
                if(t.build.block instanceof OreBlock) t.build.damage(bullet.damage);

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
