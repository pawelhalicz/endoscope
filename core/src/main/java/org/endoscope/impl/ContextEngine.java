package org.endoscope.impl;

import java.util.LinkedList;

public class ContextEngine {
    private ThreadLocal<LinkedList<Context>> contextStack = new ThreadLocal<>();
    private Boolean enabled = null;
    private StatsEngine statsEngine = new StatsEngine();

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
            statsEngine.store(context);
        }
    }

    public StatsEngine getStatsEngine() {
        return statsEngine;
    }
}
