package org.endoscope.storage.jdbc;

import org.endoscope.core.Stats;
import org.endoscope.storage.StatsStorage;
import org.slf4j.Logger;


import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;


public class JdbcStorage extends StatsStorage {
    private static final Logger log = getLogger(JdbcStorage.class);

    public JdbcStorage(String initParam){
        super(initParam);
    }

    @Override
    public String save(Stats stats) throws IOException {
        return null;
    }
}
