package org.endoscope.cdiui;

import org.endoscope.Endoscope;
import org.endoscope.core.Stat;
import org.endoscope.storage.JsonUtil;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
@Path("/endoscope/current")
public class InMemoryStatsUiController extends StaticResourceController {
    private static final Logger log = getLogger(InMemoryStatsUiController.class);

    private JsonUtil jsonUtil = new JsonUtil();
    private TopLevelStatsSerializer topLevelStatsSerializer = new TopLevelStatsSerializer();

    @GET
    @Path("ui/data/top")
    @Produces("application/json")
    public String top() {
        return Endoscope.processStats(stats -> topLevelStatsSerializer.serialize(stats.getMap()));
    }

    @GET
    @Path("ui/data/sub/{id}")
    @Produces("application/json")
    public String sub(@PathParam("id") String id){
        return Endoscope.processStats(stats -> {
            Stat child = stats.getMap().get(id);
            return jsonUtil.toJson(child);
        });
    }
}
