package main.items.types;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Teamc;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class TurretItem extends DelveItem {

    public float reload = 20;
    public int clip = 10;
    public float range = Vars.tilesize * 20;
    public float rotateSpeed = 3f;
    public float cone = 15;

    public BulletType bullet = null;

    public TextureRegion baseRegion, turretRegion;


    public TurretItem(String name, Color color) {
        super(name, color);
        bullet = ((ItemTurret) Blocks.duo).ammoTypes.get(Items.copper);
    }
    public TurretItem(String name) {
        this(name, Color.white);
    }

    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find("block-1");
        turretRegion = Blocks.duo.fullIcon;
        uiIcon = fullIcon = turretRegion;
    }


    public class TurretItemState extends UpdateItemState{

        public float reloadCounter = 0;
        public float rotation = 90;

        public TurretItemState(DelveItem item, float size) {
            super(item, size);
        }

        @Override
        public void update() {
            super.update();

            reloadCounter = Mathf.clamp(reloadCounter + Time.delta, 0, reload);

            var target = Units.closestEnemy(parrent.team(), x, y, range, u -> true);
            if(target == null) return;

            float targetAngle = Tmp.v1.set(x, y).angleTo(target);


            rotation = Angles.moveToward(rotation, targetAngle, rotateSpeed);


            if(reloadCounter >= reload && Angles.within(rotation, targetAngle,cone)){
                reloadCounter -= reload;
                shoot();
            }
        }

        public void shoot(){

            if(parrent instanceof Teamc teamed){
                bullet.create(teamed, x, y, rotation);
            }
            else {
                bullet.create(null, parrent.team(), x, y, rotation);
            }
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.rect(turretRegion, x, y, rotation - 90);
        }
    }
}
