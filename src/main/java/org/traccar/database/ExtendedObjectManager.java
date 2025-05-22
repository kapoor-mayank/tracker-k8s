package org.traccar.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.model.BaseModel;
import org.traccar.model.Device;
import org.traccar.model.Group;
import org.traccar.model.Permission;


public abstract class ExtendedObjectManager<T extends BaseModel>
        extends SimpleObjectManager<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedObjectManager.class);

    private final Map<Long, Set<Long>> deviceItems = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> deviceItemsWithGroups = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> groupItems = new ConcurrentHashMap<>();

    protected ExtendedObjectManager(DataManager dataManager, Class<T> baseClass) {
        super(dataManager, baseClass);
        refreshExtendedPermissions();
    }

    public final Set<Long> getGroupItems(long groupId) {
        if (!this.groupItems.containsKey(Long.valueOf(groupId))) {
            this.groupItems.put(Long.valueOf(groupId), new HashSet<>());
        }
        return this.groupItems.get(Long.valueOf(groupId));
    }

    public final Set<Long> getDeviceItems(long deviceId) {
        if (!this.deviceItems.containsKey(Long.valueOf(deviceId))) {
            this.deviceItems.put(Long.valueOf(deviceId), new HashSet<>());
        }
        return this.deviceItems.get(Long.valueOf(deviceId));
    }

    public Set<Long> getAllDeviceItems(long deviceId) {
        if (!this.deviceItemsWithGroups.containsKey(Long.valueOf(deviceId))) {
            this.deviceItemsWithGroups.put(Long.valueOf(deviceId), new HashSet<>());
        }
        return this.deviceItemsWithGroups.get(Long.valueOf(deviceId));
    }


    public void removeItem(long itemId) throws SQLException {
        super.removeItem(itemId);
        refreshExtendedPermissions();
    }

    public void refreshExtendedPermissions() {
        if (getDataManager() != null)

            try {

                Collection<Permission> databaseGroupPermissions = getDataManager().getPermissions((Class) Group.class, getBaseClass());

                this.groupItems.clear();
                for (Permission groupPermission : databaseGroupPermissions) {
                    getGroupItems(groupPermission.getOwnerId()).add(Long.valueOf(groupPermission.getPropertyId()));
                }


                Collection<Permission> databaseDevicePermissions = getDataManager().getPermissions((Class) Device.class, getBaseClass());

                this.deviceItems.clear();
                this.deviceItemsWithGroups.clear();

                for (Permission devicePermission : databaseDevicePermissions) {
                    getDeviceItems(devicePermission.getOwnerId()).add(Long.valueOf(devicePermission.getPropertyId()));
                    getAllDeviceItems(devicePermission.getOwnerId()).add(Long.valueOf(devicePermission.getPropertyId()));
                }

                for (Device device : Context.getDeviceManager().getAllDevices()) {
                    long groupId = device.getGroupId();
                    while (groupId != 0L) {
                        getAllDeviceItems(device.getId()).addAll(getGroupItems(groupId));
                        Group group = Context.getGroupsManager().getById(groupId);
                        if (group != null) {
                            groupId = group.getGroupId();
                            continue;
                        }
                        groupId = 0L;
                    }

                }

            } catch (SQLException | ClassNotFoundException error) {
                LOGGER.warn("Refresh permissions error", error);
            }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\ExtendedObjectManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */