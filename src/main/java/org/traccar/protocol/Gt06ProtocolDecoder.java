/*      */ package org.traccar.protocol;
/*      */ 
/*      */ import io.netty.buffer.ByteBuf;
/*      */ import io.netty.buffer.ByteBufUtil;
/*      */ import io.netty.buffer.Unpooled;
/*      */ import io.netty.channel.Channel;
/*      */ import java.net.SocketAddress;
/*      */ import java.nio.charset.StandardCharsets;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Map;
/*      */ import java.util.TimeZone;
/*      */ import java.util.regex.Pattern;
/*      */ import org.traccar.BaseProtocolDecoder;
/*      */ import org.traccar.Context;
/*      */ import org.traccar.DeviceSession;
/*      */ import org.traccar.NetworkMessage;
/*      */ import org.traccar.Protocol;
/*      */ import org.traccar.handler.TimeHandler;
/*      */ import org.traccar.helper.BcdUtil;
/*      */ import org.traccar.helper.BitUtil;
/*      */ import org.traccar.helper.Checksum;
/*      */ import org.traccar.helper.DateBuilder;
/*      */ import org.traccar.helper.Parser;
/*      */ import org.traccar.helper.PatternBuilder;
/*      */ import org.traccar.helper.UnitsConverter;
/*      */ import org.traccar.model.CellTower;
/*      */ import org.traccar.model.Device;
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
/*      */ public class Gt06ProtocolDecoder
/*      */   extends BaseProtocolDecoder
/*      */ {
/*      */   private final boolean adjustTime;
/*      */   private final boolean additionalAlarms;
/*      */   private final boolean noAck;
/*   56 */   private final Map<Integer, ByteBuf> photos = new HashMap<>(); public static final int MSG_LOGIN = 1; public static final int MSG_GPS = 16; public static final int MSG_GPS_LBS_6 = 17; public static final int MSG_GPS_LBS_1 = 18; public static final int MSG_GPS_LBS_2 = 34; public static final int MSG_GPS_LBS_3 = 55; public static final int MSG_GPS_LBS_4 = 45; public static final int MSG_STATUS = 19; public static final int MSG_SATELLITE = 20; public static final int MSG_STRING = 21; public static final int MSG_GPS_LBS_STATUS_1 = 22; public static final int MSG_WIFI = 23; public static final int MSG_GPS_LBS_RFID = 23; public static final int MSG_GPS_LBS_STATUS_2 = 38; public static final int MSG_GPS_LBS_STATUS_3 = 39; public static final int MSG_LBS_MULTIPLE_1 = 40; public static final int MSG_LBS_MULTIPLE_2 = 46; public static final int MSG_LBS_MULTIPLE_3 = 36; public static final int MSG_LBS_WIFI = 44; public static final int MSG_LBS_EXTEND = 24; public static final int MSG_LBS_STATUS = 25; public static final int MSG_GPS_PHONE = 26; public static final int MSG_GPS_LBS_EXTEND = 30; public static final int MSG_HEARTBEAT = 35; public static final int MSG_ADDRESS_REQUEST = 42; public static final int MSG_ADDRESS_RESPONSE = 151; public static final int MSG_GPS_LBS_5 = 49; public static final int MSG_GPS_LBS_STATUS_4 = 50; public static final int MSG_WIFI_5 = 51;
/*      */   
/*      */   private Date processTime(Date original) {
/*   59 */     return processTime(original, this.adjustTime);
/*      */   }
/*      */   public static final int MSG_AZ735_GPS = 50; public static final int MSG_AZ735_ALARM = 51; public static final int MSG_X1_GPS = 52; public static final int MSG_X1_PHOTO_INFO = 53; public static final int MSG_X1_PHOTO_DATA = 54; public static final int MSG_WIFI_2 = 105; public static final int MSG_GPS_MODULAR = 112; public static final int MSG_WIFI_4 = 243; public static final int MSG_COMMAND_0 = 128; public static final int MSG_COMMAND_1 = 129; public static final int MSG_COMMAND_2 = 130; public static final int MSG_TIME_REQUEST = 138; public static final int MSG_INFO = 148; public static final int MSG_SERIAL = 155; public static final int MSG_STRING_INFO = 33; public static final int MSG_GPS_2 = 160; public static final int MSG_LBS_2 = 161; public static final int MSG_WIFI_3 = 162; public static final int MSG_FENCE_SINGLE = 163; public static final int MSG_FENCE_MULTI = 164; public static final int MSG_LBS_ALARM = 165; public static final int MSG_LBS_ADDRESS = 167; public static final int MSG_OBD = 140; public static final int MSG_DTC = 101; public static final int MSG_PID = 102; public static final int MSG_BMS = 64; public static final int MSG_MULTIMEDIA = 65; public static final int MSG_ALARM = 149; private Variant variant;
/*      */   private static Date processTime(Date original, boolean adjustTime) {
/*   63 */     if (adjustTime && TimeHandler.aboveThreshold(original)) {
/*   64 */       return new Date(original.getTime() + 619315200000L);
/*      */     }
/*   66 */     return original;
/*      */   }
/*      */ 
/*      */   
/*      */   public Gt06ProtocolDecoder(Protocol protocol, boolean adjustTime, boolean additionalAlarms) {
/*   71 */     super(protocol);
/*   72 */     this.adjustTime = adjustTime;
/*   73 */     this.additionalAlarms = additionalAlarms;
/*   74 */     this.noAck = Context.getConfig().getBoolean(getProtocolName() + ".noAck");
/*      */   }
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
/*      */   
/*      */   private enum Variant
/*      */   {
/*  136 */     VXT01,
/*  137 */     WANWAY_S20,
/*  138 */     GT06E_CARD,
/*  139 */     BENWAY,
/*  140 */     S5,
/*  141 */     SPACE10X,
/*  142 */     STANDARD,
/*  143 */     OBD6,
/*  144 */     WETRUST,
/*  145 */     JC400,
/*  146 */     SL4X,
/*  147 */     SEEWORLD,
/*  148 */     RFID;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*  153 */   private static final Pattern PATTERN_FUEL = (new PatternBuilder())
/*  154 */     .text("!AIOIL,")
/*  155 */     .number("d+,")
/*  156 */     .number("d+.d+,")
/*  157 */     .number("(d+.d+),")
/*  158 */     .expression("[^,]+,")
/*  159 */     .number("dd")
/*  160 */     .number("d")
/*  161 */     .number("d,")
/*  162 */     .number("(d+.d+),")
/*  163 */     .expression("[01],")
/*  164 */     .number("d+,")
/*  165 */     .number("xx")
/*  166 */     .compile();
/*      */   
/*  168 */   private static final Pattern PATTERN_LOCATION = (new PatternBuilder())
/*  169 */     .text("Current position!")
/*  170 */     .number("Lat:([NS])(d+.d+),")
/*  171 */     .number("Lon:([EW])(d+.d+),")
/*  172 */     .text("Course:").number("(d+.d+),")
/*  173 */     .text("Speed:").number("(d+.d+),")
/*  174 */     .text("DateTime:")
/*  175 */     .number("(dddd)-(dd)-(dd) +")
/*  176 */     .number("(dd):(dd):(dd)")
/*  177 */     .compile();
/*      */   
/*      */   private static boolean isSupported(int type) {
/*  180 */     return (hasGps(type) || hasLbs(type) || hasStatus(type));
/*      */   }
/*      */   
/*      */   private static boolean hasGps(int type) {
/*  184 */     switch (type) {
/*      */       case 16:
/*      */       case 17:
/*      */       case 18:
/*      */       case 22:
/*      */       case 23:
/*      */       case 26:
/*      */       case 30:
/*      */       case 34:
/*      */       case 38:
/*      */       case 39:
/*      */       case 45:
/*      */       case 49:
/*      */       case 50:
/*      */       case 55:
/*      */       case 160:
/*      */       case 163:
/*      */       case 164:
/*  202 */         return true;
/*      */     } 
/*  204 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private static boolean hasLbs(int type) {
/*  209 */     switch (type) {
/*      */       case 17:
/*      */       case 18:
/*      */       case 22:
/*      */       case 23:
/*      */       case 25:
/*      */       case 34:
/*      */       case 38:
/*      */       case 39:
/*      */       case 45:
/*      */       case 49:
/*      */       case 50:
/*      */       case 55:
/*      */       case 160:
/*      */       case 163:
/*      */       case 164:
/*      */       case 165:
/*      */       case 167:
/*  227 */         return true;
/*      */     } 
/*  229 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private static boolean hasStatus(int type) {
/*  234 */     switch (type) {
/*      */       case 19:
/*      */       case 22:
/*      */       case 25:
/*      */       case 38:
/*      */       case 39:
/*      */       case 50:
/*      */       case 164:
/*  242 */         return true;
/*      */     } 
/*  244 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private static boolean hasLanguage(int type) {
/*  249 */     switch (type) {
/*      */       case 26:
/*      */       case 35:
/*      */       case 39:
/*      */       case 40:
/*      */       case 46:
/*      */       case 161:
/*      */       case 164:
/*  257 */         return true;
/*      */     } 
/*  259 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private void sendResponse(Channel channel, boolean extended, int type, int index, ByteBuf content) {
/*  264 */     if (channel != null) {
/*  265 */       ByteBuf response = Unpooled.buffer();
/*  266 */       int length = 5 + ((content != null) ? content.readableBytes() : 0);
/*  267 */       if (extended) {
/*  268 */         response.writeShort(31097);
/*  269 */         response.writeShort(length);
/*      */       } else {
/*  271 */         response.writeShort(30840);
/*  272 */         response.writeByte(length);
/*      */       } 
/*  274 */       response.writeByte(type);
/*  275 */       if (content != null) {
/*  276 */         response.writeBytes(content);
/*  277 */         content.release();
/*      */       } 
/*  279 */       response.writeShort(index);
/*  280 */       response.writeShort(Checksum.crc16(Checksum.CRC16_X25, response
/*  281 */             .nioBuffer(2, response.writerIndex() - 2)));
/*  282 */       response.writeByte(13);
/*  283 */       response.writeByte(10);
/*  284 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*      */     } 
/*      */   }
/*      */   
/*      */   private void sendPhotoRequest(Channel channel, int pictureId) {
/*  289 */     ByteBuf photo = this.photos.get(Integer.valueOf(pictureId));
/*  290 */     ByteBuf content = Unpooled.buffer();
/*  291 */     content.writeInt(pictureId);
/*  292 */     content.writeInt(photo.writerIndex());
/*  293 */     content.writeShort(Math.min(photo.writableBytes(), 1024));
/*  294 */     sendResponse(channel, false, 54, 0, content);
/*      */   }
/*      */ 
/*      */   
/*      */   public static boolean decodeGps(Position position, ByteBuf buf, boolean hasLength, TimeZone timezone, boolean adjustTime) {
/*  299 */     return decodeGps(position, buf, hasLength, true, true, timezone, adjustTime);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public static boolean decodeGps(Position position, ByteBuf buf, boolean hasLength, boolean hasSatellites, boolean hasSpeed, TimeZone timezone, boolean adjustTime) {
/*  308 */     DateBuilder dateBuilder = (new DateBuilder(timezone)).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  309 */     position.setTime(processTime(dateBuilder.getDate(), adjustTime));
/*      */     
/*  311 */     if (hasLength && buf.readUnsignedByte() == 0) {
/*  312 */       return false;
/*      */     }
/*      */     
/*  315 */     if (hasSatellites) {
/*  316 */       position.set("sat", Integer.valueOf(BitUtil.to(buf.readUnsignedByte(), 4)));
/*      */     }
/*      */     
/*  319 */     double latitude = buf.readUnsignedInt() / 60.0D / 30000.0D;
/*  320 */     double longitude = buf.readUnsignedInt() / 60.0D / 30000.0D;
/*      */     
/*  322 */     if (hasSpeed) {
/*  323 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*      */     }
/*      */     
/*  326 */     int flags = buf.readUnsignedShort();
/*  327 */     position.setCourse(BitUtil.to(flags, 10));
/*  328 */     position.setValid(BitUtil.check(flags, 12));
/*      */     
/*  330 */     if (!BitUtil.check(flags, 10)) {
/*  331 */       latitude = -latitude;
/*      */     }
/*  333 */     if (BitUtil.check(flags, 11)) {
/*  334 */       longitude = -longitude;
/*      */     }
/*      */     
/*  337 */     position.setLatitude(latitude);
/*  338 */     position.setLongitude(longitude);
/*      */     
/*  340 */     if (BitUtil.check(flags, 14)) {
/*  341 */       position.set("ignition", Boolean.valueOf(BitUtil.check(flags, 15)));
/*      */     }
/*      */     
/*  344 */     return true;
/*      */   }
/*      */   private boolean decodeLbs(Position position, ByteBuf buf, int type, boolean hasLength) {
/*      */     int mnc, lac;
/*      */     long cid;
/*  349 */     int length = 0;
/*  350 */     if (hasLength) {
/*  351 */       length = buf.readUnsignedByte();
/*  352 */       if (length == 0) {
/*  353 */         boolean zeroedData = true;
/*  354 */         for (int i = buf.readerIndex() + 9; i < buf.readerIndex() + 45 && i < buf.writerIndex(); i++) {
/*  355 */           if (buf.getByte(i) != 0) {
/*  356 */             zeroedData = false;
/*      */             break;
/*      */           } 
/*      */         } 
/*  360 */         if (zeroedData) {
/*  361 */           buf.skipBytes(Math.min(buf.readableBytes(), 45));
/*      */         }
/*  363 */         return false;
/*      */       } 
/*      */     } 
/*      */     
/*  367 */     int mcc = buf.readUnsignedShort();
/*      */     
/*  369 */     if (BitUtil.check(mcc, 15) || type == 17 || this.variant == Variant.SL4X) {
/*  370 */       mnc = buf.readUnsignedShort();
/*      */     } else {
/*  372 */       mnc = buf.readUnsignedByte();
/*      */     } 
/*      */     
/*  375 */     if (type == 165) {
/*  376 */       lac = buf.readInt();
/*      */     } else {
/*  378 */       lac = buf.readUnsignedShort();
/*      */     } 
/*      */     
/*  381 */     if (type == 165) {
/*  382 */       cid = buf.readLong();
/*  383 */     } else if (type == 17 || this.variant == Variant.SEEWORLD) {
/*  384 */       cid = buf.readUnsignedInt();
/*      */     } else {
/*  386 */       cid = buf.readUnsignedMedium();
/*      */     } 
/*      */     
/*  389 */     position.setNetwork(new Network(CellTower.from(BitUtil.to(mcc, 15), mnc, lac, cid)));
/*      */     
/*  391 */     if (length > 9) {
/*  392 */       buf.skipBytes(length - 9);
/*      */     }
/*      */     
/*  395 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   private void decodeStatus(Position position, ByteBuf buf, int type) {
/*  400 */     int status = buf.readUnsignedByte();
/*      */     
/*  402 */     position.set("status", Integer.valueOf(status));
/*  403 */     if (type != 19 || !Context.getConfig().getBoolean(getProtocolName() + ".ignoreStatusIgnition")) {
/*  404 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 1)));
/*      */     }
/*  406 */     position.set("charge", Boolean.valueOf(BitUtil.check(status, 2)));
/*  407 */     position.set("blocked", Boolean.valueOf(BitUtil.check(status, 7)));
/*      */     
/*  409 */     switch (BitUtil.between(status, 3, 6)) {
/*      */       case 1:
/*  411 */         position.set("alarm", "vibration");
/*      */         break;
/*      */       case 2:
/*  414 */         position.set("alarm", "powerCut");
/*      */         break;
/*      */       case 3:
/*  417 */         position.set("alarm", "lowBattery");
/*      */         break;
/*      */       case 4:
/*  420 */         position.set("alarm", "sos");
/*      */         break;
/*      */       case 6:
/*  423 */         position.set("alarm", "geofence");
/*      */         break;
/*      */       case 7:
/*  426 */         if (this.variant == Variant.VXT01) {
/*  427 */           position.set("alarm", "overspeed"); break;
/*      */         } 
/*  429 */         position.set("alarm", "removing");
/*      */         break;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private String decodeAlarmAlt(short value) {
/*  438 */     switch (value) {
/*      */       case 2:
/*  440 */         return "accident";
/*      */       case 4:
/*  442 */         return "fallDown";
/*      */       case 5:
/*  444 */         return "door";
/*      */       case 6:
/*  446 */         return "weakSignal";
/*      */       case 7:
/*  448 */         return "restoreSignal";
/*      */       case 9:
/*  450 */         return "hardAcceleration";
/*      */       case 10:
/*  452 */         return "hardBraking";
/*      */       case 11:
/*      */       case 12:
/*  455 */         return "hardCornering";
/*      */       case 13:
/*  457 */         return "illegalMove";
/*      */       case 30:
/*  459 */         return "resetAlert";
/*      */     } 
/*  461 */     return null;
/*      */   }
/*      */ 
/*      */   
/*      */   private String decodeAlarm(short value) {
/*  466 */     switch (value) {
/*      */       case 1:
/*  468 */         return "sos";
/*      */       case 2:
/*  470 */         return "powerCut";
/*      */       case 3:
/*  472 */         return "vibration";
/*      */       case 4:
/*  474 */         return "geofenceEnter";
/*      */       case 5:
/*  476 */         return "geofenceExit";
/*      */       case 6:
/*  478 */         return "overspeed";
/*      */       case 9:
/*  480 */         return "tow";
/*      */       case 10:
/*  482 */         return "enterBlindSpot";
/*      */       case 11:
/*  484 */         return "exitBlindSpot";
/*      */       case 12:
/*  486 */         return "tampering";
/*      */       case 13:
/*  488 */         return "fixFix";
/*      */       case 14:
/*      */       case 25:
/*  491 */         return "lowBattery";
/*      */       case 15:
/*  493 */         return "lowPower";
/*      */       case 16:
/*  495 */         return "simChanged";
/*      */       case 17:
/*  497 */         return "powerOff";
/*      */       case 18:
/*  499 */         return "airplaneMode";
/*      */       case 19:
/*      */       case 24:
/*      */       case 37:
/*  503 */         return "tampering";
/*      */       case 20:
/*  505 */         return "door";
/*      */       case 21:
/*  507 */         return "lowBatteryPowerOff";
/*      */       case 22:
/*  509 */         return "soundControl";
/*      */       case 23:
/*  511 */         return "rogueBaseStation";
/*      */       case 30:
/*  513 */         return "resetAlert";
/*      */       case 32:
/*  515 */         return "deepSleep";
/*      */       case 35:
/*  517 */         return "fallDown";
/*      */       case 40:
/*  519 */         return "hardBraking";
/*      */       case 41:
/*  521 */         return "hardAcceleration";
/*      */       case 42:
/*      */       case 43:
/*      */       case 46:
/*  525 */         return "hardCornering";
/*      */       case 44:
/*  527 */         return "accident";
/*      */       case 48:
/*  529 */         return "jamming";
/*      */       case 254:
/*  531 */         return "accOn";
/*      */       case 255:
/*  533 */         return "accOff";
/*      */     } 
/*  535 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private Object decodeBasic(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/*  541 */     int length = buf.readUnsignedByte();
/*  542 */     int dataLength = length - 5;
/*  543 */     int type = buf.readUnsignedByte();
/*      */     
/*  545 */     Position position = new Position(getProtocolName());
/*  546 */     DeviceSession deviceSession = null;
/*  547 */     if (type != 1) {
/*  548 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  549 */       if (deviceSession == null) {
/*  550 */         return null;
/*      */       }
/*  552 */       position.setDeviceId(deviceSession.getDeviceId());
/*  553 */       if (deviceSession.getTimeZone() == null) {
/*  554 */         deviceSession.setTimeZone(getTimeZone(deviceSession.getDeviceId()));
/*      */       }
/*      */     } 
/*      */     
/*  558 */     if (type == 1) {
/*      */       
/*  560 */       String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*  561 */       buf.readUnsignedShort();
/*      */       
/*  563 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  564 */       if (deviceSession != null && deviceSession.getTimeZone() == null) {
/*  565 */         deviceSession.setTimeZone(getTimeZone(deviceSession.getDeviceId()));
/*      */       }
/*      */       
/*  568 */       if (dataLength > 10) {
/*  569 */         int extensionBits = buf.readUnsignedShort();
/*  570 */         int hours = (extensionBits >> 4) / 100;
/*  571 */         int minutes = (extensionBits >> 4) % 100;
/*  572 */         int offset = (hours * 60 + minutes) * 60;
/*  573 */         if ((extensionBits & 0x8) != 0) {
/*  574 */           offset = -offset;
/*      */         }
/*  576 */         if (deviceSession != null) {
/*  577 */           TimeZone timeZone = deviceSession.getTimeZone();
/*  578 */           if (timeZone.getRawOffset() == 0) {
/*  579 */             timeZone.setRawOffset(offset * 1000);
/*  580 */             deviceSession.setTimeZone(timeZone);
/*      */           } 
/*      */         } 
/*      */       } 
/*      */       
/*  585 */       if (deviceSession != null) {
/*  586 */         sendResponse(channel, false, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */       }
/*      */       
/*  589 */       return null;
/*      */     } 
/*  591 */     if (type == 35) {
/*      */       
/*  593 */       getLastLocation(position, null);
/*      */       
/*  595 */       int status = buf.readUnsignedByte();
/*  596 */       position.set("armed", Boolean.valueOf(BitUtil.check(status, 0)));
/*  597 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 1)));
/*  598 */       position.set("charge", Boolean.valueOf(BitUtil.check(status, 2)));
/*      */       
/*  600 */       if (buf.readableBytes() >= 8) {
/*  601 */         position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*      */       }
/*  603 */       if (buf.readableBytes() >= 7) {
/*  604 */         position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*      */       }
/*      */       
/*  607 */       sendResponse(channel, false, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */       
/*  609 */       return position;
/*      */     } 
/*  611 */     if (type == 42) {
/*      */       
/*  613 */       String response = "NA&&NA&&0##";
/*  614 */       ByteBuf content = Unpooled.buffer();
/*  615 */       content.writeByte(response.length());
/*  616 */       content.writeInt(0);
/*  617 */       content.writeBytes(response.getBytes(StandardCharsets.US_ASCII));
/*  618 */       sendResponse(channel, true, 151, 0, content);
/*      */       
/*  620 */       return null;
/*      */     } 
/*  622 */     if (type == 138) {
/*      */       
/*  624 */       Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
/*  625 */       ByteBuf content = Unpooled.buffer();
/*  626 */       content.writeByte(calendar.get(1) - 2000);
/*  627 */       content.writeByte(calendar.get(2) + 1);
/*  628 */       content.writeByte(calendar.get(5));
/*  629 */       content.writeByte(calendar.get(11));
/*  630 */       content.writeByte(calendar.get(12));
/*  631 */       content.writeByte(calendar.get(13));
/*  632 */       sendResponse(channel, false, 138, 0, content);
/*      */       
/*  634 */       return null;
/*      */     } 
/*  636 */     if (type == 52) {
/*      */       
/*  638 */       buf.readUnsignedInt();
/*      */       
/*  640 */       decodeGps(position, buf, false, deviceSession.getTimeZone(), this.adjustTime);
/*      */       
/*  642 */       buf.readUnsignedShort();
/*      */       
/*  644 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*      */       
/*  646 */       position.setNetwork(new Network(CellTower.from(buf
/*  647 */               .readUnsignedShort(), buf.readUnsignedByte(), buf
/*  648 */               .readUnsignedShort(), buf.readUnsignedInt())));
/*      */       
/*  650 */       long driverId = buf.readUnsignedInt();
/*  651 */       if (driverId > 0L) {
/*  652 */         position.set("driverUniqueId", String.valueOf(driverId));
/*      */       }
/*      */       
/*  655 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*  656 */       position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*      */       
/*  658 */       long portInfo = buf.readUnsignedInt();
/*      */       
/*  660 */       position.set("input", Short.valueOf(buf.readUnsignedByte()));
/*  661 */       position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*      */       
/*  663 */       for (int i = 1; i <= BitUtil.between(portInfo, 20, 24); i++) {
/*  664 */         position.set("adc" + i, Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*      */       }
/*      */       
/*  667 */       return position;
/*      */     } 
/*  669 */     if (type == 53) {
/*      */       
/*  671 */       buf.skipBytes(6);
/*  672 */       buf.readUnsignedByte();
/*  673 */       buf.readUnsignedInt();
/*  674 */       buf.readUnsignedInt();
/*  675 */       buf.readUnsignedByte();
/*  676 */       buf.readUnsignedByte();
/*  677 */       buf.readUnsignedByte();
/*      */       
/*  679 */       ByteBuf photo = Unpooled.buffer(buf.readInt());
/*  680 */       int pictureId = buf.readInt();
/*  681 */       this.photos.put(Integer.valueOf(pictureId), photo);
/*  682 */       sendPhotoRequest(channel, pictureId);
/*      */       
/*  684 */       return null;
/*      */     } 
/*  686 */     if ((type == 23 && this.variant != Variant.RFID) || type == 105 || type == 243) {
/*      */       int wifiCount;
/*  688 */       ByteBuf time = buf.readSlice(6);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  695 */       DateBuilder dateBuilder = (new DateBuilder()).setYear(BcdUtil.readInteger(time, 2)).setMonth(BcdUtil.readInteger(time, 2)).setDay(BcdUtil.readInteger(time, 2)).setHour(BcdUtil.readInteger(time, 2)).setMinute(BcdUtil.readInteger(time, 2)).setSecond(BcdUtil.readInteger(time, 2));
/*  696 */       getLastLocation(position, processTime(dateBuilder.getDate()));
/*      */       
/*  698 */       Network network = new Network();
/*      */ 
/*      */       
/*  701 */       if (type == 243) {
/*  702 */         wifiCount = buf.readUnsignedByte();
/*      */       } else {
/*  704 */         wifiCount = buf.getUnsignedByte(2);
/*      */       } 
/*      */       
/*  707 */       for (int i = 0; i < wifiCount; i++) {
/*  708 */         if (type == 243) {
/*  709 */           buf.skipBytes(2);
/*      */         }
/*  711 */         WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
/*  712 */         wifiAccessPoint.setMacAddress(String.format("%02x:%02x:%02x:%02x:%02x:%02x", new Object[] {
/*  713 */                 Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), 
/*  714 */                 Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()) }));
/*  715 */         if (type != 243) {
/*  716 */           wifiAccessPoint.setSignalStrength(Integer.valueOf(buf.readUnsignedByte()));
/*      */         }
/*  718 */         network.addWifiAccessPoint(wifiAccessPoint);
/*      */       } 
/*      */       
/*  721 */       if (type != 243) {
/*      */         
/*  723 */         int cellCount = buf.readUnsignedByte();
/*  724 */         int mcc = buf.readUnsignedShort();
/*  725 */         int mnc = buf.readUnsignedByte();
/*  726 */         for (int j = 0; j < cellCount; j++) {
/*  727 */           network.addCellTower(CellTower.from(mcc, mnc, buf
/*  728 */                 .readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedByte()));
/*      */         }
/*      */         
/*  731 */         if (channel != null) {
/*  732 */           ByteBuf response = Unpooled.buffer();
/*  733 */           response.writeShort(30840);
/*  734 */           response.writeByte(0);
/*  735 */           response.writeByte(type);
/*  736 */           response.writeBytes(time.resetReaderIndex());
/*  737 */           response.writeByte(13);
/*  738 */           response.writeByte(10);
/*  739 */           channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*      */         } 
/*      */       } 
/*      */ 
/*      */       
/*  744 */       position.setNetwork(network);
/*      */       
/*  746 */       return position;
/*      */     } 
/*  748 */     if (type == 148) {
/*      */       
/*  750 */       getLastLocation(position, null);
/*      */       
/*  752 */       position.set("power", Double.valueOf(buf.readShort() * 0.01D));
/*      */       
/*  754 */       return position;
/*      */     } 
/*  756 */     if (type == 40 || type == 46 || type == 36 || type == 24 || type == 44 || type == 161 || type == 162 || type == 51) {
/*      */ 
/*      */ 
/*      */       
/*  760 */       boolean longFormat = (type == 161 || type == 162 || type == 51);
/*      */ 
/*      */ 
/*      */       
/*  764 */       DateBuilder dateBuilder = (new DateBuilder(deviceSession.getTimeZone())).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*      */       
/*  766 */       getLastLocation(position, processTime(dateBuilder.getDate()));
/*      */       
/*  768 */       if (this.variant == Variant.WANWAY_S20) {
/*  769 */         buf.readUnsignedByte();
/*      */       }
/*      */       
/*  772 */       int mcc = buf.readUnsignedShort();
/*  773 */       int mnc = BitUtil.check(mcc, 15) ? buf.readUnsignedShort() : buf.readUnsignedByte();
/*  774 */       Network network = new Network();
/*      */       
/*  776 */       int cellCount = (this.variant == Variant.WANWAY_S20) ? buf.readUnsignedByte() : ((type == 51) ? 6 : 7);
/*  777 */       for (int i = 0; i < cellCount; i++) {
/*  778 */         int lac = longFormat ? buf.readInt() : buf.readUnsignedShort();
/*  779 */         int cid = longFormat ? (int)buf.readLong() : buf.readUnsignedMedium();
/*  780 */         int rssi = -buf.readUnsignedByte();
/*  781 */         if (lac > 0) {
/*  782 */           network.addCellTower(CellTower.from(BitUtil.to(mcc, 15), mnc, lac, cid, rssi));
/*      */         }
/*      */       } 
/*      */       
/*  786 */       if (this.variant != Variant.WANWAY_S20) {
/*  787 */         buf.readUnsignedByte();
/*      */       }
/*      */       
/*  790 */       if (type != 40 && type != 46 && type != 36 && type != 161) {
/*      */         
/*  792 */         int wifiCount = buf.readUnsignedByte();
/*  793 */         for (int j = 0; j < wifiCount; j++) {
/*  794 */           String mac = ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/*  795 */           network.addWifiAccessPoint(WifiAccessPoint.from(mac
/*  796 */                 .substring(0, mac.length() - 1), buf.readUnsignedByte()));
/*      */         } 
/*      */       } 
/*      */       
/*  800 */       position.setNetwork(network);
/*      */     }
/*  802 */     else if (type == 21) {
/*      */       
/*  804 */       getLastLocation(position, null);
/*      */       
/*  806 */       int commandLength = buf.readUnsignedByte();
/*      */       
/*  808 */       if (commandLength > 0) {
/*  809 */         buf.readUnsignedInt();
/*  810 */         String data = buf.readSlice(commandLength - 4).toString(StandardCharsets.US_ASCII);
/*  811 */         if (data.startsWith("<ICCID:")) {
/*  812 */           position.set("iccid", data.substring(7, 27));
/*      */         } else {
/*  814 */           position.set("result", data);
/*      */         } 
/*      */       } 
/*      */     } else {
/*  818 */       if (type == 64) {
/*      */         
/*  820 */         buf.skipBytes(8);
/*      */         
/*  822 */         getLastLocation(position, new Date(buf.readUnsignedInt() * 1000L));
/*      */         
/*  824 */         position.set("relativeCapacity", Short.valueOf(buf.readUnsignedByte()));
/*  825 */         position.set("remainingCapacity", Integer.valueOf(buf.readUnsignedShort()));
/*  826 */         position.set("absoluteCapacity", Short.valueOf(buf.readUnsignedByte()));
/*  827 */         position.set("fullCapacity", Integer.valueOf(buf.readUnsignedShort()));
/*  828 */         position.set("batteryHealth", Short.valueOf(buf.readUnsignedByte()));
/*  829 */         position.set("batteryTemp", Double.valueOf(buf.readUnsignedShort() * 0.1D - 273.1D));
/*  830 */         position.set("current", Integer.valueOf(buf.readUnsignedShort()));
/*  831 */         position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*  832 */         position.set("cycleIndex", Integer.valueOf(buf.readUnsignedShort()));
/*  833 */         for (int i = 1; i <= 14; i++) {
/*  834 */           position.set("batteryCell" + i, Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*      */         }
/*  836 */         position.set("currentChargeInterval", Integer.valueOf(buf.readUnsignedShort()));
/*  837 */         position.set("maxChargeInterval", Integer.valueOf(buf.readUnsignedShort()));
/*  838 */         position.set("barcode", buf.readCharSequence(16, StandardCharsets.US_ASCII).toString().trim());
/*  839 */         position.set("batteryVersion", Integer.valueOf(buf.readUnsignedShort()));
/*  840 */         position.set("manufacturer", buf.readCharSequence(16, StandardCharsets.US_ASCII).toString().trim());
/*  841 */         position.set("batteryStatus", Long.valueOf(buf.readUnsignedInt()));
/*      */         
/*  843 */         position.set("controllerStatus", Long.valueOf(buf.readUnsignedInt()));
/*  844 */         position.set("controllerFault", Long.valueOf(buf.readUnsignedInt()));
/*      */         
/*  846 */         sendResponse(channel, false, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */         
/*  848 */         return position;
/*      */       } 
/*  850 */       if (type == 19 && buf.readableBytes() == 22) {
/*      */         
/*  852 */         getLastLocation(position, null);
/*      */         
/*  854 */         buf.readUnsignedByte();
/*  855 */         buf.readUnsignedShort();
/*  856 */         buf.readUnsignedByte();
/*  857 */         buf.readUnsignedByte();
/*      */         
/*  859 */         position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/*      */         
/*  861 */         buf.readUnsignedByte();
/*  862 */         buf.readUnsignedShort();
/*  863 */         buf.readUnsignedByte();
/*  864 */         buf.readUnsignedShort();
/*  865 */         buf.readUnsignedShort();
/*      */         
/*  867 */         int value = buf.readUnsignedShort();
/*  868 */         double temperature = BitUtil.to(value, 15) * 0.1D;
/*  869 */         position.set("temp1", Double.valueOf(BitUtil.check(value, 15) ? temperature : -temperature));
/*      */       }
/*  871 */       else if (isSupported(type)) {
/*      */         
/*  873 */         if (type == 25 && this.variant == Variant.SPACE10X) {
/*  874 */           return null;
/*      */         }
/*      */         
/*  877 */         if (hasGps(type)) {
/*  878 */           decodeGps(position, buf, false, deviceSession.getTimeZone(), this.adjustTime);
/*      */         } else {
/*  880 */           getLastLocation(position, null);
/*      */         } 
/*      */         
/*  883 */         if (hasLbs(type) && buf.readableBytes() > 6) {
/*  884 */           boolean hasLength = (hasStatus(type) && type != 25 && type != 165 && (type != 22 || this.variant != Variant.VXT01));
/*      */ 
/*      */ 
/*      */           
/*  888 */           decodeLbs(position, buf, type, hasLength);
/*      */         } 
/*      */         
/*  891 */         if (hasStatus(type)) {
/*  892 */           decodeStatus(position, buf, type);
/*  893 */           if (this.variant == Variant.OBD6) {
/*  894 */             int signal = buf.readUnsignedShort();
/*  895 */             int satellites = BitUtil.between(signal, 10, 15) + BitUtil.between(signal, 5, 10);
/*  896 */             position.set("sat", Integer.valueOf(satellites));
/*  897 */             position.set("rssi", Integer.valueOf(BitUtil.to(signal, 5)));
/*  898 */             position.set("alarm", decodeAlarm(buf.readUnsignedByte()));
/*  899 */             buf.readUnsignedByte();
/*  900 */             position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/*  901 */             buf.readUnsignedByte();
/*  902 */             position.set("power", Double.valueOf(buf.readUnsignedShort() / 100.0D));
/*      */           } else {
/*  904 */             int battery = buf.readUnsignedByte();
/*  905 */             position.set("batteryLevel", Integer.valueOf((battery <= 6) ? (battery * 100 / 6) : battery));
/*  906 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*  907 */             short alarmExtension = buf.readUnsignedByte();
/*  908 */             if (this.variant != Variant.VXT01) {
/*  909 */               if (this.additionalAlarms) {
/*  910 */                 position.set("alarm", decodeAlarmAlt(alarmExtension));
/*      */               } else {
/*  912 */                 position.set("alarm", decodeAlarm(alarmExtension));
/*      */               } 
/*      */             }
/*      */           } 
/*      */         } 
/*      */         
/*  918 */         if (type == 18) {
/*  919 */           if (this.variant == Variant.GT06E_CARD) {
/*  920 */             position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*  921 */             String data = buf.readCharSequence(buf.readUnsignedByte(), StandardCharsets.US_ASCII).toString();
/*  922 */             buf.readUnsignedByte();
/*  923 */             buf.readUnsignedByte();
/*  924 */             position.set("card", data.trim());
/*  925 */           } else if (this.variant == Variant.BENWAY) {
/*  926 */             int mask = buf.readUnsignedShort();
/*  927 */             position.set("ignition", Boolean.valueOf(BitUtil.check(mask, 15)));
/*  928 */             position.set("in2", Boolean.valueOf(BitUtil.check(mask, 14)));
/*  929 */             if (BitUtil.check(mask, 12)) {
/*  930 */               int value = BitUtil.to(mask, 9);
/*  931 */               if (BitUtil.check(mask, 9)) {
/*  932 */                 value = -value;
/*      */               }
/*  934 */               position.set("temp1", Integer.valueOf(value));
/*      */             } else {
/*  936 */               int value = BitUtil.to(mask, 10);
/*  937 */               if (BitUtil.check(mask, 13)) {
/*  938 */                 position.set("adc1", Integer.valueOf(value));
/*      */               } else {
/*  940 */                 position.set("adc1", Double.valueOf(value * 0.1D));
/*      */               } 
/*      */             } 
/*  943 */           } else if (this.variant == Variant.VXT01) {
/*  944 */             decodeStatus(position, buf, type);
/*  945 */             position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*  946 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*  947 */             buf.readUnsignedByte();
/*  948 */           } else if (this.variant == Variant.S5) {
/*  949 */             decodeStatus(position, buf, type);
/*  950 */             position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*  951 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*  952 */             if (this.additionalAlarms) {
/*  953 */               position.set("alarm", decodeAlarmAlt(buf.readUnsignedByte()));
/*      */             } else {
/*  955 */               position.set("alarm", decodeAlarm(buf.readUnsignedByte()));
/*      */             } 
/*  957 */             position.set("oil", Integer.valueOf(buf.readUnsignedShort()));
/*  958 */             int temperature = buf.readUnsignedByte();
/*  959 */             if (BitUtil.check(temperature, 7)) {
/*  960 */               temperature = -BitUtil.to(temperature, 7);
/*      */             }
/*  962 */             position.set("temp1", Integer.valueOf(temperature));
/*  963 */             position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 10L));
/*  964 */           } else if (this.variant == Variant.WETRUST) {
/*  965 */             position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*  966 */             position.set("card", buf.readCharSequence(buf
/*  967 */                   .readUnsignedByte(), StandardCharsets.US_ASCII).toString());
/*  968 */             position.set("alarm", (buf.readUnsignedByte() > 0) ? "general" : null);
/*  969 */             position.set("cardStatus", Short.valueOf(buf.readUnsignedByte()));
/*  970 */             position.set("drivingTime", Integer.valueOf(buf.readUnsignedShort()));
/*      */           } 
/*      */         }
/*      */         
/*  974 */         if (type == 34 && this.variant == Variant.SEEWORLD) {
/*  975 */           position.set("ignition", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/*  976 */           buf.readUnsignedByte();
/*  977 */           buf.readUnsignedByte();
/*  978 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*  979 */           buf.readUnsignedInt();
/*  980 */           int temperature = buf.readUnsignedShort();
/*  981 */           if (BitUtil.check(temperature, 15)) {
/*  982 */             temperature = -BitUtil.to(temperature, 15);
/*      */           }
/*  984 */           position.set("temp1", Double.valueOf(temperature * 0.01D));
/*  985 */           position.set("humidity", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*      */         } 
/*      */         
/*  988 */         if ((type == 34 || type == 55 || type == 45) && buf
/*  989 */           .readableBytes() >= 9) {
/*  990 */           position.set("ignition", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/*  991 */           position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*  992 */           position.set("archive", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/*      */         } 
/*      */         
/*  995 */         if (type == 55) {
/*  996 */           int module = buf.readUnsignedShort();
/*  997 */           int subLength = buf.readUnsignedByte();
/*  998 */           switch (module) {
/*      */             case 39:
/* 1000 */               position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*      */               break;
/*      */             case 46:
/* 1003 */               position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*      */               break;
/*      */             case 59:
/* 1006 */               position.setAccuracy(buf.readUnsignedShort() * 0.01D);
/*      */               break;
/*      */             default:
/* 1009 */               buf.skipBytes(subLength);
/*      */               break;
/*      */           } 
/*      */         
/*      */         } 
/* 1014 */         if (type == 23) {
/* 1015 */           buf.readUnsignedByte();
/* 1016 */           position.set("driverUniqueId", ByteBufUtil.hexDump(buf.readSlice(8)));
/*      */         } 
/*      */         
/* 1019 */         if (buf.readableBytes() == 9 || buf.readableBytes() == 13) {
/* 1020 */           position.set("ignition", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/* 1021 */           buf.readUnsignedByte();
/* 1022 */           position.set("archive", (buf.readUnsignedByte() > 0) ? Boolean.valueOf(true) : null);
/*      */         } 
/*      */         
/* 1025 */         if (buf.readableBytes() == 10) {
/* 1026 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*      */         }
/*      */       }
/* 1029 */       else if (type == 149) {
/*      */         
/* 1031 */         boolean extendedAlarm = (dataLength > 7);
/* 1032 */         if (extendedAlarm) {
/* 1033 */           decodeGps(position, buf, false, false, false, deviceSession.getTimeZone(), this.adjustTime);
/*      */         }
/*      */         else {
/*      */           
/* 1037 */           DateBuilder dateBuilder = (new DateBuilder(deviceSession.getTimeZone())).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 1038 */           getLastLocation(position, dateBuilder.getDate());
/*      */         } 
/* 1040 */         short alarmType = buf.readUnsignedByte();
/* 1041 */         switch (alarmType) {
/*      */           case 1:
/* 1043 */             position.set("alarm", extendedAlarm ? "sos" : "general");
/*      */             break;
/*      */           case 128:
/* 1046 */             position.set("alarm", "vibration");
/*      */             break;
/*      */           case 135:
/* 1049 */             position.set("alarm", "overspeed");
/*      */             break;
/*      */           case 144:
/* 1052 */             position.set("alarm", "hardAcceleration");
/*      */             break;
/*      */           case 145:
/* 1055 */             position.set("alarm", "hardBraking");
/*      */             break;
/*      */           case 146:
/* 1058 */             position.set("alarm", "hardCornering");
/*      */             break;
/*      */           case 147:
/* 1061 */             position.set("alarm", "accident");
/*      */             break;
/*      */           default:
/* 1064 */             position.set("alarm", "general");
/*      */             break;
/*      */         } 
/*      */ 
/*      */       
/*      */       } else {
/* 1070 */         if (dataLength > 0) {
/* 1071 */           buf.skipBytes(dataLength);
/*      */         }
/* 1073 */         if (type != 128 && type != 129 && type != 130) {
/* 1074 */           sendResponse(channel, false, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */         }
/* 1076 */         return null;
/*      */       } 
/*      */     } 
/*      */     
/* 1080 */     if (hasLanguage(type)) {
/* 1081 */       buf.readUnsignedShort();
/*      */     }
/*      */     
/* 1084 */     if (type == 39 || type == 164) {
/* 1085 */       position.set("geofence", Short.valueOf(buf.readUnsignedByte()));
/*      */     }
/*      */     
/* 1088 */     if (type != 18 || !this.noAck) {
/* 1089 */       sendResponse(channel, false, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */     }
/*      */     
/* 1092 */     return position;
/*      */   }
/*      */ 
/*      */   
/*      */   private Object decodeExtended(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 1097 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 1098 */     if (deviceSession == null) {
/* 1099 */       return null;
/*      */     }
/*      */     
/* 1102 */     if (deviceSession.getTimeZone() == null) {
/* 1103 */       deviceSession.setTimeZone(getTimeZone(deviceSession.getDeviceId()));
/*      */     }
/*      */     
/* 1106 */     Position position = new Position(getProtocolName());
/* 1107 */     position.setDeviceId(deviceSession.getDeviceId());
/*      */     
/* 1109 */     buf.readUnsignedShort();
/* 1110 */     int type = buf.readUnsignedByte();
/*      */     
/* 1112 */     if (type == 33) {
/*      */       String data;
/* 1114 */       buf.readUnsignedInt();
/*      */       
/* 1116 */       if (buf.readUnsignedByte() == 1) {
/* 1117 */         data = buf.readSlice(buf.readableBytes() - 6).toString(StandardCharsets.US_ASCII);
/*      */       } else {
/* 1119 */         data = buf.readSlice(buf.readableBytes() - 6).toString(StandardCharsets.UTF_16BE);
/*      */       } 
/*      */       
/* 1122 */       Parser parser = new Parser(PATTERN_LOCATION, data);
/*      */       
/* 1124 */       if (parser.matches()) {
/* 1125 */         position.setValid(true);
/* 1126 */         position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 1127 */         position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 1128 */         position.setCourse(parser.nextDouble().doubleValue());
/* 1129 */         position.setSpeed(parser.nextDouble().doubleValue());
/* 1130 */         position.setTime(processTime(parser.nextDateTime(Parser.DateTimeFormat.YMD_HMS)));
/*      */       } else {
/* 1132 */         getLastLocation(position, null);
/* 1133 */         position.set("result", data);
/*      */       } 
/*      */       
/* 1136 */       return position;
/*      */     } 
/* 1138 */     if (type == 148) {
/*      */       
/* 1140 */       int subType = buf.readUnsignedByte();
/*      */       
/* 1142 */       getLastLocation(position, null);
/*      */       
/* 1144 */       if (subType == 0) {
/*      */         
/* 1146 */         double voltage = buf.readUnsignedShort() * 0.01D;
/* 1147 */         position.set("power", Double.valueOf(voltage));
/* 1148 */         position.set("adc1", Double.valueOf(voltage));
/* 1149 */         return position;
/*      */       } 
/* 1151 */       if (subType == 4) {
/*      */         
/* 1153 */         CharSequence content = buf.readCharSequence(buf.readableBytes() - 4 - 2, StandardCharsets.US_ASCII);
/* 1154 */         String[] values = content.toString().split(";");
/* 1155 */         for (String value : values) {
/* 1156 */           String[] pair = value.split("=");
/* 1157 */           switch (pair[0]) {
/*      */             case "ALM1":
/*      */             case "ALM2":
/*      */             case "ALM3":
/* 1161 */               position.set("alarm" + pair[0].charAt(3) + "Status", Integer.valueOf(Integer.parseInt(pair[1], 16)));
/*      */             case "STA1":
/* 1163 */               position.set("otherStatus", Integer.valueOf(Integer.parseInt(pair[1], 16)));
/*      */               break;
/*      */             case "DYD":
/* 1166 */               position.set("engineStatus", Integer.valueOf(Integer.parseInt(pair[1], 16)));
/*      */               break;
/*      */           } 
/*      */ 
/*      */         
/*      */         } 
/* 1172 */         return position;
/*      */       } 
/* 1174 */       if (subType == 5) {
/*      */         
/* 1176 */         int flags = buf.readUnsignedByte();
/* 1177 */         position.set("door", Boolean.valueOf(BitUtil.check(flags, 0)));
/* 1178 */         position.set("io1", Boolean.valueOf(BitUtil.check(flags, 2)));
/* 1179 */         return position;
/*      */       } 
/* 1181 */       if (subType == 10) {
/*      */         
/* 1183 */         buf.skipBytes(8);
/* 1184 */         buf.skipBytes(8);
/* 1185 */         position.set("iccid", ByteBufUtil.hexDump(buf.readSlice(10)).replaceAll("f", ""));
/* 1186 */         return position;
/*      */       } 
/* 1188 */       if (subType == 13) {
/*      */         
/* 1190 */         if (buf.getByte(buf.readerIndex()) != 33) {
/* 1191 */           buf.skipBytes(6);
/*      */         }
/*      */         
/* 1194 */         Parser parser = new Parser(PATTERN_FUEL, buf.toString(buf
/* 1195 */               .readerIndex(), buf.readableBytes() - 4 - 2, StandardCharsets.US_ASCII));
/* 1196 */         if (!parser.matches()) {
/* 1197 */           return null;
/*      */         }
/*      */         
/* 1200 */         position.set("temp1", Double.valueOf(parser.nextDouble(0.0D)));
/* 1201 */         position.set("fuel", Double.valueOf(parser.nextDouble(0.0D)));
/*      */         
/* 1203 */         return position;
/*      */       } 
/* 1205 */       if (subType == 27)
/*      */       {
/* 1207 */         buf.readUnsignedByte();
/* 1208 */         buf.readUnsignedByte();
/* 1209 */         position.set("driverUniqueId", ByteBufUtil.hexDump(buf.readSlice(4)));
/* 1210 */         buf.readUnsignedByte();
/* 1211 */         buf.readUnsignedByte();
/* 1212 */         return position;
/*      */       }
/*      */     
/*      */     }
/* 1216 */     else if (type == 54) {
/*      */       
/* 1218 */       int pictureId = buf.readInt();
/*      */       
/* 1220 */       ByteBuf photo = this.photos.get(Integer.valueOf(pictureId));
/*      */       
/* 1222 */       buf.readUnsignedInt();
/* 1223 */       buf.readBytes(photo, buf.readUnsignedShort());
/*      */       
/* 1225 */       if (photo.writableBytes() > 0) {
/* 1226 */         sendPhotoRequest(channel, pictureId);
/*      */       } else {
/* 1228 */         Device device = (Device)Context.getDeviceManager().getById(deviceSession.getDeviceId());
/* 1229 */         position.set("image", 
/* 1230 */             Context.getMediaManager().writeFile(device.getUniqueId(), photo, "jpg"));
/* 1231 */         ((ByteBuf)this.photos.remove(Integer.valueOf(pictureId))).release();
/*      */       } 
/*      */     } else {
/* 1234 */       if (type == 50 || type == 51) {
/*      */         
/* 1236 */         if (!decodeGps(position, buf, true, deviceSession.getTimeZone(), this.adjustTime)) {
/* 1237 */           getLastLocation(position, processTime(position.getDeviceTime()));
/*      */         }
/*      */         
/* 1240 */         if (decodeLbs(position, buf, type, true)) {
/* 1241 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*      */         }
/*      */         
/* 1244 */         buf.skipBytes(buf.readUnsignedByte());
/* 1245 */         buf.skipBytes(buf.readUnsignedByte());
/*      */         
/* 1247 */         int status = buf.readUnsignedByte();
/* 1248 */         position.set("status", Integer.valueOf(status));
/*      */         
/* 1250 */         if (type == 51) {
/* 1251 */           switch (status) {
/*      */             case 160:
/* 1253 */               position.set("armed", Boolean.valueOf(true));
/*      */               break;
/*      */             case 161:
/* 1256 */               position.set("armed", Boolean.valueOf(false));
/*      */               break;
/*      */             case 162:
/*      */             case 163:
/* 1260 */               position.set("alarm", "lowBattery");
/*      */               break;
/*      */             case 164:
/* 1263 */               position.set("alarm", "general");
/*      */               break;
/*      */             case 165:
/* 1266 */               position.set("alarm", "door");
/*      */               break;
/*      */           } 
/*      */ 
/*      */ 
/*      */         
/*      */         }
/* 1273 */         buf.skipBytes(buf.readUnsignedByte());
/*      */         
/* 1275 */         sendResponse(channel, true, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */         
/* 1277 */         return position;
/*      */       } 
/* 1279 */       if (type == 140) {
/*      */ 
/*      */ 
/*      */         
/* 1283 */         DateBuilder dateBuilder = (new DateBuilder(deviceSession.getTimeZone())).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*      */         
/* 1285 */         getLastLocation(position, processTime(dateBuilder.getDate()));
/*      */         
/* 1287 */         position.set("ignition", Boolean.valueOf((buf.readUnsignedByte() > 0)));
/*      */         
/* 1289 */         String data = buf.readCharSequence(buf.readableBytes() - 18, StandardCharsets.US_ASCII).toString();
/* 1290 */         for (String pair : data.split(",")) {
/* 1291 */           String[] values = pair.split("=");
/* 1292 */           if (values.length >= 2) {
/* 1293 */             switch (Integer.parseInt(values[0].substring(0, 2), 16)) {
/*      */               case 40:
/* 1295 */                 position.set("odometer", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 43:
/* 1298 */                 position.set("fuel", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 45:
/* 1301 */                 position.set("coolantTemp", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 53:
/* 1304 */                 position.set("obdSpeed", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 54:
/* 1307 */                 position.set("rpm", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 71:
/* 1310 */                 position.set("fuelUsed", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 73:
/* 1313 */                 position.set("hours", Double.valueOf(Integer.parseInt(values[1], 16) * 0.01D));
/*      */                 break;
/*      */               case 74:
/* 1316 */                 position.set("vin", values[1]);
/*      */                 break;
/*      */             } 
/*      */ 
/*      */ 
/*      */           
/*      */           }
/*      */         } 
/* 1324 */         return position;
/*      */       } 
/* 1326 */       if (type == 112) {
/*      */         
/* 1328 */         while (buf.readableBytes() > 6) {
/* 1329 */           CellTower cellTower; int input, event; double latitude, longitude; int flags, moduleType = buf.readUnsignedShort();
/* 1330 */           int moduleLength = buf.readUnsignedShort();
/*      */           
/* 1332 */           switch (moduleType) {
/*      */             case 3:
/* 1334 */               position.set("iccid", ByteBufUtil.hexDump(buf.readSlice(10)));
/*      */               continue;
/*      */             case 9:
/* 1337 */               position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*      */               continue;
/*      */             case 10:
/* 1340 */               position.set("satVisible", Short.valueOf(buf.readUnsignedByte()));
/*      */               continue;
/*      */             case 17:
/* 1343 */               cellTower = CellTower.from(buf
/* 1344 */                   .readUnsignedShort(), buf
/* 1345 */                   .readUnsignedShort(), buf
/* 1346 */                   .readUnsignedShort(), buf
/* 1347 */                   .readUnsignedMedium(), buf
/* 1348 */                   .readUnsignedByte());
/* 1349 */               if (cellTower.getCellId().longValue() > 0L) {
/* 1350 */                 position.setNetwork(new Network(cellTower));
/*      */               }
/*      */               continue;
/*      */             case 24:
/* 1354 */               position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*      */               continue;
/*      */             case 40:
/* 1357 */               position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*      */               continue;
/*      */             case 41:
/* 1360 */               position.set("index", Long.valueOf(buf.readUnsignedInt()));
/*      */               continue;
/*      */             case 42:
/* 1363 */               input = buf.readUnsignedByte();
/* 1364 */               position.set("door", Boolean.valueOf((BitUtil.to(input, 4) > 0)));
/* 1365 */               position.set("tamper", Boolean.valueOf((BitUtil.from(input, 4) > 0)));
/*      */               continue;
/*      */             case 43:
/* 1368 */               event = buf.readUnsignedByte();
/* 1369 */               switch (event) {
/*      */                 case 17:
/* 1371 */                   position.set("alarm", "lowBattery");
/*      */                   break;
/*      */                 case 18:
/* 1374 */                   position.set("alarm", "lowPower");
/*      */                   break;
/*      */                 case 19:
/* 1377 */                   position.set("alarm", "powerCut");
/*      */                   break;
/*      */                 case 20:
/* 1380 */                   position.set("alarm", "removing");
/*      */                   break;
/*      */               } 
/*      */ 
/*      */               
/* 1385 */               position.set("event", Integer.valueOf(event));
/*      */               continue;
/*      */             case 46:
/* 1388 */               position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*      */               continue;
/*      */             case 51:
/* 1391 */               position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/* 1392 */               position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 1393 */               position.setAltitude(buf.readShort());
/*      */               
/* 1395 */               latitude = buf.readUnsignedInt() / 60.0D / 30000.0D;
/* 1396 */               longitude = buf.readUnsignedInt() / 60.0D / 30000.0D;
/* 1397 */               position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*      */               
/* 1399 */               flags = buf.readUnsignedShort();
/* 1400 */               position.setCourse(BitUtil.to(flags, 10));
/* 1401 */               position.setValid(BitUtil.check(flags, 12));
/*      */               
/* 1403 */               if (!BitUtil.check(flags, 10)) {
/* 1404 */                 latitude = -latitude;
/*      */               }
/* 1406 */               if (BitUtil.check(flags, 11)) {
/* 1407 */                 longitude = -longitude;
/*      */               }
/*      */               
/* 1410 */               position.setLatitude(latitude);
/* 1411 */               position.setLongitude(longitude);
/*      */               continue;
/*      */             case 52:
/* 1414 */               position.set("event", Short.valueOf(buf.readUnsignedByte()));
/* 1415 */               buf.readUnsignedIntLE();
/* 1416 */               buf.skipBytes(buf.readUnsignedByte());
/*      */               continue;
/*      */           } 
/* 1419 */           buf.skipBytes(moduleLength);
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/* 1424 */         if (position.getFixTime() == null) {
/* 1425 */           getLastLocation(position, null);
/*      */         }
/*      */         
/* 1428 */         sendResponse(channel, false, 112, buf.readUnsignedShort(), (ByteBuf)null);
/*      */         
/* 1430 */         return position;
/*      */       } 
/* 1432 */       if (type == 65) {
/*      */         
/* 1434 */         buf.skipBytes(8);
/* 1435 */         long timestamp = buf.readUnsignedInt() * 1000L;
/* 1436 */         buf.skipBytes(14);
/* 1437 */         buf.skipBytes(8);
/*      */         
/* 1439 */         int mediaId = buf.readInt();
/* 1440 */         int mediaLength = buf.readInt();
/* 1441 */         int mediaType = buf.readUnsignedByte();
/* 1442 */         int mediaFormat = buf.readUnsignedByte();
/*      */         
/* 1444 */         if (mediaType == 0 && mediaFormat == 0) {
/*      */           ByteBuf photo;
/* 1446 */           buf.readUnsignedByte();
/*      */ 
/*      */           
/* 1449 */           if (buf.readUnsignedShort() == 0) {
/* 1450 */             photo = Unpooled.buffer(mediaLength);
/* 1451 */             if (this.photos.containsKey(Integer.valueOf(mediaId))) {
/* 1452 */               ((ByteBuf)this.photos.remove(Integer.valueOf(mediaId))).release();
/*      */             }
/* 1454 */             this.photos.put(Integer.valueOf(mediaId), photo);
/*      */           } else {
/* 1456 */             photo = this.photos.get(Integer.valueOf(mediaId));
/*      */           } 
/*      */           
/* 1459 */           if (photo != null) {
/* 1460 */             buf.readBytes(photo, buf.readableBytes() - 6);
/* 1461 */             if (!photo.isWritable()) {
/* 1462 */               position = new Position(getProtocolName());
/* 1463 */               position.setDeviceId(deviceSession.getDeviceId());
/* 1464 */               getLastLocation(position, new Date(timestamp));
/* 1465 */               Device device = (Device)Context.getDeviceManager().getById(deviceSession.getDeviceId());
/* 1466 */               position.set("image", 
/* 1467 */                   Context.getMediaManager().writeFile(device.getUniqueId(), photo, "jpg"));
/* 1468 */               ((ByteBuf)this.photos.remove(Integer.valueOf(mediaId))).release();
/*      */             } 
/*      */           } 
/*      */         } 
/*      */ 
/*      */         
/* 1474 */         sendResponse(channel, true, type, buf.getShort(buf.writerIndex() - 6), (ByteBuf)null);
/*      */         
/* 1476 */         return position;
/*      */       } 
/* 1478 */       if (type == 155) {
/*      */         
/* 1480 */         getLastLocation(position, null);
/*      */         
/* 1482 */         buf.readUnsignedByte();
/* 1483 */         int length = buf.readableBytes() - 9;
/*      */         
/* 1485 */         if (length <= 0)
/* 1486 */           return null; 
/* 1487 */         if (length < 8) {
/* 1488 */           position.set("temp1", 
/*      */               
/* 1490 */               Double.valueOf(Double.parseDouble(buf.readCharSequence(length - 1, StandardCharsets.US_ASCII).toString())));
/*      */         } else {
/* 1492 */           int cardType = buf.readUnsignedByte();
/* 1493 */           if (cardType == 5) {
/* 1494 */             buf.skipBytes(6);
/* 1495 */             position.set("status", Short.valueOf(buf.readUnsignedByte()));
/* 1496 */             position.set("driverUniqueId", buf
/*      */                 
/* 1498 */                 .readCharSequence(length - 8, StandardCharsets.US_ASCII).toString());
/*      */           } else {
/* 1500 */             position.set("driverUniqueId", buf
/*      */                 
/* 1502 */                 .readCharSequence(length - 1, StandardCharsets.US_ASCII).toString());
/*      */           } 
/*      */         } 
/*      */         
/* 1506 */         return position;
/*      */       } 
/*      */     } 
/*      */     
/* 1510 */     return null;
/*      */   }
/*      */   
/*      */   private void decodeVariant(ByteBuf buf) {
/* 1514 */     int length, type, header = buf.getUnsignedShort(buf.readerIndex());
/*      */ 
/*      */     
/* 1517 */     if (header == 30840) {
/* 1518 */       length = buf.getUnsignedByte(buf.readerIndex() + 2);
/* 1519 */       type = buf.getUnsignedByte(buf.readerIndex() + 2 + 1);
/*      */     } else {
/* 1521 */       length = buf.getUnsignedShort(buf.readerIndex() + 2);
/* 1522 */       type = buf.getUnsignedByte(buf.readerIndex() + 2 + 2);
/*      */     } 
/*      */     
/* 1525 */     if (header == 30840 && type == 18 && length == 36) {
/* 1526 */       this.variant = Variant.VXT01;
/* 1527 */     } else if (header == 30840 && type == 22 && length == 36) {
/* 1528 */       this.variant = Variant.VXT01;
/* 1529 */     } else if (header == 30840 && type == 36 && length == 49) {
/* 1530 */       this.variant = Variant.WANWAY_S20;
/* 1531 */     } else if (header == 30840 && type == 18 && length >= 113) {
/* 1532 */       this.variant = Variant.GT06E_CARD;
/* 1533 */     } else if (header == 30840 && type == 18 && length == 33) {
/* 1534 */       this.variant = Variant.BENWAY;
/* 1535 */     } else if (header == 30840 && type == 18 && length == 43) {
/* 1536 */       this.variant = Variant.S5;
/* 1537 */     } else if (header == 30840 && type == 25 && length >= 23) {
/* 1538 */       this.variant = Variant.SPACE10X;
/* 1539 */     } else if (header == 30840 && type == 19 && length == 19) {
/* 1540 */       this.variant = Variant.OBD6;
/* 1541 */     } else if (header == 30840 && type == 18 && length == 41) {
/* 1542 */       this.variant = Variant.WETRUST;
/* 1543 */     } else if (header == 30840 && type == 149 && buf.getUnsignedShort(buf.readerIndex() + 4) == 65535) {
/* 1544 */       this.variant = Variant.JC400;
/* 1545 */     } else if (header == 30840 && type == 50 && length == 39) {
/* 1546 */       this.variant = Variant.SL4X;
/* 1547 */     } else if (header == 30840 && type == 34 && length == 47) {
/* 1548 */       this.variant = Variant.SEEWORLD;
/* 1549 */     } else if (header == 30840 && type == 22 && length == 38) {
/* 1550 */       this.variant = Variant.SEEWORLD;
/* 1551 */     } else if (header == 30840 && type == 23 && length == 40) {
/* 1552 */       this.variant = Variant.RFID;
/*      */     } else {
/* 1554 */       this.variant = Variant.STANDARD;
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 1562 */     ByteBuf buf = (ByteBuf)msg;
/*      */     
/* 1564 */     decodeVariant(buf);
/*      */     
/* 1566 */     int header = buf.readShort();
/*      */     
/* 1568 */     if (header == 30840) {
/* 1569 */       return decodeBasic(channel, remoteAddress, buf);
/*      */     }
/* 1571 */     return decodeExtended(channel, remoteAddress, buf);
/*      */   }
/*      */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gt06ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */