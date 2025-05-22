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
/*     */ public class AquilaProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public AquilaProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*  40 */   private static final Pattern PATTERN_A = (new PatternBuilder())
/*  41 */     .text("$$")
/*  42 */     .expression("[^,]*,")
/*  43 */     .number("(d+),")
/*  44 */     .number("(d+),")
/*  45 */     .number("(-?d+.d+),")
/*  46 */     .number("(-?d+.d+),")
/*  47 */     .number("(dd)(dd)(dd)")
/*  48 */     .number("(dd)(dd)(dd),")
/*  49 */     .expression("([AV]),")
/*  50 */     .groupBegin()
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+),")
/*  54 */     .groupBegin()
/*  55 */     .number("d+,")
/*  56 */     .number("(d+),")
/*  57 */     .number("([01]),")
/*  58 */     .number("[01],")
/*  59 */     .number("[01],")
/*  60 */     .number("[01],")
/*  61 */     .number("(?:d+,){3}")
/*  62 */     .number("([01]),")
/*  63 */     .number("([01]),")
/*  64 */     .number("d+,")
/*  65 */     .number("([01]),")
/*  66 */     .number("[01],")
/*  67 */     .number("(?:d+,){7}")
/*  68 */     .number("[01],")
/*  69 */     .number("(?:d+,){8}")
/*  70 */     .number("([01]),")
/*  71 */     .number("([01]),")
/*  72 */     .number("([01]),")
/*  73 */     .number("([01]),")
/*  74 */     .or()
/*  75 */     .number("(d+),")
/*  76 */     .number("(?:d+,){3}")
/*  77 */     .number("[01],")
/*  78 */     .number("[01],")
/*  79 */     .number("(?:d+,){3}")
/*  80 */     .number("([01]),")
/*  81 */     .number("(?:d+,){2}")
/*  82 */     .number("[01],")
/*  83 */     .number("([01]),")
/*  84 */     .number("[01],")
/*  85 */     .number("(?:d+,){5}")
/*  86 */     .number("[01],")
/*  87 */     .number("[01],")
/*  88 */     .number("(?:d+,){6}")
/*  89 */     .number("[01],")
/*  90 */     .number("[01],")
/*  91 */     .number("[01],[01],[01],[01],")
/*  92 */     .number("(d+),")
/*  93 */     .number("(d+),")
/*  94 */     .number("(?:d+,){6}")
/*  95 */     .expression("P([^,]+),")
/*  96 */     .expression("D([^,]+),")
/*  97 */     .number("-?d+,")
/*  98 */     .number("-?d+,")
/*  99 */     .number("-?d+,")
/* 100 */     .number("d+,")
/* 101 */     .or()
/* 102 */     .number("(d+),")
/* 103 */     .number("(d+),")
/* 104 */     .number("(d+.d+),")
/* 105 */     .number("(?:d+,){2}")
/* 106 */     .number("(d+),")
/* 107 */     .number("([01]),")
/* 108 */     .number("[01],")
/* 109 */     .number("[01],")
/* 110 */     .number("[01],")
/* 111 */     .number("(?:[01],){2}")
/* 112 */     .number("[01],")
/* 113 */     .number("([01]),")
/* 114 */     .number("([01]),")
/* 115 */     .number("(?:[01],){2}")
/* 116 */     .number("([01]),")
/* 117 */     .number("(?:[01],){6}")
/* 118 */     .number("[01],")
/* 119 */     .number("[01],")
/* 120 */     .number("(?:[01],){4}")
/* 121 */     .number("[01],")
/* 122 */     .number("[01],")
/* 123 */     .number("[01],")
/* 124 */     .number("[01],")
/* 125 */     .number("(?:[01],){4}")
/* 126 */     .number("(d+),")
/* 127 */     .number("(d+),")
/* 128 */     .groupEnd()
/* 129 */     .or()
/* 130 */     .number("(d+),")
/* 131 */     .expression("([^,]+),")
/* 132 */     .groupEnd()
/* 133 */     .text("*")
/* 134 */     .number("xx")
/* 135 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeA(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 139 */     Parser parser = new Parser(PATTERN_A, sentence);
/* 140 */     if (!parser.matches()) {
/* 141 */       return null;
/*     */     }
/*     */     
/* 144 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 145 */     if (deviceSession == null) {
/* 146 */       return null;
/*     */     }
/*     */     
/* 149 */     Position position = new Position(getProtocolName());
/* 150 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 152 */     position.set("event", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 154 */     position.setLatitude(parser.nextDouble(0.0D));
/* 155 */     position.setLongitude(parser.nextDouble(0.0D));
/*     */     
/* 157 */     position.setTime(parser.nextDateTime());
/*     */     
/* 159 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 161 */     if (parser.hasNext(3)) {
/* 162 */       position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/* 163 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 164 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/*     */     } 
/*     */     
/* 167 */     if (parser.hasNext(9)) {
/*     */       
/* 169 */       position.set("fuel", parser.nextInt());
/* 170 */       position.set("in1", parser.next());
/* 171 */       position.set("charge", Boolean.valueOf(parser.next().equals("1")));
/* 172 */       position.set("in2", parser.next());
/*     */       
/* 174 */       position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/*     */ 
/*     */       
/* 177 */       int course = (parser.nextInt(0) << 3) + (parser.nextInt(0) << 2) + (parser.nextInt(0) << 1) + parser.nextInt(0);
/* 178 */       if (course > 0 && course <= 8) {
/* 179 */         position.setCourse(((course - 1) * 45));
/*     */       }
/*     */     }
/* 182 */     else if (parser.hasNext(7)) {
/*     */       
/* 184 */       position.setCourse(parser.nextInt(0));
/*     */       
/* 186 */       position.set("charge", Boolean.valueOf(parser.next().equals("1")));
/* 187 */       position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/* 188 */       position.set("power", Integer.valueOf(parser.nextInt(0)));
/* 189 */       position.set("battery", Integer.valueOf(parser.nextInt(0)));
/*     */       
/* 191 */       String obd = parser.next();
/* 192 */       position.set("obd", obd.substring(1, obd.length() - 1));
/*     */       
/* 194 */       String dtcs = parser.next();
/* 195 */       position.set("dtcs", dtcs.substring(1, dtcs.length() - 1).replace('|', ' '));
/*     */     }
/* 197 */     else if (parser.hasNext(10)) {
/*     */       
/* 199 */       position.setCourse(parser.nextInt(0));
/*     */       
/* 201 */       position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 202 */       position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/* 203 */       position.set("adc1", Integer.valueOf(parser.nextInt(0)));
/* 204 */       position.set("in1", Integer.valueOf(parser.nextInt(0)));
/* 205 */       position.set("charge", Boolean.valueOf(parser.next().equals("1")));
/* 206 */       position.set("in2", Integer.valueOf(parser.nextInt(0)));
/* 207 */       position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/* 208 */       position.set("power", Integer.valueOf(parser.nextInt(0)));
/* 209 */       position.set("battery", Integer.valueOf(parser.nextInt(0)));
/*     */     }
/* 211 */     else if (parser.hasNext(2)) {
/*     */       
/* 213 */       position.set("sensorId", parser.nextInt());
/* 214 */       position.set("sensorData", parser.next());
/*     */     } 
/*     */ 
/*     */     
/* 218 */     return position;
/*     */   }
/*     */   
/* 221 */   private static final Pattern PATTERN_B_1 = (new PatternBuilder())
/* 222 */     .text("$")
/* 223 */     .expression("[^,]+,")
/* 224 */     .expression("[^,]+,")
/* 225 */     .expression("[^,]+,")
/* 226 */     .expression(".{2},")
/* 227 */     .number("d+,")
/* 228 */     .expression("[LH],")
/* 229 */     .number("(d+),")
/* 230 */     .expression("[^,]+,")
/* 231 */     .number("([01]),")
/* 232 */     .number("(dd)(dd)(dddd),")
/* 233 */     .number("(dd)(dd)(dd),")
/* 234 */     .number("(-?d+.d+),")
/* 235 */     .expression("([NS]),")
/* 236 */     .number("(-?d+.d+),")
/* 237 */     .expression("([EW]),")
/* 238 */     .number("(d+.d+),")
/* 239 */     .number("(d+),")
/* 240 */     .number("(d+),")
/* 241 */     .number("(-?d+.d+),")
/* 242 */     .number("(d+.d+),")
/* 243 */     .number("(d+.d+),")
/* 244 */     .expression("[^,]+,")
/* 245 */     .number("([01]),")
/* 246 */     .number("([01]),")
/* 247 */     .number("(d+.d+),")
/* 248 */     .number("(d+.d+),")
/* 249 */     .number("([01]),")
/* 250 */     .expression("[CO],")
/* 251 */     .number("(d+),")
/* 252 */     .number("(d+),")
/* 253 */     .number("(d+),")
/* 254 */     .number("(x+),")
/* 255 */     .number("(x+),")
/* 256 */     .number("(d+),(x+),(x+),")
/* 257 */     .number("(d+),(x+),(x+),")
/* 258 */     .number("(d+),(x+),(x+),")
/* 259 */     .number("(d+),(x+),(x+),")
/* 260 */     .number("([01])+,")
/* 261 */     .number("([01])+,")
/* 262 */     .number("d+,")
/* 263 */     .number("(d+.d+),")
/* 264 */     .number("(d+.d+),")
/* 265 */     .number("d+,")
/* 266 */     .any()
/* 267 */     .compile();
/*     */   
/* 269 */   private static final Pattern PATTERN_B_2 = (new PatternBuilder())
/* 270 */     .text("$")
/* 271 */     .expression("[^,]+,")
/* 272 */     .expression("[^,]+,")
/* 273 */     .expression("(.{3}),")
/* 274 */     .number("(d+),")
/* 275 */     .expression(".{2},")
/* 276 */     .number("(dd)(dd)(dddd)")
/* 277 */     .number("(dd)(dd)(dd),")
/* 278 */     .expression("([AV]),")
/* 279 */     .number("(-?d+.d+),")
/* 280 */     .expression("([NS]),")
/* 281 */     .number("(-?d+.d+),")
/* 282 */     .expression("([EW]),")
/* 283 */     .number("(-?d+.d+),")
/* 284 */     .number("(d+.d+),")
/* 285 */     .any()
/* 286 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeB2(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 290 */     Parser parser = new Parser(PATTERN_B_2, sentence);
/* 291 */     if (!parser.matches()) {
/* 292 */       return null;
/*     */     }
/*     */     
/* 295 */     String type = parser.next();
/* 296 */     String id = parser.next();
/* 297 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 298 */     if (deviceSession == null) {
/* 299 */       return null;
/*     */     }
/*     */     
/* 302 */     Position position = new Position(getProtocolName());
/* 303 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 305 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 306 */     position.setValid(parser.next().equals("A"));
/* 307 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 308 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 309 */     position.setAltitude(parser.nextDouble().doubleValue());
/* 310 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */     
/* 312 */     if (type.equals("EMR") && channel != null) {
/* 313 */       String password = Context.getIdentityManager().lookupAttributeString(deviceSession
/* 314 */           .getDeviceId(), getProtocolName() + ".password", "aquila123", true);
/* 315 */       channel.writeAndFlush(new NetworkMessage("#set$" + id + "@" + password + "#EMR_MODE:0*", remoteAddress));
/*     */     } 
/*     */ 
/*     */     
/* 319 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeB1(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 324 */     Parser parser = new Parser(PATTERN_B_1, sentence);
/* 325 */     if (!parser.matches()) {
/* 326 */       return null;
/*     */     }
/*     */     
/* 329 */     String id = parser.next();
/* 330 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 331 */     if (deviceSession == null) {
/* 332 */       return null;
/*     */     }
/*     */     
/* 335 */     Position position = new Position(getProtocolName());
/* 336 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 338 */     position.setValid((parser.nextInt().intValue() == 1));
/* 339 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 340 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 341 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 342 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 343 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/* 345 */     position.set("sat", parser.nextInt());
/*     */     
/* 347 */     position.setAltitude(parser.nextDouble().doubleValue());
/*     */     
/* 349 */     position.set("pdop", parser.nextDouble());
/* 350 */     position.set("hdop", parser.nextDouble());
/* 351 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/* 352 */     position.set("charge", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/* 353 */     position.set("power", parser.nextDouble());
/* 354 */     position.set("battery", parser.nextDouble());
/*     */     
/* 356 */     if (parser.nextInt().intValue() == 1) {
/* 357 */       position.set("alarm", "sos");
/*     */     }
/*     */     
/* 360 */     Network network = new Network();
/*     */     
/* 362 */     int rssi = parser.nextInt().intValue();
/* 363 */     int mcc = parser.nextInt().intValue();
/* 364 */     int mnc = parser.nextInt().intValue();
/*     */     
/* 366 */     network.addCellTower(CellTower.from(mcc, mnc, parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), rssi));
/* 367 */     for (int i = 0; i < 4; i++) {
/* 368 */       rssi = parser.nextInt().intValue();
/* 369 */       network.addCellTower(CellTower.from(mcc, mnc, parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), rssi));
/*     */     } 
/*     */     
/* 372 */     position.setNetwork(network);
/*     */     
/* 374 */     position.set("input", parser.nextBinInt());
/* 375 */     position.set("output", parser.nextBinInt());
/* 376 */     position.set("adc1", parser.nextDouble());
/* 377 */     position.set("adc2", parser.nextDouble());
/*     */     
/* 379 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeB(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 383 */     if (sentence.contains("EMR") || sentence.contains("SEM")) {
/* 384 */       return decodeB2(channel, remoteAddress, sentence);
/*     */     }
/* 386 */     return decodeB1(channel, remoteAddress, sentence);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 394 */     String sentence = (String)msg;
/*     */     
/* 396 */     if (sentence.startsWith("$$")) {
/* 397 */       return decodeA(channel, remoteAddress, sentence);
/*     */     }
/* 399 */     return decodeB(channel, remoteAddress, sentence);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AquilaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */