/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.handler.codec.mqtt.MqttPublishMessage;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseMqttProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ public class IotmProtocolDecoder
/*     */   extends BaseMqttProtocolDecoder
/*     */ {
/*     */   public IotmProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*     */   private Object readValue(ByteBuf buf, int sensorType) {
/*  39 */     switch (sensorType) {
/*     */       case 0:
/*  41 */         return Boolean.valueOf(false);
/*     */       case 1:
/*  43 */         return Boolean.valueOf(true);
/*     */       case 3:
/*  45 */         return Integer.valueOf(0);
/*     */       case 4:
/*  47 */         return Short.valueOf(buf.readUnsignedByte());
/*     */       case 5:
/*  49 */         return Integer.valueOf(buf.readUnsignedShortLE());
/*     */       case 6:
/*  51 */         return Long.valueOf(buf.readUnsignedIntLE());
/*     */       case 7:
/*     */       case 11:
/*  54 */         return Long.valueOf(buf.readLongLE());
/*     */       case 8:
/*  56 */         return Byte.valueOf(buf.readByte());
/*     */       case 9:
/*  58 */         return Short.valueOf(buf.readShortLE());
/*     */       case 10:
/*  60 */         return Integer.valueOf(buf.readIntLE());
/*     */       case 12:
/*  62 */         return Float.valueOf(buf.readFloatLE());
/*     */       case 13:
/*  64 */         return Double.valueOf(buf.readDoubleLE());
/*     */       case 32:
/*  66 */         return buf.readCharSequence(buf.readUnsignedByte(), StandardCharsets.US_ASCII).toString();
/*     */       case 33:
/*  68 */         return ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedByte()));
/*     */       case 64:
/*  70 */         return buf.readCharSequence(buf.readUnsignedShortLE(), StandardCharsets.US_ASCII).toString();
/*     */       case 65:
/*  72 */         return ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedShortLE()));
/*     */     } 
/*     */     
/*  75 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeSensor(Position position, ByteBuf record, int sensorType, int sensorId) {
/*  81 */     switch (sensorId) {
/*     */       case 2:
/*  83 */         position.set("motion", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 8:
/*     */       case 155:
/*  87 */         if (sensorType > 0) {
/*  88 */           position.set("alarm", "jamming");
/*     */         }
/*     */         return;
/*     */       case 16:
/*     */       case 17:
/*     */       case 18:
/*     */       case 19:
/*     */       case 20:
/*     */       case 21:
/*  97 */
            Object key = "in" + (sensorId - 16 + 2);
/*  98 */         position.set((String) key, Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 98:
/* 101 */         position.set("doorFL", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 99:
/* 104 */         position.set("doorFR", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 100:
/* 107 */         position.set("doorRL", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 101:
/* 110 */         position.set("doorRR", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 117:
/* 113 */         position.set("oilPressureWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 118:
/* 116 */         position.set("coolantFluidWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 119:
/* 119 */         position.set("brakeSystemWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 120:
/* 122 */         position.set("batteryVoltageWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 121:
/* 125 */         position.set("airBagWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 122:
/* 128 */         position.set("checkEngineWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 124:
/* 131 */         position.set("tirePressureWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 127:
/* 134 */         position.set("absWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 128:
/* 137 */         position.set("lowFuelLevelWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 133:
/* 140 */         position.set("driverSeatBeltWarning", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 30:
/* 143 */         position.set("buttonPresent", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 109:
/* 146 */         position.set("ignition", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 139:
/* 149 */         position.set("handBrake", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 140:
/* 152 */         position.set("footBrake", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 148:
/*     */       case 149:
/*     */       case 150:
/* 157 */         key = "out" + (sensorId - 148 + 1);
/* 158 */         position.set((String) key, Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 154:
/* 161 */         position.set("out4", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       case 158:
/* 164 */         position.set("ptoState", Boolean.valueOf((sensorType > 0)));
/*     */         return;
/*     */       
/*     */       case 8192:
/* 168 */         position.set("obdSpeed", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8193:
/* 171 */         position.set("sat", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8198:
/* 174 */         position.set("throttle", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8199:
/* 177 */         position.set("fuel", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8200:
/* 180 */         position.set("coolantTemp", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8201:
/* 183 */         position.set("fuel2", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8202:
/* 186 */         position.set("engineLoad", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8226:
/* 189 */         position.set("topSpeed", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8234:
/* 192 */         position.set("rotateDirection", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8251:
/* 195 */         position.set("ptoState", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8255:
/* 198 */         position.set("oilPressure", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8257:
/* 201 */         position.set("batteryLevel", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 8258:
/* 204 */         position.set("transmissionOilLife", Short.valueOf(record.readUnsignedByte()));
/*     */         return;
/*     */       case 12288:
/* 207 */         position.set("power", Double.valueOf(record.readUnsignedShortLE() * 0.001D));
/*     */         return;
/*     */       case 12289:
/*     */       case 12290:
/*     */       case 12291:
/* 212 */         key = "adc" + (12291 - sensorId + 3);
/* 213 */         position.set((String) key, Double.valueOf(record.readUnsignedShortLE() * 0.001D));
/*     */         return;
/*     */       case 12292:
/* 216 */         position.set("battery", Double.valueOf(record.readUnsignedShortLE() * 0.001D));
/*     */         return;
/*     */       case 12300:
/* 219 */         position.set("rpm", Integer.valueOf(record.readUnsignedShortLE()));
/*     */         return;
/*     */       case 12317:
/* 222 */         position.set("wheelSpeed", Double.valueOf(record.readUnsignedShortLE() / 256.0D));
/*     */         return;
/*     */       case 12321:
/* 225 */         position.set("fuelConsumption", Double.valueOf(record.readUnsignedShortLE() * 0.05D));
/*     */         return;
/*     */       case 12343:
/* 228 */         position.set("cargoWeight", Integer.valueOf(record.readUnsignedShortLE() * 2));
/*     */         return;
/*     */       case 16385:
/* 231 */         position.set("fuelUsed", Long.valueOf(record.readUnsignedIntLE()));
/*     */         return;
/*     */       case 16386:
/* 234 */         position.set("hours", Long.valueOf(record.readUnsignedIntLE()));
/*     */         return;
/*     */       case 16387:
/* 237 */         position.set("odometer", Long.valueOf(record.readUnsignedIntLE() * 5L));
/*     */         return;
/*     */       case 16481:
/* 240 */         position.set("wiegand26", Long.valueOf(record.readUnsignedIntLE()));
/*     */         return;
/*     */       case 16483:
/* 243 */         position.set("axleWeight", Long.valueOf(record.readUnsignedIntLE()));
/*     */         return;
/*     */       case 20480:
/* 246 */         position.set("driverUniqueId", String.valueOf(record.readLongLE()));
/*     */         return;
/*     */       case 20484:
/*     */       case 20485:
/*     */       case 20486:
/*     */       case 20487:
/* 252 */         key = "temp" + (sensorId - 20484 + 1);
/* 253 */         position.set((String) key, Long.valueOf(record.readLongLE()));
/*     */         return;
/*     */       case 20493:
/* 256 */         position.set("trailerId", String.valueOf(record.readLongLE()));
/*     */         return;
/*     */       case 28672:
/* 259 */         position.set("axesX", Double.valueOf(record.readShortLE() * 2.44140625E-4D));
/*     */         return;
/*     */       case 28673:
/* 262 */         position.set("axesY", Double.valueOf(record.readShortLE() * 2.44140625E-4D));
/*     */         return;
/*     */       case 28674:
/* 265 */         position.set("axesZ", Double.valueOf(record.readShortLE() * 2.44140625E-4D));
/*     */         return;
/*     */       case 40960:
/* 268 */         position.set("deviceTemp", Float.valueOf(record.readFloatLE()));
/*     */         return;
/*     */       case 40961:
/* 271 */         position.set("acceleration", Float.valueOf(record.readFloatLE()));
/*     */         return;
/*     */       case 40962:
/* 274 */         position.set("cornering", Float.valueOf(record.readFloatLE()));
/*     */         return;
/*     */       case 40983:
/*     */       case 40984:
/*     */       case 40985:
/*     */       case 40986:
/* 280 */         key = "temp" + (sensorId - 40983 + 1);
/* 281 */         position.set((String) key, Float.valueOf(record.readFloatLE()));
/*     */         return;
/*     */       case 40987:
/* 284 */         position.set("fuelConsumption", Float.valueOf(record.readFloatLE()));
/*     */         return;
/*     */       case 45058:
/* 287 */         position.set("obdOdometer", Double.valueOf(record.readDoubleLE()));
/*     */         return;
/*     */       case 49174:
/*     */       case 49175:
/* 291 */         position.set("driver" + (sensorId - 49174 + 1), record
/*     */             
/* 293 */             .readCharSequence(record.readUnsignedByte(), StandardCharsets.US_ASCII).toString());
/*     */         return;
/*     */     } 
/* 296 */     String key = "io" + sensorId;
/* 297 */     position.getAttributes().put(key, readValue(record, sensorType));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(DeviceSession deviceSession, MqttPublishMessage message) throws Exception {
/* 306 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 308 */     ByteBuf buf = message.payload();
/*     */     
/* 310 */     buf.readUnsignedByte();
/*     */     
/* 312 */     while (buf.readableBytes() > 1) {
/* 313 */       int type = buf.readUnsignedByte();
/* 314 */       int length = buf.readUnsignedShortLE();
/* 315 */       ByteBuf record = buf.readSlice(length);
/* 316 */       if (type == 1) {
/*     */         
/* 318 */         Position position = new Position(getProtocolName());
/* 319 */         position.setDeviceId(deviceSession.getDeviceId());
/* 320 */         position.setTime(new Date(record.readUnsignedIntLE() * 1000L));
/*     */         
/* 322 */         while (record.readableBytes() > 0) {
/* 323 */           int sensorType = record.readUnsignedByte();
/* 324 */           int sensorId = record.readUnsignedShortLE();
/* 325 */           if (sensorType == 14) {
/*     */             
/* 327 */             position.setValid(true);
/* 328 */             position.setLatitude(record.readFloatLE());
/* 329 */             position.setLongitude(record.readFloatLE());
/* 330 */             position.setSpeed(UnitsConverter.knotsFromKph(record.readUnsignedShortLE()));
/*     */             
/* 332 */             position.set("hdop", Short.valueOf(record.readUnsignedByte()));
/* 333 */             position.set("sat", Short.valueOf(record.readUnsignedByte()));
/*     */             
/* 335 */             position.setCourse(record.readUnsignedShortLE());
/* 336 */             position.setAltitude(record.readShortLE());
/*     */             
/*     */             continue;
/*     */           } 
/* 340 */           if (sensorType == 3) {
/*     */             continue;
/*     */           }
/*     */           
/* 344 */           decodeSensor(position, record, sensorType, sensorId);
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 349 */         positions.add(position); continue;
/*     */       } 
/* 351 */       if (type == 3) {
/*     */         
/* 353 */         Position position = new Position(getProtocolName());
/* 354 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 356 */         getLastLocation(position, new Date(record.readUnsignedIntLE() * 1000L));
/*     */         
/* 358 */         record.readUnsignedByte();
/*     */         
/* 360 */         position.set("event", Short.valueOf(record.readUnsignedByte()));
/*     */         
/* 362 */         positions.add(position);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 367 */     buf.readUnsignedByte();
/*     */     
/* 369 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\IotmProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */