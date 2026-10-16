package main.content.units;

import arc.func.Prov;
import arc.graphics.Color;
import arc.math.geom.Rect;
import arc.struct.ObjectIntMap;
import arc.util.Log;
import main.entities.units.HaulerUnit;
import main.entities.units.weapons.CrusherWeapon;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import arc.struct.ObjectMap.Entry;

public class DelvePlayerUnits {

    private static Entry<Class<? extends Entityc>, Prov<? extends Entityc>>[] types = new Entry[]{
            prov(HaulerUnit.class, HaulerUnit::new)
    };

    private static ObjectIntMap<Class<? extends Entityc>> idMap = new ObjectIntMap<>();

    /**
     * Internal function to flatmap {@code Class -> Prov} into an {@link Entry}.
     * @author GlennFolker
     */

    private static <T extends Entityc> Entry<Class<T>, Prov<T>> prov(Class<T> type, Prov<T> prov){
        Entry<Class<T>, Prov<T>> entry = new Entry<>();
        entry.key = type;
        entry.value = prov;
        return entry;
    }

    /**
     * Setups all entity IDs and maps them into {@link EntityMapping}.
     * Find all free ids to map to, then put the Entry(s) from types into the idMap. Starts searching after the last known index of a vanilla Entry
     */

    private static void setupID(){
        int start = 33;
        int[] free = new int[types.length];
        for (int i = start, j = 0; i < EntityMapping.idMap.length; i++) {
            if(EntityMapping.idMap[i] == null) free[j++] = i;
            if(j > free.length - 1) break;
        }

        Log.info("setting up map");
        for (int i = 0; i < free.length; i++) {
            idMap.put(types[i].key, free[i]);
            EntityMapping.idMap[free[i]] = types[i].value;
        }
    }

    public static <T extends Entityc> int classID(Class<T> type){
        return idMap.get(type, -1);
    }


    public static UnitType hauler;

    public static void load(){
        setupID();

        hauler = new UnitType("hauler"){{
            constructor = HaulerUnit::new;
            rotateToBuilding = false;
            omniMovement = true;
            rotateSpeed = 5.5f;
            drag = 0.3f;

            drawCell = false;
            outlineColor = Color.clear;

            mineSpeed = 0;
            buildSpeed = 1;
            buildRange = 80;
            lightRadius = 95;
            lightColor = Pal.accent;
            lightOpacity = 0.45f;

            hitSize = 20;
            treadPullOffset = 3;
            speed = 1.25f;
            health = 850;
            armor = 6f;
            itemCapacity = 50;
            floorMultiplier = 0.95f;
            treadRects = new Rect[]{new Rect(12 - 32f, 7 - 32f, 14, 51)};
            researchCostMultiplier = 0f;

            tankMoveVolume *= 0.32f;
            tankMoveSound = Sounds.tankMoveSmall;

            weapons.add(new CrusherWeapon(){{
                /*
                shootStatus = new StatusEffect("grinding"){{
                    speedMultiplier = 0.15f;
                    dragMultiplier = 0.08f;
                }};

                 */
                shootStatusDuration = 15;
                linearWarmup = true;
                shootWarmupSpeed = 0.5f/60f;

                shootSound = Sounds.shootStell;
                layerOffset = 0.0001f;
                shootY = 4.5f;
                recoil = 1f;
                rotate = true;
                rotateSpeed = 2.2f;
                rotationLimit = 15;
                mirror = true;
                x = 4;
                y = 8;
                heatColor = Color.valueOf("f9350f");
                cooldownTime = 30f;

                continuous = alwaysContinuous = true;
                alternate = false;

                weaponLoopSound = Sounds.tankMoveHeavy;
                pullStrength = 0.2f;


                bullet = new BasicBulletType(4f, 40/60f){{
                    sprite = "missile-large";
                    smokeEffect = Fx.shootBigSmoke;
                    shootEffect = Fx.shootBigColor;
                    width = 5f;
                    height = 7f;
                    lifetime = 40f;
                    hitSize = 4f;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");
                    frontColor = Color.white;

                    trailChance = 0;
                    shootEffect = Fx.generatespark;
                    despawnEffect = hitEffect = Fx.hitBulletSmall;
                }};
            }});
        }};
    }
}
