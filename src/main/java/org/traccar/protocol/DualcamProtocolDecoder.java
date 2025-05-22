/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ public class DualcamProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_INIT = 0;
/*     */   public static final int MSG_START = 1;
/*     */   public static final int MSG_RESUME = 2;
/*     */   public static final int MSG_SYNC = 3;
/*     */   public static final int MSG_DATA = 4;
/*     */   public static final int MSG_COMPLETE = 5;
/*     */   public static final int MSG_FILE_REQUEST = 8;
/*     */   public static final int MSG_INIT_REQUEST = 9;
/*     */   private String uniqueId;
/*     */   private int packetCount;
/*     */   private int currentPacket;
/*     */   private boolean video;
/*     */   private ByteBuf media;
/*     */   
/*     */   public DualcamProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
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
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     DeviceSession deviceSession;
/*     */     long settings;
/*  57 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  59 */     int type = buf.readUnsignedShort();
/*     */     
/*  61 */     switch (type) {
/*     */       case 0:
/*  63 */         buf.readUnsignedShort();
/*  64 */         this.uniqueId = String.valueOf(buf.readLong());
/*  65 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[] { this.uniqueId });
/*  66 */         settings = buf.readUnsignedInt();
/*  67 */         if (channel != null && deviceSession != null) {
/*  68 */           ByteBuf response = Unpooled.buffer();
/*  69 */           if (BitUtil.between(settings, 26, 30) > 0L) {
/*  70 */             String file; response.writeShort(8);
/*     */             
/*  72 */             if (BitUtil.check(settings, 26)) {
/*  73 */               this.video = false;
/*  74 */               file = "%photof";
/*  75 */             } else if (BitUtil.check(settings, 27)) {
/*  76 */               this.video = false;
/*  77 */               file = "%photor";
/*  78 */             } else if (BitUtil.check(settings, 28)) {
/*  79 */               this.video = true;
/*  80 */               file = "%videof";
/*     */             } else {
/*  82 */               this.video = true;
/*  83 */               file = "%videor";
/*     */             } 
/*  85 */             response.writeShort(file.length());
/*  86 */             response.writeCharSequence(file, StandardCharsets.US_ASCII);
/*     */           } else {
/*  88 */             response.writeShort(0);
/*     */           } 
/*  90 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         } 
/*     */         break;
/*     */       case 1:
/*  94 */         buf.readUnsignedShort();
/*  95 */         this.packetCount = buf.readInt();
/*  96 */         this.currentPacket = 1;
/*  97 */         this.media = Unpooled.buffer();
/*  98 */         if (channel != null) {
/*  99 */           ByteBuf response = Unpooled.buffer();
/* 100 */           response.writeShort(2);
/* 101 */           response.writeShort(4);
/* 102 */           response.writeInt(this.currentPacket);
/* 103 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         } 
/*     */         break;
/*     */       case 4:
/* 107 */         buf.readUnsignedShort();
/* 108 */         this.media.writeBytes(buf, buf.readableBytes() - 2);
/* 109 */         if (this.currentPacket == this.packetCount) {
/* 110 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 111 */           Position position = new Position(getProtocolName());
/* 112 */           position.setDeviceId(deviceSession.getDeviceId());
/* 113 */           getLastLocation(position, null);
/*     */           try {
/* 115 */             if (this.video) {
/* 116 */               position.set("video", Context.getMediaManager().writeFile(this.uniqueId, this.media, "h265"));
/*     */             } else {
/* 118 */               position.set("image", Context.getMediaManager().writeFile(this.uniqueId, this.media, "jpg"));
/*     */             } 
/*     */           } finally {
/* 121 */             this.media.release();
/* 122 */             this.media = null;
/*     */           } 
/* 124 */           if (channel != null) {
/* 125 */             ByteBuf response = Unpooled.buffer();
/* 126 */             response.writeShort(9);
/* 127 */             channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */           } 
/* 129 */           return position;
/*     */         } 
/* 131 */         this.currentPacket++;
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 138 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DualcamProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */