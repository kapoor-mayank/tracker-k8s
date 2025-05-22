package org.traccar;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.database.ConnectionManager;
import org.traccar.database.IdentityManager;
import org.traccar.database.StatisticsManager;
import org.traccar.handler.ForwarderHandler;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Device;
import org.traccar.model.Position;

public abstract class BaseProtocolDecoder extends ExtendedObjectDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProtocolDecoder.class);

    private static final String PROTOCOL_UNKNOWN = "unknown";

    private final Config config = Context.getConfig();

    private final IdentityManager identityManager = Context.getIdentityManager();

    private final ConnectionManager connectionManager = Context.getConnectionManager();

    private final StatisticsManager statisticsManager;

    private final Protocol protocol;

    private String modelOverride;

    private DeviceSession channelDeviceSession;

    private Map<SocketAddress, DeviceSession> addressDeviceSessions;

    public String getProtocolName() {
        return (this.protocol != null) ? this.protocol.getName() : "unknown";
    }

    public String getServer(Channel channel, char delimiter) {
        String server = this.config.getString(getProtocolName() + ".server");
        if (server == null && channel != null) {
            InetSocketAddress address = (InetSocketAddress)channel.localAddress();
            server = address.getAddress().getHostAddress() + ":" + address.getPort();
        }
        return (server != null) ? server.replace(':', delimiter) : null;
    }

    protected double convertSpeed(double value, String defaultUnits) {
        switch (this.config.getString(getProtocolName() + ".speed", defaultUnits)) {
            case "kmh":
                return UnitsConverter.knotsFromKph(value);
            case "mps":
                return UnitsConverter.knotsFromMps(value);
            case "mph":
                return UnitsConverter.knotsFromMph(value);
        }
        return value;
    }

    protected TimeZone getTimeZone(long deviceId) {
        return getTimeZone(deviceId, "UTC");
    }

    protected TimeZone getTimeZone(long deviceId, String defaultTimeZone) {
        TimeZone result = TimeZone.getTimeZone(defaultTimeZone);
        String timeZoneName = this.identityManager.lookupAttributeString(deviceId, "decoder.timezone", null, true);
        if (timeZoneName != null) {
            result = TimeZone.getTimeZone(timeZoneName);
        } else {
            int timeZoneOffset = this.config.getInteger(getProtocolName() + ".timezone", 0);
            if (timeZoneOffset != 0) {
                result.setRawOffset(timeZoneOffset * 1000);
                LOGGER.warn("Config parameter " + getProtocolName() + ".timezone is deprecated");
            }
        }
        return result;
    }

    public BaseProtocolDecoder(Protocol protocol) {
        this.addressDeviceSessions = new HashMap<>();
        this.protocol = protocol;
        this.statisticsManager = (Main.getInjector() != null) ? (StatisticsManager)Main.getInjector().getInstance(StatisticsManager.class) : null;
    }

    private long findDeviceId(SocketAddress remoteAddress, String... uniqueIds) {
        if (uniqueIds.length > 0) {
            long deviceId = 0L;
            Device device = null;
            try {
                for (String uniqueId : uniqueIds) {
                    if (uniqueId != null) {
                        device = this.identityManager.getByUniqueId(uniqueId);
                        if (device != null) {
                            deviceId = device.getId();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Find device error", e);
            }
            if (deviceId == 0L && this.config.getBoolean("database.registerUnknown"))
                return this.identityManager.addUnknownDevice(uniqueIds[0]);
            if ((device != null && !device.getDisabled()) || this.config.getBoolean("database.storeDisabled"))
                return deviceId;
            StringBuilder message = new StringBuilder();
            if (deviceId == 0L) {
                message.append("Unknown device -");
            } else {
                message.append("Disabled device -");
            }
            for (String uniqueId : uniqueIds)
                message.append(" ").append(uniqueId);
            if (remoteAddress != null)
                message.append(" (").append(((InetSocketAddress)remoteAddress).getHostString()).append(")");
            LOGGER.warn(message.toString());
        }
        return 0L;
    }

    public DeviceSession getDeviceSession(Channel channel, SocketAddress remoteAddress, String... uniqueIds) {
//        LOGGER.info("getDeviceSession: {}", channel.getClass().getSimpleName());
        return getDeviceSession(channel, remoteAddress, false, uniqueIds);
    }

    public DeviceSession getDeviceSession(Channel channel, SocketAddress remoteAddress, boolean ignoreCache, String... uniqueIds) {
        if ((channel != null && BasePipelineFactory.getHandler(channel.pipeline(), HttpRequestDecoder.class) != null) || ignoreCache || this.config
                .getBoolean(getProtocolName() + ".ignoreSessionCache")) {
            long deviceId = findDeviceId(remoteAddress, uniqueIds);
            if (deviceId != 0L) {
                if (channel != null && uniqueIds.length > 0)
                    ((ForwarderHandler)channel.pipeline().get(ForwarderHandler.class))
                            .identify(uniqueIds[0], remoteAddress);
                if (this.connectionManager != null)
                    this.connectionManager.addActiveDevice(deviceId, this.protocol, channel, remoteAddress);
                return new DeviceSession(deviceId);
            }
            return null;
        }
        if (channel instanceof io.netty.channel.socket.DatagramChannel) {
            long deviceId = findDeviceId(remoteAddress, uniqueIds);
            DeviceSession deviceSession = this.addressDeviceSessions.get(remoteAddress);
            if (deviceSession != null && (deviceSession.getDeviceId() == deviceId || uniqueIds.length == 0))
                return deviceSession;
            if (deviceId != 0L) {
                deviceSession = new DeviceSession(deviceId);
                this.addressDeviceSessions.put(remoteAddress, deviceSession);
                if (channel != null && uniqueIds.length > 0)
                    ((ForwarderHandler)channel.pipeline().get(ForwarderHandler.class))
                            .identify(uniqueIds[0], remoteAddress);
                if (this.connectionManager != null)
                    this.connectionManager.addActiveDevice(deviceId, this.protocol, channel, remoteAddress);
                return deviceSession;
            }
            return null;
        }
        if (this.channelDeviceSession == null) {
            long deviceId = findDeviceId(remoteAddress, uniqueIds);
            if (deviceId != 0L) {
                this.channelDeviceSession = new DeviceSession(deviceId);
                if (channel != null && uniqueIds.length > 0)
                    ((ForwarderHandler)channel.pipeline().get(ForwarderHandler.class))
                            .identify(uniqueIds[0], remoteAddress);
                if (this.connectionManager != null)
                    this.connectionManager.addActiveDevice(deviceId, this.protocol, channel, remoteAddress);
            }
        }
        return this.channelDeviceSession;
    }

    public void setModelOverride(String modelOverride) {
        this.modelOverride = modelOverride;
    }

    public String getDeviceModel(DeviceSession deviceSession) {
        String model;
        if (Context.getDeviceManager() != null) {
            model = ((Device)Context.getDeviceManager().getById(deviceSession.getDeviceId())).getModel();
        } else {
            model = null;
        }
        return (this.modelOverride != null) ? this.modelOverride : model;
    }

    public void getLastLocation(Position position, Date deviceTime) {
        if (position.getDeviceId() != 0L) {
            position.setOutdated(true);
            Position last = this.identityManager.getLastPosition(position.getDeviceId());
            if (last != null) {
                position.setFixTime(last.getFixTime());
                position.setValid(last.getValid());
                position.setLatitude(last.getLatitude());
                position.setLongitude(last.getLongitude());
                position.setAltitude(last.getAltitude());
                position.setSpeed(last.getSpeed());
                position.setCourse(last.getCourse());
                position.setAccuracy(last.getAccuracy());
            } else {
                position.setFixTime(new Date(0L));
            }
            position.setDeviceTime(deviceTime);
        }
    }

    protected void onMessageEvent(Channel channel, SocketAddress remoteAddress, Object originalMessage, Object decodedMessage) {
        if (this.statisticsManager != null)
            this.statisticsManager.registerMessageReceived();
        Position position = null;
        if (decodedMessage != null)
            if (decodedMessage instanceof Position) {
                position = (Position)decodedMessage;
            } else if (decodedMessage instanceof Collection) {
                Collection<Position> positions = (Collection)decodedMessage;
                if (!positions.isEmpty())
                    position = positions.iterator().next();
            }
        if (position != null) {
            this.connectionManager.updateDevice(position
                    .getDeviceId(), "online", new Date());
        } else {
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
            if (deviceSession != null)
                this.connectionManager.updateDevice(deviceSession
                        .getDeviceId(), "online", new Date());
        }
    }

    protected Object handleEmptyMessage(Channel channel, SocketAddress remoteAddress, Object msg) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
        if (this.config.getBoolean("database.saveEmpty") && deviceSession != null) {
            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            getLastLocation(position, null);
            return position;
        }
        return null;
    }
}
