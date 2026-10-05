package main.content;

import arc.graphics.Color;
import main.fluid.Aspect;
import main.fluid.AspectGroup;

import static main.fluid.AspectGroup.put;

public class DelveAspects {
    public static Aspect boundAspect;

    public static void load(){

        boundAspect = new Aspect("bound-aspect"){{
            gas = true;
            color = Color.valueOf("d7a9ef");
            temperature = 0.6f;
        }};


        put(boundAspect, AspectGroup.aspect, new AspectGroup.AspectStats(1, 2));
    }
}
