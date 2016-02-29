package org.endoscope.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.endoscope.impl.Stats;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class GzipFileStorage implements StatsStorage {
    private static final Logger log = getLogger(GzipFileStorage.class);

    public static final String PREFIX = "stats";
    public static final String SEPARATOR = "_";
    public static final String EXTENSION = "gz";
    public static final Pattern NAME_PATTERN = Pattern.compile(PREFIX + SEPARATOR + "....-..-..-..-..-.." + SEPARATOR + "....-..-..-..-..-.." + "\\." + EXTENSION);
    public static final String DATE_PATTERN = "yyyy-MM-dd-HH-mm-ss";
    public static final String DATE_TIMEZONE = "GMT";
    private File dir;
    private JsonUtil jsonUtil = new JsonUtil();

    public GzipFileStorage(String dir){
        this(new File(dir));
    }

    public GzipFileStorage(File dir){
        this.dir = dir;
        if( dir.exists() && dir.isFile() ){
            throw new RuntimeException("location exists and is a file - cannot use it as storage directory: " + dir.getAbsolutePath());
        }
        if( !dir.exists() && !dir.mkdirs() ){
            throw new RuntimeException("cannot create storage directory: " + dir.getAbsolutePath());
        }
    }

    @Override
    public File save(Stats stats) throws IOException {
        ensureDates(stats);
        String fileName = buildPartName(stats.getStartDate(), stats.getEndDate());
        return writeToGzipFile(stats, fileName);
    }

    @Override
    public List<StatsInfo> listParts(){
        String[] arr = dir.list((dir, name) -> NAME_PATTERN.matcher(name).matches());
        return toStatsInfo(arr);
    }

    private List<StatsInfo> toStatsInfo(String[] arr) {
        return Arrays.asList(arr).stream()
                .sorted()
                .map( name -> safeParseName(name))
                .filter( info -> info != null )
                .collect(Collectors.toList());
    }

    @Override
    public List<StatsInfo> findParts(Date from, Date to) {
        if( from == null || to == null || to.before(from) ){
            return Collections.emptyList();
        }

        String[] arr = dir.list((dir, name) -> {
            if( NAME_PATTERN.matcher(name).matches() ){
                StatsInfo info = safeParseName(name);
                return info != null && info.inRange(from, to);
            }
            return false;
        });
        return toStatsInfo(arr);
    }

    @Override
    public Stats load(String name) throws IOException {
        return readFromGzipFile(name);
    }

    private String buildPartName(Date dateStart, Date dateEnd) {
        DateFormat sdf = getDateFormat();
        return PREFIX + SEPARATOR + sdf.format(dateStart) + SEPARATOR + sdf.format(dateEnd) + "." + EXTENSION;
    }

    private DateFormat getDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        sdf.setTimeZone(TimeZone.getTimeZone(DATE_TIMEZONE));
        return sdf;
    }

    private StatsInfo safeParseName(String name) {
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setName(name);
        DateFormat sdf = getDateFormat();
        try{
            String noExtension = name.substring(0, name.length() - EXTENSION.length());
            String[] parts = noExtension.split(SEPARATOR);
            if( parts.length == 3 ){
                statsInfo.setFromDate(sdf.parse(parts[1]));
                statsInfo.setToDate(sdf.parse(parts[2]));
            }
        }catch(Exception e){
            log.warn("Problem parsing stats file name: {}", name, e);
        }
        return statsInfo;
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
