/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.TimeZone;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class TzoneProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TzoneProtocolDecoder(Protocol protocol) {
/*  45 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int index) {
/*  49 */     DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/*  50 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/*     */     
/*  52 */     String ack = String.format("@ACK,%d#", new Object[] { Integer.valueOf(index) });
/*  53 */     String time = String.format("@UTC time:%s", new Object[] { dateFormat.format(new Date()) });
/*     */     
/*  55 */     ByteBuf response = Unpooled.copiedBuffer(ack + time, StandardCharsets.US_ASCII);
/*     */     
/*  57 */     if (channel != null) {
/*  58 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     }
/*     */   }
/*     */   
/*     */   private String decodeAlarm(Short value) {
/*  63 */     switch (value.shortValue()) {
/*     */       case 1:
/*  65 */         return "sos";
/*     */       case 16:
/*  67 */         return "lowBattery";
/*     */       case 17:
/*  69 */         return "overspeed";
/*     */       case 20:
/*  71 */         return "hardBraking";
/*     */       case 21:
/*  73 */         return "hardAcceleration";
/*     */       case 48:
/*  75 */         return "parking";
/*     */       case 66:
/*  77 */         return "geofenceExit";
/*     */       case 67:
/*  79 */         return "geofenceEnter";
/*     */     } 
/*  81 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean decodeGps(Position position, ByteBuf buf, int hardware) {
/*     */     double lat, lon;
/*  87 */     int blockLength = buf.readUnsignedShort();
/*  88 */     int blockEnd = buf.readerIndex() + blockLength;
/*     */     
/*  90 */     if (blockLength < 22) {
/*  91 */       return false;
/*     */     }
/*     */     
/*  94 */     if (hardware == 1043) {
/*  95 */       buf.readUnsignedByte();
/*     */     } else {
/*  97 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 100 */     if (hardware == 1043) {
/* 101 */       position.setFixTime((new DateBuilder())
/* 102 */           .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
/* 103 */           .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).getDate());
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 109 */     if (hardware == 266 || hardware == 267) {
/* 110 */       lat = buf.readUnsignedInt() / 600000.0D;
/* 111 */       lon = buf.readUnsignedInt() / 600000.0D;
/*     */     } else {
/* 113 */       lat = buf.readUnsignedInt() / 100000.0D / 60.0D;
/* 114 */       lon = buf.readUnsignedInt() / 100000.0D / 60.0D;
/*     */     } 
/*     */     
/* 117 */     if (hardware == 1043) {
/*     */       
/* 119 */       position.set("hdop", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/*     */       
/* 121 */       position.setAltitude(buf.readUnsignedShort());
/* 122 */       position.setCourse(buf.readUnsignedShort());
/* 123 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1D));
/*     */       
/* 125 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     else {
/*     */       
/* 129 */       position.setFixTime((new DateBuilder())
/* 130 */           .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
/* 131 */           .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).getDate());
/*     */       
/* 133 */       position.setSpeed(buf.readUnsignedShort() * 0.01D);
/*     */       
/* 135 */       position.set("odometer", Integer.valueOf(buf.readUnsignedMedium()));
/*     */       
/* 137 */       int flags = buf.readUnsignedShort();
/* 138 */       position.setCourse(BitUtil.to(flags, 9));
/* 139 */       if (!BitUtil.check(flags, 10)) {
/* 140 */         lat = -lat;
/*     */       }
/* 142 */       position.setLatitude(lat);
/* 143 */       if (BitUtil.check(flags, 9)) {
/* 144 */         lon = -lon;
/*     */       }
/* 146 */       position.setLongitude(lon);
/* 147 */       position.setValid(BitUtil.check(flags, 11));
/*     */     } 
/*     */ 
/*     */     
/* 151 */     buf.readerIndex(blockEnd);
/*     */     
/* 153 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeCards(Position position, ByteBuf buf) {
/* 158 */     int index = 1;
/* 159 */     for (int i = 0; i < 4; i++) {
/*     */       
/* 161 */       int blockLength = buf.readUnsignedShort();
/* 162 */       int blockEnd = buf.readerIndex() + blockLength;
/*     */       
/* 164 */       if (blockLength > 0) {
/*     */         
/* 166 */         int count = buf.readUnsignedByte();
/* 167 */         for (int j = 0; j < count; j++) {
/*     */           
/* 169 */           int length = buf.readUnsignedByte();
/*     */           
/* 171 */           boolean odd = (length % 2 != 0);
/* 172 */           if (odd) {
/* 173 */             length++;
/*     */           }
/*     */           
/* 176 */           String num = ByteBufUtil.hexDump(buf.readSlice(length / 2));
/*     */           
/* 178 */           if (odd) {
/* 179 */             num = num.substring(1);
/*     */           }
/*     */           
/* 182 */           position.set("card" + index, num);
/*     */         } 
/*     */       } 
/*     */       
/* 186 */       buf.readerIndex(blockEnd);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodePassengers(Position position, ByteBuf buf) {
/* 193 */     int blockLength = buf.readUnsignedShort();
/* 194 */     int blockEnd = buf.readerIndex() + blockLength;
/*     */     
/* 196 */     if (blockLength > 0) {
/*     */       
/* 198 */       position.set("passengersOn", Integer.valueOf(buf.readUnsignedMedium()));
/* 199 */       position.set("passengersOff", Integer.valueOf(buf.readUnsignedMedium()));
/*     */     } 
/*     */ 
/*     */     
/* 203 */     buf.readerIndex(blockEnd);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void decodeTags(Position position, ByteBuf buf, int hardware) {
/* 209 */     int blockLength = buf.readUnsignedShort();
/* 210 */     int blockEnd = buf.readerIndex() + blockLength;
/*     */     
/* 212 */     if (blockLength > 0) {
/*     */       
/* 214 */       int type = buf.readUnsignedByte();
/*     */       
/* 216 */       if (hardware != 339 || type >= 2) {
/*     */         
/* 218 */         int count = buf.readUnsignedByte();
/* 219 */         int tagLength = buf.readUnsignedByte();
/*     */         
/* 221 */         for (int i = 1; i <= count; i++) {
/* 222 */           int tagEnd = buf.readerIndex() + tagLength;
/*     */           
/* 224 */           buf.readUnsignedByte();
/* 225 */           buf.readUnsignedShortLE();
/*     */           
/* 227 */           position.set("temp" + i, Double.valueOf((buf.readShortLE() & 0x3FFF) * 0.1D));
/*     */           
/* 229 */           buf.readUnsignedByte();
/* 230 */           buf.readUnsignedByte();
/*     */           
/* 232 */           buf.readerIndex(tagEnd);
/*     */         }
/*     */       
/* 235 */       } else if (type == 1) {
/*     */         
/* 237 */         position.set("card", buf.readCharSequence(blockEnd - buf
/* 238 */               .readerIndex(), StandardCharsets.UTF_8).toString());
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 244 */     buf.readerIndex(blockEnd);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 252 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 254 */     buf.skipBytes(2);
/* 255 */     buf.readUnsignedShort();
/* 256 */     if (buf.readUnsignedShort() != 9252) {
/* 257 */       return null;
/*     */     }
/* 259 */     int hardware = buf.readUnsignedShort();
/* 260 */     long firmware = buf.readUnsignedInt();
/*     */     
/* 262 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/* 263 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 264 */     if (deviceSession == null) {
/* 265 */       return null;
/*     */     }
/*     */     
/* 268 */     Position position = new Position(getProtocolName());
/* 269 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 271 */     position.set("versionHw", Integer.valueOf(hardware));
/* 272 */     position.set("versionFw", Long.valueOf(firmware));
/*     */     
/* 274 */     position.setDeviceTime((new DateBuilder())
/* 275 */         .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
/* 276 */         .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).getDate());
/*     */ 
/*     */ 
/*     */     
/* 280 */     if (hardware == 1030 || !decodeGps(position, buf, hardware))
/*     */     {
/* 282 */       getLastLocation(position, position.getDeviceTime());
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 288 */     int blockLength = buf.readUnsignedShort();
/* 289 */     int blockEnd = buf.readerIndex() + blockLength;
/*     */     
/* 291 */     if (blockLength > 0) {
/* 292 */       if (hardware == 266 || hardware == 267 || hardware == 1030) {
/*     */         
/* 294 */         position.setNetwork(new Network(
/* 295 */               CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedShort())));
/*     */       }
/* 297 */       else if (hardware == 1031) {
/*     */         
/* 299 */         Network network = new Network();
/* 300 */         int count = buf.readUnsignedByte();
/* 301 */         for (int i = 0; i < count; i++) {
/* 302 */           buf.readUnsignedByte();
/*     */           
/* 304 */           int mcc = BcdUtil.readInteger(buf, 4);
/* 305 */           int mnc = BcdUtil.readInteger(buf, 4) % 1000;
/*     */           
/* 307 */           network.addCellTower(CellTower.from(mcc, mnc, buf
/* 308 */                 .readUnsignedShort(), buf.readUnsignedInt()));
/*     */         } 
/* 310 */         position.setNetwork(network);
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/* 315 */     buf.readerIndex(blockEnd);
/*     */ 
/*     */ 
/*     */     
/* 319 */     blockLength = buf.readUnsignedShort();
/* 320 */     blockEnd = buf.readerIndex() + blockLength;
/*     */     
/* 322 */     if (hardware == 1031 || blockLength >= 13) {
/* 323 */       position.set("alarm", decodeAlarm(Short.valueOf(buf.readUnsignedByte())));
/* 324 */       position.set("terminalInfo", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 326 */       if (hardware != 1031) {
/* 327 */         int status = buf.readUnsignedByte();
/* 328 */         position.set("out1", Boolean.valueOf(BitUtil.check(status, 0)));
/* 329 */         position.set("out2", Boolean.valueOf(BitUtil.check(status, 1)));
/* 330 */         status = buf.readUnsignedByte();
/* 331 */         position.set("in1", Boolean.valueOf(BitUtil.check(status, 4)));
/* 332 */         if (BitUtil.check(status, 0)) {
/* 333 */           position.set("alarm", "sos");
/*     */         }
/*     */       } 
/*     */       
/* 337 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 338 */       position.set("gsmStatus", Short.valueOf(buf.readUnsignedByte()));
/* 339 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*     */       
/* 341 */       if (hardware != 1031) {
/* 342 */         position.set("power", Integer.valueOf(buf.readUnsignedShort()));
/* 343 */         position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
/* 344 */         position.set("adc2", Integer.valueOf(buf.readUnsignedShort()));
/*     */       } else {
/* 346 */         int temperature = buf.readUnsignedShort();
/* 347 */         if (!BitUtil.check(temperature, 15)) {
/* 348 */           double value = BitUtil.to(temperature, 14) * 0.1D;
/* 349 */           position.set("temp1", Double.valueOf(BitUtil.check(temperature, 14) ? -value : value));
/*     */         } 
/* 351 */         int humidity = buf.readUnsignedShort();
/* 352 */         if (!BitUtil.check(humidity, 15)) {
/* 353 */           position.set("humidity", Double.valueOf(BitUtil.to(humidity, 15) * 0.1D));
/*     */         }
/* 355 */         position.set("lightSensor", Boolean.valueOf((buf.readUnsignedByte() == 0)));
/*     */       } 
/*     */     } 
/*     */     
/* 359 */     if (blockLength >= 15) {
/* 360 */       position.set("temp1", Integer.valueOf(buf.readUnsignedShort()));
/*     */     }
/*     */     
/* 363 */     buf.readerIndex(blockEnd);
/*     */     
/* 365 */     if (hardware == 267) {
/*     */       
/* 367 */       decodeCards(position, buf);
/*     */       
/* 369 */       buf.skipBytes(buf.readUnsignedShort());
/* 370 */       buf.skipBytes(buf.readUnsignedShort());
/*     */       
/* 372 */       decodePassengers(position, buf);
/*     */     } 
/*     */ 
/*     */     
/* 376 */     if (hardware == 339 || hardware == 1030)
/*     */     {
/* 378 */       decodeTags(position, buf, hardware);
/*     */     }
/*     */ 
/*     */     
/* 382 */     if (Context.getConfig().getBoolean(getProtocolName() + ".ack")) {
/* 383 */       sendResponse(channel, remoteAddress, buf.getUnsignedShort(buf.writerIndex() - 6));
/*     */     }
/*     */     
/* 386 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TzoneProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */