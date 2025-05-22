package org.traccar.api.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.database.RedisManager;
import org.traccar.model.Position;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("http_rec/wifi")
@Produces({"application/json"})
@Consumes({"application/json"})
public class HttpReciever {
    private static final Logger log = LoggerFactory.getLogger(HttpReciever.class);
    RedisManager redisManager = new RedisManager();
    public HttpReciever() {
        System.out.println("In HttpReciever");
    }

    @GET
    @PermitAll
    @Path("test")
    public Response test() {
        // Return a simple JSON response
        String jsonResponse = "{\"message\":\"Test Successful\"}";
        return Response.ok(jsonResponse).build();
        //Position position = new Position("xyz");
        //redisManager.writePosition(position);
    }
}