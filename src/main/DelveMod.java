package main;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.struct.Seq;
import arc.util.*;
import main.blocks.WallBlaster;
import main.blocks.distrib.DelveConveyorBlock;
import main.content.DelveAspects;
import main.content.DelveItems;
import main.content.blocks.DelveEnvBlocks;
import main.content.blocks.DelveProdBlocks;
import main.content.DelveUnits;
import main.content.modifiers.VanillaCostModifier;
import main.graphs.ConveyorGraph;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.*;
import mindustry.world.blocks.environment.StaticTree;
import mindustry.world.blocks.environment.StaticWall;
import rhino.ImporterTopLevel;
import rhino.NativeJavaPackage;

public class DelveMod extends Mod{
    public static NativeJavaPackage p = null;

    public DelveMod(){

    }

    @Override
    public void loadContent(){

        Events.run(EventType.Trigger.drawOver, () -> {
            Draw.z(Layer.flyingUnit);
            Tmp.v1.set(Core.input.mouseWorld());
            Building build = Vars.world.buildWorld(Tmp.v1);
            if(build instanceof DelveConveyorBlock.DelveConveyorBuild b){
                ConveyorGraph.ConveyorNode node = b.node;
                if(node == null) return;

                float dx = Tmp.v1.x, dy = Tmp.v1.y;
                dx += 5;
                dy += 3;

                if(node.toHighway != null) Drawf.text(node.toHighway.toString(), dx, dy, Color.sky);
                dy -= 3;
                for (ConveyorGraph.Highway highway: node.fromHighways){
                    Drawf.text(highway.toString(), dx, dy, Color.pink);
                    Drawf.cross(highway.start.x, highway.start.y, 5, Color.red);
                    Drawf.cross(highway.end.x, highway.end.y, 5, Color.blue);
                    dy -= 3;
                    dx += 1;
                }
            }
        });

        DelveUnits.load();
        DelveAspects.load();
        DelveItems.load();
        DelveProdBlocks.load();
        DelveEnvBlocks.load();

        VanillaCostModifier.load();

        Log.info("FUCK YOU EGGMAN IM GOING TO COUNTER-PISS ON THE MOOOOOOOOOOON");
        //I touched intelij today :D
    }

    @Override
    public void init() {
        super.init();
        Vars.mods.getScripts().runConsole(
                "function buildWorldP(){return Vars.world.buildWorld(Vars.player.x, Vars.player.y)}");
        Vars.mods.getScripts().runConsole(
                "function s(){build.graph.handleEntry(build.node.toHighway, DelveItems.shadesteel.constructor.get())}");
        ImporterTopLevel scope = (ImporterTopLevel) Vars.mods.getScripts().scope;

        Seq<String> packages = Seq.with(
                "main",
                "main.content",
                "main.content.blocks",
                "main.world",
                "main.fluid",
                "main.graphics"
        );

        packages.each(name -> {

            p = new NativeJavaPackage(name, Vars.mods.mainLoader());

            p.setParentScope(scope);

            scope.importPackage(p);
        });
    }

}
