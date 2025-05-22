//package org.traccar.protocol;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.Channel;
//import java.net.SocketAddress;
//import java.util.Date;
//import org.traccar.BaseProtocolDecoder;
//import org.traccar.DeviceSession;
//import org.traccar.NetworkMessage;
//import org.traccar.Protocol;
//import org.traccar.helper.Checksum;
//import org.traccar.helper.UnitsConverter;
//import org.traccar.model.Position;
//
//public class NavigilProtocolDecoder extends BaseProtocolDecoder {
//    private static final int LEAP_SECONDS_DELTA = 25;
//    public static final int MSG_ERROR = 2;
//    public static final int MSG_INDICATION = 4;
//    public static final int MSG_CONN_OPEN = 5;
//    public static final int MSG_CONN_CLOSE = 6;
//    public static final int MSG_SYSTEM_REPORT = 7;
//    public static final int MSG_UNIT_REPORT = 8;
//    public static final int MSG_GEOFENCE_ALARM = 10;
//    public static final int MSG_INPUT_ALARM = 11;
//    public static final int MSG_TG2_REPORT = 12;
//    public static final int MSG_POSITION_REPORT = 13;
//    public static final int MSG_POSITION_REPORT_2 = 15;
//    public static final int MSG_SNAPSHOT4 = 17;
//    public static final int MSG_TRACKING_DATA = 18;
//    public static final int MSG_MOTION_ALARM = 19;
//    public static final int MSG_ACKNOWLEDGEMENT = 255;
//    private int senderSequenceNumber;
//
//    public NavigilProtocolDecoder(Protocol protocol) {
//        super(protocol);
//        this.senderSequenceNumber = 1;
//    }
//
//    private void sendAcknowledgment(Channel channel, int sequenceNumber) {
//        ByteBuf data = Unpooled.buffer(4);
//        data.writeShortLE(sequenceNumber);
//        data.writeShortLE(0);
//
//        ByteBuf header = Unpooled.buffer(20);
//        header.writeByte(1);
//        header.writeByte(0);
//        header.writeShortLE(this.senderSequenceNumber++);
//        header.writeShortLE(MSG_ACKNOWLEDGEMENT);
//        header.writeShortLE(header.capacity() + data.capacity());
//        header.writeShortLE(0);
//        header.writeShortLE(Checksum.crc16(Checksum.CRC16_CCITT_FALSE, data.nioBuffer()));
//        header.writeIntLE(0);
//        header.writeIntLE((int) (System.currentTimeMillis() / 1000L) + LEAP_SECONDS_DELTA);
//
//        if (channel != null) {
//            channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(header
