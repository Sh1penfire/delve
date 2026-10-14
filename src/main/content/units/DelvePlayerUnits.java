package main.content.units;

import arc.graphics.Color;
import arc.math.geom.Rect;
import main.entities.units.HaulerUnit;
import main.entities.units.weapons.CrusherWeapon;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class DelvePlayerUnits {

    public static UnitType hauler;

    public static void load(){
        hauler = new UnitType("hauler"){{
            constructor = HaulerUnit::new;
            rotateToBuilding = false;
            omniMovement = false;

            mineSpeed = 0;
            buildSpeed = 1;
            buildRange = 80;
            lightRadius = 95;
            lightColor = Pal.accent;
            lightOpacity = 0.45f;

            hitSize = 12f;
            treadPullOffset = 3;
            speed = 1.25f;
            rotateSpeed = 7.5f;
            health = 850;
            armor = 6f;
            itemCapacity = 50;
            floorMultiplier = 0.95f;
            treadRects = new Rect[]{new Rect(12 - 32f, 7 - 32f, 14, 51)};
            researchCostMultiplier = 0f;

            tankMoveVolume *= 0.32f;
            tankMoveSound = Sounds.tankMoveSmall;

            weapons.add(new CrusherWeapon(){{

                shootSound = Sounds.shootStell;
                layerOffset = 0.0001f;
                reload = 50f;
                shootY = 4.5f;
                recoil = 1f;
                rotate = true;
                rotateSpeed = 2.2f;
                mirror = true;
                x = 4;
                y = 8;
                heatColor = Color.valueOf("f9350f");
                cooldownTime = 30f;

                bullet = new BasicBulletType(4f, 40){{
                    sprite = "missile-large";
                    smokeEffect = Fx.shootBigSmoke;
                    shootEffect = Fx.shootBigColor;
                    width = 5f;
                    height = 7f;
                    lifetime = 40f;
                    hitSize = 4f;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");
                    frontColor = Color.white;
                    trailWidth = 1.7f;
                    trailLength = 5;
                    despawnEffect = hitEffect = Fx.hitBulletColor;
                }};
            }});
        }};
    }
}
