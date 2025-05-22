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
/*     */ public class TeltonikaProtocolDecoder
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
/*     */   public TeltonikaProtocolDecoder(Protocol protocol, boolean connectionless) {
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
/*     */     long driverUniqueId;
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
/*     */       case 17:
/* 213 */         position.set("axisX", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 18:
/* 216 */         position.set("axisY", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 19:
/* 219 */         position.set("axisZ", Long.valueOf(readValue(buf, length, true)));
/*     */         return;
/*     */       case 21:
/* 222 */         position.set("rssi", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 25:
/*     */       case 26:
/*     */       case 27:
/*     */       case 28:
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
/* 243 */         driverUniqueId = buf.readLongLE();
/* 244 */         if (driverUniqueId != 0L) {
/* 245 */           position.set("driverUniqueId", String.format("%016X", new Object[] { Long.valueOf(driverUniqueId) }));
/*     */         }
/*     */         return;
/*     */       case 80:
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
/*     */       case 237:
/* 264 */         position.set("networkType", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 238:
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
/*     */     } 
/* 291 */     position.set("io" + id, Long.valueOf(readValue(buf, length, false)));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeGh3000Parameter(Position position, int id, ByteBuf buf, int length) {
/* 297 */     switch (id) {
/*     */       case 1:
/* 299 */         position.set("batteryLevel", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 2:
/* 302 */         position.set("usbConnected", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 5:
/* 305 */         position.set("uptime", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 20:
/* 308 */         position.set("hdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 21:
/* 311 */         position.set("vdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 22:
/* 314 */         position.set("pdop", Double.valueOf(readValue(buf, length, false) * 0.1D));
/*     */         return;
/*     */       case 67:
/* 317 */         position.set("battery", Double.valueOf(readValue(buf, length, false) * 0.001D));
/*     */         return;
/*     */       case 221:
/* 320 */         position.set("button", Long.valueOf(readValue(buf, length, false)));
/*     */         return;
/*     */       case 222:
/* 323 */         if (readValue(buf, length, false) == 1L) {
/* 324 */           position.set("alarm", "sos");
/*     */         }
/*     */         return;
/*     */       case 240:
/* 328 */         position.set("motion", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */       case 244:
/* 331 */         position.set("roaming", Boolean.valueOf((readValue(buf, length, false) == 1L)));
/*     */         return;
/*     */     } 
/* 334 */     position.set("io" + id, Long.valueOf(readValue(buf, length, false)));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeParameter(Position position, int id, ByteBuf buf, int length, int codec) {
/* 340 */     if (codec == 7) {
/* 341 */       decodeGh3000Parameter(position, id, buf, length);
/*     */     } else {
/* 343 */       decodeOtherParameter(position, id, buf, length);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeNetwork(Position position) {
/* 348 */     long cid = position.getLong("io205");
/* 349 */     int lac = position.getInteger("io206");
/* 350 */     if (cid != 0L && lac != 0) {
/* 351 */       CellTower cellTower = CellTower.fromLacCid(lac, cid);
/* 352 */       long operator = position.getInteger("operator");
/* 353 */       if (operator != 0L) {
/* 354 */         cellTower.setOperator(operator);
/*     */       }
/* 356 */       position.setNetwork(new Network(cellTower));
/*     */     } 
/*     */   }
/*     */   
/*     */   private int readExtByte(ByteBuf buf, int codec, int... codecs) {
/* 361 */     boolean ext = false;
/* 362 */     for (int c : codecs) {
/* 363 */       if (codec == c) {
/* 364 */         ext = true;
/*     */         break;
/*     */       } 
/*     */     } 
/* 368 */     if (ext) {
/* 369 */       return buf.readUnsignedShort();
/*     */     }
/* 371 */     return buf.readUnsignedByte();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeLocation(Position position, ByteBuf buf, int codec) {
/* 377 */     int globalMask = 15;
/*     */     
/* 379 */     if (codec == 7) {
/*     */       
/* 381 */       long time = buf.readUnsignedInt() & 0x3FFFFFFFL;
/* 382 */       time += 1167609600L;
/*     */       
/* 384 */       globalMask = buf.readUnsignedByte();
/* 385 */       if (BitUtil.check(globalMask, 0)) {
/*     */         
/* 387 */         position.setTime(new Date(time * 1000L));
/*     */         
/* 389 */         int locationMask = buf.readUnsignedByte();
/*     */         
/* 391 */         if (BitUtil.check(locationMask, 0)) {
/* 392 */           position.setLatitude(buf.readFloat());
/* 393 */           position.setLongitude(buf.readFloat());
/*     */         } 
/*     */         
/* 396 */         if (BitUtil.check(locationMask, 1)) {
/* 397 */           position.setAltitude(buf.readUnsignedShort());
/*     */         }
/*     */         
/* 400 */         if (BitUtil.check(locationMask, 2)) {
/* 401 */           position.setCourse(buf.readUnsignedByte() * 360.0D / 256.0D);
/*     */         }
/*     */         
/* 404 */         if (BitUtil.check(locationMask, 3)) {
/* 405 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */         }
/*     */         
/* 408 */         if (BitUtil.check(locationMask, 4)) {
/* 409 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */         }
/*     */         
/* 412 */         if (BitUtil.check(locationMask, 5)) {
/* 413 */           CellTower cellTower = CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedShort());
/*     */           
/* 415 */           if (BitUtil.check(locationMask, 6)) {
/* 416 */             cellTower.setSignalStrength(Integer.valueOf(buf.readUnsignedByte()));
/*     */           }
/*     */           
/* 419 */           if (BitUtil.check(locationMask, 7)) {
/* 420 */             cellTower.setOperator(buf.readUnsignedInt());
/*     */           }
/*     */           
/* 423 */           position.setNetwork(new Network(cellTower));
/*     */         } else {
/*     */           
/* 426 */           if (BitUtil.check(locationMask, 6)) {
/* 427 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */           }
/* 429 */           if (BitUtil.check(locationMask, 7)) {
/* 430 */             position.set("operator", Long.valueOf(buf.readUnsignedInt()));
/*     */           }
/*     */         }
/*     */       
/*     */       } else {
/*     */         
/* 436 */         getLastLocation(position, new Date(time * 1000L));
/*     */       }
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 442 */       position.setTime(new Date(buf.readLong()));
/*     */       
/* 444 */       position.set("priority", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 446 */       position.setLongitude(buf.readInt() / 1.0E7D);
/* 447 */       position.setLatitude(buf.readInt() / 1.0E7D);
/* 448 */       position.setAltitude(buf.readShort());
/* 449 */       position.setCourse(buf.readUnsignedShort());
/*     */       
/* 451 */       int satellites = buf.readUnsignedByte();
/* 452 */       position.set("sat", Integer.valueOf(satellites));
/*     */       
/* 454 */       position.setValid((satellites != 0));
/*     */       
/* 456 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*     */       
/* 458 */       position.set("event", Integer.valueOf(readExtByte(buf, codec, new int[] { 142, 16 })));
/* 459 */       if (codec == 16) {
/* 460 */         buf.readUnsignedByte();
/*     */       }
/*     */       
/* 463 */       readExtByte(buf, codec, new int[] { 142 });
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 468 */     if (BitUtil.check(globalMask, 1)) {
/* 469 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 470 */       for (int j = 0; j < cnt; j++) {
/* 471 */         decodeParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 1, codec);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 476 */     if (BitUtil.check(globalMask, 2)) {
/* 477 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 478 */       for (int j = 0; j < cnt; j++) {
/* 479 */         decodeParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 2, codec);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 484 */     if (BitUtil.check(globalMask, 3)) {
/* 485 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 486 */       for (int j = 0; j < cnt; j++) {
/* 487 */         decodeParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 4, codec);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 492 */     if (codec == 8 || codec == 142 || codec == 16) {
/* 493 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 494 */       for (int j = 0; j < cnt; j++) {
/* 495 */         decodeOtherParameter(position, readExtByte(buf, codec, new int[] { 142, 16 }), buf, 8);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 500 */     if (this.extended) {
/* 501 */       int cnt = readExtByte(buf, codec, new int[] { 142 });
/* 502 */       for (int j = 0; j < cnt; j++) {
/* 503 */         int id = readExtByte(buf, codec, new int[] { 142, 16 });
/* 504 */         position.set("io" + id, ByteBufUtil.hexDump(buf.readSlice(16)));
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 509 */     if (codec == 142) {
/* 510 */       int cnt = buf.readUnsignedShort();
/* 511 */       for (int j = 0; j < cnt; j++) {
/* 512 */         int id = buf.readUnsignedShort();
/* 513 */         int length = buf.readUnsignedShort();
/* 514 */         if (id == 256 || id == 12267) {
/* 515 */           position.set("vin", buf
/* 516 */               .readSlice(length).toString(StandardCharsets.US_ASCII));
/* 517 */         } else if (id == 281) {
/* 518 */           position.set("dtcs", buf
/* 519 */               .readSlice(length).toString(StandardCharsets.US_ASCII).replace(',', ' '));
/* 520 */         } else if (id == 385) {
/* 521 */           ByteBuf data = buf.readSlice(length);
/* 522 */           data.readUnsignedByte();
/* 523 */           int index = 1;
/* 524 */           while (data.isReadable()) {
/* 525 */             int flags = data.readUnsignedByte();
/* 526 */             if (BitUtil.from(flags, 4) > 0) {
/* 527 */               position.set("beacon" + index + "Uuid", ByteBufUtil.hexDump(data.readSlice(16)));
/* 528 */               position.set("beacon" + index + "Major", Integer.valueOf(data.readUnsignedShort()));
/* 529 */               position.set("beacon" + index + "Minor", Integer.valueOf(data.readUnsignedShort()));
/*     */             } else {
/* 531 */               position.set("beacon" + index + "Namespace", ByteBufUtil.hexDump(data.readSlice(10)));
/* 532 */               position.set("beacon" + index + "Instance", ByteBufUtil.hexDump(data.readSlice(6)));
/*     */             } 
/* 534 */             position.set("beacon" + index + "Rssi", Integer.valueOf(data.readByte()));
/* 535 */             if (BitUtil.check(flags, 1)) {
/* 536 */               position.set("beacon" + index + "Battery", Double.valueOf(data.readUnsignedShort() * 0.01D));
/*     */             }
/* 538 */             if (BitUtil.check(flags, 2)) {
/* 539 */               position.set("beacon" + index + "Temp", Integer.valueOf(data.readUnsignedShort()));
/*     */             }
/* 541 */             index++;
/*     */           } 
/* 543 */         } else if (id == 548 || id == 10829 || id == 10831) {
/* 544 */           ByteBuf data = buf.readSlice(length);
/* 545 */           data.readUnsignedByte();
/* 546 */           for (int i = 1; data.isReadable(); i++) {
/* 547 */             ByteBuf beacon = data.readSlice(data.readUnsignedByte());
/* 548 */             while (beacon.isReadable()) {
/* 549 */               String beaconId, beaconData; int parameterId = beacon.readUnsignedByte();
/* 550 */               int parameterLength = beacon.readUnsignedByte();
/* 551 */               switch (parameterId) {
/*     */                 case 0:
/* 553 */                   position.set("tag" + i + "Rssi", Integer.valueOf(beacon.readByte()));
/*     */                   continue;
/*     */                 case 1:
/* 556 */                   beaconId = ByteBufUtil.hexDump(beacon.readSlice(parameterLength));
/* 557 */                   position.set("tag" + i + "Id", beaconId);
/*     */                   continue;
/*     */                 case 2:
/* 560 */                   beaconData = ByteBufUtil.hexDump(beacon.readSlice(parameterLength));
/* 561 */                   position.set("tag" + i + "Data", beaconData);
/*     */                   continue;
/*     */                 case 13:
/* 564 */                   position.set("tag" + i + "LowBattery", Short.valueOf(beacon.readUnsignedByte()));
/*     */                   continue;
/*     */                 case 14:
/* 567 */                   position.set("tag" + i + "Battery", Integer.valueOf(beacon.readUnsignedShort()));
/*     */                   continue;
/*     */               } 
/* 570 */               beacon.skipBytes(parameterLength);
/*     */             }
/*     */           
/*     */           } 
/*     */         } else {
/*     */           
/* 576 */           position.set("io" + id, ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */         } 
/*     */       } 
/*     */     } 
/*     */     
/* 581 */     decodeNetwork(position);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private List<Position> parseData(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int locationPacketId, String... imei) {
/* 587 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 589 */     if (!this.connectionless) {
/* 590 */       buf.readUnsignedInt();
/*     */     }
/*     */     
/* 593 */     int codec = buf.readUnsignedByte();
/* 594 */     int count = buf.readUnsignedByte();
/*     */     
/* 596 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
/*     */     
/* 598 */     if (deviceSession == null) {
/* 599 */       return null;
/*     */     }
/*     */     
/* 602 */     for (int i = 0; i < count; i++) {
/* 603 */       Position position = new Position(getProtocolName());
/*     */       
/* 605 */       position.setDeviceId(deviceSession.getDeviceId());
/* 606 */       position.setValid(true);
/*     */       
/* 608 */       if (codec == 13) {
/* 609 */         buf.readUnsignedByte();
/* 610 */         int length = buf.readInt() - 4;
/* 611 */         getLastLocation(position, new Date(buf.readUnsignedInt() * 1000L));
/* 612 */         if (isPrintable(buf, length)) {
/* 613 */           String data = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString().trim();
/* 614 */           if (data.startsWith("GTSL")) {
/* 615 */             position.set("driverUniqueId", data.split("\\|")[4]);
/*     */           } else {
/* 617 */             position.set("result", data);
/*     */           } 
/*     */         } else {
/* 620 */           position.set("result", 
/* 621 */               ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */         } 
/* 623 */       } else if (codec == 12) {
/* 624 */         decodeSerial(channel, remoteAddress, position, buf);
/*     */       } else {
/* 626 */         decodeLocation(position, buf, codec);
/*     */       } 
/*     */       
/* 629 */       if (!position.getOutdated() || !position.getAttributes().isEmpty()) {
/* 630 */         boolean hasBeacon = false;
/* 631 */         for (String key : position.getAttributes().keySet()) {
/* 632 */           if (key.startsWith("beacon")) {
/* 633 */             hasBeacon = true;
/*     */             break;
/*     */           } 
/*     */         } 
/* 637 */         String result = position.getString("result");
/* 638 */         if (hasBeacon || result != null) {
/* 639 */           this.bufferedBeacon.clear();
/* 640 */           for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>)position.getAttributes().entrySet()) {
/* 641 */             if (((String)entry.getKey()).startsWith("beacon")) {
/* 642 */               this.bufferedBeacon.put(entry.getKey(), entry.getValue());
/*     */             }
/*     */           } 
/* 645 */           this.bufferedResult = result;
/*     */         } else {
/* 647 */           if (!this.bufferedBeacon.isEmpty()) {
/* 648 */             position.getAttributes().putAll(this.bufferedBeacon);
/* 649 */             this.bufferedBeacon.clear();
/*     */           } 
/* 651 */           if (this.bufferedResult != null) {
/* 652 */             position.set("result", this.bufferedResult);
/* 653 */             this.bufferedResult = null;
/*     */           } 
/* 655 */           positions.add(position);
/*     */         } 
/*     */ 
/*     */         
/* 659 */         int fuel = (position.getInteger("io201") + position.getInteger("io203")) / 2 + position.getInteger("io210");
/* 660 */         position.set("combinedFuel", Integer.valueOf(fuel));
/*     */       } 
/*     */     } 
/*     */     
/* 664 */     if (channel != null && codec != 12 && codec != 13) {
/* 665 */       ByteBuf response = Unpooled.buffer();
/* 666 */       if (this.connectionless) {
/* 667 */         response.writeShort(5);
/* 668 */         response.writeShort(0);
/* 669 */         response.writeByte(1);
/* 670 */         response.writeByte(locationPacketId);
/* 671 */         response.writeByte(count);
/*     */       } else {
/* 673 */         response.writeInt(count);
/*     */       } 
/* 675 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/* 678 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 684 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 686 */     if (this.connectionless) {
/* 687 */       return decodeUdp(channel, remoteAddress, buf);
/*     */     }
/* 689 */     return decodeTcp(channel, remoteAddress, buf);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeTcp(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {
/* 695 */     if (buf.getUnsignedShort(0) > 0) {
/* 696 */       parseIdentification(channel, remoteAddress, buf);
/*     */     } else {
/* 698 */       buf.skipBytes(4);
/* 699 */       return parseData(channel, remoteAddress, buf, 0, new String[0]);
/*     */     } 
/*     */     
/* 702 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeUdp(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {
/* 707 */     buf.readUnsignedShort();
/* 708 */     buf.readUnsignedShort();
/* 709 */     buf.readUnsignedByte();
/* 710 */     int locationPacketId = buf.readUnsignedByte();
/* 711 */     String imei = buf.readSlice(buf.readUnsignedShort()).toString(StandardCharsets.US_ASCII);
/*     */     
/* 713 */     return parseData(channel, remoteAddress, buf, locationPacketId, new String[] { imei });
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TeltonikaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */