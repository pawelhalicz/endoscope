package org.endoscope;

import java.util.Map;
import java.util.function.Consumer;

import org.endoscope.impl.Stat;
import org.endoscope.impl.Engine;

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

    //TODO this is ugly hack with access to internals  - for debug purposes only!!!
    public static void processStats(Consumer<Map<String, Stat>> consumer){
        ENGINE.process(consumer);
    }
}
