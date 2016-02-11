package org.endoscope.impl;

import java.util.HashMap;
import java.util.Map;

public class Stats {
    private Map<String, Stat> map = new HashMap<>();
    private long statsLeft = Properties.getMaxStatCount();
    private long lost = 0;
    private String fatalError = null;

    private Stat getOrAddParent(Context context) {
        Stat parentStat = map.get(context.getId());
        if( parentStat == null && statsLeft > 0 ){
            parentStat = new Stat();
            statsLeft--;
            map.put(context.getId(), parentStat);
        }
        return parentStat;
    }

    public void store(Context context){
        store(context, true);
    }

    private void store(Context context, boolean firstPass){
        Stat root = getOrAddParent(context);
        if( root != null ){
            root.update(context.getTime());
            store(context, root, firstPass);
        }
    }

    private void store(Context context, final Stat parentStat, boolean firstPass){
        if( context.getChildren() != null ){
            //first collect number of calls per parent
            Map<String, Long> subcalls = new HashMap<>();
            context.getChildren().stream().forEach( child -> {
                Long perParent = subcalls.getOrDefault(child.getId(), 0L) + 1;

                //update child stats
                Stat childStat = parentStat.getChild(child.getId());
                if( childStat == null && statsLeft > 0 ){
                    childStat = parentStat.createChild(child.getId());
                    statsLeft--;
                }
                if( childStat != null ){
                    subcalls.put(child.getId(), perParent);
                    childStat.update(child.getTime());

                    //recurse and update next level of child stats
                    store(child, childStat, firstPass);

                    //recurse and update top level stats
                    if( firstPass ) {
                        store(child, false);
                    }
                }
            });

            subcalls.entrySet().stream().forEach( entry -> {
                Stat childStat = parentStat.getChildren().get(entry.getKey());
                childStat.updateAvgHits(entry.getValue());
            });
        }
    }

    public void incrementLost() {
        lost++;
    }

    public Map<String, Stat> getMap() {
        return map;
    }

    public long getLost() {
        return lost;
    }

    public long getStatsLeft() {
        return statsLeft;
    }

    public String getFatalError() {
        return fatalError;
    }

    public void setFatalError(String fatalError) {
        this.fatalError = fatalError;
    }
}
