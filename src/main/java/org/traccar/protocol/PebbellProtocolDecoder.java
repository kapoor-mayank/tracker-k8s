/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.HashSet;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.BufferUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ import org.traccar.model.WifiAccessPoint;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PebbellProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_DATA = 1;
/*     */   public static final int MSG_CONFIGURATION = 2;
/*     */   public static final int MSG_SERVICES = 3;
/*     */   public static final int MSG_SYSTEM_CONTROL = 4;
/*     */   public static final int MSG_FIRMWARE = 126;
/*     */   public static final int MSG_RESPONSE = 127;
/*     */   
/*     */   public PebbellProtocolDecoder(Protocol protocol) {
/*  46 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private String decodeAlarm(long code) {
/*  57 */     if (BitUtil.check(code, 0)) {
/*  58 */       return "lowBattery";
/*     */     }
/*  60 */     if (BitUtil.check(code, 1)) {
/*  61 */       return "overspeed";
/*     */     }
/*  63 */     if (BitUtil.check(code, 2)) {
/*  64 */       return "fallDown";
/*     */     }
/*  66 */     if (BitUtil.check(code, 8)) {
/*  67 */       return "powerOff";
/*     */     }
/*  69 */     if (BitUtil.check(code, 9)) {
/*  70 */       return "powerOn";
/*     */     }
/*  72 */     if (BitUtil.check(code, 12)) {
/*  73 */       return "sos";
/*     */     }
/*  75 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int index, int type, ByteBuf buf) {
/*  80 */     if (channel != null) {
/*     */       
/*  82 */       ByteBuf body = Unpooled.buffer();
/*  83 */       if (type == 3) {
/*  84 */         while (buf.isReadable()) {
/*  85 */           int endIndex = buf.readUnsignedByte() + buf.readerIndex();
/*  86 */           int key = buf.readUnsignedByte();
/*  87 */           switch (key) {
/*     */             case 17:
/*     */             case 33:
/*     */             case 34:
/*  91 */               body.writeByte(10);
/*  92 */               body.writeByte(key);
/*  93 */               body.writeIntLE(0);
/*  94 */               body.writeIntLE(0);
/*  95 */               body.writeByte(0);
/*     */               break;
/*     */             case 18:
/*  98 */               body.writeByte(5);
/*  99 */               body.writeByte(key);
/* 100 */               body.writeIntLE((int)(System.currentTimeMillis() / 1000L));
/*     */               break;
/*     */           } 
/*     */ 
/*     */           
/* 105 */           buf.readerIndex(endIndex);
/*     */         } 
/*     */       } else {
/* 108 */         body.writeByte(1);
/* 109 */         body.writeByte(0);
/*     */       } 
/*     */       
/* 112 */       ByteBuf content = Unpooled.buffer();
/* 113 */       content.writeByte((type == 3) ? type : 127);
/* 114 */       content.writeBytes(body);
/* 115 */       body.release();
/*     */       
/* 117 */       ByteBuf response = Unpooled.buffer();
/* 118 */       response.writeByte(171);
/* 119 */       response.writeByte(0);
/* 120 */       response.writeShortLE(content.readableBytes());
/* 121 */       response.writeShortLE(Checksum.crc16(Checksum.CRC16_XMODEM, content.nioBuffer()));
/* 122 */       response.writeShortLE(index);
/* 123 */       response.writeBytes(content);
/* 124 */       content.release();
/*     */       
/* 126 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String readTagId(ByteBuf buf) {
/* 131 */     StringBuilder tagId = new StringBuilder();
/* 132 */     for (int i = 0; i < 6; i++) {
/* 133 */       tagId.insert(0, ByteBufUtil.hexDump(buf.readSlice(1)));
/*     */     }
/* 135 */     return tagId.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 142 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 144 */     buf.readUnsignedByte();
/* 145 */     int flags = buf.readUnsignedByte();
/* 146 */     buf.readUnsignedShortLE();
/* 147 */     buf.readUnsignedShortLE();
/* 148 */     int index = buf.readUnsignedShortLE();
/* 149 */     int type = buf.readUnsignedByte();
/*     */     
/* 151 */     if (BitUtil.check(flags, 4)) {
/* 152 */       sendResponse(channel, remoteAddress, index, type, buf.slice());
/*     */     }
/*     */     
/* 155 */     if (type == 1 || type == 3) {
/*     */       
/* 157 */       List<Position> positions = new LinkedList<>();
/* 158 */       Set<Integer> keys = new HashSet<>();
/* 159 */       Position position = new Position(getProtocolName());
/*     */       
/* 161 */       DeviceSession deviceSession = null;
/*     */       
/* 163 */       while (buf.isReadable()) {
/* 164 */         long alarm; int hdop, mcc, mnc; long status; int beaconFlags, i; long barking; int heartRate, spO2, length = buf.readUnsignedByte();
/* 165 */         int endIndex = buf.readerIndex() + length;
/* 166 */         int key = buf.readUnsignedByte();
/*     */         
/* 168 */         if (keys.contains(Integer.valueOf(key))) {
/* 169 */           positions.add(position);
/* 170 */           keys.clear();
/* 171 */           position = new Position(getProtocolName());
/*     */         } 
/* 173 */         keys.add(Integer.valueOf(key));
/*     */         
/* 175 */         switch (key) {
/*     */           case 1:
/* 177 */             deviceSession = getDeviceSession(channel, remoteAddress, new String[] { buf
/* 178 */                   .readCharSequence(15, StandardCharsets.US_ASCII).toString() });
/* 179 */             if (deviceSession == null) {
/* 180 */               return null;
/*     */             }
/*     */             break;
/*     */           case 2:
/* 184 */             alarm = buf.readUnsignedIntLE();
/* 185 */             position.set("alarm", decodeAlarm(alarm));
/* 186 */             if (BitUtil.check(alarm, 31)) {
/* 187 */               position.set("bark", Boolean.valueOf(true));
/*     */             }
/* 189 */             if (length == 5) {
/* 190 */               position.setDeviceTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */             }
/*     */             break;
/*     */           case 20:
/* 194 */             position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/* 195 */             position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/*     */             break;
/*     */           case 32:
/* 198 */             position.setLatitude(buf.readIntLE() * 1.0E-7D);
/* 199 */             position.setLongitude(buf.readIntLE() * 1.0E-7D);
/* 200 */             position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));
/* 201 */             position.setCourse(buf.readUnsignedShortLE());
/* 202 */             position.setAltitude(buf.readShortLE());
/* 203 */             hdop = buf.readUnsignedShortLE();
/* 204 */             position.setValid((hdop > 0));
/* 205 */             position.set("hdop", Double.valueOf(hdop * 0.1D));
/* 206 */             position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/* 207 */             position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 33:
/*     */           case 41:
/* 211 */             mcc = buf.readUnsignedShortLE();
/* 212 */             mnc = buf.readUnsignedByte();
/* 213 */             if (position.getNetwork() == null) {
/* 214 */               position.setNetwork(new Network());
/*     */             }
/* 216 */             while (buf.readerIndex() < endIndex) {
/* 217 */               long cid; int rssi = buf.readByte();
/* 218 */               int lac = buf.readUnsignedShortLE();
/*     */               
/* 220 */               if (key == 41) {
/* 221 */                 cid = buf.readLongLE();
/*     */               } else {
/* 223 */                 cid = buf.readUnsignedShortLE();
/*     */               } 
/* 225 */               position.getNetwork().addCellTower(CellTower.from(mcc, mnc, lac, cid, rssi));
/*     */             } 
/*     */             break;
/*     */           case 34:
/* 229 */             if (position.getNetwork() == null) {
/* 230 */               position.setNetwork(new Network());
/*     */             }
/* 232 */             while (buf.readerIndex() < endIndex) {
/* 233 */               int rssi = buf.readByte();
/* 234 */               String mac = ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/* 235 */               position.getNetwork().addWifiAccessPoint(WifiAccessPoint.from(mac
/* 236 */                     .substring(0, mac.length() - 1), rssi));
/*     */             } 
/*     */             break;
/*     */           case 35:
/*     */           case 38:
/* 241 */             if (length >= 7) {
/* 242 */               position.set("tagId", readTagId(buf));
/*     */             }
/* 244 */             if (length >= 15) {
/* 245 */               position.setLatitude(buf.readIntLE() * 1.0E-7D);
/* 246 */               position.setLongitude(buf.readIntLE() * 1.0E-7D);
/* 247 */               position.setValid(true);
/*     */             } 
/* 249 */             if (key == 38) {
/* 250 */               position.set("hdop", Double.valueOf(buf.readUnsignedShortLE() * 0.1D));
/* 251 */               position.setAltitude(buf.readShortLE()); break;
/* 252 */             }  if (length > 15) {
/* 253 */               position.set("description", buf.readCharSequence(length, StandardCharsets.US_ASCII)
/* 254 */                   .toString());
/*     */             }
/*     */             break;
/*     */           case 36:
/* 258 */             position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/* 259 */             status = buf.readUnsignedIntLE();
/* 260 */             if (BitUtil.check(status, 4)) {
/* 261 */               position.set("charge", Boolean.valueOf(true));
/*     */             }
/* 263 */             if (BitUtil.check(status, 7)) {
/* 264 */               position.set("archive", Boolean.valueOf(true));
/*     */             }
/* 266 */             position.set("motion", Boolean.valueOf(BitUtil.check(status, 9)));
/* 267 */             position.set("rssi", Long.valueOf(BitUtil.between(status, 19, 24)));
/* 268 */             position.set("batteryLevel", Long.valueOf(BitUtil.from(status, 24)));
/* 269 */             position.set("status", Long.valueOf(status));
/*     */             break;
/*     */           case 39:
/* 272 */             position.setLatitude(buf.readIntLE() * 1.0E-7D);
/* 273 */             position.setLongitude(buf.readIntLE() * 1.0E-7D);
/* 274 */             position.setValid(true);
/* 275 */             position.set("hdop", Double.valueOf(buf.readUnsignedShortLE() * 0.1D));
/* 276 */             position.setAltitude(buf.readShortLE());
/*     */             break;
/*     */           case 40:
/* 279 */             beaconFlags = buf.readUnsignedByte();
/* 280 */             position.set("tagId", readTagId(buf));
/* 281 */             position.set("tagRssi", Integer.valueOf(buf.readByte()));
/* 282 */             position.set("tag1mRssi", Integer.valueOf(buf.readByte()));
/* 283 */             if (BitUtil.check(beaconFlags, 7)) {
/* 284 */               position.setLatitude(buf.readIntLE() * 1.0E-7D);
/* 285 */               position.setLongitude(buf.readIntLE() * 1.0E-7D);
/* 286 */               position.setValid(true);
/*     */             } 
/* 288 */             if (BitUtil.check(beaconFlags, 6)) {
/* 289 */               position.set("description", buf.readCharSequence(endIndex - buf
/* 290 */                     .readerIndex(), StandardCharsets.US_ASCII).toString());
/*     */             }
/*     */             break;
/*     */           case 42:
/* 294 */             buf.readUnsignedByte();
/* 295 */             buf.skipBytes(6);
/* 296 */             buf.readUnsignedByte();
/* 297 */             position.setLatitude(buf.readIntLE() * 1.0E-7D);
/* 298 */             position.setLongitude(buf.readIntLE() * 1.0E-7D);
/* 299 */             position.setValid(true);
/* 300 */             if (endIndex > buf.readerIndex()) {
/* 301 */               position.set("description", buf.readCharSequence(endIndex - buf
/* 302 */                     .readerIndex(), StandardCharsets.US_ASCII).toString());
/*     */             }
/*     */             break;
/*     */           case 48:
/* 306 */             buf.readUnsignedIntLE();
/* 307 */             position.set("steps", Long.valueOf(buf.readUnsignedIntLE()));
/*     */             break;
/*     */           case 49:
/* 310 */             i = 1;
/* 311 */             while (buf.readerIndex() < endIndex) {
/* 312 */               position.set("activity" + i + "Time", Long.valueOf(buf.readUnsignedIntLE()));
/* 313 */               position.set("activity" + i, Long.valueOf(buf.readUnsignedIntLE()));
/* 314 */               i++;
/*     */             } 
/*     */             break;
/*     */           case 55:
/* 318 */             buf.readUnsignedIntLE();
/* 319 */             barking = buf.readUnsignedIntLE();
/* 320 */             if (BitUtil.check(barking, 31)) {
/* 321 */               position.set("barkStop", Boolean.valueOf(true));
/*     */             }
/* 323 */             position.set("barkCount", Long.valueOf(BitUtil.to(barking, 31)));
/*     */             break;
/*     */           case 64:
/* 326 */             buf.readUnsignedIntLE();
/* 327 */             heartRate = buf.readUnsignedByte();
/* 328 */             if (heartRate > 1) {
/* 329 */               position.set("heartRate", Integer.valueOf(heartRate));
/*     */             }
/*     */             break;
/*     */           case 65:
/* 333 */             buf.readUnsignedIntLE();
/* 334 */             spO2 = buf.readUnsignedByte();
/* 335 */             if (spO2 > 1) {
/* 336 */               position.set("spO2", Integer.valueOf(spO2));
/*     */             }
/*     */             break;
/*     */         } 
/*     */ 
/*     */         
/* 342 */         buf.readerIndex(endIndex);
/*     */       } 
/*     */       
/* 345 */       positions.add(position);
/*     */       
/* 347 */       if (deviceSession != null) {
/* 348 */         for (Position p : positions) {
/* 349 */           p.setDeviceId(deviceSession.getDeviceId());
/* 350 */           if (!p.getValid() && !p.getAttributes().containsKey("hdop")) {
/* 351 */             getLastLocation(p, null);
/*     */           }
/*     */         } 
/*     */       } else {
/* 355 */         return null;
/*     */       } 
/*     */       
/* 358 */       return positions;
/*     */     } 
/* 360 */     if (type == 2)
/*     */     {
/* 362 */       return decodeConfiguration(channel, remoteAddress, buf);
/*     */     }
/* 364 */     if (type == 127) {
/*     */       
/* 366 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 367 */       if (deviceSession == null) {
/* 368 */         return null;
/*     */       }
/*     */       
/* 371 */       Position position = new Position(getProtocolName());
/* 372 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 374 */       getLastLocation(position, null);
/*     */       
/* 376 */       buf.readUnsignedByte();
/* 377 */       position.set("result", String.valueOf(buf.readUnsignedByte()));
/*     */       
/* 379 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 383 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeConfiguration(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 388 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 389 */     if (deviceSession == null) {
/* 390 */       return null;
/*     */     }
/*     */     
/* 393 */     Position position = new Position(getProtocolName());
/* 394 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 396 */     getLastLocation(position, null);
/*     */     
/* 398 */     while (buf.isReadable()) {
/* 399 */       int length = buf.readUnsignedByte() - 1;
/* 400 */       int endIndex = buf.readerIndex() + length + 1;
/* 401 */       int key = buf.readUnsignedByte();
/*     */       
/* 403 */       switch (key) {
/*     */         case 1:
/* 405 */           position.set("moduleNumber", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 2:
/* 408 */           position.set("versionFw", String.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 3:
/* 411 */           position.set("imei", buf.readCharSequence(length, StandardCharsets.US_ASCII).toString());
/*     */           break;
/*     */         case 4:
/* 414 */           position.set("iccid", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 5:
/* 417 */           position.set("bleMac", ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */           break;
/*     */         case 6:
/* 420 */           position.set("settingTime", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 7:
/* 423 */           position.set("runTimes", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 10:
/* 426 */           position.set("interval", Integer.valueOf(buf.readUnsignedMedium()));
/* 427 */           position.set("petMode", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 13:
/* 430 */           position.set("passwordProtect", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 14:
/* 433 */           position.set("timeZone", Integer.valueOf(buf.readByte()));
/*     */           break;
/*     */         case 15:
/* 436 */           position.set("enableControl", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 19:
/* 439 */           position.set("deviceName", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 20:
/* 442 */           position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/* 443 */           position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */           break;
/*     */         case 21:
/* 446 */           position.set("bleLatitude", Double.valueOf(buf.readIntLE() * 1.0E-7D));
/* 447 */           position.set("bleLongitude", Double.valueOf(buf.readIntLE() * 1.0E-7D));
/* 448 */           position.set("bleLocation", BufferUtil.readString(buf, length - 8));
/*     */           break;
/*     */         case 23:
/* 451 */           position.set("gpsUrl", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 24:
/* 454 */           position.set("lbsUrl", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 26:
/* 457 */           position.set("firmware", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 27:
/* 460 */           position.set("gsmModule", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 29:
/* 463 */           position.set("agpsUpdate", Short.valueOf(buf.readUnsignedByte()));
/* 464 */           position.set("agpsLatitude", Double.valueOf(buf.readIntLE() * 1.0E-7D));
/* 465 */           position.set("agpsLongitude", Double.valueOf(buf.readIntLE() * 1.0E-7D));
/*     */           break;
/*     */         case 48:
/* 468 */           position.set("numberFlag", Short.valueOf(buf.readUnsignedByte()));
/* 469 */           position.set("number", BufferUtil.readString(buf, length - 1));
/*     */           break;
/*     */         case 49:
/* 472 */           position.set("prefixFlag", Short.valueOf(buf.readUnsignedByte()));
/* 473 */           position.set("prefix", BufferUtil.readString(buf, length - 1));
/*     */           break;
/*     */         case 51:
/* 476 */           position.set("phoneSwitches", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 64:
/* 479 */           position.set("apn", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 65:
/* 482 */           position.set("apnUser", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 66:
/* 485 */           position.set("apnPassword", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 67:
/* 488 */           buf.readUnsignedByte();
/* 489 */           position.set("port", Integer.valueOf(buf.readUnsignedShort()));
/* 490 */           position.set("server", BufferUtil.readString(buf, length - 3));
/*     */           break;
/*     */         case 68:
/* 493 */           position.set("heartbeatInterval", Long.valueOf(buf.readUnsignedInt()));
/* 494 */           position.set("uploadInterval", Long.valueOf(buf.readUnsignedInt()));
/* 495 */           position.set("uploadLazyInterval", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 71:
/* 498 */           position.set("deviceId", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 78:
/* 501 */           position.set("gsmBand", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 80:
/* 504 */           position.set("powerAlert", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 81:
/* 507 */           position.set("geoAlert", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 83:
/* 510 */           position.set("motionAlert", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 92:
/* 513 */           position.set("barkLevel", Short.valueOf(buf.readUnsignedByte()));
/* 514 */           position.set("barkInterval", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 97:
/* 517 */           position.set("msisdn", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 98:
/* 520 */           position.set("wifiWhitelist", Short.valueOf(buf.readUnsignedByte()));
/* 521 */           position.set("wifiWhitelistMac", ByteBufUtil.hexDump(buf.readSlice(6)));
/*     */           break;
/*     */         case 100:
/* 524 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 525 */           position.set("networkBand", Long.valueOf(buf.readUnsignedInt()));
/* 526 */           position.set("operator", BufferUtil.readString(buf, length - 5));
/*     */           break;
/*     */         case 101:
/* 529 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 530 */           position.set("networkStatus", Short.valueOf(buf.readUnsignedByte()));
/* 531 */           position.set("serverStatus", Short.valueOf(buf.readUnsignedByte()));
/* 532 */           position.set("networkPlmn", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 533 */           position.set("homePlmn", ByteBufUtil.hexDump(buf.readSlice(6)));
/*     */           break;
/*     */         case 102:
/* 536 */           position.set("imsi", BufferUtil.readString(buf, length));
/*     */           break;
/*     */         case 117:
/* 539 */           position.set("extraEnableControl", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 545 */       buf.readerIndex(endIndex);
/*     */     } 
/*     */     
/* 548 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PebbellProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */