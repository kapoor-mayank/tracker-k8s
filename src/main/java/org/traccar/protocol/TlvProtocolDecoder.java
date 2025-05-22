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
/*     */ public class TlvProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TlvProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, String type, String... arguments) {
/*  39 */     if (channel != null) {
/*  40 */       ByteBuf response = Unpooled.buffer();
/*  41 */       response.writeCharSequence(type, StandardCharsets.US_ASCII);
/*  42 */       for (String argument : arguments) {
/*  43 */         response.writeByte(argument.length());
/*  44 */         response.writeCharSequence(argument, StandardCharsets.US_ASCII);
/*     */       } 
/*  46 */       response.writeByte(0);
/*  47 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String readArgument(ByteBuf buf) {
/*  52 */     return buf.readSlice(buf.readUnsignedByte()).toString(StandardCharsets.US_ASCII);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  59 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  61 */     String type = buf.readSlice(2).toString(StandardCharsets.US_ASCII);
/*     */     
/*  63 */     if (channel != null) {
/*  64 */       switch (type) {
/*     */         case "0A":
/*     */         case "0C":
/*  67 */           sendResponse(channel, remoteAddress, type, new String[0]);
/*     */           break;
/*     */         case "0B":
/*  70 */           sendResponse(channel, remoteAddress, type, new String[] { "1482202689", "10", "20", "15" });
/*     */           break;
/*     */         case "0E":
/*     */         case "0F":
/*  74 */           sendResponse(channel, remoteAddress, type, new String[] { "30", "Unknown" });
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */     
/*     */     }
/*  81 */     if (type.equals("0E")) {
/*     */       
/*  83 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { readArgument(buf) });
/*  84 */       if (deviceSession == null) {
/*  85 */         return null;
/*     */       }
/*     */       
/*  88 */       Position position = new Position(getProtocolName());
/*  89 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  91 */       position.setValid(true);
/*  92 */       position.setTime(new Date(Long.parseLong(readArgument(buf)) * 1000L));
/*     */       
/*  94 */       readArgument(buf);
/*     */       
/*  96 */       position.setLongitude(Double.parseDouble(readArgument(buf)));
/*  97 */       position.setLatitude(Double.parseDouble(readArgument(buf)));
/*  98 */       position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(readArgument(buf))));
/*  99 */       position.setCourse(Double.parseDouble(readArgument(buf)));
/*     */       
/* 101 */       position.set("sat", Integer.valueOf(Integer.parseInt(readArgument(buf))));
/*     */       
/* 103 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 107 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TlvProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */