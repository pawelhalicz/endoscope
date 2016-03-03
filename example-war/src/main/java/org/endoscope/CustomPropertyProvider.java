package org.endoscope;

import org.endoscope.properties.AbstractCustomPropertyProvider;
import org.endoscope.properties.Properties;
import org.endoscope.storage.GzipFileStorage;
import org.slf4j.Logger;

import java.nio.file.Files;

import static org.slf4j.LoggerFactory.getLogger;

public class CustomPropertyProvider extends AbstractCustomPropertyProvider {
    private static final Logger log = getLogger(CustomPropertyProvider.class);

    public CustomPropertyProvider() {
        try {
            String dir = Files.createTempDirectory("endoscope").toFile().getAbsolutePath();
            log.info("Using storage directory: {}", dir);

            override.put(Properties.ENABLED, "true");
            override.put(Properties.STATS_STORAGE_CLASS, GzipFileStorage.class.getName());
            override.put(Properties.STATS_STORAGE_CLASS_INIT_PARAM, dir);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
