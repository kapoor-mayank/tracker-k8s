/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ public class Tk103ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private boolean decodeLow;
/*     */   
/*     */   public Tk103ProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*  42 */     this.decodeLow = Context.getConfig().getBoolean(getProtocolName() + ".decodeLow");
/*     */   }
/*     */   
/*  45 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  46 */     .text("(").optional()
/*  47 */     .number("(d+)(,)?")
/*  48 */     .expression("(.{4}),?")
/*  49 */     .number("(d*)")
/*  50 */     .number("(dd)(dd)(dd),?")
/*  51 */     .expression("([AV]),?")
/*  52 */     .number("(d+)(dd.d+)")
/*  53 */     .expression("([NS]),?")
/*  54 */     .number("(d+)(dd.d+)")
/*  55 */     .expression("([EW]),?")
/*  56 */     .number("([ d.]{1,5})(?:d*,)?")
/*  57 */     .number("(dd)(dd)(dd),?")
/*  58 */     .groupBegin()
/*  59 */     .number("(?:([d.]{6})|(dd)),?")
/*  60 */     .number("([01])")
/*  61 */     .number("([01])")
/*  62 */     .number("(x)")
/*  63 */     .number("(x)")
/*  64 */     .number("(x)")
/*  65 */     .number("(xxx)")
/*  66 */     .number("L(x+)")
/*  67 */     .or()
/*  68 */     .number("(d+.d+)")
/*  69 */     .groupEnd()
/*  70 */     .any()
/*  71 */     .number("([+-]ddd.d)?")
/*  72 */     .text(")").optional()
/*  73 */     .compile();
/*     */   
/*  75 */   private static final Pattern PATTERN_BATTERY = (new PatternBuilder())
/*  76 */     .text("(").optional()
/*  77 */     .number("(d+),")
/*  78 */     .text("ZC20,")
/*  79 */     .number("(dd)(dd)(dd),")
/*  80 */     .number("(dd)(dd)(dd),")
/*  81 */     .number("(d+),")
/*  82 */     .number("(d+),")
/*  83 */     .number("(d+),")
/*  84 */     .number("d+")
/*  85 */     .any()
/*  86 */     .compile();
/*     */   
/*  88 */   private static final Pattern PATTERN_NETWORK = (new PatternBuilder())
/*  89 */     .text("(").optional()
/*  90 */     .number("(d{12})")
/*  91 */     .text("BZ00,")
/*  92 */     .number("(d+),")
/*  93 */     .number("(d+),")
/*  94 */     .number("(x+),")
/*  95 */     .number("(x+),")
/*  96 */     .any()
/*  97 */     .compile();
/*     */   
/*  99 */   private static final Pattern PATTERN_LBSWIFI = (new PatternBuilder())
/* 100 */     .text("(").optional()
/* 101 */     .number("(d+),")
/* 102 */     .expression("(.{4}),")
/* 103 */     .number("(d+),")
/* 104 */     .number("(d+),")
/* 105 */     .number("(d+),")
/* 106 */     .number("(d+),")
/* 107 */     .number("(d+),")
/* 108 */     .number("((?:(?:xx:){5}(?:xx)\\*[-+]?d+\\*d+,)*)")
/* 109 */     .number("(dd)(dd)(dd),")
/* 110 */     .number("(dd)(dd)(dd)")
/* 111 */     .any()
/* 112 */     .compile();
/*     */   
/* 114 */   private static final Pattern PATTERN_COMMAND_RESULT = (new PatternBuilder())
/* 115 */     .text("(").optional()
/* 116 */     .number("(d+),")
/* 117 */     .expression(".{4},")
/* 118 */     .number("(dd)(dd)(dd),")
/* 119 */     .number("(dd)(dd)(dd),")
/* 120 */     .expression("\\$([\\s\\S]*?)(?:\\$|$)")
/* 121 */     .any()
/* 122 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/* 125 */     switch (value) {
/*     */       case 1:
/* 127 */         return "accident";
/*     */       case 2:
/* 129 */         return "sos";
/*     */       case 3:
/* 131 */         return "vibration";
/*     */       case 4:
/* 133 */         return "lowspeed";
/*     */       case 5:
/* 135 */         return "overspeed";
/*     */       case 6:
/* 137 */         return "geofenceExit";
/*     */     } 
/* 139 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeType(Position position, String type, String data) {
/* 144 */     switch (type) {
/*     */       case "BO01":
/* 146 */         position.set("alarm", decodeAlarm(data.charAt(0) - 48));
/*     */         break;
/*     */       case "ZC11":
/*     */       case "DW31":
/*     */       case "DW51":
/* 151 */         position.set("alarm", "movement");
/*     */         break;
/*     */       case "ZC12":
/*     */       case "DW32":
/*     */       case "DW52":
/* 156 */         position.set("alarm", "lowBattery");
/*     */         break;
/*     */       case "ZC13":
/*     */       case "DW33":
/*     */       case "DW53":
/* 161 */         position.set("alarm", "powerCut");
/*     */         break;
/*     */       case "ZC15":
/*     */       case "DW35":
/*     */       case "DW55":
/* 166 */         position.set("ignition", Boolean.valueOf(true));
/*     */         break;
/*     */       case "ZC16":
/*     */       case "DW36":
/*     */       case "DW56":
/* 171 */         position.set("ignition", Boolean.valueOf(false));
/*     */         break;
/*     */       case "ZC29":
/*     */       case "DW42":
/*     */       case "DW62":
/* 176 */         position.set("ignition", Boolean.valueOf(true));
/*     */         break;
/*     */       case "ZC17":
/*     */       case "DW37":
/*     */       case "DW57":
/* 181 */         position.set("alarm", "removing");
/*     */         break;
/*     */       case "ZC25":
/*     */       case "DW3E":
/*     */       case "DW5E":
/* 186 */         position.set("alarm", "sos");
/*     */         break;
/*     */       case "ZC26":
/*     */       case "DW3F":
/*     */       case "DW5F":
/* 191 */         position.set("alarm", "tampering");
/*     */         break;
/*     */       case "ZC27":
/*     */       case "DW40":
/*     */       case "DW60":
/* 196 */         position.set("alarm", "lowPower");
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Integer decodeBattery(int value) {
/* 204 */     switch (value) {
/*     */       case 6:
/* 206 */         return Integer.valueOf(100);
/*     */       case 5:
/* 208 */         return Integer.valueOf(80);
/*     */       case 4:
/* 210 */         return Integer.valueOf(50);
/*     */       case 3:
/* 212 */         return Integer.valueOf(20);
/*     */       case 2:
/* 214 */         return Integer.valueOf(10);
/*     */     } 
/* 216 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeBattery(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 221 */     Parser parser = new Parser(PATTERN_BATTERY, sentence);
/* 222 */     if (!parser.matches()) {
/* 223 */       return null;
/*     */     }
/*     */     
/* 226 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 227 */     if (deviceSession == null) {
/* 228 */       return null;
/*     */     }
/*     */     
/* 231 */     Position position = new Position(getProtocolName());
/* 232 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 234 */     getLastLocation(position, parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 236 */     int batterylevel = parser.nextInt(0);
/* 237 */     if (batterylevel != 255) {
/* 238 */       position.set("batteryLevel", decodeBattery(batterylevel));
/*     */     }
/*     */     
/* 241 */     int battery = parser.nextInt(0);
/* 242 */     if (battery != 65535) {
/* 243 */       position.set("battery", Double.valueOf(battery * 0.01D));
/*     */     }
/*     */     
/* 246 */     int power = parser.nextInt(0);
/* 247 */     if (power != 65535) {
/* 248 */       position.set("power", Double.valueOf(power * 0.1D));
/*     */     }
/*     */     
/* 251 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeNetwork(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 255 */     Parser parser = new Parser(PATTERN_NETWORK, sentence);
/* 256 */     if (!parser.matches()) {
/* 257 */       return null;
/*     */     }
/*     */     
/* 260 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 261 */     if (deviceSession == null) {
/* 262 */       return null;
/*     */     }
/*     */     
/* 265 */     Position position = new Position(getProtocolName());
/* 266 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 268 */     getLastLocation(position, null);
/*     */     
/* 270 */     position.setNetwork(new Network(CellTower.from(parser
/* 271 */             .nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0))));
/*     */     
/* 273 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeLbsWifi(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 277 */     Parser parser = new Parser(PATTERN_LBSWIFI, sentence);
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
/* 290 */     decodeType(position, parser.next(), "0");
/*     */     
/* 292 */     getLastLocation(position, null);
/*     */     
/* 294 */     Network network = new Network();
/*     */     
/* 296 */     network.addCellTower(CellTower.from(parser
/* 297 */           .nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue()));
/*     */     
/* 299 */     int wifiCount = parser.nextInt().intValue();
/* 300 */     if (parser.hasNext()) {
/* 301 */       String[] wifimacs = parser.next().split(",");
/* 302 */       if (wifimacs.length == wifiCount) {
/* 303 */         for (int i = 0; i < wifiCount; i++) {
/* 304 */           String[] wifiinfo = wifimacs[i].split("\\*");
/* 305 */           network.addWifiAccessPoint(WifiAccessPoint.from(wifiinfo[0], 
/* 306 */                 Integer.parseInt(wifiinfo[1]), Integer.parseInt(wifiinfo[2])));
/*     */         } 
/*     */       }
/*     */     } 
/*     */     
/* 311 */     if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 312 */       position.setNetwork(network);
/*     */     }
/*     */     
/* 315 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 317 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeCommandResult(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 321 */     Parser parser = new Parser(PATTERN_COMMAND_RESULT, sentence);
/* 322 */     if (!parser.matches()) {
/* 323 */       return null;
/*     */     }
/*     */     
/* 326 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 327 */     if (deviceSession == null) {
/* 328 */       return null;
/*     */     }
/*     */     
/* 331 */     Position position = new Position(getProtocolName());
/* 332 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 334 */     getLastLocation(position, parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 336 */     position.set("result", parser.next());
/*     */     
/* 338 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 346 */     String sentence = (String)msg;
/*     */     
/* 348 */     if (channel != null) {
/* 349 */       String id = sentence.substring(1, 13);
/* 350 */       String type = sentence.substring(13, 17);
/* 351 */       if (type.equals("BP00")) {
/* 352 */         channel.writeAndFlush(new NetworkMessage("(" + id + "AP01HSO)", remoteAddress));
/* 353 */         return null;
/* 354 */       }  if (type.equals("BP05")) {
/* 355 */         channel.writeAndFlush(new NetworkMessage("(" + id + "AP05)", remoteAddress));
/*     */       }
/*     */     } 
/*     */     
/* 359 */     if (sentence.contains("ZC20"))
/* 360 */       return decodeBattery(channel, remoteAddress, sentence); 
/* 361 */     if (sentence.contains("BZ00"))
/* 362 */       return decodeNetwork(channel, remoteAddress, sentence); 
/* 363 */     if (sentence.contains("ZC03"))
/* 364 */       return decodeCommandResult(channel, remoteAddress, sentence); 
/* 365 */     if (sentence.contains("DW5")) {
/* 366 */       return decodeLbsWifi(channel, remoteAddress, sentence);
/*     */     }
/*     */     
/* 369 */     Parser parser = new Parser(PATTERN, sentence);
/* 370 */     if (!parser.matches()) {
/* 371 */       return null;
/*     */     }
/*     */     
/* 374 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 375 */     if (deviceSession == null) {
/* 376 */       return null;
/*     */     }
/*     */     
/* 379 */     Position position = new Position(getProtocolName());
/* 380 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 382 */     boolean alternative = (parser.next() != null);
/*     */     
/* 384 */     decodeType(position, parser.next(), parser.next());
/*     */     
/* 386 */     DateBuilder dateBuilder = new DateBuilder();
/* 387 */     if (alternative) {
/* 388 */       dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     } else {
/* 390 */       dateBuilder.setDate(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     } 
/*     */     
/* 393 */     position.setValid(parser.next().equals("A"));
/* 394 */     position.setLatitude(parser.nextCoordinate());
/* 395 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 397 */     position.setSpeed(convertSpeed(parser.nextDouble(0.0D), "kmh"));
/*     */     
/* 399 */     dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 400 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 402 */     if (parser.hasNext()) {
/* 403 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */     }
/* 405 */     if (parser.hasNext()) {
/* 406 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */     }
/*     */     
/* 409 */     if (parser.hasNext(7)) {
/* 410 */       position.set("charge", Boolean.valueOf((parser.nextInt().intValue() == 0)));
/* 411 */       position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*     */       
/* 413 */       int mask1 = parser.nextHexInt().intValue();
/* 414 */       position.set("in2", Integer.valueOf(BitUtil.check(mask1, 0) ? 1 : 0));
/* 415 */       position.set("panic", Integer.valueOf(BitUtil.check(mask1, 1) ? 1 : 0));
/* 416 */       position.set("out2", Integer.valueOf(BitUtil.check(mask1, 2) ? 1 : 0));
/* 417 */       if (this.decodeLow || BitUtil.check(mask1, 3)) {
/* 418 */         position.set("blocked", Integer.valueOf(BitUtil.check(mask1, 3) ? 1 : 0));
/*     */       }
/*     */       
/* 421 */       int mask2 = parser.nextHexInt().intValue();
/* 422 */       for (int i = 0; i < 3; i++) {
/* 423 */         if (this.decodeLow || BitUtil.check(mask2, i)) {
/* 424 */           position.set("hs" + (3 - i), Integer.valueOf(BitUtil.check(mask2, i) ? 1 : 0));
/*     */         }
/*     */       } 
/* 427 */       if (this.decodeLow || BitUtil.check(mask2, 3)) {
/* 428 */         position.set("door", Integer.valueOf(BitUtil.check(mask2, 3) ? 1 : 0));
/*     */       }
/*     */       
/* 431 */       int mask3 = parser.nextHexInt().intValue();
/* 432 */       for (int j = 1; j <= 3; j++) {
/* 433 */         if (this.decodeLow || BitUtil.check(mask3, j)) {
/* 434 */           position.set("ls" + (3 - j + 1), Integer.valueOf(BitUtil.check(mask3, j) ? 1 : 0));
/*     */         }
/*     */       } 
/*     */       
/* 438 */       position.set("fuel", parser.nextHexInt());
/* 439 */       position.set("odometer", Long.valueOf(parser.nextLong(16, 0L)));
/*     */     } 
/*     */     
/* 442 */     if (parser.hasNext()) {
/* 443 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */     }
/*     */     
/* 446 */     if (parser.hasNext()) {
/* 447 */       position.set("temp1", Double.valueOf(parser.nextDouble(0.0D)));
/*     */     }
/*     */     
/* 450 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tk103ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */