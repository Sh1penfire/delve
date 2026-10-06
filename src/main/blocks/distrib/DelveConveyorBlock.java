package main.blocks.distrib;

import main.graphs.ConveyorGraph;
import main.graphs.ItemDistributionGraph;
import mindustry.gen.Building;
import mindustry.world.Block;

public class DelveConveyorBlock extends Block {
    public DelveConveyorBlock(String name) {
        super(name);
        update = true;
        rotate = quickRotate = drawArrow = true;
    }

    public class DelveConveyorBuild extends Building {
        public ConveyorGraph graph;
        public ConveyorGraph.Vertex vertex;

        @Override
        public void created() {
            super.created();

            if(graph == null){
                DelveConveyorBuild front = null, back = null;
                if(front() instanceof DelveConveyorBuild graphed){
                    front = graphed;
                }
                if(back() instanceof DelveConveyorBuild graphed){
                    back = graphed;
                }

                if(front != null && back != null){
                    //join
                    back.graph.attach(this, back, back.vertex);
                    graph.merge(vertex, front.vertex, front, front.graph, relativeTo(front) == front.rotation);
                }
                else if(front != null){
                    front.graph.attach(this, front, front.vertex);
                }
                else if(back != null){
                    back.graph.attach(this, back, back.vertex);
                }
            }
            //If the graph is still null after that, create the new graph
            if(graph == null) graph = new ConveyorGraph(this);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
        }



        @Override
        public void draw() {
            super.draw();
            graph.draw();
        }
    }
}
