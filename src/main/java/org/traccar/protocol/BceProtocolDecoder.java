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
///*     */ import org.traccar.helper.UnitsConverter;
///*     */ import org.traccar.model.CellTower;
///*     */ import org.traccar.model.Network;
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
///*     */ public class BceProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*     */   private static final int DATA_TYPE = 7;
///*     */   public static final int MSG_ASYNC_STACK = 165;
///*     */   public static final int MSG_STACK_COFIRM = 25;
///*     */   public static final int MSG_TIME_TRIGGERED = 160;
///*     */   public static final int MSG_OUTPUT_CONTROL = 65;
///*     */   public static final int MSG_OUTPUT_CONTROL_ACK = 193;
///*     */
///*     */   public BceProtocolDecoder(Protocol protocol) {
///*  41 */     super(protocol);
///*     */   }
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
///*     */   private void decodeMask1(ByteBuf buf, int mask, Position position) {
///*  54 */     if (BitUtil.check(mask, 0)) {
///*  55 */       position.setValid(true);
///*  56 */       position.setLongitude(buf.readFloatLE());
///*  57 */       position.setLatitude(buf.readFloatLE());
///*  58 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
///*     */
///*  60 */       int status = buf.readUnsignedByte();
///*  61 */       position.set("sat", Integer.valueOf(BitUtil.to(status, 4)));
///*  62 */       position.set("hdop", Integer.valueOf(BitUtil.from(status, 4)));
///*     */
///*  64 */       position.setCourse((buf.readUnsignedByte() * 2));
///*  65 */       position.setAltitude(buf.readUnsignedShortLE());
///*     */
///*  67 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
///*     */     }
///*     */
///*  70 */     if (BitUtil.check(mask, 1)) {
///*  71 */       int digitalInputsStatus = buf.readUnsignedShortLE();
///*  72 */       for (int j = 0; j < 16; j++) {
///*  73 */         position.set("in" + (j + 1), Boolean.valueOf(BitUtil.check(digitalInputsStatus, j)));
///*     */       }
///*  75 */       position.set("ignition", Boolean.valueOf(!BitUtil.check(digitalInputsStatus, 4)));
///*     */     }
///*     */
///*  78 */     for (int i = 1; i <= 8; i++) {
///*  79 */       if (BitUtil.check(mask, i + 1)) {
///*  80 */         position.set("adc" + i, Integer.valueOf(buf.readUnsignedShortLE()));
///*     */       }
///*     */     }
///*     */
///*  84 */     if (BitUtil.check(mask, 10)) {
///*  85 */       buf.skipBytes(4);
///*     */     }
///*  87 */     if (BitUtil.check(mask, 11)) {
///*  88 */       buf.skipBytes(4);
///*     */     }
///*  90 */     if (BitUtil.check(mask, 12)) {
///*  91 */       position.set("fuel1", Integer.valueOf(buf.readUnsignedShort()));
///*     */     }
///*  93 */     if (BitUtil.check(mask, 13)) {
///*  94 */       position.set("fuel2", Integer.valueOf(buf.readUnsignedShort()));
///*     */     }
///*     */
///*  97 */     if (BitUtil.check(mask, 14)) {
///*  98 */       int mcc = buf.readUnsignedShortLE();
///*  99 */       int mnc = buf.readUnsignedByte();
///* 100 */       int lac = buf.readUnsignedShortLE();
///* 101 */       int cid = buf.readUnsignedShortLE();
///* 102 */       buf.readUnsignedByte();
///* 103 */       int rssi = -buf.readUnsignedByte();
///* 104 */       position.set("rssi", Integer.valueOf(rssi));
///* 105 */       position.setNetwork(new Network(CellTower.from(mcc, mnc, lac, cid, rssi)));
///*     */     }
///*     */   }
///*     */
///*     */
///*     */   private void decodeMask2(ByteBuf buf, int mask, Position position) {
///* 111 */     if (BitUtil.check(mask, 0)) {
///* 112 */       buf.readUnsignedShortLE();
///*     */     }
///* 114 */     if (BitUtil.check(mask, 1)) {
///* 115 */       buf.readUnsignedByte();
///*     */     }
///* 117 */     if (BitUtil.check(mask, 2)) {
///* 118 */       position.set("fuelUsed", Double.valueOf(buf.readUnsignedIntLE() * 0.5D));
///*     */     }
///* 120 */     if (BitUtil.check(mask, 3)) {
///* 121 */       position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
///*     */     }
///* 123 */     if (BitUtil.check(mask, 4)) {
///* 124 */       position.set("rpm", Integer.valueOf((int)(buf.readUnsignedShortLE() * 0.125D)));
///*     */     }
///* 126 */     if (BitUtil.check(mask, 5)) {
///* 127 */       position.set("hours", Long.valueOf(buf.readUnsignedIntLE()));
///*     */     }
///* 129 */     if (BitUtil.check(mask, 6)) {
///* 130 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
///*     */     }
///* 132 */     if (BitUtil.check(mask, 7)) {
///* 133 */       position.set("coolantTemp", Integer.valueOf(buf.readByte() - 40));
///*     */     }
///* 135 */     if (BitUtil.check(mask, 8)) {
///* 136 */       position.set("fuel2", Short.valueOf(buf.readUnsignedByte()));
///*     */     }
///* 138 */     if (BitUtil.check(mask, 9)) {
///* 139 */       position.set("engineLoad", Short.valueOf(buf.readUnsignedByte()));
///*     */     }
///* 141 */     if (BitUtil.check(mask, 10)) {
///* 142 */       position.set("serviceOdometer", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */     }
///* 144 */     if (BitUtil.check(mask, 11)) {
///* 145 */       buf.skipBytes(8);
///*     */     }
///* 147 */     if (BitUtil.check(mask, 12)) {
///* 148 */       buf.readUnsignedShortLE();
///*     */     }
///* 150 */     if (BitUtil.check(mask, 13)) {
///* 151 */       buf.skipBytes(8);
///*     */     }
///* 153 */     if (BitUtil.check(mask, 14)) {
///* 154 */       position.set("fuelConsumption", Integer.valueOf(buf.readUnsignedShortLE()));
///*     */     }
///*     */   }
///*     */
///*     */
///*     */   private void decodeMask3(ByteBuf buf, int mask, Position position) {
///* 160 */     if (BitUtil.check(mask, 0)) {
///* 161 */       buf.readUnsignedShortLE();
///*     */     }
///* 163 */     if (BitUtil.check(mask, 1)) {
///* 164 */       position.set("fuelConsumption", Long.valueOf(buf.readUnsignedIntLE()));
///*     */     }
///* 166 */     if (BitUtil.check(mask, 2)) {
///* 167 */       position.set("axleWeight", Integer.valueOf(buf.readUnsignedMediumLE()));
///*     */     }
///* 169 */     if (BitUtil.check(mask, 3)) {
///* 170 */       buf.readUnsignedByte();
///*     */     }
///* 172 */     if (BitUtil.check(mask, 4)) {
///* 173 */       buf.skipBytes(20);
///*     */     }
///* 175 */     if (BitUtil.check(mask, 5)) {
///* 176 */       buf.readUnsignedShortLE();
///*     */     }
///* 178 */     if (BitUtil.check(mask, 6)) {
///* 179 */       position.set("driverUniqueId", String.valueOf(buf.readLongLE()));
///*     */     }
///* 181 */     if (BitUtil.check(mask, 7)) {
///* 182 */       position.set("temp1", Double.valueOf(buf.readUnsignedShortLE() * 0.1D - 273.0D));
///*     */     }
///* 184 */     if (BitUtil.check(mask, 8)) {
///* 185 */       buf.readUnsignedShortLE();
///*     */     }
///* 187 */     if (BitUtil.check(mask, 9)) {
///* 188 */       position.set("fuel1", Integer.valueOf(buf.readUnsignedShortLE()));
///* 189 */       position.set("fuelTemp1", Integer.valueOf(buf.readByte()));
///* 190 */       position.set("fuel2", Integer.valueOf(buf.readUnsignedShortLE()));
///* 191 */       position.set("fuelTemp2", Integer.valueOf(buf.readByte()));
///*     */     }
///* 193 */     if (BitUtil.check(mask, 10)) {
///* 194 */       position.set("fuel3", Integer.valueOf(buf.readUnsignedShortLE()));
///* 195 */       position.set("fuelTemp3", Integer.valueOf(buf.readByte()));
///* 196 */       position.set("fuel4", Integer.valueOf(buf.readUnsignedShortLE()));
///* 197 */       position.set("fuelTemp4", Integer.valueOf(buf.readByte()));
///*     */     }
///* 199 */     if (BitUtil.check(mask, 11)) {
///* 200 */       buf.skipBytes(21);
///*     */     }
///* 202 */     if (BitUtil.check(mask, 12)) {
///* 203 */       buf.skipBytes(20);
///*     */     }
///* 205 */     if (BitUtil.check(mask, 13)) {
///* 206 */       buf.skipBytes(9);
///*     */     }
///* 208 */     if (BitUtil.check(mask, 14)) {
///* 209 */       buf.skipBytes(21);
///*     */     }
///*     */   }
///*     */
///*     */
///*     */   private void decodeMask4(ByteBuf buf, int mask, Position position) {
///* 215 */     if (BitUtil.check(mask, 0)) {
///* 216 */       buf.readUnsignedIntLE();
///*     */     }
///* 218 */     if (BitUtil.check(mask, 1)) {
///* 219 */       buf.skipBytes(30);
///*     */     }
///* 221 */     if (BitUtil.check(mask, 2)) {
///* 222 */       buf.readUnsignedIntLE();
///*     */     }
///* 224 */     if (BitUtil.check(mask, 3)) {
///* 225 */       buf.skipBytes(10);
///*     */     }
///* 227 */     if (BitUtil.check(mask, 4)) {
///* 228 */       buf.readUnsignedByte();
///*     */     }
///* 230 */     if (BitUtil.check(mask, 5)) {
///* 231 */       buf.readUnsignedShortLE();
///*     */     }
///* 233 */     if (BitUtil.check(mask, 6)) {
///* 234 */       position.set("maxAcceleration", Double.valueOf(buf.readUnsignedByte() * 0.02D));
///* 235 */       position.set("maxBraking", Double.valueOf(buf.readUnsignedByte() * 0.02D));
///* 236 */       position.set("maxCornering", Double.valueOf(buf.readUnsignedByte() * 0.02D));
///*     */     }
///* 238 */     if (BitUtil.check(mask, 7)) {
///* 239 */       buf.skipBytes(16);
///*     */     }
///* 241 */     if (BitUtil.check(mask, 8)) {
///* 242 */       for (int i = 1; i <= 4; i++) {
///* 243 */         int temperature = buf.readUnsignedShortLE();
///* 244 */         if (temperature > 0) {
///* 245 */           position.set("temp" + i, Double.valueOf(temperature * 0.1D - 273.0D));
///*     */         }
///* 247 */         buf.skipBytes(8);
///*     */       }
///*     */     }
///* 250 */     if (BitUtil.check(mask, 9)) {
///* 251 */       position.set("driver1", buf.readCharSequence(16, StandardCharsets.US_ASCII).toString().trim());
///* 252 */       position.set("driver2", buf.readCharSequence(16, StandardCharsets.US_ASCII).toString().trim());
///*     */     }
///* 254 */     if (BitUtil.check(mask, 10)) {
///* 255 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
///*     */     }
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///* 263 */     ByteBuf buf = (ByteBuf)msg;
///*     */
///* 265 */     String imei = String.format("%015d", new Object[] { Long.valueOf(buf.readLongLE()) });
///* 266 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
///* 267 */     if (deviceSession == null) {
///* 268 */       return null;
///*     */     }
///*     */
///* 271 */     List<Position> positions = new LinkedList<>();
///*     */
///* 273 */     while (buf.readableBytes() > 1) {
///*     */
///* 275 */       int dataEnd = buf.readUnsignedShortLE() + buf.readerIndex();
///* 276 */       int type = buf.readUnsignedByte();
///*     */
///* 278 */       if (type != 165 && type != 160) {
///* 279 */         return null;
///*     */       }
///*     */
///* 282 */       int confirmKey = buf.readUnsignedByte() & 0x7F;
///*     */
///* 284 */       while (buf.readerIndex() < dataEnd) {
///*     */
///* 286 */         Position position = new Position(getProtocolName());
///* 287 */         position.setDeviceId(deviceSession.getDeviceId());
///*     */
///* 289 */         int structEnd = buf.readUnsignedByte() + buf.readerIndex();
///*     */
///* 291 */         long time = buf.readUnsignedIntLE();
///* 292 */         if ((time & 0xFL) == 7L) {
///*     */
///* 294 */           time = time >> 4L << 1L;
///* 295 */           time += 1199145600L;
///* 296 */           position.setTime(new Date(time * 1000L));
///*     */
///*     */
///*     */
///* 300 */           List<Integer> masks = new LinkedList<>();
///*     */           do {
///* 302 */             mask = buf.readUnsignedShortLE();
///* 303 */             masks.add(Integer.valueOf(mask));
///* 304 */           } while (BitUtil.check(mask, 15));
///*     */
///* 306 */           int mask = ((Integer)masks.get(0)).intValue();
///* 307 */           decodeMask1(buf, mask, position);
///*     */
///* 309 */           if (masks.size() >= 2) {
///* 310 */             mask = ((Integer)masks.get(1)).intValue();
///* 311 */             decodeMask2(buf, mask, position);
///*     */           }
///*     */
///* 314 */           if (masks.size() >= 3) {
///* 315 */             mask = ((Integer)masks.get(2)).intValue();
///* 316 */             decodeMask3(buf, mask, position);
///*     */           }
///*     */
///* 319 */           if (masks.size() >= 4) {
///* 320 */             mask = ((Integer)masks.get(3)).intValue();
///* 321 */             decodeMask4(buf, mask, position);
///*     */           }
///*     */         }
///*     */
///* 325 */         buf.readerIndex(structEnd);
///*     */
///* 327 */         if (position.getValid()) {
///* 328 */           positions.add(position); continue;
///* 329 */         }  if (!position.getAttributes().isEmpty()) {
///* 330 */           getLastLocation(position, null);
///* 331 */           positions.add(position);
///*     */         }
///*     */       }
///*     */
///*     */
///* 336 */       if (type == 165 && channel != null) {
///* 337 */         ByteBuf response = Unpooled.buffer(13);
///* 338 */         response.writeLongLE(Long.parseLong(imei));
///* 339 */         response.writeShortLE(2);
///* 340 */         response.writeByte(25);
///* 341 */         response.writeByte(confirmKey);
///*     */
///* 343 */         int checksum = 0;
///* 344 */         for (int i = 0; i < response.writerIndex(); i++) {
///* 345 */           checksum += response.getUnsignedByte(i);
///*     */         }
///* 347 */         response.writeByte(checksum);
///*     */
///* 349 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
///*     */       }
///*     */     }
///*     */
///* 353 */     return positions;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BceProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */