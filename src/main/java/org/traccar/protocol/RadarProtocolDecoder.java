/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.BitSet;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ public class RadarProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_TRACKING = 76;
/*     */   
/*     */   public RadarProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  45 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  47 */     buf.readUnsignedByte();
/*  48 */     buf.readUnsignedByte();
/*     */     
/*  50 */     String serialNumber = String.valueOf(buf.readUnsignedInt());
/*  51 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { serialNumber });
/*  52 */     if (deviceSession == null) {
/*  53 */       return null;
/*     */     }
/*     */     
/*  56 */     buf.readUnsignedByte();
/*  57 */     buf.readUnsignedInt();
/*  58 */     int type = buf.readUnsignedByte();
/*  59 */     buf.readUnsignedShort();
/*     */     
/*  61 */     if (type == 76) {
/*     */       
/*  63 */       buf.readUnsignedShort();
/*  64 */       buf.readUnsignedShort();
/*  65 */       int count = buf.readUnsignedShort();
/*  66 */       buf.readUnsignedShort();
/*     */       
/*  68 */       List<Position> positions = new LinkedList<>();
/*     */       
/*  70 */       for (int index = 0; index < count; index++) {
/*     */         
/*  72 */         Position position = new Position(getProtocolName());
/*     */         
/*  74 */         position.set("event", Integer.valueOf(buf.readUnsignedShort()));
/*     */         
/*  76 */         int maskLength = buf.readUnsignedByte();
/*  77 */         BitSet mask = BitSet.valueOf(buf.nioBuffer(buf.readerIndex(), maskLength));
/*  78 */         buf.skipBytes(maskLength);
/*     */         
/*  80 */         buf.readUnsignedShort();
/*     */         
/*  82 */         if (mask.get(0)) {
/*  83 */           position.setDeviceTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */         }
/*  85 */         if (mask.get(1)) {
/*  86 */           position.setFixTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */         }
/*  88 */         if (mask.get(2)) {
/*  89 */           position.setLatitude(buf.readInt() / 360000.0D);
/*     */         }
/*  91 */         if (mask.get(3)) {
/*  92 */           position.setLongitude(buf.readInt() / 360000.0D);
/*     */         }
/*  94 */         if (mask.get(4)) {
/*  95 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*     */         }
/*  97 */         if (mask.get(5)) {
/*  98 */           position.setCourse(buf.readUnsignedShort() * 0.1D);
/*     */         }
/* 100 */         if (mask.get(6)) {
/* 101 */           position.setAltitude(buf.readShort());
/*     */         }
/* 103 */         if (mask.get(7)) {
/* 104 */           int flags = buf.readUnsignedByte();
/* 105 */           position.setValid(BitUtil.check(flags, 0));
/* 106 */           position.set("sat", Integer.valueOf(BitUtil.from(flags, 4)));
/*     */         } 
/* 108 */         if (mask.get(8)) {
/* 109 */           long flags = buf.readUnsignedInt();
/* 110 */           position.set("ignition", Boolean.valueOf(BitUtil.check(flags, 0)));
/* 111 */           position.set("charge", Boolean.valueOf(BitUtil.check(flags, 1)));
/* 112 */           position.set("motion", Boolean.valueOf(BitUtil.check(flags, 2)));
/* 113 */           for (int j = 0; j < 3; j++) {
/* 114 */             position.set("in" + j, Boolean.valueOf(BitUtil.check(flags, 4 + j)));
/*     */           }
/*     */         } 
/* 117 */         if (mask.get(9)) {
/* 118 */           int flags = buf.readUnsignedShort();
/* 119 */           position.set("blocked", Boolean.valueOf(BitUtil.check(flags, 0)));
/* 120 */           position.set("in0", Boolean.valueOf(BitUtil.check(flags, 4)));
/*     */         }  int i;
/* 122 */         for (i = 10; i <= 14; i++) {
/* 123 */           if (mask.get(i)) {
/* 124 */             buf.readUnsignedShort();
/*     */           }
/*     */         } 
/* 127 */         if (mask.get(15)) {
/* 128 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 100L));
/*     */         }
/* 130 */         if (mask.get(16)) {
/* 131 */           buf.readUnsignedInt();
/*     */         }
/* 133 */         for (i = 17; i <= 27; i++) {
/* 134 */           if (mask.get(i)) {
/* 135 */             buf.readUnsignedByte();
/*     */           }
/*     */         } 
/* 138 */         for (i = 28; i <= 37; i += 2) {
/* 139 */           if (mask.get(i)) {
/* 140 */             buf.skipBytes(12);
/*     */           }
/* 142 */           if (mask.get(i + 1)) {
/* 143 */             buf.readUnsignedByte();
/*     */           }
/*     */         } 
/* 146 */         if (mask.get(38)) {
/* 147 */           buf.skipBytes(6);
/*     */         }
/* 149 */         if (mask.get(39)) {
/* 150 */           buf.readUnsignedShort();
/*     */         }
/* 152 */         if (mask.get(40)) {
/* 153 */           buf.readShort();
/*     */         }
/* 155 */         if (mask.get(41)) {
/* 156 */           buf.readShort();
/*     */         }
/* 158 */         if (mask.get(42)) {
/* 159 */           buf.readShort();
/*     */         }
/* 161 */         if (mask.get(43)) {
/* 162 */           buf.skipBytes(10);
/*     */         }
/* 164 */         if (mask.get(44)) {
/* 165 */           buf.readUnsignedShort();
/*     */         }
/* 167 */         for (i = 45; i <= 49; i++) {
/* 168 */           if (mask.get(i)) {
/* 169 */             buf.readUnsignedByte();
/*     */           }
/*     */         } 
/* 172 */         if (mask.get(50)) {
/* 173 */           buf.readShort();
/*     */         }
/* 175 */         if (mask.get(51)) {
/* 176 */           buf.readUnsignedInt();
/*     */         }
/* 178 */         if (mask.get(52)) {
/* 179 */           buf.readUnsignedInt();
/*     */         }
/*     */         
/* 182 */         if (position.getDeviceTime() != null && position.getFixTime() != null) {
/* 183 */           positions.add(position);
/*     */         }
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 190 */       return positions.isEmpty() ? null : positions;
/*     */     } 
/*     */ 
/*     */     
/* 194 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RadarProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */