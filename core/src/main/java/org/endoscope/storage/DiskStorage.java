package org.endoscope.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.endoscope.impl.Stats;

public class DiskStorage {
    public static final String PART_PREFIX = "part_";
    public static final String BACKUP_FILE_NAME = "backup.gz";
    private File dir;
    private JsonUtil jsonUtil = new JsonUtil();

    public DiskStorage(File dir){
        this.dir = dir;
        if( dir.exists() && dir.isFile() ){
            throw new RuntimeException("location exists and is a file - cannot use it as storage directory: " + dir.getAbsolutePath());
        }
        if( !dir.exists() && !dir.mkdirs() ){
            throw new RuntimeException("cannot create storage directory: " + dir.getAbsolutePath());
        }
    }

    public File savePart(Stats stats) throws IOException {
        ensureDates(stats);
        String fileName = buildPartName(stats.getStartDate(), stats.getEndDate());
        return writeToGzipFile(stats, fileName);
    }

    public File saveBackup(Stats stats) throws IOException{
        ensureDates(stats);
        return writeToGzipFile(stats, BACKUP_FILE_NAME);
    }

    public Stats loadBackup() throws IOException{
        return load(BACKUP_FILE_NAME);
    }

    public List<String> listParts(){
        String[] arr = dir.list((File dir, String name) -> {
            return name.startsWith(PART_PREFIX);
        });
        return Arrays.asList(arr);
}

    public Stats load(String name) throws IOException {
        return readFromGzipFile(name);
    }

    private String buildPartName(Date dateStart, Date dateEnd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return PART_PREFIX + sdf.format(dateStart) + "_" + sdf.format(dateEnd) + ".gz";
    }

    private File buildFile(String fileName){
        return new File( dir.getAbsolutePath() + File.separator + fileName);
    }

    private File writeToGzipFile(Stats stats, String fileName) throws IOException {
        File file = buildFile(fileName);
        OutputStream out = null;
        try {
            out = new GZIPOutputStream(new FileOutputStream(file));
            jsonUtil.toJson(stats, out);
            return file;
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private Stats readFromGzipFile(String fileName) throws IOException {
        File file = buildFile(fileName);
        InputStream in = null;
        try {
            in = new GZIPInputStream(new FileInputStream(file));
            return jsonUtil.fromJson(Stats.class, in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void ensureDates(Stats stats) {
        if( stats.getStartDate() == null ){
            stats.setStartDate(new Date());
        }
        if( stats.getEndDate() == null ){
            stats.setEndDate(new Date());
        }
    }
}
