/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Teltonika2ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final int IMAGE_PACKET_MAX = 2048;
/*     */   private final boolean connectionless;
/*     */   private boolean extended;
/*  48 */   private final Map<String, Object> bufferedBeacon = new HashMap<>();
/*     */   private String bufferedResult;
/*  50 */   private final Map<Long, ByteBuf> photos = new HashMap<>(); public static final int CODEC_GH3000 = 7; public static final int CODEC_8 = 8; public static final int CODEC_8_EXT = 142;
/*     */   
/*     */   public void setExtended(boolean extended) {
/*  53 */     this.extended = extended;
/*     */   }
/*     */   public static final int CODEC_12 = 12; public static final int CODEC_13 = 13; public static final int CODEC_16 = 16;
/*     */   public Teltonika2ProtocolDecoder(Protocol protocol, boolean connectionless) {
/*  57 */     super(protocol);
/*  58 */     this.connectionless = connectionless;
/*  59 */     this.extended = Context.getConfig().getBoolean(getProtocolName() + ".extended");
/*     */   }
/*     */ 
/*     */   
/*     */   private void parseIdentification(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/*  64 */     int length = buf.readUnsignedShort();
/*  65 */     String imei = buf.toString(buf.readerIndex(), length, StandardCharsets.US_ASCII);
/*  66 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */     
/*  68 */     if (channel != null) {
/*  69 */       ByteBuf response = Unpooled.buffer(1);
/*  70 */       if (deviceSession != null) {
/*  71 */         response.writeByte(1);
/*     */       } else {
/*  73 */         response.writeByte(0);
/*     */       } 
/*  75 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendImageRequest(Channel channel, SocketAddress remoteAddress, long id, int offset, int size) {
/*  87 */     if (channel != null) {
/*  88 */       ByteBuf response = Unpooled.buffer();
/*  89 */       response.writeInt(0);
/*  90 */       response.writeShort(0);
/*  91 */       response.writeShort(19);
/*  92 */       response.writeByte(12);
/*  93 */       response.writeByte(1);
/*  94 */       response.writeByte(13);
/*  95 */       response.writeInt(11);
/*  96 */       response.writeByte(2);
/*  97 */       response.writeInt((int)id);
/*  98 */       response.writeInt(offset);
/*  99 */       response.writeShort(size);
/* 100 */       response.writeByte(1);
/* 101 */       response.writeShort(0);
/* 102 */       response.writeShort(Checksum.crc16(Checksum.CRC16_IBM, response
/* 103 */             .nioBuffer(8, response.readableBytes() - 10)));
/* 104 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private boolean isPrintable(ByteBuf buf, int length) {
/* 109 */     boolean printable = true;
/* 110 */     for (int i = 0; i < length; i++) {
/* 111 */       byte b = buf.getByte(buf.readerIndex() + i);
/* 112 */       if (b < 32 && b != 13 && b != 10) {
/* 113 */         printable = false;
/*     */         break;
/*     */       } 
/*     */     } 
/* 117 */     return printable;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeSerial(Channel channel, SocketAddress remoteAddress, Position position, ByteBuf buf) {
/* 122 */     getLastLocation(position, null);
/*     */     
/* 124 */     int type = buf.readUnsignedByte();
/* 125 */     if (type == 13) {
/*     */       
/* 127 */       buf.readInt();
/* 128 */       int subtype = buf.readUnsignedByte();
/* 129 */       if (subtype == 1) {
/*     */         
/* 131 */         long photoId = buf.readUnsignedInt();
/* 132 */         ByteBuf photo = Unpooled.buffer(buf.readInt());
/* 133 */         this.photos.put(Long.valueOf(photoId), photo);
/* 134 */         sendImageRequest(channel, remoteAddress, photoId, 0, 
/*     */             
/* 136 */             Math.min(2048, photo.capacity()));
/*     */       }
/* 138 */       else if (subtype == 2) {
/*     */         
/* 140 */         long photoId = buf.readUnsignedInt();
/* 141 */         buf.readInt();
/* 142 */         ByteBuf photo = this.photos.get(Long.valueOf(photoId));
/* 143 */         photo.writeBytes(buf, buf.readUnsignedShort());
/* 144 */         if (photo.writableBytes() > 0) {
/* 145 */           sendImageRequest(channel, remoteAddress, photoId, photo
/*     */               
/* 147 */               .writerIndex(), Math.min(2048, photo.writableBytes()));
/*     */         } else {
/* 149 */           String uniqueId = Context.getIdentityManager().getById(position.getDeviceId()).getUniqueId();
/* 150 */           this.photos.remove(Long.valueOf(photoId));
/*     */           try {
/* 152 */             position.set("image", Context.getMediaManager().writeFile(uniqueId, photo, "jpg"));
/*     */           } finally {
/* 154 */             photo.release();
/*     */           }
/*     */         
/*     */         }
/*     */       
/*     */       } 
/*     */     } else {
/*     */       
/* 162 */       position.set("type", Integer.valueOf(type));
/*     */       
/* 164 */       int length = buf.readInt();
/* 165 */       if (isPrintable(buf, length)) {
/* 166 */         String data = buf.readSlice(length).toString(StandardCharsets.US_ASCII).trim();
/* 167 */         if (data.startsWith("UUUUww") && data.endsWith("SSS")) {
/* 168 */           String[] values = data.substring(6, data.length() - 4).split(";");
/* 169 */           for (int i = 0; i < 8; i++) {
/* 170 */             position.set("axle" + (i + 1), Double.valueOf(Double.parseDouble(values[i])));
/*     */           }
/* 172 */           position.set("loadTruck", Double.valueOf(Double.parseDouble(values[8])));
/* 173 */           position.set("loadTrailer", Double.valueOf(Double.parseDouble(values[9])));
/* 174 */           position.set("totalTruck", Double.valueOf(Double.parseDouble(values[10])));
/* 175 */           position.set("totalTrailer", Double.valueOf(Double.parseDouble(values[11])));
/*     */         } else {
/* 177 */           position.set("result", data);
/*     */         } 
/*     */       } else {
/* 180 */         position.set("result", ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private long readValue(ByteBuf buf, int length, boolean signed) {
/* 186 */     switch (length) {
/*     */       case 1:
/* 188 */         return signed ? buf.readByte() : buf.readUnsignedByte();
/*     */       case 2:
/* 190 */         return signed ? buf.readShort() : buf.readUnsignedShort();
/*     */       case 4:
/* 192 */         return signed ? buf.readInt() : buf.readUnsignedInt();
/*     */     } 
/* 194 */     return buf.readLong();
/*     */   }
/*     */   
/*     */   private void decodeOtherParameter(Position position, int id, ByteBuf buf, int length) {
/*     */     long driverUniqueId, value;
/* 199 */     switch (id) {
/*     */       case 1:
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/* 204 */         position.set("di" + id, Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 9:
/* 207 */         position.set("adc1", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 10:
/* 210 */         position.set("adc2", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 236:
/* 213 */         position.set("axisX", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 237:
/* 216 */         position.set("axisY", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 238:
/* 219 */         position.set("axisZ", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 21:
/* 222 */         position.set("rssi", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 701:
/*     */       case 702:
/*     */       case 703:
/*     */       case 704:
/* 228 */         position.set("temp" + (id - 24 + 4), Double.valueOf(readValue(buf, length, true) * 0.1D));
/*     */         return;
/*     */       case 66:
/* 231 */         position.set("power", Double.valueOf(readValue(buf, length, false) * 0.001D));
/*     */         return;
/*     */       case 67:
/* 234 */         position.set("battery", Double.valueOf(readValue(buf, length, false) * 0.001D));
/*     */         return;
/*     */       case 72:
/*     */       case 73:
/*     */       case 74:
/*     */       case 75:
/* 240 */         position.set("temp" + (id - 71), Double.valueOf(readValue(buf, length, true) * 0.1D));
/*     */         return;
/*     */       case 78:
/* 243 */         driverUniqueId = readValue(buf, length, false);
/* 244 */         if (driverUniqueId != 0L) {
/* 245 */           position.set("driverUniqueId", String.format("%016X", new Object[] { Long.valueOf(driverUniqueId) }));
/*     */         }
/*     */         return;
/*     */       case 22:
/* 249 */         position.set("workMode", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 179:
/* 252 */         position.set("out1", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 180:
/* 255 */         position.set("out2", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 181:
/* 258 */         position.set("pdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 182:
/* 261 */         position.set("hdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 178:
/* 264 */         position.set("networkType", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 253:
/* 267 */         switch ((int)readValue(buf, length, false)) {
/*     */           case 1:
/* 269 */             position.set("alarm", "hardAcceleration");
/*     */             break;
/*     */           case 2:
/* 272 */             position.set("alarm", "hardBraking");
/*     */             break;
/*     */           case 3:
/* 275 */             position.set("alarm", "hardCornering");
/*     */             break;
/*     */         } 
/*     */         
/*     */         return;
/*     */       
/*     */       case 239:
/* 282 */         position.set("ignition", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 240:
/* 285 */         position.set("motion", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 241:
/* 288 */         position.set("operator", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 389:
/* 291 */         value = readValue(buf, length, false);
/* 292 */         if (BitUtil.between(value, 4, 8) == 1L) {
/* 293 */           position.set("alarm", "sos");
/*     */         }
/* 295 */         position.set("io" + id, Long.valueOf(value));
/*     */         return;
/*     */     } 
/* 298 */     position.set("io" + id, Long.valueOf(readValue(buf, length, false)));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeGh3000Parameter(Position position, int id, ByteBuf buf, int length) {
/* 304 */     switch (id) {
/*     */       case 1:
/* 306 */         position.set("batteryLevel", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 2:
/* 309 */         position.set("usbConnected", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 5:
/* 312 */         position.set("uptime", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 20:
/* 315 */         position.set("hdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 21:
/* 318 */         position.set("vdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 22:
/* 321 */         position.set("pdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 67:
/* 324 */         position.set("battery", Double.valueOf(readValue(buf, length, false) * 0.001D));
/*     */         return;
/*     */       case 221:
/* 327 */         position.set("button", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 222:
/* 330 */         if (readValue(buf, length, false) == 1L) {
/* 331 */           position.set("alarm", "sos");
/*     */         }
/*     */         return;
/*     */       case 240:
/* 335 */         position.set("motion", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 244:
/* 338 */         position.set("roaming", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */     } 
/* 341 */     position.set("io" + id, Long.valueOf(readValue(buf, length, false)));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeParameter(Position position, int id, ByteBuf buf, int length, int codec) {
/* 347 */     if (codec == 7) {
/* 348 */       decodeGh3000Parameter(position, id, buf, length);
/*     */     } else {
/* 350 */       decodeOtherParameter(position, id, buf, length);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeNetwork(Position position) {
/* 355 */     long cid = position.getLong("io205");
/* 356 */     int lac = position.getInteger("io206");
/* 357 */     if (cid != 0L && lac != 0) {
/* 358 */       CellTower cellTower = CellTower.fromLacCid(lac, cid);
/* 359 */       long operator = position.getInteger("operator");
/* 360 */       if (operator != 0L) {
/* 361 */         cellTower.setOperator(operator);
/*     */       }
/* 363 */       position.setNetwork(new Network(cellTower));
/*     */     } 
/*     */   }
/*     */   
/*     */   private int readExtByte(ByteBuf buf, int codec, int... codecs) {
/* 368 */     boolean ext = false;
/* 369 */     for (int c : codecs) {
/* 370 */       if (codec == c) {
/* 371 */         ext = true;
/*     */         break;
/*     */       } 
/*     */     } 
/* 375 */     if (ext) {
/* 376 */       return buf.readUnsignedShort();
/*     */     }
/* 378 */     return buf.readUnsignedByte();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeLocation(Position position, ByteBuf buf, int codec) {
/* 384 */     int globalMask = 15;
/*     */     
/* 386 */     if (codec == 7) {
/*     */       
/* 388 */       long time = buf.readUnsignedInt() & 0x3FFFFFFFL;
/* 389 */       time += 1167609600L;
/*     */       
/* 391 */       globalMask = buf.readUnsignedByte();
/* 392 */       if (BitUtil.check(globalMask, 0)) {
/*     */         
/* 394 */         position.setTime(new Date(time * 1000L));
/*     */         
/* 396 */         int locationMask = buf.readUnsignedByte();
/*     */         
/* 398 */         if (BitUtil.check(locationMask, 0)) {
/* 399 */           position.setLatitude(buf.readFloat());
/* 400 */           position.setLongitude(buf.readFloat());
/*     */         } 
/*     */         
/* 403 */         if (BitUtil.check(locationMask, 1)) {
/* 404 */           position.setAltitude(buf.readUnsignedShort());
/*     */         }
/*     */         
/* 407 */         if (BitUtil.check(locationMask, 2)) {
/* 408 */           position.setCourse(buf.readUnsignedByte() * 360.0D / 256.0D);
/*     */         }
/*     */         
/* 411 */         if (BitUtil.check(locationMask, 3)) {
/* 412 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */         }
/*     */         
/* 415 */         if (BitUtil.check(locationMask, 4)) {
/* 416 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */         }
/*     */         
/* 419 */         if (BitUtil.check(locationMask, 5)) {
/* 420 */           CellTower cellTower = CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedShort());
/*     */           
/* 422 */           if (BitUtil.check(locationMask, 6)) {
/* 423 */             cellTower.setSignalStrength(Integer.valueOf(buf.readUnsignedByte()));
/*     */           }
/*     */           
/* 426 */           if (BitUtil.check(locationMask, 7)) {
/* 427 */             cellTower.setOperator(buf.readUnsignedInt());
/*     */           }
/*     */           
/* 430 */           position.setNetwork(new Network(cellTower));
/*     */         } else {
/*     */           
/* 433 */           if (BitUtil.check(locationMask, 6)) {
/* 434 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */           }
/* 436 */           if (BitUtil.check(locationMask, 7)) {
/* 437 */             position.set("operator", Long.valueOf(buf.readUnsignedInt()));
/*     */           }
/*     */         }
/*     */       
/*     */       } else {
/*     */         
/* 443 */         getLastLocation(position, new Date(time * 1000L));
/*     */       }
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 449 */       position.setTime(new Date(buf.readLong()));
/*     */       
/* 451 */       position.set("priority", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 453 */       position.setLongitude(buf.readInt() / 1.0E7D);
/* 454 */       position.setLatitude(buf.readInt() / 1.0E7D);
/* 455 */       position.setAltitude(buf.readShort());
/* 456 */       position.setCourse(buf.readUnsignedShort());
/*     */       
/* 458 */       int satellites = buf.readUnsignedByte();
/* 459 */       position.set("sat", Integer.valueOf(satellites));
/*     */       
/* 461 */       position.setValid((satellites != 0));
/*     */       
/* 463 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*     */       
/* 465 */       position.set("event", Integer.valueOf(readExtByte(buf, codec, new int[] { 142, 16 })));
/* 466 */       if (codec == 16) {
/* 467 */         buf.readUnsignedByte();
/*     */       }
/*     */       
/* 470 */       readExtByte(buf, codec, new int[] { 142 });
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 475 */     if (BitUtil.check(globalMask, 1)) {
/* 476 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 477 */       for (int j = 0; j < cnt; j++) {
/* 478 */         decodeParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 1, codec);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 483 */     if (BitUtil.check(globalMask, 2)) {
/* 484 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 485 */       for (int j = 0; j < cnt; j++) {
/* 486 */         decodeParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 2, codec);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 491 */     if (BitUtil.check(globalMask, 3)) {
/* 492 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 493 */       for (int j = 0; j < cnt; j++) {
/* 494 */         decodeParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 4, codec);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 499 */     if (codec == 8 || codec == 142 || codec == 16) {
/* 500 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 501 */       for (int j = 0; j < cnt; j++) {
/* 502 */         decodeOtherParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 8);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 507 */     if (this.extended) {
/* 508 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 509 */       for (int j = 0; j < cnt; j++) {
/* 510 */         int id = readExtByte(buf, codec, new int[] { 142, 16 });
/* 511 */         position.set("io" + id, ByteBufUtil.hexDump(buf.readSlice(16)));
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 516 */     if (codec == 142) {
/* 517 */       int cnt = buf.readUnsignedShort();
/* 518 */       for (int j = 0; j < cnt; j++) {
/* 519 */         int id = buf.readUnsignedShort();
/* 520 */         int length = buf.readUnsignedShort();
/* 521 */         if (id == 256) {
/* 522 */           position.set("vin", buf
/* 523 */               .readSlice(length).toString(StandardCharsets.US_ASCII));
/* 524 */         } else if (id == 281) {
/* 525 */           position.set("dtcs", buf
/* 526 */               .readSlice(length).toString(StandardCharsets.US_ASCII).replace(',', ' '));
/* 527 */         } else if (id == 385) {
/* 528 */           ByteBuf data = buf.readSlice(length);
/* 529 */           data.readUnsignedByte();
/* 530 */           int index = 1;
/* 531 */           while (data.isReadable()) {
/* 532 */             int flags = data.readUnsignedByte();
/* 533 */             if (BitUtil.from(flags, 4) > 0) {
/* 534 */               position.set("beacon" + index + "Uuid", ByteBufUtil.hexDump(data.readSlice(16)));
/* 535 */               position.set("beacon" + index + "Major", Integer.valueOf(data.readUnsignedShort()));
/* 536 */               position.set("beacon" + index + "Minor", Integer.valueOf(data.readUnsignedShort()));
/*     */             } else {
/* 538 */               position.set("beacon" + index + "Namespace", ByteBufUtil.hexDump(data.readSlice(10)));
/* 539 */               position.set("beacon" + index + "Instance", ByteBufUtil.hexDump(data.readSlice(6)));
/*     */             } 
/* 541 */             position.set("beacon" + index + "Rssi", Integer.valueOf(data.readByte()));
/* 542 */             if (BitUtil.check(flags, 1)) {
/* 543 */               position.set("beacon" + index + "Battery", Double.valueOf(data.readUnsignedShort() * 0.01D));
/*     */             }
/* 545 */             if (BitUtil.check(flags, 2)) {
/* 546 */               position.set("beacon" + index + "Temp", Integer.valueOf(data.readUnsignedShort()));
/*     */             }
/* 548 */             index++;
/*     */           } 
/*     */         } else {
/* 551 */           position.set("io" + id, ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 556 */     decodeNetwork(position);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private List<Position> parseData(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int locationPacketId, String... imei) {
/* 562 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 564 */     if (!this.connectionless) {
/* 565 */       buf.readUnsignedInt();
/*     */     }
/*     */     
/* 568 */     int codec = buf.readUnsignedByte();
/* 569 */     int count = buf.readUnsignedByte();
/*     */     
/* 571 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
/*     */     
/* 573 */     if (deviceSession == null) {
/* 574 */       return null;
/*     */     }
/*     */     
/* 577 */     for (int i = 0; i < count; i++) {
/* 578 */       Position position = new Position(getProtocolName());
/*     */       
/* 580 */       position.setDeviceId(deviceSession.getDeviceId());
/* 581 */       position.setValid(true);
/*     */       
/* 583 */       if (codec == 13) {
/* 584 */         buf.readUnsignedByte();
/* 585 */         int length = buf.readInt() - 4;
/* 586 */         getLastLocation(position, new Date(buf.readUnsignedInt() * 1000L));
/* 587 */         if (isPrintable(buf, length)) {
/* 588 */           position.set("result", buf
/* 589 */               .readCharSequence(length, StandardCharsets.US_ASCII).toString().trim());
/*     */         } else {
/* 591 */           position.set("result", 
/* 592 */               ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */         } 
/* 594 */       } else if (codec == 12) {
/* 595 */         decodeSerial(channel, remoteAddress, position, buf);
/*     */       } else {
/* 597 */         decodeLocation(position, buf, codec);
/*     */       } 
/*     */       
/* 600 */       if (!position.getOutdated() || !position.getAttributes().isEmpty()) {
/* 601 */         boolean hasBeacon = false;
/* 602 */         for (String key : position.getAttributes().keySet()) {
/* 603 */           if (key.startsWith("beacon")) {
/* 604 */             hasBeacon = true;
/*     */             break;
/*     */           } 
/*     */         } 
/* 608 */         String result = position.getString("result");
/* 609 */         if (hasBeacon || result != null) {
/* 610 */           this.bufferedBeacon.clear();
/* 611 */           for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>)position.getAttributes().entrySet()) {
/* 612 */             if (((String)entry.getKey()).startsWith("beacon")) {
/* 613 */               this.bufferedBeacon.put(entry.getKey(), entry.getValue());
/*     */             }
/*     */           } 
/* 616 */           this.bufferedResult = result;
/*     */         } else {
/* 618 */           if (!this.bufferedBeacon.isEmpty()) {
/* 619 */             position.getAttributes().putAll(this.bufferedBeacon);
/* 620 */             this.bufferedBeacon.clear();
/*     */           } 
/* 622 */           if (this.bufferedResult != null) {
/* 623 */             position.set("result", this.bufferedResult);
/* 624 */             this.bufferedResult = null;
/*     */           } 
/* 626 */           positions.add(position);
/*     */         } 
/*     */ 
/*     */         
/* 630 */         int fuel = (position.getInteger("io201") + position.getInteger("io203")) / 2 + position.getInteger("io210");
/* 631 */         position.set("combinedFuel", Integer.valueOf(fuel));
/*     */       } 
/*     */     } 
/*     */     
/* 635 */     if (channel != null && codec != 12 && codec != 13) {
/* 636 */       ByteBuf response = Unpooled.buffer();
/* 637 */       if (this.connectionless) {
/* 638 */         response.writeShort(5);
/* 639 */         response.writeShort(0);
/* 640 */         response.writeByte(1);
/* 641 */         response.writeByte(locationPacketId);
/* 642 */         response.writeByte(count);
/*     */       } else {
/* 644 */         response.writeInt(count);
/*     */       } 
/* 646 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/* 649 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 655 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 657 */     if (this.connectionless) {
/* 658 */       return decodeUdp(channel, remoteAddress, buf);
/*     */     }
/* 660 */     return decodeTcp(channel, remoteAddress, buf);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeTcp(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {
/* 666 */     if (buf.getUnsignedShort(0) > 0) {
/* 667 */       parseIdentification(channel, remoteAddress, buf);
/*     */     } else {
/* 669 */       buf.skipBytes(4);
/* 670 */       return parseData(channel, remoteAddress, buf, 0, new String[0]);
/*     */     } 
/*     */     
/* 673 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeUdp(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {
/* 678 */     buf.readUnsignedShort();
/* 679 */     buf.readUnsignedShort();
/* 680 */     buf.readUnsignedByte();
/* 681 */     int locationPacketId = buf.readUnsignedByte();
/* 682 */     String imei = buf.readSlice(buf.readUnsignedShort()).toString(StandardCharsets.US_ASCII);
/*     */     
/* 684 */     return parseData(channel, remoteAddress, buf, locationPacketId, new String[] { imei });
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Teltonika2ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */