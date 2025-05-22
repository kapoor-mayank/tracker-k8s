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
/*     */ public class CellocatorProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   static final int MSG_CLIENT_STATUS = 0;
/*     */   static final int MSG_CLIENT_PROGRAMMING = 3;
/*     */   static final int MSG_CLIENT_SERIAL_LOG = 7;
/*     */   static final int MSG_CLIENT_SERIAL = 8;
/*     */   static final int MSG_CLIENT_MODULAR = 9;
/*     */   static final int MSG_CLIENT_MODULAR_EXT = 11;
/*     */   public static final int MSG_SERVER_ACKNOWLEDGE = 4;
/*     */   
/*     */   public CellocatorProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
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
/*     */   public static ByteBuf encodeContent(int type, int uniqueId, int packetNumber, ByteBuf content) {
/*  49 */     ByteBuf buf = Unpooled.buffer();
/*  50 */     buf.writeByte(77);
/*  51 */     buf.writeByte(67);
/*  52 */     buf.writeByte(71);
/*  53 */     buf.writeByte(80);
/*  54 */     buf.writeByte(type);
/*  55 */     buf.writeIntLE(uniqueId);
/*  56 */     buf.writeByte(packetNumber);
/*  57 */     buf.writeIntLE(0);
/*  58 */     buf.writeBytes(content);
/*     */     
/*  60 */     byte checksum = 0;
/*  61 */     for (int i = 4; i < buf.writerIndex(); i++) {
/*  62 */       checksum = (byte)(checksum + buf.getByte(i));
/*     */     }
/*  64 */     buf.writeByte(checksum);
/*     */     
/*  66 */     return buf;
/*     */   }
/*     */   
/*     */   private void sendReply(Channel channel, SocketAddress remoteAddress, long deviceId, byte packetNumber) {
/*  70 */     if (channel != null) {
/*  71 */       ByteBuf content = Unpooled.buffer();
/*  72 */       content.writeByte(0);
/*  73 */       content.writeByte(packetNumber);
/*  74 */       content.writeZero(11);
/*     */       
/*  76 */       ByteBuf reply = encodeContent(4, (int)deviceId, packetNumber, content);
/*  77 */       channel.writeAndFlush(new NetworkMessage(reply, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void sendModuleResponse(Channel channel, SocketAddress remoteAddress, long deviceId, byte packetNumber) {
/*  82 */     if (channel != null) {
/*  83 */       ByteBuf content = Unpooled.buffer();
/*  84 */       content.writeByte(128);
/*  85 */       content.writeShortLE(10);
/*  86 */       content.writeIntLE(0);
/*  87 */       content.writeByte(9);
/*  88 */       content.writeShortLE(3);
/*  89 */       content.writeByte(0);
/*  90 */       content.writeShortLE(0);
/*     */       
/*  92 */       ByteBuf reply = encodeContent(11, (int)deviceId, packetNumber, content);
/*  93 */       channel.writeAndFlush(new NetworkMessage(reply, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String decodeAlarm(short reason) {
/*  98 */     switch (reason) {
/*     */       case 70:
/* 100 */         return "sos";
/*     */       case 80:
/* 102 */         return "powerCut";
/*     */       case 81:
/* 104 */         return "lowPower";
/*     */     } 
/* 106 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeStatus(ByteBuf buf, DeviceSession deviceSession, boolean alternative) {
/* 112 */     Position position = new Position(getProtocolName());
/* 113 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 115 */     position.set("versionHw", Short.valueOf(buf.readUnsignedByte()));
/* 116 */     position.set("versionFw", Short.valueOf(buf.readUnsignedByte()));
/* 117 */     buf.readUnsignedByte();
/*     */     
/* 119 */     position.set("status", Integer.valueOf(buf.readUnsignedByte() & 0xF));
/*     */     
/* 121 */     buf.readUnsignedByte();
/* 122 */     buf.readUnsignedByte();
/* 123 */     short event = buf.readUnsignedByte();
/* 124 */     position.set("alarm", decodeAlarm(event));
/* 125 */     position.set("event", Short.valueOf(event));
/*     */     
/* 127 */     position.set("mode", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 129 */     long input = buf.readUnsignedInt();
/* 130 */     position.set("ignition", Boolean.valueOf(BitUtil.check(input, 29)));
/* 131 */     position.set("door", Boolean.valueOf(BitUtil.check(input, 24)));
/* 132 */     position.set("charge", Boolean.valueOf(BitUtil.check(input, 7)));
/* 133 */     position.set("input", Long.valueOf(input));
/*     */     
/* 135 */     if (alternative) {
/* 136 */       buf.readUnsignedByte();
/* 137 */       position.set("adc1", Integer.valueOf(buf.readUnsignedShortLE()));
/* 138 */       position.set("adc2", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */     } else {
/* 140 */       buf.readUnsignedByte();
/* 141 */       position.set("adc1", Short.valueOf(buf.readUnsignedByte()));
/* 142 */       position.set("adc2", Short.valueOf(buf.readUnsignedByte()));
/* 143 */       position.set("adc3", Short.valueOf(buf.readUnsignedByte()));
/* 144 */       position.set("adc4", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 147 */     position.set("odometer", Integer.valueOf(buf.readUnsignedMediumLE()));
/*     */     
/* 149 */     buf.skipBytes(6);
/* 150 */     buf.readUnsignedShortLE();
/* 151 */     buf.readUnsignedByte();
/* 152 */     buf.readUnsignedByte();
/* 153 */     buf.readUnsignedByte();
/*     */     
/* 155 */     position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 157 */     position.setValid(true);
/*     */     
/* 159 */     if (alternative) {
/* 160 */       position.setLongitude(buf.readIntLE() / 1.0E7D);
/* 161 */       position.setLatitude(buf.readIntLE() / 1.0E7D);
/*     */     } else {
/* 163 */       position.setLongitude(buf.readIntLE() / Math.PI * 180.0D / 1.0E8D);
/* 164 */       position.setLatitude(buf.readIntLE() / Math.PI * 180.0D / 1.0E8D);
/*     */     } 
/*     */     
/* 167 */     position.setAltitude(buf.readIntLE() * 0.01D);
/*     */     
/* 169 */     if (alternative) {
/* 170 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedIntLE()));
/* 171 */       position.setCourse(buf.readUnsignedShortLE() / 1000.0D);
/*     */     } else {
/* 173 */       position.setSpeed(UnitsConverter.knotsFromMps(buf.readUnsignedIntLE() * 0.01D));
/* 174 */       position.setCourse(buf.readUnsignedShortLE() / Math.PI * 180.0D / 1000.0D);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 179 */     DateBuilder dateBuilder = (new DateBuilder()).setTimeReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedShortLE());
/* 180 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 182 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeModular(ByteBuf buf, DeviceSession deviceSession) {
/* 187 */     Position position = new Position(getProtocolName());
/* 188 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 190 */     buf.readUnsignedByte();
/* 191 */     buf.readUnsignedShortLE();
/* 192 */     buf.readUnsignedShortLE();
/* 193 */     buf.readUnsignedShortLE();
/*     */     
/* 195 */     while (buf.readableBytes() > 3) {
/*     */       int count, i; DateBuilder dateBuilder;
/* 197 */       int moduleType = buf.readUnsignedByte();
/* 198 */       int endIndex = buf.readUnsignedShortLE() + buf.readerIndex();
/*     */       
/* 200 */       switch (moduleType) {
/*     */         case 2:
/* 202 */           buf.readUnsignedShortLE();
/* 203 */           buf.readUnsignedIntLE();
/* 204 */           count = buf.readUnsignedByte();
/* 205 */           for (i = 0; i < count; i++) {
/* 206 */             int id = buf.readUnsignedShortLE();
/* 207 */             buf.readUnsignedByte();
/* 208 */             position.set("io" + id, Long.valueOf(buf.readUnsignedIntLE()));
/*     */           } 
/*     */           break;
/*     */         case 6:
/* 212 */           buf.readUnsignedByte();
/* 213 */           buf.readUnsignedByte();
/* 214 */           buf.readUnsignedByte();
/* 215 */           buf.readUnsignedByte();
/* 216 */           position.setLongitude(buf.readIntLE() / Math.PI * 180.0D / 1.0E8D);
/* 217 */           position.setLatitude(buf.readIntLE() / Math.PI * 180.0D / 1.0E8D);
/* 218 */           position.setAltitude(buf.readIntLE() * 0.01D);
/* 219 */           position.setSpeed(UnitsConverter.knotsFromMps(buf.readUnsignedByte() * 0.01D));
/* 220 */           position.setCourse(buf.readUnsignedShortLE() / Math.PI * 180.0D / 1000.0D);
/*     */           break;
/*     */         case 7:
/* 223 */           buf.readUnsignedByte();
/*     */ 
/*     */           
/* 226 */           dateBuilder = (new DateBuilder()).setTimeReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 227 */           position.setTime(dateBuilder.getDate());
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 233 */       buf.readerIndex(endIndex);
/*     */     } 
/*     */ 
/*     */     
/* 237 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 244 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 246 */     boolean alternative = (buf.getByte(buf.readerIndex() + 3) != 80);
/*     */     
/* 248 */     buf.skipBytes(4);
/* 249 */     int type = buf.readUnsignedByte();
/*     */     
/* 251 */     long deviceUniqueId = buf.readUnsignedIntLE();
/* 252 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(deviceUniqueId) });
/* 253 */     if (deviceSession == null) {
/* 254 */       return null;
/*     */     }
/*     */     
/* 257 */     if (type != 8) {
/* 258 */       buf.readUnsignedShortLE();
/*     */     }
/* 260 */     byte packetNumber = buf.readByte();
/*     */     
/* 262 */     if (type == 11) {
/* 263 */       sendModuleResponse(channel, remoteAddress, deviceUniqueId, packetNumber);
/*     */     } else {
/* 265 */       sendReply(channel, remoteAddress, deviceUniqueId, packetNumber);
/*     */     } 
/*     */     
/* 268 */     if (type == 0)
/* 269 */       return decodeStatus(buf, deviceSession, alternative); 
/* 270 */     if (type == 11) {
/* 271 */       return decodeModular(buf, deviceSession);
/*     */     }
/*     */     
/* 274 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CellocatorProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */