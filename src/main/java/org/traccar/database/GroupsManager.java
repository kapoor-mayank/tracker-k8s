package org.traccar.database;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.model.BaseModel;
import org.traccar.model.Group;


public class GroupsManager
        extends BaseObjectManager<Group>
        implements ManagableObjects {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsManager.class);

    private AtomicLong groupsLastUpdate = new AtomicLong();
    private final long dataRefreshDelay;

    public GroupsManager(DataManager dataManager) {
        super(dataManager, Group.class);
        this.dataRefreshDelay = Context.getConfig().getLong("database.refreshDelay", 300L) * 1000L;
    }


    private void checkGroupCycles(Group group) {
        Set<Long> groups = new HashSet<>();
        while (group != null) {
            if (groups.contains(Long.valueOf(group.getId()))) {
                throw new IllegalArgumentException("Cycle in group hierarchy");
            }
            groups.add(Long.valueOf(group.getId()));
            group = getById(group.getGroupId());
        }
    }

    public void updateGroupCache(boolean force) throws SQLException {
        long lastUpdate = this.groupsLastUpdate.get();
        if ((force || System.currentTimeMillis() - lastUpdate > this.dataRefreshDelay) && this.groupsLastUpdate
                .compareAndSet(lastUpdate, System.currentTimeMillis())) {
            refreshItems();
        }
    }


    public Set<Long> getAllItems() {
        Set<Long> result = super.getAllItems();
        if (result.isEmpty()) {
            try {
                updateGroupCache(true);
            } catch (SQLException e) {
                LOGGER.warn("Update group cache error", e);
            }
            result = super.getAllItems();
        }
        return result;
    }


    protected void addNewItem(Group group) {
        checkGroupCycles(group);
        super.addNewItem(group);
    }


    public void updateItem(Group group) throws SQLException {
        checkGroupCycles(group);
        super.updateItem(group);
    }


    public Set<Long> getUserItems(long userId) {
        if (Context.getPermissionsManager() != null) {
            return Context.getPermissionsManager().getGroupPermissions(userId);
        }
        return new HashSet<>();
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
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\GroupsManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */