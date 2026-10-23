package main.blocks.debug;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Posc;
import mindustry.world.Block;
import mindustry.world.Tile;

public class LightWallBlock extends Block {


    public static Seq<LightMesh> meshes = new Seq<>();
    public static Seq<LightPoint> points = new Seq<>();

    public static Seq<LightVertex> verts = new Seq<>();

    public LightWallBlock(String name) {
        super(name);
        update = true;
    }

    public static class LightMesh{
        public Seq<LightPoint> points = new Seq<>();
        public Seq<LightVertex> vertexes = new Seq<>();
    }

    public static class LightPoint extends Vec2{
        public LightPoint from, to;

        public LightPoint(float x, float y){
            super(x, y);
        }
        public LightPoint(Vec2 position){
            super(position);
        }
    }

    public static class LightVertex{
        public LightPoint start, end;

        public LightVertex(LightPoint start, LightPoint end){
            this.start = start;
            this.end = end;

            start.to = end;
            end.from = start;
        }
    }

    public static LightMesh squareMesh(float x, float y, float radius){
        LightMesh output = new LightMesh();
        output.points.addAll(
                new LightPoint(x - radius, y - radius),
                new LightPoint(x + radius, y - radius),
                new LightPoint(x + radius, y + radius),
                new LightPoint(x - radius, y + radius)
        );

        output.vertexes.addAll(
                new LightVertex(output.points.get(0), output.points.get(1)),
                new LightVertex(output.points.get(1), output.points.get(2)),
                new LightVertex(output.points.get(2), output.points.get(3)),
                new LightVertex(output.points.get(3), output.points.get(0))
        );

        return output;
    }

    public class LightWallBuild extends Building{

        @Override
        public void onRemoved() {
            super.onRemoved();
            points.removeAll(mesh.points);
            meshes.remove(mesh);
            verts.removeAll(mesh.vertexes);
        }

        public LightMesh mesh = new LightMesh();


        public void createMesh(){
            for (int i = 0; i < 4; i++) {
                Point2 offset = Geometry.d8edge[i];
                mesh.points.add(new LightPoint(new Vec2(offset.x, offset.y).scl(Vars.tilesize * 8).add(tile)));
            }

            for (int i = 0; i < 4; i++){
                mesh.vertexes.add(new LightVertex(mesh.points.get(i), mesh.points.get((i + 1) % 4)));
            }

            meshes.add(mesh);
            points.addAll(mesh.points);
            verts.addAll(mesh.vertexes);
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            this.tile = tile;
            createMesh();
            return super.init(tile, team, shouldAdd, rotation);
        }

        Vec2 current;
        @Override
        public void draw() {
            Draw.color(Color.orange);
            Fill.rect(x, y, 4, 4);

            Lines.stroke(1);
            Color[] inUse = new Color[]{Color.red, Color.green, Color.blue, Color.orange};
            for(int i = 0; i < mesh.vertexes.size; i++){
                LightVertex lightVert = mesh.vertexes.get(i);

                Draw.color(Color.yellow);
                Fill.circle(lightVert.start.x, lightVert.start.y, 1.5f);
                Draw.color(inUse[i]);
                Lines.line(lightVert.start.x, lightVert.start.y, lightVert.end.x, lightVert.end.y);
            }
            Draw.color(Color.white);

        }
    }
}
