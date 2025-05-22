/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.StringProtocolEncoder;
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
/*    */ public class EnforaProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(String content) {
/* 30 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 32 */     buf.writeShort(content.length() + 6);
/* 33 */     buf.writeShort(0);
/* 34 */     buf.writeByte(4);
/* 35 */     buf.writeByte(0);
/* 36 */     buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
/*    */     
/* 38 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 43 */     switch (command.getType()) {
/*    */       case "custom":
/* 45 */         return encodeContent(command.getString("data"));
/*    */       case "engineStop":
/* 47 */         return encodeContent("AT$IOGP3=1");
/*    */       case "engineResume":
/* 49 */         return encodeContent("AT$IOGP3=0");
/*    */     } 
/* 51 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EnforaProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */