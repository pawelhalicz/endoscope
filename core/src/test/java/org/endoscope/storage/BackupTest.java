package org.endoscope.storage;

import java.util.Date;

import org.endoscope.impl.Stats;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.endoscope.impl.Properties.BACKUP_FREQ_MINUTES;
import static org.endoscope.impl.PropertyTestUtil.withProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class BackupTest {
    @Mock
    StatsStorage statsStorage;

    @Mock
    DateUtil dateUtil;

    @Test
    public void should_backup() throws Exception {
        withProperty(BACKUP_FREQ_MINUTES, "10", () -> {
            given(dateUtil.now()).willReturn(new Date(0), new Date(10 * 60 * 1000));

            Backup backup = new Backup(statsStorage, dateUtil);

            assertTrue(backup.shouldBackup());
            verify(dateUtil, times(2)).now();
            verifyNoMoreInteractions(dateUtil);
            verifyNoMoreInteractions(statsStorage);
        });
    }

    @Test
    public void should_not_backup() throws Exception {
        withProperty(BACKUP_FREQ_MINUTES, "10", () -> {
            given(dateUtil.now()).willReturn(new Date(0), new Date(10 * 60 * 1000 -1));

            Backup backup = new Backup(statsStorage, dateUtil);

            assertFalse(backup.shouldBackup());
            verify(dateUtil, times(2)).now();
            verifyNoMoreInteractions(dateUtil);
            verifyNoMoreInteractions(statsStorage);
        });
    }

    @Test
    public void should_save_file_and_update_backupo_date() throws Exception {
        Date backupTime = new Date(13 * 60 * 1000);
        given(dateUtil.now()).willReturn(new Date(0), backupTime);

        Backup backup = new Backup(statsStorage, dateUtil);

        assertEquals(0, backup.getLastBackupTime().getTime());

        Stats stats = new Stats();
        backup.safeBackup(stats);

        verify(statsStorage).save(same(stats));
        verifyNoMoreInteractions(statsStorage);
        verify(dateUtil, times(2)).now();
        verifyNoMoreInteractions(dateUtil);
        assertEquals(backupTime, backup.getLastBackupTime());
    }
}