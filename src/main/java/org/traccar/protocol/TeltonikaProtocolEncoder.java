/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocolEncoder;
/*    */ import org.traccar.helper.Checksum;
/*    */ import org.traccar.helper.DataConverter;
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
/*    */ 
/*    */ public class TeltonikaProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(String type, byte[] content) {
/* 32 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 34 */     buf.writeInt(0);
/* 35 */     buf.writeInt(content.length + 8);
/* 36 */     buf.writeByte(12);
/* 37 */     buf.writeByte(1);
/* 38 */     if (type.equals("serial")) {
/* 39 */       buf.writeByte(14);
/*    */     } else {
/* 41 */       buf.writeByte(5);
/*    */     } 
/* 43 */     buf.writeInt(content.length);
/* 44 */     buf.writeBytes(content);
/* 45 */     buf.writeByte(1);
/* 46 */     buf.writeInt(Checksum.crc16(Checksum.CRC16_IBM, buf.nioBuffer(8, buf.writerIndex() - 8)));
/*    */     
/* 48 */     return buf;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 54 */     if (command.getType().equals("custom") || command.getType().equals("serial")) {
/* 55 */       String data = command.getString("data");
/* 56 */       if (data.matches("(\\p{XDigit}{2})+")) {
/* 57 */         return encodeContent(command.getType(), DataConverter.parseHex(data));
/*    */       }
/* 59 */       return encodeContent(command.getType(), (data + "\r\n").getBytes(StandardCharsets.US_ASCII));
/*    */     } 
/*    */     
/* 62 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TeltonikaProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */