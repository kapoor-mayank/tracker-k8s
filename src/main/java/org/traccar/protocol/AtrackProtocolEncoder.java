/*    */ package org.traccar.protocol;
/*    */ 
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
/*    */ 
/*    */ public class AtrackProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 29 */     switch (command.getType()) {
/*    */       case "custom":
/* 31 */         return Unpooled.copiedBuffer(command
/* 32 */             .getString("data") + "\r\n", StandardCharsets.US_ASCII);
/*    */     } 
/* 34 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AtrackProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */