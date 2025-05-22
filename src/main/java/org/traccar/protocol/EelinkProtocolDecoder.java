/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class EelinkProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 1;
/*     */   public static final int MSG_GPS = 2;
/*     */   public static final int MSG_HEARTBEAT = 3;
/*     */   public static final int MSG_ALARM = 4;
/*     */   public static final int MSG_STATE = 5;
/*     */   public static final int MSG_SMS = 6;
/*     */   public static final int MSG_OBD = 7;
/*     */   public static final int MSG_DOWNLINK = 128;
/*     */   public static final int MSG_DATA = 129;
/*     */   
/*     */   public EelinkProtocolDecoder(Protocol protocol) {
/*  46 */     super(protocol);
/*     */   }
/*     */ 
/*     */   
/*     */   public static final int MSG_NORMAL = 18;
/*     */   
/*     */   public static final int MSG_WARNING = 20;
/*     */   
/*     */   public static final int MSG_REPORT = 21;
/*     */   
/*     */   public static final int MSG_COMMAND = 22;
/*     */   
/*     */   public static final int MSG_OBD_DATA = 23;
/*     */   
/*     */   public static final int MSG_OBD_BODY = 24;
/*     */   
/*     */   public static final int MSG_OBD_CODE = 25;
/*     */   
/*     */   public static final int MSG_CAMERA_INFO = 30;
/*     */   
/*     */   public static final int MSG_CAMERA_DATA = 31;
/*     */ 
/*     */   
/*     */   private String decodeAlarm(Short value) {
/*  70 */     switch (value.shortValue()) {
/*     */       case 1:
/*  72 */         return "powerOff";
/*     */       case 2:
/*  74 */         return "sos";
/*     */       case 3:
/*  76 */         return "lowBattery";
/*     */       case 4:
/*  78 */         return "vibration";
/*     */       case 8:
/*     */       case 9:
/*  81 */         return "gpsAntennaCut";
/*     */       case 37:
/*  83 */         return "removing";
/*     */       case 129:
/*  85 */         return "lowspeed";
/*     */       case 130:
/*  87 */         return "overspeed";
/*     */       case 131:
/*  89 */         return "geofenceEnter";
/*     */       case 132:
/*  91 */         return "geofenceExit";
/*     */       case 133:
/*  93 */         return "accident";
/*     */       case 134:
/*  95 */         return "fallDown";
/*     */     } 
/*  97 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeStatus(Position position, int status) {
/* 102 */     if (BitUtil.check(status, 1)) {
/* 103 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 2)));
/*     */     }
/* 105 */     if (BitUtil.check(status, 3)) {
/* 106 */       position.set("armed", Boolean.valueOf(BitUtil.check(status, 4)));
/*     */     }
/* 108 */     if (BitUtil.check(status, 5)) {
/* 109 */       position.set("blocked", Boolean.valueOf(!BitUtil.check(status, 6)));
/*     */     }
/* 111 */     if (BitUtil.check(status, 7)) {
/* 112 */       position.set("charge", Boolean.valueOf(BitUtil.check(status, 8)));
/*     */     }
/* 114 */     position.set("status", Integer.valueOf(status));
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeOld(DeviceSession deviceSession, ByteBuf buf, int type, int index) {
/* 119 */     Position position = new Position(getProtocolName());
/* 120 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 122 */     position.set("index", Integer.valueOf(index));
/*     */     
/* 124 */     position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/* 125 */     position.setLatitude(buf.readInt() / 1800000.0D);
/* 126 */     position.setLongitude(buf.readInt() / 1800000.0D);
/* 127 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/* 128 */     position.setCourse(buf.readUnsignedShort());
/*     */     
/* 130 */     position.setNetwork(new Network(CellTower.from(buf
/* 131 */             .readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedMedium())));
/*     */     
/* 133 */     position.setValid(((buf.readUnsignedByte() & 0x1) != 0));
/*     */     
/* 135 */     if (type == 2) {
/*     */       
/* 137 */       if (buf.readableBytes() >= 2) {
/* 138 */         decodeStatus(position, buf.readUnsignedShort());
/*     */       }
/*     */       
/* 141 */       if (buf.readableBytes() >= 8)
/*     */       {
/* 143 */         position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */         
/* 145 */         position.set("rssi", Integer.valueOf(buf.readUnsignedShort()));
/*     */         
/* 147 */         position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
/* 148 */         position.set("adc2", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */     
/*     */     }
/* 152 */     else if (type == 4) {
/*     */       
/* 154 */       position.set("alarm", decodeAlarm(Short.valueOf(buf.readUnsignedByte())));
/*     */     }
/* 156 */     else if (type == 5) {
/*     */       
/* 158 */       int statusType = buf.readUnsignedByte();
/*     */       
/* 160 */       position.set("event", Integer.valueOf(statusType));
/*     */       
/* 162 */       if (statusType == 1 || statusType == 2 || statusType == 3) {
/* 163 */         buf.readUnsignedInt();
/* 164 */         if (buf.readableBytes() >= 2) {
/* 165 */           decodeStatus(position, buf.readUnsignedShort());
/*     */         }
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 171 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeNew(DeviceSession deviceSession, ByteBuf buf, int type, int index) {
/* 176 */     Position position = new Position(getProtocolName());
/* 177 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 179 */     position.set("index", Integer.valueOf(index));
/*     */     
/* 181 */     position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */     
/* 183 */     int flags = buf.readUnsignedByte();
/*     */     
/* 185 */     if (BitUtil.check(flags, 0)) {
/* 186 */       position.setLatitude(buf.readInt() / 1800000.0D);
/* 187 */       position.setLongitude(buf.readInt() / 1800000.0D);
/* 188 */       position.setAltitude(buf.readShort());
/* 189 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/* 190 */       position.setCourse(buf.readUnsignedShort());
/* 191 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     } else {
/* 193 */       getLastLocation(position, position.getDeviceTime());
/*     */     } 
/*     */     
/* 196 */     Network network = new Network();
/*     */     
/* 198 */     int mcc = 0;
/* 199 */     int mnc = 0;
/* 200 */     if (BitUtil.check(flags, 1)) {
/* 201 */       mcc = buf.readUnsignedShort();
/* 202 */       mnc = buf.readUnsignedShort();
/* 203 */       network.addCellTower(CellTower.from(mcc, mnc, buf
/* 204 */             .readUnsignedShort(), buf.readUnsignedInt(), buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 207 */     if (BitUtil.check(flags, 2)) {
/* 208 */       network.addCellTower(CellTower.from(mcc, mnc, buf
/* 209 */             .readUnsignedShort(), buf.readUnsignedInt(), buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 212 */     if (BitUtil.check(flags, 3)) {
/* 213 */       network.addCellTower(CellTower.from(mcc, mnc, buf
/* 214 */             .readUnsignedShort(), buf.readUnsignedInt(), buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 217 */     if (BitUtil.check(flags, 4)) {
/* 218 */       String mac = ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/* 219 */       network.addWifiAccessPoint(WifiAccessPoint.from(mac
/* 220 */             .substring(0, mac.length() - 1), buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 223 */     if (BitUtil.check(flags, 5)) {
/* 224 */       String mac = ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/* 225 */       network.addWifiAccessPoint(WifiAccessPoint.from(mac
/* 226 */             .substring(0, mac.length() - 1), buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 229 */     if (BitUtil.check(flags, 6)) {
/* 230 */       String mac = ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/* 231 */       network.addWifiAccessPoint(WifiAccessPoint.from(mac
/* 232 */             .substring(0, mac.length() - 1), buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 235 */     if (BitUtil.check(flags, 7)) {
/* 236 */       buf.readUnsignedByte();
/* 237 */       int count = buf.readUnsignedByte();
/* 238 */       int lac = 0;
/* 239 */       if (count > 0) {
/* 240 */         mcc = buf.readUnsignedShort();
/* 241 */         mnc = buf.readUnsignedShort();
/* 242 */         lac = buf.readUnsignedShort();
/* 243 */         buf.readUnsignedShort();
/* 244 */         buf.readUnsignedInt();
/* 245 */         buf.readUnsignedShort();
/*     */       } 
/* 247 */       for (int i = 0; i < count; i++) {
/* 248 */         int cid = buf.readUnsignedShort();
/* 249 */         buf.readUnsignedShort();
/* 250 */         int rssi = buf.readUnsignedByte();
/* 251 */         network.addCellTower(CellTower.from(mcc, mnc, lac, cid, rssi));
/*     */       } 
/*     */     } 
/*     */     
/* 255 */     if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 256 */       position.setNetwork(network);
/*     */     }
/*     */     
/* 259 */     if (type == 20) {
/*     */       
/* 261 */       position.set("alarm", decodeAlarm(Short.valueOf(buf.readUnsignedByte())));
/*     */     }
/* 263 */     else if (type == 21) {
/*     */       
/* 265 */       buf.readUnsignedByte();
/*     */     } 
/*     */ 
/*     */     
/* 269 */     if (type == 18 || type == 20 || type == 21) {
/*     */       
/* 271 */       int status = buf.readUnsignedShort();
/* 272 */       position.setValid(BitUtil.check(status, 0));
/* 273 */       if (BitUtil.check(status, 1)) {
/* 274 */         position.set("ignition", Boolean.valueOf(BitUtil.check(status, 2)));
/*     */       }
/* 276 */       position.set("status", Integer.valueOf(status));
/*     */     } 
/*     */ 
/*     */     
/* 280 */     if (type == 18) {
/*     */       
/* 282 */       if (buf.readableBytes() >= 2) {
/* 283 */         position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       }
/*     */       
/* 286 */       if (buf.readableBytes() >= 4) {
/* 287 */         position.set("adc0", Integer.valueOf(buf.readUnsignedShort()));
/* 288 */         position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
/*     */       } 
/*     */       
/* 291 */       if (buf.readableBytes() >= 4) {
/* 292 */         position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       }
/*     */       
/* 295 */       if (buf.readableBytes() >= 4) {
/* 296 */         buf.readUnsignedShort();
/* 297 */         buf.readUnsignedShort();
/*     */       } 
/*     */       
/* 300 */       if (buf.readableBytes() >= 4) {
/* 301 */         position.set("steps", Integer.valueOf(buf.readUnsignedShort()));
/* 302 */         buf.readUnsignedShort();
/*     */       } 
/*     */       
/* 305 */       if (buf.readableBytes() >= 12) {
/* 306 */         position.set("temp1", Double.valueOf(buf.readShort() / 256.0D));
/* 307 */         position.set("humidity", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/* 308 */         position.set("illuminance", Double.valueOf(buf.readUnsignedInt() / 256.0D));
/* 309 */         position.set("co2", Long.valueOf(buf.readUnsignedInt()));
/*     */       } 
/*     */       
/* 312 */       if (buf.readableBytes() >= 2) {
/* 313 */         position.set("temp2", Double.valueOf(buf.readShort() / 16.0D));
/*     */       }
/*     */       
/* 316 */       if (buf.readableBytes() >= 2) {
/* 317 */         int count = buf.readUnsignedByte();
/* 318 */         buf.readUnsignedByte();
/* 319 */         for (int i = 1; i <= count; i++) {
/* 320 */           position.set("tag" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
/* 321 */           buf.readUnsignedByte();
/* 322 */           buf.readUnsignedByte();
/* 323 */           buf.readUnsignedByte();
/* 324 */           buf.readUnsignedByte();
/* 325 */           position.set("tag" + i + "Battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/* 326 */           position.set("tag" + i + "Temp", Double.valueOf(buf.readShort() / 256.0D));
/* 327 */           position.set("tag" + i + "Data", Integer.valueOf(buf.readUnsignedShort()));
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 334 */     return position;
/*     */   }
/*     */   
/* 337 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 338 */     .text("Lat:")
/* 339 */     .number("([NS])(d+.d+)")
/* 340 */     .any()
/* 341 */     .text("Lon:")
/* 342 */     .number("([EW])(d+.d+)")
/* 343 */     .any()
/* 344 */     .text("Course:")
/* 345 */     .number("(d+.d+)")
/* 346 */     .any()
/* 347 */     .text("Speed:")
/* 348 */     .number("(d+.d+)")
/* 349 */     .any()
/* 350 */     .expression("Date ?Time:")
/* 351 */     .number("(dddd)-(dd)-(dd) ")
/* 352 */     .number("(dd):(dd):(dd)")
/* 353 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeResult(DeviceSession deviceSession, ByteBuf buf, int index) {
/* 357 */     Position position = new Position(getProtocolName());
/* 358 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 360 */     position.set("index", Integer.valueOf(index));
/*     */     
/* 362 */     buf.readUnsignedByte();
/* 363 */     buf.readUnsignedInt();
/*     */     
/* 365 */     String sentence = buf.toString(StandardCharsets.UTF_8);
/*     */     
/* 367 */     Parser parser = new Parser(PATTERN, sentence);
/* 368 */     if (parser.matches()) {
/*     */       
/* 370 */       position.setValid(true);
/* 371 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 372 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 373 */       position.setCourse(parser.nextDouble().doubleValue());
/* 374 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 375 */       position.setTime(parser.nextDateTime());
/*     */     }
/*     */     else {
/*     */       
/* 379 */       getLastLocation(position, null);
/*     */       
/* 381 */       position.set("result", sentence);
/*     */     } 
/*     */ 
/*     */     
/* 385 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeObd(DeviceSession deviceSession, ByteBuf buf) {
/* 390 */     Position position = new Position(getProtocolName());
/* 391 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 393 */     getLastLocation(position, new Date(buf.readUnsignedInt() * 1000L));
/*     */     
/* 395 */     while (buf.readableBytes() > 0) {
/* 396 */       int pid = buf.readUnsignedByte();
/* 397 */       int value = buf.readInt();
/* 398 */       switch (pid) {
/*     */         case 137:
/* 400 */           position.set("fuelConsumption", Integer.valueOf(value));
/*     */         
/*     */         case 138:
/* 403 */           position.set("odometer", Long.valueOf(value * 1000L));
/*     */         
/*     */         case 139:
/* 406 */           position.set("fuel", Integer.valueOf(value / 10));
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 413 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     DeviceSession deviceSession;
/* 420 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 422 */     String uniqueId = null;
/*     */ 
/*     */     
/* 425 */     if (buf.getByte(0) == 69 && buf.getByte(1) == 76) {
/* 426 */       buf.skipBytes(6);
/* 427 */       uniqueId = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/* 428 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { uniqueId });
/*     */     } else {
/* 430 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */     } 
/*     */     
/* 433 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 435 */     while (buf.isReadable()) {
/* 436 */       Position position = decodePackage(channel, remoteAddress, buf, uniqueId, deviceSession);
/* 437 */       if (position != null) {
/* 438 */         positions.add(position);
/*     */       }
/*     */     } 
/*     */     
/* 442 */     if (!positions.isEmpty()) {
/* 443 */       return (positions.size() > 1) ? positions : positions.iterator().next();
/*     */     }
/* 445 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Position decodePackage(Channel channel, SocketAddress remoteAddress, ByteBuf buf, String uniqueId, DeviceSession deviceSession) throws Exception {
/* 453 */     buf.skipBytes(2);
/* 454 */     int type = buf.readUnsignedByte();
/* 455 */     buf = buf.readSlice(buf.readUnsignedShort());
/* 456 */     int index = buf.readUnsignedShort();
/*     */     
/* 458 */     if (type != 2 && type != 129) {
/* 459 */       ByteBuf content = Unpooled.buffer();
/* 460 */       if (type == 1) {
/* 461 */         content.writeInt((int)(System.currentTimeMillis() / 1000L));
/* 462 */         content.writeByte(1);
/* 463 */         content.writeByte(0);
/*     */       } 
/* 465 */       ByteBuf response = EelinkProtocolEncoder.encodeContent(channel instanceof io.netty.channel.socket.DatagramChannel, uniqueId, type, index, content);
/*     */       
/* 467 */       content.release();
/* 468 */       if (channel != null) {
/* 469 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       }
/*     */     } 
/*     */     
/* 473 */     if (type == 1) {
/*     */       
/* 475 */       if (deviceSession == null) {
/* 476 */         getDeviceSession(channel, remoteAddress, new String[] { ByteBufUtil.hexDump(buf.readSlice(8)).substring(1) });
/*     */       }
/*     */     }
/*     */     else {
/*     */       
/* 481 */       if (deviceSession == null) {
/* 482 */         return null;
/*     */       }
/*     */       
/* 485 */       if (type == 2 || type == 4 || type == 5 || type == 6)
/*     */       {
/* 487 */         return decodeOld(deviceSession, buf, type, index);
/*     */       }
/* 489 */       if (type >= 18 && type <= 25)
/*     */       {
/* 491 */         return decodeNew(deviceSession, buf, type, index);
/*     */       }
/* 493 */       if ((type == 3 && buf.readableBytes() >= 2) || (type == 7 && buf
/* 494 */         .readableBytes() == 4)) {
/*     */         
/* 496 */         Position position = new Position(getProtocolName());
/* 497 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 499 */         getLastLocation(position, null);
/*     */         
/* 501 */         decodeStatus(position, buf.readUnsignedShort());
/*     */         
/* 503 */         return position;
/*     */       } 
/* 505 */       if (type == 7)
/*     */       {
/* 507 */         return decodeObd(deviceSession, buf);
/*     */       }
/* 509 */       if (type == 128)
/*     */       {
/* 511 */         return decodeResult(deviceSession, buf, index);
/*     */       }
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 517 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EelinkProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */