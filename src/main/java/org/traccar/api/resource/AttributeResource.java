package org.traccar.api.resource;

import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.handler.ComputedAttributesHandler;
import org.traccar.model.Attribute;
import org.traccar.model.BaseModel;
import org.traccar.model.Position;


@Path("attributes/computed")
@Produces({"application/json"})
@Consumes({"application/json"})
public class AttributeResource
        extends ExtendedObjectResource<Attribute> {
    public AttributeResource() {
        super(Attribute.class);
    }

    @POST
    @Path("test")
    public Response test(@QueryParam("deviceId") long deviceId, Attribute entity) {
        Context.getPermissionsManager().checkAdmin(getUserId());
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        Position last = Context.getIdentityManager().getLastPosition(deviceId);
        if (last != null) {


            Object result = (new ComputedAttributesHandler(Context.getConfig(), Context.getIdentityManager(), Context.getAttributesManager())).computeAttribute(entity, last);
            if (result != null) {
                Number numberValue;
                Boolean booleanValue;
                switch (entity.getType()) {
                    case "number":
                        numberValue = (Number) result;
                        return Response.ok(numberValue).build();
                    case "boolean":
                        booleanValue = (Boolean) result;
                        return Response.ok(booleanValue).build();
                }
                return Response.ok(result.toString()).build();
            }

            return Response.noContent().build();
        }

        throw new IllegalArgumentException("Device has no last position");
    }


    @POST
    public Response add(Attribute entity) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return super.add((Attribute) entity);
    }

    @Path("{id}")
    @PUT
    public Response update(Attribute entity) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return super.update((Attribute) entity);
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return super.remove(id);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\AttributeResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */