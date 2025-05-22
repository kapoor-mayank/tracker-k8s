/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.TimeZone;
/*     */ import java.util.stream.Collectors;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.BufferUtil;
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
/*     */ 
/*     */ 
/*     */ public class SuntechProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private boolean universal;
/*     */   private String prefix;
/*     */   private int protocolType;
/*     */   private boolean hbm;
/*     */   private boolean includeAdc;
/*     */   private boolean includeRpm;
/*     */   private boolean includeTemp;
/*     */   private ByteBuf crash;
/*     */   
/*     */   public SuntechProtocolDecoder(Protocol protocol) {
/*  61 */     super(protocol);
/*     */   }
/*     */   
/*     */   public boolean getUniversal() {
/*  65 */     return this.universal;
/*     */   }
/*     */   
/*     */   public String getPrefix() {
/*  69 */     return this.prefix;
/*     */   }
/*     */   
/*     */   public void setProtocolType(int protocolType) {
/*  73 */     this.protocolType = protocolType;
/*     */   }
/*     */   
/*     */   public int getProtocolType(long deviceId) {
/*  77 */     return Context.getIdentityManager().lookupAttributeInteger(deviceId, 
/*  78 */         getProtocolName() + ".protocolType", this.protocolType, true);
/*     */   }
/*     */   
/*     */   public void setHbm(boolean hbm) {
/*  82 */     this.hbm = hbm;
/*     */   }
/*     */   
/*     */   public boolean isHbm(long deviceId) {
/*  86 */     return Context.getIdentityManager().lookupAttributeBoolean(deviceId, 
/*  87 */         getProtocolName() + ".hbm", this.hbm, true);
/*     */   }
/*     */   
/*     */   public void setIncludeAdc(boolean includeAdc) {
/*  91 */     this.includeAdc = includeAdc;
/*     */   }
/*     */   
/*     */   public boolean isIncludeAdc(long deviceId) {
/*  95 */     return Context.getIdentityManager().lookupAttributeBoolean(deviceId, 
/*  96 */         getProtocolName() + ".includeAdc", this.includeAdc, true);
/*     */   }
/*     */   
/*     */   public void setIncludeRpm(boolean includeRpm) {
/* 100 */     this.includeRpm = includeRpm;
/*     */   }
/*     */   
/*     */   public boolean isIncludeRpm(long deviceId) {
/* 104 */     return Context.getIdentityManager().lookupAttributeBoolean(deviceId, 
/* 105 */         getProtocolName() + ".includeRpm", this.includeRpm, true);
/*     */   }
/*     */   
/*     */   public void setIncludeTemp(boolean includeTemp) {
/* 109 */     this.includeTemp = includeTemp;
/*     */   }
/*     */   
/*     */   public boolean isIncludeTemp(long deviceId) {
/* 113 */     return Context.getIdentityManager().lookupAttributeBoolean(deviceId, 
/* 114 */         getProtocolName() + ".includeTemp", this.includeTemp, true);
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decode9(Channel channel, SocketAddress remoteAddress, String[] values) throws ParseException {
/* 119 */     int index = 1;
/*     */     
/* 121 */     String type = values[index++];
/*     */     
/* 123 */     if (!type.equals("Location") && !type.equals("Emergency") && !type.equals("Alert")) {
/* 124 */       return null;
/*     */     }
/*     */     
/* 127 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[index++] });
/* 128 */     if (deviceSession == null) {
/* 129 */       return null;
/*     */     }
/*     */     
/* 132 */     Position position = new Position(getProtocolName());
/* 133 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 135 */     if (type.equals("Emergency") || type.equals("Alert")) {
/* 136 */       position.set("alarm", "general");
/*     */     }
/*     */     
/* 139 */     if (!type.equals("Alert") || getProtocolType(deviceSession.getDeviceId()) == 0) {
/* 140 */       position.set("versionFw", values[index++]);
/*     */     }
/*     */     
/* 143 */     DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
/* 144 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 145 */     position.setTime(dateFormat.parse(values[index++] + values[index++]));
/*     */     
/* 147 */     if (getProtocolType(deviceSession.getDeviceId()) == 1) {
/* 148 */       index++;
/*     */     }
/*     */     
/* 151 */     position.setLatitude(Double.parseDouble(values[index++]));
/* 152 */     position.setLongitude(Double.parseDouble(values[index++]));
/* 153 */     position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
/* 154 */     position.setCourse(Double.parseDouble(values[index++]));
/*     */     
/* 156 */     position.setValid(values[index++].equals("1"));
/*     */     
/* 158 */     if (getProtocolType(deviceSession.getDeviceId()) == 1) {
/* 159 */       position.set("odometer", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     }
/*     */     
/* 162 */     return position;
/*     */   }
/*     */   
/*     */   private String decodeEmergency(int value) {
/* 166 */     switch (value) {
/*     */       case 1:
/* 168 */         return "sos";
/*     */       case 2:
/* 170 */         return "parking";
/*     */       case 3:
/* 172 */         return "powerCut";
/*     */       case 5:
/*     */       case 6:
/* 175 */         return "door";
/*     */       case 7:
/* 177 */         return "movement";
/*     */       case 8:
/* 179 */         return "vibration";
/*     */     } 
/* 181 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private String decodeAlert(int value) {
/* 186 */     switch (value) {
/*     */       case 1:
/* 188 */         return "overspeed";
/*     */       case 2:
/* 190 */         return "overspeedEnd";
/*     */       case 5:
/* 192 */         return "geofenceExit";
/*     */       case 6:
/* 194 */         return "geofenceEnter";
/*     */       case 9:
/* 196 */         return "sleepBegin";
/*     */       case 10:
/* 198 */         return "sleepEnd";
/*     */       case 13:
/* 200 */         return "batteryError";
/*     */       case 14:
/* 202 */         return "lowBattery";
/*     */       case 15:
/* 204 */         return "vibration";
/*     */       case 16:
/* 206 */         return "accident";
/*     */       case 33:
/* 208 */         return "ignitionOn";
/*     */       case 34:
/* 210 */         return "ignitionOff";
/*     */       case 40:
/* 212 */         return "powerRestored";
/*     */       case 41:
/* 214 */         return "powerCut";
/*     */       case 42:
/* 216 */         return "sos";
/*     */       case 44:
/* 218 */         return "batteryConnected";
/*     */       case 45:
/* 220 */         return "batteryDisconnected";
/*     */       case 46:
/* 222 */         return "hardAcceleration";
/*     */       case 47:
/* 224 */         return "hardBraking";
/*     */       case 50:
/* 226 */         return "jamming";
/*     */       case 80:
/* 228 */         return "obdDisconnected";
/*     */       case 81:
/* 230 */         return "obdConnected";
/*     */       case 83:
/* 232 */         return "obdCodeDetected";
/*     */       case 84:
/* 234 */         return "obdCodeCleared";
/*     */       case 85:
/* 236 */         return "highRpmBegin";
/*     */       case 86:
/* 238 */         return "highRpmEnd";
/*     */       case 87:
/* 240 */         return "highSpeedLowRpm";
/*     */       case 88:
/* 242 */         return "lowSpeedHighRpm";
/*     */       case 132:
/* 244 */         return "door";
/*     */     } 
/* 246 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decode4(Channel channel, SocketAddress remoteAddress, String[] values) throws ParseException {
/* 251 */     int index = 0;
/*     */     
/* 253 */     String type = values[index++].substring(5);
/*     */     
/* 255 */     if (!type.equals("STT") && !type.equals("ALT")) {
/* 256 */       return null;
/*     */     }
/*     */     
/* 259 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[index++] });
/* 260 */     if (deviceSession == null) {
/* 261 */       return null;
/*     */     }
/*     */     
/* 264 */     Position position = new Position(getProtocolName());
/* 265 */     position.setDeviceId(deviceSession.getDeviceId());
/* 266 */     position.set("type", type);
/*     */     
/* 268 */     position.set("versionFw", values[index++]);
/* 269 */     index++;
/*     */     
/* 271 */     Network network = new Network();
/*     */     
/* 273 */     for (int i = 0; i < 7; i++) {
/* 274 */       int lac, rssi, cid = Integer.parseInt(values[index++]);
/* 275 */       int mcc = Integer.parseInt(values[index++]);
/* 276 */       int mnc = Integer.parseInt(values[index++]);
/*     */       
/* 278 */       if (i == 0) {
/* 279 */         rssi = Integer.parseInt(values[index++]);
/* 280 */         lac = Integer.parseInt(values[index++]);
/*     */       } else {
/* 282 */         lac = Integer.parseInt(values[index++]);
/* 283 */         rssi = Integer.parseInt(values[index++]);
/*     */       } 
/* 285 */       index++;
/* 286 */       if (cid > 0) {
/* 287 */         network.addCellTower(CellTower.from(mcc, mnc, lac, cid, rssi));
/*     */       }
/*     */     } 
/*     */     
/* 291 */     position.setNetwork(network);
/*     */     
/* 293 */     position.set("battery", Double.valueOf(Double.parseDouble(values[index++])));
/* 294 */     position.set("archive", values[index++].equals("0") ? Boolean.valueOf(true) : null);
/* 295 */     position.set("index", Integer.valueOf(Integer.parseInt(values[index++])));
/* 296 */     position.set("status", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     
/* 298 */     if (values[index].length() == 3) {
/* 299 */       index++;
/*     */     }
/*     */     
/* 302 */     if (values[index].isEmpty()) {
/*     */       
/* 304 */       getLastLocation(position, null);
/*     */     }
/*     */     else {
/*     */       
/* 308 */       DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
/* 309 */       dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 310 */       position.setTime(dateFormat.parse(values[index++] + values[index++]));
/*     */       
/* 312 */       position.setLatitude(Double.parseDouble(values[index++]));
/* 313 */       position.setLongitude(Double.parseDouble(values[index++]));
/* 314 */       position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
/* 315 */       position.setCourse(Double.parseDouble(values[index++]));
/*     */       
/* 317 */       position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */       
/* 319 */       position.setValid(values[index++].equals("1"));
/*     */     } 
/*     */ 
/*     */     
/* 323 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private int decodeSerialData(Position position, String[] values, int index) {
/* 328 */     int remaining = Integer.parseInt(values[index++]);
/* 329 */     double totalFuel = 0.0D;
/* 330 */     while (remaining > 0) {
/* 331 */       String attribute = values[index++];
/* 332 */       if (attribute.startsWith("CabAVL")) {
/* 333 */         String[] data = attribute.split(",");
/* 334 */         double fuel1 = Double.parseDouble(data[2]);
/* 335 */         if (fuel1 > 0.0D) {
/* 336 */           totalFuel += fuel1;
/* 337 */           position.set("fuel1", Double.valueOf(fuel1));
/*     */         } 
/* 339 */         double fuel2 = Double.parseDouble(data[3]);
/* 340 */         if (fuel2 > 0.0D) {
/* 341 */           totalFuel += fuel2;
/* 342 */           position.set("fuel2", Double.valueOf(fuel2));
/*     */         } 
/* 344 */       } else if (attribute.startsWith("GTSL")) {
/* 345 */         String[] driverValues = attribute.split("\\|");
/* 346 */         position.set("driverUniqueId", driverValues[4]);
/* 347 */         position.set("journey", Integer.valueOf(Integer.parseInt(driverValues[5])));
/* 348 */       } else if (attribute.contains("=")) {
/* 349 */         String[] pair = attribute.split("=");
/* 350 */         if (pair.length >= 2) {
/* 351 */           int fuel; String value = pair[1].trim();
/* 352 */           if (value.contains(".")) {
/* 353 */             value = value.substring(0, value.indexOf('.'));
/*     */           }
/* 355 */           switch (pair[0].charAt(0)) {
/*     */             case 't':
/* 357 */               position.set("temp" + pair[0].charAt(2), Integer.valueOf(Integer.parseInt(value, 16)));
/*     */               break;
/*     */             case 'N':
/* 360 */               fuel = Integer.parseInt(value, 16);
/* 361 */               totalFuel += fuel;
/* 362 */               position.set("fuel" + pair[0].charAt(2), Integer.valueOf(fuel));
/*     */               break;
/*     */             case 'Q':
/* 365 */               position.set("drivingQuality", Integer.valueOf(Integer.parseInt(value, 16)));
/*     */               break;
/*     */           } 
/*     */ 
/*     */         
/*     */         } 
/*     */       } else {
/* 372 */         position.set("serial", attribute.trim());
/*     */       } 
/* 374 */       remaining -= attribute.length() + 1;
/*     */     } 
/* 376 */     if (totalFuel > 0.0D) {
/* 377 */       position.set("fuel", Double.valueOf(totalFuel));
/*     */     }
/* 379 */     return index + 1;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decode2356(Channel channel, SocketAddress remoteAddress, String protocol, String[] values) throws ParseException {
/* 384 */     int index = 0;
/*     */     
/* 386 */     String type = values[index++].substring(5);
/*     */     
/* 388 */     if (!type.equals("STT") && !type.equals("EMG") && !type.equals("EVT") && 
/* 389 */       !type.equals("ALT") && !type.equals("UEX")) {
/* 390 */       return null;
/*     */     }
/*     */     
/* 393 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[index++] });
/* 394 */     if (deviceSession == null) {
/* 395 */       return null;
/*     */     }
/*     */     
/* 398 */     Position position = new Position(getProtocolName());
/* 399 */     position.setDeviceId(deviceSession.getDeviceId());
/* 400 */     position.set("type", type);
/*     */     
/* 402 */     if (protocol.startsWith("ST3") || protocol.equals("ST500") || protocol.equals("ST600")) {
/* 403 */       index++;
/*     */     }
/*     */     
/* 406 */     position.set("versionFw", values[index++]);
/*     */     
/* 408 */     DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
/* 409 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 410 */     position.setTime(dateFormat.parse(values[index++] + values[index++]));
/*     */     
/* 412 */     if (!protocol.equals("ST500")) {
/* 413 */       long cid = Long.parseLong(values[index++], 16);
/* 414 */       if (protocol.equals("ST600")) {
/* 415 */         position.setNetwork(new Network(CellTower.from(
/* 416 */                 Integer.parseInt(values[index++]), Integer.parseInt(values[index++]), 
/* 417 */                 Integer.parseInt(values[index++], 16), cid, Integer.parseInt(values[index++]))));
/*     */       }
/*     */     } 
/*     */     
/* 421 */     position.setLatitude(Double.parseDouble(values[index++]));
/* 422 */     position.setLongitude(Double.parseDouble(values[index++]));
/* 423 */     position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
/* 424 */     position.setCourse(Double.parseDouble(values[index++]));
/*     */     
/* 426 */     position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     
/* 428 */     position.setValid(values[index++].equals("1"));
/*     */     
/* 430 */     position.set("odometer", Integer.valueOf(Integer.parseInt(values[index++])));
/* 431 */     position.set("power", Double.valueOf(Double.parseDouble(values[index++])));
/*     */     
/* 433 */     String io = values[index++];
/* 434 */     if (io.length() >= 6) {
/* 435 */       position.set("ignition", Boolean.valueOf((io.charAt(0) == '1')));
/* 436 */       position.set("in1", Boolean.valueOf((io.charAt(1) == '1')));
/* 437 */       position.set("in2", Boolean.valueOf((io.charAt(2) == '1')));
/* 438 */       position.set("in3", Boolean.valueOf((io.charAt(3) == '1')));
/* 439 */       position.set("out1", Boolean.valueOf((io.charAt(4) == '1')));
/* 440 */       position.set("out2", Boolean.valueOf((io.charAt(5) == '1')));
/*     */     } 
/*     */     
/* 443 */     switch (type) {
/*     */       case "STT":
/* 445 */         position.set("status", Integer.valueOf(Integer.parseInt(values[index++])));
/* 446 */         position.set("index", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */         break;
/*     */       case "EMG":
/* 449 */         position.set("alarm", decodeEmergency(Integer.parseInt(values[index++])));
/*     */         break;
/*     */       case "EVT":
/* 452 */         position.set("event", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */         break;
/*     */       case "ALT":
/* 455 */         position.set("alarm", decodeAlert(Integer.parseInt(values[index++])));
/*     */         break;
/*     */       case "UEX":
/* 458 */         index = decodeSerialData(position, values, index);
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 464 */     if (isHbm(deviceSession.getDeviceId())) {
/*     */       
/* 466 */       if (index < values.length) {
/* 467 */         position.set("hours", Long.valueOf(UnitsConverter.msFromMinutes(Integer.parseInt(values[index++]))));
/*     */       }
/*     */       
/* 470 */       if (index < values.length) {
/* 471 */         position.set("battery", Double.valueOf(Double.parseDouble(values[index++])));
/*     */       }
/*     */       
/* 474 */       if (index < values.length && values[index++].equals("0")) {
/* 475 */         position.set("archive", Boolean.valueOf(true));
/*     */       }
/*     */       
/* 478 */       if (isIncludeAdc(deviceSession.getDeviceId())) {
/* 479 */         for (int i = 1; i <= 3; i++) {
/* 480 */           if (index < values.length && !values[index++].isEmpty()) {
/* 481 */             position.set("adc" + i, Double.valueOf(Double.parseDouble(values[index - 1])));
/*     */           }
/*     */         } 
/*     */       }
/*     */       
/* 486 */       if (isIncludeRpm(deviceSession.getDeviceId()) && index < values.length) {
/* 487 */         position.set("rpm", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */       }
/*     */       
/* 490 */       if (values.length - index >= 2) {
/* 491 */         String driverUniqueId = values[index++];
/* 492 */         if (values[index++].equals("1") && !driverUniqueId.isEmpty()) {
/* 493 */           position.set("driverUniqueId", driverUniqueId);
/*     */         }
/*     */       } 
/*     */       
/* 497 */       if (isIncludeTemp(deviceSession.getDeviceId())) {
/* 498 */         for (int i = 1; i <= 3; i++) {
/* 499 */           String temperature = values[index++];
/* 500 */           String value = temperature.substring(temperature.indexOf(':') + 1);
/* 501 */           if (!value.isEmpty()) {
/* 502 */             position.set("temp" + i, Double.valueOf(Double.parseDouble(value)));
/*     */           }
/*     */         } 
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 510 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeUniversal(Channel channel, SocketAddress remoteAddress, String[] values) throws ParseException {
/* 515 */     int mask, index = 0;
/*     */     
/* 517 */     String type = values[index++];
/*     */     
/* 519 */     if (!type.equals("STT") && !type.equals("ALT") && !type.equals("BLE") && !type.equals("RES") && 
/* 520 */       !type.equals("UEX")) {
/* 521 */       return null;
/*     */     }
/*     */     
/* 524 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[index++] });
/* 525 */     if (deviceSession == null) {
/* 526 */       return null;
/*     */     }
/*     */     
/* 529 */     Position position = new Position(getProtocolName());
/* 530 */     position.setDeviceId(deviceSession.getDeviceId());
/* 531 */     position.set("type", type);
/*     */     
/* 533 */     if (type.equals("RES")) {
/* 534 */       getLastLocation(position, null);
/* 535 */       position.set("result", 
/*     */           
/* 537 */           Arrays.<CharSequence>stream((CharSequence[])values, index, values.length).collect(Collectors.joining(";")));
/* 538 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 542 */     if (type.equals("BLE")) {
/* 543 */       mask = 6198;
/*     */     } else {
/* 545 */       mask = Integer.parseInt(values[index++], 16);
/*     */     } 
/*     */     
/* 548 */     if (BitUtil.check(mask, 1)) {
/* 549 */       index++;
/*     */     }
/*     */     
/* 552 */     if (BitUtil.check(mask, 2)) {
/* 553 */       position.set("versionFw", values[index++]);
/*     */     }
/*     */     
/* 556 */     if (BitUtil.check(mask, 3) && values[index++].equals("0")) {
/* 557 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/* 560 */     if (BitUtil.check(mask, 4) && BitUtil.check(mask, 5)) {
/* 561 */       DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
/* 562 */       dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 563 */       position.setTime(dateFormat.parse(values[index++] + values[index++]));
/*     */     } 
/*     */     
/* 566 */     CellTower cellTower = new CellTower();
/* 567 */     if (BitUtil.check(mask, 6)) {
/* 568 */       cellTower.setCellId(Long.valueOf(Long.parseLong(values[index++], 16)));
/*     */     }
/* 570 */     if (BitUtil.check(mask, 7)) {
/* 571 */       cellTower.setMobileCountryCode(Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     }
/* 573 */     if (BitUtil.check(mask, 8)) {
/* 574 */       cellTower.setMobileNetworkCode(Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     }
/* 576 */     if (BitUtil.check(mask, 9)) {
/* 577 */       cellTower.setLocationAreaCode(Integer.valueOf(Integer.parseInt(values[index++], 16)));
/*     */     }
/* 579 */     if (cellTower.getCellId() != null) {
/* 580 */       position.setNetwork(new Network(cellTower));
/*     */     }
/*     */     
/* 583 */     if (BitUtil.check(mask, 10)) {
/* 584 */       position.set("rssi", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     }
/*     */     
/* 587 */     if (BitUtil.check(mask, 11)) {
/* 588 */       position.setLatitude(Double.parseDouble(values[index++]));
/*     */     }
/*     */     
/* 591 */     if (BitUtil.check(mask, 12)) {
/* 592 */       position.setLongitude(Double.parseDouble(values[index++]));
/*     */     }
/*     */     
/* 595 */     if (type.equals("BLE")) {
/*     */       
/* 597 */       position.setValid(true);
/*     */       
/* 599 */       int count = Integer.parseInt(values[index++]);
/*     */       
/* 601 */       for (int i = 1; i <= count; i++) {
/* 602 */         position.set("tag" + i + "Rssi", Integer.valueOf(Integer.parseInt(values[index++])));
/* 603 */         index++;
/* 604 */         index++;
/* 605 */         position.set("tag" + i + "Id", values[index++]);
/* 606 */         position.set("tag" + i + "Samples", Integer.valueOf(Integer.parseInt(values[index++])));
/* 607 */         position.set("tag" + i + "Major", Integer.valueOf(Integer.parseInt(values[index++])));
/* 608 */         position.set("tag" + i + "Minor", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */       }
/*     */     
/*     */     } else {
/*     */       
/* 613 */       if (BitUtil.check(mask, 13)) {
/* 614 */         position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
/*     */       }
/*     */       
/* 617 */       if (BitUtil.check(mask, 14)) {
/* 618 */         position.setCourse(Double.parseDouble(values[index++]));
/*     */       }
/*     */       
/* 621 */       if (BitUtil.check(mask, 15)) {
/* 622 */         position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */       }
/*     */       
/* 625 */       if (BitUtil.check(mask, 16)) {
/* 626 */         position.setValid(values[index++].equals("1"));
/*     */       }
/*     */       
/* 629 */       if (BitUtil.check(mask, 17)) {
/* 630 */         int input = Integer.parseInt(values[index++]);
/* 631 */         position.set("ignition", Boolean.valueOf(BitUtil.check(input, 0)));
/* 632 */         position.set("input", Integer.valueOf(input));
/*     */       } 
/*     */       
/* 635 */       if (BitUtil.check(mask, 18)) {
/* 636 */         position.set("output", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */       }
/*     */       
/* 639 */       switch (type) {
/*     */         case "ALT":
/* 641 */           if (BitUtil.check(mask, 19)) {
/* 642 */             int alertId = Integer.parseInt(values[index++]);
/* 643 */             position.set("alarm", decodeAlert(alertId));
/* 644 */             position.set("alertId", Integer.valueOf(alertId));
/*     */           } 
/* 646 */           if (BitUtil.check(mask, 20)) {
/* 647 */             position.set("alertModifier", values[index++]);
/*     */           }
/* 649 */           if (BitUtil.check(mask, 21)) {
/* 650 */             position.set("alertData", values[index++]);
/*     */           }
/*     */           break;
/*     */         case "UEX":
/* 654 */           index = decodeSerialData(position, values, index);
/*     */           break;
/*     */         default:
/* 657 */           if (BitUtil.check(mask, 19)) {
/* 658 */             position.set("mode", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           }
/* 660 */           if (BitUtil.check(mask, 20)) {
/* 661 */             position.set("reason", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           }
/* 663 */           if (BitUtil.check(mask, 21)) {
/* 664 */             position.set("index", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           }
/*     */           break;
/*     */       } 
/*     */       
/* 669 */       if (BitUtil.check(mask, 22)) {
/* 670 */         index++;
/*     */       }
/*     */       
/* 673 */       if (BitUtil.check(mask, 23) && !type.equals("UEX")) {
/* 674 */         int assignMask = Integer.parseInt(values[index++], 16);
/* 675 */         for (int i = 0; i <= 30; i++) {
/* 676 */           if (BitUtil.check(assignMask, i)) {
/* 677 */             position.set("io" + (i + 1), values[index++]);
/*     */           }
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 684 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeBinary(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 690 */     int type = buf.readUnsignedByte();
/* 691 */     buf.readUnsignedShort();
/*     */     
/* 693 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { ByteBufUtil.hexDump(buf.readSlice(5)) });
/* 694 */     if (deviceSession == null) {
/* 695 */       return null;
/*     */     }
/*     */     
/* 698 */     Position position = new Position(getProtocolName());
/* 699 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 701 */     int mask = buf.readUnsignedMedium();
/*     */     
/* 703 */     if (BitUtil.check(mask, 1)) {
/* 704 */       buf.readUnsignedByte();
/*     */     }
/*     */     
/* 707 */     if (BitUtil.check(mask, 2)) {
/* 708 */       position.set("versionFw", String.format("%d.%d.%d", new Object[] {
/* 709 */               Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte())
/*     */             }));
/*     */     }
/* 712 */     if (BitUtil.check(mask, 3) && buf.readUnsignedByte() == 0) {
/* 713 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/* 716 */     if (BitUtil.check(mask, 4) && BitUtil.check(mask, 5)) {
/* 717 */       position.setTime((new DateBuilder())
/* 718 */           .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
/* 719 */           .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
/* 720 */           .getDate());
/*     */     }
/*     */     
/* 723 */     if (BitUtil.check(mask, 6)) {
/* 724 */       buf.readUnsignedInt();
/*     */     }
/*     */     
/* 727 */     if (BitUtil.check(mask, 7)) {
/* 728 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 731 */     if (BitUtil.check(mask, 8)) {
/* 732 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 735 */     if (BitUtil.check(mask, 9)) {
/* 736 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 739 */     if (BitUtil.check(mask, 10)) {
/* 740 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 743 */     if (BitUtil.check(mask, 11)) {
/* 744 */       position.setLatitude(BufferUtil.readSignedMagnitudeInt(buf) / 1000000.0D);
/*     */     }
/*     */     
/* 747 */     if (BitUtil.check(mask, 12)) {
/* 748 */       position.setLongitude(BufferUtil.readSignedMagnitudeInt(buf) / 1000000.0D);
/*     */     }
/*     */     
/* 751 */     if (BitUtil.check(mask, 13)) {
/* 752 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() / 100.0D));
/*     */     }
/*     */     
/* 755 */     if (BitUtil.check(mask, 14)) {
/* 756 */       position.setCourse(buf.readUnsignedShort() / 100.0D);
/*     */     }
/*     */     
/* 759 */     if (BitUtil.check(mask, 15)) {
/* 760 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 763 */     if (BitUtil.check(mask, 16)) {
/* 764 */       position.setValid((buf.readUnsignedByte() > 0));
/*     */     }
/*     */     
/* 767 */     if (BitUtil.check(mask, 17)) {
/* 768 */       int input = buf.readUnsignedByte();
/* 769 */       position.set("ignition", Boolean.valueOf(BitUtil.check(input, 0)));
/* 770 */       position.set("input", Integer.valueOf(input));
/*     */     } 
/*     */     
/* 773 */     if (BitUtil.check(mask, 18)) {
/* 774 */       position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 777 */     int alertId = 0;
/* 778 */     if (BitUtil.check(mask, 19)) {
/* 779 */       alertId = buf.readUnsignedByte();
/* 780 */       if (type == 130) {
/* 781 */         position.set("alarm", decodeAlert(alertId));
/*     */       }
/*     */     } 
/*     */     
/* 785 */     if (BitUtil.check(mask, 20)) {
/* 786 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 789 */     if (BitUtil.check(mask, 21) && alertId == 59) {
/* 790 */       position.set("driverUniqueId", ByteBufUtil.hexDump(buf.readSlice(8)));
/*     */     }
/*     */     
/* 793 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeTravelReport(Channel channel, SocketAddress remoteAddress, String[] values) {
/* 797 */     int index = 1;
/*     */     
/* 799 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[index++] });
/* 800 */     if (deviceSession == null) {
/* 801 */       return null;
/*     */     }
/*     */     
/* 804 */     Position position = new Position(getProtocolName());
/* 805 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 807 */     getLastLocation(position, null);
/*     */     
/* 809 */     position.set("driverUniqueId", values[values.length - 1]);
/*     */     
/* 811 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Collection<Position> decodeCrashReport(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 816 */     if (buf.getByte(buf.readerIndex() + 3) != 59) {
/* 817 */       return null;
/*     */     }
/*     */     
/* 820 */     String[] values = buf.readCharSequence(23, StandardCharsets.US_ASCII).toString().split(";");
/*     */     
/* 822 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[1] });
/* 823 */     if (deviceSession == null) {
/* 824 */       return null;
/*     */     }
/*     */     
/* 827 */     int currentIndex = Integer.parseInt(values[2]);
/* 828 */     int totalIndex = Integer.parseInt(values[3]);
/*     */     
/* 830 */     if (this.crash == null) {
/* 831 */       this.crash = Unpooled.buffer();
/*     */     }
/*     */     
/* 834 */     this.crash.writeBytes(buf.readSlice(buf.readableBytes() - 3));
/*     */     
/* 836 */     if (currentIndex == totalIndex) {
/*     */       
/* 838 */       LinkedList<Position> positions = new LinkedList<>();
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 843 */       Date crashTime = (new DateBuilder()).setDate(this.crash.readUnsignedByte(), this.crash.readUnsignedByte(), this.crash.readUnsignedByte()).setTime(this.crash.readUnsignedByte(), this.crash.readUnsignedByte(), this.crash.readUnsignedByte()).getDate();
/*     */       
/* 845 */       List<Date> times = Arrays.asList(new Date[] { new Date(crashTime
/* 846 */               .getTime() - 3000L), new Date(crashTime
/* 847 */               .getTime() - 2000L), new Date(crashTime
/* 848 */               .getTime() - 1000L), new Date(crashTime
/* 849 */               .getTime() + 1000L) });
/*     */       
/* 851 */       for (Date time : times) {
/*     */         
/* 853 */         Position position = new Position(getProtocolName());
/* 854 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 856 */         position.setValid(true);
/* 857 */         position.setTime(time);
/* 858 */         position.setLatitude(this.crash.readIntLE() * 1.0E-7D);
/* 859 */         position.setLongitude(this.crash.readIntLE() * 1.0E-7D);
/* 860 */         position.setSpeed(UnitsConverter.knotsFromKph(this.crash.readUnsignedShort() * 0.01D));
/* 861 */         position.setCourse(this.crash.readUnsignedShort() * 0.01D);
/*     */         
/* 863 */         StringBuilder value = new StringBuilder("[");
/* 864 */         for (int i = 0; i < 100; i++) {
/* 865 */           if (value.length() > 1) {
/* 866 */             value.append(",");
/*     */           }
/* 868 */           value.append("[");
/* 869 */           value.append(this.crash.readShortLE());
/* 870 */           value.append(",");
/* 871 */           value.append(this.crash.readShortLE());
/* 872 */           value.append(",");
/* 873 */           value.append(this.crash.readShortLE());
/* 874 */           value.append("]");
/*     */         } 
/* 876 */         value.append("]");
/*     */         
/* 878 */         position.set("gSensor", value.toString());
/*     */         
/* 880 */         positions.add(position);
/*     */       } 
/*     */ 
/*     */       
/* 884 */       this.crash.release();
/* 885 */       this.crash = null;
/*     */       
/* 887 */       return positions;
/*     */     } 
/*     */ 
/*     */     
/* 891 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 901 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 903 */     if (buf.getByte(buf.readerIndex() + 1) == 0) {
/*     */       
/* 905 */       this.universal = true;
/* 906 */       return decodeBinary(channel, remoteAddress, buf);
/*     */     } 
/*     */ 
/*     */     
/* 910 */     String[] values = buf.toString(StandardCharsets.US_ASCII).split(";", -1);
/* 911 */     this.prefix = values[0];
/*     */     
/* 913 */     if (this.prefix.equals("CRR"))
/* 914 */       return decodeCrashReport(channel, remoteAddress, buf); 
/* 915 */     if (this.prefix.length() < 5) {
/* 916 */       this.universal = true;
/* 917 */       return decodeUniversal(channel, remoteAddress, values);
/* 918 */     }  if (this.prefix.endsWith("HTE"))
/* 919 */       return decodeTravelReport(channel, remoteAddress, values); 
/* 920 */     if (this.prefix.startsWith("ST9"))
/* 921 */       return decode9(channel, remoteAddress, values); 
/* 922 */     if (this.prefix.startsWith("ST4")) {
/* 923 */       return decode4(channel, remoteAddress, values);
/*     */     }
/* 925 */     return decode2356(channel, remoteAddress, this.prefix.substring(0, 5), values);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SuntechProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */