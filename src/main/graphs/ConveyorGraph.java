package main.graphs;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import main.blocks.distrib.DelveConveyorBlock;
import main.blocks.distrib.DelveGraphBlock;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.distribution.Conveyor;

public class ConveyorGraph {

    static final int halfTilesize = Vars.tilesize/2;

    public float drawTimestamp;

    public Seq<Vertex> vertices = new Seq<>();
    public Seq<ConveyorNode> nodes = new Seq<>();
    public Seq<DelveConveyorBlock.DelveConveyorBuild> builds = new Seq<>();
    public ConveyorNode root;

    public ConveyorGraph(DelveConveyorBlock.DelveConveyorBuild root){
        create(root);
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


    boolean inlineFound;
    /**
     * Merge this graph onto another graph
     * @param source
     * @param target
     * @param targetGraph
     * @param inline whether to join the vertices, or create a new segment on the target vertex
     */
    public void merge(Vertex source, Vertex target, DelveConveyorBlock.DelveConveyorBuild targetBuild, ConveyorGraph targetGraph, boolean inline){
        if(targetGraph != this){
            targetGraph.nodes.addAll(nodes);
            targetGraph.vertices.addAll(vertices);

            builds.each(b -> {
                b.graph = targetGraph;
            });
        }

        if(inline){
            Log.info(target.start.fromVertexes.size);
            //Check for if this conveyor piece is line but split
            if(target.start.fromVertexes.size > 0){
                inlineFound = false;


                float ox = Mathf.clamp(source.end.x - source.start.x, 0, 1);
                float oy = Mathf.clamp(source.end.y - source.start.y, 0, 1);;
                Log.info("Source | Offset x: @, Offset y: @", ox, oy);
                target.start.fromVertexes.each(v -> {
                    float vertox = Mathf.clamp(v.end.x - v.start.x, 0, 1);
                    float vertoy = Mathf.clamp(v.end.y - v.start.y, 0, 1);;
                    Log.info("Vertex | Offset x: @, Offset y: @", vertox, vertoy);
                    //find the line that's inline with this one
                    if(ox == vertox && oy == vertoy){
                        Log.info("found inline!");
                        inlineFound = true;
                        targetGraph.connect(source.start, v.end);
                        targetGraph.decouple(v);
                    };
                });
                if(inlineFound) return;
            }

            //Remove the source vertex
            targetGraph.decouple(target);
            //Connect start/end
            targetGraph.connect(source.start, target.end);
        }
        else {
            source.setLength(source.length + halfTilesize);
            Vertex newVert = targetGraph.attachVertices(target, source);

            targetBuild.vertex = newVert;
        }
    }

    public void decouple(Vertex vertex){
        if(vertex.start.toVertex == vertex) vertex.start.toVertex = null;
        vertex.end.fromVertexes.remove(vertex);
        vertices.remove(vertex);
    }

    //Attach a vertex onto the target vertex. Returns the new vertex split off from the target vertex, or the target vertex itself
    public Vertex attachVertices(Vertex target, Vertex attached){
        ConveyorNode originalStart = target.start;
        ConveyorNode originalEnd = target.end;

        Log.info("Target start pos: @", Tmp.v1.set(target.start));
        Log.info("Attached end pos: @", Tmp.v1.set(attached.end));

        if(target.start.x == attached.end.x && target.start.y == attached.end.y){
            connect(attached.start, target.start);
            return target;
        }
        //Make the source vertex's start connect to the attached vertex's end
        connect(originalStart, attached.end);
        target.end = attached.end;

        //Make the attached vertex's end connect to the target vertex's start
        originalEnd.fromVertexes.remove(target);
        connect(attached.end, originalEnd);

        return attached.end.toVertex;
    }

    Vertex extending;
    /**
     * Attach a building to the graph. Extends out the target's vertex based on source's position relative to it <br>
     * Ex: Extends the start of the vertex back if source is being attached from the behind, extends the end of the vertex fowards if the source is being attached from the front
     * @param source
     * @param target
     * @param vertex
     */
    public void attach(DelveConveyorBlock.DelveConveyorBuild source, DelveConveyorBlock.DelveConveyorBuild target, Vertex vertex){
        int dir = source.relativeTo(target);
        Point2 offset = Geometry.d4(dir);

        //If the conveyors are facing eachother, don't connect
        if(dir == -1 || source.rotation == (target.rotation + 2) % 4) return;

        source.graph = this;
        float length = source.block.size * Vars.tilesize;

        //Check if the building is inline with the target's rotation
        if(dir == target.rotation || dir == (target.rotation + 2) % 4){
            //Extend the current vertex
            boolean front = dir != target.rotation;

            extending = vertex;
            //Find any inline sources that are closer
            if(vertex.start.fromVertexes.size > 0){
                inlineFound = false;

                Log.info("Source | Offset x: @, Offset y: @", offset.x, offset.y);
                vertex.start.fromVertexes.each(v -> {
                    float vertox = Mathf.clamp(v.end.x - v.start.x, 0, 1);
                    float vertoy = Mathf.clamp(v.end.y - v.start.y, 0, 1);;
                    Log.info("Vertex | Offset x: @, Offset y: @", vertox, vertoy);
                    //find the line that's inline with this one
                    if(offset.x == vertox && offset.y == vertoy){
                        Log.info("found inline!");
                        inlineFound = true;
                        extending = v;
                    };
                });
            }

            ConveyorNode extendedNode = front ? extending.end : extending.start;

            extendedNode.x -= offset.x * length;
            extendedNode.y -= offset.y * length;
            source.vertex = extending;
            extending.length += length;

            //Add the building to the graph
            builds.add(source);
            source.vertex = extending;
        }
        else{
            if(dir == source.rotation);

            ConveyorNode originalEnd = vertex.end;
            ConveyorNode originalStart = vertex.start;

            //Join the two from the side if the source is facing into the target
            Vertex newVert = create(source);
            newVert.end.x += offset.x * halfTilesize;
            newVert.end.y += offset.y * halfTilesize;
            source.vertex = newVert;

            Log.info("Target start pos: @", Tmp.v1.set(vertex.start));
            Log.info("Attached end pos: @", Tmp.v1.set(newVert.end));

            //If the source's new vertex ends up on the start of the target vertex, then just link the end of thee source up to the beginning of the target and return.
            if(vertex.start.x == newVert.end.x && vertex.start.y == newVert.end.y){

                connect(newVert.start, vertex.start);
                return;
            }

            //Make the target vertex's start connect to the attached vertex's end
            connect(originalStart, newVert.end);
            vertex.end = newVert.end;

            //Make the attached vertex's end connect to the target vertex's start
            originalEnd.fromVertexes.remove(vertex);
            Vertex splitVert = connect(newVert.end, originalEnd);

            //Add the building to the graph
            builds.add(source);
            target.vertex = newVert.end.toVertex;
        }
    }

    /**
     * Return a vertex going from source to target, either modifying the toVertex from source, or creating a new one
     * @param source
     * @param target
     * @return
     */
    public Vertex connect(ConveyorNode source, ConveyorNode target){
        if(source.toVertex == null){
            return addVertex(source, target);
        }
        source.toVertex.end = target;
        target.fromVertexes.add(source.toVertex);
        return source.toVertex;
    }

    public ConveyorNode addNode(float x, float y){
        ConveyorNode node = new ConveyorNode(x, y);
        nodes.add(node);
        return node;
    }

    public Vertex addVertex(ConveyorNode start, ConveyorNode end){

        Vertex vertex = new Vertex(start, end);

        end.fromVertexes.add(vertex);
        start.toVertex = vertex;

        vertices.add(vertex);
        return vertex;
    }


    public void draw() {
        //Don't draw if you've already drawn once this frame
        if (Time.globalTime == drawTimestamp) return;
        drawTimestamp = Time.globalTime;

        vertices.each(v -> {
            Draw.z(Layer.space);
            Draw.color(Color.green);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(Layer.space + 1);
            Draw.color(Color.red);
            Fill.square(v.start.x, v.start.y, 4);
            Draw.z(Layer.space + 2);
            Draw.color(Color.blue);
            Fill.circle(v.end.x, v.end.y, 2);
        });

    }

    //Stores information about the item states
    public static class Vertex{
        public Vertex(ConveyorNode start, ConveyorNode end){
            this.start = start;
            this.end = end;

            length = Mathf.dst(start.x, start.y, end.x, end.y);

            Log.info("Vertex created from (@ ,@) to (@, @) of length @",
                    Strings.autoFixed(start.x, 0),
                    Strings.autoFixed(start.y, 0),
                    Strings.autoFixed(end.x, 0),
                    Strings.autoFixed(end.y, 0),
                    length
            );
            if(length == 0){
                int p = 0;
                p = 1/p;
            }
        }

        public Seq<Building> buildings = new Seq<>();
        public float length;

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
        public float x;
        public float y;
        //Depth of the node into the tree
        public int depth;

        public boolean loop;

        //All the vertexes that input to this
        public Seq<Vertex> fromVertexes = new Seq<>();

        //If the node doesn't output to anything, assume it is the root node
        public Vertex toVertex;

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
