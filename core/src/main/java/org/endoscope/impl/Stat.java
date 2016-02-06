package org.endoscope.impl;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

public class Stat {
    long count = 0;
    long max = -1;//not set
    long min = 0;
    long avg = 0;
    long parentCount = 0;//when method is called N time for the same parent we add just 1 here
    long parentAvgCount = 0;
    Map<String, Stat> children;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
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

    public long getParentAvgCount() {
        return parentAvgCount;
    }

    public void setParentAvgCount(long parentAvgCount) {
        this.parentAvgCount = parentAvgCount;
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
    public Stat getOrAddChild(String id){
        ensureChildrenMap();
        Stat child = children.get(id);
        if( child == null ){
            child = new Stat();
            children.put(id, child);
        }
        return child;
    }

    public void update(long time){
        if( time < 0 ) return;
        if( max < 0 ){
            max = min = avg = time;
        } else {
            max = Math.max(max, time);
            min = Math.min(min, time);
            avg = (avg*count + time)/(count+1);
        }
        count++;
    }

    public void updateParentAvgCount(long hitsPerParent) {
        parentAvgCount = (parentAvgCount*parentCount + hitsPerParent)/(parentCount+1);
        parentCount++;
    }
}
