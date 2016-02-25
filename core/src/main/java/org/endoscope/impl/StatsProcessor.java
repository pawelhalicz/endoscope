package org.endoscope.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

import org.endoscope.storage.Backup;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This class is responsible for thread safe operations on stats and queue.
 * All operations on this class must be thread safe.
 *
 * Storing contexts in queue must be thread safe and as fast as possible.
 * This is critical requirement as it would affect monitored application performance.
 *
 * Operations on stats also needs to be thread safe as we don't know who runs #process method.
 * All stats operations might take a lot of time: backup, processing and updates (in case of complex stats).
 * Because of that we need different thread that will move data from queue to stats.
 */
public class StatsProcessor {
    private static final Logger log = getLogger(StatsProcessor.class);

    private Stats stats;//will get reset after each backup
    private LinkedBlockingDeque<Context> queue;
    private Backup backup;

    public StatsProcessor(Backup backup) {
        if( backup == null ){
            throw new IllegalArgumentException("backup cannot be null");
        }

        stats = new Stats();
        if( stats == null ){
            stats = new Stats();
        }

        queue = new LinkedBlockingDeque<>(Properties.getMaxQueueSize());
        this.backup = backup;

        ExecutorService collector = Executors.newSingleThreadExecutor(runnable -> {
            Thread t = Executors.defaultThreadFactory().newThread(runnable);
            t.setDaemon(true);//we don't want to block JVM shutdown
            t.setName("endoscope-stats-collector");
            return t;
        });
        collector.submit(new StatsCollector(this));
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

    private void safeBackupIfNeeded(){
        if( backup.shouldBackup() ){
            synchronized(stats){
                backup.safeBackup(stats);
                stats = new Stats();
            }
        }
    }

    //internal use - accessed from processing thread
    void processAllFromQueue(){
        Context ctx = queue.poll();
        synchronized(stats){
            while(ctx != null){
                stats.store(ctx);
                ctx = queue.pollFirst();
            }
        }
        safeBackupIfNeeded();
    }

    //internal use - accessed from processing thread
    void setFatalError(String message){
        stats.setFatalError(message);//assignment is thread safe
    }
}
