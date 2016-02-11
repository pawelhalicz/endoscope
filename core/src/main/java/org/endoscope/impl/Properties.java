package org.endoscope.impl;

public class Properties {
    private static String safeGetProperty(String name, String defaultValue){
        try{
            return System.getProperty(name, defaultValue);
        }catch(Exception e){
        }
        return defaultValue;
    }

    public static boolean isEnabled(){
        return "true".equalsIgnoreCase(safeGetProperty("endoscope.enabled", "true"));
    }

    public static String[] getScannedPackages(){
        return safeGetProperty("endoscope.scanned-packages", "").split(",");
    }

    public static String[] getPackageExcludes(){
        return safeGetProperty("endoscope.excluded-packages", "").split(",");
    }

    public static String getSupportedNames(){
        return safeGetProperty("endoscope.supported-names", ".*(Bean|Service|Controller|Ejb|EJB)");
    }

    public static long getMaxStatCount(){

// RAM usage (org.endoscope.impl.StatsTest.estimate_stats_size)
//        100000 ~ 13 MB
//        200000 ~ 28 MB
//        300000 ~ 42 MB << default
//        400000 ~ 59 MB
//        500000 ~ 73 MB
//        600000 ~ 88 MB
//        700000 ~ 102 MB
//        800000 ~ 121 MB
//        900000 ~ 135 MB
//        1000000 ~ 150 MB
//JSON size (org.endoscope.impl.StatsTest.estimate_json_stats_size):
//        100000 ~ 6 MB
//        200000 ~ 13 MB
//        300000 ~ 19 MB << default (~1MB when compressed)
//        400000 ~ 26 MB
//        500000 ~ 32 MB
//        600000 ~ 39 MB
//        700000 ~ 45 MB
//        800000 ~ 52 MB
//        900000 ~ 59 MB
//        1000000 ~ 65 MB
        return Long.valueOf(safeGetProperty("endoscope.max.stat.count", "300000"));//~42MB
    }
}
