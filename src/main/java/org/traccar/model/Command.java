package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.traccar.database.QueryIgnore;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Command
        extends Message
        implements Cloneable {
    public static final String TYPE_CUSTOM = "custom";
    public static final String TYPE_IDENTIFICATION = "deviceIdentification";
    public static final String TYPE_POSITION_SINGLE = "positionSingle";
    public static final String TYPE_POSITION_PERIODIC = "positionPeriodic";
    public static final String TYPE_POSITION_LOG = "positionLog";
    public static final String TYPE_POSITION_STOP = "positionStop";
    public static final String TYPE_ENGINE_STOP = "engineStop";
    public static final String TYPE_ENGINE_RESUME = "engineResume";
    public static final String TYPE_ENGINE_STOP_CUSTOM = "customEngineStop";
    public static final String TYPE_ENGINE_RESUME_CUSTOM = "customEngineResume";
    public static final String TYPE_ALARM_ARM = "alarmArm";
    public static final String TYPE_ALARM_DISARM = "alarmDisarm";
    public static final String TYPE_SET_TIMEZONE = "setTimezone";
    public static final String TYPE_REQUEST_PHOTO = "requestPhoto";
    public static final String TYPE_POWER_OFF = "powerOff";
    public static final String TYPE_REBOOT_DEVICE = "rebootDevice";
    public static final String TYPE_FACTORY_RESET = "factoryReset";
    public static final String TYPE_SEND_SMS = "sendSms";
    public static final String TYPE_SEND_USSD = "sendUssd";
    public static final String TYPE_SOS_NUMBER = "sosNumber";
    public static final String TYPE_SILENCE_TIME = "silenceTime";
    public static final String TYPE_SET_PHONEBOOK = "setPhonebook";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_VOICE_MESSAGE = "voiceMessage";
    public static final String TYPE_OUTPUT_CONTROL = "outputControl";
    public static final String TYPE_VOICE_MONITORING = "voiceMonitoring";
    public static final String TYPE_SET_AGPS = "setAgps";
    public static final String TYPE_SET_INDICATOR = "setIndicator";
    public static final String TYPE_CONFIGURATION = "configuration";
    public static final String TYPE_GET_VERSION = "getVersion";
    public static final String TYPE_FIRMWARE_UPDATE = "firmwareUpdate";
    public static final String TYPE_SET_CONNECTION = "setConnection";
    public static final String TYPE_SET_ODOMETER = "setOdometer";
    public static final String TYPE_GET_MODEM_STATUS = "getModemStatus";
    public static final String TYPE_GET_DEVICE_STATUS = "getDeviceStatus";
    public static final String TYPE_SET_SPEED_LIMIT = "setSpeedLimit";
    public static final String TYPE_SERIAL = "serial";
    public static final String TYPE_MODE_POWER_SAVING = "modePowerSaving";
    public static final String TYPE_MODE_DEEP_SLEEP = "modeDeepSleep";
    public static final String TYPE_ALARM_GEOFENCE = "movementAlarm";
    public static final String TYPE_ALARM_BATTERY = "alarmBattery";
    public static final String TYPE_ALARM_SOS = "alarmSos";
    public static final String TYPE_ALARM_REMOVE = "alarmRemove";
    public static final String TYPE_ALARM_CLOCK = "alarmClock";
    public static final String TYPE_ALARM_SPEED = "alarmSpeed";
    public static final String TYPE_ALARM_FALL = "alarmFall";
    public static final String TYPE_ALARM_VIBRATION = "alarmVibration";
    public static final String KEY_UNIQUE_ID = "uniqueId";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_TIMEZONE = "timezone";
    public static final String KEY_DEVICE_PASSWORD = "devicePassword";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ENABLE = "enable";
    public static final String KEY_DATA = "data";
    public static final String KEY_INDEX = "index";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_SERVER = "server";
    public static final String KEY_PORT = "port";
    private String uniqueId;
    private boolean textChannel;
    private String description;

    public Command clone() throws CloneNotSupportedException {
        return (Command) super.clone();
    }


    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    public boolean getTextChannel() {
        return this.textChannel;
    }

    public void setTextChannel(boolean textChannel) {
        this.textChannel = textChannel;
    }


    @QueryIgnore
    public long getDeviceId() {
        return super.getDeviceId();
    }


    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Command.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */