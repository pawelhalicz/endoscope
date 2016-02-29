package org.endoscope.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.endoscope.impl.Stat;
import org.endoscope.impl.Stats;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.endoscope.storage.GzipFileStorage.PART_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiskStorageTest {
    static File dir;
    static StatsStorage ds;
    static Stats stats;
    static JsonUtil jsonUtil = new JsonUtil();

    @BeforeClass
    public static void before() throws IOException{
        dir = Files.createTempDirectory("DiskStorageTest").toFile();
        System.out.println(dir.getAbsolutePath());
        ds = new GzipFileStorage(dir);

        stats = buildCommonStats();
    }

    private static Stats buildCommonStats() {
        Stats stats = new Stats();
        stats.setFatalError("error");
        stats.setStartDate(new Date(1000000000000L));
        stats.setEndDate(new Date(1300000000000L));
        stats.setLost(111);
        Stat s1 = new Stat();
        s1.update(123);
        stats.getMap().put("aaa", s1);
        return stats;
    }

    @AfterClass
    public static void after()throws IOException{
        FileUtils.deleteDirectory(dir);
    }

    @Test
    public void should_save_and_load_part_file() throws IOException{
        File f = ds.save(stats);

        assertEquals( dir.getAbsolutePath() + File.separator + PART_PREFIX + "2001-09-09-01-46-40_2011-03-13-07-06-40.gz", f.getAbsolutePath() );

        Stats loaded = ds.load(f.getName());

        assertEquals(stats, loaded);
        assertEquals(jsonUtil.toJson(stats), jsonUtil.toJson(loaded));
    }

    @Test
    public void should_list_only_parts() throws IOException{
        File part1 = ds.save(shiftDates(buildCommonStats(), 1000000L));
        File part2 = ds.save(shiftDates(buildCommonStats(), 2000000L));

        System.out.println(part1.getName());
        System.out.println(part2.getName());
        List<String> parts = ds.listParts();

        System.out.println(parts);

        assertTrue(parts.size() >= 2 );//may be more du to different tests
        assertTrue(parts.contains(part1.getName()));
        assertTrue(parts.contains(part2.getName()));
    }

    private Stats shiftDates(Stats s, long offset) {
        s.setStartDate(new Date(s.getStartDate().getTime() + offset));
        s.setEndDate(new Date(s.getEndDate().getTime() + offset));
        return s;
    }
}