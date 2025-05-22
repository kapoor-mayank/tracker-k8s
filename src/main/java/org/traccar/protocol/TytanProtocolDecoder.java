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
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class TytanProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TytanProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void decodeExtraData(Position position, ByteBuf buf, int end) {
/*  43 */     while (buf.readerIndex() < end) {
/*     */       
/*  45 */       int n, alarm, i, fuel, fuelFormat, type = buf.readUnsignedByte();
/*  46 */       int length = buf.readUnsignedByte();
/*  47 */       if (length == 255) {
/*  48 */         length += buf.readUnsignedByte();
/*     */       }
/*     */ 
/*     */ 
/*     */       
/*  53 */       switch (type) {
/*     */         case 2:
/*  55 */           position.set("tripOdometer", Integer.valueOf(buf.readUnsignedMedium()));
/*     */           continue;
/*     */         case 5:
/*  58 */           position.set("input", Short.valueOf(buf.readUnsignedByte()));
/*     */           continue;
/*     */         case 6:
/*  61 */           n = buf.readUnsignedByte() >> 4;
/*  62 */           if (n < 2) {
/*  63 */             position.set("adc" + n, Float.valueOf(buf.readFloat())); continue;
/*     */           } 
/*  65 */           position.set("di" + (n - 2), Float.valueOf(buf.readFloat()));
/*     */           continue;
/*     */         
/*     */         case 7:
/*  69 */           alarm = buf.readUnsignedByte();
/*  70 */           buf.readUnsignedByte();
/*  71 */           if (BitUtil.check(alarm, 5)) {
/*  72 */             position.set("alarm", "general");
/*     */           }
/*     */           continue;
/*     */         case 8:
/*  76 */           position.set("antihijack", Short.valueOf(buf.readUnsignedByte()));
/*     */           continue;
/*     */         case 9:
/*  79 */           position.set("unauthorized", ByteBufUtil.hexDump(buf.readSlice(8)));
/*     */           continue;
/*     */         case 10:
/*  82 */           position.set("authorized", ByteBufUtil.hexDump(buf.readSlice(8)));
/*     */           continue;
/*     */         case 24:
/*  85 */           for (i = 0; i < length / 2; i++) {
/*  86 */             position.set("temp" + buf.readUnsignedByte(), Byte.valueOf(buf.readByte()));
/*     */           }
/*     */           continue;
/*     */         case 28:
/*  90 */           position.set("axleWeight", Integer.valueOf(buf.readUnsignedShort()));
/*  91 */           buf.readUnsignedByte();
/*     */           continue;
/*     */         case 90:
/*  94 */           position.set("power", Float.valueOf(buf.readFloat()));
/*     */           continue;
/*     */         case 101:
/*  97 */           position.set("obdSpeed", Short.valueOf(buf.readUnsignedByte()));
/*     */           continue;
/*     */         case 102:
/* 100 */           position.set("rpm", Integer.valueOf(buf.readUnsignedByte() * 50));
/*     */           continue;
/*     */         case 107:
/* 103 */           fuel = buf.readUnsignedShort();
/* 104 */           fuelFormat = fuel >> 14;
/* 105 */           if (fuelFormat == 1) {
/* 106 */             position.set("fuelValue", ((fuel & 0x3FFF) * 0.4D) + "%"); continue;
/* 107 */           }  if (fuelFormat == 2) {
/* 108 */             position.set("fuelValue", ((fuel & 0x3FFF) * 0.5D) + " l"); continue;
/* 109 */           }  if (fuelFormat == 3) {
/* 110 */             position.set("fuelValue", ((fuel & 0x3FFF) * -0.5D) + " l");
/*     */           }
/*     */           continue;
/*     */         case 108:
/* 114 */           position.set("obdOdometer", Long.valueOf(buf.readUnsignedInt() * 5L));
/*     */           continue;
/*     */         case 150:
/* 117 */           position.set("door", Short.valueOf(buf.readUnsignedByte()));
/*     */           continue;
/*     */       } 
/* 120 */       buf.skipBytes(length);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 130 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 132 */     buf.readUnsignedByte();
/* 133 */     buf.readUnsignedShort();
/* 134 */     int index = buf.readUnsignedByte() >> 3;
/*     */     
/* 136 */     if (channel != null) {
/* 137 */       ByteBuf response = Unpooled.copiedBuffer("^" + index, StandardCharsets.US_ASCII);
/* 138 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/* 141 */     String id = String.valueOf(buf.readUnsignedInt());
/* 142 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 143 */     if (deviceSession == null) {
/* 144 */       return null;
/*     */     }
/*     */     
/* 147 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 149 */     while (buf.readableBytes() > 2) {
/*     */       
/* 151 */       Position position = new Position(getProtocolName());
/* 152 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 154 */       int end = buf.readerIndex() + buf.readUnsignedByte();
/*     */       
/* 156 */       position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */       
/* 158 */       int flags = buf.readUnsignedByte();
/* 159 */       position.set("sat", Integer.valueOf(BitUtil.from(flags, 2)));
/* 160 */       position.setValid((BitUtil.to(flags, 2) > 0));
/*     */ 
/*     */       
/* 163 */       double lat = buf.readUnsignedMedium();
/* 164 */       lat = lat * -180.0D / 1.6777216E7D + 90.0D;
/* 165 */       position.setLatitude(lat);
/*     */ 
/*     */       
/* 168 */       double lon = buf.readUnsignedMedium();
/* 169 */       lon = lon * 360.0D / 1.6777216E7D - 180.0D;
/* 170 */       position.setLongitude(lon);
/*     */ 
/*     */       
/* 173 */       flags = buf.readUnsignedByte();
/* 174 */       position.set("ignition", Boolean.valueOf(BitUtil.check(flags, 0)));
/* 175 */       position.set("rssi", Integer.valueOf(BitUtil.between(flags, 2, 5)));
/* 176 */       position.setCourse(((BitUtil.from(flags, 5) * 45 + 180) % 360));
/*     */ 
/*     */       
/* 179 */       int speed = buf.readUnsignedByte();
/* 180 */       if (speed < 250) {
/* 181 */         position.setSpeed(UnitsConverter.knotsFromKph(speed));
/*     */       }
/*     */       
/* 184 */       decodeExtraData(position, buf, end);
/*     */       
/* 186 */       positions.add(position);
/*     */     } 
/*     */     
/* 189 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TytanProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */