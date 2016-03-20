package org.endoscope.storage.jdbc;

import org.endoscope.core.Stats;
import org.endoscope.storage.SearchableStatsStorage;
import org.endoscope.storage.StatDetails;
import org.slf4j.Logger;

import java.util.Date;

import static org.slf4j.LoggerFactory.getLogger;

public class SearchableJdbcStorage extends JdbcStorage implements SearchableStatsStorage {
    private static final Logger log = getLogger(SearchableJdbcStorage.class);

    public SearchableJdbcStorage(String initParam){
        super(initParam);
    }

    @Override
    public Stats topLevel(Date from, Date to) {
        return null;
    }

    @Override
    public StatDetails stat(String id, Date from, Date to) {
        return null;
    }
}
