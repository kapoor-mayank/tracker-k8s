/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ArnaviBinaryProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final byte HEADER_START_SIGN = -1;
/*     */   private static final byte HEADER_VERSION_1 = 34;
/*     */   private static final byte HEADER_VERSION_2 = 35;
/*     */   private static final byte RECORD_PING = 0;
/*     */   private static final byte RECORD_DATA = 1;
/*     */   private static final byte RECORD_TEXT = 3;
/*     */   private static final byte RECORD_FILE = 4;
/*     */   private static final byte RECORD_BINARY = 6;
/*     */   private static final byte TAG_LATITUDE = 3;
/*     */   private static final byte TAG_LONGITUDE = 4;
/*     */   private static final byte TAG_COORD_PARAMS = 5;
/*     */   
/*     */   public ArnaviBinaryProtocolDecoder(Protocol protocol) {
/*  53 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, byte version, int index) {
/*  57 */     if (channel != null) {
/*  58 */       ByteBuf response = Unpooled.buffer();
/*  59 */       response.writeByte(123);
/*  60 */       if (version == 34) {
/*  61 */         response.writeByte(0);
/*  62 */         response.writeByte((byte)index);
/*  63 */       } else if (version == 35) {
/*  64 */         response.writeByte(4);
/*  65 */         response.writeByte(0);
/*  66 */         ByteBuffer time = ByteBuffer.allocate(4).putInt((int)(System.currentTimeMillis() / 1000L));
/*  67 */         time.position(0);
/*  68 */         response.writeByte(Checksum.modulo256(time.slice()));
/*  69 */         response.writeBytes(time);
/*     */       } 
/*  71 */       response.writeByte(125);
/*  72 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, ByteBuf buf, int length, Date time) {
/*  78 */     Position position = new Position();
/*  79 */     position.setProtocol(getProtocolName());
/*  80 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  82 */     position.setTime(time);
/*     */     
/*  84 */     int readBytes = 0;
/*  85 */     while (readBytes < length) {
/*  86 */       byte satellites; short tag = buf.readUnsignedByte();
/*  87 */       switch (tag) {
/*     */         case 3:
/*  89 */           position.setLatitude(buf.readFloatLE());
/*  90 */           position.setValid(true);
/*     */           break;
/*     */         
/*     */         case 4:
/*  94 */           position.setLongitude(buf.readFloatLE());
/*  95 */           position.setValid(true);
/*     */           break;
/*     */         
/*     */         case 5:
/*  99 */           position.setCourse((buf.readUnsignedByte() * 2));
/* 100 */           position.setAltitude((buf.readUnsignedByte() * 10));
/* 101 */           satellites = buf.readByte();
/* 102 */           position.set("sat", Integer.valueOf(satellites & 15 + (satellites >> 4) & 0xF));
/* 103 */           position.setSpeed(buf.readUnsignedByte());
/*     */           break;
/*     */         
/*     */         default:
/* 107 */           buf.skipBytes(4);
/*     */           break;
/*     */       } 
/*     */       
/* 111 */       readBytes += 5;
/*     */     } 
/*     */     
/* 114 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 120 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 122 */     byte startSign = buf.readByte();
/*     */     
/* 124 */     if (startSign == -1) {
/*     */       
/* 126 */       byte version = buf.readByte();
/*     */       
/* 128 */       String imei = String.valueOf(buf.readLongLE());
/* 129 */       DeviceSession deviceSession1 = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */       
/* 131 */       if (deviceSession1 != null) {
/* 132 */         sendResponse(channel, version, 0);
/*     */       }
/*     */       
/* 135 */       return null;
/*     */     } 
/*     */     
/* 138 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 139 */     if (deviceSession == null) {
/* 140 */       return null;
/*     */     }
/*     */     
/* 143 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 145 */     int index = buf.readUnsignedByte();
/*     */     
/* 147 */     byte recordType = buf.readByte();
/* 148 */     while (buf.readableBytes() > 0) {
/* 149 */       int length; Date time; switch (recordType) {
/*     */         case 0:
/*     */         case 1:
/*     */         case 3:
/*     */         case 4:
/*     */         case 6:
/* 155 */           length = buf.readUnsignedShortLE();
/* 156 */           time = new Date(buf.readUnsignedIntLE() * 1000L);
/*     */           
/* 158 */           if (recordType == 1) {
/* 159 */             positions.add(decodePosition(deviceSession, buf, length, time));
/*     */           } else {
/* 161 */             buf.readBytes(length);
/*     */           } 
/*     */           
/* 164 */           buf.readUnsignedByte();
/*     */           break;
/*     */         
/*     */         default:
/* 168 */           return null;
/*     */       } 
/*     */       
/* 171 */       recordType = buf.readByte();
/*     */     } 
/*     */     
/* 174 */     sendResponse(channel, (byte)34, index);
/*     */     
/* 176 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ArnaviBinaryProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */