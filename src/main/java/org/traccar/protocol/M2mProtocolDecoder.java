/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class M2mProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private boolean firstPacket;
/*     */   
/*     */   public M2mProtocolDecoder(Protocol protocol) {
/*  31 */     super(protocol);
/*     */ 
/*     */     
/*  34 */     this.firstPacket = true;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  40 */     ByteBuf buf = (ByteBuf)msg;
/*     */ 
/*     */     
/*  43 */     for (int i = 0; i < buf.readableBytes(); i++) {
/*  44 */       int b = buf.getByte(i);
/*  45 */       if (b != 11) {
/*  46 */         buf.setByte(i, b - 32);
/*     */       }
/*     */     } 
/*     */     
/*  50 */     if (this.firstPacket) {
/*     */       
/*  52 */       this.firstPacket = false;
/*     */       
/*  54 */       StringBuilder imei = new StringBuilder();
/*  55 */       for (int j = 0; j < 8; j++) {
/*  56 */         int b = buf.readByte();
/*  57 */         if (j != 0) {
/*  58 */           imei.append(b / 10);
/*     */         }
/*  60 */         imei.append(b % 10);
/*     */       } 
/*     */       
/*  63 */       getDeviceSession(channel, remoteAddress, new String[] { imei.toString() });
/*     */     }
/*     */     else {
/*     */       
/*  67 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  68 */       if (deviceSession == null) {
/*  69 */         return null;
/*     */       }
/*     */       
/*  72 */       Position position = new Position(getProtocolName());
/*  73 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  81 */       DateBuilder dateBuilder = (new DateBuilder()).setDay(buf.readUnsignedByte() & 0x3F).setMonth(buf.readUnsignedByte() & 0x3F).setYear(buf.readUnsignedByte()).setHour(buf.readUnsignedByte() & 0x3F).setMinute(buf.readUnsignedByte() & 0x7F).setSecond(buf.readUnsignedByte() & 0x7F);
/*  82 */       position.setTime(dateBuilder.getDate());
/*     */       
/*  84 */       int degrees = buf.readUnsignedByte();
/*  85 */       double latitude = buf.readUnsignedByte();
/*  86 */       latitude += buf.readUnsignedByte() / 100.0D;
/*  87 */       latitude += buf.readUnsignedByte() / 10000.0D;
/*  88 */       latitude /= 60.0D;
/*  89 */       latitude += degrees;
/*     */       
/*  91 */       int b = buf.readUnsignedByte();
/*     */       
/*  93 */       degrees = (b & 0x7F) * 100 + buf.readUnsignedByte();
/*  94 */       double longitude = buf.readUnsignedByte();
/*  95 */       longitude += buf.readUnsignedByte() / 100.0D;
/*  96 */       longitude += buf.readUnsignedByte() / 10000.0D;
/*  97 */       longitude /= 60.0D;
/*  98 */       longitude += degrees;
/*     */       
/* 100 */       if ((b & 0x80) != 0) {
/* 101 */         longitude = -longitude;
/*     */       }
/* 103 */       if ((b & 0x40) != 0) {
/* 104 */         latitude = -latitude;
/*     */       }
/*     */       
/* 107 */       position.setValid(true);
/* 108 */       position.setLatitude(latitude);
/* 109 */       position.setLongitude(longitude);
/* 110 */       position.setSpeed(buf.readUnsignedByte());
/*     */       
/* 112 */       int satellites = buf.readUnsignedByte();
/* 113 */       if (satellites == 0) {
/* 114 */         return null;
/*     */       }
/* 116 */       position.set("sat", Integer.valueOf(satellites));
/*     */ 
/*     */ 
/*     */       
/* 120 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 124 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\M2mProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */