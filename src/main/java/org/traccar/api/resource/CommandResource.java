package org.traccar.api.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.ExtendedObjectResource;
import org.traccar.database.CommandsManager;
import org.traccar.model.Command;
import org.traccar.model.Typed;


@Path("commands")
@Produces({"application/json"})
@Consumes({"application/json"})
public class CommandResource
        extends ExtendedObjectResource<Command> {
    public CommandResource() {
        super(Command.class);
    }

    @GET
    @Path("send")
    public Collection<Command> get(@QueryParam("deviceId") long deviceId) {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        CommandsManager commandsManager = Context.getCommandsManager();
        Set<Long> result = new HashSet<>(commandsManager.getUserItems(getUserId()));
        result.retainAll(commandsManager.getSupportedCommands(deviceId));
        return commandsManager.getItems(result);
    }

    @POST
    @Path("send")
    public Response send(Command entity) throws Exception {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (entity.getDeviceId() == 0L) {
            entity.setDeviceId(Context.getIdentityManager().getByUniqueId(entity.getUniqueId()).getId());
        }
        long deviceId = entity.getDeviceId();
        long id = entity.getId();
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        if (id != 0L) {
            Context.getPermissionsManager().checkPermission(Command.class, getUserId(), id);
            Context.getPermissionsManager().checkUserDeviceCommand(getUserId(), deviceId, id);
        } else {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        }
        if (!Context.getCommandsManager().sendCommand(entity)) {
            return Response.accepted(entity).build();
        }
        return Response.ok(entity).build();
    }


    @GET
    @Path("types")
    public Collection<Typed> get(@QueryParam("deviceId") long deviceId, @QueryParam("protocol") String protocol, @QueryParam("textChannel") boolean textChannel) {
        if (deviceId != 0L) {
            Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
            return Context.getCommandsManager().getCommandTypes(deviceId, textChannel);
        }
        if (protocol != null) {
            return Context.getCommandsManager().getCommandTypes(protocol, textChannel);
        }
        return Context.getCommandsManager().getAllCommandTypes();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\CommandResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */