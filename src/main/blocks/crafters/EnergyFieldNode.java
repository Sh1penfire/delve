package main.blocks.crafters;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.Shader;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.io.Reads;
import main.blocks.EnergyField;
import main.graphics.Draww;
import main.graphics.TileDrawers;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Layer;
import mindustry.graphics.Shaders;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerNode;

public class EnergyFieldNode extends PowerNode {
    public EnergyFieldNode(String name) {
        super(name);
    }

    public TextureRegion particleRegion;

    @Override
    public void load() {
        super.load();
        particleRegion = Core.atlas.find("particle");
    }

    @Override
    public void drawLaser(float x1, float y1, float x2, float y2, int size1, int size2) {
        Draw.blend(Blending.additive);
        Draw.color(laserColor1);
        Draww.drawChainAlpha(particleRegion, x1, y1, x2, y2, 0.25f, 0.5f, Angles.angle(x2 - x1, y2 - y1), l -> Mathf.absin(l * Mathf.dst(x2 - x1, y2 - y1)/2 - Time.time/5,1, 1));
        Draw.blend();
    }

    @Override
    public void drawOverlay(float x, float y, int rotation) {
        super.drawOverlay(x, y, rotation);
        Tile tile = Vars.world.tileWorld(x, y);
        if(tile == null) return;

        tile = tile.build.tile;

        Geometry.circle(tile.x - sizeOffset, tile.y - sizeOffset, fieldRange, (tx, ty) -> {
            Tile curTile = Vars.world.tile(tx, ty);
            if(curTile == null) return;
            TileDrawers.drawAccent(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);
        });
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Tile tile = Vars.world.tile(x, y);
        if(tile == null) return;

        Geometry.circle(tile.x - sizeOffset, tile.y - sizeOffset, fieldRange, (tx, ty) -> {
            Tile curTile = Vars.world.tile(tx, ty);
            if(curTile == null) return;
            TileDrawers.drawAccent(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);
        });
    }

    public int fieldRange = 4;

    public class EnergyFieldBuild extends PowerNodeBuild{
        public static float drawTimestamp = Time.globalTime;

        public EnergyField.FieldCircle circle = new EnergyField.FieldCircle();

        @Override
        public void update() {
            super.update();
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            setupField(tile);

            return super.init(tile, team, shouldAdd, rotation);
        }

        public void setupField(Tile center){
            circle.set(center.x, center.y, fieldRange);
            EnergyField.add(circle);
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            EnergyField.remove(circle);
        }

        @Override
        public void draw() {
            super.draw();

            if(drawTimestamp == Time.globalTime){
                return;
            }
            drawTimestamp = Time.globalTime;

            Draw.z(Layer.blockUnder);
            for (int tx = 0; tx < Vars.world.width(); tx++) {
                for (int ty = 0; ty < Vars.world.height(); ty++) {
                    if(EnergyField.coverage[tx][ty] == 0) continue;

                    boolean edge = false;
                    for(int i = 0; i < 4; i++){
                        Point2 offset = Geometry.d4(i);
                        int ox = Mathf.clamp(tx + offset.x, 0, EnergyField.coverage.length - 1), oy = Mathf.clamp(ty + offset.y, 0, EnergyField.coverage[tx].length - 1);
                        if(EnergyField.coverage[ox][oy] < 1){
                            edge = true;
                            break;
                        }
                    }

                    if(edge){
                        TileDrawers.drawFieldEdge(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);
                        continue;
                    }
                    TileDrawers.drawField(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);
                }
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            setupField(tile);
        }
    }
}
