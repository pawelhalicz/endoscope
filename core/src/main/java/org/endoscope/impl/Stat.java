package org.endoscope.impl;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

public class Stat {
    long hits = 0;
    long max = -1;//not set
    long min = 0;
    long avg = 0;
    long parentCount = 0;//when method is called N time for the same parent we add just 1 here
    long ah10 = 0; //average hit count in context of the same parent x10 (1 digit of precision)
    Map<String, Stat> children;

    public Stat(){}

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getAvg() {
        return avg;
    }

    public void setAvg(long avg) {
        this.avg = avg;
    }

    /**
     * Average hits x10 (1 digit of precision)
     * Short name for JSON - no @JsonProperty in this module
     * @return
     */
    public long getAh10() {
        return ah10;
    }

    public void setAh10(long ah10) {
        this.ah10 = ah10;
    }

    public Map<String, Stat> getChildren() {
        return children;
    }

    public void setChildren(Map<String, Stat> children) {
        this.children = children;
    }

    public void ensureChildrenMap(){
        if(children == null){
            children = new HashMap<>();
        }
    }

    @Transient
    public Stat getChild(String id){
        ensureChildrenMap();
        return children.get(id);
    }

    @Transient
    public Stat createChild(String id){
        ensureChildrenMap();
        Stat child = new Stat();
        children.put(id, child);
        return child;
    }

    public void update(long time){
        if( time < 0 ) return;
        if( max < 0 ){
            max = min = avg = time;
        } else {
            max = Math.max(max, time);
            min = Math.min(min, time);
            avg = (avg* hits + time)/(hits +1);
        }
        hits++;
    }

    public void updateAvgHits(long hitsPerParent) {
        //10x gives 1 digit of precision
        ah10 = (ah10 * parentCount + 10 * hitsPerParent)/(parentCount+1);
        parentCount++;
    }
}
