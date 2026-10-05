package main.meta;

import arc.Core;
import arc.util.Strings;
import main.fluid.AspectGroup;
import main.type.ItemAttributes;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

public class MeldStats {

    public static void loadModifications(){
        Vars.content.items().each(c -> {
            c.stats.add(Stat.buildCost, c.cost, MeldStatUnit.ticks);
        });
    }

    public static Stat
            aspectStats = new Stat("aspect-stats"),
            itemAttributes = new Stat("item-attributes");

    public static String percent(float number){
        return (int) (number * 100) + "%";
    }

    public static void aspectStats(Liquid aspect, Stats stats){
        stats.add(aspectStats, table -> {

            table.row();

            AspectGroup.groups.each(g -> {
                if(g.hidden) return;

                AspectGroup.AspectStats aspectStat = g.stats.get(aspect);
                if(aspectStat == null) return;
                table.table(Styles.grayPanel, pannel -> {

                    pannel.left();

                    pannel.add(g.localizedName).left();
                    pannel.row();
                    pannel.add("[lightgray]" + Core.bundle.get("stat.aspect-efficiency") + ":[] " + percent(g.getEfficiency(aspect)));
                    pannel.row();
                    pannel.add("[lightgray]" + Core.bundle.get("stat.aspect-density") + ":[] " + percent(g.getDensity(aspect))).left();
                    pannel.row();
                }).growX().pad(5);
                table.row();
            });
        });
    }

    public static void itemAttributes(Item item, Stats stats){
        stats.add(itemAttributes, table -> {

            table.row();

            ItemAttributes.eachAttribute(item, (id, value) -> {
                table.table(Styles.grayPanel, pannel -> {

                    pannel.left();
                    pannel.add("[lightgray]" + Core.bundle.get("attribute." + id) + ":[] " + value);
                    pannel.row();
                }).growX().pad(5);
                table.row();
            });
        });
    }
    /*
    public static void aspectStats(Item item, Stats stats){
        stats.add(aspectStats, table -> {

            table.row();

            AspectGroup.groups.each(g -> {
                if(g.hidden) return;

                AspectGroup.AspectStats aspectStat = g.stats.get(item);
                if(aspectStat == null) return;
                table.table(Styles.grayPanel, pannel -> {

                    pannel.left();

                    pannel.add(g.localizedName).left();
                    pannel.row();
                    pannel.add("[lightgray]" + Core.bundle.get("stat.aspect-efficiency") + ":[] " + percent(g.getEfficiency(aspect)));
                    pannel.row();
                    pannel.add("[lightgray]" + Core.bundle.get("stat.aspect-density") + ":[] " + percent(g.getDensity(aspect))).left();
                    pannel.row();
                }).growX().pad(5);
                table.row();
            });
        });

     */
}