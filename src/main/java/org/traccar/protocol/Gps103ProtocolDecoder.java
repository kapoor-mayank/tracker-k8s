/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
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
/*     */ public class Gps103ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  41 */   private int photoPackets = 0;
/*     */   private ByteBuf photo;
/*     */   
/*     */   public Gps103ProtocolDecoder(Protocol protocol) {
/*  45 */     super(protocol);
/*     */   }
/*     */   
/*  48 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  49 */     .text("imei:")
/*  50 */     .number("(d+),")
/*  51 */     .expression("([^,]+),")
/*  52 */     .groupBegin()
/*  53 */     .number("(dd)/?(dd)/?(dd) ?")
/*  54 */     .number("(dd):?(dd)(?:dd)?,")
/*  55 */     .or()
/*  56 */     .number("d*,")
/*  57 */     .groupEnd()
/*  58 */     .expression("([^,]+)?,")
/*  59 */     .groupBegin()
/*  60 */     .text("L,,,")
/*  61 */     .number("(x+),,")
/*  62 */     .number("(x+),,,")
/*  63 */     .or()
/*  64 */     .text("F,")
/*  65 */     .groupBegin()
/*  66 */     .number("(dd)(dd)(dd)(?:.d+)?")
/*  67 */     .or()
/*  68 */     .number("(?:d{1,5}.d+)?")
/*  69 */     .groupEnd()
/*  70 */     .text(",")
/*  71 */     .expression("([AV]),")
/*  72 */     .expression("([NS]),").optional()
/*  73 */     .number("(d+)(dd.d+),")
/*  74 */     .expression("([NS]),").optional()
/*  75 */     .expression("([EW]),").optional()
/*  76 */     .number("(d+)(dd.d+),")
/*  77 */     .expression("([EW])?,").optional()
/*  78 */     .number("(d+.?d*)?").optional()
/*  79 */     .number(",(d+.?d*)?").optional()
/*  80 */     .number(",(d+.?d*)?").optional()
/*  81 */     .number(",([01])?").optional()
/*  82 */     .number(",([01])?").optional()
/*  83 */     .groupBegin()
/*  84 */     .number(",(?:(d+.d+)%)?")
/*  85 */     .number(",(?:(d+.d+)%|d+)?")
/*  86 */     .groupEnd("?")
/*  87 */     .number(",([-+]?d+)?").optional()
/*  88 */     .groupEnd()
/*  89 */     .any()
/*  90 */     .compile();
/*     */   
/*  92 */   private static final Pattern PATTERN_OBD = (new PatternBuilder())
/*  93 */     .text("imei:")
/*  94 */     .number("(d+),")
/*  95 */     .expression("OBD,")
/*  96 */     .number("(dd)(dd)(dd)")
/*  97 */     .number("(dd)(dd)(dd),")
/*  98 */     .number("(d+)?,")
/*  99 */     .number("(d+.d+)?,")
/* 100 */     .number("(d+.d+)?,")
/* 101 */     .number("(d+)?,")
/* 102 */     .number("(d+),")
/* 103 */     .number("(d+.?d*%),")
/* 104 */     .number("(?:([-+]?d+)|[-+]?),")
/* 105 */     .number("(d+.?d*%),")
/* 106 */     .number("(d+),")
/* 107 */     .number("(d+.d+),")
/* 108 */     .number("([^;]*)")
/* 109 */     .any()
/* 110 */     .compile();
/*     */   
/* 112 */   private static final Pattern PATTERN_ALT = (new PatternBuilder())
/* 113 */     .text("imei:")
/* 114 */     .number("(d+),")
/* 115 */     .expression("[^,]+,")
/* 116 */     .expression("(?:-+|(.+)),")
/* 117 */     .expression("(?:-+|(.+)),")
/* 118 */     .expression("(?:-+|(.+)),")
/* 119 */     .number("(dd)(dd)(dd),")
/* 120 */     .number("(dd)(dd)(dd),")
/* 121 */     .number("(d+),")
/* 122 */     .number("(d),")
/* 123 */     .number("(-?d+.d+),")
/* 124 */     .number("(-?d+.d+),")
/* 125 */     .number("(d+),")
/* 126 */     .number("(d+),")
/* 127 */     .number("(-?d+),")
/* 128 */     .number("(d+.d+),")
/* 129 */     .number("(d+),")
/* 130 */     .number("([01]),")
/* 131 */     .number("([01]),")
/* 132 */     .expression("(?:-+|(.+))")
/* 133 */     .any()
/* 134 */     .compile();
/*     */   
/*     */   private String decodeAlarm(String value) {
/* 137 */     if (value.startsWith("T:"))
/* 138 */       return "temperature"; 
/* 139 */     if (value.startsWith("oil")) {
/* 140 */       return "fuelLeak";
/*     */     }
/* 142 */     switch (value) {
/*     */       case "tracker":
/* 144 */         return null;
/*     */       case "help me":
/* 146 */         return "sos";
/*     */       case "low battery":
/* 148 */         return "lowBattery";
/*     */       case "stockade":
/* 150 */         return "geofence";
/*     */       case "move":
/* 152 */         return "movement";
/*     */       case "speed":
/* 154 */         return "overspeed";
/*     */       case "acc on":
/* 156 */         return "powerOn";
/*     */       case "acc off":
/* 158 */         return "powerOff";
/*     */       case "door alarm":
/* 160 */         return "door";
/*     */       case "ac alarm":
/* 162 */         return "powerCut";
/*     */       case "accident alarm":
/* 164 */         return "accident";
/*     */       case "sensor alarm":
/* 166 */         return "shock";
/*     */       case "bonnet alarm":
/* 168 */         return "bonnet";
/*     */       case "footbrake alarm":
/* 170 */         return "footBrake";
/*     */       case "DTC":
/* 172 */         return "fault";
/*     */     } 
/* 174 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeRegular(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 180 */     Parser parser = new Parser(PATTERN, sentence);
/* 181 */     if (!parser.matches()) {
/* 182 */       return null;
/*     */     }
/*     */     
/* 185 */     String imei = parser.next();
/* 186 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 187 */     if (deviceSession == null) {
/* 188 */       return null;
/*     */     }
/*     */     
/* 191 */     Position position = new Position(getProtocolName());
/* 192 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 194 */     String alarm = parser.next();
/* 195 */     position.set("alarm", decodeAlarm(alarm));
/* 196 */     if (alarm.equals("help me")) {
/* 197 */       if (channel != null) {
/* 198 */         channel.writeAndFlush(new NetworkMessage("**,imei:" + imei + ",E;", remoteAddress));
/*     */       }
/* 200 */     } else if (alarm.startsWith("vt")) {
/* 201 */       this.photoPackets = Integer.parseInt(alarm.substring(2));
/* 202 */       this.photo = Unpooled.buffer();
/* 203 */     } else if (alarm.equals("acc on")) {
/* 204 */       position.set("ignition", Boolean.valueOf(true));
/* 205 */     } else if (alarm.equals("acc off")) {
/* 206 */       position.set("ignition", Boolean.valueOf(false));
/* 207 */     } else if (alarm.startsWith("T:")) {
/* 208 */       position.set("temp1", Double.valueOf(Double.parseDouble(alarm.substring(2))));
/* 209 */     } else if (alarm.startsWith("oil ")) {
/* 210 */       position.set("fuel", Double.valueOf(Double.parseDouble(alarm.substring(4))));
/* 211 */     } else if (!position.getAttributes().containsKey("alarm") && !alarm.equals("tracker")) {
/* 212 */       position.set("event", alarm);
/*     */     } 
/*     */ 
/*     */     
/* 216 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 218 */     int localHours = parser.nextInt(0);
/* 219 */     int localMinutes = parser.nextInt(0);
/*     */     
/* 221 */     String rfid = parser.next();
/* 222 */     if (alarm.equals("rfid")) {
/* 223 */       position.set("driverUniqueId", rfid);
/*     */     }
/*     */     
/* 226 */     if (parser.hasNext(2)) {
/*     */       
/* 228 */       getLastLocation(position, null);
/*     */       
/* 230 */       position.setNetwork(new Network(CellTower.fromLacCid(parser.nextHexInt(0), parser.nextHexInt(0))));
/*     */     }
/*     */     else {
/*     */       
/* 234 */       String utcHours = parser.next();
/* 235 */       String utcMinutes = parser.next();
/*     */       
/* 237 */       dateBuilder.setTime(localHours, localMinutes, parser.nextInt(0));
/*     */ 
/*     */       
/* 240 */       if (utcHours != null && utcMinutes != null) {
/* 241 */         int deltaMinutes = (localHours - Integer.parseInt(utcHours)) * 60;
/* 242 */         deltaMinutes += localMinutes - Integer.parseInt(utcMinutes);
/* 243 */         if (deltaMinutes <= -720) {
/* 244 */           deltaMinutes += 1440;
/* 245 */         } else if (deltaMinutes > 720) {
/* 246 */           deltaMinutes -= 1440;
/*     */         } 
/* 248 */         dateBuilder.addMinute(-deltaMinutes);
/*     */       } 
/* 250 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 252 */       position.setValid(parser.next().equals("A"));
/* 253 */       position.setFixTime(position.getDeviceTime());
/* 254 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_HEM));
/* 255 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_HEM));
/* 256 */       position.setSpeed(parser.nextDouble(0.0D));
/* 257 */       position.setCourse(parser.nextDouble(0.0D));
/* 258 */       position.setAltitude(parser.nextDouble(0.0D));
/*     */       
/* 260 */       if (parser.hasNext()) {
/* 261 */         position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*     */       }
/* 263 */       if (parser.hasNext()) {
/* 264 */         position.set("door", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*     */       }
/* 266 */       position.set("fuel1", parser.nextDouble());
/* 267 */       position.set("fuel2", parser.nextDouble());
/* 268 */       position.set("temp1", parser.nextInt());
/*     */     } 
/*     */ 
/*     */     
/* 272 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeObd(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 277 */     Parser parser = new Parser(PATTERN_OBD, sentence);
/* 278 */     if (!parser.matches()) {
/* 279 */       return null;
/*     */     }
/*     */     
/* 282 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 283 */     if (deviceSession == null) {
/* 284 */       return null;
/*     */     }
/*     */     
/* 287 */     Position position = new Position(getProtocolName());
/* 288 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 290 */     getLastLocation(position, parser.nextDateTime());
/*     */     
/* 292 */     position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/* 293 */     parser.nextDouble(0.0D);
/* 294 */     position.set("fuelConsumption", Double.valueOf(parser.nextDouble(0.0D)));
/* 295 */     if (parser.hasNext()) {
/* 296 */       position.set("hours", Long.valueOf(UnitsConverter.msFromHours(parser.nextInt().intValue())));
/*     */     }
/* 298 */     position.set("obdSpeed", Integer.valueOf(parser.nextInt(0)));
/* 299 */     position.set("engineLoad", parser.next());
/* 300 */     position.set("coolantTemp", parser.nextInt());
/* 301 */     position.set("throttle", parser.next());
/* 302 */     position.set("rpm", Integer.valueOf(parser.nextInt(0)));
/* 303 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/* 304 */     position.set("dtcs", parser.next().replace(',', ' ').trim());
/*     */     
/* 306 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeAlternative(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 312 */     Parser parser = new Parser(PATTERN_ALT, sentence);
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
/* 325 */     position.set("event", parser.next());
/* 326 */     position.set("sensorId", parser.next());
/* 327 */     position.set("sensorVoltage", parser.nextDouble());
/*     */     
/* 329 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 331 */     position.set("rssi", parser.nextInt());
/*     */     
/* 333 */     position.setValid((parser.nextInt().intValue() > 0));
/* 334 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 335 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 336 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 337 */     position.setCourse(parser.nextInt().intValue());
/* 338 */     position.setAltitude(parser.nextInt().intValue());
/*     */     
/* 340 */     position.set("hdop", parser.nextDouble());
/* 341 */     position.set("sat", parser.nextInt());
/* 342 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 343 */     position.set("charge", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 344 */     position.set("error", parser.next());
/*     */     
/* 346 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodePhoto(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 351 */     String imei = sentence.substring(5, 20);
/* 352 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 353 */     if (deviceSession == null) {
/* 354 */       return null;
/*     */     }
/*     */     
/* 357 */     ByteBuf buf = Unpooled.wrappedBuffer(DataConverter.parseHex(sentence
/* 358 */           .substring(24, sentence.endsWith(";") ? (sentence.length() - 1) : sentence.length())));
/* 359 */     int index = buf.readUnsignedShortLE();
/* 360 */     this.photo.writeBytes(buf, buf.readerIndex() + 2, buf.readableBytes() - 4);
/*     */     
/* 362 */     if (index + 1 >= this.photoPackets) {
/* 363 */       Position position = new Position(getProtocolName());
/* 364 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 366 */       getLastLocation(position, null);
/*     */       
/*     */       try {
/* 369 */         position.set("image", Context.getMediaManager().writeFile(imei, this.photo, "jpg"));
/*     */       } finally {
/* 371 */         this.photoPackets = 0;
/* 372 */         this.photo.release();
/* 373 */         this.photo = null;
/*     */       } 
/*     */       
/* 376 */       return position;
/*     */     } 
/* 378 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 386 */     String sentence = (String)msg;
/*     */     
/* 388 */     if (sentence.contains("imei:") && sentence.length() <= 30) {
/* 389 */       if (channel != null) {
/* 390 */         channel.writeAndFlush(new NetworkMessage("LOAD", remoteAddress));
/* 391 */         Matcher matcher = Pattern.compile("imei:(\\d+),").matcher(sentence);
/* 392 */         if (matcher.find()) {
/* 393 */           getDeviceSession(channel, remoteAddress, new String[] { matcher.group(1) });
/*     */         }
/*     */       } 
/* 396 */       return null;
/*     */     } 
/*     */     
/* 399 */     if (!sentence.isEmpty() && Character.isDigit(sentence.charAt(0))) {
/* 400 */       if (channel != null) {
/* 401 */         channel.writeAndFlush(new NetworkMessage("ON", remoteAddress));
/*     */       }
/* 403 */       int start = sentence.indexOf("imei:");
/* 404 */       if (start >= 0) {
/* 405 */         sentence = sentence.substring(start);
/*     */       } else {
/* 407 */         return null;
/*     */       } 
/*     */     } 
/*     */     
/* 411 */     if (sentence.substring(21, 23).equals("vr"))
/* 412 */       return decodePhoto(channel, remoteAddress, sentence); 
/* 413 */     if (sentence.substring(21, 24).contains("OBD"))
/* 414 */       return decodeObd(channel, remoteAddress, sentence); 
/* 415 */     if (sentence.endsWith("*")) {
/* 416 */       return decodeAlternative(channel, remoteAddress, sentence);
/*     */     }
/* 418 */     return decodeRegular(channel, remoteAddress, sentence);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gps103ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */