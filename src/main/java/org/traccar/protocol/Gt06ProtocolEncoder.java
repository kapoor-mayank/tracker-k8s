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
/*    */ public class Gt06ProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(long deviceId, String content) {
/* 31 */     boolean language = Context.getIdentityManager().lookupAttributeBoolean(deviceId, "gt06.language", false, true);
/*    */     
/* 33 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 35 */     buf.writeByte(120);
/* 36 */     buf.writeByte(120);
/*    */     
/* 38 */     buf.writeByte(6 + content.length() + 2 + 2 + (language ? 2 : 0));
/*    */     
/* 40 */     buf.writeByte(128);
/*    */     
/* 42 */     buf.writeByte(4 + content.length());
/* 43 */     buf.writeInt(0);
/* 44 */     buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
/*    */     
/* 46 */     if (language) {
/* 47 */       buf.writeShort(2);
/*    */     }
/*    */     
/* 50 */     buf.writeShort(0);
/*    */     
/* 52 */     buf.writeShort(Checksum.crc16(Checksum.CRC16_X25, buf.nioBuffer(2, buf.writerIndex() - 2)));
/*    */     
/* 54 */     buf.writeByte(13);
/* 55 */     buf.writeByte(10);
/*    */     
/* 57 */     return buf;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 63 */     boolean alternative = Context.getIdentityManager().lookupAttributeBoolean(command
/* 64 */         .getDeviceId(), "gt06.alternative", false, true);
/*    */     
/* 66 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 68 */         return encodeContent(command.getDeviceId(), alternative ? "DYD,123456#" : "Relay,1#");
/*    */       case "engineResume":
/* 70 */         return encodeContent(command.getDeviceId(), alternative ? "HFYD,123456#" : "Relay,0#");
/*    */       case "custom":
/* 72 */         return encodeContent(command.getDeviceId(), command.getString("data"));
/*    */     } 
/* 74 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gt06ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */