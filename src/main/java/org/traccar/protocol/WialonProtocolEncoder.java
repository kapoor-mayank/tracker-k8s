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
/*    */ public class WialonProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     switch (command.getType()) {
/*    */       case "rebootDevice":
/* 28 */         return formatCommand(command, "reboot\r\n", new String[0]);
/*    */       case "sendUssd":
/* 30 */         return formatCommand(command, "USSD:{%s}\r\n", new String[] { "phone" });
/*    */       case "deviceIdentification":
/* 32 */         return formatCommand(command, "VER?\r\n", new String[0]);
/*    */       case "outputControl":
/* 34 */         return formatCommand(command, "L{%s}={%s}\r\n", new String[] { "index", "data" });
/*    */     } 
/* 36 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WialonProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */