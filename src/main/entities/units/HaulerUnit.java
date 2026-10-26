package main.entities.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import main.blocks.interfaces.ItemStateHolder;
import main.content.blocks.DelveStorageBlocks;
import main.content.units.DelvePlayerUnits;
import main.graphics.DelveFx;
import main.items.Inventory;
import main.items.ItemState;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.EntityCollisions;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.StatusEntry;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.input.InputHandler;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.environment.Floor;

import java.util.Iterator;

public class HaulerUnit extends MechUnit implements ItemStateHolder, CollisionComp {

    public Inventory inventory = new Inventory(itemCapacity());

    @Override
    public EntityCollisions.SolidPred solidity() {
        return !this.isFlying() && !this.ignoreSolids() ? (x, y) -> EntityCollisions.solid(x, y) && !(Vars.world.tile(x, y).block() == DelveStorageBlocks.warpCore) : null;
    }

    @Override
    public int classId() {
        return DelvePlayerUnits.classID(getClass());
    }

    @Override
    public Inventory inventory() {
        return inventory;
    }

    @Override
    public ItemState topState() {
        return inventory.size == 0 ? null : inventory.peek();
    }

    float totalOffset = 0;
    @Override
    public void handleState(ItemState state) {

        ItemStateHolder.super.handleState(state);

        totalOffset = hitSize;

        totalOffset += inventory.capacity;

        Tmp.v1.trns(rotation, totalOffset).add(x, y);
        state.x = Tmp.v1.x;
        state.y = Tmp.v1.y;
    }

    @Override
    public void removeState(ItemState state) {
        inventory.remove(state);
        state.parrent = null;
        updateStatePositions();
    }

    public void updateStatePositions(){
        totalOffset = hitSize;
        inventory.each(entry -> {
            totalOffset += entry.volume.getModified(inventory);
            Tmp.v1.trns(rotation, totalOffset).add(x, y);
            entry.x = Tmp.v1.x;
            entry.y = Tmp.v1.y;
        });
    }

    @Override
    public void collision(Vec2 delta, float wallX, float wallY) {
        float ax = Math.abs(delta.x);
        float ay = Math.abs(delta.y);
        float normalForce = mass();

        //The ending impulse vector
        Tmp.v1.set(0, 0);

        //Find out which offset is greater
        if(ax > ay){
            Tmp.v1.set(-Mathf.sign(vel.x), 0);
            normalForce *= Math.abs(vel.x);
            vel.x = 0;
        }
        else {
            Tmp.v1.set(0, -Mathf.sign(vel.y));
            normalForce *= Math.abs(vel.y);
            vel.y = 0;
        }

        normalForce = Mathf.maxZero(normalForce - 1);


        impulse(Tmp.v1.scl(normalForce));
    }

    @Override
    public void update() {
        //Vanilla logic, modified
        float bot;
        float left;

        //Used to determine how much walls should kick back
        if (!Vars.net.client() || this.isLocal()) {
            this.move(this.vel.x * Time.delta, this.vel.y * Time.delta);
            this.vel.scl(Math.max(1.0F - this.drag * Time.delta, 0.0F));
        }

        this.updateBuildLogic();
        this.hitTime -= Time.delta / 9.0F;
        this.stack.amount = Mathf.clamp(this.stack.amount, 0, this.itemCapacity());
        this.itemTime = Mathf.lerpDelta(this.itemTime, (float)Mathf.num(this.hasItem()), 0.05F);
        if (this.walked || Vars.net.client() || this.isRemote()) {
            bot = this.deltaLen();
            this.baseRotation = Angles.moveToward(this.baseRotation, this.deltaAngle(), this.type().baseRotateSpeed * Mathf.clamp(bot / this.type().speed / Time.delta) * Time.delta);
            this.walkTime += bot;
            this.walked = false;
        }

        bot = this.walkExtend(false);
        left = this.walkExtend(true);
        float offset = left % 1.0F;
        float range = this.walkExtension;
        int side;
        float cy;
        float maxMagnitude;
        float margin;
        if (!Vars.headless && offset < range && left % 2.0F > 1.0F && !this.isFlying() && !this.inFogTo(Vars.player.team())) {
            side = -Mathf.sign(bot);
            cy = this.hitSize / 2.0F * (float)side;
            maxMagnitude = this.type.mechStride * 1.35F;
            margin = this.x + Angles.trnsx(this.baseRotation, maxMagnitude, cy);
            cy = this.y + Angles.trnsy(this.baseRotation, maxMagnitude, cy);
            if (this.type.stepShake > 0.0F) {
                Effect.shake(this.type.stepShake, this.type.stepShake, margin, cy);
            }

            if (this.type.mechStepParticles) {
                Effect.floorDust(margin, cy, this.hitSize / 8.0F);
            }

            this.type.stepSound.at(margin, cy, this.type.stepSoundPitch + Mathf.range(0.1F), this.type.stepSoundVolume);
        }

        this.walkExtension = offset;
        int accepted;
        if (this.mineTile != null) {
            Building core = this.closestCore();
            Item item = this.getMineResult(this.mineTile);
            if (core != null && item != null && !this.acceptsItem(item) && this.within(core, 220.0F) && !this.offloadImmediately()) {
                accepted = core.acceptStack(this.item(), this.stack().amount, this);
                if (accepted > 0) {
                    Call.transferItemTo(this, this.item(), accepted, this.mineTile.worldx() + Mathf.range(4.0F), this.mineTile.worldy() + Mathf.range(4.0F), core);
                    this.clearItem();
                }
            }

            if ((!Vars.net.client() || this.isLocal()) && !this.validMine(this.mineTile)) {
                this.mineTile = null;
                this.mineTimer = 0.0F;
            } else if (this.mining() && item != null) {
                offset = Time.delta * this.type.mineSpeed * Vars.state.rules.unitMineSpeed(this.team());
                this.mineTimer += offset;
                if (Mathf.chance((double)(0.035F * offset))) {
                    Fx.unitMine.at(this.mineTile.worldx() + Mathf.range(2.0F), this.mineTile.worldy() + Mathf.range(2.0F), 0.0F, item.color);
                }

                if (this.mineTimer >= 50.0F + (this.type.mineHardnessScaling ? (float)item.hardness * 15.0F : 15.0F)) {
                    this.mineTimer = 0.0F;
                    if (Vars.state.rules.sector != null && this.team() == Vars.state.rules.defaultTeam) {
                        Vars.state.rules.sector.info.handleProduction(item, 1);
                    }

                    if (core != null && this.within(core, 220.0F) && core.acceptStack(item, 1, this) == 1 && this.offloadImmediately()) {
                        if (this.item() == item && !Vars.net.client()) {
                            this.addItem(item);
                        }

                        Call.transferItemTo(this, item, 1, this.mineTile.worldx() + Mathf.range(4.0F), this.mineTile.worldy() + Mathf.range(4.0F), core);
                    } else if (this.acceptsItem(item)) {
                        InputHandler.transferItemToUnit(item, this.mineTile.worldx() + Mathf.range(4.0F), this.mineTile.worldy() + Mathf.range(4.0F), this);
                    } else {
                        this.mineTile = null;
                        this.mineTimer = 0.0F;
                    }
                }

                if (!Vars.headless) {
                    Vars.control.sound.loop(this.type.mineSound, this, this.type.mineSoundVolume);
                }
            }
        }

        this.shieldAlpha -= Time.delta / 15.0F;
        if (this.shieldAlpha < 0.0F) {
            this.shieldAlpha = 0.0F;
        }

        Floor floor = this.floorOn();
        if (this.isGrounded() && !this.type.hovering) {
            this.apply(floor.status, floor.statusDuration);
        }

        this.applied.clear();
        this.armorOverride = -1.0F;
        this.speedMultiplier = this.damageMultiplier = this.healthMultiplier = this.reloadMultiplier = this.buildSpeedMultiplier = this.dragMultiplier = 1.0F;
        this.disarmed = false;
        int index;
        if (!this.statuses.isEmpty()) {
            index = 0;

            label399:
            while(true) {
                while(true) {
                    if (index >= this.statuses.size) {
                        break label399;
                    }

                    StatusEntry entry = (StatusEntry)this.statuses.get(index++);
                    entry.time = Math.max(entry.time - Time.delta, 0.0F);
                    if (entry.effect != null && (!(entry.time <= 0.0F) || entry.effect.permanent)) {
                        this.applied.set(entry.effect.id);
                        if (entry.effect.dynamic) {
                            this.speedMultiplier *= entry.speedMultiplier;
                            this.healthMultiplier *= entry.healthMultiplier;
                            this.damageMultiplier *= entry.damageMultiplier;
                            this.reloadMultiplier *= entry.reloadMultiplier;
                            this.buildSpeedMultiplier *= entry.buildSpeedMultiplier;
                            this.dragMultiplier *= entry.dragMultiplier;
                            if (entry.armorOverride >= 0.0F) {
                                this.armorOverride = entry.armorOverride;
                            }
                        } else {
                            this.speedMultiplier *= entry.effect.speedMultiplier;
                            this.healthMultiplier *= entry.effect.healthMultiplier;
                            this.damageMultiplier *= entry.effect.damageMultiplier;
                            this.reloadMultiplier *= entry.effect.reloadMultiplier;
                            this.buildSpeedMultiplier *= entry.effect.buildSpeedMultiplier;
                            this.dragMultiplier *= entry.effect.dragMultiplier;
                        }

                        this.disarmed |= entry.effect.disarm;
                        entry.effect.update(this, entry);
                    } else {
                        if (entry.effect != null) {
                            entry.effect.onRemoved(this);
                        }

                        Pools.free(entry);
                        --index;
                        this.statuses.remove(index);
                    }
                }
            }
        }

        if (Vars.net.client() && !this.isLocal() || this.isRemote()) {
            this.interpolate();
        }

        this.type.update(this);
        if (!Float.isNaN(this.x) && !Float.isNaN(this.y)) {
            float cx;
            if (this.type.bounded) {
                bot = 0.0F;
                left = 0.0F;
                offset = (float)Vars.world.unitHeight();
                range = (float)Vars.world.unitWidth();
                if (Vars.state.rules.limitMapArea && !this.team.isAI()) {
                    bot = (float)(Vars.state.rules.limitY * 8);
                    left = (float)(Vars.state.rules.limitX * 8);
                    offset = (float)(Vars.state.rules.limitHeight * 8) + bot;
                    range = (float)(Vars.state.rules.limitWidth * 8) + left;
                }

                if (!Vars.net.client() || this.isLocal()) {
                    cx = 0.0F;
                    cy = 0.0F;
                    if (this.x < left) {
                        cx += -(this.x - left) / 8.0F;
                    }

                    if (this.y < bot) {
                        cy += -(this.y - bot) / 8.0F;
                    }

                    if (this.x > range - 8.0F) {
                        cx -= (this.x - (range - 8.0F)) / 8.0F;
                    }

                    if (this.y > offset - 8.0F) {
                        cy -= (this.y - (offset - 8.0F)) / 8.0F;
                    }

                    maxMagnitude = 10.0F / Math.max(Time.delta, 1.0F);
                    this.velAddNet(Mathf.clamp(cx * Time.delta, -maxMagnitude, maxMagnitude), Mathf.clamp(cy * Time.delta, -maxMagnitude, maxMagnitude));
                    margin = 8.0F;
                    this.x = Mathf.clamp(this.x, left - margin, range - 8.0F + margin);
                    this.y = Mathf.clamp(this.y, bot - margin, offset - 8.0F + margin);
                }

                if (this.isGrounded()) {
                    this.x = Mathf.clamp(this.x, left, range - 8.0F);
                    this.y = Mathf.clamp(this.y, bot, offset - 8.0F);
                }

                if (this.x < -250.0F + left || this.y < -250.0F + bot || this.x >= range + 250.0F || this.y >= offset + 250.0F) {
                    this.kill();
                }
            }

            if (Float.isNaN(this.health)) {
                this.health = 0.0F;
                this.kill();
            }

            floor = this.floorOn();
            Tile tile = this.tileOn();
            if (this.isFlying() != this.wasFlying) {
                if (this.wasFlying && tile != null) {
                    Fx.unitLand.at(this.x, this.y, floor.isLiquid ? 1.0F : 0.5F, tile.getFloorColor());
                }

                this.wasFlying = this.isFlying();
            }

            if (!this.type.hovering && this.isGrounded() && this.type.emitWalkEffect && (this.splashTimer += Mathf.dst(this.deltaX(), this.deltaY())) >= 7.0F + this.hitSize() / 8.0F) {
                floor.walkEffect.at(this.x, this.y, this.hitSize() / 8.0F, tile != null ? tile.getFloorColor() : floor.mapColor);
                this.splashTimer = 0.0F;
                if (this.type.emitWalkSound) {
                    floor.walkSound.at(this.x, this.y, Mathf.random(floor.walkSoundPitchMin, floor.walkSoundPitchMax), floor.walkSoundVolume);
                }
            }

            this.updateDrowning();
            if (this.wasHealed && this.healTime <= -1.0F) {
                this.healTime = 1.0F;
            }

            this.healTime -= Time.delta / 20.0F;
            this.wasHealed = false;
            if (this.team.isOnlyAI() && Vars.state.isCampaign() && Vars.state.getSector().isCaptured()) {
                this.kill();
            }

            if (!Vars.headless) {
                Vars.control.sound.loop(this.type.loopSound, this, this.type.loopSoundVolume);
                if (this.type.moveSound != Sounds.none) {
                    offset = Mathf.clamp(this.vel.len() / this.type.speed);
                    range = Mathf.lerp(this.type.moveSoundPitchMin, this.type.moveSoundPitchMax, offset);
                    Vars.control.sound.loop(this.type.moveSound, this, this.type.moveSoundVolume * offset, range);
                }
            }

            if (!this.type.supportsEnv(Vars.state.rules.env) && !this.dead) {
                Call.unitEnvDeath(this);
                this.team.data().updateCount(this.type, -1);
            }

            Ability[] var19 = this.abilities;
            int var20 = var19.length;

            for(side = 0; side < var20; ++side) {
                Ability a = var19[side];
                a.update(this);
            }

            if (this.trail != null) {
                this.trail.length = this.type.trailLength;
                offset = this.type.useEngineElevation ? this.elevation : 1.0F;
                range = this.type.engineOffset / 2.0F + this.type.engineOffset / 2.0F * offset;
                cx = this.x + Angles.trnsx(this.rotation + 180.0F, range);
                cy = this.y + Angles.trnsy(this.rotation + 180.0F, range);
                this.trail.update(cx, cy);
            }

            this.drag = this.type.drag * (this.isGrounded() ? this.floorOn().dragMultiplier : 1.0F) * this.dragMultiplier * Vars.state.rules.dragMultiplier;
            if (this.team != Vars.state.rules.waveTeam && Vars.state.hasSpawns() && (!Vars.net.client() || this.isLocal()) && this.hittable()) {
                offset = Vars.state.rules.dropZoneRadius + this.hitSize / 2.0F + 1.0F;
                Iterator var21 = Vars.spawner.getSpawns().iterator();

                while(var21.hasNext()) {
                    Tile spawn = (Tile)var21.next();
                    if (this.within(spawn.worldx(), spawn.worldy(), offset)) {
                        this.velAddNet(Tmp.v1.set(this).sub(spawn.worldx(), spawn.worldy()).setLength(1.1F - this.dst(spawn) / offset).scl(0.45F * Time.delta));
                    }
                }
            }

            if (this.dead || this.health <= 0.0F) {
                this.drag = 0.01F;
                if (Mathf.chanceDelta(0.1)) {
                    Tmp.v1.rnd(Mathf.range(this.hitSize));
                    this.type.fallEffect.at(this.x + Tmp.v1.x, this.y + Tmp.v1.y);
                }

                if (Mathf.chanceDelta(0.2)) {
                    offset = this.type.engineOffset / 2.0F + this.type.engineOffset / 2.0F * this.elevation;
                    range = Mathf.range(this.type.engineSize);
                    this.type.fallEngineEffect.at(this.x + Angles.trnsx(this.rotation + 180.0F, offset) + Mathf.range(range), this.y + Angles.trnsy(this.rotation + 180.0F, offset) + Mathf.range(range), Mathf.random());
                }

                this.elevation -= this.type.fallSpeed * Time.delta;
                if (this.isGrounded() || this.health <= -this.maxHealth * this.type.wreckHealthMultiplier) {
                    Call.unitDestroy(this.id);
                }
            }

            if (tile != null && tile.build != null) {
                tile.build.unitOnAny(this);
            }

            if (tile != null && this.isGrounded() && !this.type.hovering) {
                if (tile.build != null) {
                    tile.build.unitOn(this);
                }

                if (floor.damageTaken > 0.0F) {
                    this.damageContinuous(floor.damageTaken);
                }
            }

            if (tile != null && !this.canPassOn()) {
                if (this.type.canBoost) {
                    this.elevation = 1.0F;
                } else if (!Vars.net.client() && (Vars.headless || !this.isRemote())) {
                    this.kill();
                }
            }

            if (!Vars.net.client() && !this.dead && this.shouldUpdateController()) {
                this.controller.updateUnit();
            }

            if (!this.controller.isValidController()) {
                this.resetController();
            }

            if (this.spawnedByCore && !this.isPlayer() && !this.dead) {
                Call.unitDespawn(this);
            }
        } else {
            this.remove();
        }

        WeaponMount[] var16 = this.mounts;
        index = var16.length;

        for(accepted = 0; accepted < index; ++accepted) {
            WeaponMount mount = var16[accepted];
            mount.weapon.update(this, mount);
        }


        //Inventory logic
        updateStatePositions();
        inventory.each(ItemState::update);
    }

    @Override
    public void draw() {
        super.draw();
        inventory.each(ItemState::draw);

        float dx = x, dy = y - Vars.tilesize * 2.5f;


        Drawf.text(Strings.format("@/@ vu", inventory.size, inventory.capacity), dx, dy, Color.white);
        dy -= Vars.tilesize/2f;
        float width = 8;

        Draw.z(Layer.overlayUI);
        Lines.stroke(2);
        Draw.color(Color.gray);
        Lines.line(dx - width, dy, dx - width + width * 2, dy);
        Lines.stroke(1);
        Draw.color(Color.white);
        Lines.line(dx - width, dy, dx - width + width * inventory.fullness() * 2, dy);
    }

}
