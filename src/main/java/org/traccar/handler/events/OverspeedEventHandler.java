package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.DeviceManager;
import org.traccar.database.GeofenceManager;
import org.traccar.model.Device;
import org.traccar.model.DeviceState;
import org.traccar.model.Event;
import org.traccar.model.Geofence;
import org.traccar.model.Position;


@Sharable
public class OverspeedEventHandler
        extends BaseEventHandler {
    public static final String ATTRIBUTE_SPEED = "speed";
    public static final String ATTRIBUTE_SPEED_LIMIT = "speedLimit";
    private final DeviceManager deviceManager;
    private final GeofenceManager geofenceManager;
    private final boolean notRepeat;
    private final long minimalDuration;
    private final boolean preferLowest;

    public OverspeedEventHandler(Config config, DeviceManager deviceManager, GeofenceManager geofenceManager) {
        this.deviceManager = deviceManager;
        this.geofenceManager = geofenceManager;
        this.notRepeat = config.getBoolean(Keys.EVENT_OVERSPEED_NOT_REPEAT);
        this.minimalDuration = config.getLong(Keys.EVENT_OVERSPEED_MINIMAL_DURATION) * 1000L;
        this.preferLowest = config.getBoolean(Keys.EVENT_OVERSPEED_PREFER_LOWEST);
    }

    private Map<Event, Position> newEvent(DeviceState deviceState, double speedLimit) {
        Position position = deviceState.getOverspeedPosition();
        Event event = new Event("deviceOverspeed", position.getDeviceId(), position.getId());
        event.set("speed", Double.valueOf(deviceState.getOverspeedPosition().getSpeed()));
        event.set("speedLimit", Double.valueOf(speedLimit));
        event.setGeofenceId(deviceState.getOverspeedGeofenceId());
        deviceState.setOverspeedState(this.notRepeat);
        deviceState.setOverspeedPosition(null);
        deviceState.setOverspeedGeofenceId(0L);
        return Collections.singletonMap(event, position);
    }

    public Map<Event, Position> updateOverspeedState(DeviceState deviceState, double speedLimit) {
        Map<Event, Position> result = null;
        if (deviceState.getOverspeedState() != null && !deviceState.getOverspeedState().booleanValue() && deviceState
                .getOverspeedPosition() != null && speedLimit != 0.0D) {
            long currentTime = System.currentTimeMillis();
            Position overspeedPosition = deviceState.getOverspeedPosition();
            long overspeedTime = overspeedPosition.getFixTime().getTime();
            if (overspeedTime + this.minimalDuration <= currentTime) {
                result = newEvent(deviceState, speedLimit);
            }
        }
        return result;
    }


    public Map<Event, Position> updateOverspeedState(DeviceState deviceState, Position position, double speedLimit, long geofenceId) {
        Map<Event, Position> result = null;

        Boolean oldOverspeed = deviceState.getOverspeedState();

        long currentTime = position.getFixTime().getTime();
        boolean newOverspeed = (position.getSpeed() > speedLimit);
        if (newOverspeed && !oldOverspeed.booleanValue()) {
            if (deviceState.getOverspeedPosition() == null) {
                deviceState.setOverspeedPosition(position);
                deviceState.setOverspeedGeofenceId(geofenceId);
            }
        } else if (oldOverspeed.booleanValue() && !newOverspeed) {
            deviceState.setOverspeedState(false);
            deviceState.setOverspeedPosition(null);
            deviceState.setOverspeedGeofenceId(0L);
        } else {
            deviceState.setOverspeedPosition(null);
            deviceState.setOverspeedGeofenceId(0L);
        }
        Position overspeedPosition = deviceState.getOverspeedPosition();
        if (overspeedPosition != null) {
            long overspeedTime = overspeedPosition.getFixTime().getTime();
            if (newOverspeed && overspeedTime + this.minimalDuration <= currentTime) {
                result = newEvent(deviceState, speedLimit);
            }
        }
        return result;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        long deviceId = position.getDeviceId();
        Device device = (Device) this.deviceManager.getById(deviceId);
        if (device == null) {
            return null;
        }
        if (!this.deviceManager.isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        double speedLimit = this.deviceManager.lookupAttributeDouble(deviceId, "speedLimit", 0.0D, false);

        double geofenceSpeedLimit = 0.0D;
        long overspeedGeofenceId = 0L;

        if (this.geofenceManager != null && device.getGeofenceIds() != null) {
            for (Iterator<Long> iterator = device.getGeofenceIds().iterator(); iterator.hasNext(); ) {
                long geofenceId = ((Long) iterator.next()).longValue();
                Geofence geofence = (Geofence) this.geofenceManager.getById(geofenceId);
                if (geofence != null) {
                    double currentSpeedLimit = geofence.getDouble("speedLimit");
                    if ((currentSpeedLimit > 0.0D && geofenceSpeedLimit == 0.0D) || (this.preferLowest && currentSpeedLimit < geofenceSpeedLimit) || (!this.preferLowest && currentSpeedLimit > geofenceSpeedLimit)) {


                        geofenceSpeedLimit = currentSpeedLimit;
                        overspeedGeofenceId = geofenceId;
                    }
                }
            }

        }
        if (geofenceSpeedLimit > 0.0D) {
            speedLimit = geofenceSpeedLimit;
        }

        if (speedLimit == 0.0D) {
            return null;
        }

        Map<Event, Position> result = null;
        DeviceState deviceState = this.deviceManager.getDeviceState(deviceId);

        if (deviceState.getOverspeedState() == null) {
            deviceState.setOverspeedState((position.getSpeed() > speedLimit));
            deviceState.setOverspeedGeofenceId((position.getSpeed() > speedLimit) ? overspeedGeofenceId : 0L);
        } else {
            result = updateOverspeedState(deviceState, position, speedLimit, overspeedGeofenceId);
        }

        this.deviceManager.setDeviceState(deviceId, deviceState);
        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\OverspeedEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */