package org.traccar.model;

import java.util.Date;

import org.traccar.Context;
import org.traccar.database.QueryIgnore;


public class Position
        extends Message {
    public static final String KEY_ORIGINAL = "raw";
    public static final String KEY_INDEX = "index";
    public static final String KEY_HDOP = "hdop";
    public static final String KEY_VDOP = "vdop";
    public static final String KEY_PDOP = "pdop";
    public static final String KEY_SATELLITES = "sat";
    public static final String KEY_SATELLITES_VISIBLE = "satVisible";
    public static final String KEY_RSSI = "rssi";
    public static final String KEY_GPS = "gps";
    public static final String KEY_ROAMING = "roaming";
    public static final String KEY_EVENT = "event";
    public static final String KEY_ALARM = "alarm";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ODOMETER = "odometer";
    public static final String KEY_ODOMETER_SERVICE = "serviceOdometer";
    public static final String KEY_ODOMETER_TRIP = "tripOdometer";
    public static final String KEY_HOURS = "hours";
    public static final String KEY_STEPS = "steps";
    public static final String KEY_HEART_RATE = "heartRate";
    public static final String KEY_INPUT = "input";
    public static final String KEY_OUTPUT = "output";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_VIDEO = "video";
    public static final String KEY_AUDIO = "audio";
    public static final String KEY_POWER = "power";
    public static final String KEY_BATTERY = "battery";
    public static final String KEY_BATTERY_LEVEL = "batteryLevel";
    public static final String KEY_FUEL_LEVEL = "fuel";
    public static final String KEY_FUEL_USED = "fuelUsed";
    public static final String KEY_FUEL_CONSUMPTION = "fuelConsumption";
    public static final String KEY_VERSION_FW = "versionFw";
    public static final String KEY_VERSION_HW = "versionHw";
    public static final String KEY_TYPE = "type";
    public static final String KEY_IMMOBILIZER = "immobilizer";
    public static final String KEY_GSM_SIGNAL = "GSMSignal";
    public static final String KEY_GPS_GLONASS_SIGNAL = "GPSGlonassSignal";
    public static final String KEY_EXTERNAL_VOLTAGE = "externalVoltage";
    public static final String KEY_IGNITION = "ignition";
    public static final String KEY_FLAGS = "flags";
    public static final String KEY_ANTENNA = "antenna";
    public static final String KEY_CHARGE = "charge";
    public static final String KEY_IP = "ip";
    public static final String KEY_ARCHIVE = "archive";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_TOTAL_DISTANCE = "totalDistance";
    public static final String KEY_RPM = "rpm";
    public static final String KEY_VIN = "vin";
    public static final String KEY_APPROXIMATE = "approximate";
    public static final String KEY_THROTTLE = "throttle";
    public static final String KEY_MOTION = "motion";
    public static final String KEY_ARMED = "armed";
    public static final String KEY_GEOFENCE = "geofence";
    public static final String KEY_ACCELERATION = "acceleration";
    public static final String KEY_DEVICE_TEMP = "deviceTemp";
    public static final String KEY_COOLANT_TEMP = "coolantTemp";
    public static final String KEY_ENGINE_LOAD = "engineLoad";
    public static final String KEY_OPERATOR = "operator";
    public static final String KEY_COMMAND = "command";
    public static final String KEY_BLOCKED = "blocked";
    public static final String KEY_DOOR = "door";
    public static final String KEY_AXLE_WEIGHT = "axleWeight";
    public static final String KEY_G_SENSOR = "gSensor";
    public static final String KEY_ICCID = "iccid";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_SPEED_LIMIT = "speedLimit";
    public static final String KEY_DRIVING_TIME = "drivingTime";
    public static final String KEY_DTCS = "dtcs";
    public static final String KEY_OBD_SPEED = "obdSpeed";
    public static final String KEY_OBD_ODOMETER = "obdOdometer";
    public static final String KEY_RESULT = "result";
    public static final String KEY_DRIVER_UNIQUE_ID = "driverUniqueId";
    public static final String KEY_CARD = "card";
    public static final String PREFIX_TEMP = "temp";
    public static final String PREFIX_ADC = "adc";
    public static final String PREFIX_IO = "io";
    public static final String PREFIX_COUNT = "count";
    public static final String PREFIX_IN = "in";
    public static final String PREFIX_OUT = "out";
    public static final String ALARM_GENERAL = "general";
    public static final String ALARM_SOS = "sos";
    public static final String ALARM_VIBRATION = "vibration";
    public static final String ALARM_MOVEMENT = "movement";
    public static final String ALARM_LOW_SPEED = "lowspeed";
    public static final String ALARM_OVERSPEED = "overspeed";
    public static final String ALARM_FALL_DOWN = "fallDown";
    public static final String ALARM_LOW_POWER = "lowPower";
    public static final String ALARM_LOW_BATTERY = "lowBattery";
    public static final String ALARM_FAULT = "fault";
    public static final String ALARM_POWER_OFF = "powerOff";
    public static final String ALARM_POWER_ON = "powerOn";
    public static final String ALARM_DOOR = "door";
    public static final String ALARM_LOCK = "lock";
    public static final String ALARM_UNLOCK = "unlock";
    public static final String ALARM_GEOFENCE = "geofence";
    public static final String ALARM_GEOFENCE_ENTER = "geofenceEnter";
    public static final String ALARM_GEOFENCE_EXIT = "geofenceExit";
    public static final String ALARM_GPS_ANTENNA_CUT = "gpsAntennaCut";
    public static final String ALARM_ACCIDENT = "accident";
    public static final String ALARM_TOW = "tow";
    public static final String ALARM_IDLE = "idle";
    public static final String ALARM_HIGH_RPM = "highRpm";
    public static final String ALARM_ACCELERATION = "hardAcceleration";
    public static final String ALARM_BRAKING = "hardBraking";
    public static final String ALARM_CORNERING = "hardCornering";
    public static final String ALARM_LANE_CHANGE = "laneChange";
    public static final String ALARM_FATIGUE_DRIVING = "fatigueDriving";
    public static final String ALARM_POWER_CUT = "powerCut";
    public static final String ALARM_POWER_RESTORED = "powerRestored";
    public static final String ALARM_JAMMING = "jamming";
    public static final String ALARM_TEMPERATURE = "temperature";
    public static final String ALARM_PARKING = "parking";
    public static final String ALARM_SHOCK = "shock";
    public static final String ALARM_BONNET = "bonnet";
    public static final String ALARM_FOOT_BRAKE = "footBrake";
    public static final String ALARM_FUEL_LEAK = "fuelLeak";
    public static final String ALARM_TAMPERING = "tampering";
    public static final String ALARM_REMOVING = "removing";
    public static final String ALARM_WATCH = "watchState";
    public static final String KEY_PACKET_TYPE = "packet_type";
    public static final String DEFAULT_PACKET_TYPE_SIMPLE = "simple";
    public static final String DEFAULT_PACKET_TYPE_DATA = "data";
    private String protocol;

    public Position() {
    }

    public Position(String protocol) {
        this.protocol = protocol;
        this.serverTime = new Date();
    }

    public String getUniqueId() {
        return ((Device) Context.getDeviceManager().getById(getDeviceId())).getUniqueId();
    }

    @Override
    public String toString() {
        return "Position{" +
                "protocol='" + protocol + '\'' +
                ", serverTime=" + serverTime +
                ", deviceTime=" + deviceTime +
                ", fixTime=" + fixTime +
                ", outdated=" + outdated +
                ", valid=" + valid +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", speed=" + speed +
                ", course=" + course +
                ", address='" + address + '\'' +
                ", accuracy=" + accuracy +
                ", network=" + network +
                ", packetType='" + packetType + '\'' +
                ", attributes" + this.getAttributes() + '\'' +
                '}';
    }


    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    private Date serverTime = new Date();
    private Date deviceTime;
    private Date fixTime;
    private boolean outdated;
    private boolean valid;
    private double latitude;
    private double longitude;

    public Date getServerTime() {
        return this.serverTime;
    }

    private double altitude;
    private double speed;
    private double course;
    private String address;
    private double accuracy;
    private Network network;
    private String packetType;

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }


    public Date getDeviceTime() {
        return this.deviceTime;
    }

    public void setDeviceTime(Date deviceTime) {
        this.deviceTime = deviceTime;
    }


    public Date getFixTime() {
        return this.fixTime;
    }

    public void setFixTime(Date fixTime) {
        this.fixTime = fixTime;
    }

    public void setTime(Date time) {
        setDeviceTime(time);
        setFixTime(time);
    }


    @QueryIgnore
    public boolean getOutdated() {
        return this.outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }


    public boolean getValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public double getAltitude() {
        return this.altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }


    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }


    public double getCourse() {
        return this.course;
    }

    public void setCourse(double course) {
        this.course = course;
    }


    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public double getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }


    public Network getNetwork() {
        return this.network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }


    public String getPacketType() {
        return this.packetType;
    }

    public void setPacketType(String packetType) {
        this.packetType = packetType;
    }


    @QueryIgnore
    public String getType() {
        return super.getType();
    }
}
