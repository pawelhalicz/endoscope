package org.endoscope;

import java.util.function.Consumer;

import org.endoscope.impl.Engine;
import org.endoscope.impl.Stats;

/**
 * Easy to use static facade.
 */
public class Endoscope {
    private static Engine ENGINE = new Engine();

    public static boolean isEnabled(){
        return ENGINE.isEnabled();
    }

    public static void push(String id){
        ENGINE.push(id);
    }

    public static void pop(){
        ENGINE.pop();
    }

    /**
     * This method blocks stats updating thread from storing new data.
     * Please do your job as quickly as possible otherwise internal queue will reach limit and you'll loose some data.
     * Do not expose objects outside - deep copy such object in order to keep it thread safe.
     * @param consumer
     */
    public static void processStats(Consumer<Stats> consumer){
        ENGINE.getStatsProcessor().process(consumer);
    }
}
