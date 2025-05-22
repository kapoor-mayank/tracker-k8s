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
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ public class MeitrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private int lastEvent;
/*     */   private ByteBuf photo;
/*     */   
/*     */   public MeitrackProtocolDecoder(Protocol protocol) {
/*  49 */     super(protocol);
/*     */   }
/*     */   
/*  52 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  53 */     .text("$$").expression(".")
/*  54 */     .number("d+,")
/*  55 */     .number("(d+),")
/*  56 */     .number("xxx,")
/*  57 */     .number("d+,").optional()
/*  58 */     .number("(d+),")
/*  59 */     .number("(-?d+.d+),")
/*  60 */     .number("(-?d+.d+),")
/*  61 */     .number("(dd)(dd)(dd)")
/*  62 */     .number("(dd)(dd)(dd),")
/*  63 */     .number("([AV]),")
/*  64 */     .number("(d+),")
/*  65 */     .number("(d+),")
/*  66 */     .number("(d+.?d*),")
/*  67 */     .number("(d+),")
/*  68 */     .number("(d+.?d*),")
/*  69 */     .number("(-?d+),")
/*  70 */     .number("(d+),")
/*  71 */     .number("(d+),")
/*  72 */     .number("(d+)|")
/*  73 */     .number("(d+)|")
/*  74 */     .number("(x+)?|")
/*  75 */     .number("(x+)?,")
/*  76 */     .number("(xx)")
/*  77 */     .number("(xx),")
/*  78 */     .groupBegin()
/*  79 */     .number("(d+.d+)|")
/*  80 */     .number("(d+.d+)|")
/*  81 */     .number("d+.d+|")
/*  82 */     .number("d+.d+|")
/*  83 */     .number("d+.d+,")
/*  84 */     .or()
/*  85 */     .number("(x+)?|")
/*  86 */     .number("(x+)?|")
/*  87 */     .number("(x+)?|")
/*  88 */     .number("(x+)|")
/*  89 */     .number("(x+)?,")
/*  90 */     .groupEnd()
/*  91 */     .groupBegin()
/*  92 */     .expression("([^,]+)?,").optional()
/*  93 */     .expression("[^,]*,")
/*  94 */     .number("(d+)?,")
/*  95 */     .number("(x{4})?")
/*  96 */     .groupBegin()
/*  97 */     .number(",(x{6}(?:|x{6})*)?")
/*  98 */     .groupBegin()
/*  99 */     .number(",(d+)")
/* 100 */     .expression(",([^*]*)")
/* 101 */     .groupEnd("?")
/* 102 */     .groupEnd("?")
/* 103 */     .or()
/* 104 */     .any()
/* 105 */     .groupEnd()
/* 106 */     .text("*")
/* 107 */     .number("xx")
/* 108 */     .text("\r\n").optional()
/* 109 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int event) {
/* 112 */     switch (event) {
/*     */       case 1:
/* 114 */         return "sos";
/*     */       case 17:
/* 116 */         return "lowBattery";
/*     */       case 18:
/* 118 */         return "lowPower";
/*     */       case 19:
/* 120 */         return "overspeed";
/*     */       case 20:
/* 122 */         return "geofenceEnter";
/*     */       case 21:
/* 124 */         return "geofenceExit";
/*     */       case 22:
/* 126 */         return "powerRestored";
/*     */       case 23:
/* 128 */         return "powerCut";
/*     */       case 36:
/* 130 */         return "tow";
/*     */       case 44:
/* 132 */         return "jamming";
/*     */       case 78:
/* 134 */         return "accident";
/*     */       case 90:
/*     */       case 91:
/* 137 */         return "hardCornering";
/*     */       case 129:
/* 139 */         return "hardBraking";
/*     */       case 130:
/* 141 */         return "hardAcceleration";
/*     */       case 135:
/* 143 */         return "fatigueDriving";
/*     */     } 
/* 145 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeRegular(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 151 */     Parser parser = new Parser(PATTERN, buf.toString(StandardCharsets.US_ASCII));
/* 152 */     if (!parser.matches()) {
/* 153 */       return null;
/*     */     }
/*     */     
/* 156 */     Position position = new Position(getProtocolName());
/*     */     
/* 158 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 159 */     if (deviceSession == null) {
/* 160 */       return null;
/*     */     }
/* 162 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 164 */     int event = parser.nextInt().intValue();
/* 165 */     position.set("event", Integer.valueOf(event));
/* 166 */     position.set("alarm", decodeAlarm(event));
/* 167 */     if (position.getAttributes().containsKey("alarm")) {
/* 168 */       this.lastEvent = event;
/*     */     }
/*     */     
/* 171 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 172 */     position.setLongitude(parser.nextDouble().doubleValue());
/*     */     
/* 174 */     position.setTime(parser.nextDateTime());
/*     */     
/* 176 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 178 */     position.set("sat", parser.nextInt());
/* 179 */     int rssi = parser.nextInt().intValue();
/*     */     
/* 181 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 182 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 184 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 186 */     position.setAltitude(parser.nextDouble().doubleValue());
/*     */     
/* 188 */     position.set("odometer", parser.nextInt());
/* 189 */     position.set("runtime", parser.next());
/*     */     
/* 191 */     position.set("rssi", Integer.valueOf(rssi));
/* 192 */     int mcc = parser.nextInt().intValue();
/* 193 */     int mnc = parser.nextInt().intValue();
/* 194 */     int lac = parser.nextHexInt(0);
/* 195 */     int cid = parser.nextHexInt(0);
/* 196 */     if (mcc != 0 && mnc != 0) {
/* 197 */       position.setNetwork(new Network(CellTower.from(mcc, mnc, lac, cid, rssi)));
/*     */     }
/*     */     
/* 200 */     if (parser.hasNext()) {
/* 201 */       int input = parser.nextHexInt().intValue();
/* 202 */       for (int i = 1; i <= 5; i++) {
/* 203 */         position.set("in" + i, Boolean.valueOf(BitUtil.check(input, i - 1)));
/*     */       }
/* 205 */       int output = parser.nextHexInt().intValue();
/* 206 */       for (int j = 1; j <= 5; j++) {
/* 207 */         position.set("out" + j, Boolean.valueOf(BitUtil.check(output, j - 1)));
/*     */       }
/*     */     } 
/*     */     
/* 211 */     if (parser.hasNext(2)) {
/*     */       
/* 213 */       position.set("battery", parser.nextDouble());
/* 214 */       position.set("power", parser.nextDouble());
/*     */     }
/*     */     else {
/*     */       
/* 218 */       for (int i = 1; i <= 3; i++) {
/* 219 */         position.set("adc" + i, parser.nextHexInt());
/*     */       }
/*     */       
/* 222 */       String model = getDeviceModel(deviceSession);
/* 223 */       if (model == null) {
/* 224 */         model = "";
/*     */       }
/* 226 */       switch (model.toUpperCase()) {
/*     */         case "MVT340":
/*     */         case "MVT380":
/* 229 */           position.set("battery", Double.valueOf(parser.nextHexInt(0) * 3.0D * 2.0D / 1024.0D));
/* 230 */           position.set("power", Double.valueOf(parser.nextHexInt(0) * 3.0D * 16.0D / 1024.0D));
/*     */           break;
/*     */         case "MT90":
/* 233 */           position.set("battery", Double.valueOf(parser.nextHexInt(0) * 3.3D * 2.0D / 4096.0D));
/* 234 */           position.set("power", Integer.valueOf(parser.nextHexInt(0)));
/*     */           break;
/*     */         case "T1":
/*     */         case "T3":
/*     */         case "MVT100":
/*     */         case "MVT600":
/*     */         case "MVT800":
/*     */         case "TC68":
/*     */         case "TC68S":
/* 243 */           position.set("battery", Double.valueOf(parser.nextHexInt(0) * 3.3D * 2.0D / 4096.0D));
/* 244 */           position.set("power", Double.valueOf(parser.nextHexInt(0) * 3.3D * 16.0D / 4096.0D));
/*     */           break;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         default:
/* 253 */           position.set("battery", Double.valueOf(parser.nextHexInt().intValue() / 100.0D));
/* 254 */           position.set("power", Double.valueOf(parser.nextHexInt(0) / 100.0D));
/*     */           break;
/*     */       } 
/*     */ 
/*     */     
/*     */     } 
/* 260 */     String eventData = parser.next();
/* 261 */     if (eventData != null && !eventData.isEmpty()) {
/* 262 */       switch (event) {
/*     */         case 37:
/* 264 */           position.set("driverUniqueId", eventData);
/*     */           break;
/*     */         default:
/* 267 */           position.set("eventData", eventData);
/*     */           break;
/*     */       } 
/*     */     
/*     */     }
/* 272 */     int protocol = parser.nextInt(0);
/*     */     
/* 274 */     if (parser.hasNext()) {
/* 275 */       String fuel = parser.next();
/* 276 */       position.set("fuel", 
/* 277 */           Double.valueOf(Integer.parseInt(fuel.substring(0, 2), 16) + Integer.parseInt(fuel.substring(2), 16) * 0.01D));
/*     */     } 
/*     */     
/* 280 */     if (parser.hasNext()) {
/* 281 */       for (String temp : parser.next().split("\\|")) {
/* 282 */         int index = Integer.parseInt(temp.substring(0, 2), 16);
/* 283 */         if (protocol >= 3) {
/* 284 */           double value = (short)Integer.parseInt(temp.substring(2), 16);
/* 285 */           position.set("temp" + index, Double.valueOf(value * 0.01D));
/*     */         } else {
/* 287 */           double value = Byte.parseByte(temp.substring(2, 4), 16);
/* 288 */           value += ((value < 0.0D) ? -0.01D : 0.01D) * Integer.parseInt(temp.substring(4), 16);
/* 289 */           position.set("temp" + index, Double.valueOf(value));
/*     */         } 
/*     */       } 
/*     */     }
/*     */     
/* 294 */     if (parser.hasNext(2)) {
/* 295 */       parser.nextInt();
/* 296 */       decodeDataFields(position, parser.next().split(","));
/*     */     } 
/*     */     
/* 299 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeDataFields(Position position, String[] values) {
/* 304 */     if (values.length > 1 && !values[1].isEmpty()) {
/* 305 */       position.set("tempData", values[1]);
/*     */     }
/*     */     
/* 308 */     if (values.length > 5 && !values[5].isEmpty()) {
/* 309 */       String[] data = values[5].split("\\|");
/* 310 */       boolean started = (data[0].charAt(1) == '0');
/* 311 */       position.set("taximeterOn", Boolean.valueOf(started));
/* 312 */       position.set("taximeterStart", data[1]);
/* 313 */       if (data.length > 2) {
/* 314 */         position.set("taximeterEnd", data[2]);
/* 315 */         position.set("taximeterDistance", Integer.valueOf(Integer.parseInt(data[3])));
/* 316 */         position.set("taximeterFare", Integer.valueOf(Integer.parseInt(data[4])));
/* 317 */         position.set("taximeterTrip", data[5]);
/* 318 */         position.set("taximeterWait", data[6]);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Position> decodeBinaryC(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 325 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 327 */     String flag = buf.toString(2, 1, StandardCharsets.US_ASCII);
/* 328 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/*     */     
/* 330 */     String imei = buf.toString(index + 1, 15, StandardCharsets.US_ASCII);
/* 331 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 332 */     if (deviceSession == null) {
/* 333 */       return null;
/*     */     }
/*     */     
/* 336 */     buf.skipBytes(index + 1 + 15 + 1 + 3 + 1 + 2 + 2 + 4);
/*     */     
/* 338 */     while (buf.readableBytes() >= 52) {
/*     */       
/* 340 */       Position position = new Position(getProtocolName());
/* 341 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 343 */       position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 345 */       position.setLatitude(buf.readIntLE() * 1.0E-6D);
/* 346 */       position.setLongitude(buf.readIntLE() * 1.0E-6D);
/*     */       
/* 348 */       position.setTime(new Date((946684800L + buf.readUnsignedIntLE()) * 1000L));
/*     */       
/* 350 */       position.setValid((buf.readUnsignedByte() == 1));
/*     */       
/* 352 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 353 */       int rssi = buf.readUnsignedByte();
/*     */       
/* 355 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));
/* 356 */       position.setCourse(buf.readUnsignedShortLE());
/*     */       
/* 358 */       position.set("hdop", Double.valueOf(buf.readUnsignedShortLE() * 0.1D));
/*     */       
/* 360 */       position.setAltitude(buf.readUnsignedShortLE());
/*     */       
/* 362 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/* 363 */       position.set("runtime", Long.valueOf(buf.readUnsignedIntLE()));
/*     */       
/* 365 */       position.setNetwork(new Network(CellTower.from(buf
/* 366 */               .readUnsignedShortLE(), buf.readUnsignedShortLE(), buf
/* 367 */               .readUnsignedShortLE(), buf.readUnsignedShortLE(), rssi)));
/*     */ 
/*     */       
/* 370 */       position.set("status", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/* 372 */       position.set("adc1", Integer.valueOf(buf.readUnsignedShortLE()));
/* 373 */       position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/* 374 */       position.set("power", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/* 376 */       buf.readUnsignedIntLE();
/*     */       
/* 378 */       positions.add(position);
/*     */     } 
/*     */     
/* 381 */     if (channel != null) {
/* 382 */       StringBuilder command = new StringBuilder("@@");
/* 383 */       command.append(flag).append(27 + positions.size() / 10).append(",");
/* 384 */       command.append(imei).append(",CCC,").append(positions.size()).append("*");
/* 385 */       command.append(Checksum.sum(command.toString()));
/* 386 */       command.append("\r\n");
/* 387 */       channel.writeAndFlush(new NetworkMessage(command.toString(), remoteAddress));
/*     */     } 
/*     */     
/* 390 */     return positions;
/*     */   }
/*     */   
/*     */   private List<Position> decodeBinaryE(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 394 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 396 */     buf.readerIndex(buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44) + 1);
/* 397 */     String imei = buf.readSlice(15).toString(StandardCharsets.US_ASCII);
/* 398 */     buf.skipBytes(5);
/*     */     
/* 400 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 401 */     if (deviceSession == null) {
/* 402 */       return null;
/*     */     }
/*     */     
/* 405 */     buf.readUnsignedIntLE();
/* 406 */     int count = buf.readUnsignedShortLE();
/*     */     
/* 408 */     for (int i = 0; i < count; i++) {
/* 409 */       Position position = new Position(getProtocolName());
/* 410 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 412 */       buf.readUnsignedShortLE();
/* 413 */       buf.readUnsignedShortLE();
/*     */       
/* 415 */       int paramCount = buf.readUnsignedByte(); int j;
/* 416 */       for (j = 0; j < paramCount; j++) {
/* 417 */         int input, k, lockState; boolean extension = (buf.getUnsignedByte(buf.readerIndex()) == 254);
/* 418 */         int id = extension ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 419 */         switch (id) {
/*     */           case 1:
/* 421 */             position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 5:
/* 424 */             position.setValid((buf.readUnsignedByte() > 0));
/*     */             break;
/*     */           case 6:
/* 427 */             position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 7:
/* 430 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 20:
/* 433 */             position.set("output", Integer.toBinaryString(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 21:
/* 436 */             input = buf.readUnsignedByte();
/* 437 */             for (k = 0; k < 4; k++) {
/* 438 */               position.set("in" + (k + 1), Boolean.valueOf(BitUtil.check(input, k)));
/*     */             }
/*     */             break;
/*     */           case 71:
/* 442 */             lockState = buf.readUnsignedByte();
/* 443 */             if (lockState > 0) {
/* 444 */               position.set("lock", Boolean.valueOf((lockState == 2)));
/*     */             }
/*     */             break;
/*     */           case 151:
/* 448 */             position.set("throttle", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 157:
/* 451 */             position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           case 65129:
/* 454 */             position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/*     */             break;
/*     */           default:
/* 457 */             buf.readUnsignedByte();
/*     */             break;
/*     */         } 
/*     */       
/*     */       } 
/* 462 */       paramCount = buf.readUnsignedByte();
/* 463 */       for (j = 0; j < paramCount; j++) {
/* 464 */         double battery; int percentage; boolean extension = (buf.getUnsignedByte(buf.readerIndex()) == 254);
/* 465 */         int id = extension ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 466 */         switch (id) {
/*     */           case 8:
/* 468 */             position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 9:
/* 471 */             position.setCourse(buf.readUnsignedShortLE());
/*     */             break;
/*     */           case 10:
/* 474 */             position.set("hdop", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 11:
/* 477 */             position.setAltitude(buf.readShortLE());
/*     */             break;
/*     */           case 22:
/* 480 */             position.set("adc1", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/*     */             break;
/*     */           case 25:
/* 483 */             battery = buf.readUnsignedShortLE() * 0.01D;
/* 484 */             percentage = (int)((battery - 3.4D) / 0.8D * 100.0D);
/* 485 */             if (percentage >= 0 && percentage <= 100) {
/* 486 */               position.set("batteryLevel", Integer.valueOf(percentage));
/*     */             }
/* 488 */             position.set("battery", Double.valueOf(battery));
/*     */             break;
/*     */           case 26:
/* 491 */             position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/*     */             break;
/*     */           case 41:
/* 494 */             position.set("fuel", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/*     */             break;
/*     */           case 64:
/* 497 */             position.set("event", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 145:
/*     */           case 146:
/* 501 */             position.set("obdSpeed", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 152:
/* 504 */             position.set("fuelUsed", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 153:
/* 507 */             position.set("rpm", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 156:
/* 510 */             position.set("coolantTemp", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 159:
/* 513 */             position.set("temp1", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           case 201:
/* 516 */             position.set("fuelConsumption", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */             break;
/*     */           default:
/* 519 */             buf.readUnsignedShortLE();
/*     */             break;
/*     */         } 
/*     */       
/*     */       } 
/* 524 */       paramCount = buf.readUnsignedByte();
/* 525 */       for (j = 0; j < paramCount; j++) {
/* 526 */         boolean extension = (buf.getUnsignedByte(buf.readerIndex()) == 254);
/* 527 */         int id = extension ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 528 */         switch (id) {
/*     */           case 2:
/* 530 */             position.setLatitude(buf.readIntLE() * 1.0E-6D);
/*     */             break;
/*     */           case 3:
/* 533 */             position.setLongitude(buf.readIntLE() * 1.0E-6D);
/*     */             break;
/*     */           case 4:
/* 536 */             position.setTime(new Date((946684800L + buf.readUnsignedIntLE()) * 1000L));
/*     */             break;
/*     */           case 12:
/* 539 */             position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */             break;
/*     */           case 13:
/* 542 */             position.set("runtime", Long.valueOf(buf.readUnsignedIntLE()));
/*     */             break;
/*     */           case 37:
/* 545 */             position.set("driverUniqueId", String.valueOf(buf.readUnsignedIntLE()));
/*     */             break;
/*     */           case 155:
/* 548 */             position.set("obdOdometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */             break;
/*     */           case 160:
/* 551 */             position.set("fuelUsed", Double.valueOf(buf.readUnsignedIntLE() * 0.001D));
/*     */             break;
/*     */           case 162:
/* 554 */             position.set("fuelConsumption", Double.valueOf(buf.readUnsignedIntLE() * 0.01D));
/*     */             break;
/*     */           case 65268:
/* 557 */             position.set("hours", Long.valueOf(buf.readUnsignedIntLE() * 60000L));
/*     */             break;
/*     */           default:
/* 560 */             buf.readUnsignedIntLE();
/*     */             break;
/*     */         } 
/*     */       
/*     */       } 
/* 565 */       paramCount = buf.readUnsignedByte();
/* 566 */       for (j = 0; j < paramCount; j++) {
/* 567 */         int k; boolean extension = (buf.getUnsignedByte(buf.readerIndex()) == 254);
/* 568 */         int id = extension ? buf.readUnsignedShort() : buf.readUnsignedByte();
/* 569 */         int length = buf.readUnsignedByte();
/* 570 */         switch (id) {
/*     */           case 42:
/*     */           case 43:
/*     */           case 44:
/*     */           case 45:
/*     */           case 46:
/*     */           case 47:
/*     */           case 48:
/*     */           case 49:
/* 579 */             buf.readUnsignedByte();
/* 580 */             position.set("temp" + (id - 42), Double.valueOf(buf.readShortLE() * 0.01D));
/*     */             break;
/*     */           case 65073:
/* 583 */             buf.readUnsignedByte();
/* 584 */             switch (buf.readUnsignedByte()) {
/*     */               case 1:
/* 586 */                 position.set("alarm", "lookLeft");
/*     */                 break;
/*     */               case 2:
/* 589 */                 position.set("alarm", "lookRight");
/*     */                 break;
/*     */               case 3:
/* 592 */                 position.set("alarm", "raiseHead");
/*     */                 break;
/*     */               case 4:
/* 595 */                 position.set("alarm", "lowerHead");
/*     */                 break;
/*     */               case 5:
/* 598 */                 position.set("alarm", "drowsiness");
/*     */                 break;
/*     */               case 6:
/* 601 */                 position.set("alarm", "yawning");
/*     */                 break;
/*     */               case 7:
/* 604 */                 position.set("alarm", "calling");
/*     */                 break;
/*     */               case 8:
/* 607 */                 position.set("alarm", "smoking");
/*     */                 break;
/*     */               case 9:
/* 610 */                 position.set("alarm", "drinking");
/*     */                 break;
/*     */               case 10:
/* 613 */                 position.set("alarm", "driverAbsence");
/*     */                 break;
/*     */               case 11:
/* 616 */                 position.set("alarm", "cameraOcclusion");
/*     */                 break;
/*     */               case 128:
/* 619 */                 position.set("alarm", "forwardCollision");
/*     */                 break;
/*     */               case 129:
/* 622 */                 position.set("alarm", "distanceDetection");
/*     */                 break;
/*     */               case 130:
/* 625 */                 position.set("alarm", "leftLaneDeparture");
/*     */                 break;
/*     */               case 131:
/* 628 */                 position.set("alarm", "rightLaneDeparture");
/*     */                 break;
/*     */               case 132:
/* 631 */                 position.set("alarm", "frontVehicleStarted");
/*     */                 break;
/*     */             } 
/*     */ 
/*     */             
/* 636 */             buf.skipBytes(length - 2);
/*     */             break;
/*     */           case 65139:
/* 639 */             buf.readUnsignedByte();
/* 640 */             position.set("tagName", buf
/*     */                 
/* 642 */                 .readCharSequence(buf.readUnsignedByte(), StandardCharsets.US_ASCII).toString());
/* 643 */             buf.skipBytes(6);
/* 644 */             position.set("tagBattery", Short.valueOf(buf.readUnsignedByte()));
/* 645 */             position.set("tagTemp", Double.valueOf(buf.readUnsignedShortLE() / 256.0D));
/* 646 */             position.set("tagHumidity", Double.valueOf(buf.readUnsignedShortLE() / 256.0D));
/* 647 */             buf.readUnsignedShortLE();
/* 648 */             buf.readUnsignedShortLE();
/* 649 */             buf.readUnsignedShortLE();
/* 650 */             buf.readUnsignedShortLE();
/*     */             break;
/*     */           case 65192:
/* 653 */             for (k = 1; k <= 3; k++) {
/* 654 */               if (buf.readUnsignedByte() > 0) {
/* 655 */                 String key = (k == 1) ? "batteryLevel" : ("battery" + k + "Level");
/* 656 */                 position.set(key, Short.valueOf(buf.readUnsignedByte()));
/*     */               } else {
/* 658 */                 buf.readUnsignedByte();
/*     */               } 
/*     */             } 
/* 661 */             buf.readUnsignedByte();
/*     */             break;
/*     */           default:
/* 664 */             buf.skipBytes(length);
/*     */             break;
/*     */         } 
/*     */       
/*     */       } 
/* 669 */       positions.add(position);
/*     */     } 
/*     */     
/* 672 */     return positions;
/*     */   }
/*     */   
/*     */   private void requestPhotoPacket(Channel channel, SocketAddress remoteAddress, String imei, String file, int index) {
/* 676 */     if (channel != null) {
/* 677 */       String content = "D00," + file + "," + index;
/* 678 */       int length = 1 + imei.length() + 1 + content.length() + 5;
/* 679 */       String response = String.format("@@O%02d,%s,%s*", new Object[] { Integer.valueOf(length), imei, content });
/* 680 */       response = response + Checksum.sum(response) + "\r\n";
/* 681 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   } protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     int endIndex;
/*     */     String file;
/*     */     int total, current;
/*     */     Position position;
/*     */     String result;
/* 689 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 691 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/* 692 */     String imei = buf.toString(index + 1, 15, StandardCharsets.US_ASCII);
/* 693 */     index = buf.indexOf(index + 1, buf.writerIndex(), (byte)44);
/* 694 */     String type = buf.toString(index + 1, 3, StandardCharsets.US_ASCII);
/*     */     
/* 696 */     switch (type) {
/*     */       case "AAC":
/* 698 */         if (channel != null) {
/* 699 */           String response = String.format("@@z27,%s,AAC,1*", new Object[] { imei });
/* 700 */           response = response + Checksum.sum(response) + "\r\n";
/* 701 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         } 
/* 703 */         return null;
/*     */       case "D00":
/* 705 */         if (this.photo == null) {
/* 706 */           this.photo = Unpooled.buffer();
/*     */         }
/*     */         
/* 709 */         index = index + 1 + type.length() + 1;
/* 710 */         endIndex = buf.indexOf(index, buf.writerIndex(), (byte)44);
/* 711 */         file = buf.toString(index, endIndex - index, StandardCharsets.US_ASCII);
/* 712 */         index = endIndex + 1;
/* 713 */         endIndex = buf.indexOf(index, buf.writerIndex(), (byte)44);
/* 714 */         total = Integer.parseInt(buf.toString(index, endIndex - index, StandardCharsets.US_ASCII));
/* 715 */         index = endIndex + 1;
/* 716 */         endIndex = buf.indexOf(index, buf.writerIndex(), (byte)44);
/* 717 */         current = Integer.parseInt(buf.toString(index, endIndex - index, StandardCharsets.US_ASCII));
/*     */         
/* 719 */         buf.readerIndex(endIndex + 1);
/* 720 */         this.photo.writeBytes(buf.readSlice(buf.readableBytes() - 1 - 2 - 2));
/*     */         
/* 722 */         if (current == total - 1) {
/* 723 */           Position position1 = new Position(getProtocolName());
/* 724 */           position1.setDeviceId(getDeviceSession(channel, remoteAddress, new String[] { imei }).getDeviceId());
/*     */           
/* 726 */           getLastLocation(position1, null);
/*     */           
/* 728 */           position1.set("image", 
/* 729 */               Context.getMediaManager().writeFile(imei, this.photo, String.valueOf(this.lastEvent), "jpg"));
/* 730 */           this.photo.release();
/* 731 */           this.photo = null;
/*     */           
/* 733 */           return position1;
/*     */         } 
/* 735 */         if ((current + 1) % 8 == 0) {
/* 736 */           Thread.sleep(2000L);
/* 737 */           requestPhotoPacket(channel, remoteAddress, imei, file, current + 1);
/*     */         } 
/* 739 */         return null;
/*     */       
/*     */       case "D03":
/* 742 */         this.photo = Unpooled.buffer();
/* 743 */         requestPhotoPacket(channel, remoteAddress, imei, "camera_picture.jpg", 0);
/* 744 */         return null;
/*     */       case "D82":
/* 746 */         position = new Position(getProtocolName());
/* 747 */         position.setDeviceId(getDeviceSession(channel, remoteAddress, new String[] { imei }).getDeviceId());
/* 748 */         getLastLocation(position, null);
/* 749 */         result = buf.toString(index + 1, buf.writerIndex() - index - 4, StandardCharsets.US_ASCII);
/* 750 */         position.set("result", result);
/* 751 */         return position;
/*     */       case "CCC":
/* 753 */         return decodeBinaryC(channel, remoteAddress, buf);
/*     */       case "CCE":
/* 755 */         return decodeBinaryE(channel, remoteAddress, buf);
/*     */     } 
/* 757 */     return decodeRegular(channel, remoteAddress, buf);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeitrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */