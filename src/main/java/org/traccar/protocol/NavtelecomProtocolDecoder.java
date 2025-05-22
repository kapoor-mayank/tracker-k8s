/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.BitSet;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.UnitsConverter;
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
/*     */ public class NavtelecomProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public NavtelecomProtocolDecoder(Protocol protocol) {
/*  42 */     super(protocol);
/*     */   }
/*     */   
/*  45 */   private static final Map<Integer, Integer> ITEM_LENGTH_MAP = new HashMap<>(); private BitSet bits;
/*     */   
/*     */   static {
/*  48 */     int[] l1 = { 4, 5, 6, 7, 8, 29, 30, 31, 32, 45, 46, 47, 48, 49, 50, 51, 52, 56, 63, 64, 65, 69, 72, 78, 79, 80, 81, 82, 83, 98, 99, 101, 104, 118, 122, 123, 124, 125, 126, 139, 140, 144, 145, 167, 168, 169, 170, 199, 202, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222 };
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  53 */     int[] l2 = { 2, 14, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 35, 36, 38, 39, 40, 41, 42, 43, 44, 53, 55, 58, 59, 60, 61, 62, 66, 68, 71, 75, 100, 106, 108, 110, 111, 112, 113, 114, 115, 116, 117, 119, 120, 121, 133, 134, 135, 136, 137, 138, 141, 143, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 171, 175, 177, 178, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 200, 201, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  60 */     int[] l3 = { 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 142, 146, 198 };
/*     */ 
/*     */     
/*  63 */     int[] l4 = { 1, 3, 9, 10, 11, 12, 13, 15, 16, 33, 34, 37, 54, 57, 67, 74, 76, 102, 103, 105, 127, 128, 129, 130, 131, 132, 172, 173, 174, 176, 179, 193, 194, 195, 196, 203, 205, 206, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252 };
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  68 */     for (int i : l1) {
/*  69 */       ITEM_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(1));
/*     */     }
/*  71 */     for (int i : l2) {
/*  72 */       ITEM_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(2));
/*     */     }
/*  74 */     for (int i : l3) {
/*  75 */       ITEM_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(3));
/*     */     }
/*  77 */     for (int i : l4) {
/*  78 */       ITEM_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(4));
/*     */     }
/*  80 */     ITEM_LENGTH_MAP.put(Integer.valueOf(70), Integer.valueOf(8));
/*  81 */     ITEM_LENGTH_MAP.put(Integer.valueOf(73), Integer.valueOf(16));
/*  82 */     ITEM_LENGTH_MAP.put(Integer.valueOf(77), Integer.valueOf(37));
/*  83 */     ITEM_LENGTH_MAP.put(Integer.valueOf(94), Integer.valueOf(6));
/*  84 */     ITEM_LENGTH_MAP.put(Integer.valueOf(95), Integer.valueOf(12));
/*  85 */     ITEM_LENGTH_MAP.put(Integer.valueOf(96), Integer.valueOf(24));
/*  86 */     ITEM_LENGTH_MAP.put(Integer.valueOf(97), Integer.valueOf(48));
/*  87 */     ITEM_LENGTH_MAP.put(Integer.valueOf(107), Integer.valueOf(6));
/*  88 */     ITEM_LENGTH_MAP.put(Integer.valueOf(109), Integer.valueOf(6));
/*  89 */     ITEM_LENGTH_MAP.put(Integer.valueOf(197), Integer.valueOf(6));
/*  90 */     ITEM_LENGTH_MAP.put(Integer.valueOf(204), Integer.valueOf(5));
/*  91 */     ITEM_LENGTH_MAP.put(Integer.valueOf(253), Integer.valueOf(8));
/*  92 */     ITEM_LENGTH_MAP.put(Integer.valueOf(254), Integer.valueOf(8));
/*  93 */     ITEM_LENGTH_MAP.put(Integer.valueOf(255), Integer.valueOf(8));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static int getItemLength(int id) {
/*  99 */     Integer length = ITEM_LENGTH_MAP.get(Integer.valueOf(id));
/* 100 */     if (length == null) {
/* 101 */       throw new IllegalArgumentException(String.format("Unknown item: %d", new Object[] { Integer.valueOf(id) }));
/*     */     }
/* 103 */     return length.intValue();
/*     */   }
/*     */   
/*     */   public BitSet getBits() {
/* 107 */     return this.bits;
/*     */   }
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int receiver, int sender, ByteBuf content) {
/* 112 */     if (channel != null) {
/* 113 */       ByteBuf response = Unpooled.buffer();
/* 114 */       response.writeCharSequence("@NTC", StandardCharsets.US_ASCII);
/* 115 */       response.writeIntLE(sender);
/* 116 */       response.writeIntLE(receiver);
/* 117 */       response.writeShortLE(content.readableBytes());
/* 118 */       response.writeByte(Checksum.xor(content.nioBuffer()));
/* 119 */       response.writeByte(Checksum.xor(response.nioBuffer()));
/* 120 */       response.writeBytes(content);
/* 121 */       content.release();
/*     */       
/* 123 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 131 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 133 */     if (buf.getByte(buf.readerIndex()) == 64) {
/*     */       
/* 135 */       buf.skipBytes(4);
/* 136 */       int receiver = buf.readIntLE();
/* 137 */       int sender = buf.readIntLE();
/* 138 */       int length = buf.readUnsignedShortLE();
/* 139 */       buf.readUnsignedByte();
/* 140 */       buf.readUnsignedByte();
/*     */       
/* 142 */       String type = buf.toString(buf.readerIndex(), 6, StandardCharsets.US_ASCII);
/*     */       
/* 144 */       if (type.startsWith("*>S"))
/*     */       {
/* 146 */         String sentence = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
/* 147 */         getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(4) });
/*     */         
/* 149 */         ByteBuf payload = Unpooled.copiedBuffer("*<S", StandardCharsets.US_ASCII);
/*     */         
/* 151 */         sendResponse(channel, remoteAddress, receiver, sender, payload);
/*     */       }
/* 153 */       else if (type.startsWith("*>FLEX"))
/*     */       {
/* 155 */         buf.skipBytes(6);
/*     */         
/* 157 */         ByteBuf payload = Unpooled.buffer();
/* 158 */         payload.writeCharSequence("*<FLEX", StandardCharsets.US_ASCII);
/* 159 */         payload.writeByte(buf.readUnsignedByte());
/* 160 */         payload.writeByte(buf.readUnsignedByte());
/* 161 */         payload.writeByte(buf.readUnsignedByte());
/*     */         
/* 163 */         int bitCount = buf.readUnsignedByte();
/* 164 */         this.bits = new BitSet((bitCount + 7) / 8);
/*     */         
/* 166 */         int currentByte = 0;
/* 167 */         for (int i = 0; i < bitCount; i++) {
/* 168 */           if (i % 8 == 0) {
/* 169 */             currentByte = buf.readUnsignedByte();
/*     */           }
/* 171 */           this.bits.set(i, BitUtil.check(currentByte, 7 - i % 8));
/*     */         } 
/*     */         
/* 174 */         sendResponse(channel, remoteAddress, receiver, sender, payload);
/*     */       }
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 180 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 181 */       if (deviceSession == null) {
/* 182 */         return null;
/*     */       }
/*     */       
/* 185 */       String type = buf.readCharSequence(2, StandardCharsets.US_ASCII).toString();
/*     */       
/* 187 */       if (type.equals("~A")) {
/*     */         
/* 189 */         int count = buf.readUnsignedByte();
/* 190 */         List<Position> positions = new LinkedList<>();
/*     */         
/* 192 */         for (int i = 0; i < count; i++) {
/* 193 */           Position position = new Position(getProtocolName());
/* 194 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 196 */           for (int j = 0; j < this.bits.length(); j++) {
/* 197 */             if (this.bits.get(j)) {
/*     */               int value; int k; int fuel; float fuelConsumption; int rpm; int def; int obdSpeed; int m;
/* 199 */               switch (j + 1) {
/*     */                 case 1:
/* 201 */                   position.set("index", Long.valueOf(buf.readUnsignedIntLE()));
/*     */                   break;
/*     */                 case 2:
/* 204 */                   position.set("event", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 3:
/* 207 */                   position.setDeviceTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */                   break;
/*     */                 case 7:
/* 210 */                   position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */                   break;
/*     */                 case 9:
/* 213 */                   position.setValid(true);
/* 214 */                   position.setFixTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */                   break;
/*     */                 case 10:
/* 217 */                   position.setLatitude(buf.readIntLE() * 1.0E-4D / 60.0D);
/*     */                   break;
/*     */                 case 11:
/* 220 */                   position.setLongitude(buf.readIntLE() * 1.0E-4D / 60.0D);
/*     */                   break;
/*     */                 case 12:
/* 223 */                   position.setAltitude(buf.readIntLE() * 0.1D);
/*     */                   break;
/*     */                 case 13:
/* 226 */                   position.setSpeed(UnitsConverter.knotsFromKph(buf.readFloatLE()));
/*     */                   break;
/*     */                 case 15:
/* 229 */                   position.set("odometer", Float.valueOf(buf.readFloatLE()));
/*     */                   break;
/*     */                 case 19:
/* 232 */                   position.set("mainPowerTension", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 20:
/* 235 */                   position.set("backupPowerTension", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 21:
/* 238 */                   position.set("ain1Tension", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 22:
/* 241 */                   position.set("ain2Tension", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 29:
/* 244 */                   value = buf.readUnsignedByte();
/* 245 */                   for (k = 0; k <= 7; k++) {
/* 246 */                     position.set("in" + (k + 1), Boolean.valueOf(BitUtil.check(value, k)));
/*     */                   }
/*     */                   break;
/*     */                 case 31:
/* 250 */                   value = buf.readUnsignedByte();
/* 251 */                   for (k = 0; k <= 3; k++) {
/* 252 */                     position.set("out" + (k + 1), Boolean.valueOf(BitUtil.check(value, k)));
/*     */                   }
/*     */                   break;
/*     */                 case 38:
/*     */                 case 39:
/*     */                 case 40:
/*     */                 case 41:
/*     */                 case 42:
/*     */                 case 43:
/* 261 */                   value = buf.readUnsignedShortLE();
/* 262 */                   position.set("fuel" + (j + 2 - 38), (value < 65500) ? Integer.valueOf(value) : null);
/*     */                   break;
/*     */                 case 45:
/* 265 */                   position.set("temp1", Integer.valueOf(buf.readByte()));
/*     */                   break;
/*     */                 case 46:
/* 268 */                   position.set("temp2", Integer.valueOf(buf.readByte()));
/*     */                   break;
/*     */                 case 47:
/* 271 */                   position.set("temp3", Integer.valueOf(buf.readByte()));
/*     */                   break;
/*     */                 case 53:
/* 274 */                   fuel = buf.readUnsignedShortLE();
/* 275 */                   position.set("fuel", Integer.valueOf(BitUtil.to(fuel, 15)));
/* 276 */                   position.set("fuelType", BitUtil.check(fuel, 15) ? "volume" : "percent");
/*     */                   break;
/*     */                 case 54:
/* 279 */                   fuelConsumption = buf.readFloatLE();
/* 280 */                   if (fuelConsumption > 0.0F) {
/* 281 */                     position.set("fuelConsumption", Float.valueOf(fuelConsumption));
/*     */                   }
/*     */                   break;
/*     */                 case 55:
/* 285 */                   rpm = buf.readUnsignedShortLE();
/* 286 */                   if (rpm > 0 && rpm < 65534) {
/* 287 */                     position.set("rpm", Integer.valueOf(rpm));
/*     */                   }
/*     */                   break;
/*     */                 case 56:
/* 291 */                   position.set("coolantTemp", Integer.valueOf(buf.readByte()));
/*     */                   break;
/*     */                 case 57:
/* 294 */                   position.set("obdOdometer", Float.valueOf(buf.readFloatLE()));
/*     */                   break;
/*     */                 case 63:
/* 297 */                   position.set("accPedal", Short.valueOf(buf.readUnsignedByte()));
/*     */                   break;
/*     */                 case 64:
/* 300 */                   position.set("brakePedal", Short.valueOf(buf.readUnsignedByte()));
/*     */                   break;
/*     */                 case 65:
/* 303 */                   position.set("engineLoad", Short.valueOf(buf.readUnsignedByte()));
/*     */                   break;
/*     */                 case 66:
/* 306 */                   def = buf.readUnsignedShortLE();
/* 307 */                   position.set("defLevel", Integer.valueOf(BitUtil.to(def, 15)));
/* 308 */                   position.set("defType", BitUtil.check(def, 15) ? "volume" : "percent");
/*     */                   break;
/*     */                 case 67:
/* 311 */                   position.set("hours", Long.valueOf(buf.readUnsignedIntLE()));
/*     */                   break;
/*     */                 case 68:
/* 314 */                   position.set("maintenanceDistance", Short.valueOf(buf.readShortLE()));
/*     */                   break;
/*     */                 case 69:
/* 317 */                   obdSpeed = buf.readUnsignedByte();
/* 318 */                   if (obdSpeed > 0 && obdSpeed < 255) {
/* 319 */                     position.set("obdSpeed", Integer.valueOf(obdSpeed));
/*     */                   }
/*     */                   break;
/*     */                 case 78:
/*     */                 case 79:
/*     */                 case 80:
/*     */                 case 81:
/*     */                 case 82:
/*     */                 case 83:
/* 328 */                   position.set("fuelTemp" + (j + 2 - 78), Integer.valueOf(buf.readByte()));
/*     */                   break;
/*     */                 case 108:
/* 331 */                   position.set("excessDuration", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 109:
/* 334 */                   position.set("maxAcceleration", Short.valueOf(buf.readShortLE()));
/* 335 */                   position.set("maxBraking", Short.valueOf(buf.readShortLE()));
/* 336 */                   position.set("maxCornering", Short.valueOf(buf.readShortLE()));
/*     */                   break;
/*     */                 case 201:
/* 339 */                   position.set("statusFlags", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 203:
/* 342 */                   position.set("emergencyIndicators", Long.valueOf(buf.readUnsignedIntLE()));
/*     */                   break;
/*     */                 case 204:
/* 345 */                   for (m = 1; m <= 5; m++) {
/* 346 */                     position.set("faultInfo" + m, Short.valueOf(buf.readUnsignedByte()));
/*     */                   }
/*     */                   break;
/*     */                 case 207:
/* 350 */                   position.set("user1Byte1", Short.valueOf(buf.readUnsignedByte()));
/*     */                   break;
/*     */                 case 223:
/* 353 */                   position.set("user2Byte1", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */                   break;
/*     */                 case 238:
/* 356 */                   position.set("user4Byte1", Long.valueOf(buf.readUnsignedIntLE()));
/*     */                   break;
/*     */                 default:
/* 359 */                   buf.skipBytes(getItemLength(j + 1));
/*     */                   break;
/*     */               } 
/*     */             
/*     */             } 
/*     */           } 
/* 365 */           if (position.getFixTime() == null) {
/* 366 */             getLastLocation(position, position.getDeviceTime());
/*     */           }
/*     */           
/* 369 */           positions.add(position);
/*     */         } 
/*     */         
/* 372 */         if (channel != null) {
/* 373 */           ByteBuf response = Unpooled.buffer();
/* 374 */           response.writeCharSequence(type, StandardCharsets.US_ASCII);
/* 375 */           response.writeByte(count);
/* 376 */           response.writeByte(Checksum.crc8(Checksum.CRC8_EGTS, response.nioBuffer()));
/* 377 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         } 
/*     */         
/* 380 */         return positions;
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 386 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NavtelecomProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */