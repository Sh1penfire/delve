package main.blocks.distrib;

import main.graphs.ItemDistributionGraph;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.world.Block;

public class DelveGraphBlock extends Block {
    public DelveGraphBlock(String name) {
        super(name);
        update = true;
        rotate = quickRotate = drawArrow = true;
    }

    public class DelveGraphBuild extends Building {
        public ItemDistributionGraph graph;
        public ItemDistributionGraph.Vertex vertex;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            if(graph == null){
                DelveGraphBuild front = null, back = null;
                if(front() instanceof DelveGraphBuild graphed){
                    front = graphed;
                }
                if(back() instanceof DelveGraphBuild graphed){
                    back = graphed;
                }

                if(front != null && back != null){
                    //join
                    front.graph.addBuilding(this, front, front.vertex);
                    front.graph.attach(back.graph, this, front, front.vertex);
                }
                if(front != null){
                    front.graph.addBuilding(this, front, front.vertex);
                }
                if(back != null){
                    back.graph.addBuilding(this, back, back.vertex);
                }
            }
            //If the graph is still null after that
            if(graph == null) graph = new ItemDistributionGraph(this);
        }

        @Override
        public void draw() {
            super.draw();
            graph.draw();
        }
    }
}
