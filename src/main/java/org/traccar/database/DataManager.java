package org.traccar.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.config.Config;
import org.traccar.model.Attribute;
import org.traccar.model.BaseModel;
import org.traccar.model.Calendar;
import org.traccar.model.Command;
import org.traccar.model.Device;
import org.traccar.model.Driver;
import org.traccar.model.Event;
import org.traccar.model.Geofence;
import org.traccar.model.Group;
import org.traccar.model.Maintenance;
import org.traccar.model.ManagedUser;
import org.traccar.model.Notification;
import org.traccar.model.Permission;
import org.traccar.model.Position;
import org.traccar.model.Server;
import org.traccar.model.Statistics;
import org.traccar.model.User;


public class DataManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    public static final String ACTION_SELECT_ALL = "selectAll";

    public static final String ACTION_SELECT = "select";

    public static final String ACTION_INSERT = "insert";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    private Server server;
    private User user;
    private Group group;
    private long deviceIncrement;
    private Map<Long, Device> devices = new HashMap<>();

    private boolean forceLdap;


    public DataManager(Config config) throws Exception {
        this.server = new Server();
        this.server.setId(1L);

        this.user = new User();
        this.user.setId(1L);
        this.user.setAdministrator(true);

        this.group = new Group();
        this.group.setId(1L);
    }


    public static String constructObjectQuery(String action, Class<?> clazz, boolean extended) {
        return null;
    }

    public static String constructPermissionQuery(String action, Class<?> owner, Class<?> property) {
        return null;
    }

    public User login(String email, String password) throws SQLException {
        if (email.equals("admin") && password.equals(Context.getConfig().getString("user.password", "admin"))) {
            return this.user;
        }
        return null;
    }


    public void updateUser(User user) throws SQLException {
    }


    public void updateDeviceStatus(Device device) throws SQLException {
    }

    public Collection<Position> getPositions(long deviceId, Date from, Date to) throws SQLException {
        return Collections.emptyList();
    }

    public Position getPosition(long positionId) throws SQLException {
        return null;
    }


    public void addPosition(Position position) throws SQLException {
    }


    public void updateLatestPosition(Position position) throws SQLException {
    }

    public Collection<Position> getLatestPositions() throws SQLException {
        return Collections.emptyList();
    }


    public void clearHistory() throws SQLException {
    }

    public Server getServer() throws SQLException {
        return this.server;
    }

    public Event getEvent(long eventId) throws SQLException {
        return null;
    }

    public Collection<Event> getEvents(long deviceId, Date from, Date to) throws SQLException {
        return Collections.emptyList();
    }

    public Collection<Statistics> getStatistics(Date from, Date to) throws SQLException {
        return Collections.emptyList();
    }


    public void addStatistics(Statistics statistics) throws SQLException {
    }

    public static Class<?> getClassByName(String name) throws ClassNotFoundException {
        switch (name.toLowerCase().replace("id", "")) {
            case "device":
                return Device.class;
            case "group":
                return Group.class;
            case "user":
                return User.class;
            case "manageduser":
                return ManagedUser.class;
            case "geofence":
                return Geofence.class;
            case "driver":
                return Driver.class;
            case "attribute":
                return Attribute.class;
            case "calendar":
                return Calendar.class;
            case "command":
                return Command.class;
            case "maintenance":
                return Maintenance.class;
            case "notification":
                return Notification.class;
        }
        throw new ClassNotFoundException();
    }


    public void linkObject(Class<?> owner, long ownerId, Class<?> property, long propertyId, boolean link) throws SQLException {
    }


    public <T extends BaseModel> T getObject(Class<T> clazz, long entityId) throws SQLException {
        return null;
    }

    public <T> Collection<T> getObjects(Class<T> clazz) throws SQLException {
        if (clazz.equals(Server.class))
            return Collections.singleton((T) this.server);
        if (clazz.equals(User.class))
            return Collections.singleton((T) this.user);
        if (clazz.equals(Group.class))
            return Collections.singleton((T) this.group);
        if (clazz.equals(Device.class)) {
            return (Collection) this.devices.values();
        }
        return Collections.emptyList();
    }


    public Collection<Permission> getPermissions(Class<? extends BaseModel> owner, Class<? extends BaseModel> property) throws SQLException, ClassNotFoundException {
        if (owner.equals(User.class) && property.equals(Group.class)) {
            LinkedHashMap<String, Long> permission = new LinkedHashMap<>();
            permission.put("userId", Long.valueOf(this.user.getId()));
            permission.put("groupId", Long.valueOf(this.group.getId()));
            return Collections.singleton(new Permission(permission));
        }
        return Collections.emptyList();
    }


    public synchronized void addObject(BaseModel entity) throws SQLException {
//        LOGGER.info("DataManager addObject()");
        if (entity instanceof Device) {
            Device device = (Device) entity;
            device.setId(++this.deviceIncrement);
            device.setGroupId(this.group.getId());
            if (Context.getConfig().getBoolean("redis.deviceModels")) {
                device.setModel(Context.getRedisManager().getDeviceModel(device.getUniqueId()));
            }
            this.devices.put(Long.valueOf(device.getId()), device);
        }
    }

    public void updateObject(BaseModel entity) throws SQLException {
    }

    public void removeObject(Class<? extends BaseModel> clazz, long entityId) throws SQLException {
    }
}
