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
/*    */ public class EsealProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     switch (command.getType()) {
/*    */       case "custom":
/* 28 */         return formatCommand(command, "##S,eSeal,{%s},256,3.0.8,{%s},E##", new String[] { "uniqueId", "data" });
/*    */       
/*    */       case "alarmArm":
/* 31 */         return formatCommand(command, "##S,eSeal,{%s},256,3.0.8,RC-Power Control,Power OFF,E##", new String[] { "uniqueId" });
/*    */       
/*    */       case "alarmDisarm":
/* 34 */         return formatCommand(command, "##S,eSeal,{%s},256,3.0.8,RC-Unlock,E##", new String[] { "uniqueId" });
/*    */     } 
/*    */     
/* 37 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EsealProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */