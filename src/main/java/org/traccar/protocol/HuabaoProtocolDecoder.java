///*      */ package org.traccar.protocol;
///*      */ import io.netty.buffer.ByteBuf;
///*      */ import io.netty.buffer.ByteBufUtil;
///*      */ import io.netty.buffer.Unpooled;
///*      */ import io.netty.channel.Channel;
///*      */ import java.net.SocketAddress;
///*      */ import java.nio.charset.StandardCharsets;
///*      */ import java.util.Calendar;
///*      */ import java.util.Date;
///*      */ import java.util.List;
///*      */ import java.util.TimeZone;
///*      */ import org.traccar.BaseProtocolDecoder;
///*      */ import org.traccar.DeviceSession;
///*      */ import org.traccar.NetworkMessage;
///*      */ import org.traccar.Protocol;
///*      */ import org.traccar.helper.BcdUtil;
///*      */ import org.traccar.helper.BitUtil;
///*      */ import org.traccar.helper.Checksum;
///*      */ import org.traccar.helper.DateBuilder;
///*      */ import org.traccar.model.CellTower;
///*      */ import org.traccar.model.Network;
///*      */ import org.traccar.model.Position;
///*      */
///*      */ public class HuabaoProtocolDecoder extends BaseProtocolDecoder {
///*      */   public static final int MSG_GENERAL_RESPONSE = 32769;
///*      */   public static final int MSG_GENERAL_RESPONSE_2 = 17409;
///*      */   public static final int MSG_HEARTBEAT = 2;
///*      */   public static final int MSG_TERMINAL_REGISTER = 256;
///*      */   public static final int MSG_TERMINAL_REGISTER_RESPONSE = 33024;
///*      */   public static final int MSG_TERMINAL_CONTROL = 33029;
///*      */   public static final int MSG_TERMINAL_AUTH = 258;
///*      */   public static final int MSG_LOCATION_REPORT = 512;
///*      */   public static final int MSG_LOCATION_BATCH_2 = 528;
///*      */   public static final int MSG_ACCELERATION = 8304;
///*      */   public static final int MSG_LOCATION_REPORT_2 = 21761;
///*      */   public static final int MSG_LOCATION_REPORT_BLIND = 21762;
///*      */   public static final int MSG_LOCATION_BATCH = 1796;
///*      */   public static final int MSG_OIL_CONTROL = 40966;
///*      */   public static final int MSG_TIME_SYNC_REQUEST = 265;
///*      */   public static final int MSG_TIME_SYNC_RESPONSE = 33033;
///*      */   public static final int MSG_PHOTO = 34952;
///*      */   public static final int MSG_TRANSPARENT = 2304;
///*      */   public static final int RESULT_SUCCESS = 0;
///*      */
///*      */   public HuabaoProtocolDecoder(Protocol protocol) {
///*   46 */     super(protocol);
///*      */   }
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */   public static ByteBuf formatMessage(int type, ByteBuf id, boolean shortIndex, ByteBuf data) {
///*   71 */     ByteBuf buf = Unpooled.buffer();
///*   72 */     buf.writeByte(126);
///*   73 */     buf.writeShort(type);
///*   74 */     buf.writeShort(data.readableBytes());
///*   75 */     buf.writeBytes(id);
///*   76 */     if (shortIndex) {
///*   77 */       buf.writeByte(1);
///*      */     } else {
///*   79 */       buf.writeShort(0);
///*      */     }
///*   81 */     buf.writeBytes(data);
///*   82 */     data.release();
///*   83 */     buf.writeByte(Checksum.xor(buf.nioBuffer(1, buf.readableBytes() - 1)));
///*   84 */     buf.writeByte(126);
///*   85 */     return buf;
///*      */   }
///*      */
///*      */
///*      */   private void sendGeneralResponse(Channel channel, SocketAddress remoteAddress, ByteBuf id, int type, int index) {
///*   90 */     if (channel != null) {
///*   91 */       ByteBuf response = Unpooled.buffer();
///*   92 */       response.writeShort(index);
///*   93 */       response.writeShort(type);
///*   94 */       response.writeByte(0);
///*   95 */       channel.writeAndFlush(new NetworkMessage(
///*   96 */             formatMessage(32769, id, false, response), remoteAddress));
///*      */     }
///*      */   }
///*      */
///*      */
///*      */   private void sendGeneralResponse2(Channel channel, SocketAddress remoteAddress, ByteBuf id, int type) {
///*  102 */     if (channel != null) {
///*  103 */       ByteBuf response = Unpooled.buffer();
///*  104 */       response.writeShort(type);
///*  105 */       response.writeByte(0);
///*  106 */       channel.writeAndFlush(new NetworkMessage(
///*  107 */             formatMessage(17409, id, true, response), remoteAddress));
///*      */     }
///*      */   }
///*      */
///*      */   private String decodeAlarm(long value) {
///*  112 */     if (BitUtil.check(value, 0)) {
///*  113 */       return "sos";
///*      */     }
///*  115 */     if (BitUtil.check(value, 1)) {
///*  116 */       return "overspeed";
///*      */     }
///*  118 */     if (BitUtil.check(value, 5)) {
///*  119 */       return "gpsAntennaCut";
///*      */     }
///*  121 */     if (BitUtil.check(value, 4) || BitUtil.check(value, 9) ||
///*  122 */       BitUtil.check(value, 10) || BitUtil.check(value, 11)) {
///*  123 */       return "fault";
///*      */     }
///*  125 */     if (BitUtil.check(value, 7) || BitUtil.check(value, 18)) {
///*  126 */       return "lowBattery";
///*      */     }
///*  128 */     if (BitUtil.check(value, 8)) {
///*  129 */       return "powerOff";
///*      */     }
///*  131 */     if (BitUtil.check(value, 15)) {
///*  132 */       return "vibration";
///*      */     }
///*  134 */     if (BitUtil.check(value, 16) || BitUtil.check(value, 17)) {
///*  135 */       return "tampering";
///*      */     }
///*  137 */     if (BitUtil.check(value, 20)) {
///*  138 */       return "geofence";
///*      */     }
///*  140 */     if (BitUtil.check(value, 28)) {
///*  141 */       return "movement";
///*      */     }
///*  143 */     if (BitUtil.check(value, 29) || BitUtil.check(value, 30)) {
///*  144 */       return "accident";
///*      */     }
///*  146 */     return null;
///*      */   }
///*      */
///*      */   private int readSignedWord(ByteBuf buf) {
///*  150 */     int value = buf.readUnsignedShort();
///*  151 */     return BitUtil.check(value, 15) ? -BitUtil.to(value, 15) : BitUtil.to(value, 15);
///*      */   }
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */   private Date readDate(ByteBuf buf, TimeZone timeZone) {
///*  161 */     DateBuilder dateBuilder = (new DateBuilder(timeZone)).setYear(BcdUtil.readInteger(buf, 2)).setMonth(BcdUtil.readInteger(buf, 2)).setDay(BcdUtil.readInteger(buf, 2)).setHour(BcdUtil.readInteger(buf, 2)).setMinute(BcdUtil.readInteger(buf, 2)).setSecond(BcdUtil.readInteger(buf, 2));
///*  162 */     return dateBuilder.getDate();
///*      */   }
///*      */
///*      */   private String decodeId(ByteBuf id) {
///*  166 */     String serial = ByteBufUtil.hexDump(id);
///*  167 */     if (serial.matches("[0-9]+")) {
///*  168 */       return serial;
///*      */     }
///*  170 */     long imei = id.getUnsignedShort(0);
///*  171 */     imei = (imei << 32L) + id.getUnsignedInt(2);
///*  172 */     return String.valueOf(imei) + Checksum.luhn(imei);
///*      */   }
///*      */
///*      */
///*      */
///*      */
///*      */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///*      */     int index;
///*  180 */     ByteBuf buf = (ByteBuf)msg;
///*      */
///*  182 */     if (buf.getByte(buf.readerIndex()) == 40) {
///*  183 */       return decodeResult(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII));
///*      */     }
///*      */
///*  186 */     buf.readUnsignedByte();
///*  187 */     int type = buf.readUnsignedShort();
///*  188 */     int attribute = buf.readUnsignedShort();
///*  189 */     ByteBuf id = buf.readSlice(6);
///*      */
///*  191 */     if (type == 21761 || type == 21762) {
///*  192 */       index = buf.readUnsignedByte();
///*      */     } else {
///*  194 */       index = buf.readUnsignedShort();
///*      */     }
///*      */
///*  197 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { decodeId(id) });
///*  198 */     if (deviceSession == null) {
///*  199 */       return null;
///*      */     }
///*      */
///*  202 */     if (deviceSession.getTimeZone() == null) {
///*  203 */       deviceSession.setTimeZone(getTimeZone(deviceSession.getDeviceId(), "GMT+8"));
///*      */     }
///*      */
///*  206 */     if (type == 256) {
///*      */
///*  208 */       if (channel != null) {
///*  209 */         ByteBuf response = Unpooled.buffer();
///*  210 */         response.writeShort(index);
///*  211 */         response.writeByte(0);
///*  212 */         response.writeBytes(decodeId(id).getBytes(StandardCharsets.US_ASCII));
///*  213 */         channel.writeAndFlush(new NetworkMessage(
///*  214 */               formatMessage(33024, id, false, response), remoteAddress));
///*      */       }
///*      */
///*  217 */     } else if (type == 258 || type == 2 || type == 34952) {
///*      */
///*  219 */       sendGeneralResponse(channel, remoteAddress, id, type, index);
///*      */     } else {
///*  221 */       if (type == 512) {
///*      */
///*  223 */         sendGeneralResponse(channel, remoteAddress, id, type, index);
///*      */
///*  225 */         return decodeLocation(deviceSession, buf);
///*      */       }
///*  227 */       if (type == 21761 || type == 21762) {
///*      */
///*  229 */         if (BitUtil.check(attribute, 15)) {
///*  230 */           sendGeneralResponse2(channel, remoteAddress, id, type);
///*      */         }
///*      */
///*  233 */         return decodeLocation2(deviceSession, buf, type);
///*      */       }
///*  235 */       if (type == 1796 || type == 528) {
///*      */
///*  237 */         sendGeneralResponse(channel, remoteAddress, id, type, index);
///*      */
///*  239 */         return decodeLocationBatch(deviceSession, buf, type);
///*      */       }
///*  241 */       if (type == 265) {
///*      */
///*  243 */         if (channel != null) {
///*  244 */           Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
///*  245 */           ByteBuf response = Unpooled.buffer();
///*  246 */           response.writeShort(calendar.get(1));
///*  247 */           response.writeByte(calendar.get(2) + 1);
///*  248 */           response.writeByte(calendar.get(5));
///*  249 */           response.writeByte(calendar.get(11));
///*  250 */           response.writeByte(calendar.get(12));
///*  251 */           response.writeByte(calendar.get(13));
///*  252 */           channel.writeAndFlush(new NetworkMessage(
///*  253 */                 formatMessage(33024, id, false, response), remoteAddress));
///*      */         }
///*      */       } else {
///*  256 */         if (type == 8304) {
///*      */
///*  258 */           Position position = new Position(getProtocolName());
///*  259 */           position.setDeviceId(deviceSession.getDeviceId());
///*      */
///*  261 */           getLastLocation(position, null);
///*      */
///*  263 */           StringBuilder data = new StringBuilder("[");
///*  264 */           while (buf.readableBytes() > 2) {
///*  265 */             buf.skipBytes(6);
///*  266 */             if (data.length() > 1) {
///*  267 */               data.append(",");
///*      */             }
///*  269 */             data.append("[");
///*  270 */             data.append(readSignedWord(buf));
///*  271 */             data.append(",");
///*  272 */             data.append(readSignedWord(buf));
///*  273 */             data.append(",");
///*  274 */             data.append(readSignedWord(buf));
///*  275 */             data.append("]");
///*      */           }
///*  277 */           data.append("]");
///*      */
///*  279 */           position.set("gSensor", data.toString());
///*      */
///*  281 */           return position;
///*      */         }
///*  283 */         if (type == 2304) {
///*      */
///*  285 */           sendGeneralResponse(channel, remoteAddress, id, type, index);
///*      */
///*  287 */           return decodeTransparent(deviceSession, buf);
///*      */         }
///*      */       }
///*      */     }
///*  291 */     return null;
///*      */   }
///*      */
///*      */   private Position decodeResult(Channel channel, SocketAddress remoteAddress, String sentence) {
///*  295 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
///*  296 */     if (deviceSession != null) {
///*  297 */       Position position = new Position(getProtocolName());
///*  298 */       position.setDeviceId(deviceSession.getDeviceId());
///*  299 */       getLastLocation(position, null);
///*  300 */       position.set("result", sentence);
///*  301 */       return position;
///*      */     }
///*  303 */     return null;
///*      */   }
///*      */
///*      */   private void decodeExtension(Position position, ByteBuf buf, int endIndex) {
///*  307 */     while (buf.readerIndex() < endIndex) {
///*  308 */       int type = buf.readUnsignedByte();
///*  309 */       int length = buf.readUnsignedByte();
///*  310 */       switch (type) {
///*      */         case 1:
///*  312 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 100L));
///*      */           continue;
///*      */         case 2:
///*  315 */           position.set("fuel", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */           continue;
///*      */         case 3:
///*  318 */           position.set("obdSpeed", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */           continue;
///*      */         case 86:
///*  321 */           buf.readUnsignedByte();
///*  322 */           position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
///*      */           continue;
///*      */         case 97:
///*  325 */           position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */           continue;
///*      */         case 105:
///*  328 */           position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */           continue;
///*      */         case 128:
///*  331 */           position.set("obdSpeed", Short.valueOf(buf.readUnsignedByte()));
///*      */           continue;
///*      */         case 129:
///*  334 */           position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
///*      */           continue;
///*      */         case 130:
///*  337 */           position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */           continue;
///*      */         case 131:
///*  340 */           position.set("engineLoad", Short.valueOf(buf.readUnsignedByte()));
///*      */           continue;
///*      */         case 132:
///*  343 */           position.set("coolantTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
///*      */           continue;
///*      */         case 133:
///*  346 */           position.set("fuelConsumption", Integer.valueOf(buf.readUnsignedShort()));
///*      */           continue;
///*      */         case 134:
///*  349 */           position.set("intakeTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
///*      */           continue;
///*      */         case 135:
///*  352 */           position.set("intakeFlow", Integer.valueOf(buf.readUnsignedShort()));
///*      */           continue;
///*      */         case 136:
///*  355 */           position.set("intakePressure", Short.valueOf(buf.readUnsignedByte()));
///*      */           continue;
///*      */         case 137:
///*  358 */           position.set("throttle", Short.valueOf(buf.readUnsignedByte()));
///*      */           continue;
///*      */         case 139:
///*  361 */           position.set("vin", buf.readCharSequence(17, StandardCharsets.US_ASCII).toString());
///*      */           continue;
///*      */         case 140:
///*  364 */           position.set("obdOdometer", Long.valueOf(buf.readUnsignedInt() * 100L));
///*      */           continue;
///*      */         case 141:
///*  367 */           position.set("tripOdometer", Long.valueOf(buf.readUnsignedShort() * 1000L));
///*      */           continue;
///*      */         case 142:
///*  370 */           position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
///*      */           continue;
///*      */         case 204:
///*  373 */           position.set("iccid", buf.readCharSequence(20, StandardCharsets.US_ASCII).toString());
///*      */           continue;
///*      */       }
///*  376 */       buf.skipBytes(length);
///*      */     }
///*      */   }
///*      */
///*      */
///*      */
///*      */   private double decodeCustomDouble(ByteBuf buf) {
///*  383 */     int b1 = buf.readByte();
///*  384 */     int b2 = buf.readUnsignedByte();
///*  385 */     int sign = (b1 != 0) ? (b1 / Math.abs(b1)) : 1;
///*  386 */     return sign * (Math.abs(b1) + b2 / 255.0D);
///*      */   }
///*      */
///*      */
///*      */   private void decodeCoordinates(DeviceSession deviceSession, Position position, ByteBuf buf) {
///*  391 */     long status = buf.readUnsignedInt();
///*      */
///*  393 */     if (!"NatureLink".equals(getDeviceModel(deviceSession))) {
///*  394 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 0)));
///*  395 */       position.set("blocked", Boolean.valueOf(BitUtil.check(status, 10)));
///*  396 */       position.set("seal", Boolean.valueOf(BitUtil.check(status, 14)));
///*  397 */       position.set("shackle", Boolean.valueOf(BitUtil.check(status, 15)));
///*  398 */       position.set("charge", Boolean.valueOf(BitUtil.check(status, 26)));
///*      */
///*  400 */       position.set("disassemblyAlarm", Boolean.valueOf(BitUtil.check(status, 31)));
///*      */     }
///*      */
///*  403 */     position.setValid(BitUtil.check(status, 1));
///*      */
///*  405 */     double lat = buf.readUnsignedInt() * 1.0E-6D;
///*  406 */     double lon = buf.readUnsignedInt() * 1.0E-6D;
///*      */
///*  408 */     if (BitUtil.check(status, 2)) {
///*  409 */       position.setLatitude(-lat);
///*      */     } else {
///*  411 */       position.setLatitude(lat);
///*      */     }
///*      */
///*  414 */     if (BitUtil.check(status, 3)) {
///*  415 */       position.setLongitude(-lon);
///*      */     } else {
///*  417 */       position.setLongitude(lon);
///*      */     }
///*      */   }
///*      */
///*      */
///*      */   private Position decodeLocation(DeviceSession deviceSession, ByteBuf buf) {
///*  423 */     Position position = new Position(getProtocolName());
///*  424 */     position.setDeviceId(deviceSession.getDeviceId());
///*      */
///*  426 */     position.set("alarm", decodeAlarm(buf.readUnsignedInt()));
///*      */
///*  428 */     decodeCoordinates(deviceSession, position, buf);
///*      */
///*  430 */     position.setAltitude(buf.readShort());
///*  431 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1D));
///*  432 */     position.setCourse(buf.readUnsignedShort());
///*  433 */     position.setTime(readDate(buf, deviceSession.getTimeZone()));
///*      */
///*  435 */     if (buf.readableBytes() == 20) {
///*      */
///*  437 */       buf.skipBytes(4);
///*  438 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
///*  439 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*  440 */       buf.readUnsignedInt();
///*  441 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
///*  442 */       buf.skipBytes(3);
///*      */
///*  444 */       return position;
///*      */     }
///*      */
///*      */
///*  448 */     while (buf.readableBytes() > 2) {
///*      */       String stringValue; int alarm, event, i, adc1, adc2; long userStatus;
///*  450 */       int count, deviceStatus, j, mark, subtype = buf.readUnsignedByte();
///*  451 */       int length = buf.readUnsignedByte();
///*  452 */       int endIndex = buf.readerIndex() + length;
///*      */
///*  454 */       switch (subtype) {
///*      */         case 1:
///*  456 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 100L));
///*      */           break;
///*      */         case 2:
///*  459 */           position.set("fuel", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */           break;
///*      */         case 37:
///*  462 */           position.set("input", Long.valueOf(buf.readUnsignedInt()));
///*      */           break;
///*      */         case 43:
///*  465 */           position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
///*  466 */           position.set("adc2", Integer.valueOf(buf.readUnsignedShort()));
///*      */           break;
///*      */         case 48:
///*  469 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
///*      */           break;
///*      */         case 49:
///*  472 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
///*      */           break;
///*      */         case 51:
///*  475 */           stringValue = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
///*  476 */           if (stringValue.startsWith("*M00")) {
///*  477 */             String lockStatus = stringValue.substring(8, 15);
///*  478 */             position.set("battery", Double.valueOf(Integer.parseInt(lockStatus.substring(2, 5)) * 0.01D));
///*      */           }
///*      */           break;
///*      */         case 81:
///*  482 */           if (length == 16) {
///*  483 */             for (int k = 1; k <= 8; k++) {
///*  484 */               position.set("temp" + k, Short.valueOf(buf.readShort()));
///*      */             }
///*      */           }
///*      */           break;
///*      */         case 86:
///*  489 */           position.set("batteryLevel", Integer.valueOf(buf.readUnsignedByte() * 10));
///*  490 */           buf.readUnsignedByte();
///*      */           break;
///*      */         case 87:
///*  493 */           alarm = buf.readUnsignedShort();
///*  494 */           position.set("alarm", BitUtil.check(alarm, 8) ? "hardAcceleration" : null);
///*  495 */           position.set("alarm", BitUtil.check(alarm, 9) ? "hardBraking" : null);
///*  496 */           position.set("alarm", BitUtil.check(alarm, 10) ? "hardCornering" : null);
///*  497 */           buf.readUnsignedShort();
///*  498 */           buf.skipBytes(4);
///*      */           break;
///*      */         case 96:
///*  501 */           event = buf.readUnsignedShort();
///*  502 */           position.set("event", String.format("%04x", new Object[] { Integer.valueOf(event) }));
///*  503 */           if (event >= 0 && event <= 38) {
///*  504 */             buf.readUnsignedByte();
///*  505 */             if (length > 4) {
///*  506 */               position.set("eventData", buf
///*      */
///*  508 */                   .readCharSequence(length - 3, StandardCharsets.US_ASCII).toString());
///*      */             } else {
///*  510 */               buf.skipBytes(length - 3);
///*      */             }
///*      */           }
///*  513 */           if (event >= 97 && event <= 102) {
///*  514 */             buf.skipBytes(6);
///*  515 */             stringValue = buf.readCharSequence(8, StandardCharsets.US_ASCII).toString();
///*  516 */             position.set("driverUniqueId", stringValue);
///*      */           }
///*      */           break;
///*      */         case 99:
///*  520 */           for (i = 1; i <= length / 11; i++) {
///*  521 */             position.set("lock" + i + "Id", ByteBufUtil.hexDump(buf.readSlice(6)));
///*  522 */             position.set("lock" + i + "Battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
///*  523 */             position.set("lock" + i + "Seal", Boolean.valueOf((buf.readUnsignedByte() == 49)));
///*  524 */             buf.readUnsignedByte();
///*  525 */             buf.readUnsignedByte();
///*      */           }
///*      */           break;
///*      */         case 100:
///*  529 */           buf.readUnsignedInt();
///*  530 */           buf.readUnsignedByte();
///*  531 */           switch (buf.readUnsignedByte()) {
///*      */             case 1:
///*  533 */               position.set("adasAlarm", "FCW");
///*      */               break;
///*      */             case 2:
///*  536 */               position.set("adasAlarm", "LDW");
///*      */               break;
///*      */             case 3:
///*  539 */               position.set("adasAlarm", "HMW");
///*      */               break;
///*      */             case 4:
///*  542 */               position.set("adasAlarm", "PCW");
///*      */               break;
///*      */             case 5:
///*  545 */               position.set("adasAlarm", "FLD");
///*      */               break;
///*      */             case 6:
///*  548 */               position.set("adasAlarm", "RSOL");
///*      */               break;
///*      */             case 7:
///*  551 */               position.set("adasAlarm", "OBS");
///*      */               break;
///*      */           }
///*      */
///*      */           break;
///*      */
///*      */         case 101:
///*  558 */           buf.readUnsignedInt();
///*  559 */           buf.readUnsignedByte();
///*  560 */           switch (buf.readUnsignedByte()) {
///*      */             case 1:
///*  562 */               position.set("dmsAlarm", "Fatigue");
///*      */               break;
///*      */             case 2:
///*  565 */               position.set("dmsAlarm", "Call");
///*      */               break;
///*      */             case 3:
///*  568 */               position.set("dmsAlarm", "Smoke");
///*      */               break;
///*      */             case 4:
///*  571 */               position.set("dmsAlarm", "Distract");
///*      */               break;
///*      */             case 5:
///*  574 */               position.set("dmsAlarm", "Abnormal");
///*      */               break;
///*      */             case 7:
///*  577 */               position.set("dmsAlarm", "CamCover");
///*      */               break;
///*      */           }
///*      */
///*      */           break;
///*      */
///*      */         case 112:
///*  584 */           buf.readUnsignedInt();
///*  585 */           buf.readUnsignedByte();
///*  586 */           switch (buf.readUnsignedByte()) {
///*      */             case 1:
///*  588 */               position.set("alarm", "HA");
///*      */               break;
///*      */             case 2:
///*  591 */               position.set("alarm", "HB");
///*      */               break;
///*      */             case 3:
///*  594 */               position.set("alarm", "HC");
///*      */               break;
///*      */             case 22:
///*  597 */               position.set("alarm", "Impact");
///*      */               break;
///*      */             case 23:
///*  600 */               position.set("alarm", "Rollover");
///*      */               break;
///*      */           }
///*      */
///*      */           break;
///*      */
///*      */         case 105:
///*  607 */           position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */           break;
///*      */         case 128:
///*  610 */           buf.readUnsignedByte();
///*  611 */           endIndex = buf.writerIndex() - 2;
///*  612 */           decodeExtension(position, buf, endIndex);
///*      */           break;
///*      */         case 145:
///*  615 */           position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*  616 */           position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
///*  617 */           position.set("obdSpeed", Short.valueOf(buf.readUnsignedByte()));
///*  618 */           position.set("throttle", Integer.valueOf(buf.readUnsignedByte() * 100 / 255));
///*  619 */           position.set("engineLoad", Integer.valueOf(buf.readUnsignedByte() * 100 / 255));
///*  620 */           position.set("coolantTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
///*  621 */           buf.readUnsignedShort();
///*  622 */           position.set("fuelConsumption", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*  623 */           buf.readUnsignedShort();
///*  624 */           buf.readUnsignedInt();
///*  625 */           buf.readUnsignedShort();
///*  626 */           position.set("fuelUsed", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */           break;
///*      */         case 148:
///*  629 */           if (length > 0) {
///*  630 */             stringValue = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
///*  631 */             position.set("vin", stringValue);
///*      */           }
///*      */           break;
///*      */         case 167:
///*  635 */           adc1 = buf.readUnsignedShort();
///*  636 */           adc2 = buf.readUnsignedShort();
///*  637 */           position.set("fuel", Double.valueOf(adc1 * 0.001D));
///*  638 */           position.set("adc2", Integer.valueOf(adc2));
///*      */           break;
///*      */         case 172:
///*  641 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
///*      */           break;
///*      */         case 188:
///*  644 */           stringValue = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
///*  645 */           position.set("driver", stringValue.trim());
///*      */           break;
///*      */         case 189:
///*  648 */           stringValue = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
///*  649 */           position.set("driverUniqueId", stringValue);
///*      */           break;
///*      */         case 208:
///*  652 */           userStatus = buf.readUnsignedInt();
///*  653 */           if (BitUtil.check(userStatus, 3)) {
///*  654 */             position.set("alarm", "vibration");
///*      */           }
///*      */           break;
///*      */         case 211:
///*  658 */           position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */           break;
///*      */         case 212:
///*      */         case 225:
///*  662 */           if (length == 1) {
///*  663 */             position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte())); break;
///*      */           }
///*  665 */           position.set("driverUniqueId", String.valueOf(buf.readUnsignedInt()));
///*      */           break;
///*      */
///*      */         case 213:
///*  669 */           if (length == 2) {
///*  670 */             position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D)); break;
///*      */           }
///*  672 */           count = buf.readUnsignedByte();
///*  673 */           for (j = 1; j <= count; j++) {
///*  674 */             position.set("lock" + j + "Id", ByteBufUtil.hexDump(buf.readSlice(5)));
///*  675 */             position.set("lock" + j + "Card", ByteBufUtil.hexDump(buf.readSlice(5)));
///*  676 */             position.set("lock" + j + "Battery", Short.valueOf(buf.readUnsignedByte()));
///*  677 */             int lockStatus = buf.readUnsignedShort();
///*  678 */             if (BitUtil.check(lockStatus, 5)) {
///*  679 */               position.set("Seal_Status", "unlock");
///*      */             } else {
///*  681 */               position.set("Seal_Status", "lock");
///*      */             }
///*      */           }
///*      */           break;
///*      */
///*      */         case 218:
///*  687 */           buf.readUnsignedShort();
///*  688 */           deviceStatus = buf.readUnsignedByte();
///*  689 */           position.set("string", Boolean.valueOf(BitUtil.check(deviceStatus, 0)));
///*  690 */           position.set("motion", Boolean.valueOf(BitUtil.check(deviceStatus, 2)));
///*  691 */           position.set("cover", Boolean.valueOf(BitUtil.check(deviceStatus, 3)));
///*      */           break;
///*      */         case 226:
///*  694 */           position.set("fuel", Double.valueOf(buf.readUnsignedInt() * 0.1D));
///*      */           break;
///*      */         case 230:
///*  697 */           while (buf.readerIndex() < endIndex) {
///*  698 */             int sensorIndex = buf.readUnsignedByte();
///*  699 */             buf.skipBytes(6);
///*  700 */             position.set("temp" + sensorIndex, Double.valueOf(decodeCustomDouble(buf)));
///*  701 */             position.set("humidity" + sensorIndex, Double.valueOf(decodeCustomDouble(buf)));
///*      */           }
///*      */           break;
///*      */         case 235:
///*  705 */           if (buf.getUnsignedShort(buf.readerIndex()) > 200) {
///*  706 */             Network network = new Network();
///*  707 */             int mcc = buf.readUnsignedShort();
///*  708 */             int mnc = buf.readUnsignedByte();
///*  709 */             while (buf.readerIndex() < endIndex) {
///*  710 */               network.addCellTower(CellTower.from(mcc, mnc, buf
///*  711 */                     .readUnsignedShort(), buf.readUnsignedShort(), buf
///*  712 */                     .readUnsignedByte()));
///*      */             }
///*  714 */             position.setNetwork(network); break;
///*      */           }
///*  716 */           while (buf.readerIndex() < endIndex) {
///*  717 */             Network network; int extendedLength = buf.readUnsignedShort();
///*  718 */             int extendedType = buf.readUnsignedShort();
///*  719 */             switch (extendedType) {
///*      */               case 1:
///*  721 */                 position.set("fuel1", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*  722 */                 buf.readUnsignedByte();
///*      */                 continue;
///*      */               case 35:
///*  725 */                 position.set("fuel2", Double.valueOf(Double.parseDouble(buf
///*  726 */                         .readCharSequence(6, StandardCharsets.US_ASCII).toString())));
///*      */                 continue;
///*      */               case 206:
///*  729 */                 position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */                 continue;
///*      */               case 216:
///*  732 */                 network = new Network();
///*  733 */                 network.addCellTower(CellTower.from(buf
///*  734 */                       .readUnsignedShort(), buf.readUnsignedByte(), buf
///*  735 */                       .readUnsignedShort(), buf.readUnsignedInt()));
///*  736 */                 position.setNetwork(network);
///*      */                 continue;
///*      */               case 225:
///*  739 */                 position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
///*      */                 continue;
///*      */             }
///*  742 */             buf.skipBytes(extendedLength - 2);
///*      */           }
///*      */           break;
///*      */
///*      */
///*      */
///*      */         case 237:
///*  749 */           stringValue = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
///*  750 */           position.set("card", stringValue.trim());
///*      */           break;
///*      */         case 238:
///*  753 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
///*  754 */           position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
///*  755 */           position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
///*  756 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
///*      */           break;
///*      */         case 254:
///*  759 */           if (length == 1) {
///*  760 */             position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte())); break;
///*      */           }
///*  762 */           mark = buf.readUnsignedByte();
///*  763 */           if (mark == 124) {
///*  764 */             while (buf.readerIndex() < endIndex) {
///*  765 */               long alarms; int extendedType = buf.readUnsignedByte();
///*  766 */               int extendedLength = buf.readUnsignedByte();
///*  767 */               switch (extendedType) {
///*      */                 case 1:
///*  769 */                   alarms = buf.readUnsignedInt();
///*  770 */                   if (BitUtil.check(alarms, 0)) {
///*  771 */                     position.set("alarm", "hardAcceleration");
///*      */                   }
///*  773 */                   if (BitUtil.check(alarms, 1)) {
///*  774 */                     position.set("alarm", "hardBraking");
///*      */                   }
///*  776 */                   if (BitUtil.check(alarms, 2)) {
///*  777 */                     position.set("alarm", "hardCornering");
///*      */                   }
///*  779 */                   if (BitUtil.check(alarms, 3)) {
///*  780 */                     position.set("alarm", "accident");
///*      */                   }
///*  782 */                   if (BitUtil.check(alarms, 4)) {
///*  783 */                     position.set("alarm", "tampering");
///*      */                   }
///*      */                   continue;
///*      */               }
///*  787 */               buf.skipBytes(extendedLength);
///*      */             }
///*      */           }
///*      */
///*      */
///*  792 */           position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
///*      */           break;
///*      */       }
///*      */
///*      */
///*      */
///*  798 */       buf.readerIndex(endIndex);
///*      */     }
///*      */
///*  801 */     return position;
///*      */   }
///*      */
///*      */
///*      */   private Position decodeLocation2(DeviceSession deviceSession, ByteBuf buf, int type) {
///*  806 */     Position position = new Position(getProtocolName());
///*  807 */     position.setDeviceId(deviceSession.getDeviceId());
///*      */
///*  809 */     Jt600ProtocolDecoder.decodeBinaryLocation(buf, position);
///*  810 */     position.setValid((type != 21762));
///*      */
///*  812 */     position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
///*  813 */     position.set("sat", Short.valueOf(buf.readUnsignedByte()));
///*  814 */     position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
///*      */
///*  816 */     int battery = buf.readUnsignedByte();
///*  817 */     if (battery <= 100) {
///*  818 */       position.set("batteryLevel", Integer.valueOf(battery));
///*  819 */     } else if (battery == 170) {
///*  820 */       position.set("charge", Boolean.valueOf(true));
///*      */     }
///*      */
///*  823 */     position.setNetwork(new Network(CellTower.fromCidLac(buf.readUnsignedInt(), buf.readUnsignedShort())));
///*      */
///*  825 */     int product = buf.readUnsignedByte();
///*  826 */     int status = buf.readUnsignedShort();
///*  827 */     int alarm = buf.readUnsignedShort();
///*      */
///*  829 */     if (product == 1 || product == 2) {
///*  830 */       if (BitUtil.check(alarm, 0)) {
///*  831 */         position.set("alarm", "lowPower");
///*      */       }
///*  833 */     } else if (product == 3) {
///*  834 */       position.set("blocked", Boolean.valueOf(BitUtil.check(status, 5)));
///*  835 */       if (BitUtil.check(alarm, 1)) {
///*  836 */         position.set("alarm", "lowPower");
///*      */       }
///*  838 */       if (BitUtil.check(alarm, 2)) {
///*  839 */         position.set("alarm", "vibration");
///*      */       }
///*  841 */       if (BitUtil.check(alarm, 3)) {
///*  842 */         position.set("alarm", "lowBattery");
///*      */       }
///*      */     }
///*      */
///*  846 */     position.set("status", Integer.valueOf(status));
///*      */
///*  848 */     while (buf.readableBytes() > 2) {
///*  849 */       int x, y, z, id = buf.readUnsignedByte();
///*  850 */       int length = buf.readUnsignedByte();
///*  851 */       switch (id) {
///*      */         case 2:
///*  853 */           position.setAltitude(buf.readShort());
///*      */           continue;
///*      */         case 12:
///*  856 */           x = buf.readUnsignedShort();
///*  857 */           if (x > 32768) {
///*  858 */             x -= 65536;
///*      */           }
///*  860 */           y = buf.readUnsignedShort();
///*  861 */           if (y > 32768) {
///*  862 */             y -= 65536;
///*      */           }
///*  864 */           z = buf.readUnsignedShort();
///*  865 */           if (z > 32768) {
///*  866 */             z -= 65536;
///*      */           }
///*  868 */           position.set("tilt", String.format("[%d,%d,%d]", new Object[] { Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z) }));
///*  869 */           if (y <= -75) {
///*  870 */             position.set("gyro", "upright"); continue;
///*  871 */           }  if (-10 < x && x < 10 && -75 < y && y < -10) {
///*  872 */             position.set("gyro", "forward tilt"); continue;
///*  873 */           }  if ((170 < x || x < -170) && -75 < y && y < -10) {
///*  874 */             position.set("gyro", "back tilt"); continue;
///*  875 */           }  if (-170 < x && x < -10 && -75 < y && y < -10) {
///*  876 */             position.set("gyro", "left tilt"); continue;
///*  877 */           }  if (10 < x && x < 170 && -75 < y && y < -10) {
///*  878 */             position.set("gyro", "right tilt"); continue;
///*  879 */           }  if (-170 < x && x < -10 && -10 < y && y < 10) {
///*  880 */             position.set("gyro", "stand on the left"); continue;
///*  881 */           }  if (10 < x && x < 170 && -10 < y && y < 10) {
///*  882 */             position.set("gyro", "stand on the right"); continue;
///*  883 */           }  if (-10 < x && x < 10 && -10 < y && y < 10) {
///*  884 */             position.set("gyro", "lay flat face up"); continue;
///*  885 */           }  if ((170 < x || x < -170) && -10 < y && y < 10) {
///*  886 */             position.set("gyro", "lay flat face down"); continue;
///*  887 */           }  if (y > 10) {
///*  888 */             position.set("gyro", "handstand"); continue;
///*      */           }
///*  890 */           position.set("gyro", "undefined");
///*      */           continue;
///*      */       }
///*      */
///*  894 */       buf.skipBytes(length);
///*      */     }
///*      */
///*      */
///*      */
///*  899 */     return position;
///*      */   }
///*      */
///*      */
///*      */   private List<Position> decodeLocationBatch(DeviceSession deviceSession, ByteBuf buf, int type) {
///*  904 */     List<Position> positions = new LinkedList<>();
///*      */
///*  906 */     int locationType = 0;
///*  907 */     if (type == 1796) {
///*  908 */       buf.readUnsignedShort();
///*  909 */       locationType = buf.readUnsignedByte();
///*      */     }
///*      */
///*  912 */     while (buf.readableBytes() > 2) {
///*  913 */       int length = (type == 528) ? buf.readUnsignedByte() : buf.readUnsignedShort();
///*  914 */       ByteBuf fragment = buf.readSlice(length);
///*  915 */       Position position = decodeLocation(deviceSession, fragment);
///*  916 */       if (locationType > 0) {
///*  917 */         position.set("archive", Boolean.valueOf(true));
///*      */       }
///*  919 */       positions.add(position);
///*      */     }
///*      */
///*  922 */     return positions;
///*      */   }
///*      */
///*      */
///*      */   private Position decodeTransparent(DeviceSession deviceSession, ByteBuf buf) {
///*  927 */     int type = buf.readUnsignedByte();
///*      */
///*  929 */     if (type == 240) {
///*  930 */       int count, i; Position position = new Position(getProtocolName());
///*  931 */       position.setDeviceId(deviceSession.getDeviceId());
///*      */
///*  933 */       Date time = readDate(buf, deviceSession.getTimeZone());
///*      */
///*  935 */       if (buf.readUnsignedByte() > 0) {
///*  936 */         position.set("archive", Boolean.valueOf(true));
///*      */       }
///*      */
///*  939 */       buf.readUnsignedByte();
///*      */
///*      */
///*  942 */       int subtype = buf.readUnsignedByte();
///*  943 */       switch (subtype) {
///*      */         case 1:
///*  945 */           count = buf.readUnsignedByte();
///*  946 */           for (i = 0; i < count; i++) {
///*  947 */             int id = buf.readUnsignedShort();
///*  948 */             int length = buf.readUnsignedByte();
///*  949 */             switch (id) {
///*      */               case 258:
///*      */               case 1320:
///*      */               case 1350:
///*  953 */                 position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 100L));
///*      */                 break;
///*      */               case 259:
///*  956 */                 position.set("fuel", Double.valueOf(buf.readUnsignedInt() * 0.01D));
///*      */                 break;
///*      */               case 1322:
///*  959 */                 position.set("fuel", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */                 break;
///*      */               case 261:
///*      */               case 1324:
///*  963 */                 position.set("fuelUsed", Double.valueOf(buf.readUnsignedInt() * 0.01D));
///*      */                 break;
///*      */               case 330:
///*      */               case 1335:
///*      */               case 1336:
///*      */               case 1337:
///*  969 */                 position.set("fuelConsumption", Double.valueOf(buf.readUnsignedShort() * 0.01D));
///*      */                 break;
///*      */               case 1323:
///*  972 */                 position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
///*      */                 break;
///*      */               case 1325:
///*  975 */                 position.set("coolantTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
///*      */                 break;
///*      */               case 1326:
///*  978 */                 position.set("airTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
///*      */                 break;
///*      */               case 1328:
///*  981 */                 position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
///*      */                 break;
///*      */               case 1333:
///*  984 */                 position.set("obdSpeed", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */                 break;
///*      */               case 1334:
///*  987 */                 position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
///*      */                 break;
///*      */               case 1341:
///*  990 */                 position.set("intakePressure", Double.valueOf(buf.readUnsignedShort() * 0.1D));
///*      */                 break;
///*      */               case 1348:
///*  993 */                 position.set("liquidLevel", Short.valueOf(buf.readUnsignedByte()));
///*      */                 break;
///*      */               case 1351:
///*      */               case 1352:
///*  997 */                 position.set("throttle", Short.valueOf(buf.readUnsignedByte()));
///*      */                 break;
///*      */               default:
///* 1000 */                 switch (length) {
///*      */                   case 1:
///* 1002 */                     position.set("io" + id, Short.valueOf(buf.readUnsignedByte()));
///*      */                     break;
///*      */                   case 2:
///* 1005 */                     position.set("io" + id, Integer.valueOf(buf.readUnsignedShort()));
///*      */                     break;
///*      */                   case 4:
///* 1008 */                     position.set("io" + id, Long.valueOf(buf.readUnsignedInt()));
///*      */                     break;
///*      */                 }
///* 1011 */                 buf.skipBytes(length);
///*      */                 break;
///*      */             }
///*      */
///*      */
///*      */           }
///* 1017 */           decodeCoordinates(deviceSession, position, buf);
///* 1018 */           position.setTime(time);
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///*      */
///* 1061 */           return position;case 3: count = buf.readUnsignedByte(); for (i = 0; i < count; i++) { int id = buf.readUnsignedByte(); int length = buf.readUnsignedByte(); switch (id) { case 26: position.set("alarm", "hardAcceleration"); break;case 27: position.set("alarm", "hardBraking"); break;case 28: position.set("alarm", "hardCornering"); break;case 29: case 30: case 31: position.set("alarm", "laneChange"); break;case 35: position.set("alarm", "fatigueDriving"); break; }  buf.skipBytes(length); }  decodeCoordinates(deviceSession, position, buf); position.setTime(time); return position;case 11: if (buf.readUnsignedByte() > 0) position.set("vin", buf.readCharSequence(17, StandardCharsets.US_ASCII).toString());  getLastLocation(position, time); return position;
///*      */       }  return null;
///*      */     }
///* 1064 */     return null;
///*      */   }
///*      */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HuabaoProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */