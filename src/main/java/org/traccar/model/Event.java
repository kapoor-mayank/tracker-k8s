package org.traccar.model;

import java.util.Date;

public class Event extends Message {
    public static final String ALL_EVENTS = "allEvents";

    public static final String TYPE_COMMAND_RESULT = "commandResult";

    public static final String TYPE_DEVICE_ONLINE = "deviceOnline";

    public static final String TYPE_DEVICE_UNKNOWN = "deviceUnknown";

    public static final String TYPE_DEVICE_OFFLINE = "deviceOffline";

    public static final String TYPE_DEVICE_MOVING = "deviceMoving";

    public static final String TYPE_DEVICE_STOPPED = "deviceStopped";

    public static final String TYPE_DEVICE_OVERSPEED = "deviceOverspeed";

    public static final String TYPE_DEVICE_FUEL_DROP = "deviceFuelDrop";

    public static final String TYPE_GEOFENCE_ENTER = "geofenceEnter";

    public static final String TYPE_GEOFENCE_EXIT = "geofenceExit";

    public static final String TYPE_ALARM = "alarm";

    public static final String TYPE_IGNITION_ON = "ignitionOn";

    public static final String TYPE_IGNITION_OFF = "ignitionOff";

    public static final String TYPE_MAINTENANCE = "maintenance";

    public static final String TYPE_TEXT_MESSAGE = "textMessage";

    public static final String TYPE_DRIVER_CHANGED = "driverChanged";

    private Date serverTime;

    private long positionId;

    private long geofenceId;

    private long maintenanceId;

    public Event(String type, long deviceId, long positionId) {
        this(type, deviceId);
        setPositionId(positionId);
    }

    public Event(String type, long deviceId) {
        this.geofenceId = 0L;
        this.maintenanceId = 0L;
        setType(type);
        setDeviceId(deviceId);
        this.serverTime = new Date();
    }

    public Event() {
        this.geofenceId = 0L;
        this.maintenanceId = 0L;
    }

    public Date getServerTime() {
        return this.serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public long getPositionId() {
        return this.positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    public long getGeofenceId() {
        return this.geofenceId;
    }

    public void setGeofenceId(long geofenceId) {
        this.geofenceId = geofenceId;
    }

    public long getMaintenanceId() {
        return this.maintenanceId;
    }

    public void setMaintenanceId(long maintenanceId) {
        this.maintenanceId = maintenanceId;
    }
}
