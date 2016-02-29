package org.endoscope.impl;

import java.util.LinkedList;

import org.endoscope.storage.Backup;
import org.endoscope.storage.StatsStorage;
import org.endoscope.storage.StatsStorageFactory;

public class Engine {
    private ThreadLocal<LinkedList<Context>> contextStack = new ThreadLocal<>();
    private Boolean enabled = null;
    private StatsStorage statsStorage = null;//may stay null if disabled or cannot setup it
    private Backup backup;
    private StatsProcessor statsProcessor;

    public Engine(){
        statsStorage = new StatsStorageFactory().safeCreate();//may return null
        backup = new Backup(statsStorage);
        statsProcessor = new StatsProcessor(backup);
    }

    public boolean isEnabled(){
        if( enabled == null ){
            enabled = Properties.isEnabled();
        }
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void push(String id){
        Context context = new Context(id, System.currentTimeMillis());

        LinkedList<Context> stack = contextStack.get();
        if( stack == null ){
            stack = new LinkedList<>();
            contextStack.set(stack);
        }
        Context parent = stack.peek();
        if( parent != null ){
            parent.addChild(context);
        }
        stack.push(context);
    }

    public void pop(){
        LinkedList<Context> stack = contextStack.get();
        Context context = stack.pop();
        context.setTime(System.currentTimeMillis() - context.getTime());

        if( stack.isEmpty() ){
            statsProcessor.store(context);
        }
    }

    public StatsProcessor getStatsProcessor() {
        return statsProcessor;
    }

    public StatsStorage getStatsStorage(){ return statsStorage; }
}
