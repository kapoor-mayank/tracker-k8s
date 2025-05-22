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
/*    */ 
/*    */ public class TotemProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 27 */     initDevicePassword(command, "000000");
/*    */     
/* 29 */     switch (command.getType()) {
/*    */       
/*    */       case "engineStop":
/* 32 */         return formatCommand(command, "*{%s},025,C,1#", new String[] { "devicePassword" });
/*    */       case "engineResume":
/* 34 */         return formatCommand(command, "*{%s},025,C,0#", new String[] { "devicePassword" });
/*    */     } 
/* 36 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TotemProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */