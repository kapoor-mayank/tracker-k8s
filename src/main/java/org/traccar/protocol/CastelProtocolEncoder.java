/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocolEncoder;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.helper.Checksum;
/*    */ import org.traccar.model.Command;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class CastelProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(long deviceId, short type, ByteBuf content) {
/* 31 */     ByteBuf buf = Unpooled.buffer(0);
/* 32 */     String uniqueId = Context.getIdentityManager().getById(deviceId).getUniqueId();
/*    */     
/* 34 */     buf.writeByte(64);
/* 35 */     buf.writeByte(64);
/*    */     
/* 37 */     buf.writeShortLE(27 + content.readableBytes() + 2 + 2);
/*    */     
/* 39 */     buf.writeByte(1);
/*    */     
/* 41 */     buf.writeBytes(uniqueId.getBytes(StandardCharsets.US_ASCII));
/* 42 */     buf.writeZero(20 - uniqueId.length());
/*    */     
/* 44 */     buf.writeShort(type);
/* 45 */     buf.writeBytes(content);
/*    */     
/* 47 */     buf.writeShortLE(Checksum.crc16(Checksum.CRC16_X25, buf.nioBuffer()));
/*    */     
/* 49 */     buf.writeByte(13);
/* 50 */     buf.writeByte(10);
/*    */     
/* 52 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 57 */     ByteBuf content = Unpooled.buffer(0);
/* 58 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 60 */         content.writeByte(1);
/* 61 */         return encodeContent(command.getDeviceId(), (short)17795, content);
/*    */       case "engineResume":
/* 63 */         content.writeByte(0);
/* 64 */         return encodeContent(command.getDeviceId(), (short)17795, content);
/*    */     } 
/* 66 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CastelProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */