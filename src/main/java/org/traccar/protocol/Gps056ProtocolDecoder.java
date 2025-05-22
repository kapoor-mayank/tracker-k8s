/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
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
/*     */ 
/*     */ 
/*     */ public class Gps056ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Gps056ProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*     */   private static void sendResponse(Channel channel, String type, String imei, ByteBuf content) {
/*  39 */     if (channel != null) {
/*  40 */       ByteBuf response = Unpooled.buffer();
/*  41 */       String header = "*" + type + imei;
/*  42 */       response.writeBytes(header.getBytes(StandardCharsets.US_ASCII));
/*  43 */       if (content != null) {
/*  44 */         response.writeBytes(content);
/*     */       }
/*  46 */       response.writeByte(35);
/*  47 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private static double decodeCoordinate(ByteBuf buf) {
/*  52 */     double degrees = (buf.getUnsignedShort(buf.readerIndex()) / 100);
/*  53 */     double minutes = (buf.readUnsignedShort() % 100) + buf.readUnsignedShort() * 1.0E-4D;
/*  54 */     degrees += minutes / 60.0D;
/*  55 */     byte hemisphere = buf.readByte();
/*  56 */     if (hemisphere == 83 || hemisphere == 87) {
/*  57 */       degrees = -degrees;
/*     */     }
/*  59 */     return degrees;
/*     */   }
/*     */ 
/*     */   
/*     */   private static void decodeStatus(ByteBuf buf, Position position) {
/*  64 */     position.set("input", Short.valueOf(buf.readUnsignedByte()));
/*  65 */     position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/*  67 */     position.set("adc1", Double.valueOf(buf.readShortLE() * 5.06D));
/*     */     
/*  69 */     position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*  70 */     position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  78 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  80 */     buf.skipBytes(2);
/*  81 */     buf.skipBytes(2);
/*     */     
/*  83 */     String type = buf.readSlice(7).toString(StandardCharsets.US_ASCII);
/*  84 */     String imei = buf.readSlice(15).toString(StandardCharsets.US_ASCII);
/*     */     
/*  86 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  87 */     if (deviceSession == null) {
/*  88 */       return null;
/*     */     }
/*     */     
/*  91 */     if (type.startsWith("LOGN")) {
/*     */       
/*  93 */       ByteBuf content = Unpooled.copiedBuffer("1", StandardCharsets.US_ASCII);
/*     */       try {
/*  95 */         sendResponse(channel, "LGSA" + type.substring(4), imei, content);
/*     */       } finally {
/*  97 */         content.release();
/*     */       } 
/*     */     } else {
/* 100 */       if (type.startsWith("GPSL")) {
/*     */         
/* 102 */         Position position = new Position(getProtocolName());
/* 103 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */         
/* 107 */         DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*     */         
/* 109 */         position.setValid(true);
/* 110 */         position.setTime(dateBuilder.getDate());
/* 111 */         position.setLatitude(decodeCoordinate(buf));
/* 112 */         position.setLongitude(decodeCoordinate(buf));
/* 113 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/* 114 */         position.setCourse(buf.readUnsignedShort());
/*     */         
/* 116 */         decodeStatus(buf, position);
/*     */         
/* 118 */         sendResponse(channel, "GPSA" + type.substring(4), imei, buf.readSlice(2));
/*     */         
/* 120 */         return position;
/*     */       } 
/* 122 */       if (type.startsWith("SYNC")) {
/*     */         
/* 124 */         Position position = new Position(getProtocolName());
/* 125 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 127 */         getLastLocation(position, null);
/*     */         
/* 129 */         decodeStatus(buf, position);
/*     */         
/* 131 */         sendResponse(channel, "SYSA" + type.substring(4), imei, (ByteBuf)null);
/*     */         
/* 133 */         return position;
/*     */       } 
/*     */     } 
/*     */     
/* 137 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gps056ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */