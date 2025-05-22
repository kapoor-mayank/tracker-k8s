/*    */ package org.traccar.protocol;
/*    */ 
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
/*    */ public class GranitProtocolSmsEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected String encodeCommand(Command command) {
/* 26 */     switch (command.getType()) {
/*    */       case "rebootDevice":
/* 28 */         return "BB+RESET";
/*    */       case "positionPeriodic":
/* 30 */         return formatCommand(command, "BB+BBMD={%s}", new String[] { "frequency" });
/*    */     } 
/* 32 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GranitProtocolSmsEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */