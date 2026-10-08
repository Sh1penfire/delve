package main.blocks.distrib;

import arc.util.Log;
import main.graphs.ConveyorGraph;
import main.items.DelveItem;
import main.items.ItemEntryIDMap;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;

public class DelveConveyorBlock extends Block {
    public DelveConveyorBlock(String name) {
        super(name);
        update = true;
        rotate = quickRotate = drawArrow = true;
    }

    public float speed = Vars.tilesize/60f;

    public class DelveConveyorBuild extends Building {
        public ConveyorGraph graph;
        //The vetex that outputs from this building
        public ConveyorGraph.Vertex vertex;

        public ConveyorGraph.Vertex highway;

        //Central node of the building
        public ConveyorGraph.ConveyorNode node;

        public DelveConveyorBuild frontConv;

        public int lastRot = 0;

        public void updateState(ConveyorGraph.ConveyorEntry entry){

        }

        @Override
        public void update() {
            super.update();
            graph.update();
        }

        @Override
        public void onRemoved() {
            //Disconnect this node and its target node
            if(node.toVertex != null){
                Log.info("Node & Node to vertex: @ & @", node, node.toVertex);
                graph.disconnect(node, node.toVertex.end);
            }
            //Disconnect all the nodes connected to this
            if(node.fromVertexes != null){
                Log.info(node.fromVertexes);
                ConveyorGraph.Vertex[] vOk = node.fromVertexes.toArray(ConveyorGraph.Vertex.class);

                for(ConveyorGraph.Vertex v: vOk){
                    graph.disconnect(v.start, v.end);
                }
            }
            graph.nodes.remove(node);

            super.onRemoved();
        }

        public void updateGraph(){
            Log.info("Updating graph on: @", tile);
            DelveConveyorBuild front = null;
            if(front() instanceof DelveConveyorBuild graphed){
                front = graphed;
            }

            if(front != null && frontConv == null){
                graph.merge(node, front.node, front.graph);
            }
            frontConv = front;
            /*
            Buildings handle the connections, graph just stores them
            only exception is merging in which graph handles the responsibilities
             */
        }

        public ConveyorGraph createGraph(ConveyorGraph.ConveyorNode node){
            graph = new ConveyorGraph(node);
            graph.builds.add(this);
            return graph;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return node.toHighway != null && (item instanceof DelveItem delvie ? delvie.size : DelveItem.defaultSize) <= node.toHighway.startGap;
        }

        @Override
        public void handleItem(Building source, Item item) {
            graph.handleEntry(node.toHighway, ItemEntryIDMap.getEntry(item));
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            super.init(tile, team, shouldAdd, rotation);
            node = new ConveyorGraph.ConveyorNode(this.x, this.y, this);
            createGraph(node);
            return this;
        }



        @Override
        public void onProximityUpdate() {
            if(lastRot != rotation){
                lastRot = rotation;
            }
            super.onProximityUpdate();
            //updateGraph();
        }

        @Override
        public void draw() {
            super.draw();
            if(graph != null) graph.draw();
        }
    }
}
