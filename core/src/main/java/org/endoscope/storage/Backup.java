package org.endoscope.storage;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.endoscope.properties.Properties;
import org.endoscope.core.Stats;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Backup {
    private static final Logger log = getLogger(Backup.class);

    private int backupFreqMinutes = Properties.getBackupFreqMinutes();
    private StatsStorage statsStorage = null;
    private DateUtil dateUtil;
    private Date lastBackup;

    /**
     *
     * @param statsStorage if null then backup is disabled
     */
    public Backup(StatsStorage statsStorage){
        this(statsStorage, new DateUtil());
    }

    /**
     *
     * @param statsStorage if null then backup is disabled
     * @param dateUtil
     */
    public Backup(StatsStorage statsStorage, DateUtil dateUtil){
        this.statsStorage = statsStorage;
        this.dateUtil = dateUtil;
        lastBackup = dateUtil.now();
    }

    public boolean shouldBackup(){
        if( statsStorage != null && backupFreqMinutes > 0 ){
            Date now = dateUtil.now();
            long offset = now.getTime() - lastBackup.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(offset);
            return minutes >= backupFreqMinutes;
        }
        return false;
    }

    public void safeBackup(Stats stats){
        try{
            if( statsStorage != null ){
                statsStorage.save(stats);
                lastBackup = dateUtil.now();
            }
        }catch(Exception e){
            log.error("failed to backup stats", e);
        }
    }

    public Date getLastBackupTime() {
        return lastBackup;
    }
}
