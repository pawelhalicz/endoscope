package org.endoscope.example;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/controller")
public class TheRestController {
    @Inject
    TheService theService;

    @GET
    @Path("/process")
    @Produces({ "application/json" })
    public String process() {
        int level = 5;
        for( int i=0; i<level; i++){
            theService.process(level);
        }
        return "{\"result\":\"OK\"}";
    }
}
