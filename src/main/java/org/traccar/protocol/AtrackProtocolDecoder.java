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
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TimeZone;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DataConverter;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ 
/*     */ public class AtrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final int MIN_DATA_LENGTH = 40;
/*     */   private boolean longDate;
/*     */   private final boolean decimalFuel;
/*     */   private boolean custom;
/*     */   private String form;
/*     */   private ByteBuf photo;
/*  62 */   private final Map<Integer, String> alarmMap = new HashMap<>();
/*     */   
/*     */   public AtrackProtocolDecoder(Protocol protocol) {
/*  65 */     super(protocol);
/*     */     
/*  67 */     this.longDate = Context.getConfig().getBoolean(getProtocolName() + ".longDate");
/*  68 */     this.decimalFuel = Context.getConfig().getBoolean(getProtocolName() + ".decimalFuel");
/*     */     
/*  70 */     this.custom = Context.getConfig().getBoolean(getProtocolName() + ".custom");
/*  71 */     this.form = Context.getConfig().getString(getProtocolName() + ".form");
/*  72 */     if (this.form != null) {
/*  73 */       this.custom = true;
/*     */     }
/*     */     
/*  76 */     for (String pair : Context.getConfig().getString(getProtocolName() + ".alarmMap", "").split(",")) {
/*  77 */       if (!pair.isEmpty()) {
/*  78 */         this.alarmMap.put(
/*  79 */             Integer.valueOf(Integer.parseInt(pair.substring(0, pair.indexOf('=')))), pair.substring(pair.indexOf('=') + 1));
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public void setLongDate(boolean longDate) {
/*  85 */     this.longDate = longDate;
/*     */   }
/*     */   
/*     */   public void setCustom(boolean custom) {
/*  89 */     this.custom = custom;
/*     */   }
/*     */   
/*     */   public void setForm(String form) {
/*  93 */     this.form = form;
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, long rawId, int index) {
/*  97 */     if (channel != null) {
/*  98 */       ByteBuf response = Unpooled.buffer(12);
/*  99 */       response.writeShort(65026);
/* 100 */       response.writeLong(rawId);
/* 101 */       response.writeShort(index);
/* 102 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private static String readString(ByteBuf buf) {
/* 107 */     String result = null;
/* 108 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0);
/* 109 */     if (index > buf.readerIndex()) {
/* 110 */       result = buf.readSlice(index - buf.readerIndex()).toString(StandardCharsets.US_ASCII);
/*     */     }
/* 112 */     buf.readByte();
/* 113 */     return result;
/*     */   }
/*     */   
/*     */   private void decodeBeaconData(Position position, int mode, int mask, ByteBuf data) {
/* 117 */     int i = 1;
/* 118 */     while (data.isReadable()) {
/* 119 */       if (BitUtil.check(mask, 7)) {
/* 120 */         position.set("tag" + i + "Id", ByteBufUtil.hexDump(data.readSlice(6)));
/*     */       }
/* 122 */       switch (mode) {
/*     */         case 1:
/* 124 */           if (BitUtil.check(mask, 6)) {
/* 125 */             data.readUnsignedShort();
/*     */           }
/* 127 */           if (BitUtil.check(mask, 5)) {
/* 128 */             data.readUnsignedShort();
/*     */           }
/* 130 */           if (BitUtil.check(mask, 4)) {
/* 131 */             data.readUnsignedByte();
/*     */           }
/* 133 */           if (BitUtil.check(mask, 3)) {
/* 134 */             position.set("tag" + i + "Rssi", Short.valueOf(data.readUnsignedByte()));
/*     */           }
/*     */           break;
/*     */         case 2:
/* 138 */           if (BitUtil.check(mask, 6)) {
/* 139 */             data.readUnsignedShort();
/*     */           }
/* 141 */           if (BitUtil.check(mask, 5)) {
/* 142 */             position.set("tag" + i + "Temp", Integer.valueOf(data.readUnsignedShort()));
/*     */           }
/* 144 */           if (BitUtil.check(mask, 4)) {
/* 145 */             data.readUnsignedByte();
/*     */           }
/* 147 */           if (BitUtil.check(mask, 3)) {
/* 148 */             position.set("tag" + i + "Rssi", Short.valueOf(data.readUnsignedByte()));
/*     */           }
/*     */           break;
/*     */         case 3:
/* 152 */           if (BitUtil.check(mask, 6)) {
/* 153 */             position.set("tag" + i + "Humidity", Integer.valueOf(data.readUnsignedShort()));
/*     */           }
/* 155 */           if (BitUtil.check(mask, 5)) {
/* 156 */             position.set("tag" + i + "Temp", Integer.valueOf(data.readUnsignedShort()));
/*     */           }
/* 158 */           if (BitUtil.check(mask, 3)) {
/* 159 */             position.set("tag" + i + "Rssi", Short.valueOf(data.readUnsignedByte()));
/*     */           }
/* 161 */           if (BitUtil.check(mask, 2)) {
/* 162 */             data.readUnsignedShort();
/*     */           }
/*     */           break;
/*     */         case 4:
/* 166 */           if (BitUtil.check(mask, 6)) {
/* 167 */             int hardwareId = data.readUnsignedByte();
/* 168 */             if (BitUtil.check(mask, 5)) {
/* 169 */               switch (hardwareId) {
/*     */                 case 1:
/*     */                 case 4:
/* 172 */                   data.skipBytes(11);
/*     */                   break;
/*     */                 case 2:
/* 175 */                   data.skipBytes(2);
/*     */                   break;
/*     */                 case 3:
/* 178 */                   data.skipBytes(6);
/*     */                   break;
/*     */                 case 5:
/* 181 */                   data.skipBytes(10);
/*     */                   break;
/*     */               } 
/*     */ 
/*     */             
/*     */             }
/*     */           } 
/* 188 */           if (BitUtil.check(mask, 4)) {
/* 189 */             data.skipBytes(9);
/*     */           }
/*     */           break;
/*     */       } 
/*     */ 
/*     */       
/* 195 */       i++;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void readTextCustomData(Position position, String data, String form) {
/* 200 */     CellTower cellTower = new CellTower();
/* 201 */     String[] keys = form.substring(1).split("%");
/* 202 */     String[] values = data.split(",|\r\n");
/* 203 */     for (int i = 0; i < Math.min(keys.length, values.length); i++) {
/* 204 */       String[] beaconValues; switch (keys[i]) {
/*     */         case "SA":
/* 206 */           position.set("sat", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "MV":
/* 209 */           position.set("power", Double.valueOf(Integer.parseInt(values[i]) * 0.1D));
/*     */           break;
/*     */         case "BV":
/* 212 */           position.set("battery", Double.valueOf(Integer.parseInt(values[i]) * 0.1D));
/*     */           break;
/*     */         case "GQ":
/* 215 */           cellTower.setSignalStrength(Integer.valueOf(Integer.parseInt(values[i])));
/* 216 */           position.set("rssi", cellTower.getSignalStrength());
/*     */           break;
/*     */         case "CE":
/* 219 */           cellTower.setCellId(Long.valueOf(Long.parseLong(values[i])));
/*     */           break;
/*     */         case "LC":
/* 222 */           cellTower.setLocationAreaCode(Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "CN":
/* 225 */           if (values[i].length() > 3) {
/* 226 */             cellTower.setMobileCountryCode(Integer.valueOf(Integer.parseInt(values[i].substring(0, 3))));
/* 227 */             cellTower.setMobileNetworkCode(Integer.valueOf(Integer.parseInt(values[i].substring(3))));
/*     */           } 
/*     */           break;
/*     */         case "PC":
/* 231 */           position.set("count1", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "AT":
/* 234 */           position.setAltitude(Integer.parseInt(values[i]));
/*     */           break;
/*     */         case "RP":
/* 237 */           position.set("rpm", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "GS":
/* 240 */           position.set("rssi", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "DT":
/* 243 */           position.set("archive", Boolean.valueOf((Integer.parseInt(values[i]) == 1)));
/*     */           break;
/*     */         case "VN":
/* 246 */           position.set("vin", values[i]);
/*     */           break;
/*     */         case "TR":
/* 249 */           position.set("throttle", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "ET":
/* 252 */           position.set("temp1", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "FL":
/* 255 */           position.set("fuel", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "FC":
/* 258 */           position.set("fuelConsumption", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "AV1":
/* 261 */           position.set("adc1", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "CD":
/* 264 */           position.set("iccid", values[i]);
/*     */           break;
/*     */         case "EH":
/* 267 */           position.set("hours", Long.valueOf(UnitsConverter.msFromHours(Integer.parseInt(values[i]))));
/*     */           break;
/*     */         case "BC":
/* 270 */           beaconValues = values[i].split(":");
/* 271 */           decodeBeaconData(position, 
/* 272 */               Integer.parseInt(beaconValues[0]), Integer.parseInt(beaconValues[1]), 
/* 273 */               Unpooled.wrappedBuffer(DataConverter.parseHex(beaconValues[2])));
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 280 */     if (cellTower.getMobileCountryCode() != null && cellTower
/* 281 */       .getMobileNetworkCode() != null && cellTower
/* 282 */       .getCellId() != null && cellTower
/* 283 */       .getLocationAreaCode() != null) {
/* 284 */       position.setNetwork(new Network(cellTower));
/* 285 */     } else if (cellTower.getSignalStrength() != null) {
/* 286 */       position.set("rssi", cellTower.getSignalStrength());
/*     */     } 
/*     */   }
/*     */   
/*     */   private void readBinaryCustomData(Position position, ByteBuf buf, String form) {
/* 291 */     CellTower cellTower = new CellTower();
/* 292 */     String[] keys = form.substring(1).split("%");
/* 293 */     for (String key : keys) {
/* 294 */       int combinedMobileCodes; switch (key) {
/*     */         case "SA":
/* 296 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case "MV":
/* 299 */           position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/*     */           break;
/*     */         case "BV":
/* 302 */           position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/*     */           break;
/*     */         case "GQ":
/* 305 */           cellTower.setSignalStrength(Integer.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case "CE":
/* 308 */           cellTower.setCellId(Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case "LC":
/* 311 */           cellTower.setLocationAreaCode(Integer.valueOf(buf.readUnsignedShort()));
/*     */           break;
/*     */         case "CN":
/* 314 */           combinedMobileCodes = (int)(buf.readUnsignedInt() % 100000L);
/* 315 */           cellTower.setMobileCountryCode(Integer.valueOf(combinedMobileCodes / 100));
/* 316 */           cellTower.setMobileNetworkCode(Integer.valueOf(combinedMobileCodes % 100));
/*     */           break;
/*     */         case "RL":
/* 319 */           buf.readUnsignedByte();
/*     */           break;
/*     */         case "PC":
/* 322 */           position.set("count1", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case "AT":
/* 325 */           position.setAltitude(buf.readUnsignedInt());
/*     */           break;
/*     */         case "RP":
/* 328 */           position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
/*     */           break;
/*     */         case "GS":
/* 331 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case "DT":
/* 334 */           position.set("archive", Boolean.valueOf((buf.readUnsignedByte() == 1)));
/*     */           break;
/*     */         case "VN":
/* 337 */           position.set("vin", readString(buf));
/*     */           break;
/*     */         case "MF":
/* 340 */           buf.readUnsignedShort();
/*     */           break;
/*     */         case "EL":
/* 343 */           buf.readUnsignedByte();
/*     */           break;
/*     */         case "TR":
/* 346 */           position.set("throttle", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case "ET":
/* 349 */           position.set("temp1", Integer.valueOf(buf.readUnsignedShort()));
/*     */           break;
/*     */         case "FL":
/* 352 */           position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case "ML":
/* 355 */           buf.readUnsignedByte();
/*     */           break;
/*     */         case "FC":
/* 358 */           position.set("fuelConsumption", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case "CI":
/* 361 */           readString(buf);
/*     */           break;
/*     */         case "AV1":
/* 364 */           position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
/*     */           break;
/*     */         case "NC":
/* 367 */           readString(buf);
/*     */           break;
/*     */         case "SM":
/* 370 */           buf.readUnsignedShort();
/*     */           break;
/*     */         case "GL":
/* 373 */           readString(buf);
/*     */           break;
/*     */         case "MA":
/* 376 */           readString(buf);
/*     */           break;
/*     */         case "PD":
/* 379 */           buf.readUnsignedByte();
/*     */           break;
/*     */         case "CD":
/* 382 */           position.set("iccid", readString(buf));
/*     */           break;
/*     */         case "CM":
/* 385 */           buf.readLong();
/*     */           break;
/*     */         case "GN":
/* 388 */           buf.skipBytes(60);
/*     */           break;
/*     */         case "GV":
/* 391 */           buf.skipBytes(6);
/*     */           break;
/*     */         case "ME":
/* 394 */           buf.readLong();
/*     */           break;
/*     */         case "IA":
/* 397 */           buf.readUnsignedByte();
/*     */           break;
/*     */         case "MP":
/* 400 */           buf.readUnsignedByte();
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 407 */     if (cellTower.getMobileCountryCode() != null && cellTower
/* 408 */       .getMobileNetworkCode() != null && cellTower
/* 409 */       .getCellId() != null && cellTower.getCellId().longValue() != 0L && cellTower
/* 410 */       .getLocationAreaCode() != null) {
/* 411 */       position.setNetwork(new Network(cellTower));
/* 412 */     } else if (cellTower.getSignalStrength() != null) {
/* 413 */       position.set("rssi", cellTower.getSignalStrength());
/*     */     } 
/*     */   }
/*     */   
/* 417 */   private static final Pattern PATTERN_INFO = (new PatternBuilder())
/* 418 */     .text("$INFO=")
/* 419 */     .number("(d+),")
/* 420 */     .expression("([^,]+),")
/* 421 */     .expression("([^,]+),")
/* 422 */     .number("d+,")
/* 423 */     .number("d+,")
/* 424 */     .number("d+,")
/* 425 */     .number("(d+),")
/* 426 */     .number("(d+),")
/* 427 */     .number("(d+),")
/* 428 */     .number("d+,")
/* 429 */     .number("(d+),")
/* 430 */     .number("d+,")
/* 431 */     .number("d+")
/* 432 */     .any()
/* 433 */     .compile();
/*     */   
/*     */   private Position decodeInfo(Channel channel, SocketAddress remoteAddress, String sentence) {
/*     */     DeviceSession deviceSession;
/* 437 */     Position position = new Position(getProtocolName());
/*     */     
/* 439 */     getLastLocation(position, null);
/*     */ 
/*     */ 
/*     */     
/* 443 */     if (sentence.startsWith("$INFO")) {
/*     */       
/* 445 */       Parser parser = new Parser(PATTERN_INFO, sentence);
/* 446 */       if (!parser.matches()) {
/* 447 */         return null;
/*     */       }
/*     */       
/* 450 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*     */       
/* 452 */       position.set("model", parser.next());
/* 453 */       position.set("versionFw", parser.next());
/* 454 */       position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/* 455 */       position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/* 456 */       position.set("sat", parser.nextInt());
/* 457 */       position.set("rssi", parser.nextInt());
/*     */     }
/*     */     else {
/*     */       
/* 461 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */       
/* 463 */       position.set("result", sentence);
/*     */     } 
/*     */ 
/*     */     
/* 467 */     if (deviceSession == null) {
/* 468 */       return null;
/*     */     }
/* 470 */     position.setDeviceId(deviceSession.getDeviceId());
/* 471 */     return position;
/*     */   }
/*     */ 
/*     */   
/* 475 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 476 */     .number("(d+),")
/* 477 */     .number("d+,")
/* 478 */     .number("d+,")
/* 479 */     .number("(-?d+),")
/* 480 */     .number("(-?d+),")
/* 481 */     .number("(d+),")
/* 482 */     .number("(d+),")
/* 483 */     .number("(d+.?d*),")
/* 484 */     .number("(d+),")
/* 485 */     .number("(d+),")
/* 486 */     .number("(d+),")
/* 487 */     .number("(d+),")
/* 488 */     .number("(d+),")
/* 489 */     .number("([^,]+)?,")
/* 490 */     .number("(d+),")
/* 491 */     .number("(d+),")
/* 492 */     .expression("[^,]*,")
/* 493 */     .expression("(.*)")
/* 494 */     .optional(2)
/* 495 */     .compile();
/*     */ 
/*     */   
/*     */   private List<Position> decodeText(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 499 */     int positionIndex = -1;
/* 500 */     for (int i = 0; i < 5; i++) {
/* 501 */       positionIndex = sentence.indexOf(',', positionIndex + 1);
/*     */     }
/*     */     
/* 504 */     String[] headers = sentence.substring(0, positionIndex).split(",");
/* 505 */     long id = Long.parseLong(headers[2]);
/* 506 */     int index = Integer.parseInt(headers[3]);
/*     */     
/* 508 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { headers[4] });
/* 509 */     if (deviceSession == null) {
/* 510 */       return null;
/*     */     }
/*     */     
/* 513 */     sendResponse(channel, remoteAddress, id, index);
/*     */     
/* 515 */     List<Position> positions = new LinkedList<>();
/* 516 */     String[] lines = sentence.substring(positionIndex + 1).split("\r\n");
/*     */     
/* 518 */     for (String line : lines) {
/* 519 */       Position position = decodeTextLine(deviceSession, line);
/* 520 */       if (position != null) {
/* 521 */         positions.add(position);
/*     */       }
/*     */     } 
/*     */     
/* 525 */     return positions;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeTextLine(DeviceSession deviceSession, String sentence) {
/* 531 */     Parser parser = new Parser(PATTERN, sentence);
/* 532 */     if (!parser.matches()) {
/* 533 */       return null;
/*     */     }
/*     */     
/* 536 */     Position position = new Position(getProtocolName());
/* 537 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 539 */     position.setValid(true);
/*     */     
/* 541 */     String time = parser.next();
/* 542 */     if (time.length() >= 14) {
/*     */       try {
/* 544 */         DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
/* 545 */         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 546 */         position.setTime(dateFormat.parse(time));
/* 547 */       } catch (ParseException e) {
/* 548 */         throw new RuntimeException(e);
/*     */       } 
/*     */     } else {
/* 551 */       position.setTime(new Date(Long.parseLong(time) * 1000L));
/*     */     } 
/*     */     
/* 554 */     position.setLongitude(parser.nextInt().intValue() * 1.0E-6D);
/* 555 */     position.setLatitude(parser.nextInt().intValue() * 1.0E-6D);
/* 556 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/* 558 */     position.set("event", parser.nextInt());
/* 559 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 100.0D));
/* 560 */     position.set("hdop", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/* 561 */     position.set("input", parser.nextInt());
/*     */     
/* 563 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/*     */     
/* 565 */     position.set("output", parser.nextInt());
/* 566 */     position.set("adc1", parser.nextInt());
/*     */     
/* 568 */     if (parser.hasNext()) {
/* 569 */       position.set("driverUniqueId", parser.next());
/*     */     }
/*     */     
/* 572 */     position.set("temp1", parser.nextInt());
/* 573 */     position.set("temp2", parser.nextInt());
/*     */     
/* 575 */     if (this.custom) {
/* 576 */       String data = parser.next();
/* 577 */       String form = this.form;
/* 578 */       if (form == null) {
/* 579 */         form = data.substring(0, data.indexOf(',')).substring("%CI".length());
/* 580 */         data = data.substring(data.indexOf(',') + 1);
/*     */       } 
/* 582 */       readTextCustomData(position, data, form);
/*     */     } 
/*     */     
/* 585 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodePhoto(DeviceSession deviceSession, ByteBuf buf, long id) {
/* 590 */     long time = buf.readUnsignedInt();
/* 591 */     int index = buf.readUnsignedByte();
/* 592 */     int count = buf.readUnsignedByte();
/*     */     
/* 594 */     if (this.photo == null) {
/* 595 */       this.photo = Unpooled.buffer();
/*     */     }
/* 597 */     this.photo.writeBytes(buf.readSlice(buf.readUnsignedShort()));
/*     */     
/* 599 */     if (index == count - 1) {
/* 600 */       Position position = new Position(getProtocolName());
/* 601 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 603 */       getLastLocation(position, new Date(time * 1000L));
/*     */       
/* 605 */       position.set("image", Context.getMediaManager().writeFile(String.valueOf(id), this.photo, "jpg"));
/* 606 */       this.photo.release();
/* 607 */       this.photo = null;
/*     */       
/* 609 */       return position;
/*     */     } 
/*     */     
/* 612 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Position> decodeBinary(DeviceSession deviceSession, ByteBuf buf) {
/* 617 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 619 */     while (buf.readableBytes() >= 40) {
/*     */       
/* 621 */       Position position = new Position(getProtocolName());
/* 622 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 624 */       if (this.longDate) {
/*     */ 
/*     */ 
/*     */         
/* 628 */         DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedShort(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 629 */         position.setTime(dateBuilder.getDate());
/*     */         
/* 631 */         buf.skipBytes(14);
/*     */       } else {
/*     */         
/* 634 */         position.setFixTime(new Date(buf.readUnsignedInt() * 1000L));
/* 635 */         position.setDeviceTime(new Date(buf.readUnsignedInt() * 1000L));
/* 636 */         buf.readUnsignedInt();
/*     */       } 
/*     */       
/* 639 */       position.setValid(true);
/* 640 */       position.setLongitude(buf.readInt() * 1.0E-6D);
/* 641 */       position.setLatitude(buf.readInt() * 1.0E-6D);
/* 642 */       position.setCourse(buf.readUnsignedShort());
/*     */       
/* 644 */       int type = buf.readUnsignedByte();
/* 645 */       position.set("type", Integer.valueOf(type));
/* 646 */       position.set("alarm", this.alarmMap.get(Integer.valueOf(type)));
/*     */       
/* 648 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 100L));
/* 649 */       position.set("hdop", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/* 650 */       position.set("input", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 652 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*     */       
/* 654 */       position.set("output", Short.valueOf(buf.readUnsignedByte()));
/* 655 */       position.set("adc1", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       
/* 657 */       position.set("driverUniqueId", readString(buf));
/*     */       
/* 659 */       position.set("temp1", Double.valueOf(buf.readShort() * 0.1D));
/* 660 */       position.set("temp2", Double.valueOf(buf.readShort() * 0.1D));
/*     */       
/* 662 */       String message = readString(buf);
/* 663 */       if (message != null && !message.isEmpty()) {
/* 664 */         Pattern pattern = Pattern.compile("FULS:F=(\\p{XDigit}+) t=(\\p{XDigit}+) N=(\\p{XDigit}+)");
/* 665 */         Matcher matcher = pattern.matcher(message);
/* 666 */         if (matcher.find()) {
/* 667 */           int value = Integer.parseInt(matcher.group(3), this.decimalFuel ? 10 : 16);
/* 668 */           position.set("fuel", Double.valueOf(value * 0.1D));
/*     */         } else {
/* 670 */           position.set("message", message);
/*     */         } 
/*     */       } 
/*     */       
/* 674 */       if (this.custom) {
/* 675 */         String form = this.form;
/* 676 */         if (form == null) {
/* 677 */           form = readString(buf).trim().substring("%CI".length());
/*     */         }
/* 679 */         readBinaryCustomData(position, buf, form);
/*     */       } 
/*     */       
/* 682 */       positions.add(position);
/*     */     } 
/*     */ 
/*     */     
/* 686 */     return positions;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 693 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 695 */     if (buf.getUnsignedShort(buf.readerIndex()) == 65026) {
/* 696 */       if (channel != null) {
/* 697 */         channel.writeAndFlush(new NetworkMessage(buf.retain(), remoteAddress));
/*     */       }
/* 699 */       return null;
/* 700 */     }  if (buf.getByte(buf.readerIndex()) == 36)
/* 701 */       return decodeInfo(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII).trim()); 
/* 702 */     if (buf.getByte(buf.readerIndex() + 2) == 44) {
/* 703 */       return decodeText(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII).trim());
/*     */     }
/*     */     
/* 706 */     String prefix = buf.readCharSequence(2, StandardCharsets.US_ASCII).toString();
/* 707 */     buf.readUnsignedShort();
/* 708 */     buf.readUnsignedShort();
/* 709 */     int index = buf.readUnsignedShort();
/*     */     
/* 711 */     long id = buf.readLong();
/* 712 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(id) });
/* 713 */     if (deviceSession == null) {
/* 714 */       return null;
/*     */     }
/*     */     
/* 717 */     sendResponse(channel, remoteAddress, id, index);
/*     */     
/* 719 */     if (prefix.equals("@R")) {
/* 720 */       return decodePhoto(deviceSession, buf, id);
/*     */     }
/* 722 */     return decodeBinary(deviceSession, buf);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AtrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */