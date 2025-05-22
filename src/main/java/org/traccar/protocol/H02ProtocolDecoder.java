/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.TimeZone;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ public class H02ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private final boolean g200;
/*     */   private final boolean icar;
/*     */   
/*     */   public H02ProtocolDecoder(Protocol protocol) {
/*  49 */     super(protocol);
/*  50 */     this.g200 = protocol instanceof G200Protocol;
/*  51 */     this.icar = protocol instanceof IcarProtocol;
/*     */   }
/*     */ 
/*     */   
/*     */   private static double readCoordinate(ByteBuf buf, boolean lon) {
/*  56 */     int degrees = BcdUtil.readInteger(buf, 2);
/*  57 */     if (lon) {
/*  58 */       degrees = degrees * 10 + (buf.getUnsignedByte(buf.readerIndex()) >> 4);
/*     */     }
/*     */     
/*  61 */     double result = 0.0D;
/*  62 */     if (lon) {
/*  63 */       result = (buf.readUnsignedByte() & 0xF);
/*     */     }
/*     */     
/*  66 */     int length = 6;
/*  67 */     if (lon) {
/*  68 */       length = 5;
/*     */     }
/*     */     
/*  71 */     result = result * 10.0D + BcdUtil.readInteger(buf, length) * 1.0E-4D;
/*     */     
/*  73 */     result /= 60.0D;
/*  74 */     result += degrees;
/*     */     
/*  76 */     return result;
/*     */   }
/*     */ 
/*     */   
/*     */   private void processStatus(Position position, long status) {
/*  81 */     if (!BitUtil.check(status, 0)) {
/*  82 */       position.set("alarm", "vibration");
/*  83 */     } else if (!BitUtil.check(status, 1) || !BitUtil.check(status, 18) || !BitUtil.check(status, 6)) {
/*  84 */       position.set("alarm", "sos");
/*  85 */     } else if (!BitUtil.check(status, 2)) {
/*  86 */       position.set("alarm", "overspeed");
/*  87 */     } else if (!BitUtil.check(status, 19)) {
/*  88 */       position.set("alarm", "powerCut");
/*  89 */     } else if (!BitUtil.check(status, 22)) {
/*  90 */       position.set("alarm", "shock");
/*     */     } 
/*     */     
/*  93 */     if (this.g200 && !BitUtil.check(status, 26)) {
/*  94 */       position.set("alarm", "deviceDisconnected");
/*     */     }
/*     */     
/*  97 */     position.set("immobilizer", Boolean.valueOf(!BitUtil.check(status, 27)));
/*  98 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 10)));
/*  99 */     position.set("status", Long.valueOf(status));
/*     */   }
/*     */ 
/*     */   
/*     */   private Integer decodeBattery(int value) {
/* 104 */     if (value == 0)
/* 105 */       return null; 
/* 106 */     if (value <= 3)
/* 107 */       return Integer.valueOf((value - 1) * 10); 
/* 108 */     if (value <= 6)
/* 109 */       return Integer.valueOf((value - 1) * 20); 
/* 110 */     if (value <= 100)
/* 111 */       return Integer.valueOf(value); 
/* 112 */     if (value >= 241 && value <= 246) {
/* 113 */       return Integer.valueOf(value - 240);
/*     */     }
/* 115 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {
/*     */     String id;
/* 121 */     Position position = new Position(getProtocolName());
/*     */     
/* 123 */     boolean longId = (buf.readableBytes() == 42);
/*     */     
/* 125 */     buf.readByte();
/*     */ 
/*     */     
/* 128 */     if (longId) {
/* 129 */       id = ByteBufUtil.hexDump(buf.readSlice(8)).substring(0, 15);
/*     */     } else {
/* 131 */       id = ByteBufUtil.hexDump(buf.readSlice(5));
/*     */     } 
/*     */     
/* 134 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 135 */     if (deviceSession == null) {
/* 136 */       return null;
/*     */     }
/* 138 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 146 */     DateBuilder dateBuilder = (new DateBuilder()).setHour(BcdUtil.readInteger(buf, 2)).setMinute(BcdUtil.readInteger(buf, 2)).setSecond(BcdUtil.readInteger(buf, 2)).setDay(BcdUtil.readInteger(buf, 2)).setMonth(BcdUtil.readInteger(buf, 2)).setYear(BcdUtil.readInteger(buf, 2));
/* 147 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 149 */     double latitude = readCoordinate(buf, false);
/* 150 */     position.set("batteryLevel", decodeBattery(buf.readUnsignedByte()));
/* 151 */     double longitude = readCoordinate(buf, true);
/*     */     
/* 153 */     int flags = buf.readUnsignedByte() & 0xF;
/* 154 */     position.setValid(((flags & 0x2) != 0));
/* 155 */     if ((flags & 0x4) == 0) {
/* 156 */       latitude = -latitude;
/*     */     }
/* 158 */     if ((flags & 0x8) == 0) {
/* 159 */       longitude = -longitude;
/*     */     }
/*     */     
/* 162 */     position.setLatitude(latitude);
/* 163 */     position.setLongitude(longitude);
/*     */     
/* 165 */     position.setSpeed(BcdUtil.readInteger(buf, 3));
/* 166 */     position.setCourse((buf.readUnsignedByte() & 0xF) * 100.0D + BcdUtil.readInteger(buf, 2));
/*     */     
/* 168 */     processStatus(position, buf.readUnsignedInt());
/*     */     
/* 170 */     if (this.icar) {
/* 171 */       buf.skipBytes(2);
/* 172 */       position.set("GSMSignal", Integer.valueOf(BcdUtil.readInteger(buf, 2)));
/* 173 */       position.set("GPSGlonassSignal", Byte.valueOf(buf.readByte()));
/*     */       
/* 175 */       buf.skipBytes(4);
/* 176 */       buf.skipBytes(2);
/* 177 */       BcdUtil.readInteger(buf, 2);
/* 178 */       BcdUtil.readInteger(buf, 4);
/* 179 */       BcdUtil.readInteger(buf, 4);
/*     */       
/* 181 */       position.set("externalVoltage", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/*     */     } 
/*     */     
/* 184 */     return position;
/*     */   }
/*     */   
/* 187 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 188 */     .text("*")
/* 189 */     .expression("..,")
/* 190 */     .number("(d+)?,")
/* 191 */     .groupBegin()
/* 192 */     .text("V4,")
/* 193 */     .expression("(.*),")
/* 194 */     .or()
/* 195 */     .expression("(V[^,]*),")
/* 196 */     .groupEnd()
/* 197 */     .number("(?:(dd)(dd)(dd))?,")
/* 198 */     .groupBegin()
/* 199 */     .expression("([ABV])?,")
/* 200 */     .or()
/* 201 */     .number("(d+),")
/* 202 */     .groupEnd()
/* 203 */     .groupBegin()
/* 204 */     .number("-(d+)-(d+.d+),")
/* 205 */     .or()
/* 206 */     .number("(d+)(dd.d+),")
/* 207 */     .groupEnd()
/* 208 */     .expression("([NS]),")
/* 209 */     .groupBegin()
/* 210 */     .number("-(d+)-(d+.d+),")
/* 211 */     .or()
/* 212 */     .number("(d+)(dd.d+),")
/* 213 */     .groupEnd()
/* 214 */     .expression("([EW]),")
/* 215 */     .number(" *(d+.?d*),")
/* 216 */     .number("(d+.?d*)?,")
/* 217 */     .number("(?:d+,)?")
/* 218 */     .number("(?:(dd)(dd)(dd))?")
/* 219 */     .groupBegin()
/* 220 */     .expression(",[^,]*,")
/* 221 */     .expression("[^,]*,")
/* 222 */     .expression("[^,]*")
/* 223 */     .groupEnd("?")
/* 224 */     .groupBegin()
/* 225 */     .number(",(x{8})")
/* 226 */     .groupBegin()
/* 227 */     .number(",(d+),")
/* 228 */     .number("(-?d+),")
/* 229 */     .number("(d+.d+),")
/* 230 */     .number("(-?d+),")
/* 231 */     .number("(x+),")
/* 232 */     .number("(x+)")
/* 233 */     .or()
/* 234 */     .text(",")
/* 235 */     .expression("(.*)")
/* 236 */     .or()
/* 237 */     .groupEnd()
/* 238 */     .or()
/* 239 */     .groupEnd()
/* 240 */     .text("#")
/* 241 */     .compile();
/*     */   
/* 243 */   private static final Pattern PATTERN_NBR = (new PatternBuilder())
/* 244 */     .text("*")
/* 245 */     .expression("..,")
/* 246 */     .number("(d+),")
/* 247 */     .text("NBR,")
/* 248 */     .number("(dd)(dd)(dd),")
/* 249 */     .number("(d+),")
/* 250 */     .number("(d+),")
/* 251 */     .number("d+,")
/* 252 */     .number("d+,")
/* 253 */     .number("((?:d+,d+,d+,)+)")
/* 254 */     .number("(dd)(dd)(dd),")
/* 255 */     .number("(x{8})")
/* 256 */     .any()
/* 257 */     .compile();
/*     */   
/* 259 */   private static final Pattern PATTERN_LINK = (new PatternBuilder())
/* 260 */     .text("*")
/* 261 */     .expression("..,")
/* 262 */     .number("(d+),")
/* 263 */     .text("LINK,")
/* 264 */     .number("(dd)(dd)(dd),")
/* 265 */     .number("(d+),")
/* 266 */     .number("(d+),")
/* 267 */     .number("(d+),")
/* 268 */     .number("(d+),")
/* 269 */     .number("(d+),")
/* 270 */     .number("(dd)(dd)(dd),")
/* 271 */     .number("(x{8})")
/* 272 */     .any()
/* 273 */     .compile();
/*     */   
/* 275 */   private static final Pattern PATTERN_V3 = (new PatternBuilder())
/* 276 */     .text("*")
/* 277 */     .expression("..,")
/* 278 */     .number("(d+),")
/* 279 */     .text("V3,")
/* 280 */     .number("(dd)(dd)(dd),")
/* 281 */     .number("(ddd)")
/* 282 */     .number("(d+),")
/* 283 */     .number("(d+),")
/* 284 */     .expression("(.*),")
/* 285 */     .number("(x{4}),")
/* 286 */     .number("d+,")
/* 287 */     .text("X,")
/* 288 */     .number("(dd)(dd)(dd),")
/* 289 */     .number("(x{8})")
/* 290 */     .text("#").optional()
/* 291 */     .compile();
/*     */   
/* 293 */   private static final Pattern PATTERN_VP1 = (new PatternBuilder())
/* 294 */     .text("*hq,")
/* 295 */     .number("(d{15}),")
/* 296 */     .text("VP1,")
/* 297 */     .groupBegin()
/* 298 */     .text("V,")
/* 299 */     .number("(d+),")
/* 300 */     .number("(d+),")
/* 301 */     .expression("([^#]+)")
/* 302 */     .or()
/* 303 */     .expression("[AB],")
/* 304 */     .number("(d+)(dd.d+),")
/* 305 */     .expression("([NS]),")
/* 306 */     .number("(d+)(dd.d+),")
/* 307 */     .expression("([EW]),")
/* 308 */     .number("(d+.d+),")
/* 309 */     .number("(d+.d+),")
/* 310 */     .number("(dd)(dd)(dd)")
/* 311 */     .groupEnd()
/* 312 */     .any()
/* 313 */     .compile();
/*     */   
/* 315 */   private static final Pattern PATTERN_HTBT = (new PatternBuilder())
/* 316 */     .text("*HQ,")
/* 317 */     .number("(d{15}),")
/* 318 */     .text("HTBT,")
/* 319 */     .number("(d+)")
/* 320 */     .any()
/* 321 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, String id, String type) {
/* 324 */     if (channel != null && id != null) {
/*     */       String response;
/* 326 */       DateFormat dateFormat = new SimpleDateFormat(type.equals("R12") ? "HHmmss" : "yyyyMMddHHmmss");
/* 327 */       dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 328 */       String time = dateFormat.format(new Date());
/* 329 */       if (type.equals("R12")) {
/* 330 */         response = String.format("*HQ,%s,%s,%s#", new Object[] { id, type, time });
/*     */       } else {
/* 332 */         response = String.format("*HQ,%s,V4,%s,%s#", new Object[] { id, type, time });
/*     */       } 
/* 334 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeText(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 340 */     Parser parser = new Parser(PATTERN, sentence);
/* 341 */     if (!parser.matches()) {
/* 342 */       return null;
/*     */     }
/*     */     
/* 345 */     String id = parser.next();
/* 346 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 347 */     if (deviceSession == null) {
/* 348 */       return null;
/*     */     }
/*     */     
/* 351 */     Position position = new Position(getProtocolName());
/* 352 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 354 */     if (parser.hasNext()) {
/* 355 */       position.set("result", parser.next());
/*     */     }
/*     */     
/* 358 */     if (parser.hasNext() && parser.next().equals("V1")) {
/* 359 */       sendResponse(channel, remoteAddress, id, "V1");
/* 360 */     } else if (Context.getConfig().getBoolean(getProtocolName() + ".ack")) {
/* 361 */       sendResponse(channel, remoteAddress, id, "R12");
/*     */     } 
/*     */     
/* 364 */     DateBuilder dateBuilder = new DateBuilder();
/* 365 */     if (parser.hasNext(3)) {
/* 366 */       dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     }
/*     */     
/* 369 */     if (parser.hasNext()) {
/* 370 */       position.setValid(parser.next().equals("A"));
/*     */     }
/* 372 */     if (parser.hasNext()) {
/* 373 */       parser.nextInt();
/* 374 */       position.setValid(true);
/*     */     } 
/*     */     
/* 377 */     if (parser.hasNext(2)) {
/* 378 */       position.setLatitude(-parser.nextCoordinate());
/*     */     }
/* 380 */     if (parser.hasNext(2)) {
/* 381 */       position.setLatitude(parser.nextCoordinate());
/*     */     }
/*     */     
/* 384 */     if (parser.hasNext(2)) {
/* 385 */       position.setLongitude(-parser.nextCoordinate());
/*     */     }
/* 387 */     if (parser.hasNext(2)) {
/* 388 */       position.setLongitude(parser.nextCoordinate());
/*     */     }
/*     */     
/* 391 */     position.setSpeed(parser.nextDouble(0.0D));
/* 392 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 394 */     if (parser.hasNext(3)) {
/* 395 */       dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 396 */       position.setTime(dateBuilder.getDate());
/*     */     } else {
/* 398 */       position.setTime(new Date());
/*     */     } 
/*     */     
/* 401 */     if (parser.hasNext()) {
/* 402 */       processStatus(position, parser.nextLong(16, 0L));
/*     */     }
/*     */     
/* 405 */     if (parser.hasNext(6)) {
/* 406 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/* 407 */       position.set("temp1", Integer.valueOf(parser.nextInt(0)));
/* 408 */       position.set("fuel", Double.valueOf(parser.nextDouble(0.0D)));
/*     */       
/* 410 */       position.setAltitude(parser.nextInt(0));
/*     */       
/* 412 */       position.setNetwork(new Network(CellTower.fromLacCid(parser.nextHexInt(0), parser.nextHexInt(0))));
/*     */     } 
/*     */     
/* 415 */     if (parser.hasNext(4)) {
/* 416 */       String[] values = parser.next().split(",");
/* 417 */       for (int i = 0; i < values.length; i++) {
/* 418 */         position.set("io" + (i + 1), values[i].trim());
/*     */       }
/*     */     } 
/*     */     
/* 422 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeLbs(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 427 */     Parser parser = new Parser(PATTERN_NBR, sentence);
/* 428 */     if (!parser.matches()) {
/* 429 */       return null;
/*     */     }
/*     */     
/* 432 */     String id = parser.next();
/* 433 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 434 */     if (deviceSession == null) {
/* 435 */       return null;
/*     */     }
/*     */     
/* 438 */     sendResponse(channel, remoteAddress, id, "NBR");
/*     */     
/* 440 */     Position position = new Position(getProtocolName());
/* 441 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 444 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 446 */     Network network = new Network();
/* 447 */     int mcc = parser.nextInt(0);
/* 448 */     int mnc = parser.nextInt(0);
/*     */     
/* 450 */     String[] cells = parser.next().split(",");
/* 451 */     for (int i = 0; i < cells.length / 3; i++) {
/* 452 */       network.addCellTower(CellTower.from(mcc, mnc, Integer.parseInt(cells[i * 3]), 
/* 453 */             Integer.parseInt(cells[i * 3 + 1]), Integer.parseInt(cells[i * 3 + 2])));
/*     */     }
/*     */     
/* 456 */     position.setNetwork(network);
/*     */     
/* 458 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 460 */     getLastLocation(position, dateBuilder.getDate());
/*     */     
/* 462 */     processStatus(position, parser.nextLong(16, 0L));
/*     */     
/* 464 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeLink(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 469 */     Parser parser = new Parser(PATTERN_LINK, sentence);
/* 470 */     if (!parser.matches()) {
/* 471 */       return null;
/*     */     }
/*     */     
/* 474 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 475 */     if (deviceSession == null) {
/* 476 */       return null;
/*     */     }
/*     */     
/* 479 */     Position position = new Position(getProtocolName());
/* 480 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 483 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 485 */     position.set("rssi", parser.nextInt());
/* 486 */     position.set("sat", parser.nextInt());
/* 487 */     position.set("batteryLevel", parser.nextInt());
/* 488 */     position.set("steps", parser.nextInt());
/* 489 */     position.set("turnovers", parser.nextInt());
/*     */     
/* 491 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 493 */     getLastLocation(position, dateBuilder.getDate());
/*     */     
/* 495 */     processStatus(position, parser.nextLong(16, 0L));
/*     */     
/* 497 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeV3(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 502 */     Parser parser = new Parser(PATTERN_V3, sentence);
/* 503 */     if (!parser.matches()) {
/* 504 */       return null;
/*     */     }
/*     */     
/* 507 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 508 */     if (deviceSession == null) {
/* 509 */       return null;
/*     */     }
/*     */     
/* 512 */     Position position = new Position(getProtocolName());
/* 513 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 516 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 518 */     int mcc = parser.nextInt().intValue();
/* 519 */     int mnc = parser.nextInt().intValue();
/*     */     
/* 521 */     int count = parser.nextInt().intValue();
/* 522 */     Network network = new Network();
/* 523 */     String[] values = parser.next().split(",");
/* 524 */     for (int i = 0; i < count; i++) {
/* 525 */       network.addCellTower(CellTower.from(mcc, mnc, 
/* 526 */             Integer.parseInt(values[i * 4]), Integer.parseInt(values[i * 4 + 1])));
/*     */     }
/* 528 */     position.setNetwork(network);
/*     */     
/* 530 */     position.set("battery", parser.nextHexInt());
/*     */     
/* 532 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 534 */     getLastLocation(position, dateBuilder.getDate());
/*     */     
/* 536 */     processStatus(position, parser.nextLong(16, 0L));
/*     */     
/* 538 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeVp1(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 543 */     Parser parser = new Parser(PATTERN_VP1, sentence);
/* 544 */     if (!parser.matches()) {
/* 545 */       return null;
/*     */     }
/*     */     
/* 548 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 549 */     if (deviceSession == null) {
/* 550 */       return null;
/*     */     }
/*     */     
/* 553 */     Position position = new Position(getProtocolName());
/* 554 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 556 */     if (parser.hasNext(3)) {
/*     */       
/* 558 */       getLastLocation(position, null);
/*     */       
/* 560 */       int mcc = parser.nextInt().intValue();
/* 561 */       int mnc = parser.nextInt().intValue();
/*     */       
/* 563 */       Network network = new Network();
/* 564 */       for (String cell : parser.next().split("Y")) {
/* 565 */         String[] values = cell.split(",");
/* 566 */         network.addCellTower(CellTower.from(mcc, mnc, 
/* 567 */               Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2])));
/*     */       } 
/*     */       
/* 570 */       position.setNetwork(network);
/*     */     }
/*     */     else {
/*     */       
/* 574 */       position.setValid(true);
/* 575 */       position.setLatitude(parser.nextCoordinate());
/* 576 */       position.setLongitude(parser.nextCoordinate());
/* 577 */       position.setSpeed(parser.nextDouble().doubleValue());
/* 578 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */       
/* 580 */       position.setTime((new DateBuilder())
/* 581 */           .setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0)).getDate());
/*     */     } 
/*     */ 
/*     */     
/* 585 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeHeartbeat(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 590 */     Parser parser = new Parser(PATTERN_HTBT, sentence);
/* 591 */     if (!parser.matches()) {
/* 592 */       return null;
/*     */     }
/*     */     
/* 595 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 596 */     if (deviceSession == null) {
/* 597 */       return null;
/*     */     }
/*     */     
/* 600 */     Position position = new Position(getProtocolName());
/* 601 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 603 */     getLastLocation(position, null);
/*     */     
/* 605 */     position.set("batteryLevel", parser.nextInt());
/*     */     
/* 607 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     String sentence;
/*     */     int typeStart, typeEnd;
/* 614 */     ByteBuf buf = (ByteBuf)msg;
/* 615 */     String marker = buf.toString(0, 1, StandardCharsets.US_ASCII);
/*     */     
/* 617 */     switch (marker) {
/*     */       case "*":
/* 619 */         sentence = buf.toString(StandardCharsets.US_ASCII).trim();
/* 620 */         typeStart = sentence.indexOf(',', sentence.indexOf(',') + 1) + 1;
/* 621 */         typeEnd = sentence.indexOf(',', typeStart);
/* 622 */         if (typeEnd < 0) {
/* 623 */           typeEnd = sentence.indexOf('#', typeStart);
/*     */         }
/* 625 */         if (typeEnd > 0) {
/* 626 */           String type = sentence.substring(typeStart, typeEnd);
/* 627 */           switch (type) {
/*     */             case "V0":
/*     */             case "HTBT":
/* 630 */               if (channel != null) {
/* 631 */                 String response = sentence.substring(0, typeEnd) + "#";
/* 632 */                 channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */               } 
/* 634 */               return decodeHeartbeat(sentence, channel, remoteAddress);
/*     */             case "NBR":
/* 636 */               return decodeLbs(sentence, channel, remoteAddress);
/*     */             case "LINK":
/* 638 */               return decodeLink(sentence, channel, remoteAddress);
/*     */             case "V3":
/* 640 */               return decodeV3(sentence, channel, remoteAddress);
/*     */             case "VP1":
/* 642 */               return decodeVp1(sentence, channel, remoteAddress);
/*     */           } 
/* 644 */           return decodeText(sentence, channel, remoteAddress);
/*     */         } 
/*     */         
/* 647 */         return null;
/*     */       
/*     */       case "$":
/* 650 */         return decodeBinary(buf, channel, remoteAddress);
/*     */     } 
/*     */     
/* 653 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\H02ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */