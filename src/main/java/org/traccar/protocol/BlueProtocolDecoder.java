/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ 
/*     */ 
/*     */ public class BlueProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public BlueProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */ 
/*     */   
/*     */   private double readCoordinate(ByteBuf buf, boolean negative) {
/*  40 */     int value = buf.readUnsignedShort();
/*  41 */     int degrees = value / 100;
/*  42 */     double minutes = (value % 100) + buf.readUnsignedShort() * 1.0E-4D;
/*  43 */     double coordinate = degrees + minutes / 60.0D;
/*  44 */     return negative ? -coordinate : coordinate;
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, int deviceIndex) {
/*  48 */     if (channel != null) {
/*     */       
/*  50 */       ByteBuf response = Unpooled.buffer();
/*  51 */       response.writeByte(170);
/*  52 */       response.writeShort(11);
/*  53 */       response.writeByte(134);
/*  54 */       response.writeByte(0);
/*     */       
/*  56 */       response.writeByte(6);
/*  57 */       response.writeByte(164);
/*  58 */       response.writeByte(0);
/*  59 */       response.writeByte(deviceIndex);
/*  60 */       response.writeByte(0);
/*  61 */       response.writeByte(0);
/*     */       
/*  63 */       response.writeByte(Checksum.xor(response.nioBuffer(1, response.writerIndex() - 1)));
/*     */       
/*  65 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  70 */     switch (value) {
/*     */       case 1:
/*  72 */         return "sos";
/*     */       case 8:
/*  74 */         return "overspeed";
/*     */       case 19:
/*  76 */         return "lowPower";
/*     */     } 
/*  78 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  86 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  88 */     buf.readUnsignedByte();
/*  89 */     buf.readUnsignedShort();
/*  90 */     buf.readUnsignedByte();
/*  91 */     buf.readUnsignedByte();
/*     */     
/*  93 */     String id = String.valueOf(buf.readUnsignedInt());
/*  94 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*  95 */     if (deviceSession == null) {
/*  96 */       return null;
/*     */     }
/*     */     
/*  99 */     Position position = new Position(getProtocolName());
/* 100 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 102 */     while (buf.readableBytes() > 1) {
/*     */       
/* 104 */       int frameEnd = buf.readerIndex() + buf.readUnsignedByte();
/*     */       
/* 106 */       int type = buf.readUnsignedByte();
/* 107 */       int index = buf.readUnsignedByte();
/* 108 */       buf.readUnsignedByte();
/* 109 */       buf.readUnsignedByte();
/*     */       
/* 111 */       if (type == 1) {
/*     */         
/* 113 */         buf.readUnsignedByte();
/* 114 */         int flags = buf.readUnsignedByte();
/*     */         
/* 116 */         position.setValid(BitUtil.check(flags, 7));
/* 117 */         position.setLatitude(readCoordinate(buf, BitUtil.check(flags, 6)));
/* 118 */         position.setLongitude(readCoordinate(buf, BitUtil.check(flags, 5)));
/* 119 */         position.setSpeed(buf.readUnsignedShort() + buf.readUnsignedShort() * 0.001D);
/* 120 */         position.setCourse(buf.readUnsignedShort() + buf.readUnsignedByte() * 0.01D);
/*     */ 
/*     */ 
/*     */         
/* 124 */         DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 125 */         position.setTime(dateBuilder.getDate());
/*     */         
/* 127 */         buf.readUnsignedShort();
/* 128 */         buf.readUnsignedShort();
/*     */       }
/* 130 */       else if (type == 18) {
/*     */ 
/*     */ 
/*     */         
/* 134 */         int status = buf.readUnsignedByte();
/* 135 */         position.set("alarm", BitUtil.check(status, 1) ? "vibration" : null);
/*     */         
/* 137 */         buf.readUnsignedByte();
/* 138 */         buf.readUnsignedByte();
/*     */         
/* 140 */         status = buf.readUnsignedByte();
/* 141 */         int ignition = BitUtil.between(status, 2, 4);
/* 142 */         if (ignition == 1) {
/* 143 */           position.set("ignition", Boolean.valueOf(false));
/*     */         }
/* 145 */         if (ignition == 2) {
/* 146 */           position.set("ignition", Boolean.valueOf(true));
/*     */         }
/*     */         
/* 149 */         buf.readUnsignedByte();
/* 150 */         buf.readUnsignedByte();
/*     */         
/* 152 */         position.set("status", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/* 154 */       else if (type == 129) {
/*     */         
/* 156 */         position.set("alarm", decodeAlarm(buf.readUnsignedByte()));
/*     */       }
/* 158 */       else if (type == 132) {
/*     */         
/* 160 */         sendResponse(channel, index);
/*     */       } 
/*     */ 
/*     */       
/* 164 */       buf.readerIndex(frameEnd);
/*     */     } 
/*     */     
/* 167 */     return (position.getFixTime() != null) ? position : null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BlueProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */