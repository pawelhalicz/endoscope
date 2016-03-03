package org.endoscope.cdiui;

import org.endoscope.Endoscope;
import org.endoscope.impl.Stat;
import org.endoscope.storage.JsonUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;

@ApplicationScoped
@Path("/endoscope")
public class SimpleUiController {
    private JsonUtil jsonUtil = new JsonUtil();

    @GET
    @Path("ui/data/top")
    @Produces("application/json")
    public String top() {
        StringBuilder sb = new StringBuilder();
        Endoscope.processStats(stats -> sb.append(toJsonStringTopLevel(stats.getMap())));
        return sb.toString();
    }

    @GET
    @Path("ui/data/sub/{id}")
    @Produces("application/json")
    public String sub(@PathParam("id") String id){
        StringBuilder sb = new StringBuilder();
        Endoscope.processStats(stats -> {
            Stat child = stats.getMap().get(id);
            sb.append(jsonUtil.toJson(child));
        });
        return sb.toString();
    }

    @GET
    @Path("/ui")
    @Produces("text/html")
    public Response ui() {
        return Response.ok(getClass().getResourceAsStream("/index.html")).build();
    }

    @GET
    @Path("/ui/js/jquery.min.js")
    @Produces("application/javascript")
    public Response uiResource() {
        return Response.ok(getClass().getResourceAsStream("/jquery.min.js")).build();
    }

    private static String toJsonStringTopLevel(Map<String, Stat> map) {
        StringBuilder sb = new StringBuilder("{");

        map.forEach((id, stat) -> {
            sb.append("\"").append(id).append("\":{");
            sb.append("\"hits\":"  ).append(stat.getHits()).append(",");
            sb.append("\"max\":"    ).append(stat.getMax()  ).append(",");
            sb.append("\"min\":"    ).append(stat.getMin()  ).append(",");
            sb.append("\"avg\":"    ).append(stat.getAvg()  ).append(",");
            sb.append("\"ah10\":").append(stat.getAh10()).append(",");
            sb.append("\"children\":").append(stat.getChildren() == null ? null : EMPTY_MAP);//no comma here
            sb.append("},");
        });

        if( !map.isEmpty() ){
            sb.deleteCharAt(sb.length()-1);//remove last comma
        }
        sb.append("}");
        return sb.toString();
    }
}
