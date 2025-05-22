/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AutoTrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN_REQUEST = 51;
/*     */   public static final int MSG_LOGIN_CONFIRM = 101;
/*     */   public static final int MSG_TELEMETRY_1 = 52;
/*     */   public static final int MSG_TELEMETRY_2 = 66;
/*     */   public static final int MSG_TELEMETRY_3 = 67;
/*     */   public static final int MSG_KEEP_ALIVE = 114;
/*     */   public static final int MSG_TELEMETRY_CONFIRM = 123;
/*     */   
/*     */   public AutoTrackProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeTelemetry(Channel channel, SocketAddress remoteAddress, DeviceSession deviceSession, ByteBuf buf) {
/*  50 */     Position position = new Position(getProtocolName());
/*  51 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  53 */     position.setTime(new Date(1009843200000L + buf.readUnsignedIntLE() * 1000L));
/*  54 */     position.setLatitude(buf.readIntLE() * 1.0E-7D);
/*  55 */     position.setLongitude(buf.readIntLE() * 1.0E-7D);
/*     */     
/*  57 */     position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*  58 */     position.set("fuelUsed", Long.valueOf(buf.readUnsignedIntLE()));
/*     */     
/*  60 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));
/*  61 */     buf.readUnsignedShortLE();
/*     */     
/*  63 */     position.set("input", Integer.valueOf(buf.readUnsignedShortLE()));
/*  64 */     buf.readUnsignedIntLE();
/*  65 */     buf.readUnsignedIntLE();
/*     */     
/*  67 */     for (int i = 0; i < 5; i++) {
/*  68 */       position.set("adc" + (i + 1), Integer.valueOf(buf.readUnsignedShortLE()));
/*     */     }
/*     */     
/*  71 */     position.setCourse(buf.readUnsignedShortLE());
/*     */     
/*  73 */     position.set("status", Integer.valueOf(buf.readUnsignedShortLE()));
/*  74 */     position.set("event", Integer.valueOf(buf.readUnsignedShortLE()));
/*  75 */     position.set("driverUniqueId", Long.valueOf(buf.readLongLE()));
/*     */     
/*  77 */     int index = buf.readUnsignedShortLE();
/*     */     
/*  79 */     buf.readUnsignedShortLE();
/*     */     
/*  81 */     if (channel != null) {
/*  82 */       ByteBuf response = Unpooled.buffer();
/*  83 */       response.writeInt(-235802127);
/*  84 */       response.writeByte(123);
/*  85 */       response.writeShortLE(2);
/*  86 */       response.writeShortLE(index);
/*  87 */       response.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, response.nioBuffer()));
/*  88 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/*  91 */     return position;
/*     */   }
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     String imei;
/*     */     DeviceSession deviceSession;
/*     */     int fuelConst, tripConst;
/*  98 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 100 */     buf.skipBytes(4);
/* 101 */     int type = buf.readUnsignedByte();
/* 102 */     buf.readUnsignedShortLE();
/*     */     
/* 104 */     switch (type) {
/*     */       case 51:
/* 106 */         imei = ByteBufUtil.hexDump(buf.readBytes(8));
/* 107 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 108 */         if (deviceSession == null) {
/* 109 */           return null;
/*     */         }
/* 111 */         fuelConst = buf.readUnsignedShortLE();
/* 112 */         tripConst = buf.readUnsignedShortLE();
/* 113 */         if (channel != null) {
/* 114 */           ByteBuf response = Unpooled.buffer();
/* 115 */           response.writeInt(-235802127);
/* 116 */           response.writeByte(101);
/* 117 */           response.writeShortLE(12);
/* 118 */           response.writeBytes(ByteBufUtil.decodeHexDump(imei));
/* 119 */           response.writeShortLE(fuelConst);
/* 120 */           response.writeShortLE(tripConst);
/* 121 */           response.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, response.nioBuffer()));
/* 122 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         } 
/* 124 */         return null;
/*     */       case 52:
/*     */       case 66:
/*     */       case 67:
/* 128 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 129 */         if (deviceSession == null) {
/* 130 */           return null;
/*     */         }
/* 132 */         return decodeTelemetry(channel, remoteAddress, deviceSession, buf);
/*     */     } 
/* 134 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AutoTrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */