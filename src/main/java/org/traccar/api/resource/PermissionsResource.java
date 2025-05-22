package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.LogAction;
import org.traccar.model.Device;
import org.traccar.model.Permission;
import org.traccar.model.User;


@Path("permissions")
@Produces({"application/json"})
@Consumes({"application/json"})
public class PermissionsResource
        extends BaseResource {
    private void checkPermission(Permission permission, boolean link) {
        if (!link && permission.getOwnerClass().equals(User.class) && permission
                .getPropertyClass().equals(Device.class)) {
            if (getUserId() != permission.getOwnerId()) {
                Context.getPermissionsManager().checkUser(getUserId(), permission.getOwnerId());
            } else {
                Context.getPermissionsManager().checkAdmin(getUserId());
            }
        } else {
            Context.getPermissionsManager().checkPermission(permission
                    .getOwnerClass(), getUserId(), permission.getOwnerId());
        }
        Context.getPermissionsManager().checkPermission(permission
                .getPropertyClass(), getUserId(), permission.getPropertyId());
    }

    @POST
    public Response add(LinkedHashMap<String, Long> entity) throws SQLException, ClassNotFoundException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Permission permission = new Permission(entity);
        checkPermission(permission, true);
        Context.getDataManager().linkObject(permission.getOwnerClass(), permission.getOwnerId(), permission
                .getPropertyClass(), permission.getPropertyId(), true);
        LogAction.link(getUserId(), permission.getOwnerClass(), permission.getOwnerId(), permission
                .getPropertyClass(), permission.getPropertyId());
        Context.getPermissionsManager().refreshPermissions(permission);
        return Response.noContent().build();
    }

    @DELETE
    public Response remove(LinkedHashMap<String, Long> entity) throws SQLException, ClassNotFoundException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        Permission permission = new Permission(entity);
        checkPermission(permission, false);
        Context.getDataManager().linkObject(permission.getOwnerClass(), permission.getOwnerId(), permission
                .getPropertyClass(), permission.getPropertyId(), false);
        LogAction.unlink(getUserId(), permission.getOwnerClass(), permission.getOwnerId(), permission
                .getPropertyClass(), permission.getPropertyId());
        Context.getPermissionsManager().refreshPermissions(permission);
        return Response.noContent().build();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\PermissionsResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */