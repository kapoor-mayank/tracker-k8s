///*     */ package org.traccar.protocol;
///*     */
///*     */ import io.netty.buffer.ByteBuf;
///*     */ import io.netty.buffer.Unpooled;
///*     */ import io.netty.channel.Channel;
///*     */ import java.net.SocketAddress;
///*     */ import java.nio.charset.StandardCharsets;
///*     */ import java.util.Date;
///*     */ import java.util.LinkedList;
///*     */ import java.util.List;
///*     */ import org.traccar.BaseProtocolDecoder;
///*     */ import org.traccar.DeviceSession;
///*     */ import org.traccar.NetworkMessage;
///*     */ import org.traccar.Protocol;
///*     */ import org.traccar.helper.BitUtil;
///*     */ import org.traccar.helper.Checksum;
///*     */ import org.traccar.helper.DateBuilder;
///*     */ import org.traccar.helper.UnitsConverter;
///*     */ import org.traccar.model.Position;
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */ public class NavisProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*  40 */   private static final int[] FLEX_FIELDS_SIZES = new int[] { 4, 2, 4, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 2, 4, 4, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 4, 4, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 4, 2, 1, 4, 2, 2, 2, 2, 2, 1, 1, 1, 2, 4, 2, 1, 8, 2, 1, 16, 4, 2, 4, 37, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 6, 12, 24, 48, 1, 1, 1, 1, 4, 4, 1, 4, 2, 6, 2, 6, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1 };
///*     */
///*     */   private String prefix;
///*     */
///*     */   private long deviceUniqueId;
///*     */
///*     */   private long serverId;
///*     */
///*     */   private int flexDataSize;
///*     */
///*     */   private int flexBitFieldSize;
///*     */
///*  52 */   private final byte[] flexBitField = new byte[16]; public static final int F10 = 1; public static final int F20 = 2; public static final int F30 = 3; public static final int F40 = 4;
///*     */
///*     */   public NavisProtocolDecoder(Protocol protocol) {
///*  55 */     super(protocol);
///*     */   }
///*     */
///*     */
///*     */   public static final int F50 = 5;
///*     */
///*     */   public static final int F51 = 21;
///*     */
///*     */   public static final int F52 = 37;
///*     */
///*     */   public static final int F60 = 6;
///*     */
///*     */   public int getFlexDataSize() {
///*  68 */     return this.flexDataSize;
///*     */   }
///*     */
///*     */   private static boolean isFormat(int type, int... types) {
///*  72 */     for (int i : types) {
///*  73 */       if (type == i) {
///*  74 */         return true;
///*     */       }
///*     */     }
///*  77 */     return false;
///*     */   }
///*     */   private Position parseNtcbPosition(DeviceSession deviceSession, ByteBuf buf) {
///*     */     int format;
///*  81 */     Position position = new Position(getProtocolName());
///*     */
///*  83 */     position.setDeviceId(deviceSession.getDeviceId());
///*     */
///*     */
///*  86 */     if (buf.getUnsignedByte(buf.readerIndex()) == 0) {
///*  87 */       format = buf.readUnsignedShortLE();
///*     */     } else {
///*  89 */       format = buf.readUnsignedByte();
///*     */     }
///*  91 */     position.set("format", Integer.valueOf(format));
///*     */
///*  93 */     position.set("index", Long.valueOf(buf.readUnsignedIntLE()));
///*  94 */     position.set("event", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */
///*  96 */     buf.skipBytes(6);
///*     */
///*  98 */     short armedStatus = buf.readUnsignedByte();
///*  99 */     if (isFormat(format, new int[] { 1, 2, 3, 4, 5, 21, 37 })) {
///* 100 */       position.set("armed", Integer.valueOf(BitUtil.to(armedStatus, 7)));
///* 101 */       if (BitUtil.check(armedStatus, 7)) {
///* 102 */         position.set("alarm", "general");
///*     */       }
///* 104 */     } else if (isFormat(format, new int[] { 6 })) {
///* 105 */       position.set("armed", Boolean.valueOf(BitUtil.check(armedStatus, 0)));
///* 106 */       if (BitUtil.check(armedStatus, 1)) {
///* 107 */         position.set("alarm", "general");
///*     */       }
///*     */     }
///*     */
///* 111 */     position.set("status", Short.valueOf(buf.readUnsignedByte()));
///* 112 */     position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
///*     */
///* 114 */     if (isFormat(format, new int[] { 1, 2, 3 })) {
///* 115 */       int output = buf.readUnsignedShortLE();
///* 116 */       position.set("output", Integer.valueOf(output));
///* 117 */       for (int i = 0; i < 16; i++) {
///* 118 */         position.set("out" + (i + 1), Boolean.valueOf(BitUtil.check(output, i)));
///*     */       }
///* 120 */     } else if (isFormat(format, new int[] { 5, 21, 37 })) {
///* 121 */       short extField = buf.readUnsignedByte();
///* 122 */       position.set("output", Integer.valueOf(BitUtil.to(extField, 2)));
///* 123 */       position.set("out1", Boolean.valueOf(BitUtil.check(extField, 0)));
///* 124 */       position.set("out2", Boolean.valueOf(BitUtil.check(extField, 1)));
///* 125 */       position.set("sat", Integer.valueOf(BitUtil.from(extField, 2)));
///* 126 */     } else if (isFormat(format, new int[] { 4, 6 })) {
///* 127 */       short output = buf.readUnsignedByte();
///* 128 */       position.set("output", Integer.valueOf(BitUtil.to(output, 4)));
///* 129 */       for (int i = 0; i < 4; i++) {
///* 130 */         position.set("out" + (i + 1), Boolean.valueOf(BitUtil.check(output, i)));
///*     */       }
///*     */     }
///*     */
///* 134 */     if (isFormat(format, new int[] { 1, 2, 3, 4 })) {
///* 135 */       int input = buf.readUnsignedShortLE();
///* 136 */       position.set("input", Integer.valueOf(input));
///* 137 */       if (!isFormat(format, new int[] { 4 })) {
///* 138 */         for (int i = 0; i < 16; i++) {
///* 139 */           position.set("in" + (i + 1), Boolean.valueOf(BitUtil.check(input, i)));
///*     */         }
///*     */       } else {
///* 142 */         position.set("in1", Boolean.valueOf(BitUtil.check(input, 0)));
///* 143 */         position.set("in2", Boolean.valueOf(BitUtil.check(input, 1)));
///* 144 */         position.set("in3", Boolean.valueOf(BitUtil.check(input, 2)));
///* 145 */         position.set("in4", Boolean.valueOf(BitUtil.check(input, 3)));
///* 146 */         position.set("in5", Integer.valueOf(BitUtil.between(input, 4, 7)));
///* 147 */         position.set("in6", Integer.valueOf(BitUtil.between(input, 7, 10)));
///* 148 */         position.set("in7", Integer.valueOf(BitUtil.between(input, 10, 12)));
///* 149 */         position.set("in8", Integer.valueOf(BitUtil.between(input, 12, 14)));
///*     */       }
///* 151 */     } else if (isFormat(format, new int[] { 5, 21, 37, 6 })) {
///* 152 */       short input = buf.readUnsignedByte();
///* 153 */       position.set("input", Short.valueOf(input));
///* 154 */       for (int i = 0; i < 8; i++) {
///* 155 */         position.set("in" + (i + 1), Boolean.valueOf(BitUtil.check(input, i)));
///*     */       }
///*     */     }
///*     */
///* 159 */     position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
///* 160 */     position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
///*     */
///* 162 */     if (isFormat(format, new int[] { 1, 2, 3 })) {
///* 163 */       position.set("temp1", Short.valueOf(buf.readShortLE()));
///*     */     }
///*     */
///* 166 */     if (isFormat(format, new int[] { 1, 2, 5, 21, 37, 6 })) {
///* 167 */       position.set("adc1", Integer.valueOf(buf.readUnsignedShortLE()));
///* 168 */       position.set("adc2", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */     }
///* 170 */     if (isFormat(format, new int[] { 6 })) {
///* 171 */       position.set("adc3", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */     }
///*     */
///*     */
///* 175 */     if (isFormat(format, new int[] { 2, 5, 21, 37, 6 })) {
///* 176 */       buf.readUnsignedIntLE();
///* 177 */       buf.readUnsignedIntLE();
///*     */     }
///*     */
///* 180 */     if (isFormat(format, new int[] { 6 })) {
///*     */
///* 182 */       buf.readUnsignedShortLE();
///* 183 */       buf.readUnsignedShortLE();
///* 184 */       buf.readByte();
///* 185 */       buf.readShortLE();
///* 186 */       buf.readByte();
///* 187 */       buf.readUnsignedShortLE();
///* 188 */       buf.readByte();
///* 189 */       buf.readUnsignedShortLE();
///* 190 */       buf.readByte();
///* 191 */       buf.readUnsignedShortLE();
///* 192 */       buf.readByte();
///* 193 */       buf.readUnsignedShortLE();
///* 194 */       buf.readByte();
///* 195 */       buf.readUnsignedShortLE();
///* 196 */       buf.readByte();
///* 197 */       buf.readUnsignedShortLE();
///* 198 */       buf.readByte();
///* 199 */       buf.readUnsignedShortLE();
///*     */
///* 201 */       position.set("temp1", Byte.valueOf(buf.readByte()));
///* 202 */       position.set("temp2", Byte.valueOf(buf.readByte()));
///* 203 */       position.set("temp3", Byte.valueOf(buf.readByte()));
///* 204 */       position.set("temp4", Byte.valueOf(buf.readByte()));
///* 205 */       position.set("axleWeight", Integer.valueOf(buf.readIntLE()));
///* 206 */       position.set("rpm", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */     }
///*     */
///* 209 */     if (isFormat(format, new int[] { 2, 5, 21, 37, 6 })) {
///* 210 */       int navSensorState = buf.readUnsignedByte();
///* 211 */       position.setValid(BitUtil.check(navSensorState, 1));
///* 212 */       if (isFormat(format, new int[] { 6 })) {
///* 213 */         position.set("sat", Integer.valueOf(BitUtil.from(navSensorState, 2)));
///*     */       }
///*     */
///*     */
///*     */
///* 218 */       DateBuilder dateBuilder = (new DateBuilder()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte() + 1, buf.readUnsignedByte());
///* 219 */       position.setTime(dateBuilder.getDate());
///*     */
///* 221 */       if (isFormat(format, new int[] { 6 })) {
///* 222 */         position.setLatitude(buf.readIntLE() / 600000.0D);
///* 223 */         position.setLongitude(buf.readIntLE() / 600000.0D);
///* 224 */         position.setAltitude(buf.readIntLE() * 0.1D);
///*     */       } else {
///* 226 */         position.setLatitude(buf.readFloatLE() / Math.PI * 180.0D);
///* 227 */         position.setLongitude(buf.readFloatLE() / Math.PI * 180.0D);
///*     */       }
///*     */
///* 230 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readFloatLE()));
///* 231 */       position.setCourse(buf.readUnsignedShortLE());
///*     */
///* 233 */       position.set("odometer", Float.valueOf(buf.readFloatLE() * 1000.0F));
///* 234 */       position.set("distance", Float.valueOf(buf.readFloatLE() * 1000.0F));
///*     */
///*     */
///* 237 */       buf.readUnsignedShortLE();
///* 238 */       buf.readUnsignedShortLE();
///*     */     }
///*     */
///*     */
///* 242 */     if (isFormat(format, new int[] { 21, 37 })) {
///* 243 */       buf.readUnsignedShortLE();
///* 244 */       buf.readByte();
///* 245 */       buf.readUnsignedShortLE();
///* 246 */       buf.readUnsignedShortLE();
///* 247 */       buf.readByte();
///* 248 */       buf.readUnsignedShortLE();
///* 249 */       buf.readUnsignedShortLE();
///* 250 */       buf.readByte();
///* 251 */       buf.readUnsignedShortLE();
///*     */     }
///*     */
///*     */
///* 255 */     if (isFormat(format, new int[] { 4, 37 })) {
///* 256 */       position.set("temp1", Byte.valueOf(buf.readByte()));
///* 257 */       position.set("temp2", Byte.valueOf(buf.readByte()));
///* 258 */       position.set("temp3", Byte.valueOf(buf.readByte()));
///* 259 */       position.set("temp4", Byte.valueOf(buf.readByte()));
///*     */     }
///*     */
///* 262 */     return position;
///*     */   }
///*     */
///*     */   private Object processNtcbSingle(DeviceSession deviceSession, Channel channel, ByteBuf buf) {
///* 266 */     Position position = parseNtcbPosition(deviceSession, buf);
///*     */
///* 268 */     ByteBuf response = Unpooled.buffer(7);
///* 269 */     response.writeCharSequence("*<T", StandardCharsets.US_ASCII);
///* 270 */     response.writeIntLE((int)position.getLong("index"));
///* 271 */     sendNtcbReply(channel, response);
///*     */
///* 273 */     return (position.getFixTime() != null) ? position : null;
///*     */   }
///*     */
///*     */   private Object processNtcbArray(DeviceSession deviceSession, Channel channel, ByteBuf buf) {
///* 277 */     List<Position> positions = new LinkedList<>();
///* 278 */     int count = buf.readUnsignedByte();
///*     */
///* 280 */     for (int i = 0; i < count; i++) {
///* 281 */       Position position = parseNtcbPosition(deviceSession, buf);
///* 282 */       if (position.getFixTime() != null) {
///* 283 */         positions.add(position);
///*     */       }
///*     */     }
///*     */
///* 287 */     ByteBuf response = Unpooled.buffer(7);
///* 288 */     response.writeCharSequence("*<A", StandardCharsets.US_ASCII);
///* 289 */     response.writeByte(count);
///* 290 */     sendNtcbReply(channel, response);
///*     */
///* 292 */     if (positions.isEmpty()) {
///* 293 */       return null;
///*     */     }
///*     */
///* 296 */     return positions;
///*     */   }
///*     */
///*     */   private boolean checkFlexBitfield(int index) {
///* 300 */     int byteIndex = Math.floorDiv(index, 8);
///* 301 */     int bitIndex = Math.floorMod(index, 8);
///* 302 */     return BitUtil.check(this.flexBitField[byteIndex], 7 - bitIndex);
///*     */   }
///*     */
///*     */
///*     */   private Position parseFlexPosition(DeviceSession deviceSession, ByteBuf buf) {
///* 307 */     Position position = new Position(getProtocolName());
///*     */
///* 309 */     position.setDeviceId(deviceSession.getDeviceId());
///*     */
///* 311 */     int status = 0;
///* 312 */     short input = 0;
///* 313 */     short output = 0;
///*     */
///* 315 */     for (int i = 0; i < this.flexBitFieldSize; i++) {
///* 316 */       if (checkFlexBitfield(i)) {
///*     */         short armedStatus; int status2; int navSensorState; int k; short input2; int j; short output2; int m;
///*     */         int satVisible;
///*     */         int n;
///* 320 */         switch (i) {
///*     */           case 0:
///* 322 */             position.set("index", Long.valueOf(buf.readUnsignedIntLE()));
///*     */             break;
///*     */           case 1:
///* 325 */             position.set("event", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */             break;
///*     */           case 3:
///* 328 */             armedStatus = buf.readUnsignedByte();
///* 329 */             position.set("armed", Boolean.valueOf(BitUtil.check(armedStatus, 0)));
///* 330 */             if (BitUtil.check(armedStatus, 1)) {
///* 331 */               position.set("alarm", "general");
///*     */             }
///*     */             break;
///*     */           case 4:
///* 335 */             status = buf.readUnsignedByte();
///* 336 */             position.set("status", Integer.valueOf(status));
///*     */             break;
///*     */           case 5:
///* 339 */             status2 = buf.readUnsignedByte();
///* 340 */             position.set("status", Short.valueOf((short)(BitUtil.to(status, 8) | status2 << 8)));
///*     */             break;
///*     */           case 6:
///* 343 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
///*     */             break;
///*     */           case 7:
///* 346 */             navSensorState = buf.readUnsignedByte();
///* 347 */             position.setValid(BitUtil.check(navSensorState, 1));
///* 348 */             position.set("sat", Integer.valueOf(BitUtil.from(navSensorState, 2)));
///*     */             break;
///*     */           case 8:
///* 351 */             position.setTime((new DateBuilder(new Date(buf.readUnsignedIntLE() * 1000L))).getDate());
///*     */             break;
///*     */           case 9:
///* 354 */             position.setLatitude(buf.readIntLE() / 600000.0D);
///*     */             break;
///*     */           case 10:
///* 357 */             position.setLongitude(buf.readIntLE() / 600000.0D);
///*     */             break;
///*     */           case 11:
///* 360 */             position.setAltitude(buf.readIntLE() * 0.1D);
///*     */             break;
///*     */           case 12:
///* 363 */             position.setSpeed(UnitsConverter.knotsFromKph(buf.readFloatLE()));
///*     */             break;
///*     */           case 13:
///* 366 */             position.setCourse(buf.readUnsignedShortLE());
///*     */             break;
///*     */           case 14:
///* 369 */             position.set("odometer", Float.valueOf(buf.readFloatLE() * 1000.0F));
///*     */             break;
///*     */           case 15:
///* 372 */             position.set("distance", Float.valueOf(buf.readFloatLE() * 1000.0F));
///*     */             break;
///*     */           case 18:
///* 375 */             position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
///*     */             break;
///*     */           case 19:
///* 378 */             position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
///*     */             break;
///*     */           case 20:
///*     */           case 21:
///*     */           case 22:
///*     */           case 23:
///*     */           case 24:
///*     */           case 25:
///*     */           case 26:
///*     */           case 27:
///* 388 */             position.set("adc" + (i - 19), Integer.valueOf(buf.readUnsignedShortLE()));
///*     */             break;
///*     */           case 28:
///* 391 */             input = buf.readUnsignedByte();
///* 392 */             position.set("input", Short.valueOf(input));
///* 393 */             for (k = 0; k < 8; k++) {
///* 394 */               position.set("in" + (k + 1), Boolean.valueOf(BitUtil.check(input, k)));
///*     */             }
///*     */             break;
///*     */           case 29:
///* 398 */             input2 = buf.readUnsignedByte();
///* 399 */             position.set("input", Short.valueOf((short)(BitUtil.to(input, 8) | input2 << 8)));
///* 400 */             for (j = 0; j < 8; j++) {
///* 401 */               position.set("in" + (j + 9), Boolean.valueOf(BitUtil.check(input2, j)));
///*     */             }
///*     */             break;
///*     */           case 30:
///* 405 */             output = buf.readUnsignedByte();
///* 406 */             position.set("output", Short.valueOf(output));
///* 407 */             for (j = 0; j < 8; j++) {
///* 408 */               position.set("out" + (j + 1), Boolean.valueOf(BitUtil.check(output, j)));
///*     */             }
///*     */             break;
///*     */           case 31:
///* 412 */             output2 = buf.readUnsignedByte();
///* 413 */             position.set("output", Short.valueOf((short)(BitUtil.to(output, 8) | output2 << 8)));
///* 414 */             for (m = 0; m < 8; m++) {
///* 415 */               position.set("out" + (m + 9), Boolean.valueOf(BitUtil.check(output2, m)));
///*     */             }
///*     */             break;
///*     */           case 36:
///* 419 */             position.set("hours", Long.valueOf(buf.readUnsignedIntLE() * 1000L));
///*     */             break;
///*     */           case 44:
///*     */           case 45:
///*     */           case 46:
///*     */           case 47:
///*     */           case 48:
///*     */           case 49:
///*     */           case 50:
///*     */           case 51:
///* 429 */             position.set("temp" + (i - 43), Byte.valueOf(buf.readByte()));
///*     */             break;
///*     */           case 68:
///* 432 */             position.set("can-speed", Short.valueOf(buf.readUnsignedByte()));
///*     */             break;
///*     */
///*     */           case 69:
///* 436 */             satVisible = 0;
///* 437 */             for (n = 0; n < 8; n++) {
///* 438 */               satVisible += buf.readUnsignedByte();
///*     */             }
///* 440 */             position.set("satVisible", Integer.valueOf(satVisible));
///*     */             break;
///*     */           case 70:
///* 443 */             position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
///* 444 */             position.set("pdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
///*     */             break;
///*     */           default:
///* 447 */             if (i < FLEX_FIELDS_SIZES.length) {
///* 448 */               buf.skipBytes(FLEX_FIELDS_SIZES[i]);
///*     */             }
///*     */             break;
///*     */         }
///*     */       }
///*     */     }
///* 454 */     return position;
///*     */   }
///*     */
///*     */
///*     */   private Position parseFlex20Position(DeviceSession deviceSession, ByteBuf buf) {
///* 459 */     Position position = new Position(getProtocolName());
///* 460 */     position.setDeviceId(deviceSession.getDeviceId());
///*     */
///* 462 */     int length = buf.readUnsignedShort();
///* 463 */     if (length <= buf.readableBytes() && buf.readUnsignedByte() == 10) {
///*     */
///* 465 */       buf.readUnsignedByte();
///*     */
///* 467 */       position.set("index", Long.valueOf(buf.readUnsignedIntLE()));
///*     */
///* 469 */       position.set("event", Integer.valueOf(buf.readUnsignedShortLE()));
///* 470 */       buf.readUnsignedInt();
///*     */
///* 472 */       int navSensorState = buf.readUnsignedByte();
///* 473 */       position.setValid(BitUtil.check(navSensorState, 1));
///* 474 */       position.set("sat", Integer.valueOf(BitUtil.from(navSensorState, 2)));
///*     */
///* 476 */       position.setTime((new DateBuilder(new Date(buf.readUnsignedIntLE() * 1000L))).getDate());
///* 477 */       position.setLatitude(buf.readIntLE() / 600000.0D);
///* 478 */       position.setLongitude(buf.readIntLE() / 600000.0D);
///* 479 */       position.setAltitude(buf.readIntLE() * 0.1D);
///* 480 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readFloatLE()));
///* 481 */       position.setCourse(buf.readUnsignedShortLE());
///* 482 */       position.set("odometer", Float.valueOf(buf.readFloatLE() * 1000.0F));
///*     */
///* 484 */       buf.skipBytes(length - buf.readerIndex() - 1);
///*     */     }
///*     */
///* 487 */     return position;
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */   private Object processFlexSingle(FlexPositionParser parser, String flexHeader, DeviceSession deviceSession, Channel channel, ByteBuf buf) {
///* 497 */     if (!flexHeader.equals("~C")) {
///* 498 */       buf.readUnsignedInt();
///*     */     }
///*     */
///* 501 */     Position position = parser.parsePosition(deviceSession, buf);
///*     */
///* 503 */     ByteBuf response = Unpooled.buffer();
///* 504 */     response.writeCharSequence(flexHeader, StandardCharsets.US_ASCII);
///* 505 */     response.writeIntLE((int)position.getLong("index"));
///* 506 */     sendFlexReply(channel, response);
///*     */
///* 508 */     return (position.getFixTime() != null) ? position : null;
///*     */   }
///*     */
///*     */
///*     */
///*     */   private Object processFlexArray(FlexPositionParser parser, String flexHeader, DeviceSession deviceSession, Channel channel, ByteBuf buf) {
///* 514 */     List<Position> positions = new LinkedList<>();
///* 515 */     int count = buf.readUnsignedByte();
///*     */
///* 517 */     for (int i = 0; i < count; i++) {
///* 518 */       Position position = parser.parsePosition(deviceSession, buf);
///* 519 */       if (position.getFixTime() != null) {
///* 520 */         positions.add(position);
///*     */       }
///*     */     }
///*     */
///* 524 */     ByteBuf response = Unpooled.buffer();
///* 525 */     response.writeCharSequence(flexHeader, StandardCharsets.US_ASCII);
///* 526 */     response.writeByte(count);
///* 527 */     sendFlexReply(channel, response);
///*     */
///* 529 */     return !positions.isEmpty() ? positions : null;
///*     */   }
///*     */
///*     */   private Object processFlexNegotiation(Channel channel, ByteBuf buf) {
///* 533 */     if ((byte)buf.readUnsignedByte() != -80) {
///* 534 */       return null;
///*     */     }
///*     */
///* 537 */     short flexProtocolVersion = buf.readUnsignedByte();
///* 538 */     short flexStructVersion = buf.readUnsignedByte();
///* 539 */     if ((flexProtocolVersion == 10 || flexProtocolVersion == 20) && (flexStructVersion == 10 || flexStructVersion == 20)) {
///*     */
///*     */
///* 542 */       this.flexBitFieldSize = buf.readUnsignedByte();
///* 543 */       if (this.flexBitFieldSize > 122) {
///* 544 */         return null;
///*     */       }
///*     */
///* 547 */       buf.readBytes(this.flexBitField, 0, (int)Math.ceil(this.flexBitFieldSize / 8.0D));
///*     */
///* 549 */       this.flexDataSize = 0;
///* 550 */       for (int i = 0; i < this.flexBitFieldSize; i++) {
///* 551 */         if (checkFlexBitfield(i)) {
///* 552 */           this.flexDataSize += FLEX_FIELDS_SIZES[i];
///*     */         }
///*     */       }
///*     */     } else {
///* 556 */       flexProtocolVersion = 20;
///* 557 */       flexStructVersion = 20;
///*     */     }
///*     */
///* 560 */     ByteBuf response = Unpooled.buffer(9);
///* 561 */     response.writeCharSequence("*<FLEX", StandardCharsets.US_ASCII);
///* 562 */     response.writeByte(176);
///* 563 */     response.writeByte(flexProtocolVersion);
///* 564 */     response.writeByte(flexStructVersion);
///* 565 */     sendNtcbReply(channel, response);
///*     */
///* 567 */     return null;
///*     */   }
///*     */
///*     */   private Object processHandshake(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
///* 571 */     buf.readByte();
///* 572 */     if (getDeviceSession(channel, remoteAddress, new String[] { buf.toString(StandardCharsets.US_ASCII) }) != null) {
///* 573 */       sendNtcbReply(channel, Unpooled.copiedBuffer("*<S", StandardCharsets.US_ASCII));
///*     */     }
///* 575 */     return null;
///*     */   }
///*     */
///*     */   private void sendNtcbReply(Channel channel, ByteBuf data) {
///* 579 */     if (channel != null) {
///* 580 */       ByteBuf header = Unpooled.buffer(16);
///* 581 */       header.writeCharSequence(this.prefix, StandardCharsets.US_ASCII);
///* 582 */       header.writeIntLE((int)this.deviceUniqueId);
///* 583 */       header.writeIntLE((int)this.serverId);
///* 584 */       header.writeShortLE(data.readableBytes());
///* 585 */       header.writeByte(Checksum.xor(data.nioBuffer()));
///* 586 */       header.writeByte(Checksum.xor(header.nioBuffer()));
///*     */
///* 588 */       channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(new ByteBuf[] { header, data }, ), channel.remoteAddress()));
///*     */     }
///*     */   }
///*     */
///*     */   private void sendFlexReply(Channel channel, ByteBuf data) {
///* 593 */     if (channel != null) {
///* 594 */       ByteBuf cs = Unpooled.buffer(1);
///* 595 */       cs.writeByte(Checksum.crc8(new Checksum.Algorithm(8, 49, 255, false, false, 0), data.nioBuffer()));
///*     */
///* 597 */       channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(new ByteBuf[] { data, cs }, ), channel.remoteAddress()));
///*     */     }
///*     */   }
///*     */
///*     */
///*     */   private Object decodeNtcb(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
///* 603 */     this.prefix = buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII);
///* 604 */     buf.skipBytes(this.prefix.length());
///* 605 */     this.serverId = buf.readUnsignedIntLE();
///* 606 */     this.deviceUniqueId = buf.readUnsignedIntLE();
///* 607 */     int length = buf.readUnsignedShortLE();
///* 608 */     buf.skipBytes(2);
///*     */
///* 610 */     if (length == 0) {
///* 611 */       return null;
///*     */     }
///*     */
///* 614 */     String type = buf.toString(buf.readerIndex(), 3, StandardCharsets.US_ASCII);
///* 615 */     buf.skipBytes(type.length());
///*     */
///* 617 */     if (type.equals("*>S")) {
///* 618 */       return processHandshake(channel, remoteAddress, buf);
///*     */     }
///* 620 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
///* 621 */     if (deviceSession != null) {
///* 622 */       switch (type) {
///*     */         case "*>A":
///* 624 */           return processNtcbArray(deviceSession, channel, buf);
///*     */         case "*>T":
///* 626 */           return processNtcbSingle(deviceSession, channel, buf);
///*     */         case "*>F":
///* 628 */           buf.skipBytes(3);
///* 629 */           return processFlexNegotiation(channel, buf);
///*     */       }
///*     */
///*     */
///*     */
///*     */
///*     */     }
///* 636 */     return null;
///*     */   }
///*     */
///*     */
///*     */   private Object decodeFlex(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
///* 641 */     if (buf.getByte(buf.readerIndex()) == Byte.MAX_VALUE) {
///* 642 */       return null;
///*     */     }
///*     */
///* 645 */     String type = buf.toString(buf.readerIndex(), 2, StandardCharsets.US_ASCII);
///* 646 */     buf.skipBytes(type.length());
///*     */
///* 648 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
///* 649 */     if (deviceSession != null) {
///* 650 */       switch (type) {
///*     */
///*     */         case "~A":
///* 653 */           return processFlexArray(this::parseFlexPosition, type, deviceSession, channel, buf);
///*     */         case "~T":
///*     */         case "~C":
///* 656 */           return processFlexSingle(this::parseFlexPosition, type, deviceSession, channel, buf);
///*     */
///*     */         case "~E":
///* 659 */           return processFlexArray(this::parseFlex20Position, type, deviceSession, channel, buf);
///*     */         case "~X":
///* 661 */           return processFlexSingle(this::parseFlex20Position, type, deviceSession, channel, buf);
///*     */       }
///*     */
///*     */
///*     */
///*     */     }
///* 667 */     return null;
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///* 674 */     ByteBuf buf = (ByteBuf)msg;
///*     */
///* 676 */     if (this.flexDataSize > 0) {
///* 677 */       return decodeFlex(channel, remoteAddress, buf);
///*     */     }
///* 679 */     return decodeNtcb(channel, remoteAddress, buf);
///*     */   }
///*     */
///*     */   private static interface FlexPositionParser {
///*     */     Position parsePosition(DeviceSession param1DeviceSession, ByteBuf param1ByteBuf);
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NavisProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */