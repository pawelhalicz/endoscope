package org.endoscope.cdiui;

import org.endoscope.Endoscope;
import org.endoscope.core.Stat;
import org.endoscope.core.Stats;
import org.endoscope.storage.JsonUtil;
import org.endoscope.storage.SearchableStatsStorage;
import org.slf4j.Logger;

import javax.ws.rs.*;
import java.util.Date;

import static org.slf4j.LoggerFactory.getLogger;

@Path("/endoscope/storage")
public class StorageStatsUiController extends StaticResourceController{
    private static final Logger log = getLogger(StorageStatsUiController.class);

    private JsonUtil jsonUtil = new JsonUtil();
    private TopLevelStatsSerializer topLevelStatsSerializer = new TopLevelStatsSerializer();

    @GET
    @Path("ui/data/top")
    @Produces("application/json")
    public String top(@QueryParam("from") Long from, @QueryParam("to") Long to) {
        Stats stats = topLevel(from, to);
        return topLevelStatsSerializer.serialize(stats.getMap());
    }

    @GET
    @Path("ui/data/sub/{id}")
    @Produces("application/json")
    public String sub(@PathParam("id") String id, @QueryParam("from") Long from, @QueryParam("to") Long to){
        Stat child = stat(id, from, to);
        StringBuilder sb = new StringBuilder();
        sb.append(jsonUtil.toJson(child));
        return sb.toString();
    }

    private boolean isRange(Long from, Long to){
        return from != null && to != null && from.compareTo(to) < 0;
    }

    //TODO cache?
    private Stats topLevel(Long from, Long to){
        if( !isRange(from, to) ){
            return Endoscope.processStats(stats -> stats.deepCopy(false) );
        }
        return getSearchableStatsStorage().topLevel(new Date(from), new Date(to));
    }

    //TODO cache?
    private Stat stat(String id, Long from, Long to){
        if( !isRange(from, to) ){
            return Endoscope.processStats(stats -> stats.getMap().get(id).deepCopy());
        }
        return getSearchableStatsStorage().stat(id, new Date(from), new Date(to));
    }

    private SearchableStatsStorage getSearchableStatsStorage() {
        SearchableStatsStorage storage;
        try{
            storage = (SearchableStatsStorage) Endoscope.getStatsStorage();
        }catch(ClassCastException e){
            throw new RuntimeException("Range search is not supported");
        }
        return storage;
    }
}
