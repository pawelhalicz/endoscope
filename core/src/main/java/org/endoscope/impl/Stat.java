package org.endoscope.impl;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

@com.fasterxml.jackson.annotation.JsonPropertyOrder({ "hits", "max", "min", "avg", "ah10", "children" })
public class Stat {
    private long hits = 0;
    private long max = -1;//not set
    private long min = 0;
    private double avg = 0;
    private long parentCount = 0;//when method is called N time for the same parent we add just 1 here
    double avgParent = 0;
    private Map<String, Stat> children;

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
        return Math.round(avg);
    }

    public void setAvg(long avg) {
        this.avg = avg;
    }

    /**
     * Average hits per parent x10 (1 digit of precision)
     * Short name for JSON - no @JsonProperty in this module
     * @return
     */
    public long getAh10() {
        return Math.round(avgParent*10.0f);
    }

    public void setAh10(long ah10) {
        avgParent = ((float)ah10)/10f;
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
            avg = max = min = time;
        } else {
            max = Math.max(max, time);
            min = Math.min(min, time);
            avg = (avg* hits + time)/(hits +1);
        }
        hits++;
    }

    public void updateAvgHits(long hitsPerParent) {
        avgParent = (avgParent * parentCount + hitsPerParent)/(parentCount+1);
        parentCount++;
    }
}
