/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class MxtProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_ACK = 2;
/*     */   public static final int MSG_NACK = 3;
/*     */   public static final int MSG_POSITION = 49;
/*     */   
/*     */   public MxtProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void sendResponse(Channel channel, int device, long id, int crc) {
/*  44 */     if (channel != null) {
/*  45 */       ByteBuf response = Unpooled.buffer();
/*  46 */       response.writeByte(device);
/*  47 */       response.writeByte(2);
/*  48 */       response.writeIntLE((int)id);
/*  49 */       response.writeShortLE(crc);
/*  50 */       response.writeShortLE(Checksum.crc16(Checksum.CRC16_XMODEM, response
/*  51 */             .nioBuffer()));
/*     */       
/*  53 */       ByteBuf encoded = Unpooled.buffer();
/*  54 */       encoded.writeByte(1);
/*  55 */       while (response.isReadable()) {
/*  56 */         int b = response.readByte();
/*  57 */         if (b == 1 || b == 4 || b == 16 || b == 17 || b == 19) {
/*  58 */           encoded.writeByte(16);
/*  59 */           b += 32;
/*     */         } 
/*  61 */         encoded.writeByte(b);
/*     */       } 
/*  63 */       response.release();
/*  64 */       encoded.writeByte(4);
/*  65 */       channel.writeAndFlush(new NetworkMessage(encoded, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  73 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  75 */     buf.readUnsignedByte();
/*  76 */     int device = buf.readUnsignedByte();
/*  77 */     int type = buf.readUnsignedByte();
/*     */     
/*  79 */     long id = buf.readUnsignedIntLE();
/*  80 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(id) });
/*  81 */     if (deviceSession == null) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     if (type == 49) {
/*     */       
/*  87 */       Position position = new Position(getProtocolName());
/*  88 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  90 */       buf.readUnsignedByte();
/*  91 */       int infoGroups = buf.readUnsignedByte();
/*     */       
/*  93 */       position.set("index", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/*  95 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(2000, 1, 1);
/*     */       
/*  97 */       long date = buf.readUnsignedIntLE();
/*     */       
/*  99 */       long days = BitUtil.from(date, 17);
/* 100 */       if (days < 5460L) {
/* 101 */         days += 7168L;
/*     */       }
/*     */       
/* 104 */       long hours = BitUtil.between(date, 12, 17);
/* 105 */       long minutes = BitUtil.between(date, 6, 12);
/* 106 */       long seconds = BitUtil.to(date, 6);
/*     */       
/* 108 */       dateBuilder.addMillis((((days * 24L + hours) * 60L + minutes) * 60L + seconds) * 1000L);
/*     */       
/* 110 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 112 */       position.setValid(true);
/* 113 */       position.setLatitude(buf.readIntLE() / 1000000.0D);
/* 114 */       position.setLongitude(buf.readIntLE() / 1000000.0D);
/*     */       
/* 116 */       long flags = buf.readUnsignedIntLE();
/* 117 */       position.set("ignition", Boolean.valueOf(BitUtil.check(flags, 0)));
/* 118 */       if (BitUtil.check(flags, 1)) {
/* 119 */         position.set("alarm", "general");
/*     */       }
/* 121 */       position.set("input", Long.valueOf(BitUtil.between(flags, 2, 7)));
/* 122 */       position.set("output", Long.valueOf(BitUtil.between(flags, 7, 10)));
/* 123 */       position.setCourse((BitUtil.between(flags, 10, 13) * 45L));
/*     */       
/* 125 */       position.set("charge", Boolean.valueOf(BitUtil.check(flags, 20)));
/*     */       
/* 127 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */       
/* 129 */       buf.readUnsignedByte();
/*     */       
/* 131 */       if (BitUtil.check(infoGroups, 0)) {
/* 132 */         buf.skipBytes(8);
/*     */       }
/*     */       
/* 135 */       if (BitUtil.check(infoGroups, 1)) {
/* 136 */         buf.skipBytes(8);
/*     */       }
/*     */       
/* 139 */       if (BitUtil.check(infoGroups, 2)) {
/* 140 */         position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 141 */         position.set("hdop", Short.valueOf(buf.readUnsignedByte()));
/* 142 */         position.setAccuracy(buf.readUnsignedByte());
/* 143 */         position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 144 */         buf.readUnsignedShortLE();
/* 145 */         position.set("power", Short.valueOf(buf.readUnsignedByte()));
/* 146 */         position.set("temp1", Byte.valueOf(buf.readByte()));
/*     */       } 
/*     */       
/* 149 */       if (BitUtil.check(infoGroups, 3)) {
/* 150 */         position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */       }
/*     */       
/* 153 */       if (BitUtil.check(infoGroups, 4)) {
/* 154 */         position.set("hours", Long.valueOf(UnitsConverter.msFromMinutes(buf.readUnsignedIntLE())));
/*     */       }
/*     */       
/* 157 */       if (BitUtil.check(infoGroups, 5)) {
/* 158 */         buf.readUnsignedIntLE();
/*     */       }
/*     */       
/* 161 */       if (BitUtil.check(infoGroups, 6)) {
/* 162 */         position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/* 163 */         position.set("battery", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       } 
/*     */       
/* 166 */       if (BitUtil.check(infoGroups, 7)) {
/* 167 */         position.set("driverUniqueId", String.valueOf(buf.readUnsignedIntLE()));
/*     */       }
/*     */       
/* 170 */       buf.readerIndex(buf.writerIndex() - 3);
/* 171 */       sendResponse(channel, device, id, buf.readUnsignedShortLE());
/*     */       
/* 173 */       return position;
/*     */     } 
/*     */     
/* 176 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MxtProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */