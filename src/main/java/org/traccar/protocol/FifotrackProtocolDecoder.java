/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.TimeZone;
/*     */ import java.util.regex.Matcher;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FifotrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private ByteBuf photo;
/*     */   
/*     */   public FifotrackProtocolDecoder(Protocol protocol) {
/*  50 */     super(protocol);
/*     */   }
/*     */   
/*  53 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  54 */     .text("$$")
/*  55 */     .number("d+,")
/*  56 */     .number("(d+),")
/*  57 */     .number("x+,")
/*  58 */     .expression("[^,]+,")
/*  59 */     .number("(d+)?,")
/*  60 */     .number("(dd)(dd)(dd)")
/*  61 */     .number("(dd)(dd)(dd),")
/*  62 */     .number("([AV]),")
/*  63 */     .number("(-?d+.d+),")
/*  64 */     .number("(-?d+.d+),")
/*  65 */     .number("(d+),")
/*  66 */     .number("(d+),")
/*  67 */     .number("(-?d+),")
/*  68 */     .number("(d+),")
/*  69 */     .number("(d+),")
/*  70 */     .number("(x+),")
/*  71 */     .number("(x+)?,")
/*  72 */     .number("(x+)?,")
/*  73 */     .number("(d+)|")
/*  74 */     .number("(d+)|")
/*  75 */     .number("(x+)|")
/*  76 */     .number("(x+),")
/*  77 */     .number("([x|]+)")
/*  78 */     .expression(",([^,]+)")
/*  79 */     .expression(",([^*]*)").optional(2)
/*  80 */     .any()
/*  81 */     .compile();
/*     */   
/*  83 */   private static final Pattern PATTERN_NEW = (new PatternBuilder())
/*  84 */     .text("$$")
/*  85 */     .number("d+,")
/*  86 */     .number("(d+),")
/*  87 */     .number("(x+),")
/*  88 */     .text("A03,")
/*  89 */     .number("(d+)?,")
/*  90 */     .number("(dd)(dd)(dd)")
/*  91 */     .number("(dd)(dd)(dd),")
/*  92 */     .number("(d+)|")
/*  93 */     .number("(d+)|")
/*  94 */     .number("(x+)|")
/*  95 */     .number("(x+),")
/*  96 */     .number("(d+.d+),")
/*  97 */     .number("(d+),")
/*  98 */     .number("(x+),")
/*  99 */     .groupBegin()
/* 100 */     .text("0,")
/* 101 */     .number("([AV]),")
/* 102 */     .number("(d+),")
/* 103 */     .number("(d+),")
/* 104 */     .number("(-?d+.d+),")
/* 105 */     .number("(-?d+.d+)")
/* 106 */     .or()
/* 107 */     .text("1,")
/* 108 */     .expression("([^*]+)")
/* 109 */     .groupEnd()
/* 110 */     .text("*")
/* 111 */     .number("xx")
/* 112 */     .compile();
/*     */   
/* 114 */   private static final Pattern PATTERN_PHOTO = (new PatternBuilder())
/* 115 */     .text("$$")
/* 116 */     .number("d+,")
/* 117 */     .number("(d+),")
/* 118 */     .any()
/* 119 */     .number(",(d+),")
/* 120 */     .expression("([^*]+)")
/* 121 */     .text("*")
/* 122 */     .number("xx")
/* 123 */     .compile();
/*     */   
/* 125 */   private static final Pattern PATTERN_PHOTO_DATA = (new PatternBuilder())
/* 126 */     .text("$$")
/* 127 */     .number("d+,")
/* 128 */     .number("(d+),")
/* 129 */     .number("x+,")
/* 130 */     .expression("[^,]+,")
/* 131 */     .expression("([^,]+),")
/* 132 */     .number("(d+),")
/* 133 */     .number("(d+),")
/* 134 */     .compile();
/*     */   
/* 136 */   private static final Pattern PATTERN_RESULT = (new PatternBuilder())
/* 137 */     .text("$$")
/* 138 */     .number("d+,")
/* 139 */     .number("(d+),")
/* 140 */     .any()
/* 141 */     .expression(",([A-Z]+)")
/* 142 */     .text("*")
/* 143 */     .number("xx")
/* 144 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, String imei, String content) {
/* 147 */     if (channel != null) {
/* 148 */       int length = 1 + imei.length() + 1 + content.length();
/* 149 */       String response = String.format("##%02d,%s,%s*", new Object[] { Integer.valueOf(length), imei, content });
/* 150 */       response = response + Checksum.sum(response) + "\r\n";
/* 151 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void requestPhoto(Channel channel, SocketAddress remoteAddress, String imei, String file) {
/* 156 */     String content = "1,D06," + file + "," + this.photo.writerIndex() + "," + Math.min(1024, this.photo.writableBytes());
/* 157 */     sendResponse(channel, remoteAddress, imei, content);
/*     */   }
/*     */   
/*     */   private String decodeAlarm(Integer alarm) {
/* 161 */     if (alarm != null) {
/* 162 */       switch (alarm.intValue()) {
/*     */         case 1:
/* 164 */           return "distanceTracking";
/*     */         case 2:
/* 166 */           return "input1Active";
/*     */         case 3:
/* 168 */           return "input1Inactive";
/*     */         case 4:
/* 170 */           return "input2Active";
/*     */         case 5:
/* 172 */           return "input2Inactive";
/*     */         case 6:
/* 174 */           return "input3Active";
/*     */         case 7:
/* 176 */           return "input3Inactive";
/*     */         case 8:
/* 178 */           return "input4Active";
/*     */         case 9:
/* 180 */           return "input4Inactive";
/*     */         case 14:
/* 182 */           return "lowPower";
/*     */         case 15:
/* 184 */           return "powerCut";
/*     */         case 16:
/* 186 */           return "powerRestored";
/*     */         case 17:
/* 188 */           return "lowBattery";
/*     */         case 18:
/* 190 */           return "overspeed";
/*     */         case 20:
/* 192 */           return "gpsAntennaCut";
/*     */         case 21:
/* 194 */           return "vibration";
/*     */         case 23:
/* 196 */           return "hardAcceleration";
/*     */         case 24:
/* 198 */           return "hardBraking";
/*     */         case 27:
/* 200 */           return "fatigueDriving";
/*     */         case 28:
/* 202 */           return "fatigueRelieve";
/*     */         case 29:
/* 204 */           return "parkingOvertime";
/*     */         case 30:
/*     */         case 31:
/* 207 */           return "fallDown";
/*     */         case 32:
/* 209 */           return "jamming";
/*     */         case 33:
/* 211 */           return "geofenceExit";
/*     */         case 34:
/* 213 */           return "geofenceEnter";
/*     */         case 35:
/* 215 */           return "idle";
/*     */         case 37:
/* 217 */           return "login";
/*     */         case 38:
/* 219 */           return "logout";
/*     */         case 39:
/* 221 */           return "illegalLogin";
/*     */         case 40:
/*     */         case 41:
/* 224 */           return "temperature";
/*     */         case 43:
/* 226 */           return "comPortError";
/*     */         case 44:
/* 228 */           return "fuelTheft";
/*     */         case 45:
/* 230 */           return "fuelFilling";
/*     */         case 46:
/* 232 */           return "lowFuel";
/*     */         case 47:
/* 234 */           return "highFuel";
/*     */         case 53:
/* 236 */           return "powerOn";
/*     */         case 54:
/* 238 */           return "powerOff";
/*     */       } 
/* 240 */       return null;
/*     */     } 
/*     */     
/* 243 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeLocationNew(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 250 */     Parser parser = new Parser(PATTERN_NEW, sentence);
/* 251 */     if (!parser.matches()) {
/* 252 */       return null;
/*     */     }
/*     */     
/* 255 */     String imei = parser.next();
/* 256 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 257 */     if (deviceSession == null) {
/* 258 */       return null;
/*     */     }
/*     */     
/* 261 */     String index = parser.next();
/*     */     
/* 263 */     Position position = new Position(getProtocolName());
/* 264 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 266 */     position.set("alarm", decodeAlarm(parser.nextInt()));
/*     */     
/* 268 */     position.setDeviceTime(parser.nextDateTime());
/*     */     
/* 270 */     Network network = new Network();
/* 271 */     network.addCellTower(CellTower.from(parser
/* 272 */           .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue()));
/*     */     
/* 274 */     position.set("battery", parser.nextDouble());
/* 275 */     position.set("batteryLevel", parser.nextInt());
/* 276 */     position.set("status", parser.nextHexInt());
/*     */     
/* 278 */     if (parser.hasNext(5)) {
/*     */       
/* 280 */       position.setValid(parser.next().equals("A"));
/* 281 */       position.setFixTime(position.getDeviceTime());
/* 282 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 283 */       position.set("sat", parser.nextInt());
/* 284 */       position.setLatitude(parser.nextDouble().doubleValue());
/* 285 */       position.setLongitude(parser.nextDouble().doubleValue());
/*     */     }
/*     */     else {
/*     */       
/* 289 */       String[] points = parser.next().split("\\|");
/* 290 */       for (String point : points) {
/* 291 */         String[] wifi = point.split(":");
/* 292 */         String mac = wifi[0].replaceAll("(..)", "$1:");
/* 293 */         network.addWifiAccessPoint(WifiAccessPoint.from(mac
/* 294 */               .substring(0, mac.length() - 1), Integer.parseInt(wifi[1])));
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 299 */     position.setNetwork(network);
/*     */     
/* 301 */     DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
/* 302 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 303 */     String response = index + ",A03," + dateFormat.format(new Date());
/* 304 */     sendResponse(channel, remoteAddress, imei, response);
/*     */     
/* 306 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeLocation(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 312 */     Parser parser = new Parser(PATTERN, sentence);
/* 313 */     if (!parser.matches()) {
/* 314 */       return null;
/*     */     }
/*     */     
/* 317 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 318 */     if (deviceSession == null) {
/* 319 */       return null;
/*     */     }
/*     */     
/* 322 */     Position position = new Position(getProtocolName());
/* 323 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 325 */     position.set("alarm", decodeAlarm(parser.nextInt()));
/*     */     
/* 327 */     position.setTime(parser.nextDateTime());
/*     */     
/* 329 */     position.setValid(parser.next().equals("A"));
/* 330 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 331 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 332 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 333 */     position.setCourse(parser.nextInt().intValue());
/* 334 */     position.setAltitude(parser.nextInt().intValue());
/*     */     
/* 336 */     position.set("odometer", parser.nextLong());
/* 337 */     position.set("hours", Long.valueOf(parser.nextLong().longValue() * 1000L));
/*     */     
/* 339 */     long status = parser.nextHexLong().longValue();
/* 340 */     position.set("rssi", Long.valueOf(BitUtil.between(status, 3, 8)));
/* 341 */     position.set("sat", Long.valueOf(BitUtil.from(status, 28)));
/* 342 */     position.set("status", Long.valueOf(status));
/*     */     
/* 344 */     int input = parser.nextHexInt().intValue();
/* 345 */     position.set("in1", Boolean.valueOf(BitUtil.check(input, 0)));
/* 346 */     position.set("in2", Boolean.valueOf(BitUtil.check(input, 1)));
/* 347 */     position.set("input", Integer.valueOf(input));
/* 348 */     position.set("output", parser.nextHexInt());
/*     */     
/* 350 */     position.setNetwork(new Network(CellTower.from(parser
/* 351 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue())));
/*     */     
/* 353 */     String[] adc = parser.next().split("\\|");
/* 354 */     for (int i = 0; i < adc.length; i++) {
/* 355 */       position.set("adc" + (i + 1), Integer.valueOf(Integer.parseInt(adc[i], 16)));
/*     */     }
/*     */     
/* 358 */     if (parser.hasNext()) {
/* 359 */       String value = parser.next();
/* 360 */       if (value.matches("\\p{XDigit}+")) {
/* 361 */         position.set("driverUniqueId", String.valueOf(Integer.parseInt(value, 16)));
/*     */       } else {
/* 363 */         Pattern pattern = Pattern.compile("[^^]+\\^([^$]+)\\$([^$]+)\\$([^^]+)[^+]+\\+\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+\\?");
/*     */         
/* 365 */         Matcher matcher = pattern.matcher(value);
/* 366 */         if (matcher.matches()) {
/* 367 */           position.set("card", matcher.group(3) + " " + matcher.group(2) + " " + matcher.group(1) + " : " + matcher
/* 368 */               .group(4) + " " + matcher.group(5) + " " + matcher
/* 369 */               .group(6) + " " + matcher.group(7));
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 374 */     if (parser.hasNext()) {
/* 375 */       String[] sensors = parser.next().split("\\|");
/* 376 */       for (int j = 0; j < sensors.length; j++) {
/* 377 */         position.set("io" + (j + 1), sensors[j]);
/*     */       }
/*     */     } 
/*     */     
/* 381 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeResult(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 387 */     Parser parser = new Parser(PATTERN_RESULT, sentence);
/* 388 */     if (!parser.matches()) {
/* 389 */       return null;
/*     */     }
/*     */     
/* 392 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 393 */     if (deviceSession == null) {
/* 394 */       return null;
/*     */     }
/*     */     
/* 397 */     Position position = new Position(getProtocolName());
/* 398 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 400 */     position.set("result", parser.next());
/*     */     
/* 402 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 409 */     ByteBuf buf = (ByteBuf)msg;
/* 410 */     int typeIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44) + 1;
/* 411 */     typeIndex = buf.indexOf(typeIndex, buf.writerIndex(), (byte)44) + 1;
/* 412 */     typeIndex = buf.indexOf(typeIndex, buf.writerIndex(), (byte)44) + 1;
/* 413 */     String type = buf.toString(typeIndex, 3, StandardCharsets.US_ASCII);
/*     */     
/* 415 */     if (type.startsWith("B"))
/*     */     {
/* 417 */       return decodeResult(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII));
/*     */     }
/* 419 */     if (type.equals("D05")) {
/*     */       
/* 421 */       String sentence = buf.toString(StandardCharsets.US_ASCII);
/* 422 */       Parser parser = new Parser(PATTERN_PHOTO, sentence);
/* 423 */       if (parser.matches()) {
/* 424 */         String imei = parser.next();
/* 425 */         int length = parser.nextInt().intValue();
/* 426 */         String photoId = parser.next();
/* 427 */         this.photo = Unpooled.buffer(length);
/* 428 */         requestPhoto(channel, remoteAddress, imei, photoId);
/*     */       }
/*     */     
/* 431 */     } else if (type.equals("D06")) {
/*     */       
/* 433 */       if (this.photo == null) {
/* 434 */         return null;
/*     */       }
/* 436 */       int dataIndex = buf.indexOf(typeIndex + 4, buf.writerIndex(), (byte)44) + 1;
/* 437 */       dataIndex = buf.indexOf(dataIndex, buf.writerIndex(), (byte)44) + 1;
/* 438 */       dataIndex = buf.indexOf(dataIndex, buf.writerIndex(), (byte)44) + 1;
/* 439 */       String sentence = buf.toString(buf.readerIndex(), dataIndex, StandardCharsets.US_ASCII);
/* 440 */       Parser parser = new Parser(PATTERN_PHOTO_DATA, sentence);
/* 441 */       if (parser.matches()) {
/* 442 */         String imei = parser.next();
/* 443 */         String photoId = parser.next();
/* 444 */         parser.nextInt();
/* 445 */         parser.nextInt();
/* 446 */         buf.readerIndex(dataIndex);
/* 447 */         this.photo.writeBytes(buf.readBytes(buf.readableBytes() - 3));
/* 448 */         if (this.photo.isWritable()) {
/* 449 */           requestPhoto(channel, remoteAddress, imei, photoId);
/*     */         } else {
/* 451 */           Position position = new Position(getProtocolName());
/* 452 */           position.setDeviceId(getDeviceSession(channel, remoteAddress, new String[] { imei }).getDeviceId());
/* 453 */           getLastLocation(position, null);
/* 454 */           position.set("image", Context.getMediaManager().writeFile(imei, this.photo, "jpg"));
/* 455 */           this.photo.release();
/* 456 */           this.photo = null;
/* 457 */           return position;
/*     */         } 
/*     */       } 
/*     */     } else {
/* 461 */       if (type.equals("A03"))
/*     */       {
/* 463 */         return decodeLocationNew(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII));
/*     */       }
/*     */ 
/*     */       
/* 467 */       return decodeLocation(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII));
/*     */     } 
/*     */ 
/*     */     
/* 471 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FifotrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */