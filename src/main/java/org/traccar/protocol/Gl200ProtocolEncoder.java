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
/*    */ public class Gl200ProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     initDevicePassword(command, "");
/*    */     
/* 28 */     switch (command.getType()) {
/*    */       case "positionSingle":
/* 30 */         return formatCommand(command, "AT+GTRTO={%s},1,,,,,,FFFF$", new String[] { "devicePassword" });
/*    */       case "engineStop":
/* 32 */         return formatCommand(command, "AT+GTOUT={%s},1,,,0,0,0,0,0,0,0,,,,,,,FFFF$", new String[] { "devicePassword" });
/*    */       
/*    */       case "engineResume":
/* 35 */         return formatCommand(command, "AT+GTOUT={%s},0,,,0,0,0,0,0,0,0,,,,,,,FFFF$", new String[] { "devicePassword" });
/*    */       
/*    */       case "deviceIdentification":
/* 38 */         return formatCommand(command, "AT+GTRTO={%s},8,,,,,,FFFF$", new String[] { "devicePassword" });
/*    */       case "rebootDevice":
/* 40 */         return formatCommand(command, "AT+GTRTO={%s},3,,,,,,FFFF$", new String[] { "devicePassword" });
/*    */     } 
/* 42 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl200ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */