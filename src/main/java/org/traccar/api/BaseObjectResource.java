package org.traccar.api;

import java.sql.SQLException;
import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.database.BaseObjectManager;
import org.traccar.database.ExtendedObjectManager;
import org.traccar.database.ManagableObjects;
import org.traccar.database.SimpleObjectManager;
import org.traccar.helper.LogAction;
import org.traccar.model.BaseModel;
import org.traccar.model.Calendar;
import org.traccar.model.Command;
import org.traccar.model.Device;
import org.traccar.model.Group;
import org.traccar.model.GroupedModel;
import org.traccar.model.ScheduledModel;
import org.traccar.model.User;


public abstract class BaseObjectResource<T extends BaseModel>
        extends BaseResource {
    private Class<T> baseClass;

    public BaseObjectResource(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    protected final Class<T> getBaseClass() {
        return this.baseClass;
    }

    protected final Set<Long> getSimpleManagerItems(BaseObjectManager<T> manager, boolean all, long userId) {
        Set<Long> result = null;
        if (all) {
            if (Context.getPermissionsManager().getUserAdmin(getUserId())) {
                result = manager.getAllItems();
            } else {
                Context.getPermissionsManager().checkManager(getUserId());
                result = ((ManagableObjects) manager).getManagedItems(getUserId());
            }
        } else {
            if (userId == 0L) {
                userId = getUserId();
            }
            Context.getPermissionsManager().checkUser(getUserId(), userId);
            result = ((ManagableObjects) manager).getUserItems(userId);
        }
        return result;
    }

    @POST
    public Response add(T entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (this.baseClass.equals(Device.class)) {
            Context.getPermissionsManager().checkDeviceReadonly(getUserId());
            Context.getPermissionsManager().checkDeviceLimit(getUserId());
        } else if (this.baseClass.equals(Command.class)) {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        } else if (entity instanceof GroupedModel && ((GroupedModel) entity).getGroupId() != 0L) {
            Context.getPermissionsManager().checkPermission(Group.class,
                    getUserId(), ((GroupedModel) entity).getGroupId());
        } else if (entity instanceof ScheduledModel && ((ScheduledModel) entity).getCalendarId() != 0L) {
            Context.getPermissionsManager().checkPermission(Calendar.class,
                    getUserId(), ((ScheduledModel) entity).getCalendarId());
        }

        BaseObjectManager<T> manager = Context.getManager(this.baseClass);
        manager.addItem((T) entity);
        LogAction.create(getUserId(), (BaseModel) entity);

        Context.getDataManager().linkObject(User.class, getUserId(), this.baseClass, entity.getId(), true);
        LogAction.link(getUserId(), User.class, getUserId(), this.baseClass, entity.getId());

        if (manager instanceof SimpleObjectManager) {
            ((SimpleObjectManager) manager).refreshUserItems();
        } else if (this.baseClass.equals(Group.class) || this.baseClass.equals(Device.class)) {
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
        }
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @PUT
    public Response update(T entity) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (this.baseClass.equals(Device.class)) {
            Context.getPermissionsManager().checkDeviceReadonly(getUserId());
        } else if (this.baseClass.equals(User.class)) {
            User before = Context.getPermissionsManager().getUser(entity.getId());
            Context.getPermissionsManager().checkUserUpdate(getUserId(), before, (User) entity);
        } else if (this.baseClass.equals(Command.class)) {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        } else if (entity instanceof GroupedModel && ((GroupedModel) entity).getGroupId() != 0L) {
            Context.getPermissionsManager().checkPermission(Group.class,
                    getUserId(), ((GroupedModel) entity).getGroupId());
        } else if (entity instanceof ScheduledModel && ((ScheduledModel) entity).getCalendarId() != 0L) {
            Context.getPermissionsManager().checkPermission(Calendar.class,
                    getUserId(), ((ScheduledModel) entity).getCalendarId());
        }
        Context.getPermissionsManager().checkPermission(this.baseClass, getUserId(), entity.getId());

        Context.getManager(this.baseClass).updateItem((T) entity);
        LogAction.edit(getUserId(), (BaseModel) entity);

        if (this.baseClass.equals(Group.class) || this.baseClass.equals(Device.class)) {
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            Context.getPermissionsManager().refreshAllExtendedPermissions();
        }
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws SQLException {
        Context.getPermissionsManager().checkReadonly(getUserId());
        if (this.baseClass.equals(Device.class)) {
            Context.getPermissionsManager().checkDeviceReadonly(getUserId());
        } else if (this.baseClass.equals(Command.class)) {
            Context.getPermissionsManager().checkLimitCommands(getUserId());
        }
        Context.getPermissionsManager().checkPermission(this.baseClass, getUserId(), id);

        BaseObjectManager<T> manager = Context.getManager(this.baseClass);
        manager.removeItem(id);
        LogAction.remove(getUserId(), this.baseClass, id);

        if (manager instanceof SimpleObjectManager) {
            ((SimpleObjectManager) manager).refreshUserItems();
            if (manager instanceof ExtendedObjectManager) {
                ((ExtendedObjectManager) manager).refreshExtendedPermissions();
            }
        }
        if (this.baseClass.equals(Group.class) || this.baseClass.equals(Device.class) || this.baseClass.equals(User.class)) {
            if (this.baseClass.equals(Group.class)) {
                Context.getGroupsManager().updateGroupCache(true);
                Context.getDeviceManager().updateDeviceCache(true);
            }
            Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
            if (this.baseClass.equals(User.class)) {
                Context.getPermissionsManager().refreshAllUsersPermissions();
            } else {
                Context.getPermissionsManager().refreshAllExtendedPermissions();
            }
        } else if (this.baseClass.equals(Calendar.class)) {
            Context.getGeofenceManager().refreshItems();
            Context.getNotificationManager().refreshItems();
        }
        return Response.noContent().build();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\BaseObjectResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */