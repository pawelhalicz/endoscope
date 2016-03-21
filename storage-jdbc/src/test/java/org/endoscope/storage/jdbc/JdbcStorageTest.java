package org.endoscope.storage.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.endoscope.core.Stat;
import org.endoscope.core.Stats;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JdbcStorageTest {
    private static Server server;
    private static JdbcDataSource ds;

    private static Context contextMock = Mockito.mock(Context.class);

    public static final class NCF implements InitialContextFactory {
        public Context getInitialContext(Hashtable<?,?> environment) throws NamingException {
            return contextMock;
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        server = Server.createTcpServer().start();
        server.start();

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, NCF.class.getName());

        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;");

        Mockito.when(contextMock.lookup("jdbc/dsName")).thenReturn(ds);
    }

    @AfterClass
    public static void finish(){
        server.stop();
    }

    @Test
    public void should_save_and_read_stats() throws SQLException {
        SearchableJdbcStorage storage = new SearchableJdbcStorage("jdbc/dsName");
        Stats stats = buildCommonStats();
        storage.save(stats);

        q("SELECT * from endoscopeGroup");
        q("SELECT * from endoscopeStat");

        Stats read = storage.topLevel(new Date(1000000000000L), new Date(1300000000000L));
        assertNotNull(read);
        assertEquals(stats.getLost(), read.getLost());
        assertEquals(stats.getStartDate(), read.getStartDate());
        assertEquals(stats.getEndDate(), read.getEndDate());
        assertEquals(stats.getFatalError(), read.getFatalError());
        assertEquals(stats.getStatsLeft(), read.getStatsLeft());
    }

    private static void q(String q) throws SQLException {
        QueryRunner run = new QueryRunner(ds);
        List<Map<String, Object>> result = run.query(q, new ListOfMapRSHandler());
        System.out.println(result);
    }

    private Stats buildCommonStats() {
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
}
