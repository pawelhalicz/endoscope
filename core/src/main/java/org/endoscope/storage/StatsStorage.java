package org.endoscope.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.endoscope.impl.Stats;

public interface StatsStorage {
    File save(Stats stats) throws IOException;

    List<String> listParts();

    Stats load(String name) throws IOException;
}
