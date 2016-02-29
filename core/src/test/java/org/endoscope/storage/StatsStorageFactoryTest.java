package org.endoscope.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.endoscope.impl.Properties;
import org.junit.Test;

import static org.endoscope.impl.PropertyTestUtil.withProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StatsStorageFactoryTest {
    @Test
    public void should_create_stats_storage() throws IOException {
        File dir = Files.createTempDirectory("DiskStorageTest").toFile();

        withProperty(Properties.STATS_STORAGE_CLASS_INIT_PARAM, dir.getAbsolutePath(), () -> {
            StatsStorage storage = new StatsStorageFactory().safeCreate();
            assertNotNull(storage);
            assertEquals(Properties.getStatsStorageClass(), storage.getClass().getName());
        });
    }
}