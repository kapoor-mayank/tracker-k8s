/*      */ package org.traccar.protocol;
/*      */ 
/*      */ import io.netty.buffer.ByteBuf;
/*      */ import io.netty.buffer.Unpooled;
/*      */ import io.netty.channel.Channel;
/*      */ import java.net.SocketAddress;
/*      */ import java.nio.charset.StandardCharsets;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.LinkedList;
/*      */ import java.util.TimeZone;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ import org.traccar.BaseProtocolDecoder;
/*      */ import org.traccar.Context;
/*      */ import org.traccar.DeviceSession;
/*      */ import org.traccar.NetworkMessage;
/*      */ import org.traccar.Protocol;
/*      */ import org.traccar.helper.BitUtil;
/*      */ import org.traccar.helper.DataConverter;
/*      */ import org.traccar.helper.Parser;
/*      */ import org.traccar.helper.PatternBuilder;
/*      */ import org.traccar.helper.UnitsConverter;
/*      */ import org.traccar.model.CellTower;
/*      */ import org.traccar.model.Network;
/*      */ import org.traccar.model.Position;
/*      */ import org.traccar.model.WifiAccessPoint;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class Gl200TextProtocolDecoder
/*      */   extends BaseProtocolDecoder
/*      */ {
/*      */   private final boolean ignoreFixTime;
/*      */   
/*      */   public Gl200TextProtocolDecoder(Protocol protocol) {
/*   54 */     super(protocol);
/*      */     
/*   56 */     this.ignoreFixTime = Context.getConfig().getBoolean(getProtocolName() + ".ignoreFixTime");
/*      */   }
/*      */   
/*   59 */   private static final Pattern PATTERN_ACK = (new PatternBuilder())
/*   60 */     .text("+ACK:GT")
/*   61 */     .expression("...,")
/*   62 */     .expression("(.{6}|.{10}),")
/*   63 */     .number("(d{15}|x{14}),")
/*   64 */     .any().text(",")
/*   65 */     .number("(dddd)(dd)(dd)")
/*   66 */     .number("(dd)(dd)(dd),")
/*   67 */     .number("(xxxx)")
/*   68 */     .text("$").optional()
/*   69 */     .compile();
/*      */   
/*      */   private Object decodeAck(Channel channel, SocketAddress remoteAddress, String sentence, String type) {
/*   72 */     Parser parser = new Parser(PATTERN_ACK, sentence);
/*   73 */     if (parser.matches()) {
/*   74 */       String protocolVersion = parser.next();
/*   75 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*   76 */       if (deviceSession == null) {
/*   77 */         return null;
/*      */       }
/*   79 */       if (type.equals("HBD")) {
/*   80 */         if (channel != null) {
/*   81 */           parser.skip(6);
/*   82 */           channel.writeAndFlush(new NetworkMessage("+SACK:GTHBD," + protocolVersion + "," + parser
/*   83 */                 .next() + "$", remoteAddress));
/*      */         } 
/*      */       } else {
/*   86 */         Position position = new Position(getProtocolName());
/*   87 */         position.setDeviceId(deviceSession.getDeviceId());
/*   88 */         getLastLocation(position, parser.nextDateTime());
/*   89 */         position.setValid(false);
/*   90 */         position.set("result", "Command " + type + " accepted");
/*   91 */         return position;
/*      */       } 
/*      */     } 
/*   94 */     return null;
/*      */   }
/*      */   
/*      */   private Position initPosition(Parser parser, Channel channel, SocketAddress remoteAddress) {
/*   98 */     if (parser.matches()) {
/*   99 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  100 */       if (deviceSession != null) {
/*  101 */         Position position = new Position(getProtocolName());
/*  102 */         position.setDeviceId(deviceSession.getDeviceId());
/*  103 */         return position;
/*      */       } 
/*      */     } 
/*  106 */     return null;
/*      */   }
/*      */   
/*      */   private void decodeDeviceTime(Position position, Parser parser) {
/*  110 */     if (parser.hasNext(6)) {
/*  111 */       if (this.ignoreFixTime) {
/*  112 */         position.setTime(parser.nextDateTime());
/*      */       } else {
/*  114 */         position.setDeviceTime(parser.nextDateTime());
/*      */       } 
/*      */     }
/*      */   }
/*      */   
/*      */   private Long parseHours(String hoursString) {
/*  120 */     if (hoursString != null) {
/*  121 */       String[] hours = hoursString.split(":");
/*  122 */       return Long.valueOf((Integer.parseInt(hours[0]) * 3600L + ((hours.length > 1) ? (
/*  123 */           Integer.parseInt(hours[1]) * 60L) : 0L) + ((hours.length > 2) ? 
/*  124 */           Integer.parseInt(hours[2]) : 0L)) * 1000L);
/*      */     } 
/*  126 */     return null;
/*      */   }
/*      */   
/*  129 */   private static final Pattern PATTERN_PDP = (new PatternBuilder())
/*  130 */     .text("+RESP:GTPDP,")
/*  131 */     .number("([0-9A-Z]{2}xxxx),")
/*  132 */     .number("(d{15}|x{14}),")
/*  133 */     .any().text(",")
/*  134 */     .number("(dddd)(dd)(dd)")
/*  135 */     .number("(dd)(dd)(dd),")
/*  136 */     .number("(xxxx)")
/*  137 */     .text("$").optional()
/*  138 */     .compile();
/*      */   
/*      */   private Object decodePdp(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  141 */     Parser parser = new Parser(PATTERN_PDP, sentence);
/*  142 */     Position position = initPosition(parser, channel, remoteAddress);
/*  143 */     if (position == null) {
/*  144 */       return null;
/*      */     }
/*      */     
/*  147 */     getLastLocation(position, null);
/*      */     
/*  149 */     position.set("type", "PDP");
/*      */     
/*  151 */     return position;
/*      */   }
/*      */   
/*  154 */   private static final Pattern PATTERN_INF = (new PatternBuilder())
/*  155 */     .text("+").expression("(?:RESP|BUFF):GTINF,")
/*  156 */     .expression("(?:.{6}|.{10})?,")
/*  157 */     .number("(d{15}|x{14}),")
/*  158 */     .expression("(?:[0-9A-Z]{17},)?")
/*  159 */     .expression("(?:[^,]+)?,")
/*  160 */     .number("(xx),")
/*  161 */     .expression("(?:[0-9Ff]{20})?,")
/*  162 */     .number("(d{1,2}),")
/*  163 */     .number("d{1,2},")
/*  164 */     .expression("[01]{1,2},")
/*  165 */     .number("([d.]+)?,")
/*  166 */     .number("d*,")
/*  167 */     .number("(d+.d+),")
/*  168 */     .expression("([01]),")
/*  169 */     .number("(?:d),")
/*  170 */     .number("(?:d)?,")
/*  171 */     .number("(?:d)?,")
/*  172 */     .number("(?:d)?,").optional()
/*  173 */     .number("d{14},")
/*  174 */     .groupBegin()
/*  175 */     .number("(d+),")
/*  176 */     .number("[d.]*,")
/*  177 */     .number("(-?[d.]+)?,,,")
/*  178 */     .or()
/*  179 */     .expression("(?:[01])?,").optional()
/*  180 */     .number("(d+)?,")
/*  181 */     .number("(d+)?,").optional()
/*  182 */     .number("(xx)?,")
/*  183 */     .number("(xx)?,")
/*  184 */     .number("[-+]dddd,")
/*  185 */     .expression("[01],")
/*  186 */     .or()
/*  187 */     .any()
/*  188 */     .groupEnd()
/*  189 */     .number("(dddd)(dd)(dd)")
/*  190 */     .number("(dd)(dd)(dd),")
/*  191 */     .number("(xxxx)")
/*  192 */     .text("$").optional()
/*  193 */     .compile();
/*      */   
/*      */   private Object decodeInf(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  196 */     Parser parser = new Parser(PATTERN_INF, sentence);
/*  197 */     Position position = initPosition(parser, channel, remoteAddress);
/*  198 */     if (position == null) {
/*  199 */       return null;
/*      */     }
/*      */     
/*  202 */     switch (parser.nextHexInt().intValue()) {
/*      */       case 18:
/*      */       case 22:
/*      */       case 26:
/*  206 */         position.set("ignition", Boolean.valueOf(false));
/*  207 */         position.set("motion", Boolean.valueOf(true));
/*      */         break;
/*      */       case 17:
/*  210 */         position.set("ignition", Boolean.valueOf(false));
/*  211 */         position.set("motion", Boolean.valueOf(false));
/*      */         break;
/*      */       case 33:
/*  214 */         position.set("ignition", Boolean.valueOf(true));
/*  215 */         position.set("motion", Boolean.valueOf(false));
/*      */         break;
/*      */       case 34:
/*  218 */         position.set("ignition", Boolean.valueOf(true));
/*  219 */         position.set("motion", Boolean.valueOf(true));
/*      */         break;
/*      */       case 65:
/*  222 */         position.set("motion", Boolean.valueOf(false));
/*      */         break;
/*      */       case 66:
/*  225 */         position.set("motion", Boolean.valueOf(true));
/*      */         break;
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  231 */     position.set("rssi", parser.nextInt());
/*      */     
/*  233 */     parser.next();
/*      */     
/*  235 */     position.set("battery", parser.nextDouble());
/*  236 */     position.set("charge", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*      */     
/*  238 */     position.set("batteryLevel", parser.nextInt());
/*      */     
/*  240 */     position.set("temp1", parser.next());
/*      */     
/*  242 */     position.set("adc1", parser.next());
/*  243 */     position.set("adc2", parser.next());
/*      */     
/*  245 */     position.set("input", parser.next());
/*  246 */     position.set("output", parser.next());
/*      */     
/*  248 */     getLastLocation(position, parser.nextDateTime());
/*      */     
/*  250 */     position.set("index", parser.nextHexInt());
/*      */     
/*  252 */     return position;
/*      */   }
/*      */   
/*  255 */   private static final Pattern PATTERN_VER = (new PatternBuilder())
/*  256 */     .text("+").expression("(?:RESP|BUFF):GTVER,")
/*  257 */     .expression("(?:.{6}|.{10})?,")
/*  258 */     .number("(d{15}|x{14}),")
/*  259 */     .expression("[^,]*,")
/*  260 */     .expression("([^,]*),")
/*  261 */     .number("(xxxx),")
/*  262 */     .number("(xxxx),")
/*  263 */     .number("(dddd)(dd)(dd)")
/*  264 */     .number("(dd)(dd)(dd),")
/*  265 */     .number("(xxxx)")
/*  266 */     .text("$").optional()
/*  267 */     .compile();
/*      */   
/*      */   private Object decodeVer(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  270 */     Parser parser = new Parser(PATTERN_VER, sentence);
/*  271 */     Position position = initPosition(parser, channel, remoteAddress);
/*  272 */     if (position == null) {
/*  273 */       return null;
/*      */     }
/*      */     
/*  276 */     position.set("deviceType", parser.next());
/*  277 */     position.set("versionFw", parser.nextHexInt());
/*  278 */     position.set("versionHw", parser.nextHexInt());
/*      */     
/*  280 */     getLastLocation(position, parser.nextDateTime());
/*      */     
/*  282 */     return position;
/*      */   }
/*      */   
/*      */   private void skipLocation(Parser parser) {
/*  286 */     parser.skip(19);
/*      */   }
/*      */   
/*  289 */   private static final Pattern PATTERN_LOCATION = (new PatternBuilder())
/*  290 */     .number("(d{1,2}.?d?)?,")
/*  291 */     .number("(d{1,3}.d)?,")
/*  292 */     .number("(d{1,3}.?d?)?,")
/*  293 */     .number("(-?d{1,5}.d)?,")
/*  294 */     .number("(-?d{1,3}.d{6})?,")
/*  295 */     .number("(-?d{1,2}.d{6})?,")
/*  296 */     .number("(dddd)(dd)(dd)")
/*  297 */     .number("(dd)(dd)(dd)").optional(2)
/*  298 */     .text(",")
/*  299 */     .number("(d+)?,")
/*  300 */     .number("(d+)?,")
/*  301 */     .groupBegin()
/*  302 */     .number("(d+),")
/*  303 */     .number("(d+),")
/*  304 */     .or()
/*  305 */     .number("(x+)?,")
/*  306 */     .number("(x+)?,")
/*  307 */     .groupEnd()
/*  308 */     .number("(?:d+|(d+.d))?,")
/*  309 */     .compile();
/*      */   
/*      */   private void decodeLocation(Position position, Parser parser) {
/*  312 */     Double hdop = parser.nextDouble();
/*  313 */     position.setValid((hdop == null || hdop.doubleValue() > 0.0D));
/*  314 */     position.set("hdop", hdop);
/*      */     
/*  316 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*  317 */     position.setCourse(parser.nextDouble(0.0D));
/*  318 */     position.setAltitude(parser.nextDouble(0.0D));
/*      */     
/*  320 */     if (parser.hasNext(8)) {
/*  321 */       position.setValid(true);
/*  322 */       position.setLongitude(parser.nextDouble().doubleValue());
/*  323 */       position.setLatitude(parser.nextDouble().doubleValue());
/*  324 */       position.setTime(parser.nextDateTime());
/*      */     } else {
/*  326 */       getLastLocation(position, null);
/*      */     } 
/*      */     
/*  329 */     if (parser.hasNext(6)) {
/*  330 */       int mcc = parser.nextInt().intValue();
/*  331 */       int mnc = parser.nextInt().intValue();
/*  332 */       if (parser.hasNext(2)) {
/*  333 */         position.setNetwork(new Network(CellTower.from(mcc, mnc, parser.nextInt().intValue(), parser.nextInt().intValue())));
/*      */       }
/*  335 */       if (parser.hasNext(2)) {
/*  336 */         position.setNetwork(new Network(CellTower.from(mcc, mnc, parser.nextHexInt().intValue(), parser.nextHexInt().intValue())));
/*      */       }
/*      */     } 
/*      */     
/*  340 */     if (parser.hasNext()) {
/*  341 */       position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     }
/*      */   }
/*      */   
/*  345 */   private static final Pattern PATTERN_OBD = (new PatternBuilder())
/*  346 */     .text("+RESP:GTOBD,")
/*  347 */     .expression("(?:.{6}|.{10})?,")
/*  348 */     .number("(d{15}|x{14}),")
/*  349 */     .expression("(?:[0-9A-Z]{17})?,")
/*  350 */     .expression("[^,]{0,20},")
/*  351 */     .expression("[01],")
/*  352 */     .number("x{1,8},")
/*  353 */     .expression("(?:[0-9A-Z]{17})?,")
/*  354 */     .number("[01],")
/*  355 */     .number("(?:d{1,5})?,")
/*  356 */     .number("(?:x{8})?,")
/*  357 */     .number("(d{1,5})?,")
/*  358 */     .number("(d{1,3})?,")
/*  359 */     .number("(-?d{1,3})?,")
/*  360 */     .number("(d+.?d*|Inf|NaN)?,")
/*  361 */     .number("(d{1,5})?,")
/*  362 */     .number("(?:d{1,5})?,")
/*  363 */     .expression("([01])?,")
/*  364 */     .number("(d{1,3})?,")
/*  365 */     .number("(x*),")
/*  366 */     .number("(d{1,3})?,")
/*  367 */     .number("(?:d{1,3})?,")
/*  368 */     .number("(d{1,3})?,")
/*  369 */     .expression("(?:[0-9A],)?")
/*  370 */     .number("(d+),")
/*  371 */     .expression(PATTERN_LOCATION.pattern())
/*  372 */     .number("(d{1,7}.d)?,")
/*  373 */     .number("(dddd)(dd)(dd)")
/*  374 */     .number("(dd)(dd)(dd)").optional(2)
/*  375 */     .text(",")
/*  376 */     .number("(xxxx)")
/*  377 */     .text("$").optional()
/*  378 */     .compile();
/*      */   
/*      */   private Object decodeObd(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  381 */     Parser parser = new Parser(PATTERN_OBD, sentence);
/*  382 */     Position position = initPosition(parser, channel, remoteAddress);
/*  383 */     if (position == null) {
/*  384 */       return null;
/*      */     }
/*      */     
/*  387 */     position.set("rpm", parser.nextInt());
/*  388 */     position.set("obdSpeed", parser.nextInt());
/*  389 */     position.set("temp1", parser.nextInt());
/*  390 */     position.set("fuelConsumption", parser.next());
/*  391 */     position.set("dtcsClearedDistance", parser.nextInt());
/*  392 */     if (parser.hasNext()) {
/*  393 */       position.set("odbConnect", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*      */     }
/*  395 */     position.set("dtcsNumber", parser.nextInt());
/*  396 */     position.set("dtcsCodes", parser.next());
/*  397 */     position.set("throttle", parser.nextInt());
/*  398 */     position.set("fuel", parser.nextInt());
/*  399 */     if (parser.hasNext()) {
/*  400 */       position.set("obdOdometer", Integer.valueOf(parser.nextInt().intValue() * 1000));
/*      */     }
/*      */     
/*  403 */     decodeLocation(position, parser);
/*      */     
/*  405 */     if (parser.hasNext()) {
/*  406 */       position.set("obdOdometer", Integer.valueOf((int)(parser.nextDouble().doubleValue() * 1000.0D)));
/*      */     }
/*      */     
/*  409 */     decodeDeviceTime(position, parser);
/*      */     
/*  411 */     return position;
/*      */   }
/*      */   
/*      */   private Object decodeCan(Channel channel, SocketAddress remoteAddress, String sentence) throws ParseException {
/*  415 */     Position position = new Position(getProtocolName());
/*      */     
/*  417 */     int index = 0;
/*  418 */     String[] v = sentence.split(",");
/*      */     
/*  420 */     index++;
/*  421 */     index++;
/*      */     
/*  423 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { v[index++] });
/*  424 */     position.setDeviceId(deviceSession.getDeviceId());
/*      */     
/*  426 */     String name = v[index++];
/*  427 */     String model = (name != null && !name.isEmpty()) ? name : getDeviceModel(deviceSession);
/*  428 */     index++;
/*  429 */     index++;
/*  430 */     long reportMask = Long.parseLong(v[index++], 16);
/*      */     
/*  432 */     if (BitUtil.check(reportMask, 0)) {
/*  433 */       position.set("vin", v[index++]);
/*      */     }
/*  435 */     if (BitUtil.check(reportMask, 1) && !v[index++].isEmpty()) {
/*  436 */       position.set("ignition", Boolean.valueOf((Integer.parseInt(v[index - 1]) > 0)));
/*      */     }
/*  438 */     if (BitUtil.check(reportMask, 2) && !v[index++].isEmpty()) {
/*  439 */       position.set("obdOdometer", Integer.valueOf(Integer.parseInt(v[index - 1].substring(1))));
/*      */     }
/*  441 */     if (BitUtil.check(reportMask, 3) && !v[index++].isEmpty()) {
/*  442 */       position.set("fuelUsed", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  444 */     if (BitUtil.check(reportMask, 5) && !v[index++].isEmpty()) {
/*  445 */       position.set("rpm", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  447 */     if (BitUtil.check(reportMask, 4) && !v[index++].isEmpty()) {
/*  448 */       position.set("obdSpeed", Double.valueOf(UnitsConverter.knotsFromKph(Integer.parseInt(v[index - 1]))));
/*      */     }
/*  450 */     if (BitUtil.check(reportMask, 6) && !v[index++].isEmpty()) {
/*  451 */       position.set("coolantTemp", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  453 */     if (BitUtil.check(reportMask, 7) && !v[index++].isEmpty()) {
/*  454 */       String value = v[index - 1];
/*  455 */       if (value.startsWith("L/H")) {
/*  456 */         position.set("fuelConsumption", Double.valueOf(Double.parseDouble(value.substring(3))));
/*      */       }
/*      */     } 
/*  459 */     if (BitUtil.check(reportMask, 8) && !v[index++].isEmpty()) {
/*  460 */       position.set("fuel", Double.valueOf(Double.parseDouble(v[index - 1].substring(1))));
/*      */     }
/*  462 */     if (BitUtil.check(reportMask, 9) && !v[index++].isEmpty()) {
/*  463 */       position.set("range", Long.valueOf(Long.parseLong(v[index - 1]) * 100L));
/*      */     }
/*  465 */     if (BitUtil.check(reportMask, 10) && !v[index++].isEmpty()) {
/*  466 */       position.set("throttle", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  468 */     if (BitUtil.check(reportMask, 11) && !v[index++].isEmpty()) {
/*  469 */       position.set("hours", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  471 */     if (BitUtil.check(reportMask, 12) && !v[index++].isEmpty()) {
/*  472 */       position.set("drivingTime", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  474 */     if (BitUtil.check(reportMask, 13) && !v[index++].isEmpty()) {
/*  475 */       position.set("idleHours", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  477 */     if (BitUtil.check(reportMask, 14) && !v[index++].isEmpty()) {
/*  478 */       position.set("idleFuelConsumption", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  480 */     if (BitUtil.check(reportMask, 15) && !v[index++].isEmpty()) {
/*  481 */       position.set("axleWeight", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  483 */     if (BitUtil.check(reportMask, 16) && !v[index++].isEmpty()) {
/*  484 */       position.set("tachographInfo", Integer.valueOf(Integer.parseInt(v[index - 1], 16)));
/*      */     }
/*  486 */     if (BitUtil.check(reportMask, 17) && !v[index++].isEmpty()) {
/*  487 */       position.set("indicators", Integer.valueOf(Integer.parseInt(v[index - 1], 16)));
/*      */     }
/*  489 */     if (BitUtil.check(reportMask, 18) && !v[index++].isEmpty()) {
/*  490 */       position.set("lights", Integer.valueOf(Integer.parseInt(v[index - 1], 16)));
/*      */     }
/*  492 */     if (BitUtil.check(reportMask, 19) && !v[index++].isEmpty()) {
/*  493 */       position.set("doors", Integer.valueOf(Integer.parseInt(v[index - 1], 16)));
/*      */     }
/*  495 */     if (BitUtil.check(reportMask, 20) && !v[index++].isEmpty()) {
/*  496 */       position.set("vehicleOverspeed", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  498 */     if (BitUtil.check(reportMask, 21) && !v[index++].isEmpty()) {
/*  499 */       position.set("engineOverspeed", Double.valueOf(Double.parseDouble(v[index - 1])));
/*      */     }
/*  501 */     if ("GV350M".equals(model)) {
/*  502 */       if (BitUtil.check(reportMask, 22)) {
/*  503 */         index++;
/*      */       }
/*  505 */       if (BitUtil.check(reportMask, 23)) {
/*  506 */         index++;
/*      */       }
/*  508 */       if (BitUtil.check(reportMask, 24)) {
/*  509 */         index++;
/*      */       }
/*  511 */     } else if ("GV355CEU".equals(model)) {
/*  512 */       if (BitUtil.check(reportMask, 22)) {
/*  513 */         index++;
/*      */       }
/*  515 */       if (BitUtil.check(reportMask, 23)) {
/*  516 */         position.set("engineColdStarts", v[index++]);
/*      */       }
/*  518 */       if (BitUtil.check(reportMask, 24)) {
/*  519 */         position.set("engineAllStarts", v[index++]);
/*      */       }
/*  521 */       if (BitUtil.check(reportMask, 25)) {
/*  522 */         position.set("engineStartsByIgnition", v[index++]);
/*      */       }
/*  524 */       if (BitUtil.check(reportMask, 26)) {
/*  525 */         position.set("engineColdRunningTime", v[index++]);
/*      */       }
/*  527 */       if (BitUtil.check(reportMask, 27)) {
/*  528 */         position.set("handbrakeApplies", v[index++]);
/*      */       }
/*  530 */       if (BitUtil.check(reportMask, 28)) {
/*  531 */         index++;
/*      */       }
/*      */     } 
/*      */     
/*  535 */     long reportMaskExt = 0L;
/*  536 */     if (BitUtil.check(reportMask, 29) && !v[index++].isEmpty()) {
/*  537 */       reportMaskExt = Long.parseLong(v[index - 1], 16);
/*      */     }
/*  539 */     if (BitUtil.check(reportMaskExt, 0) && !v[index++].isEmpty()) {
/*  540 */       position.set("adBlueLevel", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  542 */     if (BitUtil.check(reportMaskExt, 1) && !v[index++].isEmpty()) {
/*  543 */       position.set("axleWeight1", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  545 */     if (BitUtil.check(reportMaskExt, 2) && !v[index++].isEmpty()) {
/*  546 */       position.set("axleWeight3", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  548 */     if (BitUtil.check(reportMaskExt, 3) && !v[index++].isEmpty()) {
/*  549 */       position.set("axleWeight4", Integer.valueOf(Integer.parseInt(v[index - 1])));
/*      */     }
/*  551 */     if (BitUtil.check(reportMaskExt, 4)) {
/*  552 */       index++;
/*      */     }
/*  554 */     if (BitUtil.check(reportMaskExt, 5)) {
/*  555 */       index++;
/*      */     }
/*  557 */     if (BitUtil.check(reportMaskExt, 6)) {
/*  558 */       index++;
/*      */     }
/*  560 */     if (BitUtil.check(reportMaskExt, 7) && !v[index++].isEmpty()) {
/*  561 */       position.set("adc1", Double.valueOf(Integer.parseInt(v[index - 1]) * 0.001D));
/*      */     }
/*  563 */     if (BitUtil.check(reportMaskExt, 8)) {
/*  564 */       position.set("pedalBreakingFactor", v[index++]);
/*      */     }
/*  566 */     if (BitUtil.check(reportMaskExt, 9)) {
/*  567 */       position.set("engineBreakingFactor", v[index++]);
/*      */     }
/*  569 */     if (BitUtil.check(reportMaskExt, 10)) {
/*  570 */       position.set("acceleratorKickDowns", v[index++]);
/*      */     }
/*  572 */     if (BitUtil.check(reportMaskExt, 11)) {
/*  573 */       position.set("effectiveEngineSpeed", v[index++]);
/*      */     }
/*  575 */     if (BitUtil.check(reportMaskExt, 12)) {
/*  576 */       position.set("cruiseControlTime", v[index++]);
/*      */     }
/*  578 */     if (BitUtil.check(reportMaskExt, 13)) {
/*  579 */       position.set("acceleratorKickDownTime", v[index++]);
/*      */     }
/*  581 */     if (BitUtil.check(reportMaskExt, 14)) {
/*  582 */       position.set("brakeApplication", v[index++]);
/*      */     }
/*  584 */     if (BitUtil.check(reportMaskExt, 15) && !v[index++].isEmpty()) {
/*  585 */       position.set("driver1Card", v[index - 1]);
/*      */     }
/*  587 */     if (BitUtil.check(reportMaskExt, 16) && !v[index++].isEmpty()) {
/*  588 */       position.set("driver2Card", v[index - 1]);
/*      */     }
/*  590 */     if (BitUtil.check(reportMaskExt, 17) && !v[index++].isEmpty()) {
/*  591 */       position.set("driver1Name", v[index - 1]);
/*      */     }
/*  593 */     if (BitUtil.check(reportMaskExt, 18) && !v[index++].isEmpty()) {
/*  594 */       position.set("driver2Name", v[index - 1]);
/*      */     }
/*  596 */     if (BitUtil.check(reportMaskExt, 19) && !v[index++].isEmpty()) {
/*  597 */       position.set("registration", v[index - 1]);
/*      */     }
/*  599 */     if (BitUtil.check(reportMaskExt, 20)) {
/*  600 */       position.set("expansionInformation", v[index++]);
/*      */     }
/*  602 */     if (BitUtil.check(reportMaskExt, 21)) {
/*  603 */       index++;
/*      */     }
/*  605 */     if (BitUtil.check(reportMaskExt, 22)) {
/*  606 */       index++;
/*      */     }
/*  608 */     if (BitUtil.check(reportMaskExt, 23)) {
/*  609 */       index++;
/*      */     }
/*  611 */     if (BitUtil.check(reportMaskExt, 24)) {
/*  612 */       index++;
/*      */     }
/*  614 */     if (BitUtil.check(reportMaskExt, 25)) {
/*  615 */       index++;
/*      */     }
/*  617 */     if (BitUtil.check(reportMaskExt, 26)) {
/*  618 */       index++;
/*      */     }
/*  620 */     if (BitUtil.check(reportMaskExt, 27)) {
/*  621 */       index++;
/*      */     }
/*  623 */     if (BitUtil.check(reportMaskExt, 28)) {
/*  624 */       index++;
/*      */     }
/*  626 */     if (BitUtil.check(reportMaskExt, 29)) {
/*  627 */       index++;
/*      */     }
/*  629 */     if (BitUtil.check(reportMaskExt, 30)) {
/*  630 */       index++;
/*      */     }
/*      */     
/*  633 */     long reportMaskCan = 0L;
/*  634 */     if (BitUtil.check(reportMaskExt, 31) && !v[index++].isEmpty()) {
/*  635 */       reportMaskCan = Long.parseLong(v[index - 1], 16);
/*      */     }
/*  637 */     if (BitUtil.check(reportMaskCan, 0)) {
/*  638 */       index++;
/*      */     }
/*  640 */     if (BitUtil.check(reportMaskCan, 1)) {
/*  641 */       index++;
/*      */     }
/*  643 */     if (BitUtil.check(reportMaskCan, 2)) {
/*  644 */       index++;
/*      */     }
/*      */     
/*  647 */     DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
/*  648 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/*      */     
/*  650 */     if (!"GV355CEU".equals(model) && BitUtil.check(reportMask, 30)) {
/*  651 */       while (v[index].isEmpty()) {
/*  652 */         index++;
/*      */       }
/*  654 */       position.setValid((Integer.parseInt(v[index++]) > 0));
/*  655 */       if (!v[index].isEmpty()) {
/*  656 */         position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(v[index++])));
/*  657 */         position.setCourse(Integer.parseInt(v[index++]));
/*  658 */         position.setAltitude(Double.parseDouble(v[index++]));
/*  659 */         position.setLongitude(Double.parseDouble(v[index++]));
/*  660 */         position.setLatitude(Double.parseDouble(v[index++]));
/*  661 */         position.setTime(dateFormat.parse(v[index++]));
/*      */       } else {
/*  663 */         index += 6;
/*  664 */         getLastLocation(position, null);
/*      */       } 
/*      */     } else {
/*  667 */       getLastLocation(position, null);
/*      */     } 
/*      */     
/*  670 */     if (BitUtil.check(reportMask, 31)) {
/*  671 */       index += 4;
/*  672 */       index++;
/*      */     } 
/*      */     
/*  675 */     index = v.length - 2;
/*  676 */     if (this.ignoreFixTime) {
/*  677 */       position.setTime(dateFormat.parse(v[index]));
/*      */     } else {
/*  679 */       position.setDeviceTime(dateFormat.parse(v[index]));
/*      */     } 
/*      */     
/*  682 */     return position;
/*      */   }
/*      */   
/*      */   private void decodeStatus(Position position, Parser parser) {
/*  686 */     if (parser.hasNext(3)) {
/*  687 */       int ignition = parser.nextHexInt().intValue();
/*  688 */       if (BitUtil.check(ignition, 4)) {
/*  689 */         position.set("ignition", Boolean.valueOf(false));
/*  690 */       } else if (BitUtil.check(ignition, 5)) {
/*  691 */         position.set("ignition", Boolean.valueOf(true));
/*      */       } 
/*  693 */       position.set("input", parser.nextHexInt());
/*  694 */       position.set("output", parser.nextHexInt());
/*      */     } 
/*      */   }
/*      */   
/*  698 */   private static final Pattern PATTERN_FRI = (new PatternBuilder())
/*  699 */     .text("+").expression("(?:RESP|BUFF):GT...,")
/*  700 */     .expression("(?:.{6}|.{10})?,")
/*  701 */     .number("(d{15}|x{14}),")
/*  702 */     .expression("(?:([0-9A-Z]{17}),)?")
/*  703 */     .expression("([^,]+)?,")
/*  704 */     .number("(d+)?,")
/*  705 */     .number("(x{1,2}),").optional()
/*  706 */     .number("d{1,2},").optional()
/*  707 */     .number("d*,").optional()
/*  708 */     .number("(d+),").optional()
/*  709 */     .expression("((?:")
/*  710 */     .expression(PATTERN_LOCATION.pattern())
/*  711 */     .expression(")+)")
/*  712 */     .groupBegin()
/*  713 */     .number("d{1,2},")
/*  714 */     .number("(d{1,5})?,")
/*  715 */     .number("(d{1,3}),")
/*  716 */     .number("[01],")
/*  717 */     .number("(?:[01])?,")
/*  718 */     .number("(-?d{1,2}.d)?,")
/*  719 */     .or()
/*  720 */     .number("(d{1,7}.d)?,")
/*  721 */     .number("(d{5}:dd:dd)?,")
/*  722 */     .number("(x+)?,")
/*  723 */     .number("(x+)?,")
/*  724 */     .number("(d{1,3})?,")
/*  725 */     .number("(?:(xx)(xx)(xx))?,")
/*  726 */     .number("(d+)?,")
/*  727 */     .number("(?:d+.?d*|Inf|NaN)?,")
/*  728 */     .number("(d+)?,")
/*  729 */     .or()
/*  730 */     .number("(d{1,7}.d)?,").optional()
/*  731 */     .number("(d{1,3})?,")
/*  732 */     .or()
/*  733 */     .number("(-?d),")
/*  734 */     .number("(d{1,3}),")
/*  735 */     .groupEnd()
/*  736 */     .any()
/*  737 */     .number("(dddd)(dd)(dd)")
/*  738 */     .number("(dd)(dd)(dd)").optional(2)
/*  739 */     .text(",")
/*  740 */     .number("(xxxx)")
/*  741 */     .text("$").optional()
/*  742 */     .compile();
/*      */   
/*      */   private Object decodeFri(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  745 */     Parser parser = new Parser(PATTERN_FRI, sentence);
/*  746 */     if (!parser.matches()) {
/*  747 */       return null;
/*      */     }
/*      */     
/*  750 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  751 */     if (deviceSession == null) {
/*  752 */       return null;
/*      */     }
/*      */     
/*  755 */     LinkedList<Position> positions = new LinkedList<>();
/*      */     
/*  757 */     String vin = parser.next();
/*  758 */     String model = parser.next();
/*  759 */     String reportId = parser.next();
/*  760 */     String reportType = parser.next();
/*  761 */     Integer battery = parser.nextInt();
/*      */     
/*  763 */     Parser itemParser = new Parser(PATTERN_LOCATION, parser.next());
/*  764 */     while (itemParser.find()) {
/*  765 */       Position position1 = new Position(getProtocolName());
/*  766 */       position1.setDeviceId(deviceSession.getDeviceId());
/*      */       
/*  768 */       position1.set("vin", vin);
/*      */       
/*  770 */       decodeLocation(position1, itemParser);
/*      */       
/*  772 */       positions.add(position1);
/*      */     } 
/*      */     
/*  775 */     Position position = positions.getLast();
/*      */     
/*  777 */     skipLocation(parser);
/*      */     
/*  779 */     if (reportId != null) {
/*  780 */       if ("gv50m".equals(model)) {
/*  781 */         position.set("power", Double.valueOf(Integer.parseInt(reportId) * 0.001D));
/*      */       } else {
/*  783 */         position.set("reportId", reportId);
/*      */       } 
/*      */     }
/*  786 */     if (reportType != null) {
/*  787 */       position.set("reportType", reportType);
/*      */     }
/*      */     
/*  790 */     if (battery != null) {
/*  791 */       position.set("batteryLevel", battery);
/*      */     }
/*      */     
/*  794 */     if (parser.hasNext()) {
/*  795 */       position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/*      */     }
/*  797 */     position.set("batteryLevel", parser.nextInt());
/*  798 */     position.set("temp1", parser.nextDouble());
/*      */     
/*  800 */     if (parser.hasNext()) {
/*  801 */       position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     }
/*  803 */     position.set("hours", parser.next());
/*  804 */     position.set("adc1", parser.next());
/*  805 */     position.set("adc2", parser.next());
/*  806 */     position.set("batteryLevel", parser.nextInt());
/*      */     
/*  808 */     decodeStatus(position, parser);
/*      */     
/*  810 */     position.set("rpm", parser.nextInt());
/*  811 */     position.set("fuel", parser.nextInt());
/*      */     
/*  813 */     if (parser.hasNext()) {
/*  814 */       position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     }
/*  816 */     position.set("batteryLevel", parser.nextInt());
/*  817 */     position.set("rssi", parser.nextInt());
/*  818 */     position.set("batteryLevel", parser.nextInt());
/*      */     
/*  820 */     decodeDeviceTime(position, parser);
/*  821 */     if (this.ignoreFixTime) {
/*  822 */       positions.clear();
/*  823 */       positions.add(position);
/*      */     } 
/*      */     
/*  826 */     return positions;
/*      */   }
/*      */   
/*  829 */   private static final Pattern PATTERN_ERI = (new PatternBuilder())
/*  830 */     .text("+").expression("(?:RESP|BUFF):GTERI,")
/*  831 */     .expression("(?:.{6}|.{10})?,")
/*  832 */     .number("(d{15}|x{14}),")
/*  833 */     .expression("[^,]*,")
/*  834 */     .number("(x{8}),")
/*  835 */     .number("(d+)?,")
/*  836 */     .number("d{1,2},")
/*  837 */     .number("d{1,2},")
/*  838 */     .expression("((?:")
/*  839 */     .expression(PATTERN_LOCATION.pattern())
/*  840 */     .expression(")+)")
/*  841 */     .groupBegin()
/*  842 */     .number("(d{1,7}.d)?,")
/*  843 */     .number("(d{5}:dd:dd)?,")
/*  844 */     .number("(x+)?,")
/*  845 */     .number("(x+)?,").optional()
/*  846 */     .groupBegin()
/*  847 */     .number("(x+)?,")
/*  848 */     .number("(xx),")
/*  849 */     .number("(xx),")
/*  850 */     .or()
/*  851 */     .number("(d{1,3})?,")
/*  852 */     .number("(?:(xx)(xx)(xx))?,")
/*  853 */     .groupEnd()
/*  854 */     .expression("(.*)")
/*  855 */     .or()
/*  856 */     .number("d*,,")
/*  857 */     .number("(d+),")
/*  858 */     .any()
/*  859 */     .groupEnd()
/*  860 */     .number("(dddd)(dd)(dd)")
/*  861 */     .number("(dd)(dd)(dd)").optional(2)
/*  862 */     .text(",")
/*  863 */     .number("(xxxx)")
/*  864 */     .text("$").optional()
/*  865 */     .compile();
/*      */   
/*      */   private Object decodeEri(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  868 */     Parser parser = new Parser(PATTERN_ERI, sentence);
/*  869 */     if (!parser.matches()) {
/*  870 */       return null;
/*      */     }
/*      */     
/*  873 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  874 */     if (deviceSession == null) {
/*  875 */       return null;
/*      */     }
/*      */     
/*  878 */     long mask = parser.nextHexLong().longValue();
/*      */     
/*  880 */     LinkedList<Position> positions = new LinkedList<>();
/*      */     
/*  882 */     Integer power = parser.nextInt();
/*      */     
/*  884 */     Parser itemParser = new Parser(PATTERN_LOCATION, parser.next());
/*  885 */     while (itemParser.find()) {
/*  886 */       Position position1 = new Position(getProtocolName());
/*  887 */       position1.setDeviceId(deviceSession.getDeviceId());
/*      */       
/*  889 */       decodeLocation(position1, itemParser);
/*      */       
/*  891 */       positions.add(position1);
/*      */     } 
/*      */     
/*  894 */     Position position = positions.getLast();
/*      */     
/*  896 */     skipLocation(parser);
/*      */     
/*  898 */     if (power != null) {
/*  899 */       position.set("power", Double.valueOf(power.intValue() * 0.001D));
/*      */     }
/*      */     
/*  902 */     if (parser.hasNext(12)) {
/*      */       
/*  904 */       position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*  905 */       position.set("hours", parser.next());
/*  906 */       position.set("adc1", parser.next());
/*  907 */       position.set("adc2", parser.next());
/*  908 */       position.set("adc3", parser.next());
/*  909 */       if (parser.hasNext(2)) {
/*  910 */         position.set("input", parser.nextHexInt());
/*  911 */         position.set("output", parser.nextHexInt());
/*      */       } 
/*  913 */       if (parser.hasNext(4)) {
/*  914 */         position.set("batteryLevel", parser.nextInt());
/*  915 */         decodeStatus(position, parser);
/*      */       } 
/*      */       
/*  918 */       int index = 0;
/*  919 */       String[] data = parser.next().split(",");
/*      */       
/*  921 */       index++;
/*      */       
/*  923 */       if (BitUtil.check(mask, 0)) {
/*  924 */         position.set("fuel", Integer.valueOf(Integer.parseInt(data[index++], 16)));
/*      */       }
/*      */       
/*  927 */       if (BitUtil.check(mask, 1)) {
/*  928 */         int deviceCount = Integer.parseInt(data[index++]);
/*  929 */         for (int i = 1; i <= deviceCount; i++) {
/*  930 */           index++;
/*  931 */           index++;
/*  932 */           if (!data[index++].isEmpty()) {
/*  933 */             position.set("temp" + i, Double.valueOf((short)Integer.parseInt(data[index - 1], 16) * 0.0625D));
/*      */           }
/*      */         } 
/*      */       } 
/*      */       
/*  938 */       if (BitUtil.check(mask, 2)) {
/*  939 */         index++;
/*      */       }
/*      */       
/*  942 */       if (BitUtil.check(mask, 3) || BitUtil.check(mask, 4)) {
/*  943 */         int deviceCount = Integer.parseInt(data[index++]);
/*  944 */         for (int i = 1; i <= deviceCount; i++) {
/*  945 */           index++;
/*  946 */           if (BitUtil.check(mask, 3)) {
/*  947 */             position.set("fuel", Double.valueOf(Double.parseDouble(data[index++])));
/*      */           }
/*  949 */           if (BitUtil.check(mask, 4)) {
/*  950 */             index++;
/*      */           }
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  957 */     if (parser.hasNext()) {
/*  958 */       position.set("batteryLevel", parser.nextInt());
/*      */     }
/*      */     
/*  961 */     decodeDeviceTime(position, parser);
/*  962 */     if (this.ignoreFixTime) {
/*  963 */       positions.clear();
/*  964 */       positions.add(position);
/*      */     } 
/*      */     
/*  967 */     return positions;
/*      */   }
/*      */   
/*  970 */   private static final Pattern PATTERN_IGN = (new PatternBuilder())
/*  971 */     .text("+").expression("(?:RESP|BUFF):GTIG[NF],")
/*  972 */     .expression("(?:.{6}|.{10})?,")
/*  973 */     .number("(d{15}|x{14}),")
/*  974 */     .expression("[^,]*,")
/*  975 */     .number("d+,")
/*  976 */     .expression(PATTERN_LOCATION.pattern())
/*  977 */     .number("(d{5}:dd:dd)?,")
/*  978 */     .number("(d{1,7}.d)?,")
/*  979 */     .number("(dddd)(dd)(dd)")
/*  980 */     .number("(dd)(dd)(dd)").optional(2)
/*  981 */     .text(",")
/*  982 */     .number("(xxxx)")
/*  983 */     .text("$").optional()
/*  984 */     .compile();
/*      */   
/*      */   private Object decodeIgn(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  987 */     Parser parser = new Parser(PATTERN_IGN, sentence);
/*  988 */     Position position = initPosition(parser, channel, remoteAddress);
/*  989 */     if (position == null) {
/*  990 */       return null;
/*      */     }
/*      */     
/*  993 */     decodeLocation(position, parser);
/*      */     
/*  995 */     position.set("ignition", Boolean.valueOf(sentence.contains("IGN")));
/*  996 */     position.set("hours", parser.next());
/*  997 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     
/*  999 */     decodeDeviceTime(position, parser);
/*      */     
/* 1001 */     return position;
/*      */   }
/*      */   
/* 1004 */   private static final Pattern PATTERN_LSW = (new PatternBuilder())
/* 1005 */     .text("+RESP:").expression("GT[LT]SW,")
/* 1006 */     .expression("(?:.{6}|.{10})?,")
/* 1007 */     .number("(d{15}|x{14}),")
/* 1008 */     .expression("[^,]*,")
/* 1009 */     .number("[01],")
/* 1010 */     .number("([01]),")
/* 1011 */     .expression(PATTERN_LOCATION.pattern())
/* 1012 */     .number("(dddd)(dd)(dd)")
/* 1013 */     .number("(dd)(dd)(dd)").optional(2)
/* 1014 */     .text(",")
/* 1015 */     .number("(xxxx)")
/* 1016 */     .text("$").optional()
/* 1017 */     .compile();
/*      */   
/*      */   private Object decodeLsw(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1020 */     Parser parser = new Parser(PATTERN_LSW, sentence);
/* 1021 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1022 */     if (position == null) {
/* 1023 */       return null;
/*      */     }
/*      */     
/* 1026 */     position.set("in" + (sentence.contains("LSW") ? 1 : 2), Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*      */     
/* 1028 */     decodeLocation(position, parser);
/*      */     
/* 1030 */     decodeDeviceTime(position, parser);
/*      */     
/* 1032 */     return position;
/*      */   }
/*      */   
/* 1035 */   private static final Pattern PATTERN_IDA = (new PatternBuilder())
/* 1036 */     .text("+RESP:GTIDA,")
/* 1037 */     .expression("(?:.{6}|.{10})?,")
/* 1038 */     .number("(d{15}|x{14}),")
/* 1039 */     .expression("[^,]*,,")
/* 1040 */     .number("([^,]+),")
/* 1041 */     .expression("[01],")
/* 1042 */     .number("1,")
/* 1043 */     .expression(PATTERN_LOCATION.pattern())
/* 1044 */     .number("(d+.d),")
/* 1045 */     .text(",,,,")
/* 1046 */     .number("(dddd)(dd)(dd)")
/* 1047 */     .number("(dd)(dd)(dd)").optional(2)
/* 1048 */     .text(",")
/* 1049 */     .number("(xxxx)")
/* 1050 */     .text("$").optional()
/* 1051 */     .compile();
/*      */   
/*      */   private Object decodeIda(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1054 */     Parser parser = new Parser(PATTERN_IDA, sentence);
/* 1055 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1056 */     if (position == null) {
/* 1057 */       return null;
/*      */     }
/*      */     
/* 1060 */     position.set("driverUniqueId", parser.next());
/*      */     
/* 1062 */     decodeLocation(position, parser);
/*      */     
/* 1064 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     
/* 1066 */     decodeDeviceTime(position, parser);
/*      */     
/* 1068 */     return position;
/*      */   }
/*      */   
/* 1071 */   private static final Pattern PATTERN_WIF = (new PatternBuilder())
/* 1072 */     .text("+RESP:GTWIF,")
/* 1073 */     .expression("(?:.{6}|.{10})?,")
/* 1074 */     .number("(d{15}|x{14}),")
/* 1075 */     .expression("[^,]*,")
/* 1076 */     .number("(d+),")
/* 1077 */     .number("((?:x{12},-?d+,,,,)+),,,,")
/* 1078 */     .number("(d{1,3}),")
/* 1079 */     .number("(dddd)(dd)(dd)")
/* 1080 */     .number("(dd)(dd)(dd)").optional(2)
/* 1081 */     .text(",")
/* 1082 */     .number("(xxxx)")
/* 1083 */     .text("$").optional()
/* 1084 */     .compile();
/*      */   
/*      */   private Object decodeWif(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1087 */     Parser parser = new Parser(PATTERN_WIF, sentence);
/* 1088 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1089 */     if (position == null) {
/* 1090 */       return null;
/*      */     }
/*      */     
/* 1093 */     getLastLocation(position, null);
/*      */     
/* 1095 */     Network network = new Network();
/*      */     
/* 1097 */     parser.nextInt();
/* 1098 */     Matcher matcher = Pattern.compile("([0-9a-fA-F]{12}),(-?\\d+),,,,").matcher(parser.next());
/* 1099 */     while (matcher.find()) {
/* 1100 */       String mac = matcher.group(1).replaceAll("(..)", "$1:");
/* 1101 */       network.addWifiAccessPoint(WifiAccessPoint.from(mac
/* 1102 */             .substring(0, mac.length() - 1), Integer.parseInt(matcher.group(2))));
/*      */     } 
/*      */     
/* 1105 */     position.setNetwork(network);
/*      */     
/* 1107 */     position.set("batteryLevel", parser.nextInt());
/*      */     
/* 1109 */     return position;
/*      */   }
/*      */   
/* 1112 */   private static final Pattern PATTERN_GSM = (new PatternBuilder())
/* 1113 */     .text("+RESP:GTGSM,")
/* 1114 */     .expression("(?:.{6}|.{10})?,")
/* 1115 */     .number("(d{15}|x{14}),")
/* 1116 */     .expression("(?:STR|CTN|NMR|RTL),")
/* 1117 */     .expression("(.*)")
/* 1118 */     .number("(dddd)(dd)(dd)")
/* 1119 */     .number("(dd)(dd)(dd)").optional(2)
/* 1120 */     .text(",")
/* 1121 */     .number("(xxxx)")
/* 1122 */     .text("$").optional()
/* 1123 */     .compile();
/*      */   
/*      */   private Object decodeGsm(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1126 */     Parser parser = new Parser(PATTERN_GSM, sentence);
/* 1127 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1128 */     if (position == null) {
/* 1129 */       return null;
/*      */     }
/*      */     
/* 1132 */     getLastLocation(position, null);
/*      */     
/* 1134 */     Network network = new Network();
/*      */     
/* 1136 */     String[] data = parser.next().split(",");
/* 1137 */     for (int i = 0; i < 6; i++) {
/* 1138 */       if (!data[i * 6].isEmpty()) {
/* 1139 */         network.addCellTower(CellTower.from(
/* 1140 */               Integer.parseInt(data[i * 6]), Integer.parseInt(data[i * 6 + 1]), 
/* 1141 */               Integer.parseInt(data[i * 6 + 2], 16), Integer.parseInt(data[i * 6 + 3], 16), 
/* 1142 */               Integer.parseInt(data[i * 6 + 4])));
/*      */       }
/*      */     } 
/*      */     
/* 1146 */     position.setNetwork(network);
/*      */     
/* 1148 */     return position;
/*      */   }
/*      */   
/* 1151 */   private static final Pattern PATTERN_PNA = (new PatternBuilder())
/* 1152 */     .text("+RESP:GT").expression("P[NF]A,")
/* 1153 */     .expression("(?:.{6}|.{10})?,")
/* 1154 */     .number("(d{15}|x{14}),")
/* 1155 */     .expression("[^,]*,")
/* 1156 */     .number("(dddd)(dd)(dd)")
/* 1157 */     .number("(dd)(dd)(dd)").optional(2)
/* 1158 */     .text(",")
/* 1159 */     .number("(xxxx)")
/* 1160 */     .text("$").optional()
/* 1161 */     .compile();
/*      */   
/*      */   private Object decodePna(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1164 */     Parser parser = new Parser(PATTERN_PNA, sentence);
/* 1165 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1166 */     if (position == null) {
/* 1167 */       return null;
/*      */     }
/*      */     
/* 1170 */     getLastLocation(position, null);
/*      */     
/* 1172 */     position.set("alarm", sentence.contains("PNA") ? "powerOn" : "powerOff");
/*      */     
/* 1174 */     return position;
/*      */   }
/*      */   
/* 1177 */   private static final Pattern PATTERN_DTT = (new PatternBuilder())
/* 1178 */     .text("+RESP:GTDTT,")
/* 1179 */     .expression("(?:.{6}|.{10})?,")
/* 1180 */     .number("(d{15}|x{14}),")
/* 1181 */     .expression("[^,]*,,,")
/* 1182 */     .number("d,")
/* 1183 */     .number("d+,")
/* 1184 */     .number("(x+),")
/* 1185 */     .number("(dddd)(dd)(dd)")
/* 1186 */     .number("(dd)(dd)(dd)").optional(2)
/* 1187 */     .text(",")
/* 1188 */     .number("(xxxx)")
/* 1189 */     .text("$").optional()
/* 1190 */     .compile();
/*      */   
/*      */   private Object decodeDtt(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1193 */     Parser parser = new Parser(PATTERN_DTT, sentence);
/* 1194 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1195 */     if (position == null) {
/* 1196 */       return null;
/*      */     }
/*      */     
/* 1199 */     getLastLocation(position, null);
/*      */ 
/*      */     
/* 1202 */     String data = Unpooled.wrappedBuffer(DataConverter.parseHex(parser.next())).toString(StandardCharsets.US_ASCII);
/* 1203 */     if (data.contains("COMB")) {
/* 1204 */       String[] values = data.split(",");
/* 1205 */       position.set("fuel", Double.valueOf(Double.parseDouble(values[2])));
/*      */     } else {
/* 1207 */       position.set("result", data);
/*      */     } 
/*      */     
/* 1210 */     decodeDeviceTime(position, parser);
/*      */     
/* 1212 */     return position;
/*      */   }
/*      */   
/* 1215 */   private static final Pattern PATTERN_BAA = (new PatternBuilder())
/* 1216 */     .text("+RESP:GTBAA,")
/* 1217 */     .expression("(?:.{6}|.{10})?,")
/* 1218 */     .number("(d{15}|x{14}),")
/* 1219 */     .expression("[^,]*,")
/* 1220 */     .number("x+,")
/* 1221 */     .number("d,")
/* 1222 */     .number("d,")
/* 1223 */     .number("x+,")
/* 1224 */     .number("(x{4}),")
/* 1225 */     .expression("((?:[^,]+,){0,6})")
/* 1226 */     .expression(PATTERN_LOCATION.pattern())
/* 1227 */     .any()
/* 1228 */     .number("(dddd)(dd)(dd)")
/* 1229 */     .number("(dd)(dd)(dd)").optional(2)
/* 1230 */     .text(",")
/* 1231 */     .number("(xxxx)")
/* 1232 */     .text("$").optional()
/* 1233 */     .compile();
/*      */   
/*      */   private Object decodeBaa(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1236 */     Parser parser = new Parser(PATTERN_BAA, sentence);
/* 1237 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1238 */     if (position == null) {
/* 1239 */       return null;
/*      */     }
/*      */     
/* 1242 */     int mask = parser.nextHexInt().intValue();
/* 1243 */     String[] values = parser.next().split(",");
/* 1244 */     int index = 0;
/* 1245 */     if (BitUtil.check(mask, 0)) {
/* 1246 */       position.set("accessoryName", values[index++]);
/*      */     }
/* 1248 */     if (BitUtil.check(mask, 1)) {
/* 1249 */       position.set("accessoryMac", values[index++]);
/*      */     }
/* 1251 */     if (BitUtil.check(mask, 2)) {
/* 1252 */       position.set("accessoryStatus", Integer.valueOf(Integer.parseInt(values[index++])));
/*      */     }
/* 1254 */     if (BitUtil.check(mask, 3)) {
/* 1255 */       position.set("accessoryVoltage", Double.valueOf(Integer.parseInt(values[index++]) * 0.001D));
/*      */     }
/* 1257 */     if (BitUtil.check(mask, 4)) {
/* 1258 */       position.set("accessoryTemp", Integer.valueOf(Integer.parseInt(values[index++])));
/*      */     }
/* 1260 */     if (BitUtil.check(mask, 5)) {
/* 1261 */       position.set("accessoryHumidity", Integer.valueOf(Integer.parseInt(values[index])));
/*      */     }
/*      */     
/* 1264 */     decodeLocation(position, parser);
/*      */     
/* 1266 */     decodeDeviceTime(position, parser);
/*      */     
/* 1268 */     return position;
/*      */   }
/*      */   
/* 1271 */   private static final Pattern PATTERN_BID = (new PatternBuilder())
/* 1272 */     .text("+RESP:GTBID,")
/* 1273 */     .expression("(?:.{6}|.{10})?,")
/* 1274 */     .number("(d{15}|x{14}),")
/* 1275 */     .expression("[^,]*,")
/* 1276 */     .number("d,")
/* 1277 */     .number("d,")
/* 1278 */     .number("(x{4}),")
/* 1279 */     .expression("((?:[^,]+,){0,2})")
/* 1280 */     .expression(PATTERN_LOCATION.pattern())
/* 1281 */     .any()
/* 1282 */     .number("(dddd)(dd)(dd)")
/* 1283 */     .number("(dd)(dd)(dd)").optional(2)
/* 1284 */     .text(",")
/* 1285 */     .number("(xxxx)")
/* 1286 */     .text("$").optional()
/* 1287 */     .compile();
/*      */   
/*      */   private Object decodeBid(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1290 */     Parser parser = new Parser(PATTERN_BID, sentence);
/* 1291 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1292 */     if (position == null) {
/* 1293 */       return null;
/*      */     }
/*      */     
/* 1296 */     int mask = parser.nextHexInt().intValue();
/* 1297 */     String[] values = parser.next().split(",");
/* 1298 */     int index = 0;
/* 1299 */     if (BitUtil.check(mask, 1)) {
/* 1300 */       position.set("accessoryMac", values[index++]);
/*      */     }
/* 1302 */     if (BitUtil.check(mask, 3)) {
/* 1303 */       position.set("accessoryVoltage", Double.valueOf(Integer.parseInt(values[index]) * 0.001D));
/*      */     }
/*      */     
/* 1306 */     decodeLocation(position, parser);
/*      */     
/* 1308 */     decodeDeviceTime(position, parser);
/*      */     
/* 1310 */     return position;
/*      */   }
/*      */   
/* 1313 */   private static final Pattern PATTERN_LSA = (new PatternBuilder())
/* 1314 */     .text("+RESP:GTLSA,")
/* 1315 */     .expression("(?:.{6}|.{10})?,")
/* 1316 */     .number("(d{15}|x{14}),")
/* 1317 */     .expression("[^,]*,")
/* 1318 */     .number("d,")
/* 1319 */     .number("d,")
/* 1320 */     .number("d+,")
/* 1321 */     .expression(PATTERN_LOCATION.pattern())
/* 1322 */     .number("d+,")
/* 1323 */     .number("(d),")
/* 1324 */     .number("(d+),")
/* 1325 */     .number("[01],")
/* 1326 */     .number("[01]?,")
/* 1327 */     .number("(-?d+.d)?,")
/* 1328 */     .number("(dddd)(dd)(dd)")
/* 1329 */     .number("(dd)(dd)(dd)").optional(2)
/* 1330 */     .text(",")
/* 1331 */     .number("(xxxx)")
/* 1332 */     .text("$").optional()
/* 1333 */     .compile();
/*      */   
/*      */   private Object decodeLsa(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 1336 */     Parser parser = new Parser(PATTERN_LSA, sentence);
/* 1337 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1338 */     if (position == null) {
/* 1339 */       return null;
/*      */     }
/*      */     
/* 1342 */     decodeLocation(position, parser);
/*      */     
/* 1344 */     position.set("lightLevel", parser.nextInt());
/* 1345 */     position.set("batteryLevel", parser.nextInt());
/* 1346 */     position.set("temp1", parser.nextDouble());
/*      */     
/* 1348 */     decodeDeviceTime(position, parser);
/*      */     
/* 1350 */     return position;
/*      */   }
/*      */   
/* 1353 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 1354 */     .text("+").expression("(?:RESP|BUFF):GT...,")
/* 1355 */     .expression("(?:.{6}|.{10})?,")
/* 1356 */     .number("(d{15}|x{14}),")
/* 1357 */     .expression("[^,]*,")
/* 1358 */     .number("d*,")
/* 1359 */     .number("(x{1,2}),")
/* 1360 */     .number("d{1,2},")
/* 1361 */     .expression(PATTERN_LOCATION.pattern())
/* 1362 */     .groupBegin()
/* 1363 */     .number("(d{1,7}.d)?,").optional()
/* 1364 */     .number("(d{1,3})?,")
/* 1365 */     .or()
/* 1366 */     .number("(d{1,7}.d)?,")
/* 1367 */     .groupEnd()
/* 1368 */     .number("(dddd)(dd)(dd)")
/* 1369 */     .number("(dd)(dd)(dd)")
/* 1370 */     .text(",")
/* 1371 */     .number("(xxxx)")
/* 1372 */     .text("$").optional()
/* 1373 */     .compile();
/*      */   
/*      */   private Object decodeOther(Channel channel, SocketAddress remoteAddress, String sentence, String type) {
/* 1376 */     Parser parser = new Parser(PATTERN, sentence);
/* 1377 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1378 */     if (position == null) {
/* 1379 */       return null;
/*      */     }
/*      */     
/* 1382 */     int reportType = parser.nextHexInt().intValue();
/* 1383 */     if (type.equals("NMR")) {
/* 1384 */       position.set("motion", Boolean.valueOf((reportType == 1)));
/* 1385 */     } else if (type.equals("SOS")) {
/* 1386 */       position.set("alarm", "sos");
/* 1387 */     } else if (type.equals("DIS")) {
/* 1388 */       position.set("in" + (reportType / 16), Boolean.valueOf((reportType % 16 == 1)));
/* 1389 */     } else if (type.equals("IGL")) {
/* 1390 */       position.set("ignition", Boolean.valueOf((reportType % 16 == 1)));
/* 1391 */     } else if (type.equals("HBM")) {
/* 1392 */       switch (reportType % 16) {
/*      */         case 0:
/*      */         case 3:
/* 1395 */           position.set("alarm", "hardBraking");
/*      */           break;
/*      */         case 1:
/*      */         case 4:
/* 1399 */           position.set("alarm", "hardAcceleration");
/*      */           break;
/*      */         case 2:
/* 1402 */           position.set("alarm", "hardCornering");
/*      */           break;
/*      */         case 5:
/* 1405 */           position.set("alarm", "unknownHarshBehavior");
/*      */           break;
/*      */       } 
/*      */ 
/*      */ 
/*      */     
/*      */     } 
/* 1412 */     decodeLocation(position, parser);
/*      */     
/* 1414 */     if (parser.hasNext()) {
/* 1415 */       position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     }
/* 1417 */     position.set("batteryLevel", parser.nextInt());
/*      */     
/* 1419 */     if (parser.hasNext()) {
/* 1420 */       position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*      */     }
/*      */     
/* 1423 */     decodeDeviceTime(position, parser);
/*      */     
/* 1425 */     return position;
/*      */   }
/*      */   
/* 1428 */   private static final Pattern PATTERN_BASIC = (new PatternBuilder())
/* 1429 */     .text("+").expression("(?:RESP|BUFF)").text(":")
/* 1430 */     .expression("GT...,")
/* 1431 */     .number("(?:[0-9A-Z]{2}xxxx)?,").optional()
/* 1432 */     .number("(d{15}|x{14}),")
/* 1433 */     .any()
/* 1434 */     .number("(d{1,2})?,")
/* 1435 */     .number("(d{1,3}.d)?,")
/* 1436 */     .number("(d{1,3})?,")
/* 1437 */     .number("(-?d{1,5}.d)?,")
/* 1438 */     .number("(-?d{1,3}.d{6})?,")
/* 1439 */     .number("(-?d{1,2}.d{6})?,")
/* 1440 */     .number("(dddd)(dd)(dd)")
/* 1441 */     .number("(dd)(dd)(dd)").optional(2)
/* 1442 */     .text(",")
/* 1443 */     .number("(d+),")
/* 1444 */     .number("(d+),")
/* 1445 */     .number("(x+),")
/* 1446 */     .number("(x+),").optional(4)
/* 1447 */     .any()
/* 1448 */     .number("(dddd)(dd)(dd)")
/* 1449 */     .number("(dd)(dd)(dd)").optional(2)
/* 1450 */     .text(",")
/* 1451 */     .number("(xxxx)")
/* 1452 */     .text("$").optional()
/* 1453 */     .compile();
/*      */   
/*      */   private Object decodeBasic(Channel channel, SocketAddress remoteAddress, String sentence, String type) {
/* 1456 */     Parser parser = new Parser(PATTERN_BASIC, sentence);
/* 1457 */     Position position = initPosition(parser, channel, remoteAddress);
/* 1458 */     if (position == null) {
/* 1459 */       return null;
/*      */     }
/*      */     
/* 1462 */     if (parser.hasNext()) {
/* 1463 */       int hdop = parser.nextInt().intValue();
/* 1464 */       position.setValid((hdop > 0));
/* 1465 */       position.set("hdop", Integer.valueOf(hdop));
/*      */     } 
/*      */     
/* 1468 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 1469 */     position.setCourse(parser.nextDouble(0.0D));
/* 1470 */     position.setAltitude(parser.nextDouble(0.0D));
/*      */     
/* 1472 */     if (parser.hasNext(2)) {
/* 1473 */       position.setLongitude(parser.nextDouble().doubleValue());
/* 1474 */       position.setLatitude(parser.nextDouble().doubleValue());
/*      */     } else {
/* 1476 */       getLastLocation(position, null);
/*      */     } 
/*      */     
/* 1479 */     if (parser.hasNext(6)) {
/* 1480 */       position.setTime(parser.nextDateTime());
/*      */     }
/*      */     
/* 1483 */     if (parser.hasNext(4)) {
/* 1484 */       position.setNetwork(new Network(CellTower.from(parser
/* 1485 */               .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue())));
/*      */     }
/*      */     
/* 1488 */     decodeDeviceTime(position, parser);
/*      */     
/* 1490 */     switch (type) {
/*      */       case "TOW":
/* 1492 */         position.set("alarm", "tow");
/*      */         break;
/*      */       case "IDL":
/* 1495 */         position.set("alarm", "idle");
/*      */         break;
/*      */       case "PNA":
/* 1498 */         position.set("alarm", "powerOn");
/*      */         break;
/*      */       case "PFA":
/* 1501 */         position.set("alarm", "powerOff");
/*      */         break;
/*      */       case "EPN":
/*      */       case "MPN":
/* 1505 */         position.set("alarm", "powerRestored");
/*      */         break;
/*      */       case "EPF":
/*      */       case "MPF":
/* 1509 */         position.set("alarm", "powerCut");
/*      */         break;
/*      */       case "BPL":
/* 1512 */         position.set("alarm", "lowBattery");
/*      */         break;
/*      */       case "STT":
/* 1515 */         position.set("alarm", "movement");
/*      */         break;
/*      */       case "SWG":
/* 1518 */         position.set("alarm", "geofence");
/*      */         break;
/*      */       case "TMP":
/*      */       case "TEM":
/* 1522 */         position.set("alarm", "temperature");
/*      */         break;
/*      */       case "JDR":
/*      */       case "JDS":
/* 1526 */         position.set("alarm", "jamming");
/*      */         break;
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 1532 */     return position;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*      */     Object result;
/* 1539 */     String sentence = ((ByteBuf)msg).toString(StandardCharsets.US_ASCII);
/*      */     
/* 1541 */     int typeIndex = sentence.indexOf(":GT");
/* 1542 */     if (typeIndex < 0) {
/* 1543 */       return null;
/*      */     }
/*      */ 
/*      */     
/* 1547 */     String type = sentence.substring(typeIndex + 3, typeIndex + 6);
/* 1548 */     if (sentence.startsWith("+ACK")) {
/* 1549 */       result = decodeAck(channel, remoteAddress, sentence, type);
/*      */     } else {
/* 1551 */       switch (type) {
/*      */         case "PDP":
/* 1553 */           result = decodePdp(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "INF":
/* 1556 */           result = decodeInf(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "OBD":
/* 1559 */           result = decodeObd(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "CAN":
/* 1562 */           result = decodeCan(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "CTN":
/*      */         case "FRI":
/*      */         case "GEO":
/*      */         case "RTL":
/*      */         case "DOG":
/*      */         case "STR":
/* 1570 */           result = decodeFri(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "ERI":
/* 1573 */           result = decodeEri(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "IGN":
/*      */         case "IGF":
/* 1577 */           result = decodeIgn(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "LSW":
/*      */         case "TSW":
/* 1581 */           result = decodeLsw(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "IDA":
/* 1584 */           result = decodeIda(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "WIF":
/* 1587 */           result = decodeWif(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "GSM":
/* 1590 */           result = decodeGsm(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "VER":
/* 1593 */           result = decodeVer(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "PNA":
/*      */         case "PFA":
/* 1597 */           result = decodePna(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "DTT":
/* 1600 */           result = decodeDtt(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "BAA":
/* 1603 */           result = decodeBaa(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "BID":
/* 1606 */           result = decodeBid(channel, remoteAddress, sentence);
/*      */           break;
/*      */         case "LSA":
/* 1609 */           result = decodeLsa(channel, remoteAddress, sentence);
/*      */           break;
/*      */         default:
/* 1612 */           result = decodeOther(channel, remoteAddress, sentence, type);
/*      */           break;
/*      */       } 
/*      */       
/* 1616 */       if (result == null) {
/* 1617 */         result = decodeBasic(channel, remoteAddress, sentence, type);
/*      */       }
/*      */       
/* 1620 */       if (result != null) {
/* 1621 */         if (result instanceof Position) {
/* 1622 */           ((Position)result).set("type", type);
/*      */         } else {
/* 1624 */           for (Position p : (Iterable<? extends Position>) result) {
/* 1625 */             p.set("type", type);
/*      */           }
/*      */         } 
/*      */       }
/*      */     } 
/*      */     
/* 1631 */     if (channel != null && Context.getConfig().getBoolean(getProtocolName() + ".ack")) {
/*      */       String checksum;
/* 1633 */       if (sentence.endsWith("$")) {
/* 1634 */         checksum = sentence.substring(sentence.length() - 1 - 4, sentence.length() - 1);
/*      */       } else {
/* 1636 */         checksum = sentence.substring(sentence.length() - 4);
/*      */       } 
/* 1638 */       channel.writeAndFlush(new NetworkMessage("+SACK:" + checksum + "$", remoteAddress));
/*      */     } 
/*      */     
/* 1641 */     return result;
/*      */   }
/*      */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl200TextProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */