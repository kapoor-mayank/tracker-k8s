package org.traccar.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.config.Config;
import org.traccar.model.BaseModel;
import org.traccar.model.Device;
import org.traccar.model.DeviceAccumulators;
import org.traccar.model.DeviceState;
import org.traccar.model.Group;
import org.traccar.model.Position;
import org.traccar.model.Server;


public class DeviceManager
        extends BaseObjectManager<Device>
        implements IdentityManager, ManagableObjects {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManager.class);

    public static final long DEFAULT_REFRESH_DELAY = 300L;

    private final Config config;

    private final long dataRefreshDelay;
    private boolean lookupGroupsAttribute;
    private Map<String, Device> devicesByUniqueId;
    private Map<String, Device> devicesByPhone;
    private AtomicLong devicesLastUpdate = new AtomicLong();

    private final Map<Long, Position> positions = new ConcurrentHashMap<>();

    private final Map<Long, DeviceState> deviceStates = new ConcurrentHashMap<>();

    public DeviceManager(DataManager dataManager) {
        super(dataManager, Device.class);
        this.config = Context.getConfig();
        if (this.devicesByPhone == null) {
            this.devicesByPhone = new ConcurrentHashMap<>();
        }
        if (this.devicesByUniqueId == null) {
            this.devicesByUniqueId = new ConcurrentHashMap<>();
        }
        this.dataRefreshDelay = this.config.getLong("database.refreshDelay", 300L) * 1000L;
        this.lookupGroupsAttribute = this.config.getBoolean("deviceManager.lookupGroupsAttribute");
        refreshLastPositions();
    }


    public long addUnknownDevice(String uniqueId) {
        Device device = new Device();
        device.setName(uniqueId);
        device.setUniqueId(uniqueId);
        device.setCategory(Context.getConfig().getString("database.registerUnknown.defaultCategory"));

        long defaultGroupId = Context.getConfig().getLong("database.registerUnknown.defaultGroupId");
        if (defaultGroupId != 0L) {
            device.setGroupId(defaultGroupId);
        }

        try {
            addItem(device);

            LOGGER.info("Automatically registered device " + uniqueId);

            if (defaultGroupId != 0L) {
                Context.getPermissionsManager().refreshDeviceAndGroupPermissions();
                Context.getPermissionsManager().refreshAllExtendedPermissions();
            }

            return device.getId();
        } catch (SQLException e) {
            LOGGER.warn("Automatic device registration error", e);
            return 0L;
        }
    }

    public void updateDeviceCache(boolean force) throws SQLException {
        long lastUpdate = this.devicesLastUpdate.get();
        if ((force || System.currentTimeMillis() - lastUpdate > this.dataRefreshDelay) && this.devicesLastUpdate
                .compareAndSet(lastUpdate, System.currentTimeMillis())) {
            refreshItems();
        }
    }


    public Device getByUniqueId(String uniqueId) throws SQLException {
        return this.devicesByUniqueId.get(uniqueId);
    }

    public Device getDeviceByPhone(String phone) {
        return this.devicesByPhone.get(phone);
    }


    public Set<Long> getAllItems() {
        Set<Long> result = super.getAllItems();
        if (result.isEmpty()) {
            try {
                updateDeviceCache(true);
            } catch (SQLException e) {
                LOGGER.warn("Update device cache error", e);
            }
            result = super.getAllItems();
        }
        return result;
    }

    public Collection<Device> getAllDevices() {
        return getItems(getAllItems());
    }

    public Set<Long> getAllUserItems(long userId) {
        return Context.getPermissionsManager().getDevicePermissions(userId);
    }


    public Set<Long> getUserItems(long userId) {
        if (Context.getPermissionsManager() != null) {
            Set<Long> result = new HashSet<>();
            for (Iterator<Long> iterator = Context.getPermissionsManager().getDevicePermissions(userId).iterator(); iterator.hasNext(); ) {
                long deviceId = ((Long) iterator.next()).longValue();
                Device device = getById(deviceId);
                if (device != null && !device.getDisabled()) {
                    result.add(Long.valueOf(deviceId));
                }
            }

            return result;
        }
        return new HashSet<>();
    }


    public Set<Long> getAllManagedItems(long userId) {
        Set<Long> result = new HashSet<>();
        result.addAll(getAllUserItems(userId));
        for (Iterator<Long> iterator = Context.getUsersManager().getUserItems(userId).iterator(); iterator.hasNext(); ) {
            long managedUserId = ((Long) iterator.next()).longValue();
            result.addAll(getAllUserItems(managedUserId));
        }

        return result;
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

    private void putUniqueDeviceId(Device device) {
        if (this.devicesByUniqueId == null) {
            this.devicesByUniqueId = new ConcurrentHashMap<>(getAllItems().size());
        }
        this.devicesByUniqueId.put(device.getUniqueId(), device);
    }

    private void putPhone(Device device) {
        if (this.devicesByPhone == null) {
            this.devicesByPhone = new ConcurrentHashMap<>(getAllItems().size());
        }
        this.devicesByPhone.put(device.getPhone(), device);
    }


    protected void addNewItem(Device device) {
        super.addNewItem(device);
        putUniqueDeviceId(device);
        if (device.getPhone() != null && !device.getPhone().isEmpty()) {
            putPhone(device);
        }
        if (Context.getGeofenceManager() != null) {
            Position lastPosition = getLastPosition(device.getId());
            if (lastPosition != null) {
                device.setGeofenceIds(Context.getGeofenceManager().getCurrentDeviceGeofences(lastPosition));
            }
        }
    }


    protected void updateCachedItem(Device device) {
        Device cachedDevice = getById(device.getId());
        cachedDevice.setName(device.getName());
        cachedDevice.setGroupId(device.getGroupId());
        cachedDevice.setCategory(device.getCategory());
        cachedDevice.setContact(device.getContact());
        cachedDevice.setModel(device.getModel());
        cachedDevice.setDisabled(device.getDisabled());
        cachedDevice.setAttributes(device.getAttributes());
        if (!device.getUniqueId().equals(cachedDevice.getUniqueId())) {
            this.devicesByUniqueId.remove(cachedDevice.getUniqueId());
            cachedDevice.setUniqueId(device.getUniqueId());
            putUniqueDeviceId(cachedDevice);
        }
        if (device.getPhone() != null && !device.getPhone().isEmpty() &&
                !device.getPhone().equals(cachedDevice.getPhone())) {
            String phone = cachedDevice.getPhone();
            if (phone != null && !phone.isEmpty()) {
                this.devicesByPhone.remove(phone);
            }
            cachedDevice.setPhone(device.getPhone());
            putPhone(cachedDevice);
        }
    }


    protected void removeCachedItem(long deviceId) {
        Device cachedDevice = getById(deviceId);
        if (cachedDevice != null) {
            String deviceUniqueId = cachedDevice.getUniqueId();
            String phone = cachedDevice.getPhone();
            super.removeCachedItem(deviceId);
            this.devicesByUniqueId.remove(deviceUniqueId);
            if (phone != null && !phone.isEmpty()) {
                this.devicesByPhone.remove(phone);
            }
        }
        this.positions.remove(Long.valueOf(deviceId));
    }

    public void updateDeviceStatus(Device device) throws SQLException {
        getDataManager().updateDeviceStatus(device);
        Device cachedDevice = getById(device.getId());
        if (cachedDevice != null) {
            cachedDevice.setStatus(device.getStatus());
        }
    }

    private void refreshLastPositions() {
        if (getDataManager() != null) {
            try {
                for (Position position : getDataManager().getLatestPositions()) {
                    this.positions.put(Long.valueOf(position.getDeviceId()), position);
                }
            } catch (SQLException error) {
                LOGGER.warn("Load latest positions error", error);
            }
        }
    }

    public boolean isLatestPosition(Position position) {
        Position lastPosition = getLastPosition(position.getDeviceId());
        return (lastPosition == null || position.getFixTime().compareTo(lastPosition.getFixTime()) >= 0);
    }


    public void updateLatestPosition(Position position) throws SQLException {
        if (isLatestPosition(position)) {

            getDataManager().updateLatestPosition(position);

            Device device = getById(position.getDeviceId());
            if (device != null) {
                device.setPositionId(position.getId());
            }

            this.positions.put(Long.valueOf(position.getDeviceId()), position);

            if (Context.getConnectionManager() != null) {
                Context.getConnectionManager().updatePosition(position);
            }
        }
    }


    public Position getLastPosition(long deviceId) {
        return this.positions.get(Long.valueOf(deviceId));
    }


    public Collection<Position> getInitialState(long userId) {
        List<Position> result = new LinkedList<>();

        if (Context.getPermissionsManager() != null) {
            for (Iterator<Long> iterator = (Context.getPermissionsManager().getUserAdmin(userId) ?
                    getAllUserItems(userId) : getUserItems(userId)).iterator(); iterator.hasNext(); ) {
                long deviceId = ((Long) iterator.next()).longValue();
                if (this.positions.containsKey(Long.valueOf(deviceId))) {
                    result.add(this.positions.get(Long.valueOf(deviceId)));
                }
            }
        }

        return result;
    }


    public boolean lookupAttributeBoolean(long deviceId, String attributeName, boolean defaultValue, boolean lookupConfig) {
        Object result = lookupAttribute(deviceId, attributeName, lookupConfig);
        if (result != null) {
            return (result instanceof String) ? Boolean.parseBoolean((String) result) : ((Boolean) result).booleanValue();
        }
        return defaultValue;
    }


    public String lookupAttributeString(long deviceId, String attributeName, String defaultValue, boolean lookupConfig) {
        Object result = lookupAttribute(deviceId, attributeName, lookupConfig);
        return (result != null) ? (String) result : defaultValue;
    }


    public int lookupAttributeInteger(long deviceId, String attributeName, int defaultValue, boolean lookupConfig) {
        Object result = lookupAttribute(deviceId, attributeName, lookupConfig);
        if (result != null) {
            return (result instanceof String) ? Integer.parseInt((String) result) : ((Number) result).intValue();
        }
        return defaultValue;
    }


    public long lookupAttributeLong(long deviceId, String attributeName, long defaultValue, boolean lookupConfig) {
        Object result = lookupAttribute(deviceId, attributeName, lookupConfig);
        if (result != null) {
            return (result instanceof String) ? Long.parseLong((String) result) : ((Number) result).longValue();
        }
        return defaultValue;
    }


    public double lookupAttributeDouble(long deviceId, String attributeName, double defaultValue, boolean lookupConfig) {
        Object result = lookupAttribute(deviceId, attributeName, lookupConfig);
        if (result != null) {
            return (result instanceof String) ? Double.parseDouble((String) result) : ((Number) result).doubleValue();
        }
        return defaultValue;
    }

    private Object lookupAttribute(long deviceId, String attributeName, boolean lookupConfig) {
        Object result = null;
        Device device = getById(deviceId);
        if (device != null) {
            result = device.getAttributes().get(attributeName);
            if (result == null && this.lookupGroupsAttribute) {
                long groupId = device.getGroupId();
                while (groupId != 0L) {
                    Group group = Context.getGroupsManager().getById(groupId);
                    if (group != null) {
                        result = group.getAttributes().get(attributeName);
                        if (result != null) {
                            break;
                        }
                        groupId = group.getGroupId();
                        continue;
                    }
                    groupId = 0L;
                }
            }

            if (result == null) {
                if (lookupConfig) {
                    result = Context.getConfig().getString(attributeName);
                } else {
                    Server server = Context.getPermissionsManager().getServer();
                    result = server.getAttributes().get(attributeName);
                }
            }
        }
        return result;
    }

    public void resetDeviceAccumulators(DeviceAccumulators deviceAccumulators) throws SQLException {
        Position last = this.positions.get(Long.valueOf(deviceAccumulators.getDeviceId()));
        if (last != null) {
            if (deviceAccumulators.getTotalDistance() != null) {
                last.getAttributes().put("totalDistance", deviceAccumulators.getTotalDistance());
            }
            if (deviceAccumulators.getHours() != null) {
                last.getAttributes().put("hours", deviceAccumulators.getHours());
            }
            getDataManager().addObject((BaseModel) last);
            updateLatestPosition(last);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public DeviceState getDeviceState(long deviceId) {
        DeviceState deviceState = this.deviceStates.get(Long.valueOf(deviceId));
        if (deviceState == null) {
            deviceState = new DeviceState();
            this.deviceStates.put(Long.valueOf(deviceId), deviceState);
        }
        return deviceState;
    }

    public void setDeviceState(long deviceId, DeviceState deviceState) {
        this.deviceStates.put(Long.valueOf(deviceId), deviceState);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\DeviceManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */