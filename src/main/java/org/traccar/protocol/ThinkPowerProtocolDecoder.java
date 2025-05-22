/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BufferUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ public class ThinkPowerProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN_REQUEST = 1;
/*     */   public static final int MSG_LOGIN_RESPONSE = 2;
/*     */   public static final int MSG_HEARTBEAT_REQUEST = 3;
/*     */   public static final int MSG_HEARTBEAT_RESPONSE = 4;
/*     */   public static final int MSG_RECORD_REPORT = 5;
/*     */   public static final int MSG_RECORD_RESPONSE = 6;
/*     */   
/*     */   public ThinkPowerProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, int type, int index, ByteBuf content) {
/*  48 */     if (channel != null) {
/*  49 */       ByteBuf response = Unpooled.buffer();
/*  50 */       response.writeByte(type);
/*  51 */       response.writeByte(index);
/*  52 */       if (content != null) {
/*  53 */         response.writeShort(content.readableBytes());
/*  54 */         response.writeBytes(content);
/*  55 */         content.release();
/*     */       } else {
/*  57 */         response.writeShort(0);
/*     */       } 
/*  59 */       response.writeShort(Checksum.crc16(Checksum.CRC16_CCITT_FALSE, response.nioBuffer()));
/*  60 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeValue(Position position, int type, ByteBuf buf) {
/*  65 */     switch (type) {
/*     */       case 1:
/*  67 */         position.setValid(true);
/*  68 */         position.setLatitude(BufferUtil.readSignedMagnitudeInt(buf) * 1.0E-7D);
/*  69 */         position.setLongitude(BufferUtil.readSignedMagnitudeInt(buf) * 1.0E-7D);
/*  70 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1D));
/*  71 */         position.setCourse(buf.readUnsignedShort() * 0.01D);
/*     */         break;
/*     */       case 2:
/*  74 */         position.setValid((buf.readUnsignedByte() > 0));
/*     */         break;
/*     */       case 3:
/*  77 */         buf.skipBytes(3);
/*     */         break;
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/*  82 */         buf.skipBytes(2);
/*     */         break;
/*     */       case 9:
/*  85 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 10:
/*  88 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 16:
/*  91 */         if (buf.readUnsignedByte() > 0) {
/*  92 */           position.set("alarm", "sos");
/*     */         }
/*     */         break;
/*     */       case 18:
/*  96 */         position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/*     */         break;
/*     */       case 19:
/*  99 */         if (buf.readUnsignedByte() > 0) {
/* 100 */           position.set("alarm", "lowBattery");
/*     */         }
/*     */         break;
/*     */       case 22:
/* 104 */         buf.readUnsignedShort();
/*     */         break;
/*     */       case 23:
/* 107 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 24:
/* 110 */         buf.readUnsignedShort();
/*     */         break;
/*     */       case 25:
/* 113 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 80:
/* 116 */         if (buf.readUnsignedByte() > 0) {
/* 117 */           position.set("alarm", "removing");
/*     */         }
/*     */         break;
/*     */       case 81:
/* 121 */         if (buf.readUnsignedByte() > 0) {
/* 122 */           position.set("alarm", "tampering");
/*     */         }
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 134 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 136 */     int type = buf.readUnsignedByte();
/* 137 */     int index = buf.readUnsignedByte();
/* 138 */     buf.readUnsignedShort();
/*     */     
/* 140 */     if (type == 1) {
/*     */       
/* 142 */       buf.readUnsignedByte();
/* 143 */       buf.readUnsignedByte();
/*     */       
/* 145 */       String id = buf.readCharSequence(buf.readUnsignedByte(), StandardCharsets.US_ASCII).toString();
/* 146 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*     */       
/* 148 */       ByteBuf content = Unpooled.buffer();
/* 149 */       content.writeByte((deviceSession != null) ? 0 : 4);
/* 150 */       sendResponse(channel, 2, index, content);
/*     */     }
/* 152 */     else if (type == 3) {
/*     */       
/* 154 */       sendResponse(channel, 4, index, (ByteBuf)null);
/*     */     }
/* 156 */     else if (type == 5) {
/*     */       
/* 158 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 159 */       if (deviceSession == null) {
/* 160 */         return null;
/*     */       }
/*     */       
/* 163 */       buf.readUnsignedByte();
/*     */       
/* 165 */       Position position = new Position(getProtocolName());
/* 166 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 168 */       position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */       
/* 170 */       while (buf.readableBytes() > 2) {
/* 171 */         decodeValue(position, buf.readUnsignedByte(), buf);
/*     */       }
/*     */       
/* 174 */       sendResponse(channel, 6, index, (ByteBuf)null);
/*     */       
/* 176 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 180 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ThinkPowerProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */