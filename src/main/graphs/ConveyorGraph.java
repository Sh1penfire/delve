package main.graphs;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.*;
import main.blocks.distrib.DelveConveyorBlock;
import main.blocks.distrib.DelveGraphBlock;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.distribution.Conveyor;

public class ConveyorGraph {

    static int nextId;
    public int id;

    static final int halfTilesize = Vars.tilesize/2;

    public float drawTimestamp;

    public Seq<Vertex> highways = new Seq<>();
    public Seq<Vertex> vertices = new Seq<>();
    public Seq<ConveyorNode> nodes = new Seq<>();
    public Seq<DelveConveyorBlock.DelveConveyorBuild> builds = new Seq<>();
    public Seq<ConveyorEntry> entries = new Seq<>();

    public ConveyorNode root;

    public ConveyorGraph(DelveConveyorBlock.DelveConveyorBuild root){
        create(root);
        id = nextId++;
    }
    public ConveyorGraph(ConveyorNode node){
        root = node;
        id = nextId++;
    }

    @Override
    public String toString() {
        return "Conveyor Graph #:" + id;
    }

    /**
     *
     * @param build
     * @return
     */
    public Vertex create(DelveConveyorBlock.DelveConveyorBuild build){

        int dir = build.rotation;
        Point2 offset = Geometry.d4(dir);
        float offsetLen = Vars.tilesize/2f;
        builds.add(build);

        return build.vertex = addVertex(
                addNode(build.x - offset.x * offsetLen, build.y - offset.y * offsetLen),
                addNode(build.x + offset.x * offsetLen, build.y + offset.y * offsetLen)
        );
    }

    /**
     * Merge a source node onto a target node, and merge this graph onto a specified graph.
     * @param source
     * @param target
     * @param targetGraph
     */
    public void merge(ConveyorNode source, ConveyorNode target, ConveyorGraph targetGraph){
        if(targetGraph != this){
            targetGraph.nodes.addAll(nodes);
            targetGraph.vertices.addAll(vertices);
            targetGraph.highways.addAll(highways);

            builds.each(b -> {
                b.graph = targetGraph;
            });
            targetGraph.builds.add(builds);
        }

        targetGraph.connect(source, target);
    }

    boolean inlineFound;
    Vec2 tmpDirection = new Vec2();

    /**
     * Return a vertex going from source to target, either modifying the toVertex from source, or creating a new one
     * Does *not* return the highway vertex
     * @param source
     * @param target
     * @return
     */
    public Vertex connect(ConveyorNode source, ConveyorNode target){

        //Handle vertex connection first
        Vertex returnVertex = addVertex(source, target);

        boolean sourceLonely = source.fromHighways.size == 0;
        boolean targetLonely = target.toHighway == null;

        //Set tmpDirection to the direction from the source to the target
        tmpDirection.set(target).sub(source).setLength(1);

        if(sourceLonely){
            if(targetLonely){
                Log.info("Case 1: Lonely source, Lonely target");
                //Case 1: Both nodes are lonely, create new highway
                addHighway(source, target);
            }
            else{
                Log.info("Case 2: Lonely source, Connected target");
                //Case 2: Lonely source, connected target.
                //Extend target highway back to source if inline
                Vertex sourceInputHighway = target.toHighway;
                if (tmpDirection.equals(sourceInputHighway.direction)) {
                    Log.info("inline!");
                    target.toHighway.appendStart(source);
                    source.toHighway = target.toHighway;
                    return returnVertex;
                }

                //Otherwise, create a new highway
                addHighway(source, target);
                return returnVertex;
            }
            return returnVertex;
        }
        else{
            if(targetLonely){
                Log.info("Case 3: Connected source, Lonely target");
                //Case 3: Connected source, lonely target

                //If source only has one input highway && that hihgway is inline with the target, then extend the source highway onto the target
                if(source.fromHighways.size == 1){
                    Vertex sourceInputHighway = source.fromHighways.get(0);
                    if(tmpDirection.equals(sourceInputHighway.direction)){
                        sourceInputHighway.appendEnd(target);
                        target.fromHighways.add(sourceInputHighway);
                        return returnVertex;
                    }
                }
                //Otherwise, create a new highway
                addHighway(source, target);
                return returnVertex;
            }
            else{
                Log.info("Case 4: Connected source, Connected target");
                //Case 4: Both source and target have existing highways.

                /*
                If the source only has one input highway && that highway is inline with the target, then
                Merge target output highway to source input highway's end, transfer all data from target highway. Decouple target highway.
                 */
                if(source.fromHighways.size == 1){
                    Vertex sourceInputHighway = source.fromHighways.get(0);
                    //Note: Since we have *both* highways, check if they're both inline instead of infering it from source & target positions
                    if(tmpDirection.equals(sourceInputHighway.direction) && tmpDirection.equals(target.toHighway.direction)){
                        Log.info("Joining two inline highways| Source dir: @, Target dir: @", sourceInputHighway.direction, tmpDirection);
                        highways.remove(target.toHighway);
                        target.toHighway.mergeOnto(sourceInputHighway);
                        return returnVertex;
                    }

                    //If the direction of the source's input highway is inline with the direction from source to target, then extend the source's input
                    if(tmpDirection.equals(sourceInputHighway.direction)) {
                        Log.info("Connecting source's output highway onto new highway's start");
                        //Otherwise, merge existing highway onto target's output highway's start

                        //Set the source's output highway to its current input highway
                        source.toHighway = sourceInputHighway;

                        source.toHighway.appendEnd(target);
                        target.fromHighways.add(source.toHighway);
                        return returnVertex;
                    }
                }

                Log.info("Creating a new highway");
                //Otherwise, merge existing highway onto target's output highway's start
                addHighway(source, target);
                return returnVertex;
            }
        }
    }

    //Unlinks all vertices connected to this node, splits highways
    public void decouple(ConveyorNode node){
        node.fromVertexes.each(vertex -> {
            vertex.end = null;
        });
        node.toVertex.start = null;
    }

    public ConveyorNode addNode(float x, float y){
        ConveyorNode node = new ConveyorNode(x, y);
        nodes.add(node);
        return node;
    }

    //Returns the vertex made (not the highway)
    public Vertex addVertex(ConveyorNode start, ConveyorNode end){

        Vertex vertex = new Vertex(start, end);

        end.fromVertexes.add(vertex);
        start.toVertex = vertex;

        vertices.add(vertex);
        return vertex;
    }

    //Returns the highway made
    public Vertex addHighway(ConveyorNode start, ConveyorNode end){
        Vertex highway = new Vertex(start, end);
        end.fromHighways.add(highway);
        start.toHighway = highway;

        highways.add(highway);
        return highway;
    }


    public void draw() {
        //Don't draw if you've already drawn once this frame
        if (Time.globalTime == drawTimestamp) return;
        drawTimestamp = Time.globalTime;

        Lines.stroke(3);
        vertices.each(v -> {
            Draw.z(Layer.space - 3);
            Draw.color(Color.brown);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(Layer.space - 2);
            Draw.color(Color.orange);
            Fill.square(v.start.x, v.start.y, 3);
            Draw.z(Layer.space - 1);
            Draw.color(Color.purple);
            Fill.circle(v.end.x, v.end.y, 2);
        });

        Lines.stroke(1);
        highways.each(v -> {
            Draw.z(Layer.space);
            Draw.color(Color.green);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(Layer.space + 1);
            Draw.color(Color.red);
            Fill.square(v.start.x, v.start.y, 2);
            Draw.z(Layer.space + 2);
            Draw.color(Color.blue);
            Fill.circle(v.end.x, v.end.y, 1);
        });

    }

    public static class ConveyorEntry{
        public float size;

        public Vertex vertex;
        public @Nullable ConveyorEntry entry;
        public float distToEnd;

        public void draw(){

        }
    }

    //Stores information about the item states
    public static class Vertex{
        public Vertex(ConveyorNode start, ConveyorNode end){
            this.start = start;
            this.end = end;

            length = Mathf.dst(start.x, start.y, end.x, end.y);

            Log.info("Vertex created from (@, @) to (@, @) of length @",
                    Strings.autoFixed(start.x/Vars.tilesize, 0),
                    Strings.autoFixed(start.y/Vars.tilesize, 0),
                    Strings.autoFixed(end.x/Vars.tilesize, 0),
                    Strings.autoFixed(end.y/Vars.tilesize, 0),
                    length
            );
            if(length == 0){
                int p = 0;
                p = 1/p;
            }

            direction.set(end).sub(start).setLength(1);
            nodes.addAll(start);
        }

        //Append the target node onto the end of this vertex.
        public void appendEnd(ConveyorNode target){
            nodes.add(end);
            end = target;
        }
        //Append the target node onto the start of this vertex.
        public void appendStart(ConveyorNode target){
            nodes.add(target);
            nodes.swap(0, nodes.size - 1);
            start = target;
        }
        //Merge this vertex onto another vertex
        public void mergeOnto(Vertex target){
            if(highway) nodes.each(n -> {
                n.toHighway = target;
            });
            target.nodes.add(nodes);
            target.end = this.end;
            target.length += this.length;
        }

        //Flag for if this vertex is a highway or not.
        public boolean highway = false;
        //All nodes ordered from start up to before end
        public Seq<ConveyorNode> nodes = new Seq<>();

        public float length;

        public Vec2 direction = new Vec2();

        public ConveyorNode start;
        public ConveyorNode end;

        public void setLength(float newLength){
            Tmp.v1.set(end).sub(start).setLength(newLength).add(start);
            end.setPos(Tmp.v1);
        }
    }

    public static class ConveyorNode implements Position {
        public ConveyorNode(float x, float y){
            this.x = x;
            this.y = y;
        }
        public ConveyorNode(float x, float y, Building building){
            this.x = x;
            this.y = y;
            this.building = building;
        }
        public float x;
        public float y;

        //The building this node is assigned to.
        public @Nullable Building building;

        //Depth of the node into the tree
        public int depth;

        public boolean loop;

        //All the vertexes that input to this
        public Seq<Vertex> fromVertexes = new Seq<>();

        //If the node doesn't output to anything, assume it is the root node
        public Vertex toVertex;

        //Highway vertex that this is a part of.
        public Vertex toHighway;

        //Highways leading into this node
        public Seq<Vertex> fromHighways = new Seq<>();

        public void setPos(Position pos){
            this.x = pos.getX();
            this.y = pos.getY();
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }
    }

}
