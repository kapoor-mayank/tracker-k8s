package org.traccar.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.model.Attribute;
import org.traccar.model.BaseModel;
import org.traccar.model.Calendar;
import org.traccar.model.Command;
import org.traccar.model.Device;
import org.traccar.model.Driver;
import org.traccar.model.Geofence;
import org.traccar.model.Group;
import org.traccar.model.Maintenance;
import org.traccar.model.ManagedUser;
import org.traccar.model.Notification;
import org.traccar.model.Permission;
import org.traccar.model.Server;
import org.traccar.model.User;


public class PermissionsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsManager.class);

    private final DataManager dataManager;

    private final UsersManager usersManager;

    private volatile Server server;
    private final Map<Long, Set<Long>> groupPermissions = new HashMap<>();
    private final Map<Long, Set<Long>> devicePermissions = new HashMap<>();
    private final Map<Long, Set<Long>> deviceUsers = new HashMap<>();
    private final Map<Long, Set<Long>> groupDevices = new HashMap<>();

    public PermissionsManager(DataManager dataManager, UsersManager usersManager) {
        this.dataManager = dataManager;
        this.usersManager = usersManager;
        refreshServer();
        refreshDeviceAndGroupPermissions();
    }

    public User getUser(long userId) {
        return this.usersManager.getById(userId);
    }

    public Set<Long> getGroupPermissions(long userId) {
        if (!this.groupPermissions.containsKey(Long.valueOf(userId))) {
            this.groupPermissions.put(Long.valueOf(userId), new HashSet<>());
        }
        return this.groupPermissions.get(Long.valueOf(userId));
    }

    public Set<Long> getDevicePermissions(long userId) {
        if (!this.devicePermissions.containsKey(Long.valueOf(userId))) {
            this.devicePermissions.put(Long.valueOf(userId), new HashSet<>());
        }
        return this.devicePermissions.get(Long.valueOf(userId));
    }

    private Set<Long> getAllDeviceUsers(long deviceId) {
        if (!this.deviceUsers.containsKey(Long.valueOf(deviceId))) {
            this.deviceUsers.put(Long.valueOf(deviceId), new HashSet<>());
        }
        return this.deviceUsers.get(Long.valueOf(deviceId));
    }

    public Set<Long> getDeviceUsers(long deviceId) {
        Device device = Context.getIdentityManager().getById(deviceId);
        if (device != null && !device.getDisabled()) {
            return getAllDeviceUsers(deviceId);
        }
        Set<Long> result = new HashSet<>();
        for (Iterator<Long> iterator = getAllDeviceUsers(deviceId).iterator(); iterator.hasNext(); ) {
            long userId = ((Long) iterator.next()).longValue();
            if (getUserAdmin(userId)) {
                result.add(Long.valueOf(userId));
            }
        }

        return result;
    }


    public Set<Long> getGroupDevices(long groupId) {
        if (!this.groupDevices.containsKey(Long.valueOf(groupId))) {
            this.groupDevices.put(Long.valueOf(groupId), new HashSet<>());
        }
        return this.groupDevices.get(Long.valueOf(groupId));
    }

    public void refreshServer() {
        try {
            this.server = this.dataManager.getServer();
        } catch (SQLException error) {
            LOGGER.warn("Refresh server config error", error);
        }
    }

    public final void refreshDeviceAndGroupPermissions() {
        this.groupPermissions.clear();
        this.devicePermissions.clear();


        try {
            GroupTree groupTree = new GroupTree(Context.getGroupsManager().getItems(Context.getGroupsManager().getAllItems()), Context.getDeviceManager().getAllDevices());
            for (Object groupPermission1 : this.dataManager.getPermissions((Class) User.class, (Class) Group.class)) {
                Permission groupPermission = (Permission) groupPermission1;
                Set<Long> userGroupPermissions = getGroupPermissions(groupPermission.getOwnerId());
                Set<Long> userDevicePermissions = getDevicePermissions(groupPermission.getOwnerId());
                userGroupPermissions.add(Long.valueOf(groupPermission.getPropertyId()));
                for (Group group : groupTree.getGroups(groupPermission.getPropertyId())) {
                    userGroupPermissions.add(Long.valueOf(group.getId()));
                }
                for (Device device : groupTree.getDevices(groupPermission.getPropertyId())) {
                    userDevicePermissions.add(Long.valueOf(device.getId()));
                }
            }

            for (Object devicePermission1 : this.dataManager.getPermissions((Class) User.class, (Class) Device.class)) {
                Permission devicePermission = (Permission) devicePermission1;
                getDevicePermissions(devicePermission.getOwnerId()).add(Long.valueOf(devicePermission.getPropertyId()));
            }

            this.groupDevices.clear();
            for (Iterator<Long> iterator = Context.getGroupsManager().getAllItems().iterator(); iterator.hasNext(); ) {
                long groupId = ((Long) iterator.next()).longValue();
                for (Device device : groupTree.getDevices(groupId)) {
                    getGroupDevices(groupId).add(Long.valueOf(device.getId()));
                }
            }


        } catch (SQLException | ClassNotFoundException error) {
            LOGGER.warn("Refresh device permissions error", error);
        }

        this.deviceUsers.clear();
        for (Map.Entry<Long, Set<Long>> entry : this.devicePermissions.entrySet()) {
            for (Iterator<Long> iterator = ((Set) entry.getValue()).iterator(); iterator.hasNext(); ) {
                long deviceId = ((Long) iterator.next()).longValue();
                getAllDeviceUsers(deviceId).add(entry.getKey());
            }

        }
    }

    public boolean getUserAdmin(long userId) {
        User user = getUser(userId);
        return (user != null && user.getAdministrator());
    }

    public void checkAdmin(long userId) throws SecurityException {
        if (!getUserAdmin(userId)) {
            throw new SecurityException("Admin access required");
        }
    }

    public boolean getUserManager(long userId) {
        User user = getUser(userId);
        return (user != null && user.getUserLimit() != 0);
    }

    public void checkManager(long userId) throws SecurityException {
        if (!getUserManager(userId)) {
            throw new SecurityException("Manager access required");
        }
    }

    public void checkManager(long userId, long managedUserId) throws SecurityException {
        checkManager(userId);
        if (!this.usersManager.getUserItems(userId).contains(Long.valueOf(managedUserId))) {
            throw new SecurityException("User access denied");
        }
    }

    public void checkUserLimit(long userId) throws SecurityException {
        int userLimit = getUser(userId).getUserLimit();
        if (userLimit != -1 && this.usersManager.getUserItems(userId).size() >= userLimit) {
            throw new SecurityException("Manager user limit reached");
        }
    }

    public void checkDeviceLimit(long userId) throws SecurityException {
        int deviceLimit = getUser(userId).getDeviceLimit();
        if (deviceLimit != -1) {
            int deviceCount = 0;
            if (getUserManager(userId)) {
                deviceCount = Context.getDeviceManager().getAllManagedItems(userId).size();
            } else {
                deviceCount = Context.getDeviceManager().getAllUserItems(userId).size();
            }
            if (deviceCount >= deviceLimit) {
                throw new SecurityException("User device limit reached");
            }
        }
    }

    public boolean getUserReadonly(long userId) {
        User user = getUser(userId);
        return (user != null && user.getReadonly());
    }

    public boolean getUserDeviceReadonly(long userId) {
        User user = getUser(userId);
        return (user != null && user.getDeviceReadonly());
    }

    public boolean getUserLimitCommands(long userId) {
        User user = getUser(userId);
        return (user != null && user.getLimitCommands());
    }

    public void checkReadonly(long userId) throws SecurityException {
        if (!getUserAdmin(userId) && (this.server.getReadonly() || getUserReadonly(userId))) {
            throw new SecurityException("Account is readonly");
        }
    }

    public void checkDeviceReadonly(long userId) throws SecurityException {
        if (!getUserAdmin(userId) && (this.server.getDeviceReadonly() || getUserDeviceReadonly(userId))) {
            throw new SecurityException("Account is device readonly");
        }
    }

    public void checkLimitCommands(long userId) throws SecurityException {
        if (!getUserAdmin(userId) && (this.server.getLimitCommands() || getUserLimitCommands(userId))) {
            throw new SecurityException("Account has limit sending commands");
        }
    }

    public void checkUserDeviceCommand(long userId, long deviceId, long commandId) throws SecurityException {
        if (!getUserAdmin(userId) && Context.getCommandsManager().checkDeviceCommand(deviceId, commandId)) {
            throw new SecurityException("Command can not be sent to this device");
        }
    }

    public void checkUserEnabled(long userId) throws SecurityException {
        User user = getUser(userId);
        if (user == null) {
            throw new SecurityException("Unknown account");
        }
        if (user.getDisabled()) {
            throw new SecurityException("Account is disabled");
        }
        if (user.getExpirationTime() != null && System.currentTimeMillis() > user.getExpirationTime().getTime()) {
            throw new SecurityException("Account has expired");
        }
    }

    public void checkUserUpdate(long userId, User before, User after) throws SecurityException {
        if (before.getAdministrator() != after.getAdministrator() || before
                .getDeviceLimit() != after.getDeviceLimit() || before
                .getUserLimit() != after.getUserLimit()) {
            checkAdmin(userId);
        }
        User user = getUser(userId);
        if (user != null && user.getExpirationTime() != null && (after
                .getExpirationTime() == null || user
                .getExpirationTime().compareTo(after.getExpirationTime()) < 0)) {
            checkAdmin(userId);
        }
        if (before.getReadonly() != after.getReadonly() || before
                .getDeviceReadonly() != after.getDeviceReadonly() || before
                .getDisabled() != after.getDisabled() || before
                .getLimitCommands() != after.getLimitCommands()) {
            if (userId == after.getId()) {
                checkAdmin(userId);
            }
            if (!getUserAdmin(userId)) {
                checkManager(userId);
            }
        }
    }

    public void checkUser(long userId, long managedUserId) throws SecurityException {
        if (userId != managedUserId && !getUserAdmin(userId)) {
            checkManager(userId, managedUserId);
        }
    }

    public void checkGroup(long userId, long groupId) throws SecurityException {
        if (!getGroupPermissions(userId).contains(Long.valueOf(groupId)) && !getUserAdmin(userId)) {
            checkManager(userId);
            for (Iterator<Long> iterator = this.usersManager.getUserItems(userId).iterator(); iterator.hasNext(); ) {
                long managedUserId = ((Long) iterator.next()).longValue();
                if (getGroupPermissions(managedUserId).contains(Long.valueOf(groupId))) {
                    return;
                }
            }

            throw new SecurityException("Group access denied");
        }
    }

    public void checkDevice(long userId, long deviceId) throws SecurityException {
        if (!Context.getDeviceManager().getUserItems(userId).contains(Long.valueOf(deviceId)) && !getUserAdmin(userId)) {
            checkManager(userId);
            for (Iterator<Long> iterator = this.usersManager.getUserItems(userId).iterator(); iterator.hasNext(); ) {
                long managedUserId = ((Long) iterator.next()).longValue();
                if (Context.getDeviceManager().getUserItems(managedUserId).contains(Long.valueOf(deviceId))) {
                    return;
                }
            }

            throw new SecurityException("Device access denied");
        }
    }

    public void checkRegistration(long userId) {
        if (!this.server.getRegistration() && !getUserAdmin(userId)) {
            throw new SecurityException("Registration disabled");
        }
    }


    public void checkPermission(Class<?> object, long userId, long objectId) throws SecurityException {
        SimpleObjectManager<? extends BaseModel> manager = null;

        if (object.equals(Device.class)) {
            checkDevice(userId, objectId);
        } else if (object.equals(Group.class)) {
            checkGroup(userId, objectId);
        } else if (object.equals(User.class) || object.equals(ManagedUser.class)) {
            checkUser(userId, objectId);
        } else if (object.equals(Geofence.class)) {
            manager = Context.getGeofenceManager();
        } else if (object.equals(Attribute.class)) {
            manager = Context.getAttributesManager();
        } else if (object.equals(Driver.class)) {
            manager = Context.getDriversManager();
        } else if (object.equals(Calendar.class)) {
            manager = Context.getCalendarManager();
        } else if (object.equals(Command.class)) {
            manager = Context.getCommandsManager();
        } else if (object.equals(Maintenance.class)) {
            manager = Context.getMaintenancesManager();
        } else if (object.equals(Notification.class)) {
            manager = Context.getNotificationManager();
        } else {
            throw new IllegalArgumentException("Unknown object type");
        }

        if (manager != null && !manager.checkItemPermission(userId, objectId) && !getUserAdmin(userId)) {
            checkManager(userId);
            for (Iterator<Long> iterator = this.usersManager.getManagedItems(userId).iterator(); iterator.hasNext(); ) {
                long managedUserId = ((Long) iterator.next()).longValue();
                if (manager.checkItemPermission(managedUserId, objectId)) {
                    return;
                }
            }

            throw new SecurityException("Type " + object + " access denied");
        }
    }

    public void refreshAllUsersPermissions() {
        if (Context.getGeofenceManager() != null) {
            Context.getGeofenceManager().refreshUserItems();
        }
        Context.getCalendarManager().refreshUserItems();
        Context.getDriversManager().refreshUserItems();
        Context.getAttributesManager().refreshUserItems();
        Context.getCommandsManager().refreshUserItems();
        Context.getMaintenancesManager().refreshUserItems();
        if (Context.getNotificationManager() != null) {
            Context.getNotificationManager().refreshUserItems();
        }
    }

    public void refreshAllExtendedPermissions() {
        if (Context.getGeofenceManager() != null) {
            Context.getGeofenceManager().refreshExtendedPermissions();
        }
        Context.getDriversManager().refreshExtendedPermissions();
        Context.getAttributesManager().refreshExtendedPermissions();
        Context.getCommandsManager().refreshExtendedPermissions();
        Context.getMaintenancesManager().refreshExtendedPermissions();
    }

    public void refreshPermissions(Permission permission) {
        if (permission.getOwnerClass().equals(User.class)) {
            if (permission.getPropertyClass().equals(Device.class) || permission
                    .getPropertyClass().equals(Group.class)) {
                refreshDeviceAndGroupPermissions();
                refreshAllExtendedPermissions();
            } else if (permission.getPropertyClass().equals(ManagedUser.class)) {
                this.usersManager.refreshUserItems();
            } else if (permission.getPropertyClass().equals(Geofence.class) && Context.getGeofenceManager() != null) {
                Context.getGeofenceManager().refreshUserItems();
            } else if (permission.getPropertyClass().equals(Driver.class)) {
                Context.getDriversManager().refreshUserItems();
            } else if (permission.getPropertyClass().equals(Attribute.class)) {
                Context.getAttributesManager().refreshUserItems();
            } else if (permission.getPropertyClass().equals(Calendar.class)) {
                Context.getCalendarManager().refreshUserItems();
            } else if (permission.getPropertyClass().equals(Command.class)) {
                Context.getCommandsManager().refreshUserItems();
            } else if (permission.getPropertyClass().equals(Maintenance.class)) {
                Context.getMaintenancesManager().refreshUserItems();
            } else if (permission.getPropertyClass().equals(Notification.class) &&
                    Context.getNotificationManager() != null) {
                Context.getNotificationManager().refreshUserItems();
            }
        } else if (permission.getOwnerClass().equals(Device.class) || permission.getOwnerClass().equals(Group.class)) {
            if (permission.getPropertyClass().equals(Geofence.class) && Context.getGeofenceManager() != null) {
                Context.getGeofenceManager().refreshExtendedPermissions();
            } else if (permission.getPropertyClass().equals(Driver.class)) {
                Context.getDriversManager().refreshExtendedPermissions();
            } else if (permission.getPropertyClass().equals(Attribute.class)) {
                Context.getAttributesManager().refreshExtendedPermissions();
            } else if (permission.getPropertyClass().equals(Command.class)) {
                Context.getCommandsManager().refreshExtendedPermissions();
            } else if (permission.getPropertyClass().equals(Maintenance.class)) {
                Context.getMaintenancesManager().refreshExtendedPermissions();
            } else if (permission.getPropertyClass().equals(Notification.class) &&
                    Context.getNotificationManager() != null) {
                Context.getNotificationManager().refreshExtendedPermissions();
            }
        }
    }

    public Server getServer() {
        return this.server;
    }

    public void updateServer(Server server) throws SQLException {
        this.dataManager.updateObject((BaseModel) server);
        this.server = server;
    }

    public User login(String email, String password) throws SQLException {
        User user = this.dataManager.login(email, password);
        if (user != null) {
            checkUserEnabled(user.getId());
            return getUser(user.getId());
        }
        return null;
    }


    public Object lookupAttribute(long userId, String key, Object defaultValue) {
        Object preference, serverPreference = this.server.getAttributes().get(key);
        Object userPreference = getUser(userId).getAttributes().get(key);
        if (this.server.getForceSettings()) {
            preference = (serverPreference != null) ? serverPreference : userPreference;
        } else {
            preference = (userPreference != null) ? userPreference : serverPreference;
        }
        return (preference != null) ? preference : defaultValue;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\PermissionsManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */