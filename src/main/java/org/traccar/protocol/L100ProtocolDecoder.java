/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.ObdDecoder;
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
/*     */ public class L100ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public L100ProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */   
/*  41 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  42 */     .text("ATL")
/*  43 */     .expression(",[^,]+,").optional()
/*  44 */     .number("(d{15}),")
/*  45 */     .text("$GPRMC,")
/*  46 */     .number("(dd)(dd)(dd)")
/*  47 */     .number(".(ddd)").optional()
/*  48 */     .expression(",([AV]),")
/*  49 */     .number("(d+)(dd.d+),")
/*  50 */     .expression("([NS]),")
/*  51 */     .number("(d+)(dd.d+),")
/*  52 */     .expression("([EW]),")
/*  53 */     .number("(d+.?d*)?,")
/*  54 */     .number("(d+.?d*)?,")
/*  55 */     .number("(dd)(dd)(dd),")
/*  56 */     .any()
/*  57 */     .text("#")
/*  58 */     .number("([01]+),")
/*  59 */     .number("(d+.?d*|N.C),")
/*  60 */     .expression("[^,]*,")
/*  61 */     .expression("[^,]*,")
/*  62 */     .number("(d+.?d*),")
/*  63 */     .number("(d+.?d*),")
/*  64 */     .number("(d+.?d*),")
/*  65 */     .number("(d+),")
/*  66 */     .number("(d+),")
/*  67 */     .number("(d+),")
/*  68 */     .number("(x+),")
/*  69 */     .number("(x+)")
/*  70 */     .any()
/*  71 */     .text("ATL")
/*  72 */     .compile();
/*     */   
/*  74 */   private static final Pattern PATTERN_OBD_LOCATION = (new PatternBuilder())
/*  75 */     .expression("[LH],")
/*  76 */     .text("ATL,")
/*  77 */     .number("(d{15}),")
/*  78 */     .number("(d+),")
/*  79 */     .number("(d+),")
/*  80 */     .groupBegin()
/*  81 */     .number("(dd)(dd)(dd),")
/*  82 */     .number("(dd)(dd)(dd),")
/*  83 */     .expression("([AV]),")
/*  84 */     .number("(d+.d+);([NS]),")
/*  85 */     .number("(d+.d+);([EW]),")
/*  86 */     .number("(d+),")
/*  87 */     .number("(d+),")
/*  88 */     .number("(d+.d+),")
/*  89 */     .number("(d+.d+),")
/*  90 */     .number("(d+),")
/*  91 */     .number("(d+),")
/*  92 */     .number("(d+),")
/*  93 */     .number("(d+),")
/*  94 */     .number("(x+),")
/*  95 */     .number("#(d)(d)(d)(d),")
/*  96 */     .number("(d),")
/*  97 */     .text("ATL,")
/*  98 */     .groupEnd("?")
/*  99 */     .compile();
/*     */   
/* 101 */   private static final Pattern PATTERN_OBD_DATA = (new PatternBuilder())
/* 102 */     .expression("[LH],")
/* 103 */     .text("ATLOBD,")
/* 104 */     .number("(d{15}),")
/* 105 */     .number("d+,")
/* 106 */     .number("d+,")
/* 107 */     .number("(dd)(dd)(dd),")
/* 108 */     .number("(dd)(dd)(dd),")
/* 109 */     .expression("[^,]+,")
/* 110 */     .expression("(.+)")
/* 111 */     .compile();
/*     */   
/* 113 */   private static final Pattern PATTERN_NEW = (new PatternBuilder())
/* 114 */     .groupBegin()
/* 115 */     .text("ATL,")
/* 116 */     .expression("[LH],")
/* 117 */     .number("(d{15}),")
/* 118 */     .groupEnd("?")
/* 119 */     .expression("([NPT]),")
/* 120 */     .number("(dd)(dd)(dd),")
/* 121 */     .number("(dd)(dd)(dd),")
/* 122 */     .expression("([AV]),")
/* 123 */     .number("(d+.d+),([NS]),")
/* 124 */     .number("(d+.d+),([EW]),")
/* 125 */     .number("(d+.?d*),")
/* 126 */     .expression("(?:GPS|GSM|INV),")
/* 127 */     .number("(d+),")
/* 128 */     .number("(d+),")
/* 129 */     .number("(d+),")
/* 130 */     .number("(d+),")
/* 131 */     .number("(d+)")
/* 132 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 138 */     String sentence = (String)msg;
/*     */     
/* 140 */     if (sentence.startsWith("L") || sentence.startsWith("H")) {
/* 141 */       if (sentence.substring(2, 8).equals("ATLOBD")) {
/* 142 */         return decodeObdData(channel, remoteAddress, sentence);
/*     */       }
/* 144 */       return decodeObdLocation(channel, remoteAddress, sentence);
/*     */     } 
/* 146 */     if (!sentence.contains("$GPRMC")) {
/* 147 */       return decodeNew(channel, remoteAddress, sentence);
/*     */     }
/* 149 */     return decodeNormal(channel, remoteAddress, sentence);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeNormal(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 155 */     Parser parser = new Parser(PATTERN, sentence);
/* 156 */     if (!parser.matches()) {
/* 157 */       return null;
/*     */     }
/*     */     
/* 160 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 161 */     if (deviceSession == null) {
/* 162 */       return null;
/*     */     }
/*     */     
/* 165 */     Position position = new Position(getProtocolName());
/* 166 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 169 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt(0));
/*     */     
/* 171 */     position.setValid(parser.next().equals("A"));
/* 172 */     position.setLatitude(parser.nextCoordinate());
/* 173 */     position.setLongitude(parser.nextCoordinate());
/* 174 */     position.setSpeed(parser.nextDouble(0.0D));
/* 175 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 177 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 178 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 180 */     position.set("status", parser.next());
/* 181 */     position.set("adc1", parser.next());
/* 182 */     position.set("odometer", parser.nextDouble());
/* 183 */     position.set("temp1", parser.nextDouble());
/* 184 */     position.set("battery", parser.nextDouble());
/*     */     
/* 186 */     int rssi = parser.nextInt().intValue();
/* 187 */     if (rssi > 0) {
/* 188 */       position.setNetwork(new Network(CellTower.from(parser
/* 189 */               .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), rssi)));
/*     */     }
/*     */     
/* 192 */     if (channel != null) {
/* 193 */       channel.writeAndFlush(new NetworkMessage(String.valueOf('\001'), remoteAddress));
/*     */     }
/*     */     
/* 196 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeObdLocation(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 201 */     Parser parser = new Parser(PATTERN_OBD_LOCATION, sentence);
/* 202 */     if (!parser.matches()) {
/* 203 */       return null;
/*     */     }
/*     */     
/* 206 */     String imei = parser.next();
/* 207 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 208 */     if (deviceSession == null) {
/* 209 */       return null;
/*     */     }
/*     */     
/* 212 */     int type = parser.nextInt().intValue();
/* 213 */     int index = parser.nextInt().intValue();
/*     */     
/* 215 */     if (type == 1) {
/* 216 */       if (channel != null) {
/* 217 */         String response = "@" + imei + ",00," + index + ",";
/* 218 */         response = response + "*" + (char)Checksum.xor(response);
/* 219 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       } 
/* 221 */       return null;
/*     */     } 
/*     */     
/* 224 */     Position position = new Position(getProtocolName());
/* 225 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 227 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/* 228 */     position.setValid(parser.next().equals("A"));
/* 229 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 230 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 231 */     position.setSpeed(parser.nextInt().intValue());
/* 232 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/* 234 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/* 235 */     position.set("battery", parser.nextDouble());
/*     */     
/* 237 */     int rssi = parser.nextInt().intValue();
/* 238 */     position.setNetwork(new Network(CellTower.from(parser
/* 239 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), rssi)));
/*     */     
/* 241 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/* 242 */     parser.next();
/*     */     
/* 244 */     switch (parser.nextInt().intValue()) {
/*     */       case 0:
/* 246 */         position.set("alarm", "hardBraking");
/*     */         break;
/*     */       case 2:
/* 249 */         position.set("alarm", "hardAcceleration");
/*     */         break;
/*     */       case 1:
/* 252 */         position.set("alarm", "general");
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 258 */     position.set("charge", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*     */     
/* 260 */     if (parser.nextInt().intValue() == 1) {
/* 261 */       position.set("alarm", "overspeed");
/*     */     }
/*     */     
/* 264 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeObdData(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 269 */     Parser parser = new Parser(PATTERN_OBD_DATA, sentence);
/* 270 */     if (!parser.matches()) {
/* 271 */       return null;
/*     */     }
/*     */     
/* 274 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 275 */     if (deviceSession == null) {
/* 276 */       return null;
/*     */     }
/*     */     
/* 279 */     Position position = new Position(getProtocolName());
/* 280 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 282 */     getLastLocation(position, parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 284 */     for (String entry : parser.next().split(",")) {
/* 285 */       String[] values = entry.split(":");
/* 286 */       if (values.length == 2 && values[1].charAt(0) != 'X') {
/* 287 */         position.add(ObdDecoder.decodeData(
/* 288 */               Integer.parseInt(values[0].substring(2), 16), Integer.parseInt(values[1], 16), true));
/*     */       }
/*     */     } 
/*     */     
/* 292 */     return position;
/*     */   }
/*     */   
/*     */   private Object decodeNew(Channel channel, SocketAddress remoteAddress, String sentence) {
/*     */     DeviceSession deviceSession;
/* 297 */     Parser parser = new Parser(PATTERN_NEW, sentence);
/* 298 */     if (!parser.matches()) {
/* 299 */       return null;
/*     */     }
/*     */     
/* 302 */     String imei = parser.next();
/*     */     
/* 304 */     if (imei != null) {
/* 305 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */     } else {
/* 307 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */     } 
/* 309 */     if (deviceSession == null) {
/* 310 */       return null;
/*     */     }
/*     */     
/* 313 */     Position position = new Position(getProtocolName());
/* 314 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 316 */     switch (parser.next()) {
/*     */       case "P":
/* 318 */         position.set("alarm", "sos");
/*     */         break;
/*     */       case "T":
/* 321 */         position.set("alarm", "tampering");
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 327 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 328 */     position.setValid(parser.next().equals("A"));
/* 329 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 330 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 331 */     position.setSpeed(parser.nextDouble().doubleValue());
/*     */     
/* 333 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/*     */     
/* 335 */     position.setNetwork(new Network(CellTower.from(parser
/* 336 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue())));
/*     */     
/* 338 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\L100ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */