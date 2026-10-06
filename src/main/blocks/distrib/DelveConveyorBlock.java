package main.blocks.distrib;

import arc.util.Log;
import main.graphs.ConveyorGraph;
import main.graphs.ItemDistributionGraph;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;

public class DelveConveyorBlock extends Block {
    public DelveConveyorBlock(String name) {
        super(name);
        update = true;
        rotate = quickRotate = drawArrow = true;
    }

    public class DelveConveyorBuild extends Building {
        public ConveyorGraph graph;
        //The vetex that outputs from this building
        public ConveyorGraph.Vertex vertex;

        public ConveyorGraph.Vertex highway;

        //Central node of the building
        public ConveyorGraph.ConveyorNode node;

        public DelveConveyorBuild frontConv;

        public void updateGraph(){
            Log.info("Updating proximity on: @", tile);
            DelveConveyorBuild front = null, back = null;
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

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            super.init(tile, team, shouldAdd, rotation);
            node = new ConveyorGraph.ConveyorNode(this.x, this.y, this);
            graph = new ConveyorGraph(node);
            graph.builds.add(this);

            return this;
        }

        @Override
        public void onProximityUpdate() {
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
