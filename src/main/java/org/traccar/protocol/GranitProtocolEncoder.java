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
/*    */ public class GranitProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeCommand(String commandString) {
/* 28 */     ByteBuf buffer = Unpooled.buffer();
/* 29 */     buffer.writeBytes(commandString.getBytes(StandardCharsets.US_ASCII));
/* 30 */     GranitProtocolDecoder.appendChecksum(buffer, commandString.length());
/* 31 */     return buffer;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 36 */     switch (command.getType()) {
/*    */       case "deviceIdentification":
/* 38 */         return encodeCommand("BB+IDNT");
/*    */       case "rebootDevice":
/* 40 */         return encodeCommand("BB+RESET");
/*    */       case "positionSingle":
/* 42 */         return encodeCommand("BB+RRCD");
/*    */     } 
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GranitProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */