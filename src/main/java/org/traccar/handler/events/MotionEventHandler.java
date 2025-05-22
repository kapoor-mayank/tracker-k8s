package org.traccar.handler.events;

import io.netty.channel.ChannelHandler.Sharable;

import java.util.Collections;
import java.util.Map;

import org.traccar.database.DeviceManager;
import org.traccar.database.IdentityManager;
import org.traccar.model.Device;
import org.traccar.model.DeviceState;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.reports.ReportUtils;
import org.traccar.reports.model.TripsConfig;


@Sharable
public class MotionEventHandler
        extends BaseEventHandler {
    private final IdentityManager identityManager;
    private final DeviceManager deviceManager;
    private final TripsConfig tripsConfig;

    public MotionEventHandler(IdentityManager identityManager, DeviceManager deviceManager, TripsConfig tripsConfig) {
        this.identityManager = identityManager;
        this.deviceManager = deviceManager;
        this.tripsConfig = tripsConfig;
    }

    private Map<Event, Position> newEvent(DeviceState deviceState, boolean newMotion) {
        String eventType = newMotion ? "deviceMoving" : "deviceStopped";
        Position position = deviceState.getMotionPosition();
        Event event = new Event(eventType, position.getDeviceId(), position.getId());
        deviceState.setMotionState(newMotion);
        deviceState.setMotionPosition(null);
        return Collections.singletonMap(event, position);
    }

    public Map<Event, Position> updateMotionState(DeviceState deviceState) {
        Map<Event, Position> result = null;
        if (deviceState.getMotionState() != null && deviceState.getMotionPosition() != null) {
            boolean newMotion = !deviceState.getMotionState().booleanValue();
            Position motionPosition = deviceState.getMotionPosition();
            long currentTime = System.currentTimeMillis();

            long motionTime = motionPosition.getFixTime().getTime() + (newMotion ? this.tripsConfig.getMinimalTripDuration() : this.tripsConfig.getMinimalParkingDuration());
            if (motionTime <= currentTime) {
                result = newEvent(deviceState, newMotion);
            }
        }
        return result;
    }

    public Map<Event, Position> updateMotionState(DeviceState deviceState, Position position) {
        return updateMotionState(deviceState, position, position.getBoolean("motion"));
    }

    public Map<Event, Position> updateMotionState(DeviceState deviceState, Position position, boolean newMotion) {
        Map<Event, Position> result = null;
        Boolean oldMotion = deviceState.getMotionState();

        long currentTime = position.getFixTime().getTime();
        if (newMotion != oldMotion.booleanValue()) {
            if (deviceState.getMotionPosition() == null) {
                deviceState.setMotionPosition(position);
            }
        } else {
            deviceState.setMotionPosition(null);
        }

        Position motionPosition = deviceState.getMotionPosition();
        if (motionPosition != null) {
            long motionTime = motionPosition.getFixTime().getTime();
            double distance = ReportUtils.calculateDistance(motionPosition, position, false);
            Boolean ignition = null;
            if (this.tripsConfig.getUseIgnition() && position
                    .getAttributes().containsKey("ignition")) {
                ignition = Boolean.valueOf(position.getBoolean("ignition"));
            }
            if (newMotion) {
                if (motionTime + this.tripsConfig.getMinimalTripDuration() <= currentTime || distance >= this.tripsConfig
                        .getMinimalTripDistance()) {
                    result = newEvent(deviceState, newMotion);
                }
            } else if (motionTime + this.tripsConfig.getMinimalParkingDuration() <= currentTime || (ignition != null &&
                    !ignition.booleanValue())) {
                result = newEvent(deviceState, newMotion);
            }
        }

        return result;
    }


    protected Map<Event, Position> analyzePosition(Position position) {
        long deviceId = position.getDeviceId();
        Device device = this.identityManager.getById(deviceId);
        if (device == null) {
            return null;
        }
        if (!this.identityManager.isLatestPosition(position) || (
                !this.tripsConfig.getProcessInvalidPositions() && !position.getValid())) {
            return null;
        }

        Map<Event, Position> result = null;
        DeviceState deviceState = this.deviceManager.getDeviceState(deviceId);

        if (deviceState.getMotionState() == null) {
            deviceState.setMotionState(position.getBoolean("motion"));
        } else {
            result = updateMotionState(deviceState, position);
        }
        this.deviceManager.setDeviceState(deviceId, deviceState);
        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\events\MotionEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */