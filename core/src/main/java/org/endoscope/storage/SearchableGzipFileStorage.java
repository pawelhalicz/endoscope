package org.endoscope.storage;

import org.endoscope.core.Stat;
import org.endoscope.core.Stats;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Date;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This storage is for demo purposes as it's not efficient.
 * Notice that it loads complete stats in order to extract just part of it.
 */
public class SearchableGzipFileStorage extends GzipFileStorage implements SearchableStatsStorage {
    private static final Logger log = getLogger(SearchableGzipFileStorage.class);

    public SearchableGzipFileStorage(String dir){
        super(dir);
    }

    private Stats safeLoad(String name) {
        try {
            return load(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stats topLevel(Date from, Date to) {
        log.info("Searching for top level stats from {} to {}", getDateFormat().format(from), getDateFormat().format(to));
        Stats merged = new Stats();
        listParts().stream()
                .peek( statsInfo -> log.info("Checking {}", statsInfo.getName()))
                .filter(statsInfo -> statsInfo.inRange(from, to))
                .peek( statsInfo -> log.info("Matches: {}", statsInfo.getName()))
                .map( statsInfo -> safeLoad(statsInfo.getName()))
                .forEach(stats -> merged.merge(stats, false));
        return merged;
    }

    @Override
    public Stat stat(String id, Date from, Date to) {
        log.info("Searching for stat {} from {} to {}", id, getDateFormat().format(from), getDateFormat().format(to));
        Stat merged = new Stat();
        listParts().stream()
                .peek( statsInfo -> log.info("Checking {}", statsInfo.getName()))
                .filter(statsInfo -> statsInfo.inRange(from, to))
                .peek( statsInfo -> log.info("Matches: {}", statsInfo.getName()))
                .map( statsInfo -> safeLoad(statsInfo.getName()).getMap().get(id))
                .filter(stat -> stat != null)
                .forEach(stat -> merged.merge(stat, true));
        return merged;
    }
}
