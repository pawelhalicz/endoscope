package org.endoscope.storage;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.endoscope.impl.Stats;

public interface StatsStorage {
    File save(Stats stats) throws IOException;

    /**
     * May limit returned elements.
     * @return
     */
    List<StatsInfo> listParts();

    List<StatsInfo> findParts(Date from, Date to);

    Stats load(String name) throws IOException;
}
