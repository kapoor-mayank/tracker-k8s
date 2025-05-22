package org.traccar.api.resource;

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.model.Event;
import org.traccar.model.Notification;
import org.traccar.model.Typed;
import org.traccar.notification.MessageException;


@Path("notifications")
@Produces({"application/json"})
@Consumes({"application/json"})
public class NotificationResource
        extends ExtendedObjectResource<Notification> {
    public NotificationResource() {
        super(Notification.class);
    }

    @GET
    @Path("types")
    public Collection<Typed> get() {
        return Context.getNotificationManager().getAllNotificationTypes();
    }

    @GET
    @Path("notificators")
    public Collection<Typed> getNotificators() {
        return Context.getNotificatorManager().getAllNotificatorTypes();
    }

    @POST
    @Path("test")
    public Response testMessage() throws MessageException, InterruptedException {
        for (Typed method : Context.getNotificatorManager().getAllNotificatorTypes()) {
            Context.getNotificatorManager()
                    .getNotificator(method.getType()).sendSync(getUserId(), new Event("test", 0L), null);
        }
        return Response.noContent().build();
    }


    @POST
    @Path("test/{notificator}")
    public Response testMessage(@PathParam("notificator") String notificator) throws MessageException, InterruptedException {
        Context.getNotificatorManager().getNotificator(notificator).sendSync(getUserId(), new Event("test", 0L), null);
        return Response.noContent().build();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\NotificationResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */