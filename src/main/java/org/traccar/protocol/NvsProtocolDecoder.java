/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
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
/*     */ public class NvsProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public NvsProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, String response) {
/*  41 */     if (channel != null) {
/*  42 */       channel.writeAndFlush(new NetworkMessage(
/*  43 */             Unpooled.copiedBuffer(response, StandardCharsets.US_ASCII), remoteAddress));
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  51 */     ByteBuf buf = (ByteBuf)msg;
/*     */ 
/*     */     
/*  54 */     if (buf.getUnsignedByte(buf.readerIndex()) == 0) {
/*     */       
/*  56 */       buf.readUnsignedShort();
/*     */       
/*  58 */       String imei = buf.toString(buf.readerIndex(), 15, StandardCharsets.US_ASCII);
/*     */       
/*  60 */       if (getDeviceSession(channel, remoteAddress, new String[] { imei }) != null) {
/*  61 */         sendResponse(channel, remoteAddress, "OK");
/*     */       } else {
/*  63 */         sendResponse(channel, remoteAddress, "NO01");
/*     */       }
/*     */     
/*     */     } else {
/*     */       
/*  68 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  69 */       if (deviceSession == null) {
/*  70 */         return null;
/*     */       }
/*     */       
/*  73 */       List<Position> positions = new LinkedList<>();
/*     */       
/*  75 */       buf.skipBytes(4);
/*  76 */       buf.readUnsignedShort();
/*  77 */       buf.readLong();
/*  78 */       buf.readUnsignedByte();
/*  79 */       int count = buf.readUnsignedByte();
/*     */       
/*  81 */       for (int i = 0; i < count; i++) {
/*  82 */         Position position = new Position(getProtocolName());
/*  83 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/*  85 */         position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */         
/*  87 */         position.set("reason", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*  89 */         position.setLongitude(buf.readInt() / 1.0E7D);
/*  90 */         position.setLatitude(buf.readInt() / 1.0E7D);
/*  91 */         position.setAltitude(buf.readShort());
/*  92 */         position.setCourse(buf.readUnsignedShort());
/*     */         
/*  94 */         position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*  96 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*  97 */         position.setValid((buf.readUnsignedByte() != 0));
/*     */         
/*  99 */         buf.readUnsignedByte();
/*     */         
/* 101 */         buf.readUnsignedByte();
/*     */ 
/*     */         
/* 104 */         int cnt = buf.readUnsignedByte(); int j;
/* 105 */         for (j = 0; j < cnt; j++) {
/* 106 */           position.set("io" + buf.readUnsignedByte(), Short.valueOf(buf.readUnsignedByte()));
/*     */         }
/*     */ 
/*     */         
/* 110 */         cnt = buf.readUnsignedByte();
/* 111 */         for (j = 0; j < cnt; j++) {
/* 112 */           position.set("io" + buf.readUnsignedByte(), Integer.valueOf(buf.readUnsignedShort()));
/*     */         }
/*     */ 
/*     */         
/* 116 */         cnt = buf.readUnsignedByte();
/* 117 */         for (j = 0; j < cnt; j++) {
/* 118 */           position.set("io" + buf.readUnsignedByte(), Long.valueOf(buf.readUnsignedInt()));
/*     */         }
/*     */ 
/*     */         
/* 122 */         cnt = buf.readUnsignedByte();
/* 123 */         for (j = 0; j < cnt; j++) {
/* 124 */           position.set("io" + buf.readUnsignedByte(), Long.valueOf(buf.readLong()));
/*     */         }
/*     */         
/* 127 */         positions.add(position);
/*     */       } 
/*     */       
/* 130 */       return positions;
/*     */     } 
/*     */ 
/*     */     
/* 134 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NvsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */