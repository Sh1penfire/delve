package main.content.units;

import arc.func.Prov;
import arc.graphics.Color;
import arc.math.geom.Rect;
import arc.struct.ObjectIntMap;
import arc.util.Log;
import main.entities.units.HaulerUnit;
import main.entities.units.weapons.JabberWeapon;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.part.RegionPart;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
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


    public static UnitType picker, pogo;

    public static void load(){
        setupID();

        picker = new UnitType("picker"){{
            constructor = HaulerUnit::new;
            rotateToBuilding = false;
            omniMovement = true;
            rotateSpeed = 5.5f;
            drag = 0.3f;

            drawCell = false;
            faceTarget = false;
            outlineColor = Color.clear;

            mineSpeed = 0;
            buildSpeed = 1;
            buildRange = 80;
            lightRadius = 95;
            lightColor = Pal.accent;
            lightOpacity = 1;
            fogRadius = 2;
            mechSideSway = mechFrontSway = 0;

            hitSize = 5;
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

            parts.add(new RegionPart("-top"));

            weapons.add(new JabberWeapon("delve-picker-weapon"){{
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


                shootCone = 5;

                reload = 10;
                knockback = 0;
                tileDamage = 5;
                length *= 2;

                rotate = true;
                rotateSpeed = 180;
                mirror = false;
                x = 0;
                y = 0;
                heatColor = Color.valueOf("f9350f");
                cooldownTime = 30f;

                //continuous = alwaysContinuous = true;
                alternate = false;

                weaponLoopSound = Sounds.tankMoveHeavy;
                //pullStrength = 0.2f;
                pullStrength = 0;

                bullet = new BasicBulletType(4f, 5){{
                    sprite = "missile-large";

                    shootEffect = Fx.shootBig;
                    width = 5f;
                    height = 7f;
                    lifetime = 40f;
                    hitSize = 4f;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");
                    frontColor = Color.white;

                    trailChance = 0;
                }};
            }});
        }};

        pogo = new UnitType("pogo"){{
            constructor = HaulerUnit::new;
            rotateToBuilding = false;
            omniMovement = true;
            rotateSpeed = 5.5f;
            drag = 0.1f;

            drawCell = false;
            outlineColor = Color.clear;

            mineSpeed = 0;
            buildSpeed = 1;
            buildRange = 80;
            lightRadius = 95;
            lightColor = Pal.accent;
            lightOpacity = 1;
            fogRadius = 2;

            hitSize = 20;
            treadPullOffset = 3;
            speed = 0.75f;
            health = 850;
            armor = 6f;
            itemCapacity = 50;
            floorMultiplier = 0.95f;
            treadRects = new Rect[]{new Rect(12 - 32f, 7 - 32f, 14, 51)};
            researchCostMultiplier = 0f;

            tankMoveVolume *= 0.32f;
            tankMoveSound = Sounds.tankMoveSmall;

            weapons.add(new JabberWeapon(){{
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


                shootCone = 5;

                reload = 90;

                knockback = 12000;
                tileDamage = 120;

                rotate = true;
                rotateSpeed = 180;
                mirror = false;
                x = 0;
                y = 0;
                heatColor = Color.valueOf("f9350f");
                cooldownTime = 30f;

                //continuous = alwaysContinuous = true;
                alternate = false;
                onlyTiles = true;

                weaponLoopSound = Sounds.tankMoveHeavy;
                pullStrength = 0.05f;

                bullet = new BasicBulletType(4f, 30/60f){{
                    sprite = "missile-large";

                    shootEffect = Fx.shootBig;
                    width = 5f;
                    height = 7f;
                    lifetime = 40f;
                    hitSize = 4f;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");
                    frontColor = Color.white;

                    trailChance = 0;
                }};
            }});
        }};
    }
}
