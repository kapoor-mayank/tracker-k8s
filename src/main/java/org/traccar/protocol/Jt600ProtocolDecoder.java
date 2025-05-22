/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.BitBuffer;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class Jt600ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Jt600ProtocolDecoder(Protocol protocol) {
/*  46 */     super(protocol);
/*     */   }
/*     */   
/*     */   private static double convertCoordinate(int raw) {
/*  50 */     int degrees = raw / 1000000;
/*  51 */     double minutes = (raw % 1000000) / 10000.0D;
/*  52 */     return degrees + minutes / 60.0D;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeStatus(Position position, ByteBuf buf) {
/*  57 */     int value = buf.readUnsignedByte();
/*     */     
/*  59 */     position.set("ignition", Boolean.valueOf(BitUtil.check(value, 0)));
/*  60 */     position.set("door", Boolean.valueOf(BitUtil.check(value, 6)));
/*     */     
/*  62 */     value = buf.readUnsignedByte();
/*     */     
/*  64 */     position.set("charge", Boolean.valueOf(BitUtil.check(value, 0)));
/*  65 */     position.set("blocked", Boolean.valueOf(BitUtil.check(value, 1)));
/*     */     
/*  67 */     if (BitUtil.check(value, 2)) {
/*  68 */       position.set("alarm", "sos");
/*     */     }
/*  70 */     if (BitUtil.check(value, 3) || BitUtil.check(value, 4)) {
/*  71 */       position.set("alarm", "gpsAntennaCut");
/*     */     }
/*  73 */     if (BitUtil.check(value, 4)) {
/*  74 */       position.set("alarm", "overspeed");
/*     */     }
/*     */     
/*  77 */     value = buf.readUnsignedByte();
/*     */     
/*  79 */     if (BitUtil.check(value, 2)) {
/*  80 */       position.set("alarm", "fatigueDriving");
/*     */     }
/*  82 */     if (BitUtil.check(value, 3)) {
/*  83 */       position.set("alarm", "tow");
/*     */     }
/*     */     
/*  86 */     buf.readUnsignedByte();
/*     */   }
/*     */ 
/*     */   
/*     */   static boolean isLongFormat(ByteBuf buf) {
/*  91 */     return (buf.getUnsignedByte(buf.readerIndex() + 8) == 0);
/*     */   }
/*     */   
/*     */   public boolean isCustomProtocol(long deviceId) {
/*  95 */     return Context.getIdentityManager().lookupAttributeBoolean(deviceId, 
/*  96 */         getProtocolName() + ".custom", false, true);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static void decodeBinaryLocation(ByteBuf buf, Position position) {
/* 107 */     DateBuilder dateBuilder = (new DateBuilder()).setDay(BcdUtil.readInteger(buf, 2)).setMonth(BcdUtil.readInteger(buf, 2)).setYear(BcdUtil.readInteger(buf, 2)).setHour(BcdUtil.readInteger(buf, 2)).setMinute(BcdUtil.readInteger(buf, 2)).setSecond(BcdUtil.readInteger(buf, 2));
/* 108 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 110 */     double latitude = convertCoordinate(BcdUtil.readInteger(buf, 8));
/* 111 */     double longitude = convertCoordinate(BcdUtil.readInteger(buf, 9));
/*     */     
/* 113 */     byte flags = buf.readByte();
/* 114 */     position.setValid(BitUtil.check(flags, 0));
/* 115 */     position.setLatitude(BitUtil.check(flags, 1) ? latitude : -latitude);
/* 116 */     position.setLongitude(BitUtil.check(flags, 2) ? longitude : -longitude);
/*     */     
/* 118 */     position.setSpeed(BcdUtil.readInteger(buf, 2));
/* 119 */     position.setCourse(buf.readUnsignedByte() * 2.0D);
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Position> decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {
/* 124 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 126 */     boolean longFormat = isLongFormat(buf);
/*     */     
/* 128 */     buf.readByte();
/*     */     
/* 130 */     String id = String.valueOf(Long.parseLong(ByteBufUtil.hexDump(buf.readSlice(5))));
/* 131 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 132 */     if (deviceSession == null) {
/* 133 */       return null;
/*     */     }
/*     */     
/* 136 */     int protocolVersion = 0;
/* 137 */     if (longFormat) {
/* 138 */       protocolVersion = buf.readUnsignedByte();
/*     */     }
/*     */     
/* 141 */     int version = BitUtil.from(buf.readUnsignedByte(), 4);
/* 142 */     buf.readUnsignedShort();
/*     */     
/* 144 */     boolean responseRequired = false;
/*     */     
/* 146 */     while (buf.readableBytes() >= 17) {
/*     */       
/* 148 */       Position position = new Position(getProtocolName());
/* 149 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 151 */       decodeBinaryLocation(buf, position);
/*     */       
/* 153 */       if (longFormat) {
/*     */         
/* 155 */         position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
/* 156 */         position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/* 158 */         buf.readUnsignedInt();
/*     */         
/* 160 */         int status = buf.readUnsignedShort();
/* 161 */         if (BitUtil.check(status, 5)) {
/* 162 */           responseRequired = true;
/*     */         }
/* 164 */         if (buf.getByte(1) == 120 || buf.getByte(1) == Byte.MIN_VALUE || 
/* 165 */           isCustomProtocol(deviceSession.getDeviceId())) {
/* 166 */           position.set("lbs", Boolean.valueOf(BitUtil.check(status, 0)));
/* 167 */           position.set("alarm", BitUtil.check(status, 1) ? "geofenceEnter" : null);
/* 168 */           position.set("alarm", BitUtil.check(status, 2) ? "geofenceExit" : null);
/* 169 */           position.set("alarm", BitUtil.check(status, 3) ? "stringCut" : null);
/* 170 */           position.set("alarm", BitUtil.check(status, 4) ? "vibration" : null);
/* 171 */           position.set("steelCable", Boolean.valueOf(BitUtil.check(status, 6)));
/* 172 */           position.set("blocked", Boolean.valueOf(BitUtil.check(status, 7)));
/* 173 */           position.set("alarm", BitUtil.check(status, 8) ? "unlock" : null);
/* 174 */           position.set("alarm", BitUtil.check(status, 9) ? "wrongPassword" : null);
/* 175 */           position.set("alarm", BitUtil.check(status, 10) ? "unauthorizedSwipe" : null);
/* 176 */           position.set("alarm", BitUtil.check(status, 11) ? "lowBattery" : null);
/* 177 */           position.set("alarm", BitUtil.check(status, 12) ? "openBackCap" : null);
/* 178 */           position.set("backCap", Boolean.valueOf(BitUtil.check(status, 13)));
/* 179 */           position.set("alarm", BitUtil.check(status, 14) ? "motorFault" : null);
/*     */         } else {
/* 181 */           position.set("alarm", BitUtil.check(status, 1) ? "geofenceEnter" : null);
/* 182 */           position.set("alarm", BitUtil.check(status, 2) ? "geofenceExit" : null);
/* 183 */           position.set("alarm", BitUtil.check(status, 3) ? "powerCut" : null);
/* 184 */           position.set("alarm", BitUtil.check(status, 4) ? "vibration" : null);
/* 185 */           position.set("blocked", Boolean.valueOf(BitUtil.check(status, 7)));
/* 186 */           position.set("alarm", BitUtil.check(status, 11) ? "lowBattery" : null);
/* 187 */           position.set("alarm", BitUtil.check(status, 14) ? "fault" : null);
/*     */         } 
/* 189 */         position.set("status", Integer.valueOf(status));
/*     */         
/* 191 */         int battery = buf.readUnsignedByte();
/* 192 */         if (battery == 255) {
/* 193 */           position.set("charge", Boolean.valueOf(true));
/*     */         } else {
/* 195 */           position.set("batteryLevel", Integer.valueOf(battery));
/*     */         } 
/*     */         
/* 198 */         CellTower cellTower = CellTower.fromCidLac(buf.readUnsignedShort(), buf.readUnsignedShort());
/* 199 */         cellTower.setSignalStrength(Integer.valueOf(buf.readUnsignedByte()));
/* 200 */         position.setNetwork(new Network(cellTower));
/*     */         
/* 202 */         if (protocolVersion == 23 || protocolVersion == 25) {
/* 203 */           buf.readUnsignedByte();
/* 204 */           buf.skipBytes(3);
/* 205 */           buf.skipBytes(buf.readableBytes() - 1);
/*     */         }
/*     */       
/* 208 */       } else if (version == 1) {
/*     */         
/* 210 */         position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 211 */         position.set("power", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/* 213 */         buf.readByte();
/*     */         
/* 215 */         position.setAltitude(buf.readUnsignedShort());
/*     */         
/* 217 */         int cid = buf.readUnsignedShort();
/* 218 */         int lac = buf.readUnsignedShort();
/* 219 */         int rssi = buf.readUnsignedByte();
/*     */         
/* 221 */         if (cid != 0 && lac != 0) {
/* 222 */           CellTower cellTower = CellTower.fromCidLac(cid, lac);
/* 223 */           cellTower.setSignalStrength(Integer.valueOf(rssi));
/* 224 */           position.setNetwork(new Network(cellTower));
/*     */         } else {
/* 226 */           position.set("rssi", Integer.valueOf(rssi));
/*     */         }
/*     */       
/* 229 */       } else if (version == 2) {
/*     */         
/* 231 */         int fuel = buf.readUnsignedByte() << 8;
/*     */         
/* 233 */         decodeStatus(position, buf);
/* 234 */         position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
/*     */         
/* 236 */         fuel += buf.readUnsignedByte();
/* 237 */         position.set("fuel", Integer.valueOf(fuel));
/*     */       }
/* 239 */       else if (version == 3) {
/*     */         
/* 241 */         BitBuffer bitBuffer = new BitBuffer(buf);
/*     */         
/* 243 */         position.set("fuel1", Integer.valueOf(bitBuffer.readUnsigned(12)));
/* 244 */         position.set("fuel2", Integer.valueOf(bitBuffer.readUnsigned(12)));
/* 245 */         position.set("fuel3", Integer.valueOf(bitBuffer.readUnsigned(12)));
/* 246 */         position.set("odometer", Integer.valueOf(bitBuffer.readUnsigned(20) * 1000));
/*     */         
/* 248 */         int status = bitBuffer.readUnsigned(24);
/* 249 */         position.set("ignition", Boolean.valueOf(BitUtil.check(status, 0)));
/* 250 */         position.set("status", Integer.valueOf(status));
/*     */       } 
/*     */ 
/*     */       
/* 254 */       positions.add(position);
/*     */     } 
/*     */ 
/*     */     
/* 258 */     int index = buf.readUnsignedByte();
/*     */     
/* 260 */     if (channel != null && responseRequired) {
/* 261 */       if (protocolVersion < 25) {
/* 262 */         channel.writeAndFlush(new NetworkMessage("(P35)", remoteAddress));
/*     */       } else {
/* 264 */         channel.writeAndFlush(new NetworkMessage("(P69,0," + index + ")", remoteAddress));
/*     */       } 
/*     */     }
/*     */     
/* 268 */     return positions;
/*     */   }
/*     */   
/* 271 */   private static final Pattern PATTERN_W01 = (new PatternBuilder())
/* 272 */     .text("(")
/* 273 */     .number("(d+),")
/* 274 */     .text("W01,")
/* 275 */     .number("(ddd)(dd.dddd),")
/* 276 */     .expression("([EW]),")
/* 277 */     .number("(dd)(dd.dddd),")
/* 278 */     .expression("([NS]),")
/* 279 */     .expression("([AV]),")
/* 280 */     .number("(dd)(dd)(dd),")
/* 281 */     .number("(dd)(dd)(dd),")
/* 282 */     .number("(d+),")
/* 283 */     .number("(d+),")
/* 284 */     .number("(d+),")
/* 285 */     .number("(d+),")
/* 286 */     .number("(d+),")
/* 287 */     .number("(d+),")
/* 288 */     .any()
/* 289 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeW01(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 293 */     Parser parser = new Parser(PATTERN_W01, sentence);
/* 294 */     if (!parser.matches()) {
/* 295 */       return null;
/*     */     }
/*     */     
/* 298 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 299 */     if (deviceSession == null) {
/* 300 */       return null;
/*     */     }
/*     */     
/* 303 */     Position position = new Position(getProtocolName());
/* 304 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 306 */     position.setLongitude(parser.nextCoordinate());
/* 307 */     position.setLatitude(parser.nextCoordinate());
/* 308 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 310 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 312 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 313 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 315 */     position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/* 316 */     position.set("gps", Integer.valueOf(parser.nextInt(0)));
/* 317 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/* 318 */     position.set("alertType", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 320 */     return position;
/*     */   }
/*     */   
/* 323 */   private static final Pattern PATTERN_U01 = (new PatternBuilder())
/* 324 */     .text("(")
/* 325 */     .number("(d+),")
/* 326 */     .number("(Udd),")
/* 327 */     .number("d+,").optional()
/* 328 */     .number("(dd)(dd)(dd),")
/* 329 */     .number("(dd)(dd)(dd),")
/* 330 */     .expression("([TF]),")
/* 331 */     .number("(d+.d+),([NS]),")
/* 332 */     .number("(d+.d+),([EW]),")
/* 333 */     .number("(d+.?d*),")
/* 334 */     .number("(d+),")
/* 335 */     .number("(d+),")
/* 336 */     .number("(d+)%,")
/* 337 */     .expression("([01]+),")
/* 338 */     .number("(d+),")
/* 339 */     .number("(d+),")
/* 340 */     .number("(d+),")
/* 341 */     .number("(d+),")
/* 342 */     .number("(d+)")
/* 343 */     .number(",(xx)").optional()
/* 344 */     .any()
/* 345 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeU01(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 349 */     Parser parser = new Parser(PATTERN_U01, sentence);
/* 350 */     if (!parser.matches()) {
/* 351 */       return null;
/*     */     }
/*     */     
/* 354 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 355 */     if (deviceSession == null) {
/* 356 */       return null;
/*     */     }
/*     */     
/* 359 */     String type = parser.next();
/*     */     
/* 361 */     Position position = new Position(getProtocolName());
/* 362 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 364 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 366 */     position.setValid(parser.next().equals("T"));
/* 367 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 368 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/*     */     
/* 370 */     position.setSpeed(UnitsConverter.knotsFromMph(parser.nextDouble(0.0D)));
/* 371 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 373 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 374 */     position.set("batteryLevel", Integer.valueOf(parser.nextInt(0)));
/* 375 */     position.set("status", Integer.valueOf(parser.nextBinInt(0)));
/*     */     
/* 377 */     CellTower cellTower = CellTower.fromCidLac(parser.nextInt(0), parser.nextInt(0));
/* 378 */     cellTower.setSignalStrength(Integer.valueOf(parser.nextInt(0)));
/* 379 */     position.setNetwork(new Network(cellTower));
/*     */     
/* 381 */     position.set("odometer", Long.valueOf(parser.nextLong(0L) * 1000L));
/* 382 */     position.set("index", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 384 */     if (channel != null) {
/* 385 */       if (type.equals("U01") || type.equals("U02") || type.equals("U03")) {
/* 386 */         channel.writeAndFlush(new NetworkMessage("(S39)", remoteAddress));
/* 387 */       } else if (type.equals("U06")) {
/* 388 */         channel.writeAndFlush(new NetworkMessage("(S20)", remoteAddress));
/*     */       } 
/*     */     }
/*     */     
/* 392 */     return position;
/*     */   }
/*     */   
/* 395 */   private static final Pattern PATTERN_P45 = (new PatternBuilder())
/* 396 */     .text("(")
/* 397 */     .number("(d+),")
/* 398 */     .text("P45,")
/* 399 */     .number("(dd)(dd)(dd),")
/* 400 */     .number("(dd)(dd)(dd),")
/* 401 */     .number("(d+.d+),([NS]),")
/* 402 */     .number("(d+.d+),([EW]),")
/* 403 */     .expression("([AV]),")
/* 404 */     .number("(d+),")
/* 405 */     .number("(d+),")
/* 406 */     .number("(d+),")
/* 407 */     .number("d+,")
/* 408 */     .number("(d+),")
/* 409 */     .number("d+,")
/* 410 */     .number("d+,")
/* 411 */     .number("(d+),")
/* 412 */     .any()
/* 413 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeP45(String sentence, Channel channel, SocketAddress remoteAddress) {
/* 417 */     Parser parser = new Parser(PATTERN_P45, sentence);
/* 418 */     if (!parser.matches()) {
/* 419 */       return null;
/*     */     }
/*     */     
/* 422 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 423 */     if (deviceSession == null) {
/* 424 */       return null;
/*     */     }
/*     */     
/* 427 */     Position position = new Position(getProtocolName());
/* 428 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 430 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 432 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 433 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 434 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 436 */     position.setSpeed(UnitsConverter.knotsFromMph(parser.nextDouble().doubleValue()));
/* 437 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 439 */     position.set("eventSource", parser.nextInt());
/*     */     
/* 441 */     String rfid = parser.next();
/* 442 */     if (!rfid.equals("0000000000")) {
/* 443 */       position.set("driverUniqueId", rfid);
/*     */     }
/*     */     
/* 446 */     int index = parser.nextInt().intValue();
/*     */     
/* 448 */     if (channel != null) {
/* 449 */       channel.writeAndFlush(new NetworkMessage("(P69,0," + index + ")", remoteAddress));
/*     */     }
/*     */     
/* 452 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 459 */     ByteBuf buf = (ByteBuf)msg;
/* 460 */     char first = (char)buf.getByte(0);
/*     */     
/* 462 */     if (first == '$')
/* 463 */       return decodeBinary(buf, channel, remoteAddress); 
/* 464 */     if (first == '(') {
/* 465 */       String sentence = buf.toString(StandardCharsets.US_ASCII);
/* 466 */       if (sentence.contains("W01"))
/* 467 */         return decodeW01(sentence, channel, remoteAddress); 
/* 468 */       if (sentence.contains("P45")) {
/* 469 */         return decodeP45(sentence, channel, remoteAddress);
/*     */       }
/* 471 */       return decodeU01(sentence, channel, remoteAddress);
/*     */     } 
/*     */ 
/*     */     
/* 475 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Jt600ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */