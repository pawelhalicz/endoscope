package org.endoscope.storage;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.endoscope.impl.Properties;
import org.endoscope.impl.Stats;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class Backup {
    private static final Logger log = getLogger(Backup.class);

    private int backupFreqMinutes = Properties.getBackupFreqMinutes();
    private DiskStorage diskStorage;
    private DateUtil dateUtil;
    private Date lastBackup;

    public Backup(){
        this.diskStorage = new DiskStorage(new File(Properties.getStorageDir()));
        this.dateUtil = new DateUtil();
        lastBackup = dateUtil.now();
    }

    public Backup(DiskStorage diskStorage, DateUtil dateUtil){
        this.diskStorage = diskStorage;
        this.dateUtil = dateUtil;
        lastBackup = dateUtil.now();
    }

    public boolean shouldBackup(){
        if( backupFreqMinutes > 0 ){
            Date now = dateUtil.now();
            long offset = now.getTime() - lastBackup.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(offset);
            return minutes >= backupFreqMinutes;
        }
        return false;
    }

    public void safeBackup(Stats stats){
        try{
            diskStorage.saveBackup(stats);
            lastBackup = dateUtil.now();
        }catch(Exception e){
            log.error("failed to backup stats", e);
        }
    }

    public Date getLastBackupTime() {
        return lastBackup;
    }

    public Stats safeLoadBackup(){
        try{
            return diskStorage.loadBackup();
        }catch(Exception e){
            log.error("failed to backup stats", e);
        }
        return null;
    }
}
