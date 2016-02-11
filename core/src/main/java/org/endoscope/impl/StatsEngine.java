/*
 * Copyright (c) 2016 SmartRecruiters Inc. All Rights Reserved.
 */

package org.endoscope.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Date: 11/02/16
 * Time: 22:07
 *
 * @author p.halicz
 */
public class StatsEngine {
    private static final Logger log = getLogger(StatsEngine.class);

    private Stats stats = new Stats();
    private LinkedBlockingDeque<Context> queue = new LinkedBlockingDeque<>(Properties.getMaxQueueSize());

    public StatsEngine() {
        ExecutorService collector = Executors.newSingleThreadExecutor(runable -> {
            Thread t = Executors.defaultThreadFactory().newThread(runable);
            t.setDaemon(true);//we don't want to block JVM shutdown
            t.setName("endoscope-stats-collector");
            return t;
        });
        collector.submit(()->{
            log.info("started stats collector thread");
            try{
                while(!Thread.interrupted()){
                    Context ctx = queue.poll();
                    while(ctx != null){
                        synchronized(stats){
                            stats.store(ctx);
                        }
                        ctx = queue.pollFirst();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            }catch(Exception e){
                log.info("Stats collector thread interrupted - won't collect any more stats");
                stats.setFatalError(getStacktrace(e));//asignment is thread safe
            }
            log.info("stopped stats collector thread");
        });
    }

    private String getStacktrace(Exception e) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(buf);
        e.printStackTrace(pw);
        pw.flush();
        return buf.toString();
    }

    public void store(Context context){
        try{
            queue.addLast(context);
        }catch(IllegalStateException e){
            synchronized(stats){
                stats.incrementLost();
            }
        }
    }

    public void process(Consumer<Stats> consumer){
        synchronized(stats){
            consumer.accept(stats);
        }
    }

    public int getQueueSize(){
        return queue.size();
    }
}
