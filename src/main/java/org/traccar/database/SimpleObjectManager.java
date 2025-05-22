package org.traccar.database;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.model.BaseModel;
import org.traccar.model.Permission;
import org.traccar.model.User;


public abstract class SimpleObjectManager<T extends BaseModel>
        extends BaseObjectManager<T>
        implements ManagableObjects {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleObjectManager.class);

    private Map<Long, Set<Long>> userItems;

    protected SimpleObjectManager(DataManager dataManager, Class<T> baseClass) {
        super(dataManager, baseClass);
    }


    public final Set<Long> getUserItems(long userId) {
        if (!this.userItems.containsKey(Long.valueOf(userId))) {
            this.userItems.put(Long.valueOf(userId), new HashSet<>());
        }
        return this.userItems.get(Long.valueOf(userId));
    }


    public Set<Long> getManagedItems(long userId) {
        Set<Long> result = new HashSet<>();
        result.addAll(getUserItems(userId));
        for (Iterator<Long> iterator = Context.getUsersManager().getUserItems(userId).iterator(); iterator.hasNext(); ) {
            long managedUserId = ((Long) iterator.next()).longValue();
            result.addAll(getUserItems(managedUserId));
        }

        return result;
    }

    public final boolean checkItemPermission(long userId, long itemId) {
        return getUserItems(userId).contains(Long.valueOf(itemId));
    }


    public void refreshItems() {
        super.refreshItems();
        refreshUserItems();
    }

    public final void refreshUserItems() {
        if (getDataManager() != null) {
            try {
                if (this.userItems != null) {
                    this.userItems.clear();
                } else {
                    this.userItems = new ConcurrentHashMap<>();
                }
                for (Object permission : getDataManager().getPermissions((Class) User.class, getBaseClass())) {
                    Permission permission1 = (Permission) permission;
                    getUserItems(permission1.getOwnerId()).add(Long.valueOf(permission1.getPropertyId()));
                }
            } catch (SQLException | ClassNotFoundException error) {
                LOGGER.warn("Error getting permissions", error);
            }
        }
    }


    public void removeItem(long itemId) throws SQLException {
        super.removeItem(itemId);
        refreshUserItems();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\SimpleObjectManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */