package main.blocks.distrib;

import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Log;
import main.blocks.environment.ItemStatePile;
import main.blocks.interfaces.ItemStateHolder;
import main.graphs.ConveyorGraph;
import main.items.DelveItem;
import main.items.ItemEntryIDMap;
import main.type.ItemState;
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
        quickRotate = false;
        rotate = drawArrow = true;
    }

    public float speed = Vars.tilesize/60f;

    public class DelveConveyorBuild extends Building implements ItemStateHolder {
        public ConveyorGraph graph;

        public ConveyorGraph.Highway highway;

        //Central node of the building
        public ConveyorGraph.ConveyorNode node;

        public DelveConveyorBuild frontConv;

        public int lastRot = 0;

        @Override
        public Seq<ItemState> list() {
            return null;
        }

        @Override
        public boolean acceptsItemState(ItemState state) {
            return node.toHighway != null && state != null && state.size <= node.toHighway.startGap;
        }

        @Override
        public void handleState(ItemState state) {
            graph.handleEntry(node.toHighway, new ConveyorGraph.ConveyorEntry(state));
        }

        /*
        @Override
        public void removeState(ItemState state) {

        }
         */

        @Override
        public void update() {
            super.update();
            graph.update();
            if(back() instanceof ItemStatePile.ItemStatePileBuild pile){
                var itemState = pile.topState();

                if(acceptsItemState(itemState)){
                    pile.removeState(itemState);
                    handleState(itemState);
                }
            }
            if(node.toHighway == null) node.fromHighways.each(highway -> {
                ConveyorGraph.ConveyorEntry entry = highway.entries.firstOpt();
                if(entry == null || entry.distToFront > 0) return;

                if(front() instanceof ItemStateHolder target){

                    if (target == null || !target.acceptsItemState(entry.itemState)) return;

                    highway.entries.remove(0);
                    highway.frontIndex = 0;
                    target.handleState(entry.itemState);
                    if (highway.entries.isEmpty()) highway.startGap = highway.length;
                    else highway.entries.first().next = null;
                }
                else if(front() == null){
                    highway.removeFront();
                    entry.itemState.parrent = null;
                    var offset = Geometry.d4(rotation);
                    ItemStatePile.create(tile.x + offset.x, tile.y + offset.y, entry.itemState);
                }
            });
        }

        @Override
        public void onRemoved() {
            disconnect();
            super.onRemoved();
        }

        public void disconnect(){
            //Disconnect this node and its target node
            if(node.toNode != null){
                Log.debug("Node & Node to vertex: @ & @", node, node.toNode);
                graph.disconnect(node, node.toNode);
            }
            //Disconnect all the nodes connected to this
            if(node.fromNodes != null){
                Log.debug(node.fromNodes);
                ConveyorGraph.ConveyorNode[] vOk = node.fromNodes.toArray(ConveyorGraph.ConveyorNode.class);

                for(ConveyorGraph.ConveyorNode nodeInput: vOk){
                    graph.disconnect(nodeInput, node);
                }
            }
            graph.nodes.remove(node);
        }

        public void updateGraph(){
            Log.debug("Updating graph on: @", tile);
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
            graph.handleEntry(node.toHighway, new ConveyorGraph.ConveyorEntry(ItemEntryIDMap.getState(item)));
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            super.init(tile, team, shouldAdd, rotation);
            node = new ConveyorGraph.ConveyorNode(this.x, this.y, this);
            createGraph(node);
            lastRot = rotation;
            return this;
        }



        @Override
        public void onProximityUpdate() {
            if(lastRot != rotation){
                lastRot = rotation;
                Log.info("rotated");
            }
            super.onProximityUpdate();
            updateGraph();
        }

        @Override
        public void draw() {
            super.draw();
            if(graph != null) graph.draw();
        }
    }
}
