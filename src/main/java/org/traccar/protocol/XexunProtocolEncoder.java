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
/*    */ public class XexunProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     initDevicePassword(command, "123456");
/*    */     
/* 28 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 30 */         return formatCommand(command, "powercar{%s} 11", new String[] { "devicePassword" });
/*    */       case "engineResume":
/* 32 */         return formatCommand(command, "powercar{%s} 00", new String[] { "devicePassword" });
/*    */     } 
/* 34 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\XexunProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */