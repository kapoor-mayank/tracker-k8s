package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.NetworkMessage;
import org.traccar.Protocol;
import org.traccar.helper.BitUtil;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;

public class ObdDongleProtocolDecoder extends BaseProtocolDecoder {
    public static final int MSG_TYPE_CONNECT = 1;

    public static final int MSG_TYPE_CONNACK = 2;

    public static final int MSG_TYPE_PUBLISH = 3;

    public static final int MSG_TYPE_PUBACK = 4;

    public static final int MSG_TYPE_PINGREQ = 12;

    public static final int MSG_TYPE_PINGRESP = 13;

    public static final int MSG_TYPE_DISCONNECT = 14;

    public ObdDongleProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static void sendResponse(Channel channel, int type, int index, String imei, ByteBuf content) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeShort(21845);
            response.writeShort(index);
            response.writeBytes(imei.getBytes(StandardCharsets.US_ASCII));
            response.writeByte(type);
            response.writeShort(content.readableBytes());
            response.writeBytes(content);
            content.release();
            response.writeByte(0);
            response.writeShort(43690);
            channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
        }
    }

    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf)msg;
        buf.skipBytes(2);
        int index = buf.readUnsignedShort();
        String imei = buf.readSlice(15).toString(StandardCharsets.US_ASCII);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
        if (deviceSession == null)
            return null;
        int type = buf.readUnsignedByte();
        buf.readUnsignedShort();
        if (type == 1) {
            ByteBuf response = Unpooled.buffer();
            response.writeByte(1);
            response.writeShort(0);
            response.writeInt(0);
            sendResponse(channel, 2, index, imei, response);
        } else if (type == 3) {
            int typeMajor = buf.readUnsignedByte();
            int typeMinor = buf.readUnsignedByte();
            buf.readUnsignedByte();
            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            position.setTime(new Date(buf.readUnsignedInt() * 1000L));
            int flags = buf.readUnsignedByte();
            position.setValid(!BitUtil.check(flags, 6));
            position.set("sat", Integer.valueOf(BitUtil.to(flags, 4)));
            double longitude = ((BitUtil.to(buf.readUnsignedShort(), 1) << 24) + buf.readUnsignedMedium()) * 1.0E-5D;
            position.setLongitude(BitUtil.check(flags, 5) ? longitude : -longitude);
            double latitude = buf.readUnsignedMedium() * 1.0E-5D;
            position.setLatitude(BitUtil.check(flags, 4) ? latitude : -latitude);
            int speedCourse = buf.readUnsignedMedium();
            position.setSpeed(UnitsConverter.knotsFromMph(BitUtil.from(speedCourse, 10) * 0.1D));
            position.setCourse(BitUtil.to(speedCourse, 10));
            ByteBuf response = Unpooled.buffer();
            response.writeByte(typeMajor);
            response.writeByte(typeMinor);
            sendResponse(channel, 4, index, imei, response);
            return position;
        }
        return null;
    }
}
