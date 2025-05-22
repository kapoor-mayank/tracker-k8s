package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.*;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CellCatProtocolDecoder extends BaseProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellCatProtocolDecoder.class);

    public static final int MSG_SESSION_START = 0x01;
    public static final int MSG_ACK = 0x02;
    public static final int MSG_DATA_ACK = 0x03;
    public static final int MSG_GPS = 0x10;
    public static final int MSG_ALARM = 0x11;
    public static final int MSG_STATUS = 0x12;
    public static final int MSG_CELL = 0x13;
    public static final int MSG_DATA_COMPLETE = 0x99;
    public static final int MSG_VHF_SETTINGS = 0xC0;
    public static final int MSG_ALARM_SETTINGS = 0xC1;
    public static final int MSG_CELL_SETTINGS = 0xC3;
    public static final int MSG_RELAY_SETTINGS = 0xC4;
    public static final int MSG_UNKNOWN_C5 = 0xC5;
    public static final int MSG_IR_CONFIRM = 0xC6;
    public static final int MSG_GEOFENCE_CONFIG = 0xC7;
    public static final int MSG_GEOFENCE_CLEAR = 0xC8;
    public static final int MSG_SESSION_END = 0xFF;

    public CellCatProtocolDecoder(Protocol protocol) {
        super(protocol);
        LOGGER.info("CellCatProtocolDecoder Initialized");
    }

    private boolean validateChecksum(ByteBuf buf) {
        int sum = 0;
        for (int i = 1; i <= 28; i++) {
            sum += buf.getUnsignedByte(i);
        }
        int checksum = ((sum ^ 0xFF) + 1) & 0xFF;
        return checksum == buf.getUnsignedByte(29);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        LOGGER.info("Inside CellCatProtocolDecode");

        if (buf.readableBytes() != 30 || buf.getUnsignedByte(0) != 0x55 || !validateChecksum(buf)) {
            LOGGER.warn("Invalid packet: {}", ByteBufUtil.hexDump(buf));
            return null;
        }

        int type = buf.getUnsignedByte(1);
        LOGGER.info("Command: {}", type);

        switch (type) {
            case MSG_SESSION_START:
                return decodeSessionStart(channel, remoteAddress, buf);
            case MSG_GPS:
                return decodeGps(channel, remoteAddress, buf);
            case MSG_ALARM:
                return decodeAlarm(channel, remoteAddress, buf);
            case MSG_STATUS:
                return decodeStatus(channel, remoteAddress, buf);
            case MSG_CELL:
                return decodeCellTower(channel, remoteAddress, buf);
            case MSG_DATA_COMPLETE:
                return decodeDataComplete(channel, remoteAddress, buf);
            case MSG_SESSION_END:
                LOGGER.info("Session end received from device.");
                if (channel != null) {
                    channel.close();
                }
                return null;
            case MSG_VHF_SETTINGS:
            case MSG_ALARM_SETTINGS:
            case MSG_CELL_SETTINGS:
            case MSG_RELAY_SETTINGS:
            case MSG_UNKNOWN_C5:
            case MSG_IR_CONFIRM:
                return decodeConfig(channel, remoteAddress, buf, type);
            case MSG_GEOFENCE_CONFIG:
                return decodeGeofence(channel, remoteAddress, buf);
            case MSG_GEOFENCE_CLEAR:
                return decodeGeofenceClear(channel, remoteAddress, buf);
            default:
                LOGGER.warn("Unknown command: {}", type);
                return null;
        }
    }

    private Object decodeSessionStart(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        buf.readerIndex(2);
        ByteBuf serialBytes = buf.readSlice(4);
        byte[] serial = new byte[4];
        serialBytes.getBytes(0, serial);
        ByteBuf rawDeviceIdBytes = buf.readSlice(15);
        String deviceId = rawDeviceIdBytes.toString(StandardCharsets.US_ASCII);

        LOGGER.info("Received registration ID in 0x01 command: {}", deviceId);
        getDeviceSession(channel, remoteAddress, deviceId);

        if (channel != null) {
            ByteBuf response = Unpooled.buffer(30);
            response.writeByte(0x55);
            response.writeByte(MSG_ACK);
            response.writeBytes(serial);
            response.writeByte(0x01); // result
            response.writeZero(20); // reserved
            response.writeByte(buf.getUnsignedByte(25)); // msg count
            response.writeByte(buf.getUnsignedByte(26)); // session type

            int sum = 0;
            for (int i = 1; i <= 28; i++) {
                sum += response.getUnsignedByte(i);
            }
            response.writeByte(((sum ^ 0xFF) + 1) & 0xFF);
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }

        return null;
    }

    private List<Position> decodeGps(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        buf.readerIndex(2);
        long time = buf.readUnsignedInt() * 1000L;
        position.setTime(new Date(time));

        position.setLongitude(buf.readInt() * 1e-7);
        position.setLatitude(buf.readInt() * 1e-7);
        position.setAltitude((double) buf.readShort());
        position.setSpeed(buf.readUnsignedShort() / 100.0); // convert cm/s to m/s if you want
        position.setCourse(buf.readUnsignedShort() * 0.1);

        int fix = buf.readUnsignedByte();
        position.setValid(fix >= 2);

        position.set("satellites", buf.readUnsignedByte());
        buf.skipBytes(1); // GPS jam detect (future)
        position.set("hdop", buf.readUnsignedByte() * 0.1);
        position.set("gpsTryCount", buf.readUnsignedByte());

        // If GPS fix is invalid, mark outdated and fallback to last known location
        if (!position.getValid()) {
            position.setOutdated(true);
            getLastLocation(position, position.getDeviceTime());
        } else {
            // GPS fix is valid, so fixTime = device time
            position.setFixTime(position.getDeviceTime());
        }

        LOGGER.info("Position object after decodeGps: {}", position);

        return Collections.singletonList(position);
    }


    private List<Position> decodeAlarm(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        buf.readerIndex(2);
        position.setTime(new Date(buf.readUnsignedInt() * 1000L));
        buf.skipBytes(5);
        position.set("relayStatus", buf.readUnsignedByte());
        position.set("bw1", buf.readUnsignedByte());
        position.set("bw2", buf.readUnsignedByte());
        position.set("light", buf.readUnsignedByte());
        buf.skipBytes(1);
        position.set("vehicleStopped", buf.readUnsignedByte());
        position.set("geofence1", buf.readUnsignedByte());
        position.set("geofence2", buf.readUnsignedByte());
        position.set("agnss", buf.readUnsignedByte());
        position.set("rtc", buf.readUnsignedInt() * 1000L);
        LOGGER.info("Position object after decodeAlarm: {}", position);
        return Collections.singletonList(position);
    }

    private List<Position> decodeStatus(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setFixTime(new Date());

        buf.readerIndex(6);
        position.set("packetNumber", buf.readUnsignedShort());
        position.set("batteryVoltage", buf.readUnsignedShort());
        position.set("batteryLevel", buf.readUnsignedByte());
        position.set("externalVoltage", buf.readUnsignedShort());
        position.set("temperature", buf.readShort() * 0.1);
        LOGGER.info("Skipping two bytes to read next");
        buf.skipBytes(2);
        position.set("rssi", (double) buf.readShort());
        position.set("aband", buf.readUnsignedByte());
        position.set("earfcn", buf.readUnsignedShort());
        position.set("retryCount", buf.readUnsignedByte());
        LOGGER.info("Position object after decodeStatus: {}", position);
        return Collections.singletonList(position);
    }

    private Object decodeCellTower(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        buf.readerIndex(2);
        position.set("cellId", buf.readUnsignedInt());
        position.set("tac", buf.readUnsignedInt());
        position.set("mcc", buf.readUnsignedInt());
        position.set("mnc", buf.readUnsignedInt());
        LOGGER.info("Position object after decodeCellTower: {}", position);
        return Collections.singletonList(position);
    }

    private Object decodeDataComplete(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        LOGGER.info("Data complete received. Echoing ACK (0x02)");

        if (channel != null) {
            ByteBuf response = Unpooled.buffer(30);
            response.writeByte(0x55);
            response.writeByte(MSG_ACK);
            response.writeZero(4);
            response.writeByte(0x01);
            response.writeZero(20);
            response.writeByte(buf.getUnsignedByte(22));
            response.writeByte(buf.getUnsignedByte(23));

            int sum = 0;
            for (int i = 1; i <= 28; i++) {
                sum += response.getUnsignedByte(i);
            }
            response.writeByte(((sum ^ 0xFF) + 1) & 0xFF);
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }

        return null;
    }

    private Object decodeConfig(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int type) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setFixTime(new Date());
        buf.readerIndex(2);
        for (int i = 2; i <= 28; i++) {
            position.set("cfg_" + type + "_" + i, buf.readUnsignedByte());
        }
        LOGGER.info("Position object after decodeConfig: {}", position);
        return Collections.singletonList(position);
    }

    private Object decodeGeofence(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        buf.readerIndex(2);
        position.set("geofenceNumber", buf.readUnsignedByte());
        position.set("enabled", buf.readUnsignedByte());
        position.set("shape", buf.readUnsignedByte());
        position.set("alarmOnEntry", buf.readUnsignedByte());
        position.set("alarmOnExit", buf.readUnsignedByte());
        position.set("lat1", buf.readInt());
        position.set("lon1", buf.readInt());
        position.set("lat2", buf.readInt());
        position.set("lon2", buf.readInt());
        position.set("radius", buf.readUnsignedInt());
        LOGGER.info("Position object after decodeGeofence: {}", position);
        return Collections.singletonList(position);
    }

    private Object decodeGeofenceClear(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) return null;
        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.set("geofenceCleared", true);
        LOGGER.info("Position object after decodeGeofenceClear: {}", position);
        return Collections.singletonList(position);
    }
}
