package main;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.struct.Seq;
import arc.util.*;
import main.blocks.EnergyField;
import main.blocks.distrib.DelveConveyorBlock;
import main.blocks.interfaces.ItemStateHolder;
import main.content.DelveAspects;
import main.content.DelveItems;
import main.content.blocks.*;
import main.content.modifiers.VanillaCostModifier;
import main.content.units.DelvePlayerUnits;
import main.data.Modifyable;
import main.graphs.ConveyorGraph;
import main.type.ItemState;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.*;
import rhino.ImporterTopLevel;
import rhino.NativeJavaPackage;

public class DelveMod extends Mod{
    public static NativeJavaPackage p = null;

    public DelveMod(){
        EnergyField.init();
    }

    float dx, dy;
    @Override
    public void loadContent(){

        Events.run(EventType.Trigger.drawOver, () -> {
            Draw.z(Layer.flyingUnit);
            Tmp.v1.set(Core.input.mouseWorld());

            dx = Tmp.v1.x;
            dy = Tmp.v1.y;

            Building build = Vars.world.buildWorld(Tmp.v1);
            if(build instanceof DelveConveyorBlock.DelveConveyorBuild b){
                ConveyorGraph.ConveyorNode node = b.node;
                if(node == null) return;

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
            if(build instanceof ItemStateHolder holder){
                if(holder.inventory() == null){
                    return;
                }

                Seq<ItemState> list = holder.inventory().states;
                if(list != null) list.each(state -> {
                    if(state.attributes != null) state.attributes.map.each((entry) -> {
                        Drawf.text(Strings.format("@: @", entry.key, Strings.fixed(entry.value, 1)), dx, dy, Color.red);
                        dy -= 3;
                        dx += 1;
                    });
                });
            }
        });

        DelvePlayerUnits.load();
        DelveAspects.load();
        DelveItems.load();

        DelveProdBlocks.load();
        DelvePowerBlocks.load();
        DelveEnvBlocks.load();
        DelveStorageBlocks.load();
        DelveDistribBlocks.load();

        VanillaCostModifier.load();

        Log.info("FUCK YOU EGGMAN IM GOING TO COUNTER-PISS ON THE MOOOOOOOOOOON");
        //I touched intelij today :D
    }

    public static void modify1(){
        Modifyable.Modifier<World, Float> sizeModifier = new Modifyable.Modifier<World, Float>() {
            @Override
            public int priority() {
                return 0;
            }

            @Override
            public Float apply(Float in, World context) {
                return in * 2;
            }
        };
        DelveItems.malachite.entry.size.addModifier(sizeModifier);

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
                "main.blocks",
                "main.content",
                "main.content.blocks",
                "main.world",
                "main.fluid",
                "main.graphics",
                "main.items"
        );

        packages.each(name -> {

            p = new NativeJavaPackage(name, Vars.mods.mainLoader());

            p.setParentScope(scope);

            scope.importPackage(p);
        });
    }

}
