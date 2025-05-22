/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
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
/*     */ public class AdmProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int CMD_RESPONSE_SIZE = 132;
/*     */   public static final int MSG_IMEI = 3;
/*     */   public static final int MSG_PHOTO = 10;
/*     */   public static final int MSG_ADM5 = 1;
/*     */   
/*     */   public AdmProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeData(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int type) {
/*  44 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  45 */     if (deviceSession == null) {
/*  46 */       return null;
/*     */     }
/*     */     
/*  49 */     if (BitUtil.to(type, 2) == 0) {
/*  50 */       Position position = new Position(getProtocolName());
/*  51 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  53 */       position.set("versionFw", Short.valueOf(buf.readUnsignedByte()));
/*  54 */       position.set("index", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/*  56 */       int status = buf.readUnsignedShortLE();
/*  57 */       position.set("status", Integer.valueOf(status));
/*  58 */       position.setValid(!BitUtil.check(status, 5));
/*  59 */       position.setLatitude(buf.readFloatLE());
/*  60 */       position.setLongitude(buf.readFloatLE());
/*  61 */       position.setCourse(buf.readUnsignedShortLE() * 0.1D);
/*  62 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE() * 0.1D));
/*     */       
/*  64 */       position.set("acceleration", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*  65 */       position.setAltitude(buf.readShortLE());
/*  66 */       position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*  67 */       position.set("sat", Integer.valueOf(buf.readUnsignedByte() & 0xF));
/*     */       
/*  69 */       position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */       
/*  71 */       position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/*  72 */       position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/*     */       
/*  74 */       if (BitUtil.check(type, 2)) {
/*  75 */         buf.readUnsignedByte();
/*  76 */         buf.readUnsignedByte();
/*     */         
/*  78 */         int out = buf.readUnsignedByte();
/*  79 */         for (int i = 0; i <= 3; i++) {
/*  80 */           position.set("out" + (i + 1), Integer.valueOf(BitUtil.check(out, i) ? 1 : 0));
/*     */         }
/*     */         
/*  83 */         buf.readUnsignedByte();
/*     */       } 
/*     */       
/*  86 */       if (BitUtil.check(type, 3)) {
/*  87 */         for (int i = 1; i <= 6; i++) {
/*  88 */           position.set("adc" + i, Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/*     */         }
/*     */       }
/*     */       
/*  92 */       if (BitUtil.check(type, 4)) {
/*  93 */         for (int i = 1; i <= 2; i++) {
/*  94 */           position.set("count" + i, Long.valueOf(buf.readUnsignedIntLE()));
/*     */         }
/*     */       }
/*     */       
/*  98 */       if (BitUtil.check(type, 5)) {
/*  99 */         int i; for (i = 1; i <= 3; i++) {
/* 100 */           buf.readUnsignedShortLE();
/*     */         }
/* 102 */         for (i = 1; i <= 3; i++) {
/* 103 */           position.set("temp" + i, Short.valueOf(buf.readUnsignedByte()));
/*     */         }
/*     */       } 
/*     */       
/* 107 */       if (BitUtil.check(type, 6)) {
/* 108 */         int endIndex = buf.readerIndex() + buf.readUnsignedByte();
/* 109 */         while (buf.readerIndex() < endIndex) {
/* 110 */           long value; int mask = buf.readUnsignedByte();
/*     */           
/* 112 */           switch (BitUtil.from(mask, 6)) {
/*     */             case 3:
/* 114 */               value = buf.readLongLE();
/*     */               break;
/*     */             case 2:
/* 117 */               value = buf.readUnsignedIntLE();
/*     */               break;
/*     */             case 1:
/* 120 */               value = buf.readUnsignedShortLE();
/*     */               break;
/*     */             default:
/* 123 */               value = buf.readUnsignedByte();
/*     */               break;
/*     */           } 
/* 126 */           int index = BitUtil.to(mask, 6);
/* 127 */           switch (index) {
/*     */             case 1:
/* 129 */               position.set("temp1", Long.valueOf(value));
/*     */               continue;
/*     */             case 2:
/* 132 */               position.set("humidity", Long.valueOf(value));
/*     */               continue;
/*     */             case 3:
/* 135 */               position.set("illumination", Long.valueOf(value));
/*     */               continue;
/*     */             case 4:
/* 138 */               position.set("battery", Long.valueOf(value));
/*     */               continue;
/*     */           } 
/* 141 */           position.set("can" + index, Long.valueOf(value));
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 147 */       if (BitUtil.check(type, 7)) {
/* 148 */         position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */       }
/*     */       
/* 151 */       return position;
/*     */     } 
/*     */     
/* 154 */     return null;
/*     */   }
/*     */   
/*     */   private Position parseCommandResponse(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 158 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 159 */     if (deviceSession == null) {
/* 160 */       return null;
/*     */     }
/*     */     
/* 163 */     Position position = new Position(getProtocolName());
/* 164 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 166 */     getLastLocation(position, null);
/*     */     
/* 168 */     int responseTextLength = buf.bytesBefore((byte)0);
/* 169 */     if (responseTextLength < 0) {
/* 170 */       responseTextLength = 129;
/*     */     }
/* 172 */     position.set("result", buf.readSlice(responseTextLength).toString(StandardCharsets.UTF_8));
/*     */     
/* 174 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 179 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 181 */     buf.readUnsignedShortLE();
/*     */     
/* 183 */     int size = buf.readUnsignedByte();
/* 184 */     if (size != 132) {
/* 185 */       int type = buf.readUnsignedByte();
/* 186 */       if (type == 3) {
/* 187 */         getDeviceSession(channel, remoteAddress, new String[] { buf.readSlice(15).toString(StandardCharsets.UTF_8) });
/*     */       } else {
/* 189 */         return decodeData(channel, remoteAddress, buf, type);
/*     */       } 
/*     */     } else {
/* 192 */       return parseCommandResponse(channel, remoteAddress, buf);
/*     */     } 
/*     */     
/* 195 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AdmProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */