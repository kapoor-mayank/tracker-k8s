///*     */ package org.traccar.protocol;
///*     */
///*     */ import com.google.protobuf.InvalidProtocolBufferException;
///*     */ import io.netty.buffer.ByteBuf;
///*     */ import io.netty.buffer.ByteBufUtil;
///*     */ import io.netty.buffer.Unpooled;
///*     */ import io.netty.channel.Channel;
///*     */ import java.net.SocketAddress;
///*     */ import java.util.Date;
///*     */ import java.util.LinkedList;
///*     */ import java.util.List;
///*     */ import org.traccar.BaseProtocolDecoder;
///*     */ import org.traccar.DeviceSession;
///*     */ import org.traccar.NetworkMessage;
///*     */ import org.traccar.Protocol;
///*     */ import org.traccar.helper.BitUtil;
///*     */ import org.traccar.helper.Checksum;
///*     */ import org.traccar.helper.UnitsConverter;
///*     */ import org.traccar.model.Position;
///*     */ import org.traccar.protobuf.omnicomm.OmnicommMessageOuterClass;
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */ public class OmnicommProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*     */   public static final int MSG_IDENTIFICATION = 128;
///*     */   public static final int MSG_ARCHIVE_INQUIRY = 133;
///*     */   public static final int MSG_ARCHIVE_DATA = 134;
///*     */   public static final int MSG_REMOVE_ARCHIVE_INQUIRY = 135;
///*     */
///*     */   public OmnicommProtocolDecoder(Protocol protocol) {
///*  41 */     super(protocol);
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */   private OmnicommMessageOuterClass.OmnicommMessage parseProto(ByteBuf buf, int length) throws InvalidProtocolBufferException {
///*     */     byte[] array;
///*     */     int offset;
///*  54 */     if (buf.hasArray()) {
///*  55 */       array = buf.array();
///*  56 */       offset = buf.arrayOffset() + buf.readerIndex();
///*     */     } else {
///*  58 */       array = ByteBufUtil.getBytes(buf, buf.readerIndex(), length, false);
///*  59 */       offset = 0;
///*     */     }
///*  61 */     buf.skipBytes(length);
///*     */
///*  63 */     return
///*  64 */       (OmnicommMessageOuterClass.OmnicommMessage)OmnicommMessageOuterClass.OmnicommMessage.getDefaultInstance().getParserForType().parseFrom(array, offset, length);
///*     */   }
///*     */
///*     */   private void sendResponse(Channel channel, int type, long index) {
///*  68 */     if (channel != null) {
///*  69 */       ByteBuf response = Unpooled.buffer();
///*  70 */       response.writeByte(192);
///*  71 */       response.writeByte(type);
///*  72 */       response.writeShortLE(4);
///*  73 */       response.writeIntLE((int)index);
///*  74 */       response.writeShortLE(Checksum.crc16(Checksum.CRC16_CCITT_FALSE, response
///*  75 */             .nioBuffer(1, response.writerIndex() - 1)));
///*  76 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
///*     */     }
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///*  84 */     ByteBuf buf = (ByteBuf)msg;
///*     */
///*  86 */     buf.readUnsignedByte();
///*  87 */     int type = buf.readUnsignedByte();
///*  88 */     buf.readUnsignedShortLE();
///*     */
///*  90 */     if (type == 128) {
///*     */
///*  92 */       getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(buf.readUnsignedIntLE()) });
///*  93 */       sendResponse(channel, 133, 0L);
///*     */     }
///*  95 */     else if (type == 134) {
///*     */
///*  97 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
///*  98 */       if (deviceSession == null) {
///*  99 */         return null;
///*     */       }
///*     */
///* 102 */       long index = buf.readUnsignedIntLE();
///* 103 */       buf.readUnsignedIntLE();
///* 104 */       buf.readUnsignedByte();
///*     */
///* 106 */       List<Position> positions = new LinkedList<>();
///*     */
///* 108 */       while (buf.readableBytes() > 2) {
///*     */
///* 110 */         OmnicommMessageOuterClass.OmnicommMessage message = parseProto(buf, buf.readUnsignedShortLE());
///*     */
///* 112 */         Position position = new Position(getProtocolName());
///* 113 */         position.setDeviceId(deviceSession.getDeviceId());
///*     */
///* 115 */         if (message.hasGeneral()) {
///* 116 */           OmnicommMessageOuterClass.OmnicommMessage.General data = message.getGeneral();
///* 117 */           position.set("power", Double.valueOf(data.getUboard() * 0.1D));
///* 118 */           position.set("batteryLevel", Integer.valueOf(data.getBatLife()));
///* 119 */           position.set("ignition", Boolean.valueOf(BitUtil.check(data.getFLG(), 0)));
///* 120 */           position.set("rpm", Integer.valueOf(data.getTImp()));
///*     */         }
///*     */
///* 123 */         if (message.hasNAV()) {
///* 124 */           OmnicommMessageOuterClass.OmnicommMessage.NAV data = message.getNAV();
///* 125 */           position.setValid(true);
///* 126 */           position.setTime(new Date((data.getGPSTime() + 1230768000) * 1000L));
///* 127 */           position.setLatitude(data.getLAT() * 1.0E-7D);
///* 128 */           position.setLongitude(data.getLON() * 1.0E-7D);
///* 129 */           position.setSpeed(UnitsConverter.knotsFromKph(data.getGPSVel() * 0.1D));
///* 130 */           position.setCourse(data.getGPSDir());
///* 131 */           position.setAltitude(data.getGPSAlt() * 0.1D);
///* 132 */           position.set("sat", Integer.valueOf(data.getGPSNSat()));
///*     */         }
///*     */
///* 135 */         if (message.hasLLSDt()) {
///* 136 */           OmnicommMessageOuterClass.OmnicommMessage.LLSDt data = message.getLLSDt();
///* 137 */           position.set("fuel1Temp", Integer.valueOf(data.getTLLS1()));
///* 138 */           position.set("fuel1", Integer.valueOf(data.getCLLS1()));
///* 139 */           position.set("fuel1State", Integer.valueOf(data.getFLLS1()));
///*     */         }
///*     */
///* 142 */         if (position.getFixTime() != null) {
///* 143 */           positions.add(position);
///*     */         }
///*     */       }
///*     */
///* 147 */       if (positions.isEmpty()) {
///* 148 */         sendResponse(channel, 135, index + 1L);
///* 149 */         return null;
///*     */       }
///* 151 */       return positions;
///*     */     }
///*     */
///*     */
///* 155 */     return null;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OmnicommProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */