package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.BaseObjectResource;
import org.traccar.database.UsersManager;
import org.traccar.helper.LogAction;
import org.traccar.model.BaseModel;
import org.traccar.model.ManagedUser;
import org.traccar.model.User;


@Path("users")
@Produces({"application/json"})
@Consumes({"application/json"})
public class UserResource
        extends BaseObjectResource<User> {
    public UserResource() {
        super(User.class);
    }

    @GET
    public Collection<User> get(@QueryParam("userId") long userId) throws SQLException {
        UsersManager usersManager = Context.getUsersManager();
        Set<Long> result = null;
        if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
            if (userId != 0L) {
                result = usersManager.getUserItems(userId);
            } else {
                result = usersManager.getAllItems();
            }
        } else if (Context.getPermissionsManager().getUserManager(getUserId())) {
            result = usersManager.getManagedItems(getUserId());
        } else {
            throw new SecurityException("Admin or manager access required");
        }
        return usersManager.getItems(result);
    }


    @PermitAll
    @POST
    public Response add(User entity) throws SQLException {
        if (!Context.getPermissionsManager().getUserAdmin(getUserId())) {
            Context.getPermissionsManager().checkUserUpdate(getUserId(), new User(), entity);
            if (Context.getPermissionsManager().getUserManager(getUserId())) {
                Context.getPermissionsManager().checkUserLimit(getUserId());
            } else {
                Context.getPermissionsManager().checkRegistration(getUserId());
                entity.setDeviceLimit(Context.getConfig().getInteger("users.defaultDeviceLimit", -1));
                int expirationDays = Context.getConfig().getInteger("users.defaultExpirationDays");
                if (expirationDays > 0) {
                    entity.setExpirationTime(new Date(
                            System.currentTimeMillis() + expirationDays * 24L * 3600L * 1000L));
                }
            }
        }
        Context.getUsersManager().addItem((User) entity);
        LogAction.create(getUserId(), (BaseModel) entity);
        if (Context.getPermissionsManager().getUserManager(getUserId())) {
            Context.getDataManager().linkObject(User.class, getUserId(), ManagedUser.class, entity.getId(), true);
            LogAction.link(getUserId(), User.class, getUserId(), ManagedUser.class, entity.getId());
        }
        Context.getUsersManager().refreshUserItems();
        return Response.ok(entity).build();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\UserResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */