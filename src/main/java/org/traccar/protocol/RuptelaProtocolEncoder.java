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
/*    */ public class RuptelaProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   public static ByteBuf encodeContent(int type, ByteBuf content) {
/* 30 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 32 */     buf.writeShort(1 + content.readableBytes());
/* 33 */     buf.writeByte(100 + type);
/* 34 */     buf.writeBytes(content);
/* 35 */     buf.writeShort(Checksum.crc16(Checksum.CRC16_KERMIT, buf.nioBuffer(2, buf.writerIndex() - 2)));
/*    */     
/* 37 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     String c;
/* 43 */     ByteBuf content = Unpooled.buffer();
/*    */     
/* 45 */     switch (command.getType()) {
/*    */       case "custom":
/* 47 */         content.writeBytes(command.getString("data").getBytes(StandardCharsets.US_ASCII));
/* 48 */         return encodeContent(8, content);
/*    */       case "requestPhoto":
/* 50 */         content.writeByte(1);
/* 51 */         content.writeByte(0);
/* 52 */         content.writeInt(0);
/* 53 */         content.writeInt(2147483647);
/* 54 */         return encodeContent(37, content);
/*    */       case "configuration":
/* 56 */         content.writeBytes((command.getString("data") + "\r\n").getBytes(StandardCharsets.US_ASCII));
/* 57 */         return encodeContent(2, content);
/*    */       case "getVersion":
/* 59 */         return encodeContent(3, content);
/*    */       case "firmwareUpdate":
/* 61 */         content.writeBytes("|FU_STRT*\r\n".getBytes(StandardCharsets.US_ASCII));
/* 62 */         return encodeContent(4, content);
/*    */       case "outputControl":
/* 64 */         content.writeInt(command.getInteger("index"));
/* 65 */         content.writeInt(Integer.parseInt(command.getString("data")));
/* 66 */         return encodeContent(17, content);
/*    */       case "setConnection":
/* 68 */         c = command.getString("server") + "," + command.getInteger("port") + ",TCP";
/* 69 */         content.writeBytes(c.getBytes(StandardCharsets.US_ASCII));
/* 70 */         return encodeContent(5, content);
/*    */       case "setOdometer":
/* 72 */         content.writeInt(Integer.parseInt(command.getString("data")));
/* 73 */         return encodeContent(6, content);
/*    */     } 
/* 75 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RuptelaProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */