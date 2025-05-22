/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class PacificTrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public PacificTrackProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*     */   public static int readBitExt(ByteBuf buf) {
/*  38 */     int result = 0;
/*  39 */     while (buf.isReadable()) {
/*  40 */       int b = buf.readUnsignedByte();
/*  41 */       result <<= 7;
/*  42 */       result += BitUtil.to(b, 7);
/*  43 */       if (BitUtil.check(b, 7)) {
/*     */         break;
/*     */       }
/*     */     } 
/*  47 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  54 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  56 */     buf.readByte();
/*  57 */     readBitExt(buf);
/*  58 */     readBitExt(buf);
/*     */     
/*  60 */     DeviceSession deviceSession = null;
/*  61 */     Position position = new Position(getProtocolName());
/*     */     
/*  63 */     while (buf.isReadable()) {
/*     */       int date; DateBuilder dateBuilder; int speedAndCourse, voltage; String imei;
/*  65 */       int segmentId = readBitExt(buf);
/*  66 */       int segmentEnd = readBitExt(buf) + buf.readerIndex();
/*     */       
/*  68 */       switch (segmentId) {
/*     */         case 1:
/*  70 */           position.set("event", Integer.valueOf(readBitExt(buf)));
/*     */           continue;
/*     */         case 16:
/*  73 */           position.setValid(BitUtil.check(buf.readUnsignedByte(), 4));
/*  74 */           date = buf.readUnsignedByte();
/*     */ 
/*     */           
/*  77 */           dateBuilder = (new DateBuilder()).setDate(2020 + BitUtil.from(date, 4), BitUtil.to(date, 4), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  78 */           position.setTime(dateBuilder.getDate());
/*  79 */           position.setLatitude(buf.readUnsignedInt() / 1000000.0D - 90.0D);
/*  80 */           position.setLongitude(buf.readUnsignedInt() / 1000000.0D - 180.0D);
/*  81 */           speedAndCourse = buf.readUnsignedMedium();
/*  82 */           position.setCourse(BitUtil.from(speedAndCourse, 12));
/*  83 */           position.setSpeed(UnitsConverter.knotsFromKph(BitUtil.to(speedAndCourse, 12) * 0.1D));
/*  84 */           position.set("index", Integer.valueOf(buf.readUnsignedShort()));
/*     */           continue;
/*     */         case 32:
/*  87 */           voltage = buf.readUnsignedMedium();
/*  88 */           position.set("battery", Double.valueOf(BitUtil.between(voltage, 0, 12) * 0.01D));
/*  89 */           position.set("power", Double.valueOf(BitUtil.between(voltage, 12, 24) * 0.01D));
/*     */           continue;
/*     */         case 146:
/*  92 */           while (buf.readerIndex() < segmentEnd) {
/*  93 */             int field = buf.readUnsignedByte();
/*  94 */             int fieldPrefix = BitUtil.from(field, 5);
/*  95 */             if (fieldPrefix < 4) {
/*  96 */               switch (BitUtil.between(field, 2, 5)) {
/*     */                 case 0:
/*  98 */                   position.set("bus", Integer.valueOf(BitUtil.to(field, 2)));
/*     */                 case 1:
/* 100 */                   position.set("currentGear", Integer.valueOf(BitUtil.to(field, 2)));
/*     */                   continue;
/*     */               } 
/*     */               continue;
/*     */             } 
/* 105 */             if (fieldPrefix < 5) {
/* 106 */               switch (BitUtil.to(field, 5)) {
/*     */                 case 0:
/* 108 */                   position.set("obdSpeed", Short.valueOf(buf.readUnsignedByte()));
/*     */                   continue;
/*     */                 case 1:
/* 111 */                   position.set("rpm", Integer.valueOf(buf.readUnsignedByte() * 32));
/*     */                   continue;
/*     */                 case 3:
/* 114 */                   position.set("oilPressure", Integer.valueOf(buf.readUnsignedByte() * 4));
/*     */                   continue;
/*     */                 case 4:
/* 117 */                   position.set("oilLevel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/*     */                   continue;
/*     */                 case 5:
/* 120 */                   position.set("oilTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */                 case 6:
/* 123 */                   position.set("coolantLevel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/*     */                   continue;
/*     */                 case 7:
/* 126 */                   position.set("coolantTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */                 case 8:
/* 129 */                   position.set("fuel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/*     */                   continue;
/*     */                 case 9:
/* 132 */                   position.set("defLevel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/*     */                   continue;
/*     */                 case 10:
/* 135 */                   position.set("engineLoad", Short.valueOf(buf.readUnsignedByte()));
/*     */                   continue;
/*     */                 case 11:
/* 138 */                   position.set("barometer", Double.valueOf(buf.readUnsignedByte() * 0.5D));
/*     */                   continue;
/*     */                 case 12:
/* 141 */                   position.set("intakeManifoldTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */                 case 13:
/* 144 */                   position.set("fuelTankTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */                 case 14:
/* 147 */                   position.set("intercoolerTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */                 case 15:
/* 150 */                   position.set("turboOilTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */                 case 16:
/* 153 */                   position.set("transOilTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/*     */                   continue;
/*     */               } 
/* 156 */               buf.readUnsignedByte();
/*     */               continue;
/*     */             } 
/* 159 */             if (fieldPrefix < 6) {
/* 160 */               switch (BitUtil.to(field, 5)) {
/*     */                 case 2:
/* 162 */                   position.set("fuelConsumption", Double.valueOf(buf.readUnsignedShort() / 512.0D));
/*     */                   continue;
/*     */                 case 3:
/* 165 */                   position.set("temp1", Double.valueOf(buf.readUnsignedShort() * 0.03125D - 273.0D));
/*     */                   continue;
/*     */               } 
/* 168 */               buf.readUnsignedShort();
/*     */               continue;
/*     */             } 
/* 171 */             if (fieldPrefix < 7) {
/* 172 */               switch (BitUtil.to(field, 5)) {
/*     */                 case 0:
/* 174 */                   position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 100L));
/*     */                   continue;
/*     */                 case 1:
/* 177 */                   position.set("hours", Long.valueOf(buf.readUnsignedInt() * 180L));
/*     */                   continue;
/*     */                 case 2:
/* 180 */                   position.set("idleHours", Long.valueOf(buf.readUnsignedInt() * 180L));
/*     */                   continue;
/*     */                 case 4:
/* 183 */                   position.set("fuelUsed", Double.valueOf(buf.readUnsignedInt() * 0.5D));
/*     */                   continue;
/*     */                 case 5:
/* 186 */                   position.set("fuelUsedIdle", Double.valueOf(buf.readUnsignedInt() * 0.5D));
/*     */                   continue;
/*     */               } 
/* 189 */               buf.readUnsignedInt();
/*     */               
/*     */               continue;
/*     */             } 
/* 193 */             buf.skipBytes(buf.readUnsignedByte());
/*     */           } 
/*     */           continue;
/*     */         
/*     */         case 256:
/* 198 */           imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(0, 15);
/* 199 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */           continue;
/*     */       } 
/* 202 */       buf.readerIndex(segmentEnd);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 207 */     if (deviceSession == null) {
/* 208 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */     }
/*     */     
/* 211 */     if (deviceSession != null) {
/* 212 */       position.setDeviceId(deviceSession.getDeviceId());
/* 213 */       if (position.getFixTime() == null) {
/* 214 */         getLastLocation(position, null);
/*     */       }
/* 216 */       return position;
/*     */     } 
/* 218 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PacificTrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */