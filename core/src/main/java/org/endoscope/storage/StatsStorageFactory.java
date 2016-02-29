package org.endoscope.storage;

import org.endoscope.impl.Properties;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Date: 29/02/16
 * Time: 15:04
 *
 * @author p.halicz
 */
public class StatsStorageFactory {
    private static final Logger log = getLogger(StatsStorageFactory.class);

    /**
     * Should not fail. May return null in case of failure.
     * @return
     */
    public StatsStorage safeCreate(){
        String className = Properties.getStatsStorageClass();
        String classInitParam = Properties.getStatsStorageClassInitParam();

        try {
            Class<? extends StatsStorage> clazz = (Class<? extends StatsStorage>)Class.forName(className);
            StatsStorage storage = clazz.getConstructor(String.class).newInstance(classInitParam);
            log.info("Successfully created StatsStorage instance: {}, with params: {}", className, classInitParam);
            return storage;
        } catch (Exception e) {
            log.error("Failed to create new StatsStorage: {}, with params: {}", className, classInitParam, e);
        }
        return null;
    }
}
