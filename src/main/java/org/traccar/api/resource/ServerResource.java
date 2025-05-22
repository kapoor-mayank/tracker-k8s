package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.LogAction;
import org.traccar.model.BaseModel;
import org.traccar.model.Server;


@Path("server")
@Produces({"application/json"})
@Consumes({"application/json"})
public class ServerResource
        extends BaseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResource.class);
    @PermitAll
    @GET
    public Server get() throws SQLException {
        return Context.getPermissionsManager().getServer();
    }
//    @PermitAll
//    @POST
//    @Path("process")
//    public Response processJson(List<Map<String, Object>> data) {
//
//        LOGGER.info("JSON Object: {}", data);
//        // Iterate and process each JSON object dynamically
//        for (Map<String, Object> jsonObject : data) {
//            LOGGER.info("JSON Object Iteration: {}", jsonObject);
//        }
//        return Response.ok("Data processed successfully").build();
//    }
    @PUT
    public Response update(Server entity) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        Context.getPermissionsManager().updateServer(entity);
        LogAction.edit(getUserId(), (BaseModel) entity);
        return Response.ok(entity).build();
    }

    @Path("geocode")
    @GET
    public String geocode(@QueryParam("latitude") double latitude, @QueryParam("longitude") double longitude) {
        if (Context.getGeocoder() != null) {
            return Context.getGeocoder().getAddress(latitude, longitude, null);
        }
        throw new RuntimeException("Reverse geocoding is not enabled");
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\ServerResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */