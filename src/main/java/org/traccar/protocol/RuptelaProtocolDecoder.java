/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DataConverter;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RuptelaProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private final boolean mergePositions;
/*     */   public static final int MSG_RECORDS = 1;
/*     */   public static final int MSG_DEVICE_CONFIGURATION = 2;
/*     */   public static final int MSG_DEVICE_VERSION = 3;
/*     */   public static final int MSG_FIRMWARE_UPDATE = 4;
/*     */   public static final int MSG_SET_CONNECTION = 5;
/*     */   public static final int MSG_SET_ODOMETER = 6;
/*     */   public static final int MSG_SMS_VIA_GPRS_RESPONSE = 7;
/*     */   public static final int MSG_SMS_VIA_GPRS = 8;
/*     */   public static final int MSG_DTCS = 9;
/*     */   public static final int MSG_IDENTIFICATION = 15;
/*     */   public static final int MSG_SET_IO = 17;
/*     */   public static final int MSG_FILES = 37;
/*     */   public static final int MSG_EXTENDED_RECORDS = 68;
/*     */   
/*     */   public RuptelaProtocolDecoder(Protocol protocol, boolean mergePositions) {
/*  40 */     super(protocol);
/*  41 */     this.mergePositions = mergePositions;
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
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeCommandResponse(DeviceSession deviceSession, int type, ByteBuf buf) {
/*  59 */     Position position = new Position(getProtocolName());
/*  60 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  62 */     getLastLocation(position, null);
/*     */     
/*  64 */     position.set("type", Integer.valueOf(type));
/*     */     
/*  66 */     switch (type) {
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/*     */       case 7:
/*  71 */         position.set("result", buf
/*  72 */             .toString(buf.readerIndex(), buf.readableBytes() - 2, StandardCharsets.US_ASCII).trim());
/*  73 */         return position;
/*     */       case 17:
/*  75 */         position.set("result", 
/*  76 */             String.valueOf(buf.readUnsignedByte()));
/*  77 */         return position;
/*     */     } 
/*  79 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private long readValue(ByteBuf buf, int length, boolean signed) {
/*  84 */     switch (length) {
/*     */       case 1:
/*  86 */         return signed ? buf.readByte() : buf.readUnsignedByte();
/*     */       case 2:
/*  88 */         return signed ? buf.readShort() : buf.readUnsignedShort();
/*     */       case 4:
/*  90 */         return signed ? buf.readInt() : buf.readUnsignedInt();
/*     */     } 
/*  92 */     return buf.readLong();
/*     */   }
/*     */   
/*     */   private void decodeParameter(Position position, int id, ByteBuf buf, int length) {
/*     */     long value;
/*  97 */     switch (id) {
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/* 101 */         position.set("di" + (id - 1), Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 5:
/* 104 */         position.set("ignition", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 13:
/*     */       case 173:
/* 108 */         position.set("motion", Boolean.valueOf((readValue(buf, length, false) > 0L)));
/*     */         return;
/*     */       case 20:
/* 111 */         position.set("adc3", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 21:
/* 114 */         position.set("adc4", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 22:
/* 117 */         position.set("adc1", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 23:
/* 120 */         position.set("adc2", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 29:
/* 123 */         position.set("power", Double.valueOf(readValue(buf, length, false) * 0.001D));
/*     */         return;
/*     */       case 30:
/* 126 */         position.set("battery", Double.valueOf(readValue(buf, length, false) * 0.001D));
/*     */         return;
/*     */       case 32:
/* 129 */         position.set("deviceTemp", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 39:
/* 132 */         position.set("engineLoad", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 65:
/* 135 */         position.set("odometer", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 74:
/* 138 */         position.set("temp3", Double.valueOf(readValue(buf, length, true) * 0.1D));
/*     */         return;
/*     */       case 78:
/*     */       case 79:
/*     */       case 80:
/* 143 */         position.set("temp" + (id - 78), Double.valueOf(readValue(buf, length, true) * 0.1D));
/*     */         return;
/*     */       case 88:
/* 146 */         if (readValue(buf, length, false) > 0L) {
/* 147 */           position.set("alarm", "jamming");
/*     */         }
/*     */         return;
/*     */       case 94:
/* 151 */         position.set("rpm", Double.valueOf(readValue(buf, length, false) * 0.25D));
/*     */         return;
/*     */       case 95:
/* 154 */         position.set("obdSpeed", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 98:
/* 157 */         position.set("fuel", Double.valueOf((readValue(buf, length, false) * 100L) / 255.0D));
/*     */         return;
/*     */       case 100:
/* 160 */         position.set("fuelConsumption", Double.valueOf(readValue(buf, length, false) / 20.0D));
/*     */         return;
/*     */       case 134:
/* 163 */         if (readValue(buf, length, false) > 0L) {
/* 164 */           position.set("alarm", "hardBraking");
/*     */         }
/*     */         return;
/*     */       case 136:
/* 168 */         if (readValue(buf, length, false) > 0L) {
/* 169 */           position.set("alarm", "hardAcceleration");
/*     */         }
/*     */         return;
/*     */       case 150:
/* 173 */         position.set("operator", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 163:
/* 176 */         position.set("odometer", Long.valueOf(readValue(buf, length, false) * 5L));
/*     */         return;
/*     */       case 164:
/* 179 */         position.set("tripOdometer", Long.valueOf(readValue(buf, length, false) * 5L));
/*     */         return;
/*     */       case 165:
/* 182 */         position.set("obdSpeed", Double.valueOf(readValue(buf, length, false) / 256.0D));
/*     */         return;
/*     */       case 166:
/*     */       case 197:
/* 186 */         position.set("rpm", Double.valueOf(readValue(buf, length, false) * 0.125D));
/*     */         return;
/*     */       case 170:
/* 189 */         position.set("charge", Boolean.valueOf((readValue(buf, length, false) > 0L)));
/*     */         return;
/*     */       case 205:
/* 192 */         position.set("fuel", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 207:
/* 195 */         position.set("fuel", Double.valueOf(readValue(buf, length, false) * 0.4D));
/*     */         return;
/*     */       case 208:
/* 198 */         position.set("fuelUsed", Double.valueOf(readValue(buf, length, false) * 0.5D));
/*     */         return;
/*     */       case 251:
/*     */       case 409:
/* 202 */         position.set("ignition", Boolean.valueOf((readValue(buf, length, false) > 0L)));
/*     */         return;
/*     */       case 410:
/* 205 */         if (readValue(buf, length, false) > 0L) {
/* 206 */           position.set("alarm", "tow");
/*     */         }
/*     */         return;
/*     */       case 411:
/* 210 */         if (readValue(buf, length, false) > 0L) {
/* 211 */           position.set("alarm", "accident");
/*     */         }
/*     */         return;
/*     */       case 415:
/* 215 */         if (readValue(buf, length, false) == 0L) {
/* 216 */           position.set("alarm", "gpsAntennaCut");
/*     */         }
/*     */         return;
/*     */       case 645:
/* 220 */         value = readValue(buf, length, false);
/* 221 */         if (value != 4294967295L) {
/* 222 */           position.set("io" + id, Long.valueOf(value));
/*     */         }
/*     */         return;
/*     */       case 758:
/* 226 */         if (readValue(buf, length, false) == 1L) {
/* 227 */           position.set("alarm", "tampering");
/*     */         }
/*     */         return;
/*     */     } 
/* 231 */     position.set("io" + id, Long.valueOf(readValue(buf, length, false)));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 240 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 242 */     buf.readUnsignedShort();
/*     */     
/* 244 */     String imei = String.format("%015d", new Object[] { Long.valueOf(buf.readLong()) });
/* 245 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 246 */     if (deviceSession == null) {
/* 247 */       return null;
/*     */     }
/*     */     
/* 250 */     int type = buf.readUnsignedByte();
/*     */     
/* 252 */     if (type == 1 || type == 68) {
/*     */       
/* 254 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 256 */       buf.readUnsignedByte();
/* 257 */       int count = buf.readUnsignedByte();
/*     */       
/* 259 */       for (int i = 0; i < count; i++) {
/*     */         Position position;
/* 261 */         if (this.mergePositions && i % 2 > 0) {
/* 262 */           position = positions.get(positions.size() - 1);
/*     */         } else {
/* 264 */           position = new Position(getProtocolName());
/*     */         } 
/* 266 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 268 */         position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/* 269 */         buf.readUnsignedByte();
/*     */         
/* 271 */         if (type == 68) {
/* 272 */           buf.readUnsignedByte();
/*     */         }
/*     */         
/* 275 */         buf.readUnsignedByte();
/*     */         
/* 277 */         position.setValid(true);
/* 278 */         position.setLongitude(buf.readInt() / 1.0E7D);
/* 279 */         position.setLatitude(buf.readInt() / 1.0E7D);
/* 280 */         position.setAltitude(buf.readUnsignedShort() / 10.0D);
/* 281 */         position.setCourse(buf.readUnsignedShort() / 100.0D);
/*     */         
/* 283 */         position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/* 285 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*     */         
/* 287 */         position.set("hdop", Double.valueOf(buf.readUnsignedByte() / 10.0D));
/*     */         
/* 289 */         if (type == 68) {
/* 290 */           position.set("event", Integer.valueOf(buf.readUnsignedShort()));
/*     */         } else {
/* 292 */           position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */         } 
/*     */ 
/*     */         
/* 296 */         int cnt = buf.readUnsignedByte(); int j;
/* 297 */         for (j = 0; j < cnt; j++) {
/* 298 */           int id = (type == 68) ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 299 */           decodeParameter(position, id, buf, 1);
/*     */         } 
/*     */ 
/*     */         
/* 303 */         cnt = buf.readUnsignedByte();
/* 304 */         for (j = 0; j < cnt; j++) {
/* 305 */           int id = (type == 68) ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 306 */           decodeParameter(position, id, buf, 2);
/*     */         } 
/*     */ 
/*     */         
/* 310 */         cnt = buf.readUnsignedByte();
/* 311 */         for (j = 0; j < cnt; j++) {
/* 312 */           int id = (type == 68) ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 313 */           decodeParameter(position, id, buf, 4);
/*     */         } 
/*     */ 
/*     */         
/* 317 */         cnt = buf.readUnsignedByte();
/* 318 */         for (j = 0; j < cnt; j++) {
/* 319 */           int id = (type == 68) ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 320 */           decodeParameter(position, id, buf, 8);
/*     */         } 
/*     */         
/* 323 */         Long driverIdPart1 = (Long)position.getAttributes().remove("io126");
/* 324 */         Long driverIdPart2 = (Long)position.getAttributes().remove("io127");
/* 325 */         if (driverIdPart1 != null && driverIdPart2 != null) {
/* 326 */           ByteBuf driverId = Unpooled.copyLong(new long[] { driverIdPart1.longValue(), driverIdPart2.longValue() });
/* 327 */           position.set("driverUniqueId", driverId.toString(StandardCharsets.US_ASCII));
/* 328 */           driverId.release();
/*     */         } 
/*     */         
/* 331 */         positions.add(position);
/*     */       } 
/*     */       
/* 334 */       if (channel != null) {
/* 335 */         channel.writeAndFlush(new NetworkMessage(
/* 336 */               Unpooled.wrappedBuffer(DataConverter.parseHex("0002640113bc")), remoteAddress));
/*     */       }
/*     */       
/* 339 */       return positions;
/*     */     } 
/* 341 */     if (type == 9) {
/*     */       
/* 343 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 345 */       int count = buf.readUnsignedByte();
/*     */       
/* 347 */       for (int i = 0; i < count; i++) {
/* 348 */         Position position = new Position(getProtocolName());
/* 349 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 351 */         buf.readUnsignedByte();
/*     */         
/* 353 */         position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */         
/* 355 */         position.setValid(true);
/* 356 */         position.setLongitude(buf.readInt() / 1.0E7D);
/* 357 */         position.setLatitude(buf.readInt() / 1.0E7D);
/*     */         
/* 359 */         if (buf.readUnsignedByte() == 2) {
/* 360 */           position.set("archive", Boolean.valueOf(true));
/*     */         }
/*     */         
/* 363 */         position.set("dtcs", buf.readSlice(5).toString(StandardCharsets.US_ASCII));
/*     */         
/* 365 */         positions.add(position);
/*     */       } 
/*     */       
/* 368 */       if (channel != null) {
/* 369 */         channel.writeAndFlush(new NetworkMessage(
/* 370 */               Unpooled.wrappedBuffer(DataConverter.parseHex("00026d01c4a4")), remoteAddress));
/*     */       }
/*     */       
/* 373 */       return positions;
/*     */     } 
/* 375 */     if (type == 15) {
/*     */       
/* 377 */       ByteBuf content = Unpooled.buffer();
/* 378 */       content.writeByte(1);
/* 379 */       ByteBuf response = RuptelaProtocolEncoder.encodeContent(type, content);
/* 380 */       content.release();
/* 381 */       if (channel != null) {
/* 382 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       }
/*     */       
/* 385 */       return null;
/*     */     } 
/*     */ 
/*     */     
/* 389 */     return decodeCommandResponse(deviceSession, type, buf);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RuptelaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */