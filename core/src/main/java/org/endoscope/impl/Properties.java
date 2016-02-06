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
}
