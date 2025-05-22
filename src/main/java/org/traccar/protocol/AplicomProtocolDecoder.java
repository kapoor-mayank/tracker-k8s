/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AplicomProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final long IMEI_BASE_TC65_V20 = 355631999483904L;
/*     */   private static final long IMEI_BASE_TC65_V28 = 358244010000000L;
/*     */   private static final long IMEI_BASE_TC65I_V11 = 353234015223808L;
/*  38 */   private static final Logger LOGGER = LoggerFactory.getLogger(AplicomProtocolDecoder.class);
/*     */   
/*     */   public AplicomProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*     */   }
/*     */ 
/*     */   
/*     */   private static final int DEFAULT_SELECTOR_D = 764;
/*     */   private static final int DEFAULT_SELECTOR_E = 32764;
/*     */   
/*     */   private static boolean validateImei(long imei) {
/*  49 */     return (Checksum.luhn(imei / 10L) == imei % 10L);
/*     */   }
/*     */   private static final int DEFAULT_SELECTOR_F = 2045; private static final int EVENT_DATA = 119;
/*     */   
/*     */   private static long imeiFromUnitId(long unitId) {
/*  54 */     if (unitId == 0L)
/*     */     {
/*  56 */       return 0L;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*  61 */     long imei = 353234015223808L + unitId;
/*  62 */     if (validateImei(imei)) {
/*  63 */       return imei;
/*     */     }
/*     */ 
/*     */     
/*  67 */     imei = 358244010000000L + (unitId + 688512L & 0xFFFFFFL);
/*  68 */     if (validateImei(imei)) {
/*  69 */       return imei;
/*     */     }
/*     */ 
/*     */     
/*  73 */     imei = 355631999483904L + unitId;
/*  74 */     if (validateImei(imei)) {
/*  75 */       return imei;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*  80 */     return unitId;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeEventData(Position position, ByteBuf buf, int event) {
/*  90 */     switch (event) {
/*     */       case 2:
/*     */       case 40:
/*  93 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 9:
/*  96 */         buf.readUnsignedMedium();
/*     */         break;
/*     */       case 31:
/*     */       case 32:
/* 100 */         buf.readUnsignedShort();
/*     */         break;
/*     */       case 38:
/* 103 */         buf.skipBytes(36);
/*     */         break;
/*     */       case 113:
/* 106 */         buf.readUnsignedInt();
/* 107 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 119:
/* 110 */         position.set("eventData", ByteBufUtil.hexDump(buf, buf
/* 111 */               .readerIndex(), Math.min(buf.readableBytes(), 1024)));
/*     */         break;
/*     */       case 121:
/*     */       case 142:
/* 115 */         buf.readLong();
/*     */         break;
/*     */       case 130:
/* 118 */         buf.readUnsignedInt();
/*     */         break;
/*     */       case 188:
/* 121 */         decodeEB(position, buf);
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeCanData(ByteBuf buf, Position position) {
/* 130 */     buf.readUnsignedMedium();
/* 131 */     position.set("versionFw", Short.valueOf(buf.readUnsignedByte()));
/* 132 */     int count = buf.readUnsignedByte();
/* 133 */     buf.readUnsignedByte();
/* 134 */     buf.readUnsignedShort();
/* 135 */     buf.readUnsignedInt();
/*     */     
/* 137 */     buf.skipBytes(8);
/*     */     
/* 139 */     ArrayList<ByteBuf> values = new ArrayList<>(count);
/*     */     int i;
/* 141 */     for (i = 0; i < count; i++) {
/* 142 */       values.add(buf.readSlice(8));
/*     */     }
/*     */     
/* 145 */     for (i = 0; i < count; i++) {
/* 146 */       ByteBuf value = values.get(i);
/* 147 */       switch (buf.readInt()) {
/*     */         case 525:
/* 149 */           position.set("rpm", Short.valueOf(value.readShortLE()));
/* 150 */           position.set("dieselTemperature", Double.valueOf(value.readShortLE() * 0.1D));
/* 151 */           position.set("batteryVoltage", Double.valueOf(value.readShortLE() * 0.01D));
/* 152 */           position.set("supplyAirTempDep1", Double.valueOf(value.readShortLE() * 0.1D));
/*     */           break;
/*     */         case 781:
/* 155 */           position.set("activeAlarm", ByteBufUtil.hexDump(value));
/*     */           break;
/*     */         case 1036:
/* 158 */           position.set("airTempDep1", Double.valueOf(value.readShortLE() * 0.1D));
/* 159 */           position.set("airTempDep2", Double.valueOf(value.readShortLE() * 0.1D));
/*     */           break;
/*     */         case 1037:
/* 162 */           position.set("coldUnitState", ByteBufUtil.hexDump(value));
/*     */           break;
/*     */         case 1292:
/* 165 */           position.set("defrostTempDep1", Double.valueOf(value.readShortLE() * 0.1D));
/* 166 */           position.set("defrostTempDep2", Double.valueOf(value.readShortLE() * 0.1D));
/*     */           break;
/*     */         case 1293:
/* 169 */           position.set("condenserPressure", Double.valueOf(value.readShortLE() * 0.1D));
/* 170 */           position.set("suctionPressure", Double.valueOf(value.readShortLE() * 0.1D));
/*     */           break;
/*     */         case 1420:
/* 173 */           value.readByte();
/* 174 */           value.readShort();
/* 175 */           switch (value.readByte()) {
/*     */             case 1:
/* 177 */               position.set("setpointZone1", Double.valueOf(value.readIntLE() * 0.1D));
/*     */               break;
/*     */             case 2:
/* 180 */               position.set("setpointZone2", Double.valueOf(value.readIntLE() * 0.1D));
/*     */               break;
/*     */             case 5:
/* 183 */               position.set("unitType", Integer.valueOf(value.readIntLE()));
/*     */               break;
/*     */             case 19:
/* 186 */               position.set("dieselHours", Integer.valueOf(value.readIntLE() / 60 / 60));
/*     */               break;
/*     */             case 20:
/* 189 */               position.set("electricHours", Integer.valueOf(value.readIntLE() / 60 / 60));
/*     */               break;
/*     */             case 23:
/* 192 */               position.set("serviceIndicator", Integer.valueOf(value.readIntLE()));
/*     */               break;
/*     */             case 24:
/* 195 */               position.set("softwareVersion", Double.valueOf(value.readIntLE() * 0.01D));
/*     */               break;
/*     */           } 
/*     */           
/*     */           break;
/*     */         
/*     */         default:
/* 202 */           LOGGER.warn("Aplicom CAN decoding error", new UnsupportedOperationException());
/*     */           break;
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeD(Position position, ByteBuf buf, int selector, int event) {
/* 210 */     if ((selector & 0x8) != 0) {
/* 211 */       position.setValid(((buf.readUnsignedByte() & 0x40) != 0));
/*     */     } else {
/* 213 */       getLastLocation(position, null);
/*     */     } 
/*     */     
/* 216 */     if ((selector & 0x4) != 0) {
/* 217 */       position.setDeviceTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */     }
/*     */     
/* 220 */     if ((selector & 0x8) != 0) {
/* 221 */       position.setFixTime(new Date(buf.readUnsignedInt() * 1000L));
/* 222 */       if (position.getDeviceTime() == null) {
/* 223 */         position.setDeviceTime(position.getFixTime());
/*     */       }
/* 225 */       position.setLatitude(buf.readInt() / 1000000.0D);
/* 226 */       position.setLongitude(buf.readInt() / 1000000.0D);
/* 227 */       position.set("satVisible", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 230 */     if ((selector & 0x10) != 0) {
/* 231 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/* 232 */       position.set("maximumSpeed", Short.valueOf(buf.readUnsignedByte()));
/* 233 */       position.setCourse(buf.readUnsignedByte() * 2.0D);
/*     */     } 
/*     */     
/* 236 */     if ((selector & 0x40) != 0) {
/* 237 */       position.set("input", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 240 */     if ((selector & 0x20) != 0) {
/* 241 */       position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
/* 242 */       position.set("adc2", Integer.valueOf(buf.readUnsignedShort()));
/* 243 */       position.set("adc3", Integer.valueOf(buf.readUnsignedShort()));
/* 244 */       position.set("adc4", Integer.valueOf(buf.readUnsignedShort()));
/*     */     } 
/*     */     
/* 247 */     if ((selector & 0x8000) != 0) {
/* 248 */       position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/* 249 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */     } 
/*     */ 
/*     */     
/* 253 */     if ((selector & 0x10000) != 0) {
/* 254 */       buf.readUnsignedShort();
/* 255 */       buf.readUnsignedInt();
/*     */     } 
/*     */ 
/*     */     
/* 259 */     if ((selector & 0x20000) != 0) {
/* 260 */       buf.readUnsignedShort();
/* 261 */       buf.readUnsignedInt();
/*     */     } 
/*     */     
/* 264 */     if ((selector & 0x80) != 0) {
/* 265 */       position.set("trip1", Long.valueOf(buf.readUnsignedInt()));
/*     */     }
/*     */     
/* 268 */     if ((selector & 0x100) != 0) {
/* 269 */       position.set("trip2", Long.valueOf(buf.readUnsignedInt()));
/*     */     }
/*     */     
/* 272 */     if ((selector & 0x40) != 0) {
/* 273 */       position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 276 */     if ((selector & 0x200) != 0) {
/* 277 */       position.set("driverUniqueId", 
/* 278 */           String.valueOf(buf.readUnsignedShort() << 32L) + buf.readUnsignedInt());
/*     */     }
/*     */     
/* 281 */     if ((selector & 0x400) != 0) {
/* 282 */       buf.readUnsignedByte();
/*     */     }
/*     */     
/* 285 */     if ((selector & 0x800) != 0) {
/* 286 */       position.setAltitude(buf.readShort());
/*     */     }
/*     */     
/* 289 */     if ((selector & 0x2000) != 0) {
/* 290 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 293 */     if ((selector & 0x4000) != 0) {
/* 294 */       buf.skipBytes(8);
/*     */     }
/*     */     
/* 297 */     if ((selector & 0x80000) != 0) {
/* 298 */       buf.skipBytes(11);
/*     */     }
/*     */     
/* 301 */     if ((selector & 0x1000) != 0) {
/* 302 */       decodeEventData(position, buf, event);
/*     */     }
/*     */     
/* 305 */     if (Context.getConfig().getBoolean(getProtocolName() + ".can") && buf
/* 306 */       .isReadable() && (selector & 0x1000) != 0 && event == 119) {
/* 307 */       decodeCanData(buf, position);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeE(Position position, ByteBuf buf, int selector) {
/* 313 */     if ((selector & 0x8) != 0) {
/* 314 */       position.set("tachographEvent", Integer.valueOf(buf.readUnsignedShort()));
/*     */     }
/*     */     
/* 317 */     if ((selector & 0x4) != 0) {
/* 318 */       getLastLocation(position, new Date(buf.readUnsignedInt() * 1000L));
/*     */     } else {
/* 320 */       getLastLocation(position, null);
/*     */     } 
/*     */     
/* 323 */     if ((selector & 0x10) != 0) {
/*     */ 
/*     */       
/* 326 */       String time = buf.readUnsignedByte() + "s " + buf.readUnsignedByte() + "m " + buf.readUnsignedByte() + "h " + buf.readUnsignedByte() + "M " + buf.readUnsignedByte() + "D " + buf.readUnsignedByte() + "Y " + buf.readByte() + "m " + buf.readByte() + "h";
/* 327 */       position.set("tachographTime", time);
/*     */     } 
/*     */     
/* 330 */     position.set("workState", Short.valueOf(buf.readUnsignedByte()));
/* 331 */     position.set("driver1State", Short.valueOf(buf.readUnsignedByte()));
/* 332 */     position.set("driver2State", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 334 */     if ((selector & 0x20) != 0) {
/* 335 */       position.set("tachographStatus", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 338 */     if ((selector & 0x40) != 0) {
/* 339 */       position.set("obdSpeed", Double.valueOf(buf.readUnsignedShort() / 256.0D));
/*     */     }
/*     */     
/* 342 */     if ((selector & 0x80) != 0) {
/* 343 */       position.set("obdOdometer", Long.valueOf(buf.readUnsignedInt() * 5L));
/*     */     }
/*     */     
/* 346 */     if ((selector & 0x100) != 0) {
/* 347 */       position.set("tripOdometer", Long.valueOf(buf.readUnsignedInt() * 5L));
/*     */     }
/*     */     
/* 350 */     if ((selector & 0x8000) != 0) {
/* 351 */       position.set("kFactor", (buf.readUnsignedShort() * 0.001D) + " pulses/m");
/*     */     }
/*     */     
/* 354 */     if ((selector & 0x200) != 0) {
/* 355 */       position.set("rpm", Double.valueOf(buf.readUnsignedShort() * 0.125D));
/*     */     }
/*     */     
/* 358 */     if ((selector & 0x400) != 0) {
/* 359 */       position.set("extraInfo", Integer.valueOf(buf.readUnsignedShort()));
/*     */     }
/*     */     
/* 362 */     if ((selector & 0x800) != 0) {
/* 363 */       position.set("vin", buf.readSlice(18).toString(StandardCharsets.US_ASCII).trim());
/*     */     }
/*     */     
/* 366 */     if ((selector & 0x2000) != 0) {
/* 367 */       buf.readUnsignedByte();
/* 368 */       buf.readUnsignedByte();
/* 369 */       String card = buf.readSlice(20).toString(StandardCharsets.US_ASCII).trim();
/* 370 */       if (!card.isEmpty()) {
/* 371 */         position.set("card1", card);
/*     */       }
/*     */     } 
/*     */     
/* 375 */     if ((selector & 0x4000) != 0) {
/* 376 */       buf.readUnsignedByte();
/* 377 */       buf.readUnsignedByte();
/* 378 */       String card = buf.readSlice(20).toString(StandardCharsets.US_ASCII).trim();
/* 379 */       if (!card.isEmpty()) {
/* 380 */         position.set("card2", card);
/*     */       }
/*     */     } 
/*     */     
/* 384 */     if ((selector & 0x10000) != 0) {
/* 385 */       int count = buf.readUnsignedByte();
/* 386 */       for (int i = 1; i <= count; i++) {
/* 387 */         position.set("driver" + i, buf.readSlice(22).toString(StandardCharsets.US_ASCII).trim());
/* 388 */         position.set("driverTime" + i, Long.valueOf(buf.readUnsignedInt()));
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeH(Position position, ByteBuf buf, int selector) {
/* 395 */     if ((selector & 0x4) != 0) {
/* 396 */       getLastLocation(position, new Date(buf.readUnsignedInt() * 1000L));
/*     */     } else {
/* 398 */       getLastLocation(position, null);
/*     */     } 
/*     */     
/* 401 */     if ((selector & 0x40) != 0) {
/* 402 */       buf.readUnsignedInt();
/*     */     }
/*     */     
/* 405 */     if ((selector & 0x2000) != 0) {
/* 406 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 409 */     int index = 1;
/* 410 */     while (buf.readableBytes() > 0) {
/*     */       
/* 412 */       position.set("h" + index + "Index", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 414 */       buf.readUnsignedShort();
/*     */       
/* 416 */       int n = buf.readUnsignedByte();
/* 417 */       int m = buf.readUnsignedByte();
/*     */       
/* 419 */       position.set("h" + index + "XLength", Integer.valueOf(n));
/* 420 */       position.set("h" + index + "YLength", Integer.valueOf(m));
/*     */       
/* 422 */       if ((selector & 0x8) != 0) {
/* 423 */         position.set("h" + index + "XType", Short.valueOf(buf.readUnsignedByte()));
/* 424 */         position.set("h" + index + "YType", Short.valueOf(buf.readUnsignedByte()));
/* 425 */         position.set("h" + index + "Parameters", Short.valueOf(buf.readUnsignedByte()));
/*     */       } 
/*     */       
/* 428 */       boolean percentageFormat = ((selector & 0x20) != 0);
/*     */       
/* 430 */       StringBuilder data = new StringBuilder();
/* 431 */       for (int i = 0; i < n * m; i++) {
/* 432 */         if (percentageFormat) {
/* 433 */           data.append(buf.readUnsignedByte() * 0.5D).append("%").append(" ");
/*     */         } else {
/* 435 */           data.append(buf.readUnsignedShort()).append(" ");
/*     */         } 
/*     */       } 
/*     */       
/* 439 */       position.set("h" + index + "Data", data.toString().trim());
/*     */       
/* 441 */       position.set("h" + index + "Total", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 443 */       if ((selector & 0x10) != 0) {
/* 444 */         int k = buf.readUnsignedByte();
/*     */         
/* 446 */         data = new StringBuilder(); int j;
/* 447 */         for (j = 1; j < n; j++) {
/* 448 */           if (k == 1) {
/* 449 */             data.append(buf.readByte()).append(" ");
/* 450 */           } else if (k == 2) {
/* 451 */             data.append(buf.readShort()).append(" ");
/*     */           } 
/*     */         } 
/* 454 */         position.set("h" + index + "XLimits", data.toString().trim());
/*     */         
/* 456 */         data = new StringBuilder();
/* 457 */         for (j = 1; j < m; j++) {
/* 458 */           if (k == 1) {
/* 459 */             data.append(buf.readByte()).append(" ");
/* 460 */           } else if (k == 2) {
/* 461 */             data.append(buf.readShort()).append(" ");
/*     */           } 
/*     */         } 
/* 464 */         position.set("h" + index + "YLimits", data.toString().trim());
/*     */       } 
/*     */       
/* 467 */       index++;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeEB(Position position, ByteBuf buf) {
/* 473 */     if (buf.readByte() != 69 || buf.readByte() != 66) {
/*     */       return;
/*     */     }
/*     */     
/* 477 */     position.set("versionFw", Short.valueOf(buf.readUnsignedByte()));
/* 478 */     position.set("event", Integer.valueOf(buf.readUnsignedShort()));
/* 479 */     position.set("dataValidity", Short.valueOf(buf.readUnsignedByte()));
/* 480 */     position.set("towed", Short.valueOf(buf.readUnsignedByte()));
/* 481 */     buf.readUnsignedShort();
/*     */     
/* 483 */     while (buf.readableBytes() > 0) {
/* 484 */       buf.readUnsignedByte();
/* 485 */       int type = buf.readUnsignedByte();
/* 486 */       int length = buf.readUnsignedByte();
/* 487 */       int end = buf.readerIndex() + length;
/*     */       
/* 489 */       switch (type) {
/*     */         case 1:
/* 491 */           position.set("brakeFlags", ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */           break;
/*     */         case 2:
/* 494 */           position.set("wheelSpeed", Double.valueOf(buf.readUnsignedShort() / 256.0D));
/* 495 */           position.set("wheelSpeedDifference", Double.valueOf(buf.readUnsignedShort() / 256.0D - 125.0D));
/* 496 */           position.set("lateralAcceleration", Double.valueOf(buf.readUnsignedByte() / 10.0D - 12.5D));
/* 497 */           position.set("vehicleSpeed", Double.valueOf(buf.readUnsignedShort() / 256.0D));
/*     */           break;
/*     */         case 3:
/* 500 */           position.set("axleWeight", Integer.valueOf(buf.readUnsignedShort() * 2));
/*     */           break;
/*     */         case 4:
/* 503 */           position.set("tirePressure", Integer.valueOf(buf.readUnsignedByte() * 10));
/* 504 */           position.set("pneumaticPressure", Integer.valueOf(buf.readUnsignedByte() * 5));
/*     */           break;
/*     */         case 5:
/* 507 */           position.set("brakeLining", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/* 508 */           position.set("brakeTemperature", Integer.valueOf(buf.readUnsignedByte() * 10));
/*     */           break;
/*     */         case 6:
/* 511 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 5L));
/* 512 */           position.set("tripOdometer", Long.valueOf(buf.readUnsignedInt() * 5L));
/* 513 */           position.set("serviceOdometer", Long.valueOf((buf.readUnsignedInt() - 2105540607L) * 5L));
/*     */           break;
/*     */         case 10:
/* 516 */           position.set("absStatusCounter", Integer.valueOf(buf.readUnsignedShort()));
/* 517 */           position.set("atvbStatusCounter", Integer.valueOf(buf.readUnsignedShort()));
/* 518 */           position.set("vdcActiveCounter", Integer.valueOf(buf.readUnsignedShort()));
/*     */           break;
/*     */         case 11:
/* 521 */           position.set("brakeMinMaxData", ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */           break;
/*     */         case 12:
/* 524 */           position.set("missingPgn", ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */           break;
/*     */         case 13:
/* 527 */           buf.readUnsignedByte();
/* 528 */           position.set("towedDetectionStatus", Long.valueOf(buf.readUnsignedInt()));
/* 529 */           buf.skipBytes(17);
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 536 */       buf.readerIndex(end);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeF(Position position, ByteBuf buf, int selector) {
/* 542 */     Date deviceTime = null;
/*     */     
/* 544 */     if ((selector & 0x4) != 0) {
/* 545 */       deviceTime = new Date(buf.readUnsignedInt() * 1000L);
/*     */     }
/*     */     
/* 548 */     getLastLocation(position, deviceTime);
/*     */     
/* 550 */     buf.readUnsignedByte();
/*     */     
/* 552 */     if ((selector & 0x8) != 0) {
/* 553 */       position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
/* 554 */       position.set("rpmMax", Integer.valueOf(buf.readUnsignedShort()));
/* 555 */       position.set("rpmMin", Integer.valueOf(buf.readUnsignedShort()));
/*     */     } 
/*     */     
/* 558 */     if ((selector & 0x10) != 0) {
/* 559 */       position.set("engineTemp", Short.valueOf(buf.readShort()));
/* 560 */       position.set("engineTempMax", Short.valueOf(buf.readShort()));
/* 561 */       position.set("engineTempMin", Short.valueOf(buf.readShort()));
/*     */     } 
/*     */     
/* 564 */     if ((selector & 0x20) != 0) {
/* 565 */       position.set("hours", Long.valueOf(UnitsConverter.msFromHours(buf.readUnsignedInt())));
/* 566 */       position.set("serviceDistance", Integer.valueOf(buf.readInt()));
/* 567 */       position.set("driverActivity", Short.valueOf(buf.readUnsignedByte()));
/* 568 */       position.set("throttle", Short.valueOf(buf.readUnsignedByte()));
/* 569 */       position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 572 */     if ((selector & 0x40) != 0) {
/* 573 */       position.set("fuelUsed", Long.valueOf(buf.readUnsignedInt()));
/*     */     }
/*     */     
/* 576 */     if ((selector & 0x80) != 0) {
/* 577 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */     }
/*     */     
/* 580 */     if ((selector & 0x100) != 0) {
/* 581 */       position.set("obdSpeed", Short.valueOf(buf.readUnsignedByte()));
/* 582 */       position.set("speedMax", Short.valueOf(buf.readUnsignedByte()));
/* 583 */       position.set("speedMin", Short.valueOf(buf.readUnsignedByte()));
/* 584 */       position.set("hardBraking", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 587 */     if ((selector & 0x200) != 0) {
/* 588 */       position.set("tachographSpeed", Short.valueOf(buf.readUnsignedByte()));
/* 589 */       position.set("driver1State", Short.valueOf(buf.readUnsignedByte()));
/* 590 */       position.set("driver2State", Short.valueOf(buf.readUnsignedByte()));
/* 591 */       position.set("tachographStatus", Short.valueOf(buf.readUnsignedByte()));
/* 592 */       position.set("overspeedCount", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 595 */     if ((selector & 0x800) != 0) {
/* 596 */       position.set("hours", Double.valueOf(buf.readUnsignedInt() * 0.05D));
/* 597 */       position.set("rpm", Double.valueOf(buf.readUnsignedShort() * 0.125D));
/* 598 */       position.set("obdSpeed", Double.valueOf(buf.readUnsignedShort() / 256.0D));
/* 599 */       position.set("fuelUsed", Double.valueOf(buf.readUnsignedInt() * 0.5D));
/* 600 */       position.set("fuel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/*     */     } 
/*     */     
/* 603 */     if ((selector & 0x1000) != 0) {
/* 604 */       position.set("ambientTemperature", Double.valueOf(buf.readUnsignedShort() * 0.03125D - 273.0D));
/* 605 */       buf.readUnsignedShort();
/* 606 */       position.set("fuelEconomy", Double.valueOf(buf.readUnsignedShort() / 512.0D));
/* 607 */       position.set("fuelConsumption", Double.valueOf(buf.readUnsignedInt() * 0.001D));
/* 608 */       buf.readUnsignedByte();
/*     */     } 
/*     */     
/* 611 */     if ((selector & 0x2000) != 0) {
/* 612 */       buf.skipBytes(buf.readUnsignedByte());
/*     */     }
/*     */     
/* 615 */     if ((selector & 0x4000) != 0) {
/* 616 */       position.set("torque", Short.valueOf(buf.readUnsignedByte()));
/* 617 */       position.set("brakePressure1", Integer.valueOf(buf.readUnsignedByte() * 8));
/* 618 */       position.set("brakePressure2", Integer.valueOf(buf.readUnsignedByte() * 8));
/* 619 */       position.set("grossWeight", Integer.valueOf(buf.readUnsignedShort() * 10));
/* 620 */       position.set("exhaustFluid", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/* 621 */       buf.readUnsignedByte();
/* 622 */       position.set("retarderTorque", Short.valueOf(buf.readUnsignedByte()));
/* 623 */       position.set("retarderSelection", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/* 624 */       buf.skipBytes(8);
/* 625 */       buf.skipBytes(8);
/* 626 */       buf.skipBytes(8);
/* 627 */       buf.skipBytes(8);
/*     */     } 
/*     */     
/* 630 */     if ((selector & 0x8000) != 0) {
/* 631 */       position.set("parkingBrakeStatus", Short.valueOf(buf.readUnsignedByte()));
/* 632 */       position.set("doorStatus", Short.valueOf(buf.readUnsignedByte()));
/* 633 */       buf.skipBytes(8);
/* 634 */       position.set("alternatorStatus", Short.valueOf(buf.readUnsignedByte()));
/* 635 */       position.set("selectedGear", Short.valueOf(buf.readUnsignedByte()));
/* 636 */       position.set("currentGear", Short.valueOf(buf.readUnsignedByte()));
/* 637 */       buf.skipBytes(8);
/*     */     } 
/*     */     
/* 640 */     if ((selector & 0x400) != 0) {
/* 641 */       int count = buf.readUnsignedByte();
/* 642 */       for (int i = 0; i < count; i++) {
/* 643 */         position.set("axle" + i, Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     String imei;
/* 652 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 654 */     char protocol = (char)buf.readByte();
/* 655 */     int version = buf.readUnsignedByte();
/*     */ 
/*     */     
/* 658 */     if ((version & 0x80) != 0) {
/* 659 */       imei = String.valueOf(buf.readUnsignedInt() << 24L | buf.readUnsignedMedium());
/*     */     } else {
/* 661 */       imei = String.valueOf(imeiFromUnitId(buf.readUnsignedMedium()));
/*     */     } 
/*     */     
/* 664 */     buf.readUnsignedShort();
/*     */     
/* 666 */     int selector = 764;
/* 667 */     if (protocol == 'E') {
/* 668 */       selector = 32764;
/* 669 */     } else if (protocol == 'F') {
/* 670 */       selector = 2045;
/*     */     } 
/* 672 */     if ((version & 0x40) != 0) {
/* 673 */       selector = buf.readUnsignedMedium();
/*     */     }
/*     */     
/* 676 */     Position position = new Position(getProtocolName());
/* 677 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 678 */     if (deviceSession == null) {
/* 679 */       return null;
/*     */     }
/* 681 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 683 */     int event = buf.readUnsignedByte();
/* 684 */     position.set("event", Integer.valueOf(event));
/* 685 */     position.set("eventInfo", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 687 */     if (protocol == 'D') {
/* 688 */       decodeD(position, buf, selector, event);
/* 689 */     } else if (protocol == 'E') {
/* 690 */       decodeE(position, buf, selector);
/* 691 */     } else if (protocol == 'H') {
/* 692 */       decodeH(position, buf, selector);
/* 693 */     } else if (protocol == 'F') {
/* 694 */       decodeF(position, buf, selector);
/*     */     } else {
/* 696 */       return null;
/*     */     } 
/*     */     
/* 699 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AplicomProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */