package org.endoscope.impl;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class Engine {
    private ThreadLocal<LinkedList<Context>> contextStack = new ThreadLocal<>();
    private Stats stats = new Stats();
    private Boolean enabled = null;

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
        context.time = System.currentTimeMillis() - context.time;

        if( stack.isEmpty() ){
            synchronized(stats){
                stats.store(context);
            }
        }
    }

    //TODO this is ugly hack with access to internals  - for debug purposes only!!!
    public void process(Consumer<Map<String, Stat>> consumer){
        synchronized(stats){
            stats.process(consumer);
        }
    }
}
