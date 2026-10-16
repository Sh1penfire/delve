package main.blocks.debug;

import arc.math.geom.Vec2;
import mindustry.Vars;

public class LightWallAlt extends LightWallBlock{
    public LightWallAlt(String name) {
        super(name);
    }

    public class LightWallAltBuild extends LightWallBuild{
        @Override
        public void createMesh() {

            mesh = new LightMesh();
            mesh.points.addAll(new LightPoint(new Vec2(0, Vars.tilesize * 8).add(tile)), new LightPoint(new Vec2(0, -Vars.tilesize * 8).add(tile)));
            mesh.vertexes.add(new LightVertex(mesh.points.get(0), mesh.points.get(1)));
            mesh.points.get(0).from = mesh.points.get(1);
            mesh.points.get(1).to = mesh.points.get(0);
            meshes.add(mesh);
            points.addAll(mesh.points);
            verts.addAll(mesh.vertexes);
        }
    }
}
