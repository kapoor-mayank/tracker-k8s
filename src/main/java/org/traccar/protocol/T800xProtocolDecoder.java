/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.math.BigInteger;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class T800xProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  41 */   private short header = 8995; public static final short DEFAULT_HEADER = 8995; public static final int MSG_LOGIN = 1; public static final int MSG_GPS = 2; public static final int MSG_HEARTBEAT = 3; public static final int MSG_ALARM = 4; public static final int MSG_NETWORK = 5;
/*     */   
/*     */   public short getHeader() {
/*  44 */     return this.header;
/*     */   }
/*     */   public static final int MSG_DRIVER_BEHAVIOR_1 = 5; public static final int MSG_DRIVER_BEHAVIOR_2 = 6; public static final int MSG_BLE = 16; public static final int MSG_NETWORK_2 = 17; public static final int MSG_GPS_2 = 19; public static final int MSG_ALARM_2 = 20; public static final int MSG_COMMAND = 129;
/*     */   public T800xProtocolDecoder(Protocol protocol) {
/*  48 */     super(protocol);
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
/*     */   
/*     */   private void sendResponse(Channel channel, short header, int type, int index, ByteBuf imei, int alarm) {
/*  67 */     if (channel != null) {
/*  68 */       ByteBuf response = Unpooled.buffer((alarm > 0) ? 16 : 15);
/*  69 */       response.writeShort(header);
/*  70 */       response.writeByte(type);
/*  71 */       response.writeShort(response.capacity());
/*  72 */       response.writeShort(index);
/*  73 */       response.writeBytes(imei);
/*  74 */       if (alarm > 0) {
/*  75 */         response.writeByte(alarm);
/*     */       }
/*  77 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String decodeAlarm1(int value) {
/*  82 */     switch (value) {
/*     */       case 1:
/*  84 */         return "powerCut";
/*     */       case 2:
/*  86 */         return "lowBattery";
/*     */       case 3:
/*  88 */         return "sos";
/*     */       case 4:
/*  90 */         return "overspeed";
/*     */       case 5:
/*  92 */         return "geofenceEnter";
/*     */       case 6:
/*  94 */         return "geofenceExit";
/*     */       case 7:
/*  96 */         return "tow";
/*     */       case 8:
/*     */       case 10:
/*  99 */         return "vibration";
/*     */       case 21:
/* 101 */         return "jamming";
/*     */       case 23:
/* 103 */         return "powerRestored";
/*     */       case 24:
/* 105 */         return "lowPower";
/*     */     } 
/* 107 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private String decodeAlarm2(int value) {
/* 112 */     switch (value) {
/*     */       case 1:
/*     */       case 4:
/* 115 */         return "removing";
/*     */       case 2:
/* 117 */         return "tampering";
/*     */       case 3:
/* 119 */         return "sos";
/*     */       case 5:
/* 121 */         return "fallDown";
/*     */       case 6:
/* 123 */         return "lowBattery";
/*     */       case 14:
/* 125 */         return "geofenceEnter";
/*     */       case 15:
/* 127 */         return "geofenceExit";
/*     */     } 
/* 129 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Date readDate(ByteBuf buf) {
/* 134 */     return (new DateBuilder())
/* 135 */       .setYear(BcdUtil.readInteger(buf, 2))
/* 136 */       .setMonth(BcdUtil.readInteger(buf, 2))
/* 137 */       .setDay(BcdUtil.readInteger(buf, 2))
/* 138 */       .setHour(BcdUtil.readInteger(buf, 2))
/* 139 */       .setMinute(BcdUtil.readInteger(buf, 2))
/* 140 */       .setSecond(BcdUtil.readInteger(buf, 2))
/* 141 */       .getDate();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 148 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 150 */     this.header = buf.readShort();
/* 151 */     int type = buf.readUnsignedByte();
/* 152 */     buf.readUnsignedShort();
/* 153 */     int index = buf.readUnsignedShort();
/* 154 */     ByteBuf imei = buf.readSlice(8);
/*     */     
/* 156 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] {
/* 157 */           ByteBufUtil.hexDump(imei).substring(1) });
/* 158 */     if (deviceSession == null) {
/* 159 */       return null;
/*     */     }
/*     */     
/* 162 */     boolean positionType = (type == 2 || type == 19 || type == 4 || type == 20);
/* 163 */     if (!positionType) {
/* 164 */       sendResponse(channel, this.header, type, index, imei, 0);
/*     */     }
/*     */     
/* 167 */     if (positionType)
/*     */     {
/* 169 */       return decodePosition(channel, deviceSession, buf, type, index, imei);
/*     */     }
/* 171 */     if ((type == 5 && this.header == 10023) || type == 17) {
/*     */       
/* 173 */       Position position = new Position(getProtocolName());
/* 174 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 176 */       getLastLocation(position, readDate(buf));
/*     */       
/* 178 */       position.set("operator", buf.readCharSequence(buf
/* 179 */             .readUnsignedByte(), StandardCharsets.UTF_16LE).toString());
/* 180 */       position.set("networkTechnology", buf.readCharSequence(buf
/* 181 */             .readUnsignedByte(), StandardCharsets.US_ASCII).toString());
/* 182 */       position.set("networkBand", buf.readCharSequence(buf
/* 183 */             .readUnsignedByte(), StandardCharsets.US_ASCII).toString());
/* 184 */       buf.readCharSequence(buf.readUnsignedByte(), StandardCharsets.US_ASCII);
/* 185 */       position.set("iccid", buf.readCharSequence(buf
/* 186 */             .readUnsignedByte(), StandardCharsets.US_ASCII).toString());
/*     */       
/* 188 */       return position;
/*     */     } 
/* 190 */     if ((type == 5 || type == 6) && this.header == 9766) {
/*     */       
/* 192 */       Position position = new Position(getProtocolName());
/* 193 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 195 */       switch (buf.readUnsignedByte()) {
/*     */         case 0:
/*     */         case 4:
/* 198 */           position.set("alarm", "hardBraking");
/*     */           break;
/*     */         case 1:
/*     */         case 3:
/*     */         case 5:
/* 203 */           position.set("alarm", "hardAcceleration");
/*     */           break;
/*     */         case 2:
/* 206 */           if (type == 5) {
/* 207 */             position.set("alarm", "hardBraking"); break;
/*     */           } 
/* 209 */           position.set("alarm", "hardCornering");
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 216 */       position.setTime(readDate(buf));
/*     */       
/* 218 */       if (type == 6) {
/* 219 */         int status = buf.readUnsignedByte();
/* 220 */         position.setValid(!BitUtil.check(status, 7));
/* 221 */         buf.skipBytes(5);
/*     */       } else {
/* 223 */         position.setValid(true);
/*     */       } 
/*     */       
/* 226 */       position.setAltitude(buf.readFloatLE());
/* 227 */       position.setLongitude(buf.readFloatLE());
/* 228 */       position.setLatitude(buf.readFloatLE());
/* 229 */       position.setSpeed(UnitsConverter.knotsFromKph(BcdUtil.readInteger(buf, 4) * 0.1D));
/* 230 */       position.setCourse(buf.readUnsignedShort());
/*     */       
/* 232 */       position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
/*     */       
/* 234 */       return position;
/*     */     } 
/* 236 */     if (type == 16)
/*     */     {
/* 238 */       return decodeBle(channel, deviceSession, buf, type, index, imei);
/*     */     }
/* 240 */     if (type == 129) {
/*     */       
/* 242 */       Position position = new Position(getProtocolName());
/* 243 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 245 */       getLastLocation(position, null);
/*     */       
/* 247 */       buf.readUnsignedByte();
/*     */       
/* 249 */       position.set("result", buf.toString(StandardCharsets.UTF_16LE));
/*     */       
/* 251 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 255 */     return null;
/*     */   }
/*     */   
/*     */   private double decodeBleTemp(ByteBuf buf) {
/* 259 */     int value = buf.readUnsignedShort();
/* 260 */     return (BitUtil.check(value, 15) ? -BitUtil.to(value, 15) : BitUtil.to(value, 15)) * 0.01D;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeBle(Channel channel, DeviceSession deviceSession, ByteBuf buf, int type, int index, ByteBuf imei) {
/* 266 */     Position position = new Position(getProtocolName());
/* 267 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 269 */     getLastLocation(position, readDate(buf));
/*     */     
/* 271 */     position.set("ignition", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/*     */     
/* 273 */     int i = 1;
/* 274 */     while (buf.isReadable()) {
/* 275 */       switch (buf.readUnsignedShort()) {
/*     */         case 1:
/* 277 */           position.set("tag" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 278 */           position.set("tag" + i + "Battery", Double.valueOf(buf.readUnsignedByte() * 0.01D + 1.22D));
/* 279 */           position.set("tag" + i + "TirePressure", Double.valueOf(buf.readUnsignedByte() * 1.527D * 2.0D));
/* 280 */           position.set("tag" + i + "TireTemp", Integer.valueOf(buf.readUnsignedByte() - 55));
/* 281 */           position.set("tag" + i + "TireStatus", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 2:
/* 284 */           position.set("tag" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 285 */           position.set("tag" + i + "Battery", Double.valueOf(BcdUtil.readInteger(buf, 2) * 0.1D));
/* 286 */           switch (buf.readUnsignedByte()) {
/*     */             case 0:
/* 288 */               position.set("alarm", "sos");
/*     */               break;
/*     */             case 1:
/* 291 */               position.set("alarm", "lowBattery");
/*     */               break;
/*     */           } 
/*     */ 
/*     */           
/* 296 */           buf.readUnsignedByte();
/* 297 */           buf.skipBytes(16);
/*     */           break;
/*     */         case 3:
/* 300 */           position.set("driverUniqueId", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 301 */           position.set("tag" + i + "Battery", Double.valueOf(BcdUtil.readInteger(buf, 2) * 0.1D));
/* 302 */           if (buf.readUnsignedByte() == 1) {
/* 303 */             position.set("alarm", "lowBattery");
/*     */           }
/* 305 */           buf.readUnsignedByte();
/* 306 */           buf.skipBytes(16);
/*     */           break;
/*     */         case 4:
/* 309 */           position.set("tag" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 310 */           position.set("tag" + i + "Battery", Double.valueOf(buf.readUnsignedByte() * 0.01D + 2.0D));
/* 311 */           buf.readUnsignedByte();
/* 312 */           position.set("tag" + i + "Temp", Double.valueOf(decodeBleTemp(buf)));
/* 313 */           position.set("tag" + i + "Humidity", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/* 314 */           position.set("tag" + i + "LightSensor", Integer.valueOf(buf.readUnsignedShort()));
/* 315 */           position.set("tag" + i + "Rssi", Integer.valueOf(buf.readUnsignedByte() - 128));
/*     */           break;
/*     */         case 5:
/* 318 */           position.set("tag" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 319 */           position.set("tag" + i + "Battery", Double.valueOf(buf.readUnsignedByte() * 0.01D + 2.0D));
/* 320 */           buf.readUnsignedByte();
/* 321 */           position.set("tag" + i + "Temp", Double.valueOf(decodeBleTemp(buf)));
/* 322 */           position.set("tag" + i + "Door", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/* 323 */           position.set("tag" + i + "Rssi", Integer.valueOf(buf.readUnsignedByte() - 128));
/*     */           break;
/*     */         case 6:
/* 326 */           position.set("tag" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 327 */           position.set("tag" + i + "Battery", Double.valueOf(buf.readUnsignedByte() * 0.01D + 2.0D));
/* 328 */           position.set("tag" + i + "Output", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/* 329 */           position.set("tag" + i + "Rssi", Integer.valueOf(buf.readUnsignedByte() - 128));
/*     */           break;
/*     */       } 
/*     */ 
/*     */       
/* 334 */       i++;
/*     */     } 
/*     */     
/* 337 */     sendResponse(channel, this.header, type, index, imei, 0);
/*     */     
/* 339 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodePosition(Channel channel, DeviceSession deviceSession, ByteBuf buf, int type, int index, ByteBuf imei) {
/* 345 */     Position position = new Position(getProtocolName());
/* 346 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 348 */     position.set("index", Integer.valueOf(index));
/*     */     
/* 350 */     if (this.header != 10023) {
/*     */       
/* 352 */       buf.readUnsignedShort();
/* 353 */       buf.readUnsignedShort();
/* 354 */       buf.readUnsignedByte();
/* 355 */       buf.readUnsignedShort();
/*     */       
/* 357 */       position.set("rssi", Integer.valueOf(BitUtil.to(buf.readUnsignedShort(), 7)));
/*     */     } 
/*     */ 
/*     */     
/* 361 */     int status = buf.readUnsignedByte();
/* 362 */     position.set("sat", Integer.valueOf(BitUtil.to(status, 5)));
/*     */     
/* 364 */     if (this.header != 10023) {
/*     */       
/* 366 */       buf.readUnsignedByte();
/* 367 */       buf.readUnsignedByte();
/* 368 */       buf.readUnsignedByte();
/* 369 */       buf.readUnsignedByte();
/* 370 */       buf.readUnsignedShort();
/*     */       
/* 372 */       int io = buf.readUnsignedShort();
/* 373 */       position.set("ignition", Boolean.valueOf(BitUtil.check(io, 14)));
/* 374 */       position.set("ac", Boolean.valueOf(BitUtil.check(io, 13)));
/* 375 */       position.set("in3", Boolean.valueOf(BitUtil.check(io, 12)));
/* 376 */       position.set("in4", Boolean.valueOf(BitUtil.check(io, 11)));
/*     */       
/* 378 */       if (type == 19 || type == 20) {
/* 379 */         position.set("output", Short.valueOf(buf.readUnsignedByte()));
/* 380 */         buf.readUnsignedByte();
/*     */       } else {
/* 382 */         position.set("out1", Boolean.valueOf(BitUtil.check(io, 7)));
/* 383 */         position.set("out2", Boolean.valueOf(BitUtil.check(io, 8)));
/* 384 */         position.set("out3", Boolean.valueOf(BitUtil.check(io, 9)));
/*     */       } 
/*     */       
/* 387 */       if (this.header != 9766) {
/* 388 */         int adcCount = (type == 19 || type == 20) ? 5 : 2;
/* 389 */         for (int i = 1; i <= adcCount; i++) {
/* 390 */           String value = ByteBufUtil.hexDump(buf.readSlice(2));
/* 391 */           if (!value.equals("ffff")) {
/* 392 */             position.set("adc" + i, Double.valueOf(Integer.parseInt(value, 16) * 0.01D));
/*     */           }
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 399 */     int alarm = buf.readUnsignedByte();
/* 400 */     position.set("alarm", (this.header != 10023) ? decodeAlarm1(alarm) : decodeAlarm2(alarm));
/*     */     
/* 402 */     if (this.header != 10023) {
/*     */       
/* 404 */       buf.readUnsignedByte();
/*     */       
/* 406 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 408 */       int battery = BcdUtil.readInteger(buf, 2);
/* 409 */       position.set("batteryLevel", Integer.valueOf((battery > 0) ? battery : 100));
/*     */     } 
/*     */ 
/*     */     
/* 413 */     if (BitUtil.check(status, 6)) {
/*     */       
/* 415 */       position.setValid(true);
/* 416 */       position.setTime(readDate(buf));
/* 417 */       position.setAltitude(buf.readFloatLE());
/* 418 */       position.setLongitude(buf.readFloatLE());
/* 419 */       position.setLatitude(buf.readFloatLE());
/* 420 */       position.setSpeed(UnitsConverter.knotsFromKph(BcdUtil.readInteger(buf, 4) * 0.1D));
/* 421 */       position.setCourse(buf.readUnsignedShort());
/*     */     }
/*     */     else {
/*     */       
/* 425 */       getLastLocation(position, readDate(buf));
/*     */       
/* 427 */       int mcc = buf.readUnsignedShortLE();
/* 428 */       int mnc = buf.readUnsignedShortLE();
/*     */       
/* 430 */       if (mcc != 65535 && mnc != 65535) {
/* 431 */         Network network = new Network();
/* 432 */         for (int i = 0; i < 3; i++) {
/* 433 */           network.addCellTower(CellTower.from(mcc, mnc, buf
/* 434 */                 .readUnsignedShortLE(), buf.readUnsignedShortLE()));
/*     */         }
/* 436 */         position.setNetwork(network);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 441 */     if (this.header == 10023) {
/*     */       
/* 443 */       byte[] accelerationBytes = new byte[5];
/* 444 */       buf.readBytes(accelerationBytes);
/* 445 */       long acceleration = (new BigInteger(accelerationBytes)).longValue();
/* 446 */       double accelerationZ = BitUtil.between(acceleration, 8, 15) + BitUtil.between(acceleration, 4, 8) * 0.1D;
/* 447 */       if (!BitUtil.check(acceleration, 15)) {
/* 448 */         accelerationZ = -accelerationZ;
/*     */       }
/* 450 */       double accelerationY = BitUtil.between(acceleration, 20, 27) + BitUtil.between(acceleration, 16, 20) * 0.1D;
/* 451 */       if (!BitUtil.check(acceleration, 27)) {
/* 452 */         accelerationY = -accelerationY;
/*     */       }
/* 454 */       double accelerationX = BitUtil.between(acceleration, 28, 32) + BitUtil.between(acceleration, 32, 39) * 0.1D;
/* 455 */       if (!BitUtil.check(acceleration, 39)) {
/* 456 */         accelerationX = -accelerationX;
/*     */       }
/* 458 */       position.set("gSensor", "[" + accelerationX + "," + accelerationY + "," + accelerationZ + "]");
/*     */       
/* 460 */       int battery = BcdUtil.readInteger(buf, 2);
/* 461 */       position.set("batteryLevel", Integer.valueOf((battery > 0) ? battery : 100));
/* 462 */       position.set("deviceTemp", Integer.valueOf(buf.readByte()));
/* 463 */       position.set("lightSensor", Double.valueOf(BcdUtil.readInteger(buf, 2) * 0.1D));
/* 464 */       position.set("battery", Double.valueOf(BcdUtil.readInteger(buf, 2) * 0.1D));
/* 465 */       position.set("solarPanel", Double.valueOf(BcdUtil.readInteger(buf, 2) * 0.1D));
/* 466 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 468 */       int inputStatus = buf.readUnsignedShort();
/* 469 */       position.set("ignition", Boolean.valueOf(BitUtil.check(inputStatus, 2)));
/* 470 */       position.set("rssi", Integer.valueOf(BitUtil.between(inputStatus, 4, 11)));
/*     */       
/* 472 */       buf.readUnsignedShort();
/* 473 */       buf.readUnsignedInt();
/* 474 */       buf.readUnsignedByte();
/* 475 */       buf.readUnsignedShort();
/* 476 */       buf.readUnsignedByte();
/*     */     }
/*     */     else {
/*     */       
/* 480 */       if (buf.readableBytes() >= 2) {
/* 481 */         position.set("power", Double.valueOf(BcdUtil.readInteger(buf, 4) * 0.01D));
/*     */       }
/* 483 */       if (buf.readableBytes() >= 19) {
/* 484 */         position.set("obdSpeed", Double.valueOf(BcdUtil.readInteger(buf, 4) * 0.01D));
/* 485 */         position.set("fuelUsed", Double.valueOf(buf.readUnsignedInt() * 0.001D));
/* 486 */         position.set("fuelConsumption", Double.valueOf(buf.readUnsignedInt() * 0.001D));
/* 487 */         position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
/*     */         
/* 489 */         int value = buf.readUnsignedByte();
/* 490 */         if (value != 255) {
/* 491 */           position.set("airInput", Integer.valueOf(value));
/*     */         }
/* 493 */         if (value != 255) {
/* 494 */           position.set("airPressure", Integer.valueOf(value));
/*     */         }
/* 496 */         if (value != 255) {
/* 497 */           position.set("coolantTemp", Integer.valueOf(value - 40));
/*     */         }
/* 499 */         if (value != 255) {
/* 500 */           position.set("airTemp", Integer.valueOf(value - 40));
/*     */         }
/* 502 */         if (value != 255) {
/* 503 */           position.set("engineLoad", Integer.valueOf(value));
/*     */         }
/* 505 */         if (value != 255) {
/* 506 */           position.set("throttle", Integer.valueOf(value));
/*     */         }
/* 508 */         if (value != 255) {
/* 509 */           position.set("fuel", Integer.valueOf(value));
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 514 */     if (type == 4 || type == 20) {
/* 515 */       sendResponse(channel, this.header, type, index, imei, alarm);
/*     */     }
/*     */     
/* 518 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T800xProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */