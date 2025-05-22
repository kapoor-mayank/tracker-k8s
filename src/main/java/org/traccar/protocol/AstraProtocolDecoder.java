package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.NetworkMessage;
import org.traccar.Protocol;
import org.traccar.helper.BitUtil;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;

public class AstraProtocolDecoder extends BaseProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AstraProtocolDecoder.class);

    public AstraProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_HEARTBEAT = 26;
    public static final int MSG_DATA = 16;

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        if (channel != null) {
            channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(new byte[]{6}), remoteAddress));
        }

        buf.readUnsignedByte();
        buf.readUnsignedShort();

        String imei = String.format("%08d", buf.readUnsignedInt()) + String.format("%07d", buf.readUnsignedMedium());
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }

        List<Position> positions = new LinkedList<>();

        while (buf.readableBytes() > 2) {

            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            buf.readUnsignedByte();

            position.setValid(true);
            position.setLatitude(buf.readInt() * 1.0E-6D);
            position.setLongitude(buf.readInt() * 1.0E-6D);

            DateBuilder dateBuilder = new DateBuilder().setDate(1980, 1, 6).addMillis(buf.readUnsignedInt() * 1000L);
            position.setTime(dateBuilder.getDate());

            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte() * 2));
            position.setCourse(buf.readUnsignedByte() * 2);

            int reason = buf.readUnsignedMedium();
            position.set("binaryEvent", String.format("%24s", Integer.toBinaryString(reason)).replace(' ', '0'));

            int status = buf.readUnsignedShort();
            position.set("binaryStatus", String.format("%16s", Integer.toBinaryString(status)).replace(' ', '0'));

            position.set("io1", buf.readUnsignedByte());
            position.set("adc1", buf.readUnsignedByte());
            position.set("battery", buf.readUnsignedByte());
            position.set("power", buf.readUnsignedByte());

            buf.readUnsignedByte();
            buf.skipBytes(6);
            position.set("tripOdometer", buf.readUnsignedShort());
            buf.readUnsignedShort();

            position.setAltitude(buf.readUnsignedByte() * 20);

            int quality = buf.readUnsignedByte();
            position.set("sat", quality & 0xF);
            position.set("rssi", quality >> 4);

            buf.readUnsignedByte();

            if (BitUtil.check(status, 8)) {
                position.set("driverUniqueId", buf.readSlice(7).toString(StandardCharsets.US_ASCII));
                position.set("odometer", buf.readUnsignedMedium() * 1000);
                position.set("hours", UnitsConverter.msFromHours(buf.readUnsignedShort()));
            }

            if (BitUtil.check(status, 6)) {
                LOGGER.warn("Extension data is not supported");
                return position;
            }

            positions.add(position);
        }

        return positions;
    }
}
