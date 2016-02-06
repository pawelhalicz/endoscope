package org.endoscope.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Stats {
    private Map<String, Stat> map = new HashMap<>();

    private Stat getOrAddParent(Context context) {
        Stat parentStat = map.get(context.id);
        if( parentStat == null ){
            parentStat = new Stat();
            map.put(context.id, parentStat);
        }
        return parentStat;
    }

    public void store(Context context){
        store(context, true);
    }

    private void store(Context context, boolean firstPass){
        Stat root = getOrAddParent(context);
        root.update(context.time);
        store(context, root, firstPass);
    }

    private void store(Context context, final Stat parentStat, boolean firstPass){
        if( context.children != null ){
            //first collect number of calls per parent
            Map<String, Long> subcalls = new HashMap<>();
            context.children.stream().forEach( child -> {
                Long perParent = subcalls.getOrDefault(child.id, 0L) + 1;
                subcalls.put(child.id, perParent);

                //update child stats
                Stat childStat = parentStat.getOrAddChild(child.id);
                childStat.update(child.time);

                //recurse and update next level of child stats
                store(child, childStat, firstPass);

                //recurse and update top level stats
                if( firstPass ) {
                    store(child, false);
                }
            });

            subcalls.entrySet().stream().forEach( entry -> {
                Stat childStat = parentStat.children.get(entry.getKey());
                childStat.updateParentAvgCount(entry.getValue());
            });
        }
    }

    //TODO this is ugly hack with access to internals  - for debug purposes only!!!
    public void process(Consumer<Map<String, Stat>> consumer){
        consumer.accept(map);
    }
}
