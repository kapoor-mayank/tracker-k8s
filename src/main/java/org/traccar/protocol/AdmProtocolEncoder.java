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
/*    */ public class AdmProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 27 */     switch (command.getType()) {
/*    */       case "getDeviceStatus":
/* 29 */         return formatCommand(command, "STATUS\r\n", new String[0]);
/*    */       
/*    */       case "custom":
/* 32 */         return formatCommand(command, "{%s}\r\n", new String[] { "data" });
/*    */     } 
/*    */     
/* 35 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AdmProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */