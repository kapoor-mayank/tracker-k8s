/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocolEncoder;
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
/*    */ public class NoranProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(String content) {
/* 29 */     ByteBuf buf = Unpooled.buffer(68);
/*    */     
/* 31 */     buf.writeCharSequence("\r\n*KW", StandardCharsets.US_ASCII);
/* 32 */     buf.writeByte(0);
/* 33 */     buf.writeShortLE(buf.capacity());
/* 34 */     buf.writeShortLE(2);
/* 35 */     buf.writeInt(0);
/* 36 */     buf.writeShortLE(0);
/* 37 */     buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
/* 38 */     buf.writerIndex(buf.writerIndex() + 50 - content.length());
/* 39 */     buf.writeCharSequence("\r\n", StandardCharsets.US_ASCII);
/*    */     
/* 41 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     int interval;
/* 47 */     switch (command.getType()) {
/*    */       case "positionSingle":
/* 49 */         return encodeContent("*KW,000,000,000000#");
/*    */       case "positionPeriodic":
/* 51 */         interval = command.getInteger("frequency");
/* 52 */         return encodeContent("*KW,000,002,000000," + interval + "#");
/*    */       case "positionStop":
/* 54 */         return encodeContent("*KW,000,002,000000,0#");
/*    */       case "engineStop":
/* 56 */         return encodeContent("*KW,000,007,000000,0#");
/*    */       case "engineResume":
/* 58 */         return encodeContent("*KW,000,007,000000,1#");
/*    */     } 
/* 60 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NoranProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */