package org.endoscope.impl;

import java.io.File;
import java.util.LinkedList;

import org.endoscope.storage.Backup;
import org.endoscope.storage.DiskStorage;

public class Engine {
    private ThreadLocal<LinkedList<Context>> contextStack = new ThreadLocal<>();
    private Boolean enabled = null;
    private DiskStorage diskStorage = null;//may stay null if disabled or folder is invalid/inaccessible
    private Backup backup;
    private StatsProcessor statsProcessor;

    public Engine(){
        try{
            String dir = Properties.getStorageDir();
            diskStorage = dir == null ? null : new DiskStorage(new File(dir));
        }catch(Exception e){
        }
        backup = new Backup(diskStorage);
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

    public DiskStorage getDiskStorage(){ return diskStorage; }
}
