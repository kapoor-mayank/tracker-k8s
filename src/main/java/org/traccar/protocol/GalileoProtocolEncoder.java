/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocolEncoder;
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
/*    */ public class GalileoProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeText(String uniqueId, String text) {
/* 30 */     ByteBuf buf = Unpooled.buffer(256);
/*    */     
/* 32 */     buf.writeByte(1);
/* 33 */     buf.writeShortLE(uniqueId.length() + text.length() + 11);
/*    */     
/* 35 */     buf.writeByte(3);
/* 36 */     buf.writeBytes(uniqueId.getBytes(StandardCharsets.US_ASCII));
/*    */     
/* 38 */     buf.writeByte(4);
/* 39 */     buf.writeShortLE(0);
/*    */     
/* 41 */     buf.writeByte(224);
/* 42 */     buf.writeIntLE(0);
/*    */     
/* 44 */     buf.writeByte(225);
/* 45 */     buf.writeByte(text.length());
/* 46 */     buf.writeBytes(text.getBytes(StandardCharsets.US_ASCII));
/*    */     
/* 48 */     buf.writeShortLE(Checksum.crc16(Checksum.CRC16_MODBUS, buf.nioBuffer(0, buf.writerIndex())));
/*    */     
/* 50 */     return buf;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 56 */     switch (command.getType()) {
/*    */       case "custom":
/* 58 */         return encodeText(getUniqueId(command.getDeviceId()), command.getString("data"));
/*    */       case "outputControl":
/* 60 */         return encodeText(getUniqueId(command.getDeviceId()), "Out " + command
/* 61 */             .getInteger("index") + "," + command.getString("data"));
/*    */     } 
/* 63 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GalileoProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */