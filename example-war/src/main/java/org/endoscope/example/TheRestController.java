package org.endoscope.example;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endoscope.Endoscope;
import org.endoscope.impl.Stat;

import static java.util.Collections.EMPTY_MAP;

@Path("/controller")
public class TheRestController {
    @Inject
    TheService theService;

    @GET
    @Path("/process")
    @Produces({ "application/json" })
    public String process() {
        int level = 4;
        for( int i=0; i<level; i++){
            theService.process(level);
        }
        return "{\"result\":\"OK\"}";
    }

    @GET
    @Path("ui/data/top")
    @Produces("application/json")
    public String top() {
        StringBuilder sb = new StringBuilder();
        Endoscope.processStats(stats -> sb.append(toJsonStringTopLevel(stats)));
        return sb.toString();
    }

    @GET
    @Path("ui/data/sub/{id}")
    @Produces("application/json")
    public String sub(@PathParam("id") String id){
        StringBuilder sb = new StringBuilder();
        Endoscope.processStats(stats -> {
            Stat child = stats.get(id);
            sb.append(toJsonString(child));
        });
        return sb.toString();
    }

    @GET
    @Path("/ui")
    @Produces("text/html")
    public Response ui() {
        process();
        return Response.ok(getClass().getResourceAsStream("/index.html")).build();
    }

    @GET
    @Path("/ui/js/jquery.min.js")
    @Produces("application/javascript")
    public Response uiResource() {
        return Response.ok(getClass().getResourceAsStream("/jquery.min.js")).build();
    }

    private String toJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJsonStringTopLevel(Map<String, Stat> map) {
        StringBuilder sb = new StringBuilder("{");

        map.forEach((id, stat) -> {
            sb.append("\"").append(id).append("\":{");
            sb.append("\"count\":"  ).append(stat.getCount()).append(",");
            sb.append("\"max\":"    ).append(stat.getMax()  ).append(",");
            sb.append("\"min\":"    ).append(stat.getMin()  ).append(",");
            sb.append("\"avg\":"    ).append(stat.getAvg()  ).append(",");
            sb.append("\"parentAvgCount\":").append(stat.getParentAvgCount()).append(",");
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
