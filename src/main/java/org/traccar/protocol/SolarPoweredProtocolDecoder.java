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
/*     */ public class SolarPoweredProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_ACTIVE_REPORTING = 17;
/*     */   
/*     */   public SolarPoweredProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  43 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  45 */     buf.readUnsignedByte();
/*     */     
/*  47 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(0, 15);
/*  48 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  49 */     if (deviceSession == null) {
/*  50 */       return null;
/*     */     }
/*     */     
/*  53 */     int type = buf.readUnsignedByte();
/*  54 */     buf.readUnsignedShort();
/*     */     
/*  56 */     if (type == 17) {
/*     */       
/*  58 */       Position position = new Position(getProtocolName());
/*  59 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  61 */       while (buf.readableBytes() > 2) {
/*  62 */         int status; DateBuilder dateBuilder; int temperature, alarmMask, alarm, tag = buf.readUnsignedByte();
/*  63 */         int length = buf.readUnsignedByte();
/*  64 */         switch (tag) {
/*     */           case 129:
/*  66 */             status = buf.readUnsignedByte();
/*     */ 
/*     */             
/*  69 */             dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  70 */             position.setTime(dateBuilder.getDate());
/*  71 */             position.setLatitude(buf.readUnsignedInt() * 1.0E-6D);
/*  72 */             if (BitUtil.check(status, 3)) {
/*  73 */               position.setLatitude(-position.getLatitude());
/*     */             }
/*  75 */             position.setLongitude(buf.readUnsignedInt() * 1.0E-6D);
/*  76 */             if (BitUtil.check(status, 2)) {
/*  77 */               position.setLongitude(-position.getLongitude());
/*     */             }
/*  79 */             position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*  80 */             temperature = buf.readUnsignedByte();
/*  81 */             if (BitUtil.check(temperature, 7)) {
/*  82 */               position.set("deviceTemp", Integer.valueOf(-BitUtil.to(temperature, 7)));
/*     */             } else {
/*  84 */               position.set("deviceTemp", Integer.valueOf(BitUtil.to(temperature, 7)));
/*     */             } 
/*  86 */             position.set("battery", Double.valueOf(buf.readUnsignedByte() * 0.02D));
/*  87 */             position.setCourse(buf.readUnsignedByte());
/*     */             continue;
/*     */           case 130:
/*  90 */             alarmMask = buf.readUnsignedByte();
/*  91 */             alarm = buf.readUnsignedByte();
/*  92 */             if (BitUtil.check(alarmMask, 0) && BitUtil.check(alarm, 0)) {
/*  93 */               position.set("alarm", "tampering");
/*     */             }
/*  95 */             if (BitUtil.check(alarmMask, 1) && BitUtil.check(alarm, 1)) {
/*  96 */               position.set("alarm", "lowPower");
/*     */             }
/*  98 */             if (BitUtil.check(alarmMask, 2) && BitUtil.check(alarm, 2)) {
/*  99 */               position.set("alarm", "sos");
/*     */             }
/* 101 */             if (BitUtil.check(alarmMask, 3) && BitUtil.check(alarm, 3)) {
/* 102 */               position.set("alarm", "fallDown");
/*     */             }
/* 104 */             if (BitUtil.check(alarmMask, 4)) {
/* 105 */               position.set("motion", Boolean.valueOf(BitUtil.check(alarm, 4)));
/*     */             }
/*     */             continue;
/*     */           case 131:
/* 109 */             buf.readUnsignedInt();
/* 110 */             buf.readUnsignedInt();
/* 111 */             buf.readUnsignedInt();
/* 112 */             buf.readUnsignedByte();
/* 113 */             buf.readUnsignedByte();
/* 114 */             buf.readUnsignedByte();
/* 115 */             position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */             continue;
/*     */         } 
/* 118 */         buf.skipBytes(length);
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 123 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 127 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SolarPoweredProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */