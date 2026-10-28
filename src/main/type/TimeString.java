package main.type;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.world.meta.StatUnit;

public class TimeString {

    public static String format(float secconds, int minValue, int places){
        float out = secconds;
        String unit = StatUnit.seconds.localized();

        if(out/minValue >= 60){
            unit = StatUnit.minutes.localized();
            out = Mathf.floor(out/60);
        }

        return Strings.format(Strings.fixed(out, places) + " " + unit);
    }
}
