package org.traccar.api;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

import org.traccar.Context;
import org.traccar.database.BaseObjectManager;
import org.traccar.database.ExtendedObjectManager;
import org.traccar.model.BaseModel;


public class ExtendedObjectResource<T extends BaseModel>
        extends BaseObjectResource<T> {
    public ExtendedObjectResource(Class<T> baseClass) {
        super(baseClass);
    }


    @GET
    public Collection<T> get(@QueryParam("all") boolean all, @QueryParam("userId") long userId, @QueryParam("groupId") long groupId, @QueryParam("deviceId") long deviceId, @QueryParam("refresh") boolean refresh) throws SQLException {
        ExtendedObjectManager<T> manager = (ExtendedObjectManager<T>) Context.getManager(getBaseClass());
        if (refresh) {
            manager.refreshItems();
        }

        Set<Long> result = new HashSet<>(getSimpleManagerItems((BaseObjectManager<T>) manager, all, userId));

        if (groupId != 0L) {
            Context.getPermissionsManager().checkGroup(getUserId(), groupId);
            result.retainAll(manager.getGroupItems(groupId));
        }

        if (deviceId != 0L) {
            Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
            result.retainAll(manager.getDeviceItems(deviceId));
        }
        return manager.getItems(result);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\ExtendedObjectResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */