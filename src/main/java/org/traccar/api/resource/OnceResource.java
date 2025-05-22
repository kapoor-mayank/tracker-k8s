package org.traccar.api.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.MainEventHandler;
import org.traccar.api.BaseResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("api")
@Produces({"application/json"})
@Consumes({"application/json"})
public class OnceResource extends
        BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnceResource.class);

//    @PermitAll
//    @POST
//    @Path("proces")
//    public Response processJson(List<Map<String, Object>> data) {
//
//        LOGGER.info("JSON Object: {}", data);
//        // Iterate and process each JSON object dynamically
//        for (Map<String, Object> jsonObject : data) {
//            LOGGER.info("JSON Object Iteration: {}", jsonObject);
//        }
//        return Response.ok("Data processed successfully").build();
//    }
}

