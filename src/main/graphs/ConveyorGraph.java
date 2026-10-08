package main.graphs;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.*;
import main.blocks.distrib.DelveConveyorBlock;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.graphics.Drawf;
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

    //No root node for this graph

    public ConveyorGraph(ConveyorNode node){
        this();
        nodes.add(node);
    }

    public ConveyorGraph(){
        id = nextId++;
    }

    @Override
    public String toString() {
        return Strings.format("Conveyor Graph #@, \nNodes: @\nVertexes: @\nHighways: @", id, nodes, vertices, highways);
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
    //Removes a vertex, and derefrences it
    public void removeVertex(Vertex vertex){
        vertex.start.toVertex = null;
        vertex.end.fromVertexes.remove(vertex);
        vertices.remove(vertex);
    }
    //Removes a highway, and derefrences it
    public void removeHighway(Highway highway){
        highway.start.toHighway = null;
        highway.end.fromHighways.remove(highway);
        highways.remove(highway);
    }

    boolean inlineFound;
    Vec2 tmpDirection = new Vec2();

    public Seq<ConveyorNode> toVistit = new Seq<>(), currentlyVistiting = new Seq<>(), seen = new Seq<>();
    public Seq<Vertex> vertexList = new Seq<>();
    public Seq<Highway> highwayList = new Seq<>();
    //build.graph.explosionNoises(build.node, null)

    //Shorten the highway from the back to the new node
    public void shortenHighway(Highway highway, ConveyorNode newStart){

        ConveyorNode start = highway.start;
        //Set the target as the new highway start, shrink the startGap to compensate
        highway.start = newStart;
        float shrinkAmount = start.dst(newStart);
        highway.length -= shrinkAmount;
        //Always reduce the start gap, since we're removing length from the starting gap
        highway.startGap -= shrinkAmount;

        newStart.fromHighways.remove(highway);
        newStart.toHighway = highway;
    }

    /**
     * Disconnects two nodes in a graph, assuming they're already connected
     * @param source
     * @param target
     */
    public void disconnect(ConveyorNode source, ConveyorNode target){

        //Target highway to split or remove
        Highway splitTarget = source.toHighway;
        Vertex vertexTarget = source.toVertex;

        //This process again, but assume source & target are already linked via vertex & look at the vertexes instead
        boolean sourceLonely = source.fromVertexes.size == 0;
        boolean targetLonely = target.toVertex == null;

        if(sourceLonely){
            if(targetLonely){
                //Case 1: We actually aren't connected to anything else, and it's just the two nodes
                Log.info("Case 1: Lonely source, Lonely target");
                if (splitTarget != null) highways.remove(splitTarget);

                removeVertex(vertexTarget);
                removeHighway(splitTarget);

                DelveConveyorBlock.DelveConveyorBuild targetBuild = target.building;

                targetBuild.graph = targetBuild.createGraph(target);
                nodes.remove(target);
            }
            else {
                //Case 2: source is the starting end of a highway, so we shrink the highway fowards
                Log.info("Case 2: Lonely source, Connected target");
                removeVertex(vertexTarget);
                //But if we're grabbing the ends of the highway, then remove it
                if(splitTarget.start == source && splitTarget.end == target){
                    removeHighway(splitTarget);
                    return;
                }

                shortenHighway(splitTarget, target);
            }
        }
        else {
            if(targetLonely){
                //Case 3: Target is actually the ending of our current highway
                Log.info("Case 3: Connected source, Lonely target");
                if(splitTarget == null){
                    Log.info("ABORT ABORT ABORT SOMETHING IS TERRIBLY WRONG WHAT THE FUCK");
                    Log.info("Source: @, Targ: @",source, target);
                    Log.info("Vertex: @",vertexTarget);
                    return;
                }
                removeVertex(vertexTarget);

                //Shorten the highway
                splitTarget.end = source;
                splitTarget.end.toHighway = null;
                float shrinkAmount = source.dst(target);
                splitTarget.length -= shrinkAmount;
                //Set the startGap to the length if the highway is empty
                if(splitTarget.entries.isEmpty()) splitTarget.startGap = splitTarget.length;
                else splitTarget.entries.get(0).distToFront -= shrinkAmount;

                //Remove all entries too close to the end
                curDist = 0;

                //Put the target node & its building onto its own graph
                DelveConveyorBlock.DelveConveyorBuild targetBuild = target.building;

                target.fromHighways.clear();

                targetBuild.graph = targetBuild.createGraph(target);
                nodes.remove(target);
            }
            else {
                //This is when we have to considder how to split/update the graph, if applicable
                //The worst case by far, god can't save me here
                Log.info("Case 4: Connected source, Connected target");
                Log.info("Source: @, Target: @", source, target);

                //Set tmpDirection to the direction from the source to the target in preparation for all the edge cases
                tmpDirection.set(target).sub(source).setLength(1);

                //Case 4.a: Target is the endpoint of the splitTarget (Highway), and so we can just disconnect the nodes
                if(splitTarget.end == target){

                }
                //Case 4.b:
                //Source and target are inline && source is the start of the highway, so we can just shorten the target's highway like earlier
                if(source.toHighway.start == source && splitTarget.direction.equals(tmpDirection)){
                    Log.info("Source and vertex target: @ & @", source, source.toVertex);
                    shortenHighway(splitTarget, source.toVertex.end);
                    removeVertex(vertexTarget);
                }
                //Case 4.c: We need to... *sigh* split a vertex
                else {

                    //Reset all the lists... I know im going to need them
                    toVistit.clear();
                    currentlyVistiting.clear();
                    seen.clear();
                    vertexList.clear();
                    highwayList.clear();

                    toVistit.add(source);

                    //If we find a loop, don't sever the graph, but instead just disconnect the source & target
                    boolean loop = false;

                    while (toVistit.size > 0){
                        toVistit.each(node -> {
                            //Mark this node off as seen
                            seen.add(node);
                            node.mark = true;

                            //Get the next nodes
                            node.fromVertexes.each(vertex -> {
                                if(!vertex.start.mark) currentlyVistiting.add(vertex.start);
                            });
                            vertexList.add(node.toVertex);
                            if(node.toHighway != null && node.toHighway.start == node) highwayList.add(node.toHighway);
                        });

                        toVistit.set(currentlyVistiting);
                        currentlyVistiting.clear();
                    }

                    //Special case for if it's a loop, don't separate the graphs
                    if(loop) {
                        seen.each(s -> s.mark = false);
                        return;
                    }

                    //Create a new highway between the target and the end of the current highway
                    Log.info("Current highway list: @", highwayList);
                    Log.info("Creating new highway!");
                    Highway newHighway = addHighway(target, splitTarget.end);
                    Log.info("Current highway list: @", highwayList);

                    //Shrink by the distance from the splitTarget's new end to the previous ending
                    float shrinkAmount = source.dst(splitTarget.end);
                    Log.info("Shrinking from @, to @ by @", splitTarget.end, source, shrinkAmount);

                    //Shrink the current highway all the way back to the source after calculating that
                    splitTarget.end = source;

                    splitTarget.length -= shrinkAmount;
                    //Set the startGap to the length if the highway is empty
                    if(splitTarget.entries.isEmpty()) splitTarget.startGap = splitTarget.length;
                    else splitTarget.entries.get(0).distToFront -= shrinkAmount;

                    vertices.remove(vertexTarget);

                    ConveyorGraph sourceGraph = new ConveyorGraph();
                    sourceGraph.nodes.add(seen);
                    sourceGraph.vertices.add(vertexList);
                    sourceGraph.highways.add(highwayList);

                    nodes.removeAll(seen);
                    vertices.removeAll(vertexList);
                    highways.removeAll(highwayList);

                    sourceGraph.removeVertex(vertexTarget);

                    seen.each(node -> {
                        node.mark = false;
                        //node.mark = false;
                        node.building.graph = sourceGraph;
                        sourceGraph.builds.add(node.building);
                    });

                    Log.info(vertexList);
                    Log.info(highwayList);

                    seen.each(s -> {
                        Fx.explosion.at(s);
                    });
                }
            }
        }

        /*

         */

    }


    public void space(Highway highway, float removeLength, float spacing){
        curDist = 0;
        highway.entries.each(e -> {
            curDist += e.distToFront;
            if(Math.abs(curDist - removeLength) <= spacing){
                Fx.explosion.at(e);
            }
            else Fx.airBubble.at(e);
        });
    }

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
                //Extend target highway back to source if inline && target isn't already an intersection
                Highway sourceInputHighway = target.toHighway;
                if (target.fromHighways.size < 1 && tmpDirection.equals(sourceInputHighway.direction)) {
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

                        //Set the source's new current output highway to the inline input highway
                        source.toHighway = sourceInputHighway;
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

                        //Add the gap between the source/target to the length
                        sourceInputHighway.length += source.dst(target);

                        //Join the source/target themselves to the highway
                        source.toHighway = sourceInputHighway;
                        target.fromHighways.add(sourceInputHighway);

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
    ConveyorEntry nextEntry;

    //Move an entry along a highway, returning any remaining distance
    //Will move entries into and past intersections
    public float moveState(ConveyorEntry entry, Highway highway, boolean front, boolean back, float remainingDistance){
        //Find how far it should be from the front of the chain
        float targDist = 0;

        //If this isn't the front most entry, then offset the target distance by the front state's size + this state's size / 2
        if(!front) {
            nextEntry = entry.next;
            targDist = (entry.size + nextEntry.size) / 2;
        }

        //Move
        entry.distToFront = entry.distToFront - remainingDistance;


        //If we've moved past the target by any amount, then set that amount to remainingDistance
        remainingDst = Math.max((entry.distToFront - targDist) * -1, 0);
        entry.distToFront = Math.max(entry.distToFront, targDist);

        //Find the distance traveled and increase the current highway's startGap by that much
        highway.startGap += Math.max(remainingDistance - remainingDst, 0);


        Highway nextHighway = highway.end.toHighway;

        if(entry.next == entry) Log.info("THERES BEEN AN ISSUE ERE");
        if(remainingDst > 0 && front && nextHighway != null){
            Log.info("Theres distance left, seize the day!");
            boolean emptyTarget = nextHighway.entries.isEmpty();

            //Return if the next highway isn't empty & its backmost state doesn't leave enough room for the current entry
            if(!emptyTarget && nextHighway.startGap < (nextHighway.backState().size + entry.size)/2f) {
                stuck = true;
                return remainingDst;
            }

            highway.entries.remove(0);

            //Set the endpoint of the previous state to null if aplicable (ties it onto the current highway's endpoint)
            if(!highway.entries.isEmpty()) highway.entries.first().next = null;

            //Set the next entry for the current entry to the target highway's back-most state before adding this
            //Don't ask how I know not doing it in this order causes things to break
            if(!emptyTarget) entry.next = nextHighway.backState();
            entry.distToFront = emptyTarget ? nextHighway.length : nextHighway.startGap;

            //Add this entry to the next highway
            nextHighway.entries.add(entry);
            nextHighway.startGap = 0;

            //If the highway is empty, make the start gap the length of the highway
            if(highway.entries.isEmpty()) highway.startGap = highway.length;


            //Let that stupid fuckass neighbour know that we actually transfered their dog over to the vet, ty
            //This is only front if the highway was empty, otherwise it's literally the back state on the highway
            return moveState(entry, highway.end.toHighway, emptyTarget, true, remainingDst);
        }

        return remainingDst;
    }

    //I CAN HEAR YOU CAROL

    int currentIndex = 0;
    boolean stuck = false;
    float remainingDst;
    public void update(){
        if(Time.time == updateTimestamp) return;;
        updateTimestamp = Time.time;

        highways.each(highway -> {
            //No entries to update, return!
            if(highway.entries.isEmpty()) return;

            remainingDst = highway.speed;
            currentIndex = highway.frontIndex;

            while(remainingDst > 0){
                if(currentIndex >= highway.entries.size) break;

                //Get the frontmost entry which can still move
                ConveyorEntry currentEntry = highway.entries.get(currentIndex);

                stuck = false;
                remainingDst = moveState(currentEntry, highway, currentEntry.next == null, currentIndex == highway.entries.size - 1, remainingDst);
                if(stuck == true) currentIndex++;
                //If theres no more distance to push items along, break
                if(remainingDst == 0) {
                    break;
                }

                //current's distance is now at desired position, increase the frontIndex and start the move again on the next entry
                if(highway.end.toHighway == null) {
                    highway.frontIndex++;
                }

                //Increment currentIndex reguardless of if stuck or not to avoid getting into crashloops
                currentIndex++;

            }

            curDist = 0;
            highway.entries.each(entry -> {
                curDist += entry.distToFront;
                Tmp.v1.set(highway.direction).rotate(180).setLength(curDist).add(highway.end);
                float x = Tmp.v1.x;
                float y = Tmp.v1.y;
                entry.x = x;
                entry.y = y;
                entry.update(x, y);
            });
        });
    }

    float curDist = 0;
    Vec2 edgeOffset = new Vec2();
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
            Draw.color(v.start.mark ? Color.yellow : Color.orange);
            Fill.square(v.start.x, v.start.y, 3);
            Draw.z(drawLayer - 1);
            Draw.color(v.start.mark ? Color.magenta : Color.purple);
            Fill.circle(v.end.x, v.end.y, 2);
        });

        Lines.stroke(1);
        highways.each(v -> {

            Draw.z(drawLayer);
            Draw.color(v.start.mark ? Color.lime : Color.olive);
            Lines.line(v.start.x, v.start.y, v.end.x, v.end.y);

            Draw.z(drawLayer + 1);
            Draw.color(v.start.mark ? Color.pink : Color.red);
            Fill.square(v.start.x, v.start.y, 2);
            Draw.z(drawLayer + 2);
            Draw.color(v.start.mark ? Color.sky : Color.blue);
            Fill.circle(v.end.x, v.end.y, 1);

            curDist = 0;
            //Direction vector
            Tmp.v1.set(v.direction).rotate(180);

            Drawf.text(Strings.fixed(v.startGap, 0), v.start.x, v.start.y - 8, Color.green);
            v.entries.each(entry -> {

                Draw.color();
                //Increment the current drawing dist
                curDist += entry.distToFront;
                //Get the offset from the conveyor's front to the current item
                edgeOffset.set(v.direction).rotate(180).setLength(curDist);
                float entx = v.end.x + edgeOffset.x, enty = v.end.y + edgeOffset.y;

                Tmp.v2.set(v.end);


                Lines.stroke(1);
                Draw.color(Color.yellow);
                Lines.line(entx, enty, Tmp.v2.x, Tmp.v2.y);
                Fill.circle(Tmp.v2.x, Tmp.v2.y, 2);


                /*
                Tmp.v2.set(entry);
                Tmp.v1.set(v.direction).rotate(180).setLength(entry.distToBack).add(entx, enty);
                Lines.stroke(2);
                Draw.color(Color.blue);
                Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
                Fill.circle(Tmp.v1.x, Tmp.v1.y, 2);

                 */

                Draw.color();
                Drawf.text(Strings.fixed(entry.distToFront, 0), entx, enty + 8, Color.white);
                entry.draw(entx, enty);
            });
        });
    }

    public static class ConveyorEntry implements Position{

        public ConveyorEntry(Item item){
            this.item = item;
        }
        public ConveyorEntry(Item item, float size){
            this.item = item;
            this.size = size;
        }

        public float size = 8;

        //Next conveyor entry
        public @Nullable ConveyorEntry next;

        //Distance to the endpoint of where the item should travel to.
        public float distToFront;

        public Item item = Items.copper;

        //The conveyor nodes this entry is currently on.
        public Seq<ConveyorNode> nodes;

        public float x = 0;
        public float y = 0;
        public boolean stationary;

        public void update(float x, float y){
        }

        public void draw(float x, float y){

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

    //Handle a new entry to the graph popped onto the start of a highway
    public boolean handleEntry(Highway highway, ConveyorEntry itemState){
        try {
            //Add the state to the list of current entries
            entries.add(itemState);

            //If theres no entries on the highway currently, add the state and set the distance to its fromt to the highway's length
            if(highway.entries.size == 0){
                highway.entries.add(itemState);
                itemState.distToFront = highway.length;
            }
            //If there *are* entries on the highway currently, then attach this item state onto the next item state
            else{
                //Attach the entry onto the last possible
                itemState.next = highway.entries.peek();

                //Add the entry to the target highway's list
                highway.entries.add(itemState);
                itemState.distToFront = highway.startGap;
            }

            //Nuke the start gap now that theres an entry here
            highway.startGap = 0;
        }
        catch (Exception exception){
            Log.err(exception);
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

            //Empty highway, so both length & startGap are the same
            startGap = length = Mathf.dst(start.x, start.y, end.x, end.y);

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
                startGap += start.dst(target);
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

                    //Snap on the target highway's frontmost entry to the startGap of this highway
                    target.entries.get(0).distToFront += startGap;

                    //Use the target highway's start gap
                }
                else{
                    targetFrontState.distToFront += length;
                }
            }
            //If the target is empty, add the startGap + the distance between the two together
            else {
                Log.info("Sum of gaps| This: @, Gap: @, Total: @", startGap, start.dst(target.end), startGap + start.dst(target.end));
                target.startGap += startGap + start.dst(target.end);
            }


            entries.add(target.entries);
            target.entries = entries;
            target.nodes.add(nodes);

            target.end = this.end;

            //Remap the current end node of this highway into the target's end node
            this.end.fromHighways.clear();
            this.end.fromHighways.add(target);

            //Increase length without increasing its start gap
            target.length += this.length;
        }

        //Flag for if this highway is straight and extendable
        public boolean straight = true;

        //All nodes ordered from start up to before end
        public Seq<ConveyorNode> nodes = new Seq<>();

        public float length;

        //The gap from the start of the highway to its backmost entry, rehashed every time something moves on it
        public float startGap;

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
            float dif = length;
            length = Mathf.dst(start.x, start.y, end.x, end.y);

            //If theres no entries, find how much the length changed from its original, and extend/shrink the starting gap by that much
            dif = length - dif;
            if(entries.size == 0) startGap += dif;
        }

        public void setLength(float newLength){
            Tmp.v1.set(end).sub(start).setLength(newLength).add(start);
            end.setPos(Tmp.v1);
        }
    }


    //Stores information about the individual connections between blocks
    public static class Vertex {

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

        //Flag for when splitting graphs
        public boolean mark;

        @Override
        public String toString() {
            return Strings.format("(@, @)",x/Vars.tilesize, y/Vars.tilesize);
        }

        public ConveyorNode(float x, float y){
            this.x = x;
            this.y = y;
        }
        public ConveyorNode(float x, float y, DelveConveyorBlock.DelveConveyorBuild building){
            this.x = x;
            this.y = y;
            this.building = building;
        }
        public float x;
        public float y;

        //The building this node is assigned to.
        public @Nullable DelveConveyorBlock.DelveConveyorBuild building;

        //Depth of the node into the tree
        public int depth;

        public boolean loop;

        //All the vertexes that input to this
        public Seq<Vertex> fromVertexes = new Seq<Vertex>();

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
