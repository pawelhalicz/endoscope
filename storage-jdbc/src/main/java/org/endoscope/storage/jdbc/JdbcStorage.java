package org.endoscope.storage.jdbc;

import org.apache.commons.dbutils.QueryRunner;
import org.endoscope.core.Stat;
import org.endoscope.core.Stats;
import org.endoscope.storage.StatsStorage;
import org.slf4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;


public class JdbcStorage extends StatsStorage {
    private static final Logger log = getLogger(JdbcStorage.class);
    protected QueryRunner run;
    protected ListOfMapRSHandler handler = new ListOfMapRSHandler();

    public JdbcStorage(String initParam){
        super(initParam);

        //initParam = "java:jboss/datasources/ExampleDS"
        DataSource ds = findDatasource(initParam);
        run = new QueryRunner(ds);

        ensureTableExists();
    }

    private DataSource findDatasource(String dsJndiPath) {
        try{
            Context initContext = new InitialContext();
            DataSource ds = (DataSource)initContext.lookup(dsJndiPath);
            return ds;
        }catch(NamingException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String save(Stats stats) {
        try{
            try(Connection conn = run.getDataSource().getConnection()){
                conn.setAutoCommit(false);
                try{
                    String groupId = UUID.randomUUID().toString();
                    int u = run.update(conn, "INSERT INTO endoscopeGroup(id, startDate, endDate, statsLeft, lost, fatalError) values(?,?,?,?,?,?)",
                            groupId, stats.getStartDate(), stats.getEndDate(),
                            stats.getStatsLeft(), stats.getLost(), stats.getFatalError()
                    );
                    if( u != 1 ){
                        throw new RuntimeException("Failed to insert stats group. Expected 1 result but got: " + u);
                    }

                    Object[][] data = prepareStatsData(groupId, stats);
                    int[] result = run.batch(conn, "INSERT INTO endoscopeStat(id, group, parent, root, name, hits, max, min, avg, ah10) values(?,?,?,?,?,?,?,?,?,?)", data);
                    int inserts = Arrays.stream(result).sum();
                    if( inserts != data.length ){
                        throw new RuntimeException("Failed to insert stats. Expected " + data.length + " but got: " + inserts);
                    }

                    conn.commit();
                    return groupId;
                }catch(Exception e){
                    conn.rollback();
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[][] prepareStatsData(String groupId, Stats stats) {
        List<Object[]> list = new ArrayList<>();
        process(groupId, null, null, stats.getMap(), list);
        return list.toArray(new Object[8][list.size()]);
    }

    private void process(String groupId, String parentId, String rootId, Map<String, Stat> map, List<Object[]> list){
        map.forEach((statName, stat) -> {
            String statId = UUID.randomUUID().toString();
            list.add(new Object[]{
                    statId, groupId, parentId, statName,
                    stat.getHits(), stat.getMax(), stat.getMin(), stat.getAvg(), stat.getAh10()
            });
            if( stat.getChildren() != null ){
                String currentRoot = rootId == null ? statId : rootId;
                process(groupId, statId, currentRoot, stat.getChildren(), list);
            }
        });
    }

    private void ensureTableExists() {
        //this is DB specific - so far just for H2
        try {
            List<Map<String, Object>> result = run.query("SHOW COLUMNS FROM endoscope", handler);
            if( result.size() == 0 ){
                int updated = run.update(
                        "CREATE TABLE endoscopeGroup(" +
                        "  id VARCHAR(36) PRIMARY KEY, " +
                        "  startDate TIMESTAMP, " +
                        "  endDate TIMESTAMP, " +
                        "  statsLeft INT, " +
                        "  lost INT, " +
                        "  fatalError VARCHAR(255)" +
                        ")");
                if( updated != 1 ){
                    throw new RuntimeException("Failed to create SQL stats group table");
                }

                updated = run.update(
                        "CREATE TABLE endoscopeStat(" +
                        "  id VARCHAR(36) PRIMARY KEY, " +
                        "  group VARCHAR(36), " +
                        "  parent VARCHAR(36), " +
                        "  root VARCHAR(36), " +
                        "  name VARCHAR(255), " +
                        "  hits INT, " +
                        "  max INT, " +
                        "  min INT, " +
                        "  avg INT, " +
                        "  ah10 INT " +
                        ")");
                if( updated != 1 ){
                    throw new RuntimeException("Failed to create SQL stats table");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
