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
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Item;

public class ConveyorGraph {

    static int nextId;
    public int id;

    static final int halfTilesize = Vars.tilesize/2;

    public float drawTimestamp;
    public float updateTimestamp;

    public Seq<Highway> highways = new Seq<>();
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
        return Strings.format("Conveyor Graph #@, \nNodes: @\nVertexes: @\nHighways: @", id, nodes, vertices, highways);
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
                Highway sourceInputHighway = target.toHighway;
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
                    Highway sourceInputHighway = source.fromHighways.get(0);
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
                    Highway sourceInputHighway = source.fromHighways.get(0);
                    //Note: Since we have *both* highways, check if they're both inline instead of infering it from source & target positions
                    if(tmpDirection.equals(sourceInputHighway.direction) && tmpDirection.equals(target.toHighway.direction)){
                        Log.info("Joining two inline highways| Source dir: @, Target dir: @", sourceInputHighway.direction, tmpDirection);
                        highways.remove(target.toHighway);
                        target.toHighway.mergeOnto(sourceInputHighway);

                        Fx.explosion.at(target.toHighway.start);
                        Fx.fire.at(target.toHighway.end);

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
    public Highway addHighway(ConveyorNode start, ConveyorNode end){
        Highway highway = new Highway(start, end);
        end.fromHighways.add(highway);
        start.toHighway = highway;

        highways.add(highway);
        return highway;
    }


    public static float drawLayer = Layer.endPixeled;
    ConveyorEntry current;

    //Move an entry along a highway, returning any remaining distance
    //Will move entries into and past intersections
    public float moveState(ConveyorEntry entry, Highway highway, boolean front, float remainingDistance){
        //Find how far it should be from the front of the chain
        float targDist = 0;

        //If this isn't the front most entry, then offset the target distance by the front state's size + this state's size / 2
        if(!front) {
            current = entry.next;
            targDist = (current.size + current.next.size) / 2;
        }

        //Move
        current.distToFront = current.distToFront - remainingDst;

        //If we've moved past the target by any amount, then set that amount to remainingDistance
        remainingDistance = Math.max(Math.max(current.distToFront - targDist, 0) * -1, 0);
        current.distToFront += remainingDistance;

        Highway nextHighway = highway.end.toHighway;
        if(remainingDst > 0 && front && nextHighway != null){
            boolean emptyTarget = nextHighway.entries.isEmpty();

            //Return if the next highway isn't empty & its backmost state doesn't leave enough room for the current entry
            if(!emptyTarget && nextHighway.backState().distToBack < (nextHighway.backState().size + entry.size)/2f) return remainingDistance;

            highway.entries.remove(entry);
            nextHighway.entries.add(entry);
            entry.distToFront = emptyTarget ? nextHighway.length : nextHighway.backState().distToBack;
            return moveState(entry, highway.end.toHighway, front, remainingDst);
        }

        return remainingDistance;
    }

    boolean stationary = false;
    float remainingDst;
    float totalDisplacement;
    public void update(){
        if(Time.time == updateTimestamp) return;;
        updateTimestamp = Time.time;

        highways.each(highway -> {
            //No entries to update, return!
            if(highway.entries.isEmpty()) return;

            remainingDst = highway.speed;
            while(remainingDst > 0){
                if(highway.frontIndex >= highway.entries.size) break;
                //Get the frontmost entry which can still move
                ConveyorEntry current = highway.entries.get(highway.frontIndex);

                //Find how far it should be from the front of the chain
                float targDist = 0;

                //Update how far it should be from the target position
                if(current.next != null) targDist = (current.size + current.next.size)/2;

                //Move
                current.distToFront = current.distToFront - remainingDst;

                //If the current distance is greater than the target distance, check for any future highways
                if(current.distToFront > targDist) {

                    //If theres no additional highways, break
                    if(highway.end.toHighway == null){
                        //Set the total displacement to how much the state moved
                        totalDisplacement = remainingDst;
                        break;
                    }


                }

                //Find the leftover movement
                remainingDst = targDist - current.distToFront;

                //Find the displacement that was traveled
                totalDisplacement += highway.speed - remainingDst;

                //Snap the current entry to its target distance
                current.distToFront = targDist;
                //current's distance is now at desired position, increase the frontIndex and start the move again on the next entry
                highway.frontIndex++;
            }
            highway.backState().distToBack += totalDisplacement;


            highway.entries.each(entry -> {
                entry.update();
                Tmp.v1.set(highway.direction).rotate(180).setLength(entry.distToFront).add(entry.next != null ? entry.next : highway.end);
                entry.x = Tmp.v1.x;
                entry.y = Tmp.v1.y;
            });
        });
    }

    public void draw() {
        //Don't draw if you've already drawn once this frame
        if (Time.globalTime == drawTimestamp) return;
        drawTimestamp = Time.globalTime;

        Lines.stroke(3);
        vertices.each(v -> {
            Draw.z(drawLayer - 3);
            Draw.color(Color.brown);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(drawLayer - 2);
            Draw.color(Color.orange);
            Fill.square(v.start.x, v.start.y, 3);
            Draw.z(drawLayer - 1);
            Draw.color(Color.purple);
            Fill.circle(v.end.x, v.end.y, 2);
        });

        Lines.stroke(1);
        highways.each(v -> {
            Draw.z(drawLayer);
            Draw.color(Color.green);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(drawLayer + 1);
            Draw.color(Color.red);
            Fill.square(v.start.x, v.start.y, 2);
            Draw.z(drawLayer + 2);
            Draw.color(Color.blue);
            Fill.circle(v.end.x, v.end.y, 1);

            v.entries.each(entry -> {
                entry.draw();
            });
        });

    }

    public static class ConveyorEntry implements Position{

        public ConveyorEntry(Item item, Highway highway){
            this.item = item;
            this.highway = highway;
        }
        public ConveyorEntry(Item item, float size){
            this.item = item;
            this.size = size;
        }

        public float size = 8;

        public Highway highway;

        //Next conveyor entry
        public @Nullable ConveyorEntry next;

        //Distance to the endpoint of where the item should travel to.
        public float distToFront;

        //Distance from the back state behind this entry
        public float distToBack;


        public Item item = Items.copper;

        //The conveyor nodes this entry is currently on.
        public Seq<ConveyorNode> nodes;

        public float x = 0;
        public float y = 0;
        public boolean stationary;

        public void update(){
        }

        public void draw(){
            Draw.color();
            Draw.rect(item.uiIcon, x, y);
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

    //build.graph.handleEntry(build.node.toHighway, DelveItems.shadesteel.itemState.get())
    public ConveyorEntry entry(Item item, float size){
        return new ConveyorEntry(item, size);
    }

    public boolean handleEntry(Highway highway, ConveyorEntry itemState){
        itemState.highway = highway;

        if(entries.size == 0){
            entries.add(itemState);
            highway.entries.add(itemState);
            itemState.distToFront = highway.length;
        }
        else{
            //Attach the entry onto the last possible
            itemState.next = highway.entries.get(highway.entries.size - 1);

            entries.add(itemState);
            highway.entries.add(itemState);
            itemState.distToFront = Tmp.v1.set(highway.start).dst(itemState.next) - (itemState.next.size + itemState.size)/2f;
        }
        return false;
    }

    //Stores information about the connections between intersections & corners, as well as item states
    public static class Highway{

        @Override
        public String toString() {
            return Strings.format("Highway| From: @, To: @", start, end);
        }

        public float speed = Vars.tilesize/60f * 2;

        public Highway(ConveyorNode start, ConveyorNode end){
            this.start = start;
            this.end = end;

            length = Mathf.dst(start.x, start.y, end.x, end.y);

            Log.info("Highway created from (@, @) to (@, @) of length @",
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
            if(entries.size > 0){
                frontIndex = 0;
                entries.get(0).distToFront += end.dst(target);
            }
            nodes.add(end);
            end = target;
            calculateLength();
        }
        //Append the target node onto the start of this vertex.
        public void appendStart(ConveyorNode target){
            if(entries.size > 0){
                backState().distToBack += start.dst(target);
            }
            nodes.add(target);
            nodes.swap(0, nodes.size - 1);
            start = target;
            calculateLength();
        }
        //Merge this vertex onto the front of another vertex
        public void mergeOnto(Highway target){
            nodes.each(n -> {
                n.toHighway = target;
            });

            if(target.entries.size > 0){
                ConveyorEntry targetFrontState = target.entries.get(0);

                //Jump the merge target's entries back by the gap between the two highway's endpoints
                targetFrontState.distToFront += target.end.dst(start);

                target.frontIndex = 0;

                //If this highway has any items, jump the ending item of the merge target back by the back spacing of the backmost state of this highway
                if(entries.size > 0){
                    ConveyorEntry thisBackEntry = this.entries.get(this.entries.size - 1);
                    target.entries.get(0).next = thisBackEntry;

                    target.entries.get(0).distToFront += thisBackEntry.distToBack;

                    //Snap on this highway's back entry to the target highway's front entry
                    thisBackEntry.distToBack = target.entries.get(0).distToFront;
                }
                else{
                    targetFrontState.distToFront += length;
                }
            }
            entries.add(target.entries);
            target.entries = entries;
            target.nodes.add(nodes);

            target.end = this.end;

            //Remap the current end node of this highway into the target's end node
            this.end.fromHighways.clear();
            this.end.fromHighways.add(target);

            target.length += this.length;
            target.calculateLength();
        }

        //Flag for if this highway is straight and extendable
        public boolean straight = true;

        //All nodes ordered from start up to before end
        public Seq<ConveyorNode> nodes = new Seq<>();

        public float length;

        public Vec2 direction = new Vec2();

        public ConveyorNode start;
        public ConveyorNode end;

        //Entries on the highway
        public Seq<ConveyorEntry> entries = new Seq<>();

        //Entry at the front of the stack that's still moving
        public int frontIndex = 0;

        public ConveyorEntry backState(){
            return entries.get(entries.size - 1);
        }

        public void calculateLength(){
            length = Mathf.dst(start.x, start.y, end.x, end.y);
        }

        public void setLength(float newLength){
            Tmp.v1.set(end).sub(start).setLength(newLength).add(start);
            end.setPos(Tmp.v1);
        }
    }


    //Stores information about the individual connections between blocks
    public static class Vertex{

        @Override
        public String toString() {
            return Strings.format("Vertex| From: @, To: @", start, end);
        }

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
        }
        //Flag for if this vertex is straight
        public boolean straight = true;

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

        @Override
        public String toString() {
            return Strings.format("(@, @)",x/Vars.tilesize, y/Vars.tilesize);
        }

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
        public Highway toHighway;

        //Highways leading into this node
        public Seq<Highway> fromHighways = new Seq<>();

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
