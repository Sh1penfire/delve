package main.content;

import main.units.weapons.LaserGun;
import mindustry.audio.SoundControl;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.weapons.RepairBeamWeapon;

public class DelveUnits {

    public static UnitType coreUnit;

    public static void load(){
        coreUnit = new UnitType("the fucking child"){{
            flying = true;
            constructor = UnitEntity::create;
            aimDst = 0f;

            weapons.add(new LaserGun(){{
                widthSinMag = 0.11f;
                reload = 5f;
                x = 0f;
                y = 6.5f;
                rotate = false;
                shootY = 0f;
                beamWidth = 0.7f;
                repairSpeed = 3.1f;
                fractionRepairSpeed = 0.06f;
                shootCone = 15f;
                mirror = false;

                targetUnits = false;
                targetBuildings = true;
                autoTarget = false;
                controllable = true;
                laserColor = Pal.accent;
                healColor = Pal.accent;

                bullet = new BulletType(){{
                    maxRange = 60f;
                }};
            }});
        }};
    }

}
