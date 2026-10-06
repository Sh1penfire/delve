package main.graphs;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import main.blocks.distrib.DelveGraphBlock;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Layer;

//A directional tree with an edge case for loops which stores and moves item states
public class ItemDistributionGraph {

    public ItemDistributionGraph(DelveGraphBlock.DelveGraphBuild root){
        create(root);
    }

    public float drawTimestamp;
    static int halfTilesize = Vars.tilesize/2;

    public Seq<ConveyorNode> nodes = new Seq<>();
    public Seq<Vertex> vertexes = new Seq<>();
    public Seq<DelveGraphBlock.DelveGraphBuild> builds;

    public static class ConveyorNode{
        public ConveyorNode(float x, float y){
            this.x = x;
            this.y = y;
        }
        public ConveyorNode(float x, float y, Building build){
            this(x, y);
            source = build;
        }
        public float x;
        public float y;
        public Building source;
    }

    /**
     * Creates a graph with a vertex spanning the building's size, with nodes at the start and end of the building.
     */

    public Vertex create(DelveGraphBlock.DelveGraphBuild build){

        int dir = build.rotation;
        Point2 offset = Geometry.d4(dir);
        float offsetLen = Vars.tilesize/2f;

        return build.vertex = addVertex(
                addNode(build.x - offset.x * offsetLen, build.y - offset.y * offsetLen, build),
                addNode(build.x + offset.x * offsetLen, build.y + offset.y * offsetLen, build),
                build
        );
    }

    /**
     * Attach a building to the graph. Extends out the target's vertex based on source's position relative to it <br>
     * Ex: Extends the start of the vertex back if source is being attached from the behind, extends the end of the vertex fowards if the source is being attached from the front
     * @param source
     * @param target
     * @param vertex
     */
    public void addBuilding(DelveGraphBlock.DelveGraphBuild source, DelveGraphBlock.DelveGraphBuild target, Vertex vertex){
        int dir = source.relativeTo(target);
        Point2 offset = Geometry.d4(dir);

        //If the conveyors are facing eachother, don't connect
        if(dir == -1 || source.rotation == (target.rotation + 2) % 4) return;

        source.graph = this;
        float length = source.block.size * Vars.tilesize;

        if(dir == target.rotation || dir == (target.rotation + 2) % 4){
            boolean front = dir != target.rotation;

            ConveyorNode extendedNode = front ? vertex.end : vertex.start;

            extendedNode.x -= offset.x * length;
            extendedNode.y -= offset.y * length;
            source.vertex = vertex;
            vertex.length += length;
        }
        else{
            if(dir == source.rotation);
            //Join the two from the side if the source is facing into the target
            Vertex newVert = create(source);
            newVert.end.x += offset.x * halfTilesize;
            newVert.end.y += offset.y * halfTilesize;
        }
    }

    public void joinVertex(Vertex source, Vertex target){

    }

    public void attach(ItemDistributionGraph graph){

    }
    public void attach(ItemDistributionGraph graph, DelveGraphBlock.DelveGraphBuild source, DelveGraphBlock.DelveGraphBuild target, Vertex vertex){

    }

    //Assign a building to the vertex created
    public Vertex addVertex(ConveyorNode start, ConveyorNode end, Building build){
        Vertex vertex = addVertex(start, end);
        vertex.buildings.add(build);
        return vertex;
    }

    public Vertex addVertex(ConveyorNode start, ConveyorNode end){
        Vertex vertex = new Vertex(start, end);
        vertexes.add(vertex);
        return vertex;
    }

    public ConveyorNode addNode(float x, float y, Building building){
        ConveyorNode node = new ConveyorNode(x, y, building);
        nodes.add(node);
        return node;
    }

    /**
     * Splits a vertex, creating a new vertex from the reer end of the split.
     * @param vertex The original vertex
     * @param fromStart the distance from the end the split happens at
     * @return returns a list with the old vertex (0) and new vertex (1)
     */
    public Vertex[] splitVertex(Vertex vertex, float fromStart){
        float initialLength = vertex.length;
        Log.debug("Start position at @,@",vertex.start.x, vertex.start.y);
        Log.debug("End position at @,@",vertex.end.x, vertex.end.y);

        Log.debug("Lerp amount: @", fromStart/vertex.length);

        float splitf = fromStart/vertex.length;
        Tmp.v1.set(vertex.start.x, vertex.start.y).lerp(vertex.end.x, vertex.end.y, splitf);
        Fx.explosion.at(Tmp.v1);


        Log.debug("Tmp v1 position at @,@",Tmp.v1.x, Tmp.v1.y);
        ConveyorNode split = addNode(Tmp.v1.x, Tmp.v1.y, Vars.world.buildWorld(Tmp.v1));
        Log.debug("Split position at @,@",split.x, split.y);

        Vertex newVertex = addVertex(vertex.start, split);
        newVertex.length = initialLength - fromStart;

        vertex.start = split;
        vertex.length = fromStart;


        return new Vertex[]{vertex, newVertex};
    }


    public class Vertex{

        public Vertex(ConveyorNode start, ConveyorNode end){
            this.start = start;
            this.end = end;
            length = Mathf.dst(start.x, start.y, end.x, end.y);
        }

        public Seq<Building> buildings = new Seq<>();
        public float length;
        public ConveyorNode start, end;
    }

    public void draw(){
        //Don't draw if you've already drawn once this frame
        if(Time.globalTime == drawTimestamp) return;
        drawTimestamp = Time.globalTime;


        Lines.stroke(1);
        vertexes.each(v -> {
            Draw.z(Layer.space);
            Draw.color(Color.green);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(Layer.space + 1);
            Draw.color(Color.red);
            Fill.circle(v.start.x, v.start.y, 4);
            Draw.z(Layer.space + 2);
            Draw.color(Color.blue);
            Fill.circle(v.end.x, v.end.y, 2);
        });
    }
}
