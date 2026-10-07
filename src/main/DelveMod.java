package main;

import arc.struct.Seq;
import arc.util.*;
import main.blocks.WallBlaster;
import main.content.DelveAspects;
import main.content.DelveItems;
import main.content.blocks.DelveEnvBlocks;
import main.content.blocks.DelveProdBlocks;
import main.content.DelveUnits;
import main.content.modifiers.VanillaCostModifier;
import mindustry.Vars;
import mindustry.content.Blocks;
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
