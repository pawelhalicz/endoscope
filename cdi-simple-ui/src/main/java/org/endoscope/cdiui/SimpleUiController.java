package org.endoscope.cdiui;

import org.endoscope.Endoscope;
import org.endoscope.core.Stat;
import org.endoscope.storage.JsonUtil;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
@Path("/endoscope")
public class SimpleUiController {
    private static final Logger log = getLogger(SimpleUiController.class);

    private JsonUtil jsonUtil = new JsonUtil();
    private static final String DEV_DIR;

    static {
        DEV_DIR = System.getProperty("org.endoscope.dev.res.dir");
    }

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
    @Path("/ui")// this path is related in PopulateUiDataFilter
    public Response ui() throws FileNotFoundException {
        return uiResource("index.html");
    }

    @GET
    @Path("/ui/res/{path:.*}")
    public Response uiResource(@PathParam("path") String path) throws FileNotFoundException {
        InputStream resourceAsStream = null;
        if( DEV_DIR != null ){
            resourceAsStream = new FileInputStream(new File(DEV_DIR + "/" + path));
        } else {
            resourceAsStream = getClass().getResourceAsStream("/res/" + path);
        }
        if( resourceAsStream == null ){
            return Response.status(404).build();
        }
        return Response.ok(resourceAsStream)
                .type(mediaType(path))
                .cacheControl(oneDayCache()).build();
    }

    private String mediaType(String path){
        if( path.endsWith(".js") )return "application/javascript";
        if( path.endsWith(".css") )return "text/css";
        if( path.endsWith(".html"))return "text/html";

        log.warn("can't find media type for path: {}", path);
        return null;
    }
    private CacheControl oneDayCache() {
        CacheControl cc = new CacheControl();
        //Set max age to one day
        cc.setMaxAge(86400);
        return cc;
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
