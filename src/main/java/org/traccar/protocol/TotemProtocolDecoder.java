/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ public class TotemProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TotemProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*     */   }
/*     */   
/*  42 */   private static final Pattern PATTERN1 = (new PatternBuilder())
/*  43 */     .text("$$")
/*  44 */     .number("xx")
/*  45 */     .number("(d+)|")
/*  46 */     .expression("(..)")
/*  47 */     .text("$GPRMC,")
/*  48 */     .number("(dd)(dd)(dd).d+,")
/*  49 */     .expression("([AV]),")
/*  50 */     .number("(d+)(dd.d+),([NS]),")
/*  51 */     .number("(d+)(dd.d+),([EW]),")
/*  52 */     .number("(d+.?d*)?,")
/*  53 */     .number("(d+.?d*)?,")
/*  54 */     .number("(dd)(dd)(dd)")
/*  55 */     .expression("[^*]*").text("*")
/*  56 */     .number("xx|")
/*  57 */     .number("(d+.d+)|")
/*  58 */     .number("(d+.d+)|")
/*  59 */     .number("(d+.d+)|")
/*  60 */     .number("(d+)|")
/*  61 */     .number("d+|")
/*  62 */     .number("d")
/*  63 */     .number("(ddd)")
/*  64 */     .number("(dddd)|")
/*  65 */     .number("(d+)|").optional()
/*  66 */     .number("x*(xxxx)")
/*  67 */     .number("(xxxx)|")
/*  68 */     .number("(d+)|")
/*  69 */     .number("(d+.d+)|")
/*  70 */     .number("d+|")
/*  71 */     .any()
/*  72 */     .number("xxxx")
/*  73 */     .any()
/*  74 */     .compile();
/*     */   
/*  76 */   private static final Pattern PATTERN2 = (new PatternBuilder())
/*  77 */     .text("$$")
/*  78 */     .number("xx")
/*  79 */     .number("(d+)|")
/*  80 */     .expression("(..)")
/*  81 */     .number("(dd)(dd)(dd)")
/*  82 */     .number("(dd)(dd)(dd)|")
/*  83 */     .expression("([AV])|")
/*  84 */     .number("(d+)(dd.d+)|")
/*  85 */     .expression("([NS])|")
/*  86 */     .number("(d+)(dd.d+)|")
/*  87 */     .expression("([EW])|")
/*  88 */     .number("(d+.d+)?|")
/*  89 */     .number("(d+)?|")
/*  90 */     .number("(d+.d+)|")
/*  91 */     .number("(d+)|")
/*  92 */     .number("d")
/*  93 */     .number("(dd)")
/*  94 */     .number("(dd)|")
/*  95 */     .number("(d+)|")
/*  96 */     .number("(xxxx)")
/*  97 */     .number("(xxxx)|")
/*  98 */     .number("(d+)|")
/*  99 */     .number("(d+.d+)|")
/* 100 */     .number("d+|")
/* 101 */     .number("xxxx")
/* 102 */     .any()
/* 103 */     .compile();
/*     */   
/* 105 */   private static final Pattern PATTERN3 = (new PatternBuilder())
/* 106 */     .text("$$")
/* 107 */     .number("xx")
/* 108 */     .number("(d+)|")
/* 109 */     .expression("(..)")
/* 110 */     .number("(dd)(dd)(dd)")
/* 111 */     .number("(dd)(dd)(dd)")
/* 112 */     .number("(xxxx)")
/* 113 */     .expression("[01]")
/* 114 */     .number("(dd)")
/* 115 */     .number("(dd)")
/* 116 */     .number("(dddd)")
/* 117 */     .number("(dddd)")
/* 118 */     .number("(ddd)")
/* 119 */     .number("(ddd)")
/* 120 */     .number("(xxxx)")
/* 121 */     .number("(xxxx)")
/* 122 */     .expression("([AV])")
/* 123 */     .number("(dd)")
/* 124 */     .number("(ddd)")
/* 125 */     .number("(ddd)")
/* 126 */     .number("(dd.d)")
/* 127 */     .number("(d{7})")
/* 128 */     .number("(dd)(dd.dddd)([NS])")
/* 129 */     .number("(ddd)(dd.dddd)([EW])")
/* 130 */     .number("dddd")
/* 131 */     .number("xxxx")
/* 132 */     .any()
/* 133 */     .compile();
/*     */   
/* 135 */   private static final Pattern PATTERN4 = (new PatternBuilder())
/* 136 */     .text("$$")
/* 137 */     .number("dddd")
/* 138 */     .number("xx")
/* 139 */     .number("(d+)|")
/* 140 */     .number("(x{8})")
/* 141 */     .number("(dd)(dd)(dd)")
/* 142 */     .number("(dd)(dd)(dd)")
/* 143 */     .number("(dd)")
/* 144 */     .number("(dd)")
/* 145 */     .number("(dddd)")
/* 146 */     .groupBegin()
/* 147 */     .groupBegin()
/* 148 */     .number("(dddd)")
/* 149 */     .number("(dddd)")
/* 150 */     .number("(dddd)")
/* 151 */     .groupEnd("?")
/* 152 */     .number("(dddd)")
/* 153 */     .number("(dddd)?")
/* 154 */     .groupEnd("?")
/* 155 */     .number("(xxxx)")
/* 156 */     .number("(xxxx)")
/* 157 */     .groupBegin()
/* 158 */     .number("(dd)")
/* 159 */     .number("(ddd)")
/* 160 */     .groupEnd("?")
/* 161 */     .number("(dd)")
/* 162 */     .number("(dd)")
/* 163 */     .number("(ddd)")
/* 164 */     .number("(ddd)")
/* 165 */     .number("(dd.d)")
/* 166 */     .number("(d{7})")
/* 167 */     .number("(dd)(dd.dddd)([NS])")
/* 168 */     .number("(ddd)(dd.dddd)([EW])")
/* 169 */     .number("dddd")
/* 170 */     .number("xx")
/* 171 */     .any()
/* 172 */     .compile();
/*     */   
/* 174 */   private static final Pattern PATTERN_E2 = (new PatternBuilder())
/* 175 */     .text("$$")
/* 176 */     .number("dddd")
/* 177 */     .number("xx")
/* 178 */     .number("(d+)|")
/* 179 */     .number("(dd)(dd)(dd)")
/* 180 */     .number("(dd)(dd)(dd),")
/* 181 */     .number("(-?d+.d+),")
/* 182 */     .number("(-?d+.d+),")
/* 183 */     .expression("(.+)")
/* 184 */     .number("|xx")
/* 185 */     .any()
/* 186 */     .compile();
/*     */   
/* 188 */   private static final Pattern PATTERN_E5 = (new PatternBuilder())
/* 189 */     .text("$$")
/* 190 */     .number("dddd")
/* 191 */     .number("xx")
/* 192 */     .number("(d+)|")
/* 193 */     .number("(dd)(dd)(dd)")
/* 194 */     .number("(dd)(dd)(dd),")
/* 195 */     .number("(-?d+.d+),")
/* 196 */     .number("(-?d+.d+),")
/* 197 */     .expression("[^,]*,")
/* 198 */     .number("(d+),")
/* 199 */     .number("(d+),")
/* 200 */     .number("(d+),")
/* 201 */     .number("(d+),")
/* 202 */     .number("(d+),")
/* 203 */     .number("(d+),")
/* 204 */     .number("(d+),")
/* 205 */     .number("(d+),")
/* 206 */     .number("(d+),")
/* 207 */     .number("(d+),")
/* 208 */     .number("(d+),")
/* 209 */     .number("(d+),")
/* 210 */     .number("(d+),")
/* 211 */     .number("|xx")
/* 212 */     .any()
/* 213 */     .compile();
/*     */   
/*     */   private String decodeAlarm123(int value) {
/* 216 */     switch (value) {
/*     */       case 1:
/* 218 */         return "sos";
/*     */       case 73:
/* 220 */         return "buttonA";
/*     */       case 16:
/* 222 */         return "lowBattery";
/*     */       case 17:
/* 224 */         return "overspeed";
/*     */       case 48:
/* 226 */         return "parking";
/*     */       case 66:
/* 228 */         return "geofenceExit";
/*     */       case 67:
/* 230 */         return "geofenceEnter";
/*     */       case 80:
/* 232 */         return "io1Close";
/*     */       case 81:
/* 234 */         return "io1Open";
/*     */       case 82:
/* 236 */         return "io2Close";
/*     */       case 83:
/* 238 */         return "io2Open";
/*     */       case 84:
/* 240 */         return "io3Close";
/*     */       case 85:
/* 242 */         return "io3Open";
/*     */       case 86:
/* 244 */         return "io4Close";
/*     */       case 87:
/* 246 */         return "io4Open";
/*     */       case 96:
/* 248 */         return "chargeBegin";
/*     */       case 97:
/* 250 */         return "chargeEnd";
/*     */       case 136:
/* 252 */         return "heartbeat";
/*     */       case 145:
/* 254 */         return "sleepBegin";
/*     */       case 146:
/* 256 */         return "sleepEnd";
/*     */       case 170:
/* 258 */         return "dataInterval";
/*     */     } 
/* 260 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private String decodeAlarm4(int value) {
/* 265 */     switch (value) {
/*     */       case 1:
/* 267 */         return "sos";
/*     */       case 2:
/* 269 */         return "overspeed";
/*     */       case 4:
/* 271 */         return "geofenceExit";
/*     */       case 5:
/* 273 */         return "geofenceEnter";
/*     */       case 64:
/* 275 */         return "shock";
/*     */       case 66:
/* 277 */         return "hardAcceleration";
/*     */       case 67:
/* 279 */         return "hardBraking";
/*     */     } 
/* 281 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decode12(Channel channel, SocketAddress remoteAddress, String sentence, Pattern pattern) {
/* 287 */     Parser parser = new Parser(pattern, sentence);
/* 288 */     if (!parser.matches()) {
/* 289 */       return null;
/*     */     }
/*     */     
/* 292 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 293 */     if (deviceSession == null) {
/* 294 */       return null;
/*     */     }
/*     */     
/* 297 */     Position position = new Position(getProtocolName());
/* 298 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 300 */     if (parser.hasNext()) {
/* 301 */       position.set("alarm", decodeAlarm123(Short.parseShort(parser.next(), 16)));
/*     */     }
/* 303 */     DateBuilder dateBuilder = new DateBuilder();
/* 304 */     int year = 0, month = 0, day = 0;
/* 305 */     if (pattern == PATTERN2) {
/* 306 */       day = parser.nextInt(0);
/* 307 */       month = parser.nextInt(0);
/* 308 */       year = parser.nextInt(0);
/*     */     } 
/* 310 */     dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 312 */     position.setValid(parser.next().equals("A"));
/* 313 */     position.setLatitude(parser.nextCoordinate());
/* 314 */     position.setLongitude(parser.nextCoordinate());
/* 315 */     position.setSpeed(parser.nextDouble(0.0D));
/* 316 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 318 */     if (pattern == PATTERN1) {
/* 319 */       day = parser.nextInt(0);
/* 320 */       month = parser.nextInt(0);
/* 321 */       year = parser.nextInt(0);
/*     */     } 
/* 323 */     if (year == 0) {
/* 324 */       return null;
/*     */     }
/* 326 */     dateBuilder.setDate(year, month, day);
/* 327 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 329 */     if (pattern == PATTERN1) {
/* 330 */       position.set("pdop", parser.nextDouble());
/* 331 */       position.set("hdop", parser.nextDouble());
/* 332 */       position.set("vdop", parser.nextDouble());
/*     */     } else {
/* 334 */       position.set("hdop", parser.nextDouble());
/*     */     } 
/*     */     
/* 337 */     String stringIo = parser.next();
/* 338 */     int io = Integer.parseInt(stringIo, 2);
/* 339 */     position.set("status", stringIo);
/* 340 */     if (pattern == PATTERN1) {
/* 341 */       position.set("alarm", BitUtil.check(io, 0) ? "sos" : null);
/* 342 */       position.set("in3", Boolean.valueOf(BitUtil.check(io, 4)));
/* 343 */       position.set("in4", Boolean.valueOf(BitUtil.check(io, 5)));
/* 344 */       position.set("in1", Boolean.valueOf(BitUtil.check(io, 6)));
/* 345 */       position.set("in2", Boolean.valueOf(BitUtil.check(io, 7)));
/* 346 */       position.set("out1", Boolean.valueOf(BitUtil.check(io, 8)));
/* 347 */       position.set("out2", Boolean.valueOf(BitUtil.check(io, 9)));
/* 348 */       position.set("battery", Double.valueOf(parser.nextDouble(0.0D) * 0.01D));
/*     */     } else {
/* 350 */       position.set("antenna", Boolean.valueOf(BitUtil.check(io, 0)));
/* 351 */       position.set("charge", Boolean.valueOf(BitUtil.check(io, 1))); int i;
/* 352 */       for (i = 1; i <= 6; i++) {
/* 353 */         position.set("in" + i, Boolean.valueOf(BitUtil.check(io, 1 + i)));
/*     */       }
/* 355 */       for (i = 1; i <= 4; i++) {
/* 356 */         position.set("out" + i, Boolean.valueOf(BitUtil.check(io, 7 + i)));
/*     */       }
/* 358 */       position.set("battery", Double.valueOf(parser.nextDouble(0.0D) * 0.1D));
/*     */     } 
/*     */     
/* 361 */     position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/* 362 */     position.set("adc1", parser.next());
/*     */     
/* 364 */     int lac = parser.nextHexInt(0);
/* 365 */     int cid = parser.nextHexInt(0);
/* 366 */     if (lac != 0 && cid != 0) {
/* 367 */       position.setNetwork(new Network(CellTower.fromLacCid(lac, cid)));
/*     */     }
/*     */     
/* 370 */     position.set("temp1", parser.next());
/* 371 */     position.set("odometer", Double.valueOf(parser.nextDouble(0.0D) * 1000.0D));
/*     */     
/* 373 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decode3(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 378 */     Parser parser = new Parser(PATTERN3, sentence);
/* 379 */     if (!parser.matches()) {
/* 380 */       return null;
/*     */     }
/*     */     
/* 383 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 384 */     if (deviceSession == null) {
/* 385 */       return null;
/*     */     }
/*     */     
/* 388 */     Position position = new Position(getProtocolName());
/* 389 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 391 */     if (parser.hasNext()) {
/* 392 */       position.set("alarm", decodeAlarm123(Short.parseShort(parser.next(), 16)));
/*     */     }
/*     */     
/* 395 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 397 */     position.set("io1", parser.next());
/* 398 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D) * 0.1D));
/* 399 */     position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/* 400 */     position.set("adc1", parser.next());
/* 401 */     position.set("adc2", parser.next());
/* 402 */     position.set("temp1", parser.next());
/* 403 */     position.set("temp2", parser.next());
/*     */     
/* 405 */     position.setNetwork(new Network(
/* 406 */           CellTower.fromLacCid(parser.nextHexInt(0), parser.nextHexInt(0))));
/*     */     
/* 408 */     position.setValid(parser.next().equals("A"));
/* 409 */     position.set("sat", parser.nextInt());
/* 410 */     position.setCourse(parser.nextDouble(0.0D));
/* 411 */     position.setSpeed(parser.nextDouble(0.0D));
/* 412 */     position.set("pdop", parser.nextDouble());
/* 413 */     position.set("odometer", Integer.valueOf(parser.nextInt(0) * 1000));
/*     */     
/* 415 */     position.setLatitude(parser.nextCoordinate());
/* 416 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 418 */     return position;
/*     */   }
/*     */   
/*     */   private Position decode4(Channel channel, SocketAddress remoteAddress, String sentence) {
/*     */     CellTower cellTower;
/* 423 */     int type = Integer.parseInt(sentence.substring(6, 8), 16);
/*     */     
/* 425 */     switch (type) {
/*     */       case 226:
/* 427 */         return decodeE2(channel, remoteAddress, sentence);
/*     */       case 229:
/* 429 */         return decodeE5(channel, remoteAddress, sentence);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 434 */     Parser parser = new Parser(PATTERN4, sentence);
/* 435 */     if (!parser.matches()) {
/* 436 */       return null;
/*     */     }
/*     */     
/* 439 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 440 */     if (deviceSession == null) {
/* 441 */       return null;
/*     */     }
/*     */     
/* 444 */     Position position = new Position(getProtocolName());
/* 445 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 447 */     position.set("alarm", decodeAlarm4(type));
/*     */     
/* 449 */     long status = parser.nextHexLong().longValue();
/*     */     
/* 451 */     position.set("alarm", BitUtil.check(status, 31) ? "sos" : null);
/* 452 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 30)));
/* 453 */     position.set("alarm", BitUtil.check(status, 29) ? "overspeed" : null);
/* 454 */     position.set("charge", Boolean.valueOf(BitUtil.check(status, 28)));
/* 455 */     position.set("alarm", BitUtil.check(status, 27) ? "geofenceExit" : null);
/* 456 */     position.set("alarm", BitUtil.check(status, 26) ? "geofenceEnter" : null);
/* 457 */     position.set("out1", Boolean.valueOf(BitUtil.check(status, 23)));
/* 458 */     position.set("out2", Boolean.valueOf(BitUtil.check(status, 22)));
/* 459 */     position.set("out3", Boolean.valueOf(BitUtil.check(status, 21)));
/* 460 */     position.set("out4", Boolean.valueOf(BitUtil.check(status, 20)));
/* 461 */     position.set("in2", Boolean.valueOf(BitUtil.check(status, 19)));
/* 462 */     position.set("in3", Boolean.valueOf(BitUtil.check(status, 18)));
/* 463 */     position.set("in4", Boolean.valueOf(BitUtil.check(status, 17)));
/* 464 */     position.set("alarm", BitUtil.check(status, 16) ? "shock" : null);
/* 465 */     position.set("alarm", BitUtil.check(status, 14) ? "lowBattery" : null);
/* 466 */     position.set("alarm", BitUtil.check(status, 10) ? "jamming" : null);
/*     */ 
/*     */     
/* 469 */     position.setTime(parser.nextDateTime());
/*     */     
/* 471 */     position.set("battery", Double.valueOf(parser.nextDouble().doubleValue() * 0.1D));
/* 472 */     position.set("power", parser.nextDouble());
/*     */     
/* 474 */     position.set("adc1", parser.next());
/* 475 */     position.set("adc2", parser.next());
/* 476 */     position.set("adc3", parser.next());
/* 477 */     position.set("adc4", parser.next());
/* 478 */     position.set("temp1", parser.next());
/*     */     
/* 480 */     if (parser.hasNext()) {
/* 481 */       position.set("temp2", parser.next());
/* 482 */       position.setValid(BitUtil.check(status, 12));
/*     */     } else {
/* 484 */       position.setValid(BitUtil.check(status, 14));
/*     */     } 
/*     */     
/* 487 */     int lac = parser.nextHexInt().intValue();
/* 488 */     int cid = parser.nextHexInt().intValue();
/*     */     
/* 490 */     if (parser.hasNext(2)) {
/* 491 */       int mnc = parser.nextInt().intValue();
/* 492 */       int mcc = parser.nextInt().intValue();
/* 493 */       cellTower = CellTower.from(mcc, mnc, lac, cid);
/*     */     } else {
/* 495 */       cellTower = CellTower.fromLacCid(lac, cid);
/*     */     } 
/* 497 */     position.set("sat", parser.nextInt());
/* 498 */     cellTower.setSignalStrength(parser.nextInt());
/* 499 */     position.setNetwork(new Network(cellTower));
/*     */     
/* 501 */     position.setCourse(parser.nextDouble().doubleValue());
/* 502 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 503 */     position.set("hdop", parser.nextDouble());
/* 504 */     position.set("odometer", Integer.valueOf(parser.nextInt().intValue() * 1000));
/*     */     
/* 506 */     position.setLatitude(parser.nextCoordinate());
/* 507 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 509 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeE2(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 514 */     Parser parser = new Parser(PATTERN_E2, sentence);
/* 515 */     if (!parser.matches()) {
/* 516 */       return null;
/*     */     }
/*     */     
/* 519 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 520 */     if (deviceSession == null) {
/* 521 */       return null;
/*     */     }
/*     */     
/* 524 */     Position position = new Position(getProtocolName());
/* 525 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 527 */     position.setValid(true);
/* 528 */     position.setTime(parser.nextDateTime());
/* 529 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 530 */     position.setLatitude(parser.nextDouble().doubleValue());
/*     */     
/* 532 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 534 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeE5(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 539 */     Parser parser = new Parser(PATTERN_E5, sentence);
/* 540 */     if (!parser.matches()) {
/* 541 */       return null;
/*     */     }
/*     */     
/* 544 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 545 */     if (deviceSession == null) {
/* 546 */       return null;
/*     */     }
/*     */     
/* 549 */     Position position = new Position(getProtocolName());
/* 550 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 552 */     position.setValid(true);
/* 553 */     position.setTime(parser.nextDateTime());
/* 554 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 555 */     position.setLongitude(parser.nextDouble().doubleValue());
/*     */     
/* 557 */     position.set("odometer", parser.nextLong());
/* 558 */     position.set("fuelUsed", parser.nextInt());
/* 559 */     position.set("fuelConsumption", parser.nextInt());
/* 560 */     position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/* 561 */     position.set("rpm", parser.nextInt());
/* 562 */     position.set("obdSpeed", parser.nextInt());
/* 563 */     parser.nextInt();
/* 564 */     parser.nextInt();
/* 565 */     position.set("coolantTemp", parser.nextInt());
/* 566 */     position.set("intakeTemp", parser.nextInt());
/* 567 */     position.set("engineLoad", parser.nextInt());
/* 568 */     position.set("throttle", parser.nextInt());
/* 569 */     position.set("fuel", parser.nextInt());
/*     */     
/* 571 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     Position position;
/* 578 */     String sentence = (String)msg;
/*     */ 
/*     */     
/* 581 */     if (sentence.charAt(2) == '0') {
/* 582 */       position = decode4(channel, remoteAddress, sentence);
/* 583 */     } else if (sentence.contains("$GPRMC")) {
/* 584 */       position = decode12(channel, remoteAddress, sentence, PATTERN1);
/*     */     } else {
/* 586 */       int index = sentence.indexOf('|');
/* 587 */       if (index != -1 && sentence.indexOf('|', index + 1) != -1) {
/* 588 */         position = decode12(channel, remoteAddress, sentence, PATTERN2);
/*     */       } else {
/* 590 */         position = decode3(channel, remoteAddress, sentence);
/*     */       } 
/*     */     } 
/*     */     
/* 594 */     if (channel != null) {
/* 595 */       if (sentence.charAt(2) == '0') {
/* 596 */         String response = "$$0014AA" + sentence.substring(sentence.length() - 6, sentence.length() - 2);
/* 597 */         response = response + String.format("%02X", new Object[] { Integer.valueOf(Checksum.xor(response)) }).toUpperCase();
/* 598 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       } else {
/* 600 */         channel.writeAndFlush(new NetworkMessage("ACK OK\r\n", remoteAddress));
/*     */       } 
/*     */     }
/*     */     
/* 604 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TotemProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */