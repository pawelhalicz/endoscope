package org.endoscope.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

import org.endoscope.storage.Backup;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StatsEngine {
    private static final Logger log = getLogger(StatsEngine.class);

    private Stats stats;
    private LinkedBlockingDeque<Context> queue;

    public StatsEngine() {
        Backup backup = new Backup();
        stats = backup.safeLoadBackup();
        if( stats == null ){
            stats = new Stats();
        }

        queue = new LinkedBlockingDeque<>(Properties.getMaxQueueSize());

        ExecutorService collector = Executors.newSingleThreadExecutor(runnable -> {
            Thread t = Executors.defaultThreadFactory().newThread(runnable);
            t.setDaemon(true);//we don't want to block JVM shutdown
            t.setName("endoscope-stats-collector");
            return t;
        });
        collector.submit(new StatsCollector(stats, queue, backup));
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
