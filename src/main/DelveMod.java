package main;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Bits;
import arc.struct.Seq;
import arc.util.*;
import main.blocks.EnergyField;
import main.blocks.debug.LightWallBlock;
import main.blocks.debug.LightWallBlock.LightPoint;
import main.blocks.distrib.DelveConveyorBlock;
import main.blocks.environment.ItemPile;
import main.blocks.environment.ItemStatePile;
import main.blocks.info.Lidar;
import main.blocks.interfaces.ItemStateHolder;
import main.content.DelveAspects;
import main.content.DelveItems;
import main.content.blocks.*;
import main.content.modifiers.VanillaCostModifier;
import main.content.units.DelvePlayerUnits;
import main.data.Modifyable;
import main.entities.units.DelveEntityCollisions;
import main.graphics.TileDrawers;
import main.graphs.ConveyorGraph;
import main.items.ItemAttributes;
import main.items.ItemConstructors;
import main.items.ItemState;
import main.math.Raycasting;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.core.ContentLoader;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.FogControl;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.mod.*;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import rhino.ImporterTopLevel;
import rhino.NativeJavaPackage;


public class DelveMod extends Mod{
    public static NativeJavaPackage p = null;

    public DelveMod(){
        EnergyField.init();
        Log.info("PAPYRUSSSSSSSSSSSSSSSSSS");
    }

    float dx, dy;
    boolean focusMode = false;
    float interactionRadius =  Vars.tilesize * 8;

    float cx = 0;
    float cy = 0;
    float px, py;

    public static Vec2 closest = null, source = new Vec2();
    public static Seq<Vec2> trianglePoints = new Seq<>();
    public static boolean debugVisuals = false, raycastLight = false;
    int r = 0;
    Vec2 ray = new Vec2();

    public static boolean[][] blocked = new boolean[][]{};

    static boolean found = false;

    public static void setupBlocked(){

        blocked = new boolean[Vars.world.width()][Vars.world.height()];

        Vars.world.tiles.each((tx, ty) -> {
            Tile tile = Vars.world.tile(tx, ty);
            if(tile == null || !tile.isDarkened()) return;
            found = false;

            for(int i = 0; i < Geometry.d8.length; i++){
                Tile nearby = tile.nearby(Geometry.d8(i).x, Geometry.d8(i).y);
                if(nearby == null) continue;
                if(nearby.isDarkened()) continue;
                found = true;
            }
            if(!found) return;
            blocked[tx][ty] = true;

            LightWallBlock.LightMesh mesh = LightWallBlock.squareMesh(tx * Vars.tilesize, ty * Vars.tilesize, Vars.tilesize/2f);
            LightWallBlock.meshes.add(mesh);
            LightWallBlock.verts.add(mesh.vertexes);
            LightWallBlock.points.add(mesh.points);
        });
    }

    @Override
    public void loadContent(){

        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if(!e.breaking) return;

            final var build = e.tile.build;

            if(!(build instanceof ConstructBlock.ConstructBuild con)) return;

            var requirements = con.current.requirements;

            final ItemAttributes.AttributeData data = new ItemAttributes.AttributeData();

            Time.run(0, () -> {
                for(ItemStack stack: requirements){
                    ItemAttributes.AttributeData asd = ItemAttributes.get(stack.item);
                    if(asd == null) continue;
                    data.addData(asd, stack.amount);
                }

                ItemAttributes.pureMap.each((id, item) -> {
                    float amount = data.idGet(id, 0);
                    if(amount == 0) return;
                    ItemStatePile.dump(e.tile.x, e.tile.y, ItemConstructors.get(item.name, ItemConstructors.defaultData, amount));
                });
            });
        });

        StatusEffect focus = new StatusEffect("focus"){{
            disarm = true;
            buildSpeedMultiplier = 0;
        }};

        Events.run(EventType.Trigger.update, () -> {
            if(!Vars.state.isPlaying()) return;
            cx = Core.input.mouseWorldX();
            cy = Core.input.mouseWorldY();
            px = Vars.player.x;
            py = Vars.player.y;
            source.set(px, py);
            if(Core.input.keyTap(KeyCode.tab)) focusMode = !focusMode;
            if(focusMode && Vars.player.unit() != null){
                Vars.player.unit().apply(focus, 5);

                if(Core.input.keyTap(KeyCode.mouseLeft) || Core.input.keyDown(KeyCode.mouseLeft) && Core.input.keyDown(KeyCode.controlLeft)){
                    Building build = Vars.world.buildWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
                    if(build instanceof ItemStateHolder holder && holder.inventory() != null && Vars.player.unit() instanceof ItemStateHolder inventoryling){
                        if(Vars.player.unit().dst(build) <= interactionRadius) {
                            if(holder.topState() == null) return;
                            if (inventoryling.acceptsItemState(holder.topState()))
                                ItemStateHolder.handleTransfer(holder, holder.topState(), inventoryling);
                        }
                    }
                }
            }
        });

        LightPoint[] points = {
                new LightPoint(0, 0.1f),
                new LightPoint(50 * Vars.tilesize, 0.1f),
                new LightPoint(50 * Vars.tilesize, 50 * Vars.tilesize),
                new LightPoint(0, 50 * Vars.tilesize),
        };

        LightWallBlock.LightMesh tmpMesh = new LightWallBlock.LightMesh();
        tmpMesh.vertexes.add(new LightWallBlock.LightVertex(points[0], points[1]));
        tmpMesh.vertexes.add(new LightWallBlock.LightVertex(points[1], points[2]));
        tmpMesh.vertexes.add(new LightWallBlock.LightVertex(points[2], points[3]));
        tmpMesh.vertexes.add(new LightWallBlock.LightVertex(points[3], points[0]));
        tmpMesh.points.addAll(points);

        LightWallBlock.meshes.add(tmpMesh);
        LightWallBlock.points.add(points);
        LightWallBlock.verts.add(tmpMesh.vertexes);


        Events.run(EventType.Trigger.drawOver, () -> {

            Draw.z(Layer.fogOfWar + 1);

            for (int x = 0; x < blocked.length; x++){
                for(int y = 0; y < blocked[x].length; y++){
                    if(blocked[x][y]) TileDrawers.drawFog(x * Vars.tilesize, y * Vars.tilesize, Vars.tilesize/2f);
                }
            }

            float toBotLeft = source.angleTo(0, 0);
            Seq<LightPoint> checkingPoints = LightWallBlock.points.sort(target -> (source.angleTo(target) - toBotLeft) % 360);

            trianglePoints.clear();

            //The debug visual
            if(raycastLight){
                ray.set(cx, cy).sub(source);

                if(debugVisuals) Lines.line(source.x, source.y, Tmp.v1.set(ray).scl(100).add(source).x, Tmp.v1.y);
                closest = null;
                for (int i = 0; i < LightWallBlock.meshes.size; i++) {

                    LightWallBlock.LightMesh mesh = LightWallBlock.meshes.get(i);
                    for(int j = 0; j < mesh.vertexes.size; j++){
                        LightWallBlock.LightVertex vert = mesh.vertexes.get(j);

                        LightPoint pStart = vert.start;
                        LightPoint pEnd = vert.end;
                        Vec2 intersection = Raycasting.rayIntersect(source.x, source.y, cx, cy, pStart.x, pStart.y, pEnd.x, pEnd.y);

                        if(intersection != null && !Raycasting.end){

                            if(debugVisuals){
                                Draw.color(Color.pink);
                                Lines.line(vert.start.x, vert.start.y, vert.end.x, vert.end.y);

                                Draw.color(Color.red);
                                Fill.circle(intersection.x, intersection.y, 2);
                                Draw.color();
                            }

                            if(closest == null || source.dst(closest) > source.dst(intersection)){
                                closest = intersection;
                            }
                        }
                    }
                }


                //The actual vis polygon code
                float rayRange = 100;

                r = 0;
                for (int q = 0; q < checkingPoints.size; q++) {
                    LightPoint target = checkingPoints.get(q);

                    if(debugVisuals) Drawf.text(q + "", target.x + Vars.tilesize, target.y-Vars.tilesize, Color.red);

                    r++;

                    Draw.color(Color.white);

                    //Shoot a ray offset in angle by 0.0001 deg on either side. I hate this solution with my being but it'll work.
                    for(int offset = -1; offset < 2; offset++){
                        float rotOffset = offset * 0.05f;
                        ray.trns(source.angleTo(target) + rotOffset, rayRange).add(source);

                        if(debugVisuals) Lines.line(source.x, source.y, ray.x, ray.y);

                        closest = null;

                        //Raycast now
                        for (int i = 0; i < LightWallBlock.meshes.size; i++) {

                            LightWallBlock.LightMesh mesh = LightWallBlock.meshes.get(i);
                            for(int j = 0; j < mesh.vertexes.size; j++){
                                LightWallBlock.LightVertex vert = mesh.vertexes.get(j);

                                LightPoint pStart = vert.start;
                                LightPoint pEnd = vert.end;

                                Vec2 intersection = Raycasting.rayIntersect(source.x, source.y, ray.x, ray.y, pStart.x, pStart.y, pEnd.x, pEnd.y);

                                if(intersection != null){

                                    if(debugVisuals) {
                                        Draw.color(Color.pink);
                                        Lines.line(vert.start.x, vert.start.y, vert.end.x, vert.end.y);

                                        Draw.color(Color.red);
                                        Fill.circle(intersection.x, intersection.y, 2);
                                        Draw.color();
                                    }

                                    if(closest == null || source.dst(closest) > source.dst(intersection)){
                                        closest = intersection;
                                    }
                                }
                            }
                        }

                        if(closest != null){
                            trianglePoints.add(closest);
                        }
                    }
                }

                Lines.stroke(0.5f);
                if(trianglePoints.size > 1) {
                    for (int i = 0; i < trianglePoints.size; i++) {
                        Vec2 start = trianglePoints.get((i) % trianglePoints.size);
                        Vec2 end = trianglePoints.get((i + 1) % trianglePoints.size);

                        if(debugVisuals){
                            Draw.color(Color.white);
                            Fill.circle(start.x, start.y, 1);
                            Drawf.text(i + ", " + Strings.fixed(source.angleTo(start), 1), start.x, start.y, Color.white);
                        }
                        Draw.color(Tmp.c1.set(Color.yellow).a(0.25f));
                        Fill.tri(start.x, start.y, end.x, end.y, source.x, source.y);
                    }
                }
                Fill.light(source.x, source.y, (int) rayRange/10, rayRange, Tmp.c1.set(Color.white), Tmp.c1.set(Color.white).a(0));

            }
            /*
            for (int i = 0; i < LightWallBlock.meshes.size; i++) {

                LightWallBlock.LightMesh mesh = LightWallBlock.meshes.get(i);
                for(int j = 0; j < mesh.vertexes.size; j++){
                    LightWallBlock.LightVertex vert = mesh.vertexes.get(j);
                    Vec2 pStart = vert.start;
                    Vec2 pEnd = vert.end;
                    Vec2 intersection = Raycasting.intersection(px, py, cx, cy, pStart.x, pStart.y, pEnd.x, pEnd.y);
                    source.set(px, py);

                    if(intersection != null){
                        if(closest == null || source.dst(closest) > source.dst(intersection)){
                            closest = intersection;
                        }
                    }
                }
            }

            if(closest != null){
                Draw.color(Color.red);
                Fill.circle(closest.x, closest.y, 5);
                Draw.color();
            }
             */

            //End of interaction range
            
            Unit current = Vars.player.unit();
            if(focusMode && current != null){
                Lines.circle(current.x, current.y, interactionRadius);
            }

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
                    Drawf.text(state.volume.getBase().toString() + "vu", dx, dy, Color.magenta);
                    dy -= 3;
                    dx += 1;
                });
            }
        });

        Vars.content.items().each(item -> {
            if(!ItemConstructors.idMap.containsKey(item.name)) ItemConstructors.put(item.name, new ItemConstructors.VanillaEntry(item));
        });

        DelvePlayerUnits.load();
        DelveAspects.load();
        DelveItems.load();

        DelveBlocks.load();
        DelveProdBlocks.load();
        DelvePowerBlocks.load();
        DelveEnvBlocks.load();
        DelveStorageBlocks.load();
        DelveDistribBlocks.load();

        VanillaCostModifier.load();
        //I touched intelij today :D
    }

    //TODO: add modifiers onto the item type
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

    }

    @Override
    public void init() {
        super.init();

        Vars.collisions = new DelveEntityCollisions();

        Vars.mods.getScripts().runConsole(
                "function buildWorldP(){return Vars.world.buildWorld(Vars.player.x, Vars.player.y)}");
        Vars.mods.getScripts().runConsole(
                "function s(){build.graph.handleEntry(build.node.toHighway, DelveItems.shadesteel.constructor.get())}");
        ImporterTopLevel scope = (ImporterTopLevel) Vars.mods.getScripts().scope;

        Seq<String> packages = Seq.with(
                "main",
                "main.blocks",
                "main.blocks.environment",
                "main.blocks.debug",
                "main.blocks.interfaces",
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
