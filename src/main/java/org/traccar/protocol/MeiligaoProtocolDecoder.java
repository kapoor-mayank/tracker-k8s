/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ public class MeiligaoProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  43 */   private Map<Byte, ByteBuf> photos = new HashMap<>();
/*     */   
/*     */   public MeiligaoProtocolDecoder(Protocol protocol) {
/*  46 */     super(protocol);
/*     */   }
/*     */   
/*  49 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  50 */     .number("(d+)(dd)(dd).?d*,")
/*  51 */     .expression("([AV]),")
/*  52 */     .number("(d+)(dd.d+),")
/*  53 */     .expression("([NS]),")
/*  54 */     .number("(d+)(dd.d+),")
/*  55 */     .expression("([EW]),")
/*  56 */     .number("(d+.?d*)?,")
/*  57 */     .number("(d+.?d*)?,")
/*  58 */     .number("(dd)(dd)(dd)")
/*  59 */     .expression("[^\\|]*")
/*  60 */     .groupBegin()
/*  61 */     .number("|(d+.d+)?")
/*  62 */     .number("|(-?d+.?d*)?")
/*  63 */     .number("|(xxxx)?")
/*  64 */     .groupBegin()
/*  65 */     .number("|(xxxx),(xxxx)")
/*  66 */     .number(",(xxxx)").optional()
/*  67 */     .number(",(xxxx)").optional()
/*  68 */     .number(",(xxxx)").optional()
/*  69 */     .number(",(xxxx)").optional()
/*  70 */     .number(",(xxxx)").optional()
/*  71 */     .number(",(xxxx)").optional()
/*  72 */     .groupBegin()
/*  73 */     .number("|x{16,20}")
/*  74 */     .number("|(xx)")
/*  75 */     .number("|(x{8})")
/*  76 */     .groupBegin()
/*  77 */     .number("|(xx)")
/*  78 */     .groupBegin()
/*  79 */     .text("|")
/*  80 */     .expression("(.*)")
/*  81 */     .groupEnd("?")
/*  82 */     .groupEnd("?")
/*  83 */     .or()
/*  84 */     .number("|(d{1,9})")
/*  85 */     .groupBegin()
/*  86 */     .number("|(x{5,})")
/*  87 */     .groupEnd("?")
/*  88 */     .groupEnd("?")
/*  89 */     .groupEnd("?")
/*  90 */     .groupEnd("?")
/*  91 */     .any()
/*  92 */     .compile();
/*     */   
/*  94 */   private static final Pattern PATTERN_RFID = (new PatternBuilder())
/*  95 */     .number("|(dd)(dd)(dd),")
/*  96 */     .number("(dd)(dd)(dd),")
/*  97 */     .number("(d+)(dd.d+),")
/*  98 */     .expression("([NS]),")
/*  99 */     .number("(d+)(dd.d+),")
/* 100 */     .expression("([EW])")
/* 101 */     .compile();
/*     */   
/* 103 */   private static final Pattern PATTERN_OBD = (new PatternBuilder())
/* 104 */     .number("(d+.d+),")
/* 105 */     .number("(d+),")
/* 106 */     .number("(d+),")
/* 107 */     .number("(d+.d+),")
/* 108 */     .number("(d+.d+),")
/* 109 */     .number("(-?d+),")
/* 110 */     .number("(d+.d+),")
/* 111 */     .number("(d+.d+),")
/* 112 */     .number("(d+.d+),")
/* 113 */     .number("(d+.?d*),")
/* 114 */     .number("(d+.d+),")
/* 115 */     .number("(d+.d+),")
/* 116 */     .number("(d+),")
/* 117 */     .number("(d+),")
/* 118 */     .number("(d+)")
/* 119 */     .compile();
/*     */   
/* 121 */   private static final Pattern PATTERN_OBDA = (new PatternBuilder())
/* 122 */     .number("(d+),")
/* 123 */     .number("(d+.d+),")
/* 124 */     .number("(d+.d+),")
/* 125 */     .number("(d+),")
/* 126 */     .number("(d+),")
/* 127 */     .number("(d+),")
/* 128 */     .number("(d+),")
/* 129 */     .number("(d+),")
/* 130 */     .number("(d+)")
/* 131 */     .compile();
/*     */   
/*     */   public static final int MSG_HEARTBEAT = 1;
/*     */   
/*     */   public static final int MSG_SERVER = 2;
/*     */   
/*     */   public static final int MSG_LOGIN = 20480;
/*     */   public static final int MSG_LOGIN_RESPONSE = 16384;
/*     */   public static final int MSG_POSITION = 39253;
/*     */   public static final int MSG_POSITION_LOGGED = 36886;
/*     */   public static final int MSG_ALARM = 39321;
/*     */   public static final int MSG_RFID = 39270;
/*     */   public static final int MSG_RETRANSMISSION = 26248;
/*     */   public static final int MSG_OBD_RT = 39169;
/*     */   public static final int MSG_OBD_RTA = 39170;
/*     */   public static final int MSG_TRACK_ON_DEMAND = 16641;
/*     */   public static final int MSG_TRACK_BY_INTERVAL = 16642;
/*     */   public static final int MSG_MOVEMENT_ALARM = 16646;
/*     */   public static final int MSG_OUTPUT_CONTROL = 16660;
/*     */   public static final int MSG_TIME_ZONE = 16690;
/*     */   public static final int MSG_TAKE_PHOTO = 16721;
/*     */   public static final int MSG_UPLOAD_PHOTO = 2048;
/*     */   public static final int MSG_UPLOAD_PHOTO_RESPONSE = 34817;
/*     */   public static final int MSG_DATA_PHOTO = 39304;
/*     */   public static final int MSG_POSITION_IMAGE = 39287;
/*     */   public static final int MSG_UPLOAD_COMPLETE = 3968;
/*     */   public static final int MSG_REBOOT_GPS = 18690;
/*     */   
/*     */   private DeviceSession identify(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {
/* 160 */     StringBuilder builder = new StringBuilder();
/*     */     
/* 162 */     for (int i = 0; i < 7; i++) {
/* 163 */       int b = buf.readUnsignedByte();
/*     */ 
/*     */       
/* 166 */       int d1 = (b & 0xF0) >> 4;
/* 167 */       if (d1 == 15) {
/*     */         break;
/*     */       }
/* 170 */       builder.append(d1);
/*     */ 
/*     */       
/* 173 */       int d2 = b & 0xF;
/* 174 */       if (d2 == 15) {
/*     */         break;
/*     */       }
/* 177 */       builder.append(d2);
/*     */     } 
/*     */     
/* 180 */     String id = builder.toString();
/*     */     
/* 182 */     if (id.length() == 14) {
/* 183 */       return getDeviceSession(channel, remoteAddress, new String[] { id, id + Checksum.luhn(Long.parseLong(id)) });
/*     */     }
/* 185 */     return getDeviceSession(channel, remoteAddress, new String[] { id });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void sendResponse(Channel channel, SocketAddress remoteAddress, ByteBuf id, int type, ByteBuf msg) {
/* 192 */     if (channel != null) {
/* 193 */       ByteBuf buf = Unpooled.buffer(4 + id
/* 194 */           .readableBytes() + 2 + msg.readableBytes() + 2 + 2);
/*     */       
/* 196 */       buf.writeByte(64);
/* 197 */       buf.writeByte(64);
/* 198 */       buf.writeShort(buf.capacity());
/* 199 */       buf.writeBytes(id);
/* 200 */       buf.writeShort(type);
/* 201 */       buf.writeBytes(msg);
/* 202 */       msg.release();
/* 203 */       buf.writeShort(Checksum.crc16(Checksum.CRC16_CCITT_FALSE, buf.nioBuffer()));
/* 204 */       buf.writeByte(13);
/* 205 */       buf.writeByte(10);
/*     */       
/* 207 */       channel.writeAndFlush(new NetworkMessage(buf, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String decodeAlarm(short value) {
/* 212 */     switch (value) {
/*     */       case 1:
/* 214 */         return "sos";
/*     */       case 3:
/* 216 */         return "powerOn";
/*     */       case 16:
/* 218 */         return "lowBattery";
/*     */       case 17:
/* 220 */         return "overspeed";
/*     */       case 18:
/* 222 */         return "movement";
/*     */       case 19:
/* 224 */         return "geofenceEnter";
/*     */       case 20:
/* 226 */         return "accident";
/*     */       case 21:
/* 228 */         return "enterBlindArea";
/*     */       case 22:
/* 230 */         return "exitBlindArea";
/*     */       case 23:
/* 232 */         return "lowPower";
/*     */       case 49:
/* 234 */         return "sosReleased";
/*     */       case 51:
/* 236 */         return "powerOff";
/*     */       case 80:
/* 238 */         return "powerCut";
/*     */       case 82:
/* 240 */         return "headingChange";
/*     */       case 83:
/* 242 */         return "gpsAntennaCut";
/*     */       case 84:
/* 244 */         return "powerRestored";
/*     */       case 86:
/* 246 */         return "tow";
/*     */       case 87:
/* 248 */         return "jamming";
/*     */       case 89:
/* 250 */         return "jammingLost";
/*     */       case 99:
/* 252 */         return "distanceInterval";
/*     */       case 100:
/* 254 */         return "fuelTheft";
/*     */       case 115:
/* 256 */         return "powerOff";
/*     */       case 116:
/* 258 */         return "rfidEvent";
/*     */       case 117:
/* 260 */         return "cardEvent";
/*     */       case 120:
/* 262 */         return "illegalDriving";
/*     */       case 128:
/* 264 */         return "hardAcceleration";
/*     */       case 129:
/* 266 */         return "hardBraking";
/*     */     } 
/* 268 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeRegular(Position position, String sentence) {
/* 273 */     Parser parser = new Parser(PATTERN, sentence);
/* 274 */     if (!parser.matches()) {
/* 275 */       return null;
/*     */     }
/*     */ 
/*     */     
/* 279 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 281 */     position.setValid(parser.next().equals("A"));
/* 282 */     position.setLatitude(parser.nextCoordinate());
/* 283 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 285 */     if (parser.hasNext()) {
/* 286 */       position.setSpeed(parser.nextDouble(0.0D));
/*     */     }
/*     */     
/* 289 */     if (parser.hasNext()) {
/* 290 */       position.setCourse(parser.nextDouble(0.0D));
/*     */     }
/*     */     
/* 293 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 294 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 296 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 298 */     if (parser.hasNext()) {
/* 299 */       position.setAltitude(parser.nextDouble(0.0D));
/*     */     }
/*     */     
/* 302 */     if (parser.hasNext()) {
/* 303 */       int status = parser.nextHexInt().intValue(); int j;
/* 304 */       for (j = 1; j <= 5; j++) {
/* 305 */         position.set("out" + j, Boolean.valueOf(BitUtil.check(status, j - 1)));
/*     */       }
/* 307 */       for (j = 1; j <= 5; j++) {
/* 308 */         position.set("in" + j, Boolean.valueOf(BitUtil.check(status, j - 1 + 8)));
/*     */       }
/*     */     } 
/*     */     
/* 312 */     for (int i = 1; i <= 8; i++) {
/* 313 */       position.set("adc" + i, parser.nextHexInt());
/*     */     }
/*     */     
/* 316 */     position.set("rssi", parser.nextHexInt());
/* 317 */     position.set("odometer", parser.nextHexLong());
/* 318 */     position.set("sat", parser.nextHexInt());
/* 319 */     position.set("card", parser.next());
/* 320 */     position.set("odometer", parser.nextLong());
/* 321 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 323 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeRfid(Position position, String sentence) {
/* 327 */     Parser parser = new Parser(PATTERN_RFID, sentence);
/* 328 */     if (!parser.matches()) {
/* 329 */       return null;
/*     */     }
/*     */     
/* 332 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 334 */     position.setValid(true);
/* 335 */     position.setLatitude(parser.nextCoordinate());
/* 336 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 338 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeObd(Position position, String sentence) {
/* 342 */     Parser parser = new Parser(PATTERN_OBD, sentence);
/* 343 */     if (!parser.matches()) {
/* 344 */       return null;
/*     */     }
/*     */     
/* 347 */     getLastLocation(position, null);
/*     */     
/* 349 */     position.set("battery", parser.nextDouble());
/* 350 */     position.set("rpm", parser.nextInt());
/* 351 */     position.set("obdSpeed", parser.nextInt());
/* 352 */     position.set("throttle", parser.nextDouble());
/* 353 */     position.set("engineLoad", parser.nextDouble());
/* 354 */     position.set("coolantTemp", parser.nextInt());
/* 355 */     position.set("fuelConsumption", parser.nextDouble());
/* 356 */     position.set("averageFuelConsumption", parser.nextDouble());
/* 357 */     position.set("drivingRange", parser.nextDouble());
/* 358 */     position.set("odometer", parser.nextDouble());
/* 359 */     position.set("singleFuelConsumption", parser.nextDouble());
/* 360 */     position.set("fuelUsed", parser.nextDouble());
/* 361 */     position.set("dtcs", parser.nextInt());
/* 362 */     position.set("hardAccelerationCount", parser.nextInt());
/* 363 */     position.set("hardBrakingCount", parser.nextInt());
/*     */     
/* 365 */     return position;
/*     */   }
/*     */   
/*     */   private Position decodeObdA(Position position, String sentence) {
/* 369 */     Parser parser = new Parser(PATTERN_OBDA, sentence);
/* 370 */     if (!parser.matches()) {
/* 371 */       return null;
/*     */     }
/*     */     
/* 374 */     getLastLocation(position, null);
/*     */     
/* 376 */     position.set("totalIgnitionNo", Integer.valueOf(parser.nextInt(0)));
/* 377 */     position.set("totalDrivingTime", Double.valueOf(parser.nextDouble(0.0D)));
/* 378 */     position.set("totalIdlingTime", Double.valueOf(parser.nextDouble(0.0D)));
/* 379 */     position.set("averageHotStartTime", Integer.valueOf(parser.nextInt(0)));
/* 380 */     position.set("averageSpeed", Integer.valueOf(parser.nextInt(0)));
/* 381 */     position.set("historyHighestSpeed", Integer.valueOf(parser.nextInt(0)));
/* 382 */     position.set("historyHighestRpm", Integer.valueOf(parser.nextInt(0)));
/* 383 */     position.set("totalHarshAccerleration", Integer.valueOf(parser.nextInt(0)));
/* 384 */     position.set("totalHarshBrake", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 386 */     return position;
/*     */   }
/*     */   
/*     */   private List<Position> decodeRetransmission(ByteBuf buf, DeviceSession deviceSession) {
/* 390 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 392 */     int count = buf.readUnsignedByte();
/* 393 */     for (int i = 0; i < count; i++) {
/*     */       
/* 395 */       buf.readUnsignedByte();
/*     */       
/* 397 */       int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)92);
/* 398 */       if (endIndex < 0) {
/* 399 */         endIndex = buf.writerIndex() - 4;
/*     */       }
/*     */       
/* 402 */       String sentence = buf.readSlice(endIndex - buf.readerIndex()).toString(StandardCharsets.US_ASCII);
/*     */       
/* 404 */       Position position = new Position(getProtocolName());
/* 405 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 407 */       position = decodeRegular(position, sentence);
/*     */       
/* 409 */       if (position != null) {
/* 410 */         positions.add(position);
/*     */       }
/*     */       
/* 413 */       if (buf.readableBytes() > 4) {
/* 414 */         buf.readUnsignedByte();
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 419 */     return positions;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 426 */     ByteBuf buf = (ByteBuf)msg;
/* 427 */     buf.skipBytes(2);
/* 428 */     buf.readShort();
/* 429 */     ByteBuf id = buf.readSlice(7);
/* 430 */     int command = buf.readUnsignedShort();
/*     */     
/* 432 */     if (command == 20480) {
/* 433 */       ByteBuf response = Unpooled.wrappedBuffer(new byte[] { 1 });
/* 434 */       sendResponse(channel, remoteAddress, id, 16384, response);
/* 435 */       return null;
/* 436 */     }  if (command == 1) {
/* 437 */       ByteBuf response = Unpooled.wrappedBuffer(new byte[] { 1 });
/* 438 */       sendResponse(channel, remoteAddress, id, 1, response);
/* 439 */       return null;
/* 440 */     }  if (command == 2) {
/* 441 */       ByteBuf response = Unpooled.copiedBuffer(getServer(channel, ':'), StandardCharsets.US_ASCII);
/* 442 */       sendResponse(channel, remoteAddress, id, 2, response);
/* 443 */       return null;
/* 444 */     }  if (command == 2048) {
/* 445 */       byte imageIndex = buf.readByte();
/* 446 */       this.photos.put(Byte.valueOf(imageIndex), Unpooled.buffer());
/* 447 */       ByteBuf response = Unpooled.copiedBuffer(new byte[] { imageIndex });
/* 448 */       sendResponse(channel, remoteAddress, id, 34817, response);
/* 449 */       return null;
/* 450 */     }  if (command == 3968) {
/* 451 */       byte imageIndex = buf.readByte();
/* 452 */       ByteBuf response = Unpooled.copiedBuffer(new byte[] { imageIndex, 0, 0 });
/* 453 */       sendResponse(channel, remoteAddress, id, 26248, response);
/* 454 */       return null;
/*     */     } 
/*     */     
/* 457 */     DeviceSession deviceSession = identify(id, channel, remoteAddress);
/* 458 */     if (deviceSession == null) {
/* 459 */       return null;
/*     */     }
/*     */     
/* 462 */     if (command == 39304) {
/*     */       
/* 464 */       byte imageIndex = buf.readByte();
/* 465 */       buf.readUnsignedShort();
/* 466 */       buf.readUnsignedByte();
/* 467 */       buf.readUnsignedByte();
/*     */       
/* 469 */       ((ByteBuf)this.photos.get(Byte.valueOf(imageIndex))).writeBytes(buf, buf.readableBytes() - 2 - 2);
/*     */       
/* 471 */       return null;
/*     */     } 
/* 473 */     if (command == 26248)
/*     */     {
/* 475 */       return decodeRetransmission(buf, deviceSession);
/*     */     }
/*     */ 
/*     */     
/* 479 */     Position position = new Position(getProtocolName());
/*     */     
/* 481 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 483 */     if (command == 39321) {
/* 484 */       short alarmCode = buf.readUnsignedByte();
/* 485 */       position.set("alarm", decodeAlarm(alarmCode));
/* 486 */       if (alarmCode >= 2 && alarmCode <= 5) {
/* 487 */         position.set("in" + alarmCode, Integer.valueOf(1));
/* 488 */       } else if (alarmCode >= 50 && alarmCode <= 53) {
/* 489 */         position.set("in" + (alarmCode - 48), Integer.valueOf(0));
/*     */       } 
/* 491 */     } else if (command == 36886) {
/* 492 */       buf.skipBytes(6);
/* 493 */     } else if (command == 39270) {
/* 494 */       for (int i = 0; i < 15; i++) {
/* 495 */         long rfid = buf.readUnsignedInt();
/* 496 */         if (rfid != 0L) {
/* 497 */           String card = String.format("%010d", new Object[] { Long.valueOf(rfid) });
/* 498 */           position.set("card" + (i + 1), card);
/* 499 */           position.set("driverUniqueId", card);
/*     */         } 
/*     */       } 
/* 502 */     } else if (command == 39287) {
/* 503 */       byte imageIndex = buf.readByte();
/* 504 */       buf.readUnsignedByte();
/* 505 */       String uniqueId = Context.getIdentityManager().getById(deviceSession.getDeviceId()).getUniqueId();
/* 506 */       ByteBuf photo = this.photos.remove(Byte.valueOf(imageIndex));
/*     */       try {
/* 508 */         position.set("image", Context.getMediaManager().writeFile(uniqueId, photo, "jpg"));
/*     */       } finally {
/* 510 */         photo.release();
/*     */       } 
/*     */     } 
/*     */     
/* 514 */     String sentence = buf.toString(buf.readerIndex(), buf.readableBytes() - 4, StandardCharsets.US_ASCII);
/*     */     
/* 516 */     switch (command) {
/*     */       case 36886:
/*     */       case 39253:
/*     */       case 39287:
/*     */       case 39321:
/* 521 */         return decodeRegular(position, sentence);
/*     */       case 39270:
/* 523 */         return decodeRfid(position, sentence);
/*     */       case 39169:
/* 525 */         return decodeObd(position, sentence);
/*     */       case 39170:
/* 527 */         return decodeObdA(position, sentence);
/*     */     } 
/* 529 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeiligaoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */