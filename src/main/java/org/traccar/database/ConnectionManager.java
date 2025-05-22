package org.traccar.database;

import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.GlobalTimer;
import org.traccar.Main;
import org.traccar.Protocol;
import org.traccar.handler.events.MotionEventHandler;
import org.traccar.handler.events.OverspeedEventHandler;
import org.traccar.model.Device;
import org.traccar.model.DeviceState;
import org.traccar.model.Event;
import org.traccar.model.Position;


public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private static final long DEFAULT_TIMEOUT = 600L;

    private final long deviceTimeout;

    private final boolean enableStatusEvents = true;
    private final boolean updateDeviceState;
    private final Map<Long, ActiveDevice> activeDevices = new ConcurrentHashMap<>();
    private final Map<Long, Set<UpdateListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Long, Timeout> timeouts = new ConcurrentHashMap<>();

    public ConnectionManager() {
        this.deviceTimeout = Context.getConfig().getLong("status.timeout", 600L) * 1000L;
        this.updateDeviceState = Context.getConfig().getBoolean("status.updateDeviceState");
    }

    public void addActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
        if (!"dualcam".equals(protocol.getName())) {
            this.activeDevices.put(Long.valueOf(deviceId), new ActiveDevice(deviceId, protocol, channel, remoteAddress));
        }
    }

    public void removeActiveDevice(Channel channel) {
        for (ActiveDevice activeDevice : this.activeDevices.values()) {
            if (activeDevice.getChannel() == channel) {
                updateDevice(activeDevice.getDeviceId(), "offline", null);
                this.activeDevices.remove(Long.valueOf(activeDevice.getDeviceId()));
                break;
            }
        }
    }

    public ActiveDevice getActiveDevice(long deviceId) {
        return this.activeDevices.get(Long.valueOf(deviceId));
    }

    public void updateDevice(final long deviceId, String status, Date time) {
        Device device = Context.getIdentityManager().getById(deviceId);
        if (device == null) {
            return;
        }

        String oldStatus = device.getStatus();
        device.setStatus(status);

        if (!status.equals(oldStatus)) {
            String eventType;
            Map<Event, Position> events = new HashMap<>();
            switch (status) {
                case "online":
                    if (Context.getRedisManager() != null) {
                        Context.getRedisManager().addDevice(device);
                    }
                    eventType = "deviceOnline";
                    break;
                case "unknown":
                    if (Context.getRedisManager() != null) {
                        Context.getRedisManager().removeDevice(device);
                    }
                    eventType = "deviceUnknown";
                    if (this.updateDeviceState) {
                        events.putAll(updateDeviceState(deviceId));
                    }
                    break;
                default:
                    if (Context.getRedisManager() != null) {
                        Context.getRedisManager().removeDevice(device);
                    }
                    eventType = "deviceOffline";
                    if (this.updateDeviceState) {
                        events.putAll(updateDeviceState(deviceId));
                    }
                    break;
            }
            events.put(new Event(eventType, deviceId), null);
            Context.getNotificationManager().updateEvents(events);
        }

        Timeout timeout = this.timeouts.remove(Long.valueOf(deviceId));
        if (timeout != null) {
            timeout.cancel();
        }

        if (time != null) {
            device.setLastUpdate(time);
        }

        if (status.equals("online")) {
            this.timeouts.put(Long.valueOf(deviceId), GlobalTimer.getTimer().newTimeout(new TimerTask() {
                public void run(Timeout timeout) {
                    if (!timeout.isCancelled()) {
                        ConnectionManager.this.updateDevice(deviceId, "unknown", null);
                    }
                }
            }, this.deviceTimeout, TimeUnit.MILLISECONDS));
        }

        try {
            Context.getDeviceManager().updateDeviceStatus(device);
        } catch (SQLException error) {
            LOGGER.warn("Update device status error", error);
        }

        updateDevice(device);

        if (status.equals("online") && !oldStatus.equals("online")) {
            Context.getCommandsManager().sendQueuedCommands(getActiveDevice(deviceId));
        }
    }

    public Map<Event, Position> updateDeviceState(long deviceId) {
        DeviceState deviceState = Context.getDeviceManager().getDeviceState(deviceId);
        Map<Event, Position> result = new HashMap<>();


        Map<Event, Position> event = ((MotionEventHandler) Main.getInjector().getInstance(MotionEventHandler.class)).updateMotionState(deviceState);
        if (event != null) {
            result.putAll(event);
        }


        event = ((OverspeedEventHandler) Main.getInjector().getInstance(OverspeedEventHandler.class)).updateOverspeedState(deviceState, Context.getDeviceManager()
                .lookupAttributeDouble(deviceId, "speedLimit", 0.0D, false));
        if (event != null) {
            result.putAll(event);
        }

        return result;
    }

    public static interface UpdateListener {
        void onUpdateDevice(Device param1Device);

        void onUpdatePosition(Position param1Position);

        void onUpdateEvent(Event param1Event);
    }

    public synchronized void updateDevice(Device device) {
        for (Iterator<Long> iterator = Context.getPermissionsManager().getDeviceUsers(device.getId()).iterator(); iterator.hasNext(); ) {
            long userId = ((Long) iterator.next()).longValue();
            if (this.listeners.containsKey(Long.valueOf(userId))) {
                for (UpdateListener listener : this.listeners.get(Long.valueOf(userId))) {
                    listener.onUpdateDevice(device);
                }
            }
        }

    }

    public synchronized void updatePosition(Position position) {
        long deviceId = position.getDeviceId();

        for (Iterator<Long> iterator = Context.getPermissionsManager().getDeviceUsers(deviceId).iterator(); iterator.hasNext(); ) {
            long userId = ((Long) iterator.next()).longValue();
            if (this.listeners.containsKey(Long.valueOf(userId))) {
                for (UpdateListener listener : this.listeners.get(Long.valueOf(userId))) {
                    listener.onUpdatePosition(position);
                }
            }
        }

    }

    public synchronized void updateEvent(long userId, Event event) {
        if (this.listeners.containsKey(Long.valueOf(userId))) {
            for (UpdateListener listener : this.listeners.get(Long.valueOf(userId))) {
                listener.onUpdateEvent(event);
            }
        }
    }


    public synchronized void addListener(long userId, UpdateListener listener) {
        if (!this.listeners.containsKey(Long.valueOf(userId))) {
            this.listeners.put(Long.valueOf(userId), new HashSet<>());
        }
        ((Set<UpdateListener>) this.listeners.get(Long.valueOf(userId))).add(listener);
    }

    public synchronized void removeListener(long userId, UpdateListener listener) {
        if (!this.listeners.containsKey(Long.valueOf(userId))) {
            this.listeners.put(Long.valueOf(userId), new HashSet<>());
        }
        ((Set) this.listeners.get(Long.valueOf(userId))).remove(listener);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\ConnectionManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */