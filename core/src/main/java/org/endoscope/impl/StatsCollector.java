package org.endoscope.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingDeque;

import org.endoscope.storage.Backup;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class StatsCollector implements Runnable {
    private static final Logger log = getLogger(StatsCollector.class);

    private Backup backup;
    private Stats stats;
    private LinkedBlockingDeque<Context> queue;

    public StatsCollector(Stats stats, LinkedBlockingDeque<Context> queue, Backup backup){
        this.stats = stats;
        this.queue = queue;
        this.backup = backup;
    }

    @Override
    public void run() {
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
                safeBackupIfNeeded();
                safeSleep();
            }
        }catch(Exception e){
            log.info("stats collector thread interrupted - won't collect any more stats");
            stats.setFatalError(getStacktrace(e));//assignment is thread safe
        }
        log.info("stopped stats collector thread");
    }

    private void safeSleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }

    private void safeBackupIfNeeded(){
        if( backup.shouldBackup() ){
            synchronized(stats){
                backup.safeBackup(stats);
            }
        }
    }

    private String getStacktrace(Exception e) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(buf);
        e.printStackTrace(pw);
        pw.flush();
        return buf.toString();
    }
}
