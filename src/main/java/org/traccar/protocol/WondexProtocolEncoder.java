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
/*    */ public class WondexProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     initDevicePassword(command, "0000");
/*    */     
/* 28 */     switch (command.getType()) {
/*    */       case "rebootDevice":
/* 30 */         return formatCommand(command, "$WP+REBOOT={%s}", new String[] { "devicePassword" });
/*    */       case "getDeviceStatus":
/* 32 */         return formatCommand(command, "$WP+TEST={%s}", new String[] { "devicePassword" });
/*    */       case "getModemStatus":
/* 34 */         return formatCommand(command, "$WP+GSMINFO={%s}", new String[] { "devicePassword" });
/*    */       case "deviceIdentification":
/* 36 */         return formatCommand(command, "$WP+IMEI={%s}", new String[] { "devicePassword" });
/*    */       case "positionSingle":
/* 38 */         return formatCommand(command, "$WP+GETLOCATION={%s}", new String[] { "devicePassword" });
/*    */       case "getVersion":
/* 40 */         return formatCommand(command, "$WP+VER={%s}", new String[] { "devicePassword" });
/*    */     } 
/* 42 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WondexProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */