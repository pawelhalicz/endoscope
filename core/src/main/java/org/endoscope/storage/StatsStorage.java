package org.endoscope.storage;

import java.io.IOException;

import org.endoscope.impl.Stats;

public interface StatsStorage {
    /**
     * Save stats.
     * @param stats
     * @return stats identifier
     * @throws IOException
     */
    String save(Stats stats) throws IOException;
}
