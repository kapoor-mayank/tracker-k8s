/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ public class MegastekProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public MegastekProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN_GPRMC = (new PatternBuilder())
/*  39 */     .text("$GPRMC,")
/*  40 */     .number("(dd)(dd)(dd).(ddd),")
/*  41 */     .expression("([AV]),")
/*  42 */     .number("(d+)(dd.d+),([NS]),")
/*  43 */     .number("(d+)(dd.d+),([EW]),")
/*  44 */     .number("(d+.d+)?,")
/*  45 */     .number("(d+.d+)?,")
/*  46 */     .number("(dd)(dd)(dd)")
/*  47 */     .any()
/*  48 */     .compile();
/*     */   
/*  50 */   private static final Pattern PATTERN_SIMPLE = (new PatternBuilder())
/*  51 */     .expression("[FL],")
/*  52 */     .expression("([^,]*),")
/*  53 */     .number("imei:(d+),")
/*  54 */     .number("(d+/?d*)?,")
/*  55 */     .number("(d+.d+)?,")
/*  56 */     .number("Battery=(d+)%,,?")
/*  57 */     .number("(d)?,")
/*  58 */     .number("(d+)?,")
/*  59 */     .number("(d+)?,")
/*  60 */     .number("(xxxx),")
/*  61 */     .number("(xxxx);")
/*  62 */     .any()
/*  63 */     .compile();
/*     */   
/*  65 */   private static final Pattern PATTERN_ALTERNATIVE = (new PatternBuilder())
/*  66 */     .number("(d+),")
/*  67 */     .number("(d+),")
/*  68 */     .number("(xxxx),")
/*  69 */     .number("(xxxx),")
/*  70 */     .number("(d+),")
/*  71 */     .number("(d+),")
/*  72 */     .number("(d+),")
/*  73 */     .number("(d+),")
/*  74 */     .number("(?:(d+),)?")
/*  75 */     .number("(d.?d*),")
/*  76 */     .groupBegin()
/*  77 */     .number("(d.dd),")
/*  78 */     .number("(d.dd),")
/*  79 */     .groupEnd("?")
/*  80 */     .expression("([^;]+);")
/*  81 */     .any()
/*  82 */     .compile();
/*     */ 
/*     */   
/*     */   private boolean parseLocation(String location, Position position) {
/*  86 */     Parser parser = new Parser(PATTERN_GPRMC, location);
/*  87 */     if (!parser.matches()) {
/*  88 */       return false;
/*     */     }
/*     */ 
/*     */     
/*  92 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/*  94 */     position.setValid(parser.next().equals("A"));
/*  95 */     position.setLatitude(parser.nextCoordinate());
/*  96 */     position.setLongitude(parser.nextCoordinate());
/*  97 */     position.setSpeed(parser.nextDouble(0.0D));
/*  98 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 100 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 101 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 103 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeOld(Channel channel, SocketAddress remoteAddress, String sentence) {
/*     */     String id, location, status;
/* 109 */     boolean simple = (sentence.charAt(3) == ',' || sentence.charAt(6) == ',');
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 115 */     if (simple) {
/*     */       
/* 117 */       int beginIndex = sentence.indexOf(',') + 1;
/* 118 */       int endIndex = sentence.indexOf(',', beginIndex);
/* 119 */       id = sentence.substring(beginIndex, endIndex);
/*     */       
/* 121 */       beginIndex = endIndex + 1;
/* 122 */       endIndex = sentence.indexOf('*', beginIndex);
/* 123 */       if (endIndex != -1) {
/* 124 */         endIndex += 3;
/*     */       } else {
/* 126 */         endIndex = sentence.length();
/*     */       } 
/* 128 */       location = sentence.substring(beginIndex, endIndex);
/*     */       
/* 130 */       beginIndex = endIndex + 1;
/* 131 */       if (beginIndex > sentence.length()) {
/* 132 */         beginIndex = endIndex;
/*     */       }
/* 134 */       status = sentence.substring(beginIndex);
/*     */     }
/*     */     else {
/*     */       
/* 138 */       int beginIndex = 3;
/* 139 */       int endIndex = beginIndex + 16;
/* 140 */       id = sentence.substring(beginIndex, endIndex).trim();
/*     */       
/* 142 */       beginIndex = endIndex + 2;
/* 143 */       endIndex = sentence.indexOf('*', beginIndex) + 3;
/* 144 */       location = sentence.substring(beginIndex, endIndex);
/*     */       
/* 146 */       beginIndex = endIndex + 1;
/* 147 */       status = sentence.substring(beginIndex);
/*     */     } 
/*     */ 
/*     */     
/* 151 */     Position position = new Position(getProtocolName());
/* 152 */     if (!parseLocation(location, position)) {
/* 153 */       return null;
/*     */     }
/*     */     
/* 156 */     if (simple) {
/*     */       
/* 158 */       Parser parser = new Parser(PATTERN_SIMPLE, status);
/* 159 */       if (parser.matches())
/*     */       {
/* 161 */         position.set("alarm", decodeAlarm(parser.next()));
/*     */         
/* 163 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next(), id });
/* 164 */         if (deviceSession == null) {
/* 165 */           return null;
/*     */         }
/* 167 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 169 */         String sat = parser.next();
/* 170 */         if (sat.contains("/")) {
/* 171 */           position.set("sat", Integer.valueOf(Integer.parseInt(sat.split("/")[0])));
/* 172 */           position.set("satVisible", Integer.valueOf(Integer.parseInt(sat.split("/")[1])));
/*     */         } else {
/* 174 */           position.set("sat", Integer.valueOf(Integer.parseInt(sat)));
/*     */         } 
/*     */         
/* 177 */         position.setAltitude(parser.nextDouble(0.0D));
/*     */         
/* 179 */         position.set("batteryLevel", Double.valueOf(parser.nextDouble(0.0D)));
/*     */         
/* 181 */         String charger = parser.next();
/* 182 */         if (charger != null) {
/* 183 */           position.set("charge", Boolean.valueOf((Integer.parseInt(charger) == 1)));
/*     */         }
/*     */         
/* 186 */         if (parser.hasNext(4)) {
/* 187 */           position.setNetwork(new Network(CellTower.from(parser
/* 188 */                   .nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0))));
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 193 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 194 */         if (deviceSession == null) {
/* 195 */           return null;
/*     */         }
/* 197 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */       }
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 203 */       Parser parser = new Parser(PATTERN_ALTERNATIVE, status);
/* 204 */       if (parser.matches()) {
/*     */         
/* 206 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 207 */         if (deviceSession == null) {
/* 208 */           return null;
/*     */         }
/* 210 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 212 */         position.setNetwork(new Network(CellTower.from(parser.nextInt(0), parser.nextInt(0), parser
/* 213 */                 .nextHexInt(0), parser.nextHexInt(0), parser.nextInt(0))));
/*     */         
/* 215 */         position.set("batteryLevel", parser.nextDouble());
/*     */         
/* 217 */         position.set("flags", parser.next());
/* 218 */         position.set("input", parser.next());
/* 219 */         position.set("output", parser.next());
/* 220 */         position.set("adc1", parser.next());
/* 221 */         position.set("adc2", parser.next());
/* 222 */         position.set("adc3", parser.next());
/* 223 */         position.set("alarm", decodeAlarm(parser.next()));
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 228 */     return position;
/*     */   }
/*     */   
/* 231 */   private static final Pattern PATTERN_NEW = (new PatternBuilder())
/* 232 */     .number("dddd").optional()
/* 233 */     .text("$MGV")
/* 234 */     .number("ddd,")
/* 235 */     .number("(d+),")
/* 236 */     .expression("[^,]*,")
/* 237 */     .expression("([RS]),")
/* 238 */     .number("(dd)(dd)(dd),")
/* 239 */     .number("(dd)(dd)(dd),")
/* 240 */     .expression("([AV]),")
/* 241 */     .number("(d+)(dd.d+),([NS]),")
/* 242 */     .number("(d+)(dd.d+),([EW]),")
/* 243 */     .number("dd,")
/* 244 */     .number("(dd),")
/* 245 */     .number("dd,")
/* 246 */     .number("(d+.d+),")
/* 247 */     .number("(d+.d+)?,")
/* 248 */     .number("(d+.d+)?,")
/* 249 */     .number("(-?d+.d+),")
/* 250 */     .number("(d+.d+)?,")
/* 251 */     .number("(d+),")
/* 252 */     .number("(d+),")
/* 253 */     .number("(xxxx)?,")
/* 254 */     .number("(x+)?,")
/* 255 */     .number("(d+)?,")
/* 256 */     .groupBegin()
/* 257 */     .number("([01]{4})?,")
/* 258 */     .number("([01]{4})?,")
/* 259 */     .number("(d+)?,")
/* 260 */     .number("(d+)?,")
/* 261 */     .number("(d+)?,")
/* 262 */     .or()
/* 263 */     .number("(d+),")
/* 264 */     .number("(d+),")
/* 265 */     .number("(d+),")
/* 266 */     .number("(d+),")
/* 267 */     .number("(d+),")
/* 268 */     .groupEnd()
/* 269 */     .groupBegin()
/* 270 */     .number("(-?d+.?d*)")
/* 271 */     .or().text(" ")
/* 272 */     .groupEnd("?").text(",")
/* 273 */     .groupBegin()
/* 274 */     .number("(-?d+.?d*)")
/* 275 */     .or().text(" ")
/* 276 */     .groupEnd("?").text(",")
/* 277 */     .number("(d+)?,")
/* 278 */     .expression("[^,]*,")
/* 279 */     .number("(d+)?,")
/* 280 */     .expression("([^,]*)")
/* 281 */     .any()
/* 282 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeNew(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 286 */     Parser parser = new Parser(PATTERN_NEW, sentence);
/* 287 */     if (!parser.matches()) {
/* 288 */       return null;
/*     */     }
/*     */     
/* 291 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 292 */     if (deviceSession == null) {
/* 293 */       return null;
/*     */     }
/*     */     
/* 296 */     Position position = new Position(getProtocolName());
/* 297 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 299 */     if (parser.next().equals("S")) {
/* 300 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/* 303 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 305 */     position.setValid(parser.next().equals("A"));
/* 306 */     position.setLatitude(parser.nextCoordinate());
/* 307 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 309 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 310 */     position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/*     */     
/* 312 */     position.setSpeed(parser.nextDouble(0.0D));
/* 313 */     position.setCourse(parser.nextDouble(0.0D));
/* 314 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 316 */     if (parser.hasNext()) {
/* 317 */       position.set("odometer", Double.valueOf(parser.nextDouble(0.0D) * 1000.0D));
/*     */     }
/*     */     
/* 320 */     int mcc = parser.nextInt().intValue();
/* 321 */     int mnc = parser.nextInt().intValue();
/* 322 */     Integer lac = parser.nextHexInt();
/* 323 */     Integer cid = parser.nextHexInt();
/* 324 */     Integer rssi = parser.nextInt();
/* 325 */     if (lac != null && cid != null) {
/* 326 */       CellTower tower = CellTower.from(mcc, mnc, lac.intValue(), cid.intValue());
/* 327 */       if (rssi != null) {
/* 328 */         tower.setSignalStrength(rssi);
/*     */       }
/* 330 */       position.setNetwork(new Network(tower));
/*     */     } 
/*     */     
/* 333 */     if (parser.hasNext(5)) {
/* 334 */       position.set("input", Integer.valueOf(parser.nextBinInt(0)));
/* 335 */       position.set("output", Integer.valueOf(parser.nextBinInt(0)));
/* 336 */       for (int j = 1; j <= 3; j++) {
/* 337 */         position.set("adc" + j, Integer.valueOf(parser.nextInt(0)));
/*     */       }
/*     */     } 
/*     */     
/* 341 */     if (parser.hasNext(5)) {
/* 342 */       position.set("heartRate", parser.nextInt());
/* 343 */       position.set("steps", parser.nextInt());
/* 344 */       position.set("activityTime", parser.nextInt());
/* 345 */       position.set("lightSleepTime", parser.nextInt());
/* 346 */       position.set("deepSleepTime", parser.nextInt());
/*     */     } 
/*     */     
/* 349 */     for (int i = 1; i <= 2; i++) {
/* 350 */       String adc = parser.next();
/* 351 */       if (adc != null) {
/* 352 */         position.set("temp" + i, Double.valueOf(Double.parseDouble(adc)));
/*     */       }
/*     */     } 
/*     */     
/* 356 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 358 */     String battery = parser.next();
/* 359 */     if (battery != null) {
/* 360 */       position.set("battery", Integer.valueOf(Integer.parseInt(battery)));
/*     */     }
/*     */     
/* 363 */     position.set("alarm", decodeAlarm(parser.next()));
/*     */     
/* 365 */     return position;
/*     */   }
/*     */   
/*     */   private String decodeAlarm(String value) {
/* 369 */     value = value.toLowerCase();
/* 370 */     if (value.startsWith("geo")) {
/* 371 */       if (value.endsWith("in"))
/* 372 */         return "geofenceEnter"; 
/* 373 */       if (value.endsWith("out")) {
/* 374 */         return "geofenceExit";
/*     */       }
/*     */     } 
/* 377 */     switch (value) {
/*     */       case "poweron":
/* 379 */         return "powerOn";
/*     */       case "poweroff":
/* 381 */         return "powerOn";
/*     */       case "sos":
/*     */       case "help":
/* 384 */         return "sos";
/*     */       case "over speed":
/*     */       case "overspeed":
/* 387 */         return "overspeed";
/*     */       case "lowspeed":
/* 389 */         return "lowspeed";
/*     */       case "low battery":
/*     */       case "lowbattery":
/* 392 */         return "lowBattery";
/*     */       case "vib":
/* 394 */         return "vibration";
/*     */       case "move in":
/* 396 */         return "geofenceEnter";
/*     */       case "move out":
/* 398 */         return "geofenceExit";
/*     */       case "error":
/* 400 */         return "fault";
/*     */     } 
/* 402 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 410 */     String sentence = (String)msg;
/*     */     
/* 412 */     if (sentence.contains("$MG")) {
/* 413 */       return decodeNew(channel, remoteAddress, sentence);
/*     */     }
/* 415 */     return decodeOld(channel, remoteAddress, sentence);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MegastekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */